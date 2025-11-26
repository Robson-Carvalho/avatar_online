function handleOpenBlockchain(){
  const user = getUser();
  getHistoryBlockchain(user.id)
}

function handlePlayCard() {
  const user = getUser();
  const match = getMatch();
  playCard(user.id, match.matchId);
}

function handleJoinInQueue() {
  const user = getUser();

  if (user != null) {
    JoinInQueue(user.id);
  }
}

function handleLogout() {
  const user = getUser();

  if (user != null) {
    logoutUser(user.id);
  }
}

function handleLogin(event) {
  event.preventDefault();

  const nickname = document.getElementById("nickname-login").value;
  const password = document.getElementById("password-login").value;

  console.log("Dados do login:", { nickname, password });

  loginUser(nickname, password);
}

function handleRegister(event) {
  event.preventDefault();

  const name = document.getElementById("name-register").value;
  const nickname = document.getElementById("nickname-register").value;
  const email = document.getElementById("email-register").value;
  const password = document.getElementById("password-register").value;

  console.log("Dados de registro:", { name, nickname, email, password });

  if (!name || !nickname || !email || !password) {
    showWarning("Complete todos os campos!");
    return;
  }

  createUser(name, nickname, email, password);
}

function handleOpenPackage() {
  const user = getUser();

  if (user != null) {
    openPackage(user.id);
  }

  openLoadingOpenPack();
}

function handleGetDeck() {
  const user = getUser();

  if (user != null) {
    getDeck(user.id);
  }
}

function handleUpdateDeck() {
  const user = getUser();

  if (user != null) {
    let cards = getDeckInfo();
    updateDeck(
      user.id,
      cards[0].id,
      cards[1].id,
      cards[2].id,
      cards[3].id,
      cards[4].id
    );
  }
}

function handleGetCards() {
  const user = getUser();

  if (user != null) {
    getCards(user.id);
  }
}

function logout() {
  localStorage.removeItem("user_avatar_online");
  updateViewsBasedOnConnection();
}
