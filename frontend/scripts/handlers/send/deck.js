function getDeck(userID) {
  if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "GET_DECK", 
      payload: {
       userID: userID
      }
    }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}

function updateDeck(userID, cardId1, cardId2, cardId3, cardId4, cardId5) {
   if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const data = {
      operationType: "UPDATE_DECK", 
      payload: {
        userID: userID,
        cardId1: cardId1,
        cardId2: cardId2,
        cardId3: cardId3,
        cardId4: cardId4,
        cardId5: cardId5
      }
  }

    stompClient.send("/app/operation", {}, JSON.stringify(data));
    log(`📤 Enviado: ${data.operationType}`);
}