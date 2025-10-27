function onlineUserTemplate(id, nickname) {
  return `
   <div 
  onclick="openExchangeCard('${id}', '${nickname}')"
  class="flex justify-between items-center bg-white rounded-lg shadow px-3 py-4 gap-4 cursor-pointer hover:text-indigo-500"
>
  <p class="text-sm font-bold text-gray-800 truncate">${id}</p>
  <p class="text-sm font-medium text-gray-600 truncate">${nickname}</p>
</div>

  `;
}
