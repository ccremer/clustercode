<template>
    <div>
        <p class="text-left">
            This page displays the tasks that are currently being processed by the cluster members.
        </p>
        <v-client-table name="tasks" :data="tableData" :columns="columns" :options="options">
            <template slot="progress" scope="props">
                <div>
                    <div class="progress progress-striped active col-xs-7">
                        <div class="progress-bar progress-bar-success"
                             role="progressbar"
                             :style="{width: props.row.progress + '%'}"
                        ></div>
                    </div>
                    <div class="col-xs-1 progress-label">{{props.row.progress}}%</div>
                </div>
            </template>
            <template slot="h__nodename" scope="props">Host</template>
            <template slot="h__percentage" scope="props">Progress</template>
            <template slot="percentage" scope="props">
                <div class="col-xs-1 progress-label">{{props.row.progress}}%</div>
            </template>
        </v-client-table>
    </div>
</template>

<script>
    import {ClientTable} from "vue-tables-2";
    import Vue from "vue";
    import Axios from "axios/dist/axios.min";
    import Notification from "../js/notifications";
    import {TITLE_MUTATION} from "../store/module.navigation"
    import {ADD_NOTIFICATION, CLEAR_NOTIFICATION, containsNotificationKey} from "../store/module.notification"

    Vue.use(ClientTable, {}, true);

    const TASK_FAIL_KEY = "TASK_FAIL";
    const TASK_FETCH_KEY = "TASK_FETCH";

    export default {
        name: 'tasklist',
        data() {
            return {
                tableData: [],
                columns: [
                    "source", "nodename", "progress", "percentage"
                ],
                options: {
                    filterable: false,
                    texts: {
                        count: "",
                        filter: 'Filter Results:',
                        filterPlaceholder: 'Search query',
                        limit: 'Records:',
                        noResults: 'No tasks in process',
                        page: 'Page:', // for dropdown pagination
                        filterBy: 'Filter by {column}', // Placeholder for search fields when filtering by column
                        loading: 'Loading...', // First request to server
                        defaultOption: 'Select {column}' // default option for list filters
                    },
                    orderBy: {
                        column: "progress",
                        ascending: false
                    },
                    columnsDisplay: {
                        "percentage": "mobile",
                        "progress": "not_mobile",
                        "nodename": "not_mobile"
                    }
                },
                destroyed: false
            }
        },
        methods: {
            loadTaskData: function () {
                if (this.destroyed) return;
                let self = this;
                Axios.get(self.$store.state.settings.taskUrl)
                    .then(function (response) {
                        try {
                            self.$store.state.tasks.data = response.data;
                            self.removeFetchNotification();
                            self.removeFailedNotification();
                            setTimeout(self.loadTaskData, 10000);
                        } catch (error) {
                            console.error(error)
                        }
                    })
                    .catch(function (error) {
                        self.removeFetchNotification();
                        self.addFailNotification("Backend: " + error.message);
                        setTimeout(self.loadTaskData, 10000);
                    });
            },
            addFailNotification: function (message) {
                if (containsNotificationKey(TASK_FAIL_KEY)) return;
                let notification = new Notification(Notification.LEVEL.ERROR, message, true);
                notification.key = TASK_FAIL_KEY;
                this.$store.commit(ADD_NOTIFICATION, notification);
            },
            removeFetchNotification: function () {
                if (containsNotificationKey(TASK_FETCH_KEY)) this.$store.commit(CLEAR_NOTIFICATION, TASK_FETCH_KEY);
            },
            removeFailedNotification: function () {
                if (containsNotificationKey(TASK_FAIL_KEY)) this.$store.commit(CLEAR_NOTIFICATION, TASK_FAIL_KEY);
            }
        },
        mounted: function () {
            this.$store.commit(TITLE_MUTATION, "Tasks");
            let notification = new Notification(Notification.LEVEL.INFO, "Fetching data. Please wait...");
            notification.key = TASK_FETCH_KEY;
            this.$store.commit(ADD_NOTIFICATION, notification);
            this.loadTaskData();
        },
        beforeDestroy: function () {
            this.destroyed = true;
        }
    }
</script>

