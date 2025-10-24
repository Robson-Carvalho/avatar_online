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

function playCard(userID, matchID) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "PLAY_CARD", 
      payload: {
        userID: userID,
        matchID: matchID
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}

function activateCard(userID, matchID, cardID) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "ACTIVATE_CARD", 
      payload: {
        userID: userID,
        matchID: matchID,
        //cardID: cardID | Comentei só pq a gente n ta enviando ID de carta ainda
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}