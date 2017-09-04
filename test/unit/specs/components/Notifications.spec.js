import Vue from "vue"
import Vuex from "vuex"
import {mount} from "avoriaz"
import Notifications from "@/components/Notifications"
import Notification from "@/js/notifications"

Vue.use(Vuex);

describe("Notifications.vue", () => {

    let state;
    let store;

    beforeEach("setup", () => {

        state = {
            notifications: {
                list: []
            }
        };

        store = new Vuex.Store({
            state,
        });
    });

    it("should render no content if no notifications", () => {
        const wrapper = mount(Notifications, {store});
        expect(wrapper.find("div")).to.be.empty;
    });

    it("should render only one notification", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.INFO, "message"));
        const wrapper = mount(Notifications, {store});
        expect(wrapper.find(".alert")).to.have.lengthOf(1);
    });

    it("should render notification without button if not dismissible", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.INFO, "message"));
        const wrapper = mount(Notifications, {store});
        expect(wrapper.contains("button")).to.be.false;
    });

    it("should render notification with message", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.INFO, "message"));
        const wrapper = mount(Notifications, {store});
        expect(wrapper.first(".alert").text().trim()).to.be.equal("message");
    });

    it("should render notification with button if dismissible", () => {
        const notification = new Notification(Notification.LEVEL.INFO, "message");
        notification.dismissible = true;
        state.notifications.list.push(notification);
        const wrapper = mount(Notifications, {store});
        const button = wrapper.first("button");
        expect(button.hasClass("close")).to.be.true;
    });

    it("should render info notification", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.INFO, "message"));
        const wrapper = mount(Notifications, {store});
        const div = wrapper.first(".alert");
        expect(div.hasClass("alert-info")).to.be.true;
    });

    it("should render warning notification", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.WARN, "message"));
        const wrapper = mount(Notifications, {store});
        const div = wrapper.first(".alert");
        expect(div.hasClass("alert-warning")).to.be.true;
    });

    it("should render error notification", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.ERROR, "message"));
        const wrapper = mount(Notifications, {store});
        const div = wrapper.first(".alert");
        expect(div.hasClass("alert-danger")).to.be.true;
    });

    it("should render success notification", () => {
        state.notifications.list.push(new Notification(Notification.LEVEL.SUCCESS, "message"));
        const wrapper = mount(Notifications, {store});
        const div = wrapper.first(".alert");
        expect(div.hasClass("alert-success")).to.be.true;
    });
});
