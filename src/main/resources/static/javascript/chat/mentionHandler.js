import { socketManager } from "./socketManager.js";
import {MentionController} from "./mentionController.js";
import {MentionUI} from "./mentionUI.js";

export class MentionHandler {
	constructor() {
		this.mentionHandlers = [];
	}

	/**
	 * Subscribes to a user specific channel based on the user id.
	 * The client that subscribes to this channel with be the user associated with this id, and they will receive
	 * notifications of when they have been mentioned in a chat
	 *
	 * @param {number} userId - the id of the user being mentioned (and thus, receiving the message)
	 */
	async subscribeToChannel(userId) {
		const destination = `/topic/mention/${userId}`;
		socketManager.subscribe(
			destination,
			() => {},
			(body) => {
				this.mentionHandlers.forEach((handler) => handler(body));
			}
		);
	}

	/**
	 * Registers a handler to be called when a mention is received. To be called by a
	 *
	 * @param {Function} handler - Function that takes the parsed mention as argument.
	 */
	registerMentionHandler(handler) {
		if (typeof handler !== "function") {
			throw new Error("Mention handler must be a function");
		}
		this.mentionHandlers.push(handler);
	}

}

/**
 * Main entry point for the mention system.
 */
const mentionHandler = new MentionHandler();
window.mentionUI = new MentionUI();
const mentionController = new MentionController(window.mentionUI, mentionHandler)

socketManager.onConnect(() =>
	mentionHandler.subscribeToChannel(userId)
);

mentionController.init()
