package net.chrigel.clustercode.scan.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.chrigel.clustercode.scan.ScanSettings;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.util.FilesystemProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class ScanServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ScanServiceImpl subject;
    private FileSystem fs;
    private Path inputDir;

    @Mock
    private ScanSettings scanSettings;

    private Map<Path, List<MediaCandidate>> candidates;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FilesystemProvider.setFileSystem(Jimfs.newFileSystem(Configuration.forCurrentPlatform()));
        fs = FilesystemProvider.getInstance();
        when(scanSettings.getAllowedExtensions()).thenReturn(Arrays.asList(".mp4"));
        when(scanSettings.getBaseInputDir()).thenReturn(fs.getPath("input"));
        when(scanSettings.getSkipExtension()).thenReturn(".done");

        inputDir = scanSettings.getBaseInputDir();
        subject = new ScanServiceImpl(scanSettings, () -> new FileScannerImpl());
    }

    @Test
    public void doExecute_Szenario1() throws Exception {
        Path dir1 = inputDir.resolve("1");
        Path dir2 = inputDir.resolve("2");

        Files.createDirectories(dir1);
        Files.createDirectories(dir2);

        Path file11 = dir1.resolve("file11.mp4");
        Path file12 = dir1.resolve("file12.mp4");
        Path file12_done = dir1.resolve("file12.mp4.done");
        Path file13 = dir1.resolve("file13.mp4");

        Path file21 = dir2.resolve("file21.mp4");
        Path file22 = dir2.resolve("file22.mkv");

        Arrays.asList(file11, file12, file12_done, file13, file21, file22).forEach(this::createFile);

        candidates = subject.retrieveFiles();

        assertThat(candidates.get(dir1).stream().map(c -> c.getSourcePath()).collect(Collectors.toList()),
                hasItems(inputDir.relativize(file11), inputDir.relativize(file13)));
        assertThat(candidates.get(dir2).stream().map(c -> c.getSourcePath()).collect(Collectors.toList()),
                hasItems(inputDir.relativize(file21)));
    }

    @Test
    public void getListOfMediaFiles_ShouldReturnListWithTwoEntries() throws Exception {
        Path dir1 = inputDir.resolve("1");

        Files.createDirectories(dir1);

        Path file11 = dir1.resolve("file11.mp4");
        Path file12 = dir1.resolve("file12.mp4");

        Arrays.asList(file11, file12).forEach(this::createFile);

        List<MediaCandidate> result = subject.getListOfMediaFiles(dir1);

        assertThat(result.stream().map(c -> c.getSourcePath()).collect(Collectors.toList()),
                hasItems(inputDir.relativize(file11), inputDir.relativize(file12)));
        assertThat(result.size(), equalTo(2));
    }

    @Test
    public void getListOfMediaFiles_ShouldReturnListWithOneEntry_AndIgnoreFiles() throws Exception {
        Path dir1 = inputDir.resolve("1");

        Files.createDirectories(dir1);

        Path file11 = dir1.resolve("file11.mp4");
        Path file12 = dir1.resolve("file12.mp4");
        Path file12_done = dir1.resolve("file12.mp4.done");
        Path file13 = dir1.resolve("file13.txt");

        Arrays.asList(file11, file12, file12_done, file13).forEach(this::createFile);

        List<MediaCandidate> result = subject.getListOfMediaFiles(dir1);

        assertThat(result.stream().map(c -> c.getSourcePath()).collect(Collectors.toList()),
                hasItems(inputDir.relativize(file11)));
        assertThat(result.size(), equalTo(1));
    }

    @Test
    public void getListOfMediaFiles_ShouldReturnEmptyList_IfNoFilesFound() throws Exception {
        Path dir1 = inputDir.resolve("1");

        Files.createDirectories(dir1);

        List<MediaCandidate> result = subject.getListOfMediaFiles(dir1);

        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    public void retrieveFiles_ShouldReturnOneEntry_AndIgnoreInvalidDirectories() throws Exception {
        Path dir1 = inputDir.resolve("1");
        Path dir2 = inputDir.resolve("inexistent");

        Files.createDirectories(dir1);
        Files.createDirectories(dir2);

        candidates = subject.retrieveFiles();

        assertThat(candidates.containsKey(dir1), equalTo(true));
        assertThat(candidates.get(dir1).isEmpty(), equalTo(true));
        assertThat(candidates.size(), equalTo(1));
    }


    @Test
    public void doExecute_ShouldThrowException_IfInputDirIsInexistent() throws Exception {
        expectedException.expect(RuntimeException.class);
        subject.retrieveFiles();
    }


    private void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isPriorityDirectory_ShouldReturnTrue_IfDirectoryIsPositive() throws Exception {
        Path dir = inputDir.resolve("2");
        assertThat(subject.isPriorityDirectory(dir), equalTo(true));
    }

    @Test
    public void isPriorityDirectory_ShouldReturnTrue_IfDirectoryIsZero() throws Exception {
        Path dir = inputDir.resolve("0");
        assertThat(subject.isPriorityDirectory(dir), equalTo(true));
    }

    @Test
    public void isPriorityDirectory_ShouldReturnFalse_IfDirectoryIsInvalid() throws Exception {
        Path dir = inputDir.resolve("-1");
        assertThat(subject.isPriorityDirectory(dir), equalTo(false));
    }

    @Test
    public void getNumberFromDir_ShouldReturn2_IfPathBeginsWith2() throws Exception {
        Path dir = inputDir.resolve("2");
        assertThat(subject.getNumberFromDir(dir), equalTo(2));
    }

    @Test
    public void getNumberFromDir_ShouldThrowException_IfPathDoesNotContainNumber() throws Exception {
        expectedException.expect(NumberFormatException.class);
        Path dir = inputDir.resolve("error");
        subject.getNumberFromDir(dir);
    }
}