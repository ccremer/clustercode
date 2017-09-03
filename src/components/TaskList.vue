<template>
    <div class="text-left row">
        <p class="text-left">
            This page displays the tasks that are currently being processed by the cluster members.
        </p>
        <v-client-table :data="tableData" :columns="columns" :options="options">
            <template slot="percentage" scope="props">
                <div style="width: 100%;">
                    <div class="progress progress-striped active" style="width: calc(100% - 35px); float: left">
                        <div class="progress-bar progress-bar-success"
                             role="progressbar"
                             :aria-valuenow="props.row.percentage"
                             aria-valuemin="0"
                             aria-valuemax="100"
                             :style="{width: props.row.percentage + '%'}"
                        ></div>
                    </div>
                    <div style="width: 30px; float: right">{{props.row.percentage}}%</div>
                </div>
            </template>
        </v-client-table>
    </div>
</template>

<script>
    import {ServerTable, ClientTable, Event} from "vue-tables-2";
    import Vue from "vue";
    import Axios from "axios/dist/axios.min";
    import Notification from "../js/notifications";
    import {TITLE_MUTATION} from "../store/module.navigation"
    import {ADD_NOTIFICATION, CLEAR_NOTIFICATION, containsNotificationKey} from "../store/module.notification"

    Vue.use(ClientTable);

    export default {
        name: 'tasklist',
        data() {
            return {
                tableData: [
                    {percentage: 80, name: "Task 4"},
                ],
                columns: [
                    "source", "percentage"
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
                        column: "percentage",
                        ascending: false
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
                        self.tableData = response.data;
                        if (containsNotificationKey("TASK_FETCH")) self.$store.commit(CLEAR_NOTIFICATION, "TASK_FETCH");
                        setTimeout(self.loadTaskData, 10000);
                    })
                    .catch(function (error) {
                        console.log(error.message);
                        self.addNotification("Backend: " + error.message, Notification.LEVEL.ERROR, true)
                    });
            },
            addNotification: function (message, level, dismissible) {
                let notification = new Notification(level, message, dismissible);
                notification.key = "TASKS";
                this.$store.commit(ADD_NOTIFICATION, notification);
            }
        },
        mounted: function () {
            this.$store.commit(TITLE_MUTATION, "Tasks");
            let notification = new Notification(Notification.LEVEL.INFO, "Fetching data. Please wait...");
            notification.key = "TASK_FETCH";
            this.$store.commit(ADD_NOTIFICATION, notification);
            this.loadTaskData();
        },
        beforeDestroy: function () {
            this.destroyed = true;
        }
    }
</script>

