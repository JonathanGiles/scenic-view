/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.scenicview.utils;

import org.scenicview.ScenicView;
import org.scenicview.dialog.SubmitExceptionDialog;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

/**
 *
 */
public class ExceptionLogger implements Thread.UncaughtExceptionHandler {
    private static final boolean EXCEPTION_LOGGING_ENABLED = false;
    private static final String SUBMIT_EXCEPTIONS_KEY = "submitExceptions";
    
    private static boolean isInited = false;
    private static ExceptionLogger exceptionLogger;
    
    private static synchronized void init() {
        if (isInited) return;
        isInited = true;
        
        // set up our event queue proxy to make it easier to see all uncaught exceptions
        exceptionLogger = new ExceptionLogger();
        Thread.setDefaultUncaughtExceptionHandler(exceptionLogger);
    }

    private ExceptionLogger() {
        // no-op
    }
    
    public void uncaughtException(final Thread t, final Throwable e) {
        exceptionLogger.submitException(e);
    }

    public static void submitException(final Throwable t) {
        exceptionLogger.submitException(t, "");
    }

    public static void submitException(final Throwable t, final String summary) {
        t.printStackTrace();
        
        if (! EXCEPTION_LOGGING_ENABLED) {
            return;
        }
        
        if (! isInited) {
            init();
        }
        
        String submission = buildSubmissionString(t, summary);
        
        Properties p = PropertiesUtils.getProperties();
        boolean isSubmissionAllowed = false;
        if (p.containsKey(SUBMIT_EXCEPTIONS_KEY)) {
            isSubmissionAllowed = Boolean.parseBoolean(p.getProperty(SUBMIT_EXCEPTIONS_KEY));
        } else {
            // the key has not been set to true or false, so prompt the user
            // to see if they will allow it
            // TODO show dialog to user to ask if they will allow for submission
            SubmitExceptionDialog dialog = new SubmitExceptionDialog(submission);
            isSubmissionAllowed = dialog.isSubmissionAllowed();
            
            if (dialog.isRememberDecision()) {
                p.put(SUBMIT_EXCEPTIONS_KEY, Boolean.FALSE.toString());
            }
            
            submission = dialog.getSubmissionText();
        }
        
        if (isSubmissionAllowed) {
            // submit exception for analysis
            doSubmit(submission);
        }
    }
    
    private static String buildSubmissionString(Throwable t, String summary) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Submitter: \n");
        sb.append("Email: \n");
        
        sb.append("Scenic View version ");
        sb.append(ScenicView.VERSION);
        sb.append("\n\n");
        
        sb.append("Summary: ");
        sb.append(summary);
        sb.append("\n\n");
        
        final Writer writer = new StringWriter();  
        final PrintWriter printWriter = new PrintWriter(writer);  
        t.printStackTrace(printWriter);  
        sb.append("Exception:\n");
        sb.append(writer.toString());
        
        return sb.toString();
    }
    
    private static void doSubmit(final String submission) {
        try {
            // Construct data
            String data = URLEncoder.encode("submission", "UTF-8") + "=" + URLEncoder.encode(submission, "UTF-8");

            // Send data
            URL url = new URL("http://www.jonathangiles.net/scenicView/exceptionLogger.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
//            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
//            while ((line = rd.readLine()) != null) {
//                System.out.println(line);
//            }
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}