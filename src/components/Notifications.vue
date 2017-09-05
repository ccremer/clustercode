<template>
    <div class="row" v-if="notifications.length">
        <div class="col-lg-12">
            <template v-for="notification in notifications">
                <div class="alert"
                     v-bind:class="getClass(notification)">
                    <button type="button"
                            v-if="notification.dismissible"
                            v-on:click="dismiss(notification)"
                            class="close">×
                    </button>
                    {{notification.message}}
                </div>
            </template>
        </div>
    </div>
</template>

<script>
    import Notification from "../js/notifications";
    import {action_types} from "../store/module.notification"

    export default {
        name: 'notification',
        data() {
            return {
                title: "Clustercode WebAdmin UI"
            }
        },
        computed: {
            notifications() {
                return this.$store.getters.getNotifications;
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
                this.$store.dispatch(action_types.CLEAR, notification);
            }
        }
    }
</script>

