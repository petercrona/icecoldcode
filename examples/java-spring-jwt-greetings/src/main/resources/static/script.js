import { mkApi, formDataToJson } from "/util.js";

const authApi = mkApi("", "/auth");
const authUsersApi = mkApi("/users", "/auth");
const greetingsApi = mkApi("/v1/greetings");

// Boot

const headerController = setupHeader();
setupLoginHeader();
setupAuthenticatedHeader();
const greetingListController = setupGreetings();
const greetingDialogController = setupGreetingDialog();

document.addEventListener("authEvent", function (event) {
  const authUser = event.detail.authUser;

  greetingListController.loadAndRenderGreetings(authUser);
  greetingDialogController.setButtonHidden(
    authUser === null
  );
  greetingDialogController.setAuthUser(authUser);

  if (authUser) {
    headerController.showAuthenticatedHeader(authUser);
  } else {
    headerController.showLoginHeader();
  }
});

authApi.list().then((authUser) => {
  dispatchAuthEvent(authUser);
});

// ==============

function dispatchAuthEvent(authUser) {
  const customEvent = new CustomEvent("authEvent", {
    detail: {
      authUser,
    },
  });
  document.dispatchEvent(customEvent);
};

function setupHeader() {
  const headerLogin = document.querySelector("#headerLogin");
  const headerAuthenticated = document.querySelector(
    "#headerAuthenticated"
  );

  const showLoginHeader = () => {
    headerLogin.hidden = false;
    headerAuthenticated.hidden = true;
  };

  const showAuthenticatedHeader = (authUser) => {
    headerLogin.hidden = true;
    headerAuthenticated.hidden = false;

    headerAuthenticated.querySelector(
      "#headerAuthenticated__name"
    ).textContent = authUser.username;
    headerAuthenticated.querySelector(
      "#headerAuthenticated__company"
    ).textContent = authUser.companyId;
  };

  return { showLoginHeader, showAuthenticatedHeader };
};

function setupLoginHeader() {
  const loginButton = document.querySelector("#loginButton");
  const registerButton = document.querySelector("#registerButton");
  const loginDialog = document.querySelector("#loginDialog");
  const loginForm = loginDialog.querySelector("form");
  const registerDialog = document.querySelector("#registerDialog");
  const registerForm = registerDialog.querySelector("form");

  loginButton.addEventListener("click", () => {
    loginDialog.showModal();
  });

  registerButton.addEventListener("click", () => {
    registerDialog.showModal();
  });

  loginForm.addEventListener("submit", (submitEv) => {
    submitEv.preventDefault();

    const formData = formDataToJson(new FormData(submitEv.target));
    authApi
      .create(formData)
      .then((authUser) => {
        dispatchAuthEvent(authUser);
        submitEv.target.reset();
        loginDialog
          .querySelectorAll(".error")
          .forEach((x) => (x.hidden = true));
        loginDialog.close();
      })
      .catch(() => {
        loginDialog
          .querySelectorAll(".error")
          .forEach((x) => (x.hidden = false));
      });
  });

  registerForm.addEventListener("submit", (submitEv) => {
    submitEv.preventDefault();

    const res = formDataToJson(new FormData(submitEv.target));
    authUsersApi
      .create(res)
      .then(() => {
        submitEv.target.reset();
        registerDialog
          .querySelectorAll(".error")
          .forEach((x) => (x.hidden = true));
        registerDialog.close();
      })
      .catch(() => {
        registerDialog
          .querySelectorAll(".error")
          .forEach((x) => (x.hidden = false));
      });
  });
};

function setupAuthenticatedHeader() {
  const logoutButton = document.querySelector(
    "#headerAuthenticated__logoutButton"
  );

  logoutButton.addEventListener("click", () => {
    authApi.remove().then(() => {
      dispatchAuthEvent(null);
    });
  });
};

function setupGreetings() {
  const list = document.querySelector("#greetingsList");

  const createGreetingLi = (authUser) => (greeting) => {
    const li = document.createElement("li");
    li.classList.add("greeting");

    const topWrapper = document.createElement("div");
    topWrapper.classList.add("topWrapper");

    const message = document.createElement("div");
    message.classList.add("message");

    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Delete";
    deleteButton.hidden = true;
    deleteButton.classList.add("deleteButton");

    if (
      authUser &&
      authUser.companyId === greeting.company &&
      (authUser.isAdmin || authUser.username === greeting.author)
    ) {
      deleteButton.hidden = false;
    }

    deleteButton.addEventListener("click", () => {
      if (window.confirm("You sure?")) {
        greetingsApi.remove(greeting.id).then(() => {
          li.remove();
        });
      }
    });

    const footer = document.createElement("small");

    message.textContent = greeting.message;
    footer.textContent = `By ${greeting.author} in ${greeting.company}`;

    topWrapper.append(message);
    topWrapper.append(deleteButton);
    li.append(topWrapper);
    li.append(footer);

    return li;
  };

  const loadAndRenderGreetings = (authUser) => {
    greetingsApi.list().then((greetings) => {
      list.innerHTML = "";
      greetings
        .map(createGreetingLi(authUser))
        .forEach((li) => list.append(li));
    });
  };

  const addGreeting = (authUser, partialGreeting) => {
    const greeting = {
      ...partialGreeting,
      author: authUser.username,
      company: authUser.companyId,
    };
    list.append(createGreetingLi(authUser)(greeting));
  };

  return { loadAndRenderGreetings, addGreeting };
};

function setupGreetingDialog() {
  let authUser = null;

  const greetingDialog = document.querySelector("#greetingDialog");
  const greetingButton = document.querySelector("#writeGreetingButton");

  greetingButton.hidden = false;
  greetingButton.addEventListener("click", () => {
    greetingDialog.showModal();
  });

  greetingDialog
    .querySelector("form")
    .addEventListener("submit", (ev) => {
      const greetingData = formDataToJson(new FormData(ev.target));
      greetingsApi.create(greetingData).then((idOfGreeting) => {
        const partialGreeting = { id: idOfGreeting, ...greetingData };
        greetingListController.addGreeting(authUser, partialGreeting);
        ev.target.reset();
      });
    });

  return {
    setAuthUser: (newAuthUser) => {
      authUser = newAuthUser;
    },
    setButtonHidden: (hidden) => {
      greetingButton.hidden = hidden;
    },
  };
};