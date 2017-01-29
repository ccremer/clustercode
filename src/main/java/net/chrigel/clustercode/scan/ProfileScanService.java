package net.chrigel.clustercode.scan;

import net.chrigel.clustercode.task.Media;

import java.util.Optional;

public interface ProfileScanService {

    /**
     * Scans and parses the most matching profile for the given candidate. <p>A special algorithm comes to play when
     * selecting the profile. This is the order in which profiles get selected, where {@code /input} is considered the
     * base input directory, and {@code /profiles} the folder for templates.</p>
     * <ol>
     * <li>A file named like the media file, with file extension [.handbrake|.ffmpeg].</li>
     * <li>A file named profile.[handbrake|ffmpeg] in the same relative directory structure as the media candidate in
     * the profiles folder. The structure would be scanned in reverse recursion, up until the priority input dir.</li>
     * <li>If even then a profile file could not be found, then {@code /profiles/default.[handbrake|ffmpeg]} is
     * applied.</li>
     * </ol>
     * <p>
     * Examples. Consider the following existing files in the {@code /input} directory.
     * <ul>
     * <li>/input/0/movies/subdir/movie1.mp4</li>
     * <li>/input/0/movies/movie2.mp4</li>
     * <li>/input/0/movies/movie2.mp4.ffmpeg</li>
     * <li>/input/0/movies/movie3.mp4</li>
     * <li>/input/1/movie4.mkv</li>
     * <li>/input/2/movie5.avi</li>
     * </ul>
     * Now {@code /profiles} is the root dir for the profiles. The following files exist in there:
     * <ul>
     * <li>/profiles/0/movies/subdir/profile.ffmpeg</li>
     * <li>/profiles/0/movies/profile.ffmpeg</li>
     * <li>/profiles/0/profile.ffmpeg</li>
     * <li>/profiles/1/profile.ffmpeg</li>
     * <li>/profiles/default.ffmpeg</li>
     * <li>/profiles/default.handbrake</li>
     * </ul>
     * </p>
     * The applied profiles for FFMPEG would be as follows:
     * <ul>
     * <li>{@code /input/0/movies/subdir/movie1.mp4} -> {@code /profiles/0/movies/subdir/profile.ffmpeg} since there is
     * no profile in the input dir where it resides, but a default profile exists for {@code subdir}.</li>
     * <li>{@code /input/0/movies/movie2.mp4} -> {@code /input/0/movies/movie2.mp4.ffmpeg} since this file is
     * specifically for the media itself.</li>
     * <li>{@code /input/0/movies/movie3.mp4} -> {@code /profiles/0/movies/profile.ffmpeg} since it matches partially
     * the parent directory.</li>
     * <li>{@code /input/1/movie4.mkv} -> {@code /profiles/1/profile.ffmpeg} since this profile is the default profile
     * for priority {@code 1} media.</li>
     * <li>{@code /input/2/movie5.avi} -> {@code /profiles/default.ffmpeg} since no profile has been found in priority
     * directory 2.</li>
     * </ul>
     * Note that the profiles only apply for FFMPEG-enabled nodes. For nodes running on Handbrake every media will be
     * having the default.handbrake profile applied. If not even the default.handbrake exists, then this method returns
     * empty.
     * <p>
     * The profile file will be read and parsed. If the file cannot be read then the next matching file will be tried.
     * The profile will not be validated, this means that the encoder may fail when attempting to transcode.
     * </p>
     *
     * @param candidate the selected media job, not null. The instance will not be modified.
     * @return an empty profile if it could not be parsed for some reason. Otherwise contains the most appropriate
     * profile for the internally configured transcoder.
     */
    Optional<Profile> selectProfile(Media candidate);

}
