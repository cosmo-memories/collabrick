import { ChatController } from "./chatController.js";
import { UI } from "./chatUI.js";
import { socketManager } from "./socketManager.js";

export class ChatHandler {
	constructor() {
		this.messageHandlers = [];
		this.channelConnectedHandlers = [];
	}

	/**
	 * Subscribes to a specific chat topic based on the renovation and channel ID. Clients subscribed to this topic will
	 * receive messages broadcast to this renovation's channel.
	 *
	 * @param {number} renovationId - The ID of the renovation.
	 * @param {number} channelId - The ID of the chat channel within the renovation.
	 */
	async subscribeToChannel(renovationId, channelId) {
		const destination = `/topic/renovation/${renovationId}/channel/${channelId}`;
		socketManager.subscribe(
			destination,
			() => this.channelConnectedHandlers.forEach((handler) => handler()),
			(body) => this.messageHandlers.forEach((handler) => handler(body))
		);
	}

	/**
	 * Sends a message to the server via the STOMP endpoint.
	 *
	 * @param {string} messageContent - The message content to send.
	 */
	async sendMessage(messageContent) {
		if (!socketManager.isConnected) {
			console.error("Cannot send message: STOMP client not connected");
			return;
		}

		//This object is what gets turned into a java IncomingMessage
		const message = JSON.stringify({
			content: messageContent.content,
			channelId: channelId,
			renovationId: renovationId,
			mentions: messageContent.mentions,
		});

		try {
			socketManager.stompClient.publish({
				destination: "/app/chat",
				body: message,
			});
		} catch (error) {
			console.error(`Failed to send message to channel :`, error);
		}
	}

	/**
	 * Registers a handler to be called when a message is received.
	 *
	 * @param {Function} handler - Function that takes the parsed message as argument.
	 */
	registerMessageHandler(handler) {
		if (typeof handler !== "function") {
			throw new Error("Message handler must be a function");
		}
		this.messageHandlers.push(handler);
	}

	/**
	 * Registers a handler to be called once a chat channel is successfully connected.
	 *
	 * @param {Function} handler - Function to run on successful connection.
	 */
	registerSubscribedToChannelHandler(handler) {
		if (typeof handler !== "function") {
			throw new Error("Channel connect handler must be a function");
		}
		this.channelConnectedHandlers.push(handler);
	}
}

/**
 * Main entry point for the chat system.
 */
const ui = new UI();
const chatHandler = new ChatHandler(socketManager);
const chatController = new ChatController(ui, chatHandler);

// Register a handler for when the WebSocket connects, then subscribe to renovations channel.
socketManager.onConnect(() =>
	chatHandler.subscribeToChannel(renovationId, channelId)
);

// Initialise the message manager
chatController.init();
