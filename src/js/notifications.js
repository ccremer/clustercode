const LEVEL =  {
    SUCCESS: "Success",
    INFO: "Info",
    WARN: "Warn",
    ERROR: "Error"
};

export default class Notification {
    constructor(level, message, dismissible) {
        this.message = message;
        this.level = level;
        this.dismissible = dismissible !== undefined && dismissible;
        this.key = null;
        this.autotimeout = false;
    }
}

Notification.LEVEL = LEVEL;



