let csrfToken;
let csrfHeader;

/**
 * Gets the csrf information from the html head
 */
document.addEventListener("DOMContentLoaded", () => {
    csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
});


/**
 * Sends post request to the allow brick AI for user chat endpoint, to toggle the brick AI permissions
 * for viewing a users chats when the toggle button is switched
 *
 * This approach was chosen because a regular form submission causes the whole page
 * to reload, which was very slow
 */
function setUserBrickAI() {
    fetch(`${window.fullBaseUrl}/userPage/allowBrickAI`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    }).then();
}
