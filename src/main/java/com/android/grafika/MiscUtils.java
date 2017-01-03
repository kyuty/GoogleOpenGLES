/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
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
 */

package com.android.grafika;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some handy utilities.
 */
public class MiscUtils {
    private static final String TAG = MainActivity.TAG + " MiscUtils";

    private MiscUtils() {}

    /**
     * Obtains a list of files that live in the specified directory and match the glob pattern.
     * dir是一个目录（文件夹）
     * 该方法返回该文件夹里所有后缀是glob的fileName
     */
    public static String[] getFiles(File dir, String glob) {
        Log.d(TAG, "getFiles() dir = " + dir + " glob = " + glob);
        String regex = globToRegex(glob);
        final Pattern pattern = Pattern.compile(regex);
        Log.d(TAG, "getFiles() pattern = " + pattern.toString());
        String[] result = dir.list(new FilenameFilter() {
            @Override 
            public boolean accept(File dir, String name) {
                Log.d(TAG, "getFiles() accept. dir = " + dir + " name = " + name);
                Matcher matcher = pattern.matcher(name);
                return matcher.matches();
            }
        });
        Arrays.sort(result);
        
        for (String str : result) {
            Log.d(TAG, "getFiles() str = " + str);
        }

        return result;
    }

    /**
     * Converts a filename globbing pattern to a regular expression.
     * 后缀转成正则表达式
     * <p>
     * The regex is suitable for use by Matcher.matches(), which matches the entire string, so
     * we don't specify leading '^' or trailing '$'.
     */
    private static String globToRegex(String glob) {
        // Quick, overly-simplistic implementation -- just want to handle something simple
        // like "*.mp4".
        //
        // See e.g. http://stackoverflow.com/questions/1247772/ for a more thorough treatment.
        StringBuilder regex = new StringBuilder(glob.length());
        //regex.append('^');
        for (char ch : glob.toCharArray()) {
            switch (ch) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append('.');
                    break;
                case '.':
                    regex.append("\\.");
                    break;
                default:
                    regex.append(ch);
                    break;
            }
        }
        //regex.append('$');
        Log.d(TAG, "globToRegex(). glob = " + glob + " regex = " + regex.toString());
        return regex.toString();
    }

    /**
     * Obtains the approximate refresh time, in nanoseconds, of the default display associated
     * with the activity.
     * <p>
     * The actual refresh rate can vary slightly (e.g. 58-62fps on a 60fps device).
     */
    public static long getDisplayRefreshNsec(Activity activity) {
        Display display = ((WindowManager)
                activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        double displayFps = display.getRefreshRate();
        long refreshNs = Math.round(1000000000L / displayFps);
        Log.d(TAG, "refresh rate is " + displayFps + " fps --> " + refreshNs + " ns");
        return refreshNs;
    }
}
