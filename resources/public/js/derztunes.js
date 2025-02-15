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
let shuffle = false;

const allTracks = document.querySelectorAll(".track");
/**
 * @type {HTMLAudioElement}
 */
const audioElement = document.querySelector("#audio");
const titleElement = document.querySelector("#title");
const playButton = document.querySelector("#play");
const nextButton = document.querySelector("#next");
const prevButton = document.querySelector("#prev");
const shuffleButton = document.querySelector("#shuffle");
const repeatButton = document.querySelector("#repeat");

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

  playButton.innerHTML = "Pause";

  // Grab the track's title and update the player title and page title.
  const title =
    trackElement.querySelector("span").textContent ?? "Unknown Track";
  titleElement.innerHTML = title;
  document.title = title;
}

/**
 * Reset the player to its initial state.
 */
async function resetTrack() {
  audioElement.src = "";
  audioElement.load();
  audioElement.pause();

  playButton.innerHTML = "???";
  titleElement.innerHTML = "DerzTunes";
  document.title = "DerzTunes";
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
  if (shuffle) {
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

// Handle what happens when a user cllcks on the play / pause button.
playButton.addEventListener(
  "click",
  () => {
    // Toggle the play / pause state.
    if (audioElement.paused) {
      audioElement.play();
      playButton.innerHTML = "Pause";
    } else {
      audioElement.pause();
      playButton.innerHTML = "Play";
    }
  },
  false
);

// Handle what happens when a user cllcks on the "next track" button.
nextButton.addEventListener("click", async () => {
  await playNextTrack();
});

// Handle what happens when a user cllcks on the "prev track" button.
prevButton.addEventListener("click", async () => {
  await playPrevTrack();
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
