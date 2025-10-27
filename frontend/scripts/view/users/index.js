function sendOperationGetOnlineUsers() {
  const user = getUser();
  if (user) {
    getOnlineUsers(user.id);
  }
}

function renderUsersInDashboard() {
  const onlineUsers = getOnlineUsersLocalStorage();
  const user = getUser();
  const usersContainer = document.getElementById("online-users-container");

  usersContainer.innerHTML = "";

  if (onlineUsers && onlineUsers.length > 1) {
    onlineUsers.forEach((onlineUser) => {
      if (onlineUser.id != user.id) {
        usersContainer.innerHTML += onlineUserTemplate(
          onlineUser.id,
          onlineUser.nickname
        );
      }
    });
  } else {
    usersContainer.innerHTML = `
      <div class="flex justify-center items-center h-full text-xl font-medium text-slate-600">
        Não há usuários online
      </div>
    `;
  }
}

function openExchangeCard(id, nickname) {
  const modal = document.getElementById("exchange-card-modal");
  modal.classList.remove("hidden");
}

function sendExchangeCard(id) {
  const user = getUser();
  const modal = document.getElementById("exchange-card-modal");
  const card1 = document.getElementById("card1");
  const card2 = document.getElementById("card2");

  if (card1.value == card2.value) {
    showWarning("O ID das cartas não podem ser iguais!");
    return;
  }

  if (card1.value == false || card2.value == false) {
    showWarning("Complete todos os campos!");
    return;
  }

  modal.classList.add("hidden");

  let player1Payload = user.id;
  let card1Payload = card1.value;
  let player2Payload = id;
  let card2Payload = card2.value;

  // enviar proposta
  proposolExchangeCard(
    player1Payload,
    card1Payload,
    player2Payload,
    card2Payload
  );

  card1.value = "";
  card2.value = "";
}

function cancelExchangeCard() {
  const modal = document.getElementById("exchange-card-modal");
  modal.classList.add("hidden");
}

function viewCardPlayer(id) {
  // envia requsição
  getCardsPlayerId(id);
}

// quando receber requição abre o modal
function openModalCardsToPlayer(cardsData) {
  const modal = document.getElementById("modal-cards-player");
  const container = document.getElementById("cards-to-player");

  container.innerHTML = "";

  cardsData.forEach((card) => {
    container.innerHTML += cardTemplate(
      card.id,
      card.name,
      card.element,
      card.phase,
      card.attack,
      card.life,
      card.defense,
      card.rarity
    );
  });

  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

document.getElementById("close-modal-cards").addEventListener("click", () => {
  const modal = document.getElementById("modal-cards-player");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
});

document.getElementById("modal-cards-player").addEventListener("click", (e) => {
  if (e.target.id === "modal-cards-player") {
    e.target.classList.add("hidden");
    e.target.classList.remove("flex");
  }
});

// Abre o modal de troca e renderiza os cards
function openModalExchangeCard() {
  const modal = document.getElementById("your-accept-exchange");
  if (!modal) return;

  // Pegar a proposta atual
  const proposal = getPROPOSAL();
  if (!proposal) return;

  // Pegar containers dos cards
  const card1Container = document.getElementById("card1-exchange");
  const card2Container = document.getElementById("card2-exchange");

  // Renderizar card1
  if (card1Container) {
    card1Container.innerHTML = cardTemplate(
      proposal.card1ID,
      proposal.card1Name,
      proposal.card1Element,
      proposal.card1Phase,
      proposal.card1Attack,
      proposal.card1Life,
      proposal.card1Defense,
      proposal.card1Rarity
    );
  }

  // Renderizar card2
  if (card2Container) {
    card2Container.innerHTML = cardTemplate(
      proposal.card2ID,
      proposal.card2Name,
      proposal.card2Element,
      proposal.card2Phase,
      proposal.card2Attack,
      proposal.card2Life,
      proposal.card2Defense,
      proposal.card2Rarity
    );
  }

  // Mostrar modal
  modal.classList.remove("hidden");
}

// Fecha o modal e limpa os cards
function cancelToServerExchangeCard() {
  const modal = document.getElementById("your-accept-exchange");
  if (!modal) return;

  // Esconder modal
  modal.classList.add("hidden");

  // Limpar containers
  const card1Container = document.getElementById("card1-exchange");
  const card2Container = document.getElementById("card2-exchange");
  if (card1Container) card1Container.innerHTML = "";
  if (card2Container) card2Container.innerHTML = "";
}

// Envia a troca aceita para o servidor
function sendToServerExchangeCard() {
  const proposal = getPROPOSAL();

  if (!proposal) return;
  
  exchangeCard(
    proposal.player1,
    proposal.card1ID,
    proposal.player2,
    proposal.card2ID
  );

  // Fecha modal após enviar
  cancelToServerExchangeCard();
}
