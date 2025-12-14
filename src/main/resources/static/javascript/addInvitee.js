let autocompleteResultsContainer,
    invitationInput,
    autocompleteSuggestionTemplate,
    invitationError,
    invitationErrorMessage,
    addedInviteesDiv,
    addedInviteeTemplate,
    invitationModal, autocompleteResultsDivs, autocompleteResults,
    invitationForm, submitInviteButton



let currentFocusIndex = -1;


/**
 * Get all html elements and templates for adding invitees
 */
document.addEventListener("DOMContentLoaded", () => {
    // Load HTML elements
    autocompleteResultsContainer = document.getElementById('autocomplete-results-container')
    invitationInput = document.getElementById('invitation-input')
    invitationInput.value = "" // Clear input on page reload
    addedInviteesDiv = document.getElementById('added-invitees-div')
    invitationModal = document.getElementById("inviteModal" + id)

    // Load HTML templates
    autocompleteSuggestionTemplate = document.getElementById("user-badge-template")
    addedInviteeTemplate = document.getElementById("invitee-badge-template")

    // Load error elements
    invitationError = document.getElementById("invitation-error")
    invitationErrorMessage = document.getElementById("invitation-error-message")

    // Add event listeners
    invitationModal.addEventListener('hidden.bs.modal', function (e) {
        clearErrors()
        clearInput()
        addedInviteesDiv.innerHTML = ""
    })
    invitationInput.addEventListener("keydown", function (e)  {
        currentFocusIndex = autocompleteKeyBehaviour(e, autocompleteResultsDivs, currentFocusIndex, () => addButtonClicked())
    })

    invitationForm = document.getElementById("invitation-form")
    submitInviteButton = document.getElementById("submit-invite")
    submitInviteButton.onclick =() => {
        const alreadyInvitedEmails = new Set(
            Array.from(addedInviteesDiv.querySelectorAll(".invitation-input"))
                .map(input => input.value.trim().toLowerCase())
        );
        if (alreadyInvitedEmails.size !== 0) {
            invitationForm.submit()
        } else {
            addError("You must add at least one member before sending invitations")
        }
    }
})



/**
 * Fetches suggestions from the rest controller if required, and adds results to the autocomplete div
 * @returns {Promise<void>}
 */
async function fetchSuggestedInvitees() {
    const query = invitationInput.value
    if (query.length < 2) {
        autocompleteResultsContainer.innerHTML = ""
        return;
    }

    // Fetch autocomplete results from REST controller
    const resultsFetch = await fetch(`${window.fullBaseUrl}/invitation/user-matching?renovationId=${id}&search=${encodeURIComponent(query)}`)
    if (!resultsFetch.ok) {
        throw new Error("Failed to fetch suggestions");
    }
    autocompleteResults = await resultsFetch.json();
    autocompleteResultsContainer.innerHTML = ""

    // List to collect divs of autocomplete suggestions
    autocompleteResultsDivs = [];

    // This snippet was GPT generated, used to get the already invited emails
    const alreadyInvitedEmails = new Set(
        Array.from(addedInviteesDiv.querySelectorAll(".invitation-input"))
            .map(input => input.value.trim().toLowerCase())
    );

    autocompleteResults.forEach((result, index) => {
        // Checks if user is already be added to the list of invites to be sent off.
        // if it is, this prevents it from appearing in the autocomplete
        if (alreadyInvitedEmails.has(result.email.trim().toLowerCase())) {
            return; // Exits the current iteration
        }
        createAutocompleteOption(result);

    })
}

/**
 * Creates the user badge div for a given autocomplete result
 * and appends it to the autocomplete results div
 *
 * @param result the autocomplete result to create
 */
function createAutocompleteOption(result) {
    const templateClone = autocompleteSuggestionTemplate.content.cloneNode(true)
    templateClone.querySelector(".user-badge-template-image").src = window.fullBaseUrl + "/" + (result.image || 'images/PlaceholderIcon.png');
    const displayName = (result.firstName && result.lastName) ? result.firstName + ' ' + result.lastName : result.email
    templateClone.querySelector(".user-badge-template-name").textContent = displayName;
    const userBadgeDiv = templateClone.querySelector(".user-badge-template-div")

    autocompleteResultsDivs.push(userBadgeDiv);
    userBadgeDiv.setAttribute("tabindex", "-1"); //Make div focusable
    userBadgeDiv.addEventListener("keydown", (e) => {
        currentFocusIndex = autocompleteKeyBehaviour(e, autocompleteResultsDivs, currentFocusIndex, () => addButtonClicked())
    });
    const userNameDiv = templateClone.querySelector(".user-name-div")

    let canBeInvited = true;
    const statusSpan = document.createElement('span');
    if (result.member) {
        statusSpan.textContent = 'is already a member';
        canBeInvited = false;
    } else if (result.invited) {
        if (result.invitationStatus === "DECLINED") {
            statusSpan.textContent = 'has declined a previous invitation';
        } else if (result.invitationStatus === "EXPIRED") {
            statusSpan.textContent = "has an invitation that has expired";
        } else {
            statusSpan.textContent = 'already has a pending invitation';
            canBeInvited = false;
        }
    }
    statusSpan.classList.add("autocomplete-disabled-message");

    if (canBeInvited) {
        userBadgeDiv.onclick = () => {
            addToInviteList(result.email, displayName, result.image, true)
        }
    } else {
        disableAutocompleteOption(result, userBadgeDiv)

    }

    userNameDiv.appendChild(statusSpan);
    autocompleteResultsContainer.appendChild(templateClone);
}

/**
 * Disables an autocomplete option if the user is invited or a member of
 * that renovation already
 *
 * @param result the autocomplete result to disable
 * @param userBadgeDiv the div for the autocomplete result to disable
 */
function disableAutocompleteOption(result, userBadgeDiv) {
    userBadgeDiv.style.pointerEvents = "none";
    userBadgeDiv.style.opacity = "0.5";
    userBadgeDiv.classList.add("disabled");
    userBadgeDiv.onclick = () => {};
}

/**
 * Invoked when the "Add" button is pressed
 * Validates the email input, if the email is valid, the email is added to the invitee list and div.
 * If the email is invalid, and error message is shown
 */
function addButtonClicked() {
    // If focused on an autocomplete suggestion, simulate clicking on that user
    if (document.activeElement.classList.contains("user-badge-template-div")) {
        document.activeElement.click()
        return;
    }

    // If focused on the input box, validate email manually
    const emailRegex = /^\w+([\-+.'']\w+)*@\w+([\-\.]\w+)*\.([A-Za-z]{2,})$/;
    const email = invitationInput.value
    autocompleteResultsContainer.innerHTML = "";

    if (!emailRegex.test(email)) {
        addError("Email address must be in the form 'jane@doe.nz'");
    } else if (isEmailInAddedInvitees(email) || isEmailInMemberList(email) || isEmailInPendingInvitationList(email)) {
        addError("User with email address " + email + " has already been invited");
    } else {

        // Allows recognised users to be shown without warning even when manually entered their email
        for (const result of autocompleteResults) {
            if (result.email.trim().toLowerCase() === invitationInput.value.trim().toLowerCase()) {
                addToInviteList(invitationInput.value, result.firstName + ' ' + result.lastName,
                    result.image, true)
                return;

            }
        }

        // Add the email to the invite list when the user is not recognised
        addToInviteList(invitationInput.value, invitationInput.value, null, false)
    }
}

/**
 * Adds an error message to the invitation input
 *
 * @param errorMessage
 */
function addError(errorMessage) {
    invitationError.style.display = "block";
    invitationErrorMessage.textContent = errorMessage;
    invitationInput.classList.add("error");
}

/**
 * Clears Invitation Error Message
 */
function clearErrors() {
    invitationError.style.display = "none"
    invitationErrorMessage.textContent = "";
    invitationInput.classList.remove("error")
}

/**
 * Clear input from invitation input box, including autocomplete results
 */
function clearInput() {
    autocompleteResultsContainer.innerHTML = "";
    invitationInput.value = "";

    // Focus back on the input box
    currentFocusIndex = -1;
    invitationInput.focus()
}

/**
 * Adds a new invitee badge to the added invitees div
 * @param email the email associated with the user
 * @param displayName the display name: email if the user is entered manually, first and last name if the user is recognised
 * @param image the profile pic of the user or the default profile picture
 * @param recognised boolean if the user is recognised or not
 */
function addToInviteList(email, displayName, image, recognised) {
    const addedInviteeTemplateClone = addedInviteeTemplate.content.cloneNode(true)
    addedInviteeTemplateClone.querySelector(".invitee-badge-template-image").src = window.fullBaseUrl + '/' + (image || 'images/PlaceholderIcon.png');
    addedInviteeTemplateClone.querySelector(".invitee-badge-template-name").textContent = displayName;
    addedInviteeTemplateClone.querySelector(".invitation-input").value = email;

    // Add warning for manually entered emails
    if (recognised) {
        addedInviteeTemplateClone.querySelector(".warning-container").style.display = "none"
    } else {
        addedInviteeTemplateClone.querySelector(".warning-container").style.display = "block"
        addedInviteeTemplateClone.querySelector(".warning-label").textContent = "You haven't interacted with this user before"
    }

    // x delete button functionality
    addedInviteeTemplateClone.querySelector("#remove-invitee-button").onclick = function() { this.closest(".invitee-badge-container").remove()}

    addedInviteesDiv.appendChild(addedInviteeTemplateClone);
    clearInput()
    clearErrors()
}

/**
 * Checks if a given email has already been added to the invite list
 *
 * @param email        The email to check
 * @returns {boolean}  True if email is already in the list, false otherwise
 */
function isEmailInAddedInvitees(email) {
    const currentInvitees = addedInviteesDiv.querySelectorAll(".invitation-input");
    for (const invitee of currentInvitees) {
        if (invitee.value.trim().toLowerCase() === email.trim().toLowerCase()) {
            return true;
        }
    }
    return false;
}

/**
 * Checks if an email is associated with a member of this renovation
 *
 * @param email email to check if it is an email of a member of this renovation
 */
function isEmailInMemberList(email) {
    for (const result of autocompleteResults) {
        if (result.email.trim().toLowerCase() === email.trim().toLowerCase() && result.member) {
            return true
        }
    }
    return false;
}

/**
 * Checks if an email is associated with someone who has a pending invitation to this renovation
 *
 * @param email email to check if it is an email of someone who has a pending invitation
 */
function isEmailInPendingInvitationList(email) {
    for (const result of autocompleteResults) {
        if (result.email.trim().toLowerCase() === email.trim().toLowerCase() && result.invited) {
            return true
        }
    }
    return false;
}





