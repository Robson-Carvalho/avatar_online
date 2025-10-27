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