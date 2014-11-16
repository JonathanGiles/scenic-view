package org.fxmisc.cssfx.impl.monitoring;

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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathsWatcher {
    private WatchService watchService;
    private Map<String, Map<String, List<Runnable>>> filesActions = new HashMap<>();
    private Thread watcherThread;

    public PathsWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            logger(PathsWatcher.class).error("cannot create WatchService", e);
        }
    }

    public void monitor(Path directory, Path sourceFile, Runnable action) {
        if (watchService != null) {
            logger(PathsWatcher.class).info("registering action %d for monitoring %s in %s", System.identityHashCode(action), sourceFile, directory);
            Map<String, List<Runnable>> fileAction = filesActions.computeIfAbsent(
                    directory.toString(), (p) -> {
                        try {
                            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new HashMap<>();
                    });

            List<Runnable> actions = fileAction.computeIfAbsent(sourceFile.toString(), k -> new LinkedList<>());
            actions.add(action);
            logger(PathsWatcher.class).debug("%d CSS modification actions registered for file %s", actions.size(), sourceFile);
        } else {
            logger(PathsWatcher.class).warn("no WatchService active, CSS monitoring cannot occur");
        }
    }

    public void watch() {
        watcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger(PathsWatcher.class).info("starting to monitor physical files");
                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException ex) {
                        return;
                    }
                    Path directory = ((Path) key.watchable()).toAbsolutePath().normalize();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        logger(PathsWatcher.class).debug("'%s' change detected in directory %s", kind, directory);

                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            // it is a modification
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path modifiedFile = directory.resolve(ev.context()).toAbsolutePath().normalize();

                            if (filesActions.containsKey(directory.toString())) {
                                logger(PathsWatcher.class).debug("file: %s was modified", modifiedFile.getFileName());
                                Map<String, List<Runnable>> filesAction = filesActions.get(directory.toString());
                                if (filesAction.containsKey(modifiedFile.toString())) {
                                    logger(PathsWatcher.class).debug("file is monitored");
                                    List<Runnable> actions = filesAction.get(modifiedFile.toString());
                                    logger(PathsWatcher.class).debug("%d CSS modification will be performed ", actions.size());
                                    
                                    for (Runnable action : actions) {
                                        action.run();
                                    }
                                } else {
                                    logger(PathsWatcher.class).debug("file is not monitored");
                                }
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        }, "CSSFX-file-monitor");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    public void stop() {
        watcherThread.interrupt();
    }
}
