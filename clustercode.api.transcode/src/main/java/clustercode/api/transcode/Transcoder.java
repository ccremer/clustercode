package clustercode.api.transcode;

public enum Transcoder {

    FFMPEG,
    HANDBRAKE;

    private Class<? extends TranscodeProgress> outputType;

    public Class<? extends TranscodeProgress> getOutputType() {
        return outputType;
    }

    public void setOutputType(Class<? extends TranscodeProgress> type) {
        this.outputType = type;
    }
}
