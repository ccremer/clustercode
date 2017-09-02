<template>
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
</template>

<script>
    import {ServerTable, ClientTable, Event} from "vue-tables-2";
    import Vue from "vue";
    import Axios from "axios/dist/axios.min";
    import Errors from "../js/clustercode";
    import Notification from "../js/notifications";

    Vue.use(ClientTable);

    export default {
        name: 'tasklist',
        data() {
            return {
                tableData: [
                    {percentage: 80, name: "Task 4"},
                    {percentage: 40, name: "Task yay"},
                    {percentage: 40, name: "Task yay"},
                    {percentage: 30, name: "Task yay"},
                    {percentage: 10, name: "Task yasdfy"},
                    {percentage: 40, name: "Task yay"},
                    {percentage: 60, name: "Task yasayay"},
                    {percentage: 50, name: "Task yay"}
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
                Axios.get(cc.clustercodeUrl + "/tasks")
                    .then(function (response) {
                        self.tableData = response.data;
                        setTimeout(self.loadTaskData, 10000);
                    })
                    .catch(function (error) {
                        console.log(error.message);
                        self.$store.commit("addNotification",
                            new Notification(Notification.LEVEL.ERROR, "Backend: " + error.message, true))
                    });
            }

        },
        mounted: function () {
            this.loadTaskData();
        },
        beforeDestroy: function () {
            this.destroyed = true;
        }
    }
</script>

