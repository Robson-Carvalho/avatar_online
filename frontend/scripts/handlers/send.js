function handleLogin(event) {
    event.preventDefault();
    
    const nickname = document.getElementById('nickname-login').value;
    const password = document.getElementById('password-login').value;
    
    console.log('Dados do login:', { nickname, password });
    
    loginUser(nickname, password);
}

function handleRegister(event) {
    event.preventDefault();

    const name = document.getElementById('name-register').value;
    const nickname = document.getElementById('nickname-register').value;
    const email = document.getElementById('email-register').value;
    const password = document.getElementById('password-register').value;
      
    console.log('Dados de registro:', { name, nickname, email, password });
  
    createUser(name, nickname, email, password);
}

function handleOpenPackage() {
    const user = getUser()

    if (user != null) {
        openPackage(user.id)   
    }
}

function handleUpdateDeck() {
  
}

function handleJoinInGame() {
  
}

function handleLeaveGame() {
  
}
