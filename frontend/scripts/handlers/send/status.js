function sendPing() {
  if (!stompClient || !stompClient.connected) {
    return;
  }

  const data = {
    operationType: "PING",
    payload: {},
  };

  stompClient.send("/app/operation", {}, JSON.stringify(data));
}
