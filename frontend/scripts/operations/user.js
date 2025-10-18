function createUser(name = "Elinaldo", nickname="L", email="L@gmail.com", password="123456") {
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

function loginUser(name = "Elinaldo", nickname="L", email="L@gmail.com", password="123456") {
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
