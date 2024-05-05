const mainNavButton = document.querySelector("#mainNavButtonContainer > button");
const mainNavContainer = document.querySelector("#mainNavContainer");
const hamburger = document.querySelector(".hamburger-icon");
const header = document.querySelector("footer");

mainNavContainer.style.height = '0px';

mainNavButton.addEventListener('click', () => {
    hamburger.classList.toggle('hamburger-icon-open');
    header.classList.toggle('open');
    if (mainNavContainer.style.height === '0px') {
	mainNavContainer.style.height = `${mainNavContainer.scrollHeight}px`;
    } else {
	mainNavContainer.style.height = '0px';
    }
});
