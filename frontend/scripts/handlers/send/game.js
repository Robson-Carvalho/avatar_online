function JoinInQueue(userID) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "JOIN_QUEUE", 
      payload: {
        userID: userID
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}