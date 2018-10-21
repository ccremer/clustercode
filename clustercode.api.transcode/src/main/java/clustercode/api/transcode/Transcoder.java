package clustercode.api.transcode;

public enum Transcoder {

    FFMPEG,
    HANDBRAKE;

    private Class<? extends TranscodeReport> outputType;

    public Class<? extends TranscodeReport> getOutputType() {
        return outputType;
    }

    public void setOutputType(Class<? extends TranscodeReport> type) {
        this.outputType = type;
    }
}
