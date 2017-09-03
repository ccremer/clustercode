import Vue from 'vue'
import Vuex from "vuex"
import NavigationModule from "./module.navigation"
import SettingsModule from "./module.settings"
import NotificationModule from "./module.notification"

Vue.use(Vuex);

export const store = new Vuex.Store({
    modules: {
        navigation: NavigationModule,
        settings: SettingsModule,
        notifications: NotificationModule
    }
});
