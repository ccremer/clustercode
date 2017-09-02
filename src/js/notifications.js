const LEVEL =  {
    SUCCESS: "Success",
    INFO: "Info",
    WARN: "Warn",
    ERROR: "Error"
};

export default class Notification {
    constructor(level, message, dismissable) {
        this.message = message;
        this.level = level;
        this.dismissable = dismissable !== undefined;
    }

    setDismissable() {
        this.dismissable = true;
    }
}

Notification.LEVEL = LEVEL;



