// https://developer.mozilla.org/en-US/docs/Web/API/HTMLAudioElement
// https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API

/**
 * Get a signed URL for accessing a track's audio data.
 * @param {string} trackID
 * @returns {Promise<string>}
 */
async function getSignedURL(trackID) {
  const response = await fetch(`/api/v1/track/${trackID}/sign`, {
    method: "POST",
  });
  const data = await response.json();
  return data["signed-url"];
}

/**
 * Will hold the currently playing track element (used for auto-play).
 * @type {HTMLElement | null}
 */
let trackElement = null;

let elapsed = 0;
let duration = 0;

let shuffle = false;
let repeat = false;

/**
 * @type {HTMLAudioElement}
 */
const audioElement = document.querySelector("#audio");

/**
 * @type {HTMLInputElement}
 */
const volumeElement = document.querySelector("#volume");
volumeElement.value = audioElement.volume;

/**
 * @type {HTMLButtonElement}
 */
const playButton = document.querySelector("#play");

/**
 * @type {HTMLButtonElement}
 */
const nextButton = document.querySelector("#next");

/**
 * @type {HTMLButtonElement}
 */
const prevButton = document.querySelector("#prev");

/**
 * @type {HTMLButtonElement}
 */
const shuffleButton = document.querySelector("#shuffle");

/**
 * @type {HTMLButtonElement}
 */
const repeatButton = document.querySelector("#repeat");

const titleElement = document.querySelector("#title");
const artistElement = document.querySelector("#artist");
const elapsedElement = document.querySelector("#elapsed");
const remainingElement = document.querySelector("#remaining");
const barElement = document.querySelector("#bar");
const filledElement = document.querySelector("#filled");
const allTracks = document.querySelectorAll(".track");

/**
 * @param {HTMLElement} trackElement
 * @returns {string}
 */
function getTrackTitle(trackElement) {
  return trackElement.querySelector(".title").textContent ?? "Unknown Track";
}

/**
 * @param {HTMLElement} trackElement
 * @returns {string}
 */
function getTrackArtist(trackElement) {
  return trackElement.querySelector(".artist").textContent ?? "Unknown Artist";
}

/**
 * @param {number} duration
 * @returns {string}
 */
function formatDurationAsMinutesSeconds(duration) {
  const minutes = Math.floor(duration / 60);
  const seconds = Math.floor(duration % 60);
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

/**
 * Play the currently selected track and update titles.
 */
async function playTrack() {
  if (!trackElement) {
    return;
  }

  const trackID = trackElement.querySelector("input").value;
  const url = await getSignedURL(trackID);

  audioElement.src = url;
  audioElement.load();
  audioElement.play();

  playButton.childNodes[0].src = "/img/pause.svg";

  // Grab the track's title and update the player title and page title.
  const title = getTrackTitle(trackElement);
  titleElement.innerHTML = title;
  document.title = title;

  // Grab the track's artist and update the player artist.
  const artist = getTrackArtist(trackElement);
  artistElement.innerHTML = artist;

  // Reset the time display and progress bar.
  elapsedElement.innerHTML = "0:00";
  remainingElement.innerHTML = "0:00";
  filledElement.style.width = "0%";
}

/**
 * Reset the player to its initial state.
 */
async function resetTrack() {
  audioElement.src = "";
  audioElement.load();
  audioElement.pause();

  // Reset the player title and page title.
  playButton.childNodes[0].src = "/img/play.svg";
  titleElement.innerHTML = "DerzTunes";
  document.title = "DerzTunes";

  // Reset the player artist.
  artistElement.innerHTML = "Click on any track to start playing...";

  // Reset the time display and progress bar.
  elapsed = 0;
  duration = 0;
  elapsedElement.innerHTML = formatDurationAsMinutesSeconds(elapsed);
  remainingElement.innerHTML = formatDurationAsMinutesSeconds(duration);
  filledElement.style.width = "0%";
}

/**
 * Select and play the next track in the list.
 */
async function playNextTrack() {
  if (!trackElement) {
    return;
  }

  /**
   * @type {HTMLElement | null}
   */
  let next = null;
  if (repeat) {
    // If repeating, just play the current track again.
    next = trackElement;
  } else if (shuffle) {
    // If shuffling, get a random track element.
    next = allTracks[Math.floor(Math.random() * allTracks.length)];
  } else {
    // Get the next track element (in the current order).
    next = trackElement.nextElementSibling;
  }

  if (!next) {
    await resetTrack();
    return;
  }

  trackElement = next;
  await playTrack();
}

/**
 * Select and play the previous track in the list.
 *
 * TODO: How does this work with shuffle? Keep a history of played tracks?
 * TODO: How does this work with repeat? Keep a history of played tracks?
 */
async function playPrevTrack() {
  if (!trackElement) {
    return;
  }

  // Get the previous track element (in the current order).
  const prev = trackElement.previousElementSibling;
  if (!prev) {
    await resetTrack();
    return;
  }

  trackElement = prev;
  await playTrack();
}

// Handle what happens when a track finishes playing.
audioElement.addEventListener("ended", async () => {
  await playNextTrack();
});

// Handle what happens when a track's duration changes.
audioElement.addEventListener("durationchange", async (event) => {
  if (!audioElement.duration) {
    return;
  }

  duration = audioElement.duration;
  remainingElement.innerHTML = "-" + formatDurationAsMinutesSeconds(duration);
});

// Handle what happens when a track makes progress.
audioElement.addEventListener("timeupdate", async () => {
  elapsed = audioElement.currentTime;
  elapsedElement.innerHTML = formatDurationAsMinutesSeconds(elapsed);

  if (!duration) {
    return;
  }

  // Calculate the remaining time.
  const remaining = duration - elapsed;
  remainingElement.innerHTML = "-" + formatDurationAsMinutesSeconds(remaining);

  // Update the progress bar.
  const percent = (elapsed / duration) * 100;
  filledElement.style.width = percent + "%";
});

// Handle what happens when the volume changes.
volumeElement.addEventListener("input", async (event) => {
  audioElement.volume = event.target.value;
});

// Handle what happens when a user cllcks on the play / pause button.
playButton.addEventListener(
  "click",
  () => {
    if (!trackElement) {
      return;
    }

    // Toggle the play / pause state.
    if (audioElement.paused) {
      audioElement.play();
      playButton.childNodes[0].src = "/img/pause.svg";
    } else {
      audioElement.pause();
      playButton.childNodes[0].src = "/img/play.svg";
    }
  },
  false
);

// Handle what happens when a user cllcks on the "next track" button.
nextButton.addEventListener("click", async () => {
  if (!trackElement) {
    return;
  }

  await playNextTrack();
});

// Handle what happens when a user cllcks on the "prev track" button.
prevButton.addEventListener("click", async () => {
  if (!trackElement) {
    return;
  }

  await playPrevTrack();
});

// Handle what happens when a user clicks on the progress bar.
barElement.addEventListener("click", async (event) => {
  if (!trackElement) {
    return;
  }

  // Calculate the elapsed time based on the click position.
  const rect = event.target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const width = rect.width;
  const percent = x / width;

  // Update the progress bar.
  filledElement.style.width = percent * 100 + "%";

  // Calculate the elapsed time and update the time display.
  elapsed = percent * duration;
  elapsedElement.innerHTML = formatDurationAsMinutesSeconds(elapsed);

  // Calculate the remaining time and update the time display.
  const remaining = duration - elapsed;
  remainingElement.innerHTML = "-" + formatDurationAsMinutesSeconds(remaining);

  // Update the audio element's currentTime (this actually changes the player's state).
  audioElement.currentTime = elapsed;
});

// Handle what happens when a user clicks on a track.
document.querySelectorAll(".track").forEach((e) => {
  e.addEventListener("click", async (e) => {
    trackElement = e.target.parentElement;
    await playTrack();
  });
});

// Handle what happens when a user clicks on the "shuffle" button.
shuffleButton.addEventListener("click", async () => {
  shuffle = !shuffle;
  if (shuffle) {
    shuffleButton.innerHTML = "Shuffle: On";
  } else {
    shuffleButton.innerHTML = "Shuffle: Off";
  }
});

// Handle what happens when a user clicks on the "shuffle" button.
repeatButton.addEventListener("click", async () => {
  repeat = !repeat;
  if (repeat) {
    repeatButton.innerHTML = "Repeat: On";
  } else {
    repeatButton.innerHTML = "Repeat: Off";
  }
});
