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
package org.fxconnector.remote.util;

import java.lang.reflect.Method;

/**
 *
 */
public class ScenicViewExceptionLogger {
    
    private static final boolean canSubmitViaScenicView;
    private static Method submitExceptionMethod = null;;
    
    static {
        Class<?> exceptionLoggerClass = null;
        try {
            exceptionLoggerClass = Class.forName("org.scenicview.utils.ExceptionLogger");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if (exceptionLoggerClass != null) {
            try {
                submitExceptionMethod = exceptionLoggerClass.getMethod("submitException", Throwable.class, String.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        canSubmitViaScenicView = submitExceptionMethod != null;
    }
    
    public static void submitException(final Throwable t) {
        submitException(t, "");
    }

    public static void submitException(final Throwable t, final String summary) {
        // temporary: just log to System.err
        if (summary != null && ! summary.isEmpty()) {
            System.err.println(summary);
        }
        t.printStackTrace();
        // end temporary code
        
        if (canSubmitViaScenicView) {
            try {
                submitExceptionMethod.invoke(null, t, summary);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            t.printStackTrace();
        }
    }
}
