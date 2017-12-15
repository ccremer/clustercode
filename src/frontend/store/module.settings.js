const baseApi = "/api/v1/";

const SettingsModule = {
    state: {
        taskUrl: baseApi + "tasks",
        taskCancelUrl: baseApi + "tasks/stop"
    }
};

export default SettingsModule
