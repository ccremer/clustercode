package clustercode.impl.scan;

import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileParser;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.ext.XLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XSlf4j
public class ProfileParserImpl implements ProfileParser {

    public static final Pattern FORMAT_PATTERN = Pattern.compile("%\\{([a-zA-Z]+)=(.*)\\}");

    @Override
    public Optional<Profile> parseFile(Path path) {
        log.entry(path);
        try {
            List<String> lines = Files
                    .lines(path)
                    .map(String::trim)
                    .filter(this::isNotCommentLine)
                    .collect(Collectors.toList());
            return log.exit(Optional.of(
                    Profile.builder()
                            .arguments(lines.stream()
                                    .filter(this::isNotFieldLine)
                                    .collect(Collectors.toList()))
                            .fields(lines.stream()
                                    .filter(this::isFieldLine)
                                    .collect(Collectors.toMap(this::extractKey, this::extractValue)))
                            .location(path)
                            .build()));
        } catch (IOException e) {
            log.catching(XLogger.Level.WARN, e);
            return log.exit(Optional.empty());
        }
    }

    /**
     * Returns the key (first group) extracted from {@link #FORMAT_PATTERN}.
     *
     * @param t
     * @return the string with the key in upper case.
     */
    String extractKey(String t) {
        Matcher m = FORMAT_PATTERN.matcher(t);
        m.find();
        return m.group(1).toUpperCase(Locale.ENGLISH);
    }

    /**
     * Returns the value (second group) extracted from {@link #FORMAT_PATTERN}.
     *
     * @param t
     * @return the string with the value.
     */
    String extractValue(String t) {
        Matcher m = FORMAT_PATTERN.matcher(t);
        m.find();
        return m.group(2);
    }

    /**
     * Negates {@link #isFieldLine(String)}.
     */
    boolean isNotFieldLine(String s) {
        return !isFieldLine(s);
    }

    /**
     * Returns true if the given string begins with "%{", ends with "}" and has a "=" in between.
     *
     * @param s
     * @return
     */
    boolean isFieldLine(String s) {
        return FORMAT_PATTERN.matcher(s).find();
    }

    /**
     * Negates {@link #isCommentLine(String)}.
     */
    boolean isNotCommentLine(String s) {
        return !isCommentLine(s);
    }

    /**
     * Tests whether the given string is a comment. Comments are empty lines or lines starting with "#" at the
     * beginning.
     *
     * @param subject the line to test, not null.
     * @return true if the line is comment.
     * @throws NullPointerException if subject is null.
     */
    boolean isCommentLine(String subject) {
        return subject.startsWith("#") || "".equals(subject);
    }

}
