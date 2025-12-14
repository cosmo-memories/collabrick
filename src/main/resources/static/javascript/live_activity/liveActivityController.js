/**
 * Handles the coordination between the activity UI and the activity handler that interfaces with the websockets
 */
export class LiveActivityController {

    /**
     * @param  {LiveActivityUI} liveActivityUi - The UI instance used to display activity
     * @param {LiveActivityHandler} liveActivityHandler - handles sending and receiving activity through the websockets
     */
    constructor(liveActivityUi, liveActivityHandler) {
        this.liveActivityUi = liveActivityUi;
        this.liveActivityHandler = liveActivityHandler;
    }

    /**
     * Initializes the activity controller by setting up UI listeners and Websocket activity handlers
     */
    init() {
        this.liveActivityUi.initializeActivityFeed();
        this.liveActivityHandler.registerLiveActivityHandler(this.#handleIncomingActivity.bind(this))
    }

    /**
     * Handles activity received from the activityHandler and forwards them to the UI
     * @param activity - The incoming activity object
     */
    #handleIncomingActivity(activity) {
        this.liveActivityUi.addActivityItem(activity);
    }

}