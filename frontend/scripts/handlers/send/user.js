function createUser(name, nickname, email, password) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "CREATE_USER", 
      payload: {
        name,
        nickname,
        email,
        password
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}

function verifyAuth() {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
  
    const user = getUser()
    
    const data = {
      operationType: "AUTH_USER", 
      payload: {
       userID: user.id
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}

function loginUser(nickname, password) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "LOGIN_USER", 
      payload: {
        nickname,
        password
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}
