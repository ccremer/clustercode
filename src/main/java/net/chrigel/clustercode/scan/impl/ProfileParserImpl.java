package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import org.slf4j.ext.XLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XSlf4j
class ProfileParserImpl implements ProfileParser {

    @Override
    public Optional<Profile> parseFile(Path path) {
        log.entry(path);
        try {
            return log.exit(Optional.of(Profile.builder()
                    .arguments(Files
                            .lines(path)
                            .map(String::trim)
                            .filter(this::isNotCommentLine)
                            .flatMap(this::separateWhitespace)
                            .collect(Collectors.toList()))
                    .location(path)
                    .build()));
        } catch (IOException e) {
            log.catching(XLogger.Level.WARN, e);
            return log.exit(Optional.empty());
        }
    }

    /**
     * Negates {@link #isCommentLine(String)}.
     */
    boolean isNotCommentLine(String s) {
        return !isCommentLine(s);
    }

    /**
     * Separates any white spaces in the given string.
     *
     * @param s the string, not null.
     * @return
     */
    Stream<? extends String> separateWhitespace(String s) {
        return Stream.of(s.split(" "));
    }

    /**
     * Tests whether the given string is a comment. Comments are empty lines or lines starting with "#" at the
     * beginning.
     *
     * @param subject
     * @return true if the line is comment.
     * @throws NullPointerException if subject is null.
     */
    boolean isCommentLine(String subject) {
        return subject.startsWith("#") || "".equals(subject);
    }

}
