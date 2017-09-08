<template>
    <div>
            <h4>{{displaymessage}}</h4>
        <div class="pull-right">
            <button type="button" class="btn btn-default" v-on:click="cancel()">Cancel</button>
            <button type="button" class="btn" :class="btnClass" v-on:click="ok()">{{proceedText}}</button>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'confirmcanceldialog',
        props: ["message", "data", "btnclass", "proceedtext"],
        computed: {
            displaymessage: function () {
                if (!this.message) return "Are you sure about this?";
                return this.message;
            },
            proceedText: function () {
                if (!this.proceedtext) return "Proceed";
                return this.proceedtext;
            },
            btnClass: function () {
                return {
                    "btn-danger": this.btnclass === "btn-danger",
                    "btn-primary": this.btnclass === "btn-primary",
                    "btn-warning": this.btnclass === "btn-warning",
                    "btn-info": this.btnclass === "btn-info",
                }
            }
        },
        methods: {
            cancel: function () {
                this.$emit("vuedals:close", {result: "cancel", data: this.data});
            },
            ok: function () {
                this.$emit("vuedals:close", {result: "ok", data: this.data});
            }
        }
    }
</script>

