import { socketManager } from "../chat/socketManager.js";
import { LiveActivityController } from "./liveActivityController.js";
import {LiveActivityUI} from "./liveActivityUI.js";

/**
 * Handles subscribing users and creating handlers for live activity feed websockets.
 */
export class LiveActivityHandler {

    constructor() {
        this.liveActivityHandlers = [];
    }

    /**
     * Subscribes to a user specific channel based on the user id
     * The client that subscribes to this channel with the user associated with this id, and they will receive
     * notifications of when there has been an activity on a renovation they are a member of
     *
     * @param {number} userId - the id of the user logged in
     */
    async subscribeToChannel(userId) {
        const destination = `/topic/feed/${userId}`
        socketManager.subscribe(
            destination,
            () => {
            },
            (body) => {
                this.liveActivityHandlers.forEach((handler) => handler(body));
            }
        )
    }

    /**
     * Registers a handler to be called when an activity is received.
     * @param {Function} handler - Function that takes the parsed mention as argument.
     */
    registerLiveActivityHandler(handler) {
        if (typeof handler !== "function") {
            throw new Error("Live activity handler must be a function")
        }
        this.liveActivityHandlers.push(handler)
    }

}

/**
 * Main entry point for the activity system
 */
const liveActivityHandler = new LiveActivityHandler()
const liveActivityUI = new LiveActivityUI()
const liveActivityController = new LiveActivityController(liveActivityUI, liveActivityHandler)

socketManager.onConnect(() =>
    liveActivityHandler.subscribeToChannel(userId)
)

liveActivityController.init()

