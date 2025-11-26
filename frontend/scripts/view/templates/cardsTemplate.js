function cardTemplateOpenPackage(
  id,
  name,
  element,
  phase,
  attack,
  life,
  defense,
  rarity
) {
  return `
    <div
      class="package-card w-48 h-64 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300 flex flex-col justify-between">
      
      <p class="package-card-name text-center text-sm font-bold text-gray-800 truncate">
        ${name}
      </p>

      <div class="flex flex-col gap-1 mt-2 text-sm text-gray-600 flex-grow">
        <p>Elemento: <span class="font-medium">${element}</span></p>
        <p>Fase: <span class="font-medium">${phase}</span></p>
        <p>⚔️ Ataque: <span class="text-green-600 font-medium">${attack}</span></p>
        <p>❤️ Vida: <span class="text-red-600 font-medium">${life}</span></p>
        <p>🛡️ Defesa: <span class="text-blue-600 font-medium">${defense}</span></p>
        <p>⭐ Raridade: <span class="font-medium">${rarity}</span></p>
      </div>
    </div>
  `;
}

function cardTemplate(id, name, element, phase, attack, life, defense, rarity) {
  return `
    <div
      class="package-card w-64 h-72 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300 flex flex-col justify-between relative">

      <!-- Nome -->
      <p class="package-card-name text-left text-base font-bold text-gray-800 mb-2">
        ${name}
      </p>

      <!-- ID -->
      <div class="bg-gray-100 px-2 py-1 rounded text-xs text-gray-700 font-mono break-all mb-3">
        ID: ${id}
      </div>

      <!-- Atributos -->
      <div class="flex flex-col gap-1 text-sm text-gray-600 flex-grow">
        <p>🌪️ Elemento: <span class="font-medium">${element}</span></p>
        <p>📘 Fase: <span class="font-medium">${phase}</span></p>
        <p>⚔️ Ataque: <span class="text-green-600 font-medium">${attack}</span></p>
        <p>❤️ Vida: <span class="text-red-600 font-medium">${life}</span></p>
        <p>🛡️ Defesa: <span class="text-blue-600 font-medium">${defense}</span></p>
        <p>⭐ Raridade: <span class="font-medium">${rarity}</span></p>
      </div>
    </div>
  `;
}

function cardTemplateSwapCard(
  id,
  name,
  element,
  phase,
  attack,
  life,
  defense,
  rarity,
  tokenId
) {
  return `
    <div
      class="package-card w-64 h-72 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300 flex flex-col justify-between relative">

      <!-- Nome -->
      <p class="package-card-name text-left text-base font-bold text-gray-800 mb-2">
        ${name}
      </p>

      <!-- ID -->
      <div class="bg-gray-100 px-2 py-1 rounded text-xs text-gray-700 font-mono break-all mb-3">
        ID: ${id}
      </div>

      <!-- Atributos -->
      <div class="flex flex-col gap-1 text-sm text-gray-600 flex-grow">
        <p>🌪️ Elemento: <span class="font-medium">${element}</span></p>
        <p>📘 Fase: <span class="font-medium">${phase}</span></p>
        <p>⚔️ Ataque: <span class="text-green-600 font-medium">${attack}</span></p>
        <p>❤️ Vida: <span class="text-red-600 font-medium">${life}</span></p>
        <p>🛡️ Defesa: <span class="text-blue-600 font-medium">${defense}</span></p>
        <p>⭐ Raridade: <span class="font-medium">${rarity}</span></p>
      </div>
    </div>
  `;
}

function cardTemplateDeck(
  id,
  name,
  element,
  phase,
  attack,
  life,
  defense,
  rarity
) {
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

function cardTemplateGame(
  id,
  name,
  element,
  phase,
  attack,
  life,
  defense,
  rarity
) {
  return `
    <div id="${id}" draggable="true" data-id="${id}" data-name="${name}" data-element="${element} data-phase="${phase}" data-attack="${attack}" data-life="${life}" data-defense="${defense}"  data-rarity="${rarity}"
      class="battle-card max-w-48 bg-white rounded-lg shadow-md border border-gray-200 p-4 hover:shadow-lg transition-shadow duration-300">
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
