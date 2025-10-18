function updateViewsBasedOnConnection() {
  const isConnected = stompClient !== null && stompClient.connected;
    
  console.log("🔄 Atualizando views - Conectado:", isConnected);
    
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
        document.getElementById("user_id").innerText = "user:"
        document.getElementById("container-dashboard").classList.remove("hidden");
        document.getElementById("container-signIn").classList.add("hidden");
        document.getElementById("container-signUp").classList.add("hidden");
    }
}

function logout() {
    localStorage.removeItem('user_avatar_online');
    updateViewsBasedOnConnection();
}