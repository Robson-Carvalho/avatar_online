document.addEventListener('DOMContentLoaded', function() {
    const statusIndicator = document.getElementById('status-indicator');
    const journeyButton = document.getElementById('journey-button');
    
    let isServerOnline = false;
    let statusCheckInterval = null; 

    function updateUI(isOnline) {
        isServerOnline = isOnline;
        if (isOnline) {
            statusIndicator.classList.remove('bg-gray-400', 'bg-red-500');
            statusIndicator.classList.add('bg-green-500');
            journeyButton.textContent = 'Iniciar Jornada';
        } else {
            statusIndicator.classList.remove('bg-gray-400', 'bg-green-500');
            statusIndicator.classList.add('bg-red-500');
            journeyButton.textContent = 'Tentar Novamente';
        }
    }

    function checkServerStatus() {
        if (journeyButton.textContent === 'Verificando Servidor...') return;
        
        journeyButton.textContent = 'Verificando Servidor...';
        
        WebSocketService.connect(
            () => {
                console.log("Servidor online. Conexão de verificação bem-sucedida.");
                updateUI(true);
                
                if (statusCheckInterval) {
                    clearInterval(statusCheckInterval);
                    statusCheckInterval = null;
                }
                
                WebSocketService.disconnect();
            },
            () => { 
                console.error("Servidor offline ou inacessível. Próxima tentativa em 5 segundos.");
                updateUI(false);
            }
        );
    }

    journeyButton.addEventListener('click', function(e) {
        e.preventDefault(); 

        if (isServerOnline) {
            window.location.href = 'screens/signin.html';
        } else {
            console.log("Tentando reconectar manualmente...");
            checkServerStatus();
        }
    });

    checkServerStatus();
    
    if (!statusCheckInterval) {
        statusCheckInterval = setInterval(checkServerStatus, 5000); 
    }
});

