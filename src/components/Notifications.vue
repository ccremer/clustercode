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

    export default {
        name: 'notification',
        data() {
            return {
                title: "Clustercode WebAdmin UI"
            }
        },
        computed: {
            notifications() {
                return this.$store.state.notifications.list;
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
                console.log("dismissing");
                this.$store.commit("removeNotification", notification);
            }
        }
    }
</script>

