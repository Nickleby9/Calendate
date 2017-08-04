package com.calendate.calendate;

import android.content.Context;
import com.beardedhen.androidbootstrap.BootstrapButton;

public class MyUtils {

    public static String btnDateFormat = "dd MMMM, yyyy";
    public static String dateForamt = "yyyyMMdd";
    public static String fixEmail(String s) {
        return s.replace(".", ",");
    }

    public static void fixBootstrapButton(Context context, BootstrapButton button) {
        button.setBootstrapBrand(new CustomBootstrapStyle(context));
    }
}
