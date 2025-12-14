/**
 * Manages WebSocket/STOMP connections, subscriptions, and message sending/receiving.
 */
export class SocketManager {
	constructor() {
		this.stompClient = null;
		this.isConnected = false;
		this.connectionHandlers = [];
	}

	/**
	 * Establishes a connection to the WebSocket broker using STOMP.
	 */
	async connect() {
		if (!this.isConnected) {
			this.stompClient = new StompJs.Client({
				brokerURL: webSocketUrl, // variable passed via thymeleaf
				onConnect: this.#onSocketConnect,
				onStompError: this.#onSocketError,
				onWebSocketClose: this.#onSocketDisconnect,
				withCredentials: true,
			});
			await this.stompClient.activate();
		}
	}

	/**
	 * Subscribes to a STOMP destination.
	 *
	 * @param {string} destination - The STOMP topic/queue to subscribe to.
	 * @param {Function} onSubscribe - Called once the subscription is successfully established.
	 * @param {Function} onMessage - Called when a message is received.
	 */
	subscribe(destination, onSubscribe, onMessage) {
		if (!this.isConnected || !this.stompClient) {
			throw new Error("Cannot subscribe: STOMP client is not connected.");
		}
		if (typeof onMessage !== "function") {
			throw new Error("onMessage callback must be a function.");
		}
		if (typeof onSubscribe !== "function") {
			onSubscribe = () => {}; // default no-op
		}

		const subscription = this.stompClient.subscribe(
			destination,
			(message) => {
				try {
					const parsedBody = JSON.parse(message.body);
					onMessage(parsedBody);
				} catch (err) {
					console.warn("Failed to parse STOMP message body:", err);
				}
			}
		);

		onSubscribe(subscription);
	}

	/**
	 * Called when the WebSocket connects successfully.
	 *
	 * @param {Frame} frame - The STOMP connection frame.
	 * @private
	 */
	#onSocketConnect = (frame) => {
		console.log("Socket connected");
		this.isConnected = true;
		this.connectionHandlers.forEach((handler) => handler());
	};

	/**
	 * Called when the WebSocket connection closes.
	 *
	 * @param {CloseEvent} frame - WebSocket close event.
	 * @private
	 */
	#onSocketDisconnect = (frame) => {
		console.log("Socket closed");
	};

	/**
	 * Called when the STOMP client encounters an error.
	 *
	 * @param {Object} error - The STOMP error object.
	 * @private
	 */
	#onSocketError = (error) => {
		console.error("Socket error", error);
	};

	/**
	 * Registers a handler to be called once the WebSocket is successfully connected.
	 *
	 * @param {Function} handler - Function to run on successful connection.
	 */
	onConnect(handler) {
		if (typeof handler !== "function") {
			throw new Error("Connect handler must be a function");
		}
		this.connectionHandlers.push(handler);
	}
}

/**
 * Global socket manager variable
 * @type {SocketManager}
 */
export const socketManager = new SocketManager();

/**
 * Initializes the WebSocket connection once the DOM is fully loaded.
 */
document.addEventListener("DOMContentLoaded", () => {
	socketManager.connect();
});
