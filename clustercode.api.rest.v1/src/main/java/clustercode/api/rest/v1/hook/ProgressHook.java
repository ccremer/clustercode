package clustercode.api.rest.v1.hook;

public interface ProgressHook {

    /**
     * Gets the most recent percentage from the transcoding progress.
     *
     * @return a decimal value between 0 and 100, -1 if not job active.
     */
    double getPercentage();

}
