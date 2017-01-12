package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import net.chrigel.clustercode.util.PredicateUtil;
import org.slf4j.ext.XLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

@XSlf4j
class ProfileParserImpl implements ProfileParser {

    @Override
    public Optional<Profile> parseFile(Path path) {
        try {
            return Optional.of(Profile.builder()
                    .arguments(Files
                            .lines(path)
                            .map(String::trim)
                            .filter(PredicateUtil.not(this::isCommentLine))
                            .collect(Collectors.toList()))
                    .location(path)
                    .build());
        } catch (IOException e) {
            log.catching(XLogger.Level.WARN, e);
            return Optional.empty();
        }
    }

    /**
     * Tests whether the given string is a comment.
     *
     * @param subject
     * @return true if the line is comment.
     * @throws NullPointerException if subject is null.
     */
    boolean isCommentLine(String subject) {
        return subject.startsWith("#") || "".equals(subject);
    }

}
