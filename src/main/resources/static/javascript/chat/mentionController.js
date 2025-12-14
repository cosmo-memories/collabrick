import {
    formatChatShortDateTime,
    formatChatTime,
    isDifferentDate,
    isToday,
} from "./chatUtils.js";

/**
 * Handles the coordination between the mention UI and the mention handler that interfaces
 * with the websockets
 */
export class MentionController {
    /**
     * @param {MentionUI} ui - The UI instance used to display mention notifications
     * @param {MentionHandler} mentionHandler - handles sending and receiving mentions through the websockets
     */
    constructor(ui, mentionHandler) {
        this.ui = ui;
        this.mentionHandler = mentionHandler;

    }

    /**
     * Initializes the mention controller by setting up UI listeners and WebSocket mention handlers.
     */
    init() {
        this.ui.initMentionsUI(
            this.#loadPreviousMentions.bind(this)
        );
        this.mentionHandler.registerMentionHandler(this.#handleIncomingMention.bind(this));

        document.addEventListener('mentionsMarkedSeen', (event) => {
            const { channelId } = event.detail;
            this.ui.removeMentionsForChannel(channelId);
        })
    }

    /**
     * Handles mentions received from the mentionHandler and forwards them to the UI.
     * @param mention - The incoming mention object.
     * @private
     */
    #handleIncomingMention(mention) {
        const websocket = true
        const fullUrl = window.location.href;
        if (fullUrl === `${window.fullBaseUrl}/renovation/${mention.renovationDetails.id}/chat/${mention.channelId}`) {
            fetch(`${window.fullBaseUrl}/mark-seen/${channelId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                }
            })
        } else {
            this.ui.addNotificationDot();
            this.ui.addNotifications([mention], websocket);
        }
    }


    /**
     * Loads the all unseen mentions
     *
     * @returns {[]} Resolves when all unseen mentions have been fetched and processed
     */
    #loadPreviousMentions() {
        return chatMentions;
    }

}
