<template>
    <!-- NavigationBar -->
    <nav class="navbar navbar-default navbar-static-top" style="margin-bottom: 0">
        <div class="navbar-header">
            <a class="navbar-brand" href="index.html">{{title}}</a>
        </div>
        <!-- /.navbar-header -->

        <ul class="nav navbar-top-links navbar-right">
            <!-- /.dropdown -->
            <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                    <i class="fa fa-tasks fa-fw"></i> <i class="fa fa-caret-down"></i>
                </a>
                <!-- /.dropdown-tasks -->
            </li>
        </ul>
        <!-- /.navbar-top-links -->

        <div class="navbar-default sidebar" role="navigation">
            <div class="sidebar-nav navbar-collapse">
                <ul class="nav" id="side-menu">
                    <li>
                        <router-link to="/">
                            <i class="fa fa-dashboard fa-fw"></i> Tasks
                        </router-link>
                    </li>
                    <li>
                        <router-link to="/input">
                            <i class="fa fa-dashboard fa-fw"></i> Input
                        </router-link>
                    </li>
                </ul>
            </div>
            <!-- /.sidebar-collapse -->
        </div>
        <!-- /.navbar-static-side -->
    </nav>
</template>

<script>
    export default {
        name: 'navigation',
        data() {
            return {
                title: "Clustercode WebAdmin UI"
            }
        }
    }
</script>

