<template>
    <div>
        <p class="text-left">
            This page displays the tasks that are currently being processed by the cluster members.
        </p>
        <v-client-table name="tasks" :data="tableData" :columns="columns" :options="options">
            <template slot="progress" slot-scope="props">
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
            <template slot="h__nodename" slot-scope="props">Host</template>
            <template slot="h__percentage" slot-scope="props">Progress</template>
            <template slot="percentage" slot-scope="props">
                <div class="col-xs-1 progress-label">{{props.row.progress}}%</div>
            </template>
            <template slot="actions" slot-scope="props">
                <a href="javascript:void(0)" v-on:click="confirmCancel(props.row.nodename)">Cancel</a>
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
    import {action_types} from "../store/module.notification"
    import ConfirmCancelDialog from "./ConfirmCancelDialog"

    Vue.use(ClientTable, {}, true);

    const TASK_FAIL_KEY = "TASK_FAIL";
    const TASK_FETCH_KEY = "TASK_FETCH";
    const TASK_CANCEL_KEY = "TASK_CANCEL";

    export default {
        name: 'tasklist',
        data() {
            return {
                tableData: [],
                columns: [
                    "source", "nodename", "progress", "percentage", "actions"
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
                    },
                    saveState: true
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
                            console.error(error);
                        }
                    })
                    .catch(function (error) {
                        self.removeFetchNotification();
                        self.addFailNotification("Backend: " + error.message);
                        setTimeout(self.loadTaskData, 10000);
                    });
            },
            confirmCancel: function (hostname) {
                let self = this;
                this.$vuedals.open({
                    name: "confirm",
                    props: {
                        message: "Really stop task on " + hostname + "?",
                        btnclass: "btn-danger",
                        data: hostname,
                        proceedtext: "Yes!"
                    },
                    escapable: true,
                    component: ConfirmCancelDialog,
                    onClose(response) {
                        if (response.result === "ok") {
                            self.cancelTask(response.data);
                        }
                    }

                })
            },
            cancelTask: function (hostname) {
                let self = this;
                Axios.delete(self.$store.state.settings.taskCancelUrl + "?hostname=" + hostname)
                    .then(function () {
                        let n = new Notification(Notification.LEVEL.SUCCESS, "Cancelled job on " + hostname, TASK_CANCEL_KEY);
                        self.$store.dispatch(action_types.ADD_WITH_TIMEOUT, n);
                        self.addFetchNotification();
                    })
                    .catch(function (error) {
                        console.error(error);
                        self.addFailNotification("Backend: " + error.message);
                    });
            },
            addFailNotification: function (message) {
                let notification = new Notification(Notification.LEVEL.ERROR, message, TASK_FAIL_KEY);
                notification.dismissible = true;
                this.$store.dispatch(action_types.ADD, notification);
            },
            removeFetchNotification: function () {
                this.$store.dispatch(action_types.CLEAR, TASK_FETCH_KEY);
            },
            removeFailedNotification: function () {
                this.$store.dispatch(action_types.CLEAR, TASK_FAIL_KEY);
            },
            addFetchNotification: function () {
                let n = new Notification(Notification.LEVEL.INFO, "Fetching data. Please wait...", TASK_FETCH_KEY);
                this.$store.dispatch(action_types.ADD, n);
            }
        },
        mounted: function () {
            this.$store.commit(TITLE_MUTATION, "Tasks");
            this.addFetchNotification();
            this.loadTaskData();
        },
        beforeDestroy: function () {
            this.destroyed = true;
        }
    }
</script>

