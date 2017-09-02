<template>
    <div class="row" v-if="notifications.length">
        <template v-for="notification in notifications">
                <div class="alert"
                     v-bind:class="getClass(notification)"
                >
                    <button type="button"
                            v-if="notification.dismissable"
                            class="close"
                            data-dismiss="alert"
                            v-on:click="dismiss(notification)"
                            aria-hidden="true">
                        ×
                    </button>
                    {{notification.message}}
                </div>
        </template>
    </div>
</template>

<script>
    import Notification from "../js/notifications";

    export default {
        name: 'notification',
        data() {
            return {
                title: "Clustercode WebAdmin UI"
            }
        },
        computed: {
            notifications() {
                return this.$store.state.notifications;
            }
        },
        methods: {
            getClass(notification) {
                return {
                    "alert-danger": notification.level === Notification.LEVEL.ERROR,
                    "alert-warning": notification.level === Notification.LEVEL.WARN,
                    "alert-success": notification.level === Notification.LEVEL.SUCCESS,
                    "alert-info": notification.level === Notification.LEVEL.INFO
                }
            },
            dismiss(notification) {
                this.$store.commit("removeNotification", notification);
            }
        }
    }
</script>

