<template>
    <div id="wrapper">
        <navigation/>
        <div id="page-wrapper">
            <page-header />
            <notifications />
            <div>
                <div class="ui container">
                    <router-view />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Navigation from "../components/Navigation"
    import ContentTitle from "../components/ContentTitle"
    import Notifications from "../components/Notifications"

    export default {
        name: 'app',
        components: {
            "navigation" : Navigation,
            "page-header" : ContentTitle,
            "notifications" : Notifications
        }
    }
</script>
