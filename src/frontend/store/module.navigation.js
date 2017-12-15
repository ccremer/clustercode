export const TITLE_MUTATION = "title";

const NavigationModule = {
    state: {
        title: "Welcome"
    },
    mutations: {
        title: function (state, title) {
            state.title = title;
        }
    }
};

export default NavigationModule
