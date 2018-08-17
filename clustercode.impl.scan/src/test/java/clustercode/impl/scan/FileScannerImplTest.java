package clustercode.impl.scan;

import clustercode.test.util.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FileScannerImplTest implements FileBasedUnitTest {

    private FileScannerImpl subject;

    @Before
    public void setUp() throws Exception {
        setupFileSystem();
        subject = new FileScannerImpl();
    }


    @Test
    public void scan_ShouldReturnEmptyList_IfSearchDirDoesNotExist() throws Exception {
        Path searchDir = getPath("input");

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true)
                                              .scan();

        assertThat(results.isPresent()).isFalse();
    }

    @Test
    public void scan_ShouldFindOneFile_IfRecursionIsDisabled() throws Exception {
        Path searchDir = getPath("input");
        Path testMedia = createFile(searchDir.resolve("media.mp4"));
        createFile(searchDir.resolve("subdir/ignored.mp4"));

        Optional<List<Path>> results = subject.searchIn(searchDir).withDepth(1).withRecursion(false)
                                              .scan();

        assertThat(results.get()).containsExactly(testMedia);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void scan_ShouldFindDirectory_IfDirSearchIsEnabled() throws Exception {
        Path searchDir = getPath("input");

        Path subdir = createDirectory(searchDir.resolve("subdir"));

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true).withDirectories(true)
                                              .scan();

        assertThat(results.get()).containsExactly(subdir);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void scan_ShouldFindRecursiveFile_IfFileExists() throws Exception {
        Path searchDir = getPath("input");

        Path testMedia = createFile(searchDir.resolve("subdir/media.mp4"));

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true)
                                              .scan();

        assertThat(results.get()).containsExactly(testMedia);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList(".mp4"));
        Path testFile = getPath("something.mp4");

        assertThat(subject.hasAllowedExtension(testFile)).isTrue();
    }

    @Test
    public void hasAllowedExtension_ShouldReturnFalse_IfNotHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList("mp4"));
        Path testFile = getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile)).isFalse();
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfNoFilterInstalled() throws Exception {
        Path testFile = getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnFalseIfFileExists() throws Exception {
        String ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        Path testFile = createFile(getPath("foo", "bar.ext"));
        createFile(getPath("foo", "bar.ext.done"));

        assertThat(subject.hasNotCompanionFile(testFile)).isEqualTo(false);
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrueIfFileNotExists() throws Exception {
        String ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        Path testFile = createFile(getPath("foo", "bar.ext"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrue_IfExtensionNotProvided() throws Exception {
        Path testFile = createFile(getPath("foo", "bar.ext"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrue_IfMarkDirProvided_ButFileNotFound() throws Exception {
        String ext = ".done";
        Path testFile = createFile(getPath("foo", "bar.ext"));
        Path dir = createDirectory(getPath("mark"));

        subject.whileSkippingExtraFilesWith(ext)
               .whileSkippingExtraFilesIn(dir)
               .searchIn(getPath("input", "foo"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnFalse_IfFileExistsInDirectory() throws Exception {
        String ext = ".done";
        Path inputFolder = getPath("input", "0");
        Path testFile = createFile(inputFolder.resolve(("bar.ext")));
        createFile(getPath("mark", "0", "bar.ext" + ext));

        subject.whileSkippingExtraFilesWith(ext)
               .whileSkippingExtraFilesIn(getPath("mark"))
               .searchIn(inputFolder);

        assertThat(subject.hasNotCompanionFile(testFile)).isFalse();
    }

    @Test
    public void stream_ShouldReturnEmptyStream_IfIOExceptionOccurred() throws Exception {
        Path testDir = getPath("foo", "bar");
        assertThat(subject.searchIn(testDir).streamAndIgnoreErrors()).isEmpty();
    }

    @Test
    public void emptyStreamOnError_ShouldThrowException_IfIOExceptionOccurred() throws Exception {
        Path testDir = getPath("foo", "bar");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                subject.searchIn(testDir).stream());
    }
}
