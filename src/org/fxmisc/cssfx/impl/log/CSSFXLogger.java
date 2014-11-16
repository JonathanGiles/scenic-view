package org.fxmisc.cssfx.impl.log;

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


import java.io.PrintStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

public class CSSFXLogger {
    private static final Clock clock = Clock.systemDefaultZone();

    public static void setLoggerFactory(LoggerFactory factory) {
        loggerFactory = factory;
    }

    public static boolean isInitialized() {
        return loggerFactory != null;
    }
    
    public static void setLogLevel(LogLevel newLevel) {
        logLevel = newLevel;
    }

    public static void console() {
        loggerFactory = CONSOLE_LOGGER_FACTORY;
    }
    
    public static void jul() {
        loggerFactory = JUL_LOGGER_FACTORY;
    }
    
    public static void noop() {
        loggerFactory = NOOP_LOGGER_FACTORY;
    }
    
    /*
     * Always use one of the 2 following methods and never keep static references to a logger. 
     */
    public static Logger logger(String loggerName) {
        return loggerFactory.getLogger(loggerName);
    }
    public static Logger logger(Class<?> loggerClass) {
        return loggerFactory.getLogger(loggerClass);
    }
    
    public static enum LogLevel {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG        
    } 
    
    @FunctionalInterface
    public static interface Logger {
        public void log(LogLevel level, String message, Object ... args);
        public default void info(String message, Object ... args) {log(LogLevel.INFO, message, args);}; 
        public default void debug(String message, Object ... args) {log(LogLevel.DEBUG, message, args);}; 
        public default void warn(String message, Object ... args) {log(LogLevel.WARN, message, args);}; 
        public default void error(String message, Object ... args) {log(LogLevel.ERROR, message, args);}; 
    }

    @FunctionalInterface
    public static interface LoggerFactory {
        public default Logger getLogger(Class<?> loggerClass) {
            return getLogger(loggerClass.getName());
        }
        public Logger getLogger(String loggerName);
    }
    
    private static final Logger CONSOLE_LOGGER = new Logger() {
        private void printLastThrowableArgument(PrintStream output, Object ...args) {
            if (args.length > 0 && Throwable.class.isAssignableFrom(args[args.length-1].getClass())) {
                Throwable t = (Throwable) args[args.length-1];
                t.printStackTrace(output);
            }
        }
        
        @Override
        public void log(LogLevel askedLevel, String message, Object ... args) {
            if (askedLevel.ordinal() <= logLevel.ordinal()) {
                List<Object> params = new ArrayList<Object>(args.length + 2);
                params.add(clock.instant());
                params.add(askedLevel);
                params.addAll(Arrays.asList(args));
                final PrintStream writer = (LogLevel.ERROR.equals(askedLevel))?System.err:System.out;
                writer.println(String.format("%s [%5s] " + message, params.toArray()));
                printLastThrowableArgument(writer, args);
            }
        };
    };
    
    private final static LoggerFactory JUL_LOGGER_FACTORY = new LoggerFactory() {
        @SuppressWarnings("serial")
        Map<LogLevel, Level> levelMapping = new HashMap<CSSFXLogger.LogLevel, Level>(){{
            put(LogLevel.NONE, Level.OFF);
            put(LogLevel.ERROR, Level.SEVERE);
            put(LogLevel.WARN, Level.WARNING);
            put(LogLevel.INFO, Level.INFO);
            put(LogLevel.DEBUG, Level.FINE);
        }};
        @Override
        public Logger getLogger(String loggerName) {
            java.util.logging.Logger delegate = java.util.logging.Logger.getLogger(loggerName);
            
            return new Logger() {
                @Override
                public void log(LogLevel askedLevel, String message, Object ... args) {
                    Level julLevel = levelMapping.get(askedLevel);
                    
                    Supplier<String> log = () -> String.format(message, args);
                    if (args.length > 0 && Throwable.class.isAssignableFrom(args[args.length-1].getClass())) {
                        Throwable t = (Throwable) args[args.length-1];
                        delegate.log(julLevel, t, log);
                    } else {
                        delegate.log(julLevel, log);
                    }
                };
            };
        }
    };  

    private final static LoggerFactory CONSOLE_LOGGER_FACTORY = (s) -> CONSOLE_LOGGER;  
    
    private final static Logger NOOP_LOGGER = (level, message, args) -> {};  
    private final static LoggerFactory NOOP_LOGGER_FACTORY = (s) -> NOOP_LOGGER;  

    private static LoggerFactory loggerFactory = null;
    private static LogLevel logLevel = LogLevel.INFO;
}
