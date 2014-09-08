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
