package clustercode.impl.cleanup;

import clustercode.api.config.converter.PathConverter;
import clustercode.impl.cleanup.processor.CleanupProcessors;
import org.aeonbits.owner.Config;

import java.nio.file.Path;
import java.util.List;

public interface CleanupConfig extends Config {

    /**
     * Gets the root path of the output directory.
     *
     * @return the path, not null.
     */
    @ConverterClass(PathConverter.class)
    @Key("CC_MEDIA_OUTPUT_DIR")
    @DefaultValue("/output")
    Path base_output_dir();

    @ConverterClass(PathConverter.class)
    @Key("CC_MEDIA_INPUT_DIR")
    @DefaultValue("/input")
    Path base_input_dir();
    /**
     * Returns true if existing files are allowed to be overwritten.
     */
    @Key("CC_CLEANUP_OVERWRITE")
    @DefaultValue("true")
    boolean overwrite_files();

    /**
     * Gets the group id of the new owner of the output file(s).
     */
    @Key("CC_CLEANUP_CHOWN_GROUPID")
    @DefaultValue("0")
    int group_id();

    /**
     * Gets the user id of the new owner of the output file(s).
     */
    @Key("CC_CLEANUP_CHOWN_USERID")
    @DefaultValue("0")
    int user_id();

    /**
     * Gets the root path of the directory in which the sources should get marked as done.
     *
     * @return the path or empty.
     */
    @ConverterClass(PathConverter.class)
    @Key("CC_CLEANUP_MARK_SOURCE_DIR")
    @DefaultValue("/input/done")
    Path mark_source_dir();

    @Key("CC_MEDIA_SKIP_NAME")
    @DefaultValue(".done")
    String skip_extension();

    @Key("CC_CLEANUP_STRATEGY")
    @DefaultValue("STRUCTURED_OUTPUT MARK_SOURCE")
    @Separator(" ")
    List<CleanupProcessors> cleanup_processors();
}
