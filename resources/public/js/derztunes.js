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
 * @type {HTMLAudioElement | null}
 */
let audioElement = null;

const statusText = document.querySelector("#status");

// Handle what happens when a user cllcks on the play / pause button.
const playButton = document.querySelector("#player");
playButton.addEventListener(
  "click",
  () => {
    if (audioElement === null) {
      console.log("No track selected, return.");
      return;
    }

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
    if (audioElement) {
      audioElement.pause();
      audioElement.remove();
    }

    const input = e.target.querySelector("input");
    const trackID = input.value;
    const url = await getSignedURL(trackID);

    audioElement = new Audio(url);
    audioElement.play();

    const title = e.target.textContent ?? "Unknown Track";
    statusText.innerHTML = title;
    playButton.innerHTML = "Pause";
  });
});
