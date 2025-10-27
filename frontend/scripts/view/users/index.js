function sendOperationGetOnlineUsers() {
  const user = getUser();
  if (user) {
    getOnlineUsers(user.id);  
  }
}

function renderUsersInDashboard() {
  const onlineUsers = getOnlineUsersLocalStorage();
  const usersContainer = document.getElementById("users");

  

}
