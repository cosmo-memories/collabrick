let createChannelButton, channelNameInput, channelNameForm, channelNameErrorText, channelErrorDiv, newChannelModal;

const CHANNEL_NAME_EMPTY_MESSAGE = "Channel name cannot be empty";
const INVALID_CHANNEL_NAME_MESSAGE = "Channel name must only include letters, numbers, spaces, " +
    "dots, hyphens, or apostrophes, and must contain at least one letter or number";
const CHANNEL_NAME_TOO_LONG_MESSAGE = "Channel name must be 64 characters or less";
const CHANNEL_NAME_ALREADY_EXISTS_MESSAGE = "A channel with this name already exists";
const CHANNEL_NAME_BRICK_AI = "Channel name cannot be \"brickAI\"";
const CHANNEL_NAME_MAX_LENGTH = 64;
const nameRegex = /^(?=.*[\p{L}\p{N}])[\p{L}\p{N} .'-]+$/u;


/**
 * Gets all the required html elements from the newChannelModal
 *
 * Implements event listeners for hiding the modal and submitting the form on the modal
 */
document.addEventListener('DOMContentLoaded', () => {
    createChannelButton = document.getElementById("create-channel");
    channelNameInput = document.getElementById("new-channel-input");
    channelNameForm = document.getElementById("new-channel-form");
    channelNameErrorText = document.getElementById("channel-error-message");
    channelErrorDiv = document.getElementById("channel-error");
    newChannelModal = document.getElementById("newChannelModal");

    clearErrors();

    // Listens for modal closing
    newChannelModal.addEventListener('hidden.bs.modal', () => {
        clearErrors();
        if (channelNameInput) channelNameInput.value = "";
    });

    // Listens for modal form submit
    channelNameForm.addEventListener("submit", (e) => {
        // Stop default form submission until validation passes
        e.preventDefault();
        if (validateChannelName()) {
            // Submit the form if all validation is passed
            channelNameForm.submit();
        }
    });
});

/**
 * Runs front-end validation for the newChannelModal
 *
 * Returns true if there are no errors shown, otherwise return false
 */
function validateChannelName() {
    clearErrors();
    const nameInput = channelNameInput.value.trim().toLowerCase();
    // Validation
    if (nameInput === "") {
        showErrors(CHANNEL_NAME_EMPTY_MESSAGE);
    } else if (nameInput.length > CHANNEL_NAME_MAX_LENGTH) {
        showErrors(CHANNEL_NAME_TOO_LONG_MESSAGE);
    } else if (!nameRegex.test(nameInput)) {
        showErrors(INVALID_CHANNEL_NAME_MESSAGE);
    } else if (nameInput === "brickai") {
        showErrors(CHANNEL_NAME_BRICK_AI)
    }

    channelNamesList.forEach((channelName) => {
        channelName = channelName.trim().toLowerCase();
        if (nameInput === channelName) {
            showErrors(CHANNEL_NAME_ALREADY_EXISTS_MESSAGE);
        }
    });


    return !channelNameInput.classList.contains("error");
}

/**
 * Helper function to show a given error on the newChannelModal
 */
function showErrors(errorMessage) {
    channelErrorDiv.style.display = "block";
    channelNameErrorText.textContent = errorMessage;
    channelNameInput.classList.add("error");
}

/**
 * Helper function to clear any error message from the newChannelModal
 */
function clearErrors() {
    channelErrorDiv.style.display = "none"
    channelNameErrorText.textContent = "";
    channelNameInput.classList.remove("error")
}