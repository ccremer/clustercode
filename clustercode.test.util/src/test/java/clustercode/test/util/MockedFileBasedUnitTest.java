package clustercode.test.util;

import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.when;

public interface MockedFileBasedUnitTest {

    /**
     * Creates a mock of a path with the given structure using the system default file separator. The mock will be
     * configured to return a string when {@link Path#toString()} is called and implements {@link Path#toFile()}.
     *
     * @param first the first occurrence.
     * @param more  more
     * @return a mocked Path using Mockito.
     */
    default Path createPath(String first, String... more) {
        return createPath(File.separatorChar, first, more);
    }

    /**
     * Creates a mock of a path with the given structure using the given file separator. The mock will be
     * configured to return a string when {@link Path#toString()} is called and implements {@link Path#toFile()}.
     *
     * @param separator the file separator char.
     * @param first     the first occurrence.
     * @param more      more
     * @return a mocked Path using Mockito.
     */
    default Path createPath(char separator, String first, String... more) {
        Path path = Mockito.mock(Path.class);
        StringBuilder sb = new StringBuilder(first);
        for (String dir : more) {
            sb.append(separator).append(dir);
        }
        when(path.toString()).thenReturn(sb.toString());
        when(path.toFile()).thenReturn(new File(sb.toString()));
        return path;
    }

}
