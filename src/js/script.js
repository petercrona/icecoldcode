'use strict';

(function postList() {
    const list = document.querySelector("#latestArticlesList");

    if (!list) {
	return;
    }
    
    let page = 0;
    const showPerPage = 4;
    const n = list.children.length;
    const lastPage = Math.floor(n / showPerPage);
    
    if (lastPage === 0) {
	return;
    }
    
    const hideAll = () => {
	for (let i = 0; i < n; i++) {
	    list.children[i].hidden = true;
	}
    }

    // add buttons
    const prev = document.createElement("button");
    prev.onclick = () => {
	showNodes(--page);
    };
    prev.innerText = "Previous";
    
    const next = document.createElement("button");
    next.onclick = () => {
	showNodes(++page);
    };
    next.innerText = "Next";
    
    const showNodes = (pageNr) => {
	hideAll();
	const start = pageNr * showPerPage;
	for (let i = start; i < Math.min(start + showPerPage, n); i++) {
	    list.children[i].hidden = false;
	}

	prev.disabled = false;
	next.disabled = false;
	
	if (pageNr === 0) {
	    prev.disabled = true;
	} else if (pageNr === lastPage) {
	    next.disabled = true;
	}

	const newHeight = Math.max(
	    list.scrollHeight,
	    parseInt(list.style.height) || 0
	);

	list.style.height = `${newHeight}px`;
    }

    showNodes(0);

    const buttonContainer = document.createElement("div");
    buttonContainer.id = "latestArticleListNav";
    buttonContainer.appendChild(prev);
    buttonContainer.appendChild(next);
    list.parentElement.appendChild(buttonContainer);
})();

(function footer() {
    const mainNavButton = document.querySelector("#mainNavButtonContainer > button");
    const mainNavContainer = document.querySelector("#mainNavContainer");
    const hamburger = document.querySelector(".hamburger-icon");
    const header = document.querySelector("footer");

    mainNavContainer.style.height = '0px';

    let state = 'initial';
    let block = false;

    const handleCloseMenu = () => {
	state = 'initial';
	hamburger.classList.remove('hamburger-icon-open');
	header.classList.remove('open');
	mainNavContainer.style.height = '0px';
    };

    const openWithHover = (ev) => {
	if (block
            || !ev.relatedTarget
	    || header.contains(ev.relatedTarget)
	    || ev.relatedTarget === document.querySelector("html")
	   ) {
	    return;
	}
	
	block = true;

	if (state === 'initial' && !header.classList.contains('open')) {
	    hamburger.classList.add('hamburger-icon-open');
	    header.classList.add('open');
	    mainNavContainer.style.height = `${mainNavContainer.scrollHeight}px`;
	}

	setTimeout(() => {
	    block = false;
	});
    };


    const toggleWithClick = () => {
	if (block) {
	    return;
	}

	block = true;
	
	if (header.classList.contains('open')) {
	    state = 'closedByClick';
	}

	hamburger.classList.toggle('hamburger-icon-open');
	header.classList.toggle('open');
	if (mainNavContainer.style.height === '0px') {
	    mainNavContainer.style.height = `${mainNavContainer.scrollHeight}px`;
	} else {
	    mainNavContainer.style.height = '0px';
	}

	setTimeout(() => {
	    block = false;
	});
    };

    header.addEventListener('mouseover', openWithHover);
    mainNavButton.addEventListener('click', toggleWithClick);
    header.addEventListener('mouseleave', handleCloseMenu);
})();

(function navScrollBehavior() {
    let accumulated = 0;
    let lastScrollPos = window.scrollY;
    const footer = document.querySelector("footer");
    const footerHeight = footer.scrollHeight;
    
    document.addEventListener("scroll", (event) => {
	const diff = window.scrollY - lastScrollPos;
	accumulated = Math.min(Math.max(0, accumulated + diff), footerHeight);
	lastScrollPos = window.scrollY;
	footer.style.bottom = `${-accumulated}px`;
    });
})();
