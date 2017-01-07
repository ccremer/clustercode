package net.chrigel.clustercode.scan.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.chrigel.clustercode.util.FilesystemProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileScannerImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private FileScannerImpl subject;
    private FileSystem fs;

    @Before
    public void setUp() throws Exception {
        FilesystemProvider.setFileSystem(Jimfs.newFileSystem(Configuration.forCurrentPlatform()));
        fs = FilesystemProvider.getInstance();
        subject = new FileScannerImpl();
    }


    @Test
    public void scan_ShouldReturnEmptyList_IfSearchDirDoesNotExist() throws Exception {
        Path searchDir = fs.getPath("input");

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true)
                .scan();

        assertThat(results.isPresent(), equalTo(false));
    }

    @Test
    public void scan_ShouldFindOneFile_IfRecursionIsDisabled() throws Exception {
        Path searchDir = fs.getPath("input");
        Files.createDirectories(searchDir);

        Path testMedia = searchDir.resolve("media.mp4");
        Files.createFile(testMedia);

        Path subDir = searchDir.resolve("subdirToIgnore");
        Files.createDirectory(subDir);

        Path ignoreFile = subDir.resolve("ignored.mp4");

        Optional<List<Path>> results = subject.searchIn(searchDir).withDepth(1).withRecursion(false)
                .scan();

        assertThat(results.get(), hasItem(testMedia));
        assertThat(results.get(), not(hasItem(ignoreFile)));
        assertThat(results.get().size(), equalTo(1));
    }

    @Test
    public void scan_ShouldFindDirectory_IfDirSearchIsEnabled() throws Exception {
        Path searchDir = fs.getPath("input");
        Files.createDirectories(searchDir);

        Path subdir = searchDir.resolve("subdir");
        Files.createDirectory(subdir);

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true).withDirectories(true)
                .scan();

        assertThat(results.get(), hasItem(subdir));
        assertThat(results.get().size(), equalTo(1));
    }

    @Test
    public void scan_ShouldFindRecursiveFile_IfFileExists() throws Exception {

        Path searchDir = fs.getPath("input");
        Path subDir = searchDir.resolve("subdir");

        Files.createDirectories(subDir);
        Path testMedia = subDir.resolve("media.mp4");
        Files.createFile(testMedia);

        Optional<List<Path>> results = subject.searchIn(searchDir).withRecursion(true)
                .scan();

        assertThat(results.get(), hasItem(testMedia));
        assertThat(results.get().size(), equalTo(1));
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList(".mp4"));
        Path testFile = fs.getPath("something.mp4");

        assertThat(subject.hasAllowedExtension(testFile), equalTo(true));
    }

    @Test
    public void hasAllowedExtension_ShouldReturnFalse_IfNotHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList("mp4"));
        Path testFile = fs.getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile), equalTo(false));
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfNoFilterInstalled() throws Exception {
        Path testFile = fs.getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile), equalTo(true));
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnFalseIfFileExists() throws Exception {
        String ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        Path testDir = fs.getPath("foo");
        Path testFile = fs.getPath("foo", "bar.ext");
        Path companionFile = fs.getPath("foo", "bar.ext.done");

        Files.createDirectories(testDir);
        Files.createFile(testFile);
        Files.createFile(companionFile);

        assertThat(subject.hasNotCompanionFile(testFile), equalTo(false));
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrueIfFileNotExists() throws Exception {
        String ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        Path testDir = fs.getPath("foo");
        Path testFile = fs.getPath("foo", "bar.ext");

        Files.createDirectories(testDir);
        Files.createFile(testFile);

        assertThat(subject.hasNotCompanionFile(testFile), equalTo(true));
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrue_IfExtensionNotProvided() throws Exception {
        Path testDir = fs.getPath("foo");
        Path testFile = fs.getPath("foo", "bar.ext");

        Files.createDirectories(testDir);
        Files.createFile(testFile);

        assertThat(subject.hasNotCompanionFile(testFile), equalTo(true));
    }

    @Test
    public void stream_ShouldReturnEmptyStream_IfIOExceptionOccurred() throws Exception {
        Path testDir = fs.getPath("foo", "bar");
        assertThat(subject.searchIn(testDir).streamAndIgnoreErrors().count(), equalTo(0L));
    }

    @Test
    public void emptyStreamOnError_ShouldThrowException_IfIOExceptionOccurred() throws Exception {
        expectedException.expect(RuntimeException.class);
        Path testDir = fs.getPath("foo", "bar");
        subject.searchIn(testDir).stream();
    }
}