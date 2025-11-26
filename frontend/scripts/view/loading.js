const divLoadingOpenPack = document.getElementById("loadingOpenPackage");

function openLoadingOpenPack() {
  divLoadingOpenPack.classList.remove("hidden");
}

function closeLoadingOpenPack() {
  divLoadingOpenPack.classList.add("hidden");
}

function updateViewsBasedOnConnection() {
  const isConnected = stompClient !== null && stompClient.connected;

  if (!isConnected) {
    document.getElementById("container-loading").classList.remove("hidden");
    document.getElementById("container-signIn").classList.add("hidden");
    document.getElementById("container-signUp").classList.add("hidden");
    document.getElementById("container-dashboard").classList.add("hidden");
  } else {
    if (localStorage.getItem("user_avatar_online") !== null) {
      document.getElementById("container-signIn").classList.add("hidden");
      document.getElementById("container-loading").classList.add("hidden");
      document.getElementById("container-signUp").classList.add("hidden");
      document.getElementById("container-dashboard").classList.remove("hidden");
    } else {
      document.getElementById("container-signIn").classList.remove("hidden");
      document.getElementById("container-loading").classList.add("hidden");
      document.getElementById("container-signUp").classList.add("hidden");
      document.getElementById("container-dashboard").classList.add("hidden");
    }
  }
}

function showSignIn() {
  if (stompClient && stompClient.connected) {
    document.getElementById("container-signIn").classList.remove("hidden");
    document.getElementById("container-signUp").classList.add("hidden");
    document.getElementById("container-dashboard").classList.add("hidden");
  }
  window.location.reload();
}

function showSignUp() {
  if (stompClient && stompClient.connected) {
    document.getElementById("container-signUp").classList.remove("hidden");
    document.getElementById("container-signIn").classList.add("hidden");
    document.getElementById("container-dashboard").classList.add("hidden");
  }
}

function showDashboard() {
  if (stompClient && stompClient.connected) {
    document.getElementById("user_id").innerText = "user:";
    document.getElementById("container-dashboard").classList.remove("hidden");
    document.getElementById("container-signIn").classList.add("hidden");
    document.getElementById("container-signUp").classList.add("hidden");
  }
}

function openModal(modalId) {
  const overlay = document.getElementById("modal-overlay");
  const modal = document.getElementById(modalId);

  // Mostrar overlay
  overlay.classList.remove("hidden");

  // Mostrar modal
  modal.classList.remove("hidden");

  // Animar entrada
  setTimeout(() => {
    overlay.classList.add("opacity-100");
    modal.classList.remove("scale-95", "opacity-0");
    modal.classList.add("scale-100", "opacity-100");
  }, 10);
}

function closeModal(modalId) {
  const overlay = document.getElementById("modal-overlay");
  const modal = document.getElementById(modalId);

  // Animar saída
  modal.classList.remove("scale-100", "opacity-100");
  modal.classList.add("scale-95", "opacity-0");

  overlay.classList.remove("opacity-100");

  // Esconder após animação
  setTimeout(() => {
    modal.classList.add("hidden");
    overlay.classList.add("hidden");
  }, 300);

  clearDeckSlots();
}

// Fechar modal clicando no overlay
document.getElementById("modal-overlay").addEventListener("click", function () {
  closeAllModals();
});

// Fechar com ESC
document.addEventListener("keydown", function (e) {
  if (e.key === "Escape") {
    closeAllModals();
  }
});

function closeAllModals() {
  const modals = ["deck-modal", "package-modal", "cards-modal"];
  modals.forEach((modalId) => {
    const modal = document.getElementById(modalId);
    if (!modal.classList.contains("hidden")) {
      closeModal(modalId);
    }
  });
  clearDeckSlots();
}
