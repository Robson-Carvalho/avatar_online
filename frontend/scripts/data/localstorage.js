function getUser() {
  const data = localStorage.getItem("user_avatar_online")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}

function getOnlineUsersLocalStorage() {
  const data = localStorage.getItem("online_users_avatar_online")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}


function getMatch() {
  const data = localStorage.getItem("match_avatar_online")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}