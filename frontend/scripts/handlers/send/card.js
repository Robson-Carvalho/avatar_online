function openPackage(userID) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "OPEN_PACKAGE",
    payload: {
      userID: userID,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function getCards(userID, addressWallet) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "GET_CARDS",
    payload: {
      userID: userID,
      addressWallet: addressWallet,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function getCardsPlayerId(userID) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "GET_CARDS_BY_PLAYER_ID",
    payload: {
      userID: userID,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function proposolExchangeCard(
  player1Payload,
  card1Payload,
  player2Payload,
  card2Payload
) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "PROPOSAL_EXCHANGE_CARD",
    payload: {
      player1ID: player1Payload,
      card1ID: card1Payload,
      player2ID: player2Payload,
      card2ID: card2Payload,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function exchangeCard(
  player1Payload,
  card1Payload,
  player2Payload,
  card2Payload
) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "EXCHANGE_CARD",
    payload: {
      player1ID: player1Payload,
      card1ID: card1Payload,
      player2ID: player2Payload,
      card2ID: card2Payload,
    },
  };

  console.log(data.payload);

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}
