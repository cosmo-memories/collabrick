import {formatChatShortDateTime, formatChatTime, isDifferentDate, isToday} from "./chatUtils.js";

/**
 * Handles the coordination between the chat UI and the chat handler (that interfaces with the websockets).
 */
export class ChatController {
	/**
	 * @param {UI} ui - The UI instance used to display messages and register events.
	 * @param {ChatHandler} chatHandler - handles sending and receiving messages through the websockets.
	 */
	constructor(ui, chatHandler) {
		this.ui = ui;
		this.chatHandler = chatHandler;
		this.lastMessage = null;
		this.topMessage = null;
		this.isLoadingPreviousMessages = false;
		this.loadedAllMessages = false;
		this.isLoadingNewerMessages = false;
		this.loadedAllNewerMessages = false;
		this.bottomMessage = null; // Track the last (newest) message loaded

	}

	/**
	 * Initializes the chat controller by setting up UI listeners and WebSocket message handlers.
	 */
	init() {
		this.ui.initMessagesUI(
			this.#handleOutgoingMessage.bind(this),
			this.#loadMessages.bind(this)
		);
		this.chatHandler.registerMessageHandler(this.#handleIncomingRawMessage.bind(this));
		if (mentionTime) {
			this.chatHandler.registerSubscribedToChannelHandler(this.#pullUpMention.bind(this, mentionTime));
		} else {
			this.chatHandler.registerSubscribedToChannelHandler(this.#loadMessageHistory.bind(this))
		}


	}

    /**
     * Handles messages sent from the UI and forwards them through the socket.
     * @param {string} message - The message content to send.
     * @private
     */
    async #handleOutgoingMessage(message) {
        try {
            const msg = {
                content: message,
                mentions: window.mentions
            }
            await this.chatHandler.sendMessage(msg);
        } catch (error) {
            console.error("Failed to send message:", error);
        }
    }

	/**
	 * Parses the raw incoming message's dates.
	 * @param message  - The parsed incoming message object.
	 * @returns
	 */
	#parseMessage(message) {
		// parse the date from ISO timestamp
		const parsedDate = new Date(message.date);
		const formattedDate = isToday(parsedDate)
			? formatChatTime(parsedDate)
			: formatChatShortDateTime(parsedDate);

		return (message = {
			...message,
			date: parsedDate,
			formattedDate: formattedDate,
		});
	}

	/**
	 * Handles a raw message received from the WebSocket. Parses then forwards to
	 * the incoming message handler.
	 * @param message - The raw incoming message object.
	 * @private
	 */
	#handleIncomingRawMessage(message) {
		message = this.#parseMessage(message);
		this.#handleIncomingMessage(message);
	}

