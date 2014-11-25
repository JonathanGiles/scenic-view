package org.fxmisc.cssfx.api;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static org.fxmisc.cssfx.impl.log.CSSFXLogger.logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIToPathConverters {
    private static final URIToPathConverter MAVEN_RESOURCE = new URIToPathConverter() {
        @Override
        public Path convert(String uri) {
            if (uri != null && uri.startsWith("file:")) {
                if (uri.contains("target/classes")) {
                    String[] classesTransform = {
                            "src/main/java", "src/main/resources" };
                    for (String ct : classesTransform) {
                        String potentialSourceURI = uri.replace("target/classes", ct);
                        try {
                            Path p = Paths.get(new URI(potentialSourceURI));
                            if (Files.exists(p)) {
                                return p;
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (uri.contains("target/test-classes")) {
                    String[] testClassesTransform = {
                            "src/test/java", "src/test/resources" };
                    for (String tct : testClassesTransform) {
                        String potentialSourceURI = uri.replace("target/test-classes", tct);
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
            logger(URIToPathConverters.class).debug("MAVEN converter failed to map css[%s] to a source file", uri);

            return null;
        }
    };

    private static final URIToPathConverter GRADLE_RESOURCE = new URIToPathConverter() {
        @Override
        public Path convert(String uri) {
            if (uri != null && uri.startsWith("file:")) {
                if (uri.contains("build/classes/main")) {
                    String[] classesTransform = {
                            "src/main/java", "src/main/resources" };
                    for (String ct : classesTransform) {
                        String potentialSourceURI = uri.replace("target/classes", ct);
                        try {
                            Path p = Paths.get(new URI(potentialSourceURI));
                            if (Files.exists(p)) {
                                return p;
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (uri.contains("build/classes/test")) {
                    String[] testClassesTransform = {
                            "src/test/java", "src/test/resources" };
                    for (String tct : testClassesTransform) {
                        String potentialSourceURI = uri.replace("target/test-classes", tct);
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

            logger(URIToPathConverters.class).debug("GRADLE converter failed to map css[%s] to a source file", uri);
            return null;
        }
    };

    private static Pattern[] JAR_PATTERNS = {
            Pattern.compile("jar:file:/(.*)/target/(.*)\\.jar!/(.*\\.css)") // resource from maven jar in target directory
            , Pattern.compile("jar:file:/(.*)/build/(.*)\\.jar!/(.*\\.css)") // resource from gradle jar in target directory
    };
    private static String[] JAR_SOURCES_REPLACEMENTS = {
            "src/main/java", "src/main/resources", "src/test/java", "src/test/resources" };

    private static final URIToPathConverter JAR_RESOURCE = new URIToPathConverter() {
        @Override
        public Path convert(String uri) {
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

    public static URIToPathConverter[] DEFAULT_CONVERTERS = {
            MAVEN_RESOURCE
            , GRADLE_RESOURCE
            , JAR_RESOURCE
    };
}
