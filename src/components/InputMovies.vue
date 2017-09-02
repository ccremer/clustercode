<template>
    <div id="page-wrapper">
        <page-header />
        <notifications />
        <div>
            <div class="ui container">
                <tasklist/>
            </div>
        </div>
    </div>
</template>

<script>
    import ContentTitle from "./ContentTitle"
    import Tasks from "./TaskList"
    import Notifications from "./Notifications"
    export default {
        name: "input",
        components: {
            "page-header": ContentTitle,
            "tasklist" : Tasks,
            "notifications" : Notifications
        }
    }
</script>

