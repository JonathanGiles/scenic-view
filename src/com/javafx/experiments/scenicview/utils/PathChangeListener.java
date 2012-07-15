package com.javafx.experiments.scenicview.utils;

import java.net.URI;
import java.util.Map;

/**
 *
 */
public interface PathChangeListener {
    public static final String TOOLS_JAR_KEY = "tools.jar";
    public static final String JFXRT_JAR_KEY = "jfxrt.jar";

    public void onPathChanged(Map<String, URI> map);
}