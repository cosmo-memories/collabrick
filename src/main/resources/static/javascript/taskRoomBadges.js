const checkboxes = document.querySelectorAll("input[type=checkbox]");
const roomContainer = document.getElementById("addedRooms")

function updateSelectedRooms() {
    roomContainer.replaceChildren();
    const checkedBoxes = Array.from(checkboxes)
        .filter(checkbox => checkbox.checked);

    checkedBoxes.forEach(checkedBox => {
        const span = document.createElement('span');
        span.textContent = checkedBox.dataset.roomName;
        span.classList.add('room-badge');
        roomContainer.appendChild(span);
    })
}

checkboxes.forEach(checkbox => {
    checkbox.addEventListener('change', updateSelectedRooms);
})

document.addEventListener("DOMContentLoaded", updateSelectedRooms);