import {actions, mutations, getters, mutation_types, util} from "@/frontend/store/module.notification";
import Notification from "@/frontend/js/notifications"

describe("Notification mutation", () => {

    let state = {list: []};

    it("should add new notification", () => {
        const n = new Notification(Notification.LEVEL.INFO, "testing", null);
        mutations.addNotification(state, n);

        expect(state.list).to.have.lengthOf(1);
        expect(state.list[0]).to.equal(n);
    });

    it("should remove existing notification", () => {
        const n = new Notification(Notification.LEVEL.INFO, "testing", null);
        state.list = [
            n
        ];
        mutations.clearNotification(state, n);

        expect(state.list).to.be.empty;
    });

    it("should remove existing notifications by key", () => {
        state.list = [
            new Notification(Notification.LEVEL.INFO, "1", "KEY"),
            new Notification(Notification.LEVEL.ERROR, "2", "KEY")
        ];
        mutations.clearNotificationsByKey(state, "KEY");

        expect(state.list).to.be.empty;
    });

    it("should do nothing when removing notifications by key if no messages present", () => {
        mutations.clearNotificationsByKey(state, "KEY");

        expect(state.list).to.be.empty;
    });

    it("should do nothing when removing notifications by key if key is null", () => {
        state.list = [
            new Notification(Notification.LEVEL.INFO, "1", "KEY"),
            new Notification(Notification.LEVEL.ERROR, "2", "KEY")
        ];
        mutations.clearNotificationsByKey(state, null);

        expect(state.list).to.have.lengthOf(2);
    });

    it("should do clear all notifications", () => {
        state.list = [
            new Notification(Notification.LEVEL.INFO, "1", "KEY"),
            new Notification(Notification.LEVEL.ERROR, "2", "KEY")
        ];
        mutations.clearAll(state);

        expect(state.list).to.be.empty;
    });
});

describe("Notification action", () => {

    let state;
    beforeEach("clean", () => {
        state = {list: []}
    });

    it("should mutate state by adding a notification", () => {
        const commit = (type, payload) => {
            state.list.push(payload);
        };

        const n = new Notification(Notification.LEVEL.INFO, "testing", null);
        actions.add({commit, state, getters}, n);

        expect(state.list).to.have.lengthOf(1);
    });

    it("should skip mutation for new notification if key is existing", () => {
        state.list = [
            new Notification(Notification.LEVEL.INFO, "1", "KEY"),
        ];
        const n = new Notification(Notification.LEVEL.INFO, "testing", "KEY");
        const commit = (type, payload) => {
            fail(type, null, "This commit should not execute!");
        };
        actions.add({commit, state, getters}, n);

        expect(state.list).to.have.lengthOf(1);
    });

    it("should clear all notification if payload is undefined", () => {
        let called = false;
        const commit = (type, payload) => {
            if (type!==mutation_types.CLEAR_ALL) fail(type, mutation_types.CLEAR_ALL, "Wrong commit type.");
            expect(payload).to.equal(undefined);
            called = true;
        };

        actions.clear({commit, getters});

        expect(called).to.be.true;
    });

    it("should clear all notification if payload is null", () => {
        let called = false;
        const commit = (type, payload) => {
            if (type!==mutation_types.CLEAR_ALL) fail(type, mutation_types.CLEAR_ALL, "Wrong commit type.");
            expect(payload).to.equal(undefined);
            called = true;
        };

        actions.clear({commit, getters}, null);

        expect(called).to.be.true;
    });

    it("should clear all notifications by key if payload is string", () => {
        state.list = [
            new Notification(Notification.LEVEL.INFO, "1", "KEY"),
        ];
        let called = false;
        const commit = (type, payload) => {
            if (type!==mutation_types.CLEAR_BY_KEY) fail(type, mutation_types.CLEAR_BY_KEY, "Wrong commit type.");
            expect(payload).to.equal("KEY");
            called = true;
        };

        actions.clear({commit, state, getters}, "KEY");

        expect(called).to.be.true;
    });

    it("should clear all notifications by key if payload is string", () => {
        let n = new Notification(Notification.LEVEL.INFO, "1", "KEY");
        state.list = [
            n
        ];
        let called = false;
        const commit = (type, payload) => {
            if (type!==mutation_types.CLEAR_NOTIFICATION)
                fail(type, mutation_types.CLEAR_NOTIFICATION, "Wrong commit type.");
            expect(payload).to.equal(n);
            called = true;
        };

        actions.clear({commit, state, getters}, n);

        expect(called).to.be.true;
    });

});
