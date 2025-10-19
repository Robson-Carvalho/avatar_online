function getUser() {
  const data = localStorage.getItem("user_avatar_online")
  
  if (data != null) return JSON.parse(data);
  
  return null;
}