import { formatChatFullDate } from "./chatUtils.js";

/**
 * Handles chat-related UI interactions, including message form submission and message display.
 */
export class UI {
	/**
	 * Initializes the message form UI and sets up the submit event listener. Also sets up an input listener for
	 * character counting
	 *
	 * @param {function(string): void} onSendMessageCallback - A callback to invoke when a message is submitted.
	 * @param onLoadMessageHistoryCallback - A callback to invoke when the message history should be loaded.
	 */
	initMessagesUI(onSendMessageCallback, onLoadMessageHistoryCallback) {
		this.messageForm = this.#getElement("message-form");
		this.messagesContainer = this.#getElement("message-container");

		// the container that holds the welcome message, chats, and is scrollable
		this.chatAreaContainer = this.#getElement("chat-area-container");

		if (this.messageForm) {
			const inputField = this.messageForm.querySelector(
				'input[name="message-input"]'
			);
			const charCounterLabel = this.messageForm.querySelector(
				".char-counter-label"
			);
			const errorMessage =
				this.messageForm.querySelector("#message-error");
			const sendButton = this.messageForm.querySelector(
				"button[type='submit'], .button-primary"
			);
			const maxLength = 2048;
			const overMaxLengthErrorMessage =
				"Message must be 2048 characters or less";

			// Disables the send button on page load (input length is zero on load)
			if (sendButton) {
				sendButton.disabled = true;
				sendButton.style.backgroundColor = "var(--color-gray-disabled)";
				sendButton.style.cursor = "default";
			}

			const disableSendButton = () => {
				if (sendButton) {
					sendButton.style.backgroundColor =
						"var(--color-gray-disabled)";
					sendButton.disabled = true;
					sendButton.style.cursor = "default";
				}
			};

			this.messageForm.addEventListener("submit", (event) => {
				event.preventDefault();

				const formData = new FormData(event.target);
				const message = formData.get("message-input").trim();
				if (!message || message === "") {
					return;
				}

				if (window.recomputeMentionsFromText) {
					window.mentions = window.recomputeMentionsFromText(message, members);
				}

				onSendMessageCallback(message);
				event.target.reset();
				disableSendButton();

				setCharacterCounterLabel(charCounterLabel, 0, 2048);
			});

			inputField.addEventListener("input", () => {
				const currentGraphemeClustersCount = countGraphemeClusters(
					inputField.value
				);
				const isTooLong = currentGraphemeClustersCount > maxLength;
				const isEmpty = currentGraphemeClustersCount <= 0;

				const enableSendButton = () => {
					if (sendButton) {
						sendButton.style.backgroundColor =
							"var(--color-primary)";
						sendButton.disabled = false;
						sendButton.style.cursor = "pointer";
					}
				};

				const showError = (msg) => {
					if (errorMessage) {
						errorMessage.textContent = msg;
						errorMessage.style.display = "block";
					}
				};

				const hideError = () => {
					if (errorMessage) {
						errorMessage.style.display = "none";
					}
				};

				if (isTooLong) {
					showError(overMaxLengthErrorMessage);
					disableSendButton();
				} else if (isEmpty) {
					hideError();
					disableSendButton();
				} else {
					hideError();
					enableSendButton();
				}
			});
		}

		this.isProgrammaticScroll = false;

		this.chatAreaContainer.addEventListener("scroll", (event) => {
			if (this.isProgrammaticScroll) return;

			const container = event.target;

			// near top -> load older messages
			if (container.scrollTop <= 700) {
				onLoadMessageHistoryCallback("previous");
			}

			// near bottom -> load newer messages
			const distanceFromBottom =
				container.scrollHeight - container.scrollTop - container.clientHeight;
			if (distanceFromBottom <= 700) {
				onLoadMessageHistoryCallback("next");
			}
		});

	}



	/**
	 * Appends a new message to the messages container in the DOM.
	 *
	 * @param {Object} message - The message object to display.
	 * @param {Object} message - The message object containing content and user info.
	 */

	addMessage(message) {
		const element = this.createMessageElement(message);
		if (!element) {
			return;
		}

		const atBottom =
			Math.abs(
				this.chatAreaContainer.scrollHeight -
					this.chatAreaContainer.scrollTop -
					this.chatAreaContainer.clientHeight
			) < 2;

		this.messagesContainer.appendChild(element);
		if (atBottom || message.user.id === userId) {
			this.chatAreaContainer.scrollTop =
				this.chatAreaContainer.scrollHeight;
		}
	}

	/**
	 * Appends a new message to the messages container in the DOM, though ensures scroll is not affected.
	 *
	 * @param {Object} message - The message object to display.
	 * @param {Object} message - The message object containing content and user info.
	 */
	addMessageWithoutScroll(message) {
		const element = this.createMessageElement(message);
		if (!element) {
			return;
		}
		this.messagesContainer.appendChild(element);

	}

	/**
	 * Clears all of the messages inside the message container.
	 */
	clearAllMessages() {
		this.messagesContainer.innerHTML = "";
	}

	/**
	 * Creates a DOM element representing a single chat message.
	 *
	 * @param {Object} message - The message object containing content and user info.
	 * @returns {DocumentFragment|null} - A cloned message element, or null if template not found.
	 */
	createMessageElement(message) {
		if (!this.messagesContainer) {
			return;
		}

		const template = document.getElementById("message-template");
		if (!template) {
			console.error("Message Template not found");
			return;
		}

		const clone = template.content.cloneNode(true);
		const contentElement = clone.querySelector(".message-content");
		const nameElement = clone.querySelector(".user-name");
		const imageElement = clone.querySelector(".user-img");
        const dateElement = clone.querySelector(".date");
        const aiBadgeElement = clone.querySelector(".ai-badge");

		if (contentElement) {
            message.fragments.forEach((fragment) => {
                if (fragment.type === "TEXT") {
                    const parts = fragment.text.split("\n");
                    parts.forEach((part, index) => {
                        const spanElement = document.createElement("span");
                        spanElement.textContent = part;
                        contentElement.appendChild(spanElement);

                        // insert line breaks where needed
                        if (index < parts.length - 1) {
                            contentElement.appendChild(document.createElement("br"));
                        }
                    });
                } else if (fragment.type === "MENTION") {
                    const spanElement = document.createElement("span");
                    spanElement.textContent = fragment.text;
                    spanElement.classList.add("mention");
                    contentElement.appendChild(spanElement);
                }
				else if (fragment.type === "LINK") {
					const linkElement = document.createElement("a");
					linkElement.textContent = fragment.text;
					linkElement.href = fragment.link;
					linkElement.classList.add("link");
					contentElement.appendChild(linkElement);
				}
            });
		}

		// if (contentElement) contentElement.textContent = message.content;
		if (nameElement)
		if (nameElement)
			nameElement.textContent =
				message.user.firstName + " " + message.user.lastName;
		if (imageElement) imageElement.src = window.fullBaseUrl + "/" + message.user.image;
		if (dateElement) dateElement.innerText = message.formattedDate;
        if (!message.ai) aiBadgeElement.remove();

		return clone;
	}

	/**
	 * Pauses lazy loading and scrolls to the middle where mention should approximately be
	 *
	 * @param messageElement - The approximate middle message element
	 */
	scrollToMiddle(messageElement) {
		if (!messageElement || !this.chatAreaContainer) return;

		const container = this.chatAreaContainer;
		const containerHeight = container.clientHeight;

		const messageTop = messageElement.offsetTop;
		const messageBottom = messageTop + messageElement.offsetHeight;

		const containerTop = container.scrollTop;
		const containerBottom = containerTop + containerHeight;

		// Already visible? Don't scroll at all
		if (messageTop >= containerTop && messageBottom <= containerBottom) {
			return;
		}

		// Otherwise same logic: bottom vs middle
		const distanceFromBottom = container.scrollHeight - messageBottom;
		if (distanceFromBottom < containerHeight / 2) {
			container.scrollTo({ top: container.scrollHeight, behavior: "smooth" });
		} else {
			const scrollTop = messageTop - (containerHeight / 2) + (messageElement.offsetHeight / 2);
			container.scrollTo({ top: scrollTop, behavior: "smooth" });
		}
	}



	/**
	 * Creates a DOM element representing a date separator in the chat.
	 *
	 * @param {Date} date - The date to display.
	 * @returns {DocumentFragment|null} - A cloned date separator element, or null if template not found.
	 */

	createDateSeparator(date) {
		if (!this.messagesContainer) {
			return;
		}

		const template = this.#getElement("date-separator-template");
		if (!template) {
			return;
		}

		const clone = template.content.cloneNode(true);
		const dateElement = clone.querySelector(".date");
		if (dateElement) {
			dateElement.innerText = formatChatFullDate(date);
		}

		return clone;
	}

	/**
	 * Inserts a date separator element into the messages' container.
	 *
	 * @param {Date} date - The date to display in the separator.
	 */
	addDateSeparator(date) {
		const element = this.createDateSeparator(date);
		if (element) {
			this.messagesContainer.appendChild(element);
		}
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
}
