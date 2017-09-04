const LEVEL =  {
    SUCCESS: "Success",
    INFO: "Info",
    WARN: "Warn",
    ERROR: "Error"
};

export default class Notification {
    constructor(level, message, key) {
        if (message === undefined) throw new Error("Message cannot be undefined");
        this.message = message;
        this.level = level;
        this.dismissible = false;
        this.key = key === undefined ? null : key;
    }
}

Notification.LEVEL = LEVEL;



