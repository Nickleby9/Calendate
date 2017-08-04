package com.calendate.calendate;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;


public class CustomBootstrapStyleDark implements BootstrapBrand {

    @ColorInt private final int defaultFill;
    @ColorInt private final int defaultTextColor;
    @ColorInt private final int activeFill;
    @ColorInt private final int edge;

    @SuppressWarnings("deprecation") public CustomBootstrapStyleDark(Context context) {
//        defaultFill = Color.TRANSPARENT;
        defaultFill = context.getResources().getColor(R.color.colorPrimaryDark);
//        defaultTextColor = Color.parseColor("#0288d1");
        defaultTextColor = Color.WHITE;
        activeFill = Color.parseColor("#f5f5f5");
        edge = 0;
    }


    @Override
    public int defaultFill(Context context) {
        return defaultFill;
    }

    @Override
    public int defaultEdge(Context context) {
        return edge;
    }

    @Override
    public int defaultTextColor(Context context) {
        return defaultTextColor;
    }

    @Override
    public int activeFill(Context context) {
        return activeFill;
    }

    @Override
    public int activeEdge(Context context) {
        return edge;
    }

    @Override
    public int activeTextColor(Context context) {
        return defaultTextColor;
    }

    @Override
    public int disabledFill(Context context) {
        return 0;
    }

    @Override
    public int disabledEdge(Context context) {
        return edge;
    }

    @Override
    public int disabledTextColor(Context context) {
        return 0;
    }

    @Override
    public int getColor() {
        return 0;
    }
}
