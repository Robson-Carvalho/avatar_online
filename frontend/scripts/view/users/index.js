function sendOperationGetOnlineUsers() {
  const user = getUser();
  if (user) {
    getOnlineUsers(user.id);  
  }
}

function renderUsersInDashboard() {
  const onlineUsers = getOnlineUsersLocalStorage(); 
  const user = getUser()
  const usersContainer = document.getElementById("online-users-container");

  // limpa conteúdo anterior
  usersContainer.innerHTML = "";

  if (onlineUsers && onlineUsers.length > 1) {
    onlineUsers.forEach(onlineUser => {
      if (onlineUser.id != user.id) {
        usersContainer.innerHTML += onlineUserTemplate(onlineUser.id, onlineUser.nickname);  
      }
    });
  } else {
    // mensagem quando não há usuários online
    usersContainer.innerHTML = `
      <div class="flex justify-center items-center h-full text-xl font-medium text-slate-600">
        Não há usuários online
      </div>
    `;
  }
}

function openExchangeCard(id, nickname) {
  const modal = document.getElementById("exchange-card-modal");
  const userInfo = document.getElementById("modalUserInfo");
  const cancelBtn = document.getElementById("cancelBtn");
  const sendBtn = document.getElementById("sendBtn");

  // Atualiza informações dentro do modal
  userInfo.textContent = `ID: ${id} | Nickname: ${nickname}`;

  // Mostra o modal
  modal.classList.remove("hidden");
}

function sendExchangeCard(id, nickname) {
  const modal = document.getElementById("exchange-card-modal");
  modal.classList.add("hidden");
}

function cancelExchangeCard(id, nickname) {
  const modal = document.getElementById("exchange-card-modal");
  modal.classList.add("hidden");
}

