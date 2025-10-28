let lastPingTime = 0;
let pingInterval = null;
let lastPingValue = 0;

function sendPingToServer() {
  lastPingTime = Date.now(); 
  sendPing(); 
}

function handlePingResponse() {
  const now = Date.now();
  const ping = Math.round(now - lastPingTime);
  lastPingValue = ping;

  updatePingDisplay(ping);
}

function updatePingDisplay(ping) {
  const pingDash = document.getElementById("ping-dash");
  const pingGame = document.getElementById("ping-game");

  if (pingDash) pingDash.textContent = `${ping} ms`;
  if (pingGame) pingGame.textContent = `${ping} ms`;
}

function startPingLoop() {
  if (pingInterval) clearInterval(pingInterval);
  
  sendPingToServer();
  pingInterval = setInterval(sendPingToServer, 1000); 

function stopPingLoop() {
  if (pingInterval) {
    clearInterval(pingInterval);
    pingInterval = null;
  }
}
