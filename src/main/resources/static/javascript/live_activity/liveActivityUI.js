import { formatRelativeTime } from "../chat/chatUtils.js";

export class LiveActivityUI {
    /**
     * @param {{containerId?: string, templateId?: string}} options
     */
    constructor({ containerId = "activity-feed-items-container", templateId = "activity-feed-item-template" } = {}) {
        this.container = document.getElementById(containerId);
        this.template = document.getElementById(templateId);
        this.maxUpdates = 10;

        if (!this.container) console.warn(`[LiveActivityUI] Missing container #${containerId}`);
        if (!this.template) console.warn(`[LiveActivityUI] Missing template #${templateId}`);
    }

    /**
     * Render initial list of items.
     */
    initializeActivityFeed() {
        if (!this.container || !this.template) return;
        if (activityItems.length === 0) {
            this.showNoActivityMessage();
        } else {
            this.hideNoActivityMessage();
            activityItems.forEach((item) => this.addActivityItem(item, /* prepend */ false));
        }
    }

    /**
     * Add a single item to the feed (used for live updates).
     * @param {object} activityItem
     * @param {boolean} prepend - put newest first by default
     */
    addActivityItem(activityItem, prepend = true) {
        this.hideNoActivityMessage();
        if (!this.container || !this.template) return;
        const node = this.createActivityItem(activityItem);
        prepend ? this.container.prepend(node) : this.container.appendChild(node);

        this.#trimToMax();
    }

    /**
     * Remove oldest items when over 10 updates.
     */
    #trimToMax() {
        while (this.container.childElementCount > this.maxUpdates) {
            if (this.container.lastElementChild) {
                this.container.lastElementChild.remove();
            }
        }
    }

    /**
     * Build a DOM node for one activity item.
     * @param {object} activityItem
     * @returns {Node}
     */
    createActivityItem(activityItem) {
        const clone = this.template.content.cloneNode(true);

        const $icon = clone.querySelector(".activity-icon");
        const $sender = clone.querySelector(".activity-sender-name");
        const $timestamp = clone.querySelector(".activity-timestamp");
        const $message = clone.querySelector(".activity-message");

        if ($icon) $icon.innerHTML = this.getIcon(activityItem);
        if ($sender) $sender.textContent = activityItem.senderName ?? activityItem.email ?? "";
        if ($timestamp) $timestamp.textContent = formatRelativeTime(activityItem.timestamp);
        if ($message) $message.innerHTML = this.getActivityItemMessage(activityItem);

        // Add links to appropriate destination based on activity type
        const link = clone.querySelector('a.activity-link');
        switch (activityItem.activityType) {
            case "TASK_ADDED":
            case "TASK_EDITED":
            case "TASK_CHANGED_FROM_NOT_STARTED":
            case "TASK_CHANGED_FROM_IN_PROGRESS":
            case "TASK_CHANGED_FROM_PROGRESS":
            case "TASK_CHANGED_FROM_BLOCKED":
            case "TASK_CHANGED_FROM_COMPLETED":
            case "TASK_CHANGED_FROM_CANCELLED":
            case "EXPENSE_ADDED":
                link.href = `${baseUrl}/renovation/${activityItem.renovationId}/tasks/${activityItem.taskId}`;
                break;
            case "BUDGET_EDITED":
                link.href = `${baseUrl}/renovation/${activityItem.renovationId}/budget`;
                break;
            case "INVITE_ACCEPTED":
            case "INVITE_DECLINED":
                link.href = `${baseUrl}/renovation/${activityItem.renovationId}/members`;
                break;
            default:
                link.href = `${baseUrl}/renovation/${activityItem.renovationId}`;
        }


        return clone;
    }

    showNoActivityMessage() {
        if (!this.container) return;

        // Don't add if already exists
        if (this.container.querySelector(`.${this.noActivityClass}`)) return;

        const messageElement = document.createElement('div');
        messageElement.className = `${this.noActivityClass} text-muted text-center p-4`;
        messageElement.innerHTML = `
            <div class="d-flex flex-column align-items-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mb-3 opacity-50">
                    <path d="M8 2v4"/>
                    <path d="M16 2v4"/>
                    <rect width="18" height="18" x="3" y="4" rx="2"/>
                    <path d="M3 10h18"/>
                </svg>
                <p class="mb-0">No recent activity in your renovations</p>
            </div>
        `;

        this.container.appendChild(messageElement);
    }

    /**
     * Hide "no recent activity" message
     */
    hideNoActivityMessage() {
        if (!this.container) return;

        const messageElement = this.container.querySelector(`.${this.noActivityClass}`);
        if (messageElement) {
            messageElement.remove();
        }
    }

    /**
     * Pretty badges for task states.
     * @param {string} taskState
     * @returns {string}
     */
    createTaskStateBadge(taskState) {
        switch (taskState) {
            case "NOT_STARTED":
                return '<span class="badge bg-secondary">Not Started</span>';
            case "IN_PROGRESS":
                return '<span class="badge bg-primary">In Progress</span>';
            case "BLOCKED":
                return '<span class="badge bg-warning">Blocked</span>';
            case "COMPLETED":
                return '<span class="badge bg-success">Completed</span>';
            case "CANCELLED":
                return '<span class="badge bg-danger">Cancelled</span>';
            default:
                return "";
        }
    }

    /**
     * Normalizes the activity type between the JavaScript and what's gotten from the DTO
     * @param type the type inputted from the DTO
     * @returns The correct activity type as a string
     */
    normalizeType(type) {
        if (!type) return "default";
        const t = String(type).toUpperCase();

        if (t.startsWith("TASK_CHANGED_FROM_")) return "taskStateChanged";

        const map = {
            "TASK_ADDED": "taskAdded",
            "TASK_EDITED": "taskEdited",
            "BUDGET_EDITED": "budgetEdited",
            "EXPENSE_ADDED": "expenseAdded",
            "INVITE_ACCEPTED": "inviteAccepted",
            "INVITE_DECLINED": "inviteDeclined",
        };
        return map[t] ?? "default";
    }

    /**
     * Compose the message HTML from the activity model.
     * @param {object} activityItem
     * @returns {string}
     */
    getActivityItemMessage(activityItem) {
        const normalizedType = this.normalizeType(activityItem.activityType);
        const messages = {
            taskAdded: (item) => `created a task '${item.taskName}' for `,
            taskStateChanged: (item) =>
                `changed task state '${item.taskName}' from ${this.createTaskStateBadge(item.oldState)} → ${this.createTaskStateBadge(item.newState)} for `,
            taskEdited: (item) => `edited task '${item.taskName}' for `,
            budgetEdited: () => `edited budget for `,
            expenseAdded: (item) => `added a <span class="expense-highlight">$${item.expenseAmount}</span> expense for '${item.expenseName}' to `,
            inviteAccepted: () => `accepted your invitation to `,
            inviteDeclined: () => `declined your invitation to `,
        };

        const renovationName = `<b><i>${activityItem.renovationName}</i></b>`;
        return messages[normalizedType] ? messages[normalizedType](activityItem) + renovationName : "";
    }

    /**
     * Pick the icon SVG for the item type.
     * @param {object} activityItem
     * @returns {string}
     */
    getIcon(activityItem) {
        const normalizedType = this.normalizeType(activityItem.activityType);
        return this.svgIcons[normalizedType] || this.svgIcons.default || "";
    }

    /** Icon set */
    svgIcons = {
        taskAdded: `<div class="stat-icon text-warning">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-file-plus-2">
        <path d="M4 22h14a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v4" />
        <path d="M14 2v4a2 2 0 0 0 2 2h4" />
        <path d="M3 15h6" />
        <path d="M6 12v6" />
      </svg>
    </div>`,
        taskStateChanged: `<div class="stat-icon text-warning">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-move-horizontal">
        <path d="m18 8 4 4-4 4" />
        <path d="M2 12h20" />
        <path d="m6 8-4 4 4 4" />
      </svg>
    </div>`,
        taskEdited: `<div class="stat-icon text-warning">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-square-pen">
        <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z" />
      </svg>
    </div>`,
        budgetEdited: `<div class="stat-icon color-primary">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-dollar-sign">
        <line x1="12" x2="12" y1="2" y2="22" />
        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
      </svg>
    </div>`,
        expenseAdded: `<div class="stat-icon color-primary">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-circle-dollar-sign">
        <circle cx="12" cy="12" r="10" />
        <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8" />
        <path d="M12 18V6" />
      </svg>
    </div>`,
        inviteAccepted: `<div class="stat-icon text-success">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-mail-check">
        <path d="M22 13V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h8" />
        <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
        <path d="m16 19 2 2 4-4" />
      </svg>
    </div>`,
        inviteDeclined: `<div class="stat-icon text-danger">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-mail-x">
        <path d="M22 13V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h9" />
        <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
        <path d="m17 17 4 4" />
        <path d="m21 17-4 4" />
      </svg>
    </div>`,
        default: "",
    };
}