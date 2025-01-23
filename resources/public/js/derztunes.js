console.log("Hello from DerzTunes!");

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

/**
 * @type {HTMLAudioElement}
 */
const audioElement = document.querySelector("#audio");
audioElement.addEventListener("ended", () => {
  if (trackElement === null) {
    return;
  }

  // Get the next track element (in the current order).
  const next = trackElement.nextElementSibling;
  if (next === null) {
    return;
  }

  // Simulate a click on the next track.
  next.click();
});

const titleElement = document.querySelector("#title");

// Handle what happens when a user cllcks on the play / pause button.
const playButton = document.querySelector("#player");
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

// Handle what happens when a user clicks on a track.
document.querySelectorAll(".track").forEach((e) => {
  e.addEventListener("click", async (e) => {
    trackElement = e.target;

    const trackID = e.target.querySelector("input").value;
    const url = await getSignedURL(trackID);

    audioElement.src = url;
    audioElement.load();
    audioElement.play();

    playButton.innerHTML = "Pause";

    const title = e.target?.textContent ?? "Unknown Track";
    titleElement.innerHTML = title;
  });
});
