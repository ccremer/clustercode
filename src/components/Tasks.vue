<template>
    <ul class="dropdown-menu dropdown-tasks">
        <template v-for="task in tasks">
            <li>
                <a href="#">
                    <div>
                        <p>
                            <strong>{{task.name}}</strong>
                            <span class="pull-right text-muted">{{task.percentage}}% Complete</span>
                        </p>
                        <div class="progress progress-striped active">
                            <div class="progress-bar progress-bar-success" role="progressbar"
                                 :aria-valuenow="task.percentage"
                                 aria-valuemin="0" aria-valuemax="100" :style="{width: task.percentage + '%'}">
                                <span class="sr-only">{{task.percentage}}% Complete (success)</span>
                            </div>
                        </div>
                    </div>
                </a>
            </li>
            <li class="divider"></li>
        </template>
    </ul>
</template>

<script>
    export default {
        name: 'tasks',
        data() {
            return {
                tasks: [
                    {percentage: 80, name: "Task 4"},
                    {percentage: 40, name: "Task yay"}
                ]
            }
        }
    }
</script>

