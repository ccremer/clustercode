(function (cc, Vue, undefined) {

    cc.app4 = new Vue({
        el: '#app-4',
        data: {
            todos: [
                { text: 'Learn JavaScript' },
                { text: 'Learn Vue' },
                { text: 'Build something awesome' }
            ]
        }
    });
    cc.app5 = new Vue({
        el: '#app-5',
        data: {
            message: 'Hello Vue.js!'
        },
        methods: {
            reverseMessage: function () {
                this.message = this.message.split('').reverse().join('')
            }
        }
    });
    cc.app6 = new Vue({
        el: '#app-6',
        data: {
            message: 'Hello Vue!'
        }
    });
    Vue.component('todo-item', {
        props: ['todo'],
        template: '<li>{{ todo.text }}</li>'
    });
    cc.app7 = new Vue({
        el: '#app-7',
        data: {
            groceryList: [
                { id: 0, text: 'Vegetables' },
                { id: 1, text: 'Cheese' },
                { id: 2, text: 'Whatever else humans are supposed to eat' }
            ]
        }
    });
    var vm = new Vue({
        el: '#example',
        data: {
            message: 'Hello'
        },
        computed: {
            // a computed getter
            reversedMessage: function () {
                // `this` points to the vm instance
                return this.message.split('').reverse().join('')
            }
        }
    })

}(window.cc = window.cc || {}, Vue));