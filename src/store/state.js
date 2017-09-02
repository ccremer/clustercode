import Vue from 'vue'
import Vuex from "vuex"

Vue.use(Vuex);

export const store = new Vuex.Store({
    state: {
        settings: {
            clustercodeUrl: null
        },
        pageTitle: "Hello",
        notifications: []
    },
    mutations: {
        increment(state) {
            state.count++;
        },
        setTitle(state, title) {
            state.pageTitle = title;
        },
        addNotification(state, notification) {
            let self = this;
            state.notifications.push(notification);
            if (notification.dismissable === false) {
                setTimeout(function () {
                    self.commit("removeNotification", notification);
                }, 5000);
            }
        },
        removeNotification(state, notification) {
            let arr = state.notifications;
            let index = arr.indexOf(notification);
            if (index > -1) {
                arr.splice(index, 1);
            }
            state.notifications = arr;
            console.log("removed " + notification);
        },
    }
});
