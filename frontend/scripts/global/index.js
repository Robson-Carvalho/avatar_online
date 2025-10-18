// chamado ao realizar login
function updateUserDisplay() {
  const data = localStorage.getItem("user_avatar_online");
  
  if (data) {
    try {
      const user = JSON.parse(data);
      
      document.getElementById("user_id").innerText = user.id;
      document.getElementById("nickname_dash").innerText = user.nickname;
    } catch (error) {
      console.error('Erro ao parse user data:', error);
    }
  }
}

document.addEventListener("DOMContentLoaded", updateUserDisplay())