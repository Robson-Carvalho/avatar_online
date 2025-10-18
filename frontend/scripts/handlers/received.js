function handlerMain(message) {
  const data = JSON.parse(message.body);

  log(`📩 Resposta: messasge ${data.message}`);
  log(`📩 Resposta: type: ${data.operationType}`);
  log(`📩 Resposta: status: ${data.operationStatus}`);
  log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);

  if (data.operationStatus == "ERROR") {
    showError(`${data.message}`);
  }

  else if(data.operationStatus === "OK" &&  (data.operationType === "LOGIN_USER" || data.operationType === "CREATE_USER")) {
    handleLoginUserOrCreateUserSuccess(data)
  }
}

function handleLoginUserOrCreateUserSuccess(data) {
  localStorage.setItem("user_avatar_online", JSON.stringify(data.data))
  updateUserDisplay();
  updateViewsBasedOnConnection()
}