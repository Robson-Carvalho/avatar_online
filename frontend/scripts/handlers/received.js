function handlerMain(message) {
  const data = JSON.parse(message.body);

  log(`📩 Resposta: messasge ${data.message}`);
  log(`📩 Resposta: type: ${data.operationType}`);
  log(`📩 Resposta: status: ${data.operationStatus}`);
  log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);

  if (data.operationStatus == "ERROR") {
    showError(`${data.message}`);
  }

  else if (data.operationType === "LOGIN_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data)) 
    handleLoginUserSuccess(data.data)
  }
    
  else if (data.operationType === "CREATE_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data)) 
    handleRegisterUserSuccess(data.data)
  }

  else if (data.operationType === "OPEN_PACKAGE") {
    handleOpenPackageSuccess(data) 
  }

  else if (data.operationType === "UPDATE_DECK") {
    handleUpdateDeckSuccess(data) 
  }
}

function handleRegisterUserSuccess() {
  showSuccess("Conta criada com sucesso!");
  showSignIn()
}

function handleLoginUserSuccess(data) {
  try {   
    updateUserDisplay(data);
  } catch (error) {
    console.error("error: ", error)
  }

  updateViewsBasedOnConnection()
}

function handleOpenPackageSuccess(data) {
  showSuccess("Pacote aberto com sucesso");

  console.log(data)

  const cards = JSON.parse(data.data);

  console.log(cards, "as cartaaas")

  document.getElementById("package-cards").innerHTML = "";
  cards.forEach(card => {
    document.getElementById("package-cards").innerHTML += cardTemplateOpenPackage(card.name, card.element, card.phase, card.attack, card.life, card.defense, card.rarity);
  });

  
  openModal('package-modal'); 
}

function handleUpdateDeckSuccess(data) {
  showSuccess("Deck atualizado com sucesso!");
}