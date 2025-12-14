
document.addEventListener("DOMContentLoaded", () => {
    csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`${window.fullBaseUrl}/mark-seen/${channelId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    }).then(() => {
        if (window.mentionUI && typeof window.mentionUI.removeMentionsForChannel === 'function') {
            window.mentionUI.removeMentionsForChannel(channelId);
        }
    });
});