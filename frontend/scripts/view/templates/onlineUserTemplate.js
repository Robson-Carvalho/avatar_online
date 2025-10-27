function onlineUserTemplate(id, nickname) {
  return `
   <div 
  class="flex justify-between items-center bg-white rounded-lg shadow px-3 py-4 gap-4 cursor-pointer hover:text-indigo-500"
>
<div class="flex items-center gap-4">
<p class="text-sm font-bold text-gray-800 truncate">${id}</p>
<p class="text-sm text-gray-200 truncate">|</p>
  <p class="text-sm font-medium text-gray-600 truncate">${nickname}</p>
</div>

<div class="flex justify-end gap-2">
            <button id="cancelBtn" onclick="viewCardPlayer('${id}')"
              class="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400">Ver cartas</button>
            <button id="sendBtn" onclick="openExchangeCard('${id}', '${nickname}')"
              class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Trocar</button>
          </div>
  
</div>

<div id="exchange-card-modal"
        class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 min-w-6xl w-full">
        <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        
          <h2 class="text-lg font-bold mb-4 text-center">Trocar Cartas</h2>

        
          <div class="flex flex-col gap-4 mb-6">
            <div>
              <label for="card1" class="block text-sm font-medium text-gray-700 mb-1">ID da sua Carta</label>
              <input type="text" id="card1" name="card1"
                class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div>
              <label for="card2" class="block text-sm font-medium text-gray-700 mb-1">ID da Carta do Player</label>
              <input type="text" id="card2" name="card2"
                class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
          </div>

         
          <div class="flex justify-end gap-2">
            <button id="cancelBtn" onclick="cancelExchangeCard()"
              class="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition-colors">
              Cancelar
            </button>
            <button id="sendBtn" onclick="sendExchangeCard('${id}')"
              class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition-colors">
              Enviar
            </button>
          </div>
        </div>
      </div>

  `;
}
