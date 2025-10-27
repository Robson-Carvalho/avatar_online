function onlineUserTemplate(id, nickname) {
  return `
    <div id="user-${id}" data-id="${id}" data-name="${nickname}"
         class="flex justify-between items-center bg-white rounded-lg shadow p-2">
      <p class="text-sm font-bold text-gray-800 truncate">${id}</p>
      <p class="text-sm font-medium text-gray-600 truncate">${nickname}</p>
    </div>
  `;
}