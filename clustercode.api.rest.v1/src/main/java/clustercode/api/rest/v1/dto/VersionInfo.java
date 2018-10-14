package clustercode.api.rest.v1.dto;

import lombok.*;

@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VersionInfo {

    @NonNull
    @Getter
    private String mainVersion;

}
