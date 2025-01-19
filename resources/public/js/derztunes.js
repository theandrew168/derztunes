console.log("Hello from DerzTunes!");

// https://developer.mozilla.org/en-US/docs/Web/API/HTMLAudioElement
// https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API

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
  e.addEventListener("click", (e) => {
    if (audioElement) {
      audioElement.pause();
      audioElement.remove();
    }

    const title = e.target.childNodes[0].textContent;
    const url = e.target.childNodes[1].src;
    audioElement = new Audio(url);
    audioElement.play();

    statusText.innerHTML = title;
    playButton.innerHTML = "Pause";
  });
});
