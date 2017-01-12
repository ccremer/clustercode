package net.chrigel.clustercode.scan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    private Path location;

    private List<String> arguments;

}
