function getUser() {
  const data = localStorage.getItem("user_avatar_online")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}

function getHistory() {
  const data = localStorage.getItem("history_avatar_online")
  
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

function getPROPOSAL() {
  const data = localStorage.getItem("avatar_online_PROPOSAL_EXCHANGE_CARD_RECEIVER")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}