let addRoomButton,
    roomNameInput,
    roomIdInput,
    errorMessageLabel,
    errorMessageText,
    addedRoomsContainer,
    confirmModal,
    confirmModalCreateButton,
    theRenovationForm;

document.addEventListener("DOMContentLoaded", () => {
    addRoomButton = document.getElementById("add-room-button");
    roomNameInput = document.getElementById("room-name-input");
    roomIdInput = document.getElementById("room-id");
    errorMessageLabel = document.getElementById("room-error-message-label");
    errorMessageText = document.getElementById("room-error-text");
    addedRoomsContainer = document.getElementById("added-rooms-container");
    addRoomButton.addEventListener("click", () => addRoom())
    roomNameInput.addEventListener("keydown", (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            addRoomButton.click();
        }
    });
    confirmModal = new bootstrap.Modal(document.getElementById('confirmSubmit'));
    confirmModalCreateButton = document.getElementById("confirmRenovationSubmission");
    confirmModalCreateButton.addEventListener("click", () => submitRenovation());
    theRenovationForm = document.getElementById("renovation-form");
    theRenovationForm.addEventListener("submit", e => checkSubmit(e));
    init();
})

/**
 * Initialises the create renovation form if the invalidRenovationRecordDetails value is true. If so,
 * it will display all the rooms and the corresponding error messages.
 */
const init = () => {
    // invalidRenovationRecordDetails, roomName, and roomErrorMessages comes from inline scripts to allow
    // Thymeleaf variables to be accessed in JS
    if (roomNames) {
        for (let i = 1; i < roomNames.length; i++) {
            addRoomElement(roomNames[i], roomIds[i], "")
        }
    }
}

/**
 * Handles validating a room name from the room name input. If a room name contains a validation
 * error, it will display the error to the user, otherwise it will add the room element to the
 * renovation room.
 */
const addRoom = () => {
    const roomName = roomNameInput.value;
    const errorMessage = getRoomNameErrors(roomName);
    if (errorMessage) {
        setError(errorMessage);
        return;
    }
    clearError(); // clear the error in case previously there was an error
    roomNameInput.value = ""; // clear the room name input
    addRoomElement(roomName, -1, undefined);
}

/**
 * Handles adding a room element to the form. The add room template is cloned and the name and error
 * messages are displayed. The cloned room template is added to the room's container.
 * @param roomName - The room name to be added.
 * @param roomId - the id of the room if it already exists, if it doesn't exist it is null
 * @param errorMessage - The validation error message for a room name.
 */
const addRoomElement = (roomName, roomId, errorMessage) => {
    // find the template (defined in /templates/fragments/addedRenovationRoomFragment.html) then clone it
    const template = document.getElementById("addedRoomTemplate");
    const container = template.content.cloneNode(true);
    const roomInput = container.querySelector(".room-input");
    const roomInputId = container.querySelector(".room-input-id");
    const roomErrorMessageLabel = container.querySelector(".room-error-message");

    // set the room name and add an event to remove the room when the delete button is pressed
    roomInput.value = roomName;
    roomInputId.value = roomId;
    container.querySelector(".room-delete-button").addEventListener("click",
        (event) => removeRoom(event.target));

    // container.querySelector(".room-delete-button").addEventListener("click", () => removeRoom(container))
    if (errorMessage && errorMessage.trim() !== "") {
        roomErrorMessageLabel.innerText = errorMessage;
    } else {
        roomErrorMessageLabel.style.display = 'none';
    }

    addedRoomsContainer.appendChild(container);
}

/**
 * Removes a room element from the form.
 * @param button - The button element that was clicked.
 */
const removeRoom = (button) => {
    const container = button.closest('.input-row');
    addedRoomsContainer.removeChild(container);
}

/**
 * Displays an error message below the input field.
 * @param message - The error message to display.
 */
const setError = (message) => {
    errorMessageText.textContent = message;
    errorMessageLabel.style.display = "block";
    roomNameInput.classList.add("error");
}

/**
 * Clears the error message from the display.
 */
const clearError = () => {
    errorMessageText.textContent = "";
    errorMessageLabel.style.display = "none";
    roomNameInput.classList.remove("error");
}

/**
 * Validates the room name input against existing room names and allowed characters.
 * @returns An error message if validation fails, otherwise an empty string.
 */
const getRoomNameErrors = (roomName) => {
    const regex = /^(?=.*[\p{L}\p{N}])[\p{L}\p{N} .,'-]+$/u;

    // trim the room name and make it lower case so we don't distinguish between extra whitespace or
    // different capitalization's
    roomName = roomName.trim().toLowerCase();

    if (roomNameInput.value === "") {
        return "Renovation record room name cannot be empty";
    }
    if (!regex.test(roomNameInput.value)) {
        return "Renovation record room names must only include letters, numbers, spaces, dots, commas, hyphens, or apostrophes, and must contain at least one letter or number";
    }
    if (roomName.length > 64) {
        return "Renovation record room names must be 64 characters or less";
    }
    return null;
}

const submitRenovation = () => {
    theRenovationForm.submit();
}

const checkSubmit = (e) => {
    e.preventDefault();
    const roomName = roomNameInput.value.trim();
    if (roomName !== "") {
        confirmModal.show();
    } else {
        submitRenovation();
    }
}

