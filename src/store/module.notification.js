export const ADD_NOTIFICATION = "addNotification";
export const REMOVE_NOTIFICATION = "removeNotification";
export const CLEAR_NOTIFICATION = "clearNotification";

const NotificationModule = {
    state: {
        list: []
    },
    mutations: {
        addNotification(state, notification) {
            let self = this;
            if (notification.key === null || state.list.length === 0) {
                state.list.push(notification);
            }
            else {
                if (!containsNotificationKey(notification.key)) state.list.push(notification);
            }
            if (notification.autotimeout === true) {
                setTimeout(function () {
                    self.commit(REMOVE_NOTIFICATION, notification);
                }, 5000);
            }
        },
        removeNotification(state, notification) {
            let arr = state.list;
            let index = arr.indexOf(notification);
            if (index > -1) {
                arr.splice(index, 1);
                console.log("removed " + notification);
            }
            state.list = arr;
        },
        clearNotification(state, key) {
            if (key === undefined || key === null) {
                state.list = [];
                return;
            }
            state.list.forEach(function (notification, index, arr) {
                if (notification.key === key) {
                    arr.splice(index, 1);
                }
            }, this)
        }
    }
};

export const containsNotificationKey = function (notificationKey) {
    if (notificationKey === undefined || NotificationModule.state.list.length === 0) return false;
    return !NotificationModule.state.list.every(function (element) {
        return element.key !== notificationKey;
    });
};

export default NotificationModule
