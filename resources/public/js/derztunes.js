console.log('Hello from DerzTunes!');

// https://developer.mozilla.org/en-US/docs/Web/API/HTMLAudioElement
// https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API

/**
 * @type {HTMLAudioElement | null}
 */
let audioElement = null;

document.querySelectorAll('.track').forEach((e) => {
	e.addEventListener('click', (e) => {
		if (audioElement) {
			audioElement.pause();
			audioElement.remove();
		}

		// TODO: This feels hacky. Is there a better way to get the URL?
		const url = e.target.childNodes[1].src;
		audioElement = new Audio(url);
		audioElement.play();
	});
});

const playButton = document.querySelector("button");
playButton.addEventListener(
  "click",
  () => {
	if (audioElement === null) {
		console.log('No track selected, return.');
		return;
	}

	if (audioElement.paused) {
		audioElement.play();
	} else {
		audioElement.pause();
	}
  },
  false,
);
