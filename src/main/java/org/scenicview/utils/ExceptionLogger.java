/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
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
package org.scenicview.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.scenicview.view.ScenicViewGui;
import org.scenicview.view.dialog.SubmitExceptionDialog;

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
    
    @Override public void uncaughtException(final Thread t, final Throwable e) {
        ExceptionLogger.submitException(e);
    }

    public static void submitException(final Throwable t) {
        ExceptionLogger.submitException(t, "");
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
        sb.append(ScenicViewGui.VERSION);
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
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}