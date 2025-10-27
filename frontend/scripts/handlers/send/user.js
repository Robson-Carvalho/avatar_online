function logoutUser(userID) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "LOGOUT_USER",
    payload: {
      userID: userID,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

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
      password,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function verifyAuth(userID) {
  if (!stompClient || !stompClient.connected) {
    return;
  }

  const data = {
    operationType: "AUTH_USER",
    payload: {
      userID: userID,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}

function getOnlineUsers(userID) {
  if (!stompClient || !stompClient.connected) {
    alert("⚠️ Não conectado ao servidor!");
    return;
  }

  const data = {
    operationType: "GET_ONLINE_USERS",
    payload: {
      userID: userID,
    },
  };

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
      password,
    },
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
  log(`📤 Enviado: ${data.operationType}`);
}
