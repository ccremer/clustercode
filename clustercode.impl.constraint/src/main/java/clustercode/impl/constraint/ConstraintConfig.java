package clustercode.impl.constraint;

import clustercode.api.config.converter.PathConverter;
import org.aeonbits.owner.Config;

import java.nio.file.Path;
import java.util.List;

public interface ConstraintConfig extends Config {

    @Key("CC_CONSTRAINT_FILE_REGEX")
    @DefaultValue("")
    String filename_regex();

    /**
     * @return size in MB. x >= 0
     */
    @Key("CC_CONSTRAINT_FILE_MIN_SIZE")
    @DefaultValue("150")
    long min_file_size();

    /**
     * @return size in MB. x >= 0
     */
    @Key("CC_CONSTRAINT_FILE_MAX_SIZE")
    @DefaultValue("0")
    long max_file_size();

    /**
     * Unordered List of constraints.
     * @return one of: ALL, FILE_SIZE, TIME, FILE_NAME, NONE
     */
    @Separator(" ")
    @Key("CC_CONSTRAINTS_ACTIVE")
    @DefaultValue("FILE_SIZE CLUSTER")
    List<Constraints> active_constraints();

    @Key("CC_CONSTRAINT_TIME_BEGIN")
    @DefaultValue("08:00")
    String time_begin();

    @Key("CC_CONSTRAINT_TIME_STOP")
    @DefaultValue("16:00")
    String time_stop();

    @Key("CC_MEDIA_INPUT_DIR")
    @DefaultValue("/input")
    @ConverterClass(PathConverter.class)
    Path base_input_dir();

}
