function cardTemplateOpenPackage(name, element, phase, attack, life, defense, rarity) {
  return `
            <div
              class="package-card  min-w-32 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300">
              <p class="package-card-name text-center text-sm font-bold text-gray-800 mb-4 truncate">
                ${name}
              </p>

              <p class="package-card-element text-sm font-bold text-gray-600 mb-2">
                Elemento: <span class="text-gray-600 font-medium">${element}</span>
              </p>

              <p class="package-card-phase text-sm font-bold text-gray-600 mb-2">
                Fase: <span class="text-gray-600 font-medium">${phase}</span>
              </p>

              <p class="package-card-attack text-sm font-bold text-gray-600 mb-2">
                ⚔️ Ataque: <span class="text-green-600 font-medium">${attack}</span>
              </p>

              <p class="package-card-life text-sm font-bold text-gray-600 mb-2">
                ❤️ Vida: <span class="text-red-600 font-medium">${life}</span>
              </p>

              <p class="package-card-defense text-sm font-bold text-gray-600 mb-2">
                🛡️ Defesa: <span class="text-blue-600 font-medium">${defense}</span>
              </p>

              <p class="package-card-rariry text-sm font-bold text-gray-600 mb-2">
                ⭐ Raridade: <span class="text-gray-600 font-medium">${rarity}</span>
              </p>
            </div>
`;
  
}

function cardTemplateDeck(id, name, element, phase, attack, life, defense, rarity) {
  return `
    <div id="${id}" draggable="true"
      class="package-card max-w-48 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300">
      <p class="package-card-name text-center text-sm font-bold text-gray-800 mb-2 truncate">${name}</p>
      <p class="package-card-element text-sm font-bold text-gray-600 mb-1">Elemento: <span class="text-gray-600 font-medium">${element}</span></p>
      <p class="package-card-phase text-sm font-bold text-gray-600 mb-1">Fase: <span class="text-gray-600 font-medium">${phase}</span></p>
      <p class="package-card-attack text-sm font-bold text-gray-600 mb-1">⚔️ Ataque: <span class="text-green-600 font-medium">${attack}</span></p>
      <p class="package-card-life text-sm font-bold text-gray-600 mb-1">❤️ Vida: <span class="text-red-600 font-medium">${life}</span></p>
      <p class="package-card-defense text-sm font-bold text-gray-600 mb-1">🛡️ Defesa: <span class="text-blue-600 font-medium">${defense}</span></p>
      <p class="package-card-rarity text-sm font-bold text-gray-600 mb-1">⭐ Raridade: <span class="text-gray-600 font-medium">${rarity}</span></p>
    </div>
  `;
}


