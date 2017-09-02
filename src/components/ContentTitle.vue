<template>
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">{{header}}</h1>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'contenttitle',
        computed: {
            header: function () {
                return this.$store.state.pageTitle;
            }
        }
    }
</script>

