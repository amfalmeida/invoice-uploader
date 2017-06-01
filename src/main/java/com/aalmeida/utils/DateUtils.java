package com.aalmeida.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aalmeida on 05/04/2017.
 */
public final class DateUtils {

    public static String getDate(long dateMillis) {
        final Date date = new Date(dateMillis);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }
}
