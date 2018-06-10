package clustercode.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    /**
     * The location of the profile file.
     */
    private Path location;

    /**
     * The arguments that are parsed from the file.
     */
    private List<String> arguments;

    /**
     * Any additional fields read during parsing.
     */
    private Map<String, String> fields;

}
