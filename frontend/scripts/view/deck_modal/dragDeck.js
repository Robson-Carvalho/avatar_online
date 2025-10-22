let draggedCard = null;

document.addEventListener('dragstart', (e) => {
  if (e.target.classList.contains('package-card')) {
    draggedCard = e.target;
    e.dataTransfer.effectAllowed = 'move';
  }
});

const deckSlots = document.querySelectorAll('.deck-card-1, .deck-card-2, .deck-card-3, .deck-card-4, .deck-card-5');

deckSlots.forEach(slot => {
  slot.addEventListener('dragover', (e) => {
    e.preventDefault();
    slot.classList.add('border-blue-400');
  });

  slot.addEventListener('dragleave', () => {
    slot.classList.remove('border-blue-400');
  });

  slot.addEventListener('drop', (e) => {
    e.preventDefault();
    slot.classList.remove('border-blue-400');
    if (draggedCard) {
      const existingCard = slot.querySelector('.package-card');
      
      if (existingCard) {
        userDeckArea.appendChild(existingCard);
      }

      slot.innerHTML = '';
      slot.appendChild(draggedCard);
      draggedCard = null;
    }
  });
});

const userDeckArea = document.getElementById('deck-cards-user');

userDeckArea.addEventListener('dragover', (e) => {
  e.preventDefault();
  userDeckArea.classList.add('border-blue-400'); 
});

userDeckArea.addEventListener('dragleave', () => {
  userDeckArea.classList.remove('border-blue-400');
});

userDeckArea.addEventListener('drop', (e) => {
  e.preventDefault();
  userDeckArea.classList.remove('border-blue-400');
  if (draggedCard) {
    userDeckArea.appendChild(draggedCard); 
    draggedCard = null;
  }
});


function clearDeckSlots() {
  deckSlots.forEach(slot => {
    slot.innerHTML = 'Arraste aqui'; 
  });
  document.getElementById("deck-cards-user").innerHTML = "";
}

function getDeckInfo() {
  const deckInfo = [];
  deckSlots.forEach(slot => {
    const card = slot.querySelector('.package-card');
    if (card) {
      deckInfo.push({
        id: card.id,
      });
    } else {
       deckInfo.push({
        id: null,
      });
    }
  });
  return deckInfo;
}

