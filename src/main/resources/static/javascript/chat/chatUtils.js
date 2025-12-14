/**
 * Checks if the given date is today.
 *
 * @param {Date} date - The date to check.
 * @returns {boolean} True if the date is today, false otherwise.
 */
export const isToday = (date) => {
    const now = new Date();
    return (
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate()
    );
}

/**
 * Checks if the given date is yesterday.
 *
 * @param {Date} date - The date to check.
 * @returns {boolean} True if the date is yesterday, false otherwise.
 */
export const isYesterday = (date) => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    return (
        date.getFullYear() === yesterday.getFullYear() &&
        date.getMonth() === yesterday.getMonth() &&
        date.getDate() === yesterday.getDate()
    );
}


/**
 * Checks whether two Date objects represent different calendar dates.
 *
 * @param {Date} a - The first date to compare.
 * @param {Date} b - The second date to compare.
 * @returns {boolean} - Returns true if the two dates are on different days,
 *                      months, or years; otherwise, returns false.
*/
export const isDifferentDate = (a, b) => {
    return (
        a.getFullYear() !== b.getFullYear() ||
        a.getMonth() !== b.getMonth() ||
        a.getDate() !== b.getDate()
    );
}

/**
 * Formats a date into a short time string for chat message display, using the NZ locale
 *
 * Example output: "3:45 PM".
 *
 * @param {Date} date - The date to format.
 * @returns {string} The formatted time string.
 */
export const formatChatTime = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
    }).format(date);
};

/**
 * Formats a date into a short, numeric date and time string for chat message display, using the NZ locale.
 *
 * Example output: "11/08/25 10:00 AM".
 *
 * @param {Date} date - The date to format.
 * @returns {string} The formatted date-time string.
 */
export const formatChatShortDateTime = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        year: "2-digit",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: true, // ensures AM/PM format
    }).format(date);
};

/**
 * * Formats a date into a full, human-readable string for chat message display, using the NZ locale.
 *
 * Example output: "11 August 2025".
 *
 * @param {Date} date - The date to format.
 * @returns {string} The formatted date string.
 */
export const formatChatFullDate = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        year: "numeric",
        month: "long",
        day: "numeric",
    }).format(date);
};

/**
 * Formats a timestamp into a relative time string
 * @param {string} timestamp - ISO timestamp string
 * @returns {string} Formatted relative time (e.g., "now", "5 mins ago", "2 hours ago", "3 days ago")
 */
export const formatRelativeTime = (timestamp) => {
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