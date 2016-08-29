/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler, Matthieu Brouillard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.scenicview.extensions.cssfx.module.api;

import static org.scenicview.extensions.cssfx.module.impl.log.CSSFXLogger.logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIToPathConverters {
    private static final Function<String, Path> MAVEN_RESOURCE = new Function<String, Path>() {
        @Override
        public Path apply(String uri) {
            Path p = new SubstringFileMapper(uri)
                    .addEntry("target/classes", "src/main/java")
                    .addEntry("target/classes", "src/main/resources")
                    .addEntry("target/test-classes", "src/test/java")
                    .addEntry("target/test-classes", "src/test/resources")
                    .resolve();
            if (p == null) {
                logger(URIToPathConverters.class).debug("MAVEN converter failed to map css[%s] to a source file", uri);
            }

            return p;
        }
    };

    private static final Function<String, Path> GRADLE_RESOURCE = new Function<String, Path>() {
        @Override
        public Path apply(String uri) {
            Path p = new SubstringFileMapper(uri)
                    .addEntry("build/classes/main", "src/main/java")
                    .addEntry("build/resources/main", "src/main/resources")
                    .addEntry("build/classes/test", "src/test/java")
                    .addEntry("build/resources/test", "src/test/resources")
                    .resolve();

            if (p == null) {
                logger(URIToPathConverters.class).debug("GRADLE converter failed to map css[%s] to a source file", uri);
            }

            return p;
        }
    };

    private static Pattern[] JAR_PATTERNS = { 
            Pattern.compile("jar:file:/(.*)/target/(.*)\\.jar!/(.*\\.css)") // resource from maven jar in target directory
            , Pattern.compile("jar:file:/(.*)/build/(.*)\\.jar!/(.*\\.css)") // resource from gradle jar in target directory
    };
    private static String[] JAR_SOURCES_REPLACEMENTS = { "src/main/java", "src/main/resources", "src/test/java", "src/test/resources" };

    private static final Function<String, Path> JAR_RESOURCE = new Function<String, Path>() {
        @Override
        public Path apply(String uri) {
            String sourceFileURIPattern = "file:/%s/%s/%s";
            for (Pattern jp : JAR_PATTERNS) {
                Matcher m = jp.matcher(uri);
                if (m.matches()) {
                    for (String string : JAR_SOURCES_REPLACEMENTS) {
                        String potentialSourceURI = String.format(sourceFileURIPattern, m.group(1), string, m.group(3));
                        try {
                            Path p = Paths.get(new URI(potentialSourceURI));
                            if (Files.exists(p)) {
                                return p;
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            logger(URIToPathConverters.class).debug("JAR converter failed to map css[%s] to a source file", uri);
            return null;
        }
    };

    private static class SubstringFileMapper {
        private final String uri;
        private final Map<String, String> entries;

        public SubstringFileMapper(String uri) {
            this.uri = uri;
            this.entries = new TreeMap<>(); // keep insertion order
        }

        public SubstringFileMapper addEntry(String toFind, String toReplace) {
            entries.put(toFind, toReplace);
            return this;
        }

        public Path resolve() {
            if (uri != null && uri.startsWith("file:")) {
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    String potentialSourceURI = uri.replace(entry.getKey(), entry.getValue());
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }

    public static Collection<Function<String, Path>> DEFAULT_CONVERTERS = Arrays.asList(
            MAVEN_RESOURCE, 
            GRADLE_RESOURCE, 
            JAR_RESOURCE
            );
}
