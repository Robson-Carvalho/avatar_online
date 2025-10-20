function handlerMain(message) {
  const data = JSON.parse(message.body);

  log(`📩 Resposta: messasge ${data.message}`);
  log(`📩 Resposta: type: ${data.operationType}`);
  log(`📩 Resposta: status: ${data.operationStatus}`);
  log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);

  if (data.operationStatus == "ERROR") {
    showError(`${data.message}`);
  }

  else if(data.operationType === "LOGIN_USER" || data.operationType === "CREATE_USER") {
    handleLoginUserOrCreateUserSuccess(data)
  }

  else if (data.operationType === "OPEN_PACKAGE") {
    handleOpenPackageSuccess(data) 
  }

  else if (data.operationType === "UPDATE_DECK") {
    handleUpdateDeckSuccess(data) 
  }
}

function handleLoginUserOrCreateUserSuccess(data) {
  localStorage.setItem("user_avatar_online", JSON.stringify(data.data))
  updateUserDisplay();
  updateViewsBasedOnConnection()
}

function handleOpenPackageSuccess(data) {
  showSuccess("Pacote aberto com sucesso");
  // a ideia é, o botão envia a ação para o servidor e quando o servidor
  // publicar a resposta no tópico o modal abre ou uma mensagem de erro 
  // e lançada muito antes
  
  // usar o data para popular o modal de cartas ganhadas antes de abrir
  console.log(data)

  const cards = JSON.parse(data.data);

  console.log(cards, "as cartaaas")

  cards.forEach(card => {
    document.getElementById("package-cards").innerHTML += cardTemplateOpenPackage(card.name, card.element, card.phase, card.attack, card.life, card.defense, card.rarity);
  });

  
  openModal('package-modal'); 
}

function handleUpdateDeckSuccess(data) {
  showSuccess("Deck atualizado com sucesso!");
}