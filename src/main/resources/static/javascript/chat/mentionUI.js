/**
 * Handles mention-related UI interactions
 */

export class MentionUI {

    constructor() {
        this.bellWithDot = this.#getElement("bell-with-dot");
        this.bellNoDot= this.#getElement("bell-no-dot");
        this.mobileNotifsMenu = this.#getElement("mobile-notifications-menu");
        this.noNotifsMessageMenu = this.#getElement("no-notifs-message-menu")
        this.noNotifsMessageDropdown = this.#getElement("no-notifs-message-dropdown")
        this.notifsMessageDropdown = this.#getElement("notifs-message-dropdown");
        this.navbarDot = this.#getElement("navbar-dot")
        this.sideBarDot = this.#getElement("sidebar-dot")
    }

    initMentionsUI(prevMessages) {
        const websocket = false
        let res = prevMessages();
        if (res.length === 0) {
            this.removeNotificationDot()
        } else {
            this.addNotificationDot()
            this.addNotifications(res, websocket)

        }
    }

    /**
     * Add a red dot to signal a new notification when there is a new notification
     */
    addNotificationDot() {
        this.bellWithDot.classList.remove("d-none");
        this.bellNoDot.classList.add("d-none");
        this.noNotifsMessageMenu.classList.add("d-none");
        this.noNotifsMessageDropdown.classList.add("d-none");
        this.notifsMessageDropdown.classList.remove("d-none");
        this.navbarDot.classList.remove("d-none");
        this.sideBarDot.classList.remove("d-none");
    }

    /**
     * Remove red dot when there are no new notifications
     */
    removeNotificationDot() {
        this.bellWithDot.classList.add("d-none");
        this.bellNoDot.classList.remove("d-none");
        this.noNotifsMessageMenu.classList.remove("d-none");
        this.noNotifsMessageDropdown.classList.remove("d-none");
        this.notifsMessageDropdown.classList.add("d-none");
        this.navbarDot.classList.add("d-none");
    }

    /**
     * Formats a timestamp into a relative time string
     * @param {string} timestamp - ISO timestamp string
     * @returns {string} Formatted relative time (e.g., "now", "5 mins ago", "2 hours ago", "3 days ago")
     */
    formatRelativeTime(timestamp) {
        const now = new Date();
        const mentionTime = new Date(timestamp);
        const diffMs = now - mentionTime;
        const diffSeconds = Math.floor(diffMs / 1000);
        const diffMinutes = Math.floor(diffSeconds / 60);
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMinutes < 1) {
            return "now";
        } else if (diffMinutes < 60) {
            return diffMinutes === 1 ? "1 min ago" : `${diffMinutes} mins ago`;
        } else if (diffHours < 24) {
            return diffHours === 1 ? "1 hour ago" : `${diffHours} hours ago`;
        } else {
            return diffDays === 1 ? "1 day ago" : `${diffDays} days ago`;
        }
    }

    addNotifications(mentions, websocketConnection) {
        const notificationTemplate = document.getElementById("large-screen-notification-template");
        const mobileNotificationTemplate = document.getElementById("small-screen-notification-template");

        mentions.forEach((mention) => {
            const redirectUrl = `${window.fullBaseUrl}/renovation/${mention.renovationDetails.id}/chat/${mention.channelId}?mentionTime=${mention.timestamp}`;

            // clone for desktop
            const desktopTemplate = notificationTemplate.content.cloneNode(true);
            this.#fillNotification(desktopTemplate, mention, redirectUrl);

            // clone for mobile
            const mobileTemplate = mobileNotificationTemplate.content.cloneNode(true);
            this.#fillNotification(mobileTemplate, mention, redirectUrl);

            // insert into desktop dropdown
            websocketConnection
                ? this.notifsMessageDropdown.prepend(desktopTemplate)
                : this.notifsMessageDropdown.appendChild(desktopTemplate);

            // insert into mobile menu
            websocketConnection
                ? this.mobileNotifsMenu.prepend(mobileTemplate)
                : this.mobileNotifsMenu.appendChild(mobileTemplate);
        });
    }

    #fillNotification(template, mention, redirectUrl) {
        const notiName = template.querySelector(".notification-title");
        const notiImage = template.querySelector(".notification-sender-image");
        const notiSender = template.querySelector(".notification-sender");
        const notiChannel = template.querySelector(".notification-channel");
        const notiMessage = template.querySelector(".notification-message");
        const notiTime = template.querySelector(".notification-time");
        const link = template.querySelector(".notification-link");

        link.href = redirectUrl;
        notiName.textContent = `New mention from ${mention.renovationDetails.name}`;
        notiImage.src = window.fullBaseUrl + "/" + mention.sender.image;
        notiSender.textContent = `${mention.sender.firstName} ${mention.sender.lastName}`;
        notiChannel.textContent = `${mention.channelName}`;
        notiMessage.textContent = `${mention.messageContent}`;
        notiTime.textContent = this.formatRelativeTime(mention.timestamp);
    }

    /**
     * Safely retrieves a DOM element by ID.
     *
     * @private
     * @param {string} id - The ID of the element to retrieve.
     * @returns {HTMLElement|null} The found element, or null if not found.
     */
    #getElement(id) {
        const element = document.getElementById(id);
        if (!element) {
            console.error(`Element with ID ${id} not found`);
        }
        return element;
    }

    removeMentionsForChannel(channelId) {

        const notifications = this.notifsMessageDropdown.querySelectorAll(`a[href*="/chat/${channelId}"]`);

        notifications.forEach(notification => {
            // Remove the parent element if the link is wrapped
            const elementToRemove = notification.closest('.dropdown-item') || notification.parentElement || notification;
            elementToRemove.remove();
        });

        const remainingItems = this.notifsMessageDropdown.querySelectorAll('a[href*="/chat/"]');
        if (remainingItems.length === 0) {
            this.removeNotificationDot();
        }
    }
}
