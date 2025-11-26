function getHistoryBlockchain(userID) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "GET_HISTORY", 
      payload: {
        userID: userID
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}


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
        cardID: cardID 
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}

function surrenderGame(userID, matchID) {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "SURRENDER", 
      payload: {
        userID: userID,
        matchID: matchID
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}