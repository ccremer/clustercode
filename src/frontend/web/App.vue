<template>
        <div id="wrapper">
            <navigation/>
            <vuedal/>
            <div id="page-wrapper">
                <page-header/>
                <notifications/>
                <div class="row">
                    <div class="col-lg-12">
                        <router-view/>
                    </div>
                </div>
            </div>
        </div>
       <!-- <footer class="footer hidden-xs">
            <div class="container-fluid bottom-align-text">
                <p class="text-muted small">Clustercode, built with Bootstrap and Vue.js</p>
            </div>
        </footer>-->
</template>

<script>
    import Navigation from "../components/Navigation"
    import ContentTitle from "../components/ContentTitle"
    import Notifications from "../components/Notifications"
    import {Component as Vuedal} from "vuedals"

    export default {
        name: 'app',
        components: {
            "navigation": Navigation,
            "page-header": ContentTitle,
            "notifications": Notifications,
            "vuedal": Vuedal
        }
    }
</script>
