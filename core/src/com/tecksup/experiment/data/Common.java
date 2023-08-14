package com.tecksup.experiment.data;

import com.google.gson.JsonParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class Common {

    public static StringBuilder stringBuilderObject = new StringBuilder();
    public static Formatter FormatterObject = new Formatter(stringBuilderObject);

    public static JsonParser jsonParser = new JsonParser();

    public static String getNow() {
        Date date = new Date();
        String strDateFormat = "M/d/y k:mm";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        return dateFormat.format(date);
    }

}