	/**
	 * Handles messages received from the WebSocket and forwards them to the UI.
	 * @param message - The incoming message object.
	 * @private
	 */
	#handleIncomingMessage(message) {
		if (
			!this.lastMessage ||
			this.#isDateChangedSinceLastMessage(message.date)
		) {
			this.ui.addDateSeparator(message.date);
		}
		this.lastMessage = message;
		this.bottomMessage = message;
		this.ui.addMessage(message);
	}

	/**
	 * Handles incoming messages that had been lazy loaded
	 *
	 * @param message- The incoming message object.
	 */
	#handleLazyLoadedIncomingMessage(message) {
		if (
			!this.lastMessage ||
			this.#isDateChangedSinceLastMessage(message.date)
		) {
			this.ui.addDateSeparator(message.date);
		}
		this.lastMessage = message;
		this.bottomMessage = message;
		this.ui.addMessageWithoutScroll(message);
	}

	/**
	 * Handles incoming previous messages into ui
	 *
	 * @param messages - Incoming message object
	 */
	#handleIncomingPreviousMessages(messages) {
		// remove the first element in message container which will always be a date seperator
		this.ui.messagesContainer.removeChild(
			this.ui.messagesContainer.firstElementChild
		);

		const elements = [];

		// push a date separator to the first message in the block - this is what will be deleted by
		// removing the first element child above when loading more previous messages
		const firstMessageInBlock = messages[0];
		elements.push(this.ui.createDateSeparator(firstMessageInBlock.date));

		for (let i = 0; i < messages.length; i++) {
			const message = messages[i];
			const nextMessage = messages[i + 1];
			elements.push(this.ui.createMessageElement(message));

			// if the next message exists and is on a different date, add a date separator
			if (
				nextMessage &&
				isDifferentDate(message.date, nextMessage.date)
			) {
				elements.push(this.ui.createDateSeparator(nextMessage.date));
			}
		}

		// If the latest message in the block is on a different date than the top message currently rendered,
		// add a date separator for the first message to mark the boundary
		const latestMessageInBlock = messages[messages.length - 1];
		if (isDifferentDate(latestMessageInBlock.date, this.topMessage.date)) {
			elements.push(this.ui.createDateSeparator(this.topMessage.date));
		}

		// prepend adds all of the elements to the top of the messages container
		this.ui.messagesContainer.prepend(...elements);
	}

	/**
	 * Determines whether the given message's date differs from the date of the last message.
	 *
	 * @param {Date} date - The date of the incoming message to compare against the last message's date.
	 * @returns {boolean} True if the incoming message's date is different from the last message's date; otherwise, false.
	 */
	#isDateChangedSinceLastMessage(date) {
		return isDifferentDate(date, this.lastMessage.date);
	}

	/**
	 * Loads the message history at mention's time stamp.
	 *
	 * @returns {Promise<void>} Resolves when all messages have been fetched and processed
	 */
	async #pullUpMention(Timestamp) {
		try {
			const response = await fetch(
				window.fullBaseUrl + "/chat/" + channelId + "/showMention?mentionTime=" + Timestamp
			);
			let messages = await response.json();
			messages = messages.map((message) => this.#parseMessage(message));
			messages.reverse();

			this.ui.clearAllMessages();
			this.lastMessage = null;

			messages.forEach((message) => {
				this.#handleIncomingMessage(message);
			});
			const middleIndex = Math.floor(messages.length / 2);
			const middleMessageElement = this.ui.messagesContainer.children[middleIndex];
			this.ui.scrollToMiddle(middleMessageElement);

			if (messages.length > 0) {
				this.topMessage = messages[0];
				this.bottomMessage = messages[messages.length - 1];
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Loads the full message history for the current chat channel.
	 *
	 * @returns {Promise<void>} Resolves when all messages have been fetched and processed
	 */
	async #loadMessageHistory() {
		try {
			const response = await fetch(
				window.fullBaseUrl + "/chat/" + channelId + "/history"
			);
			let messages = await response.json();
			messages = messages.map((message) => this.#parseMessage(message));
			messages.reverse();

			this.ui.clearAllMessages();
			this.lastMessage = null;

			messages.forEach((message) => {
				this.#handleIncomingMessage(message);
			});

			if (messages.length > 0) {
				this.topMessage = messages[0];
				this.bottomMessage = messages[messages.length - 1];
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Loads lazy loads chat messages in the given direction.
	 *
	 * @param {"previous"|"next"} direction - Direction to load messages
	 * @returns {Promise<void>}
	 */
	async #loadMessages(direction) {
		const isPrevious = direction === "previous";
		const edgeMessage = isPrevious ? this.topMessage : this.bottomMessage;

		if (!edgeMessage) {
			if (isPrevious) this.loadedAllMessages = true;
			else this.loadedAllNewerMessages = true;
			return;
		}
		const isLoading = isPrevious ? this.isLoadingPreviousMessages : this.isLoadingNewerMessages;
		const loadedAll = isPrevious ? this.loadedAllMessages : this.loadedAllNewerMessages;
		if (isLoading || loadedAll) return;

		if (isPrevious) this.isLoadingPreviousMessages = true;
		else this.isLoadingNewerMessages = true;

		try {
			const params = new URLSearchParams();
			if (isPrevious) {
				params.append("lastMessageId", edgeMessage.id);
				params.append("lastMessageTimestamp", edgeMessage.date.toISOString());
			} else {
				params.append("recentMessageId", edgeMessage.id);
				params.append("recentMessageTimestamp", edgeMessage.date.toISOString());
			}

			const url = `${window.fullBaseUrl}/chat/${channelId}/${direction}?${params.toString()}`;
			const response = await fetch(url);
			let messages = await response.json();
			messages = messages.map((m) => this.#parseMessage(m));

			if (messages.length === 0) {
				if (isPrevious) this.loadedAllMessages = true;
				else this.loadedAllNewerMessages = true;
				return;
			}

			if (isPrevious) {
				messages.reverse();
				this.#handleIncomingPreviousMessages(messages);
				this.topMessage = messages[0];
			} else {
				messages.forEach((m) => this.#handleLazyLoadedIncomingMessage(m));
				this.bottomMessage = messages[messages.length - 1];
			}
		} catch (error) {
			console.error(error);
		} finally {
			if (isPrevious) this.isLoadingPreviousMessages = false;
			else this.isLoadingNewerMessages = false;
		}
	}


}
