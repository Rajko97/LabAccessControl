package com.vtsappsteam.labaccesscontrol.utils;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;

public class TypefaceSpan extends MetricAffectingSpan {
    private Typeface mTypeface;
    private int fontColor;

    public TypefaceSpan(Typeface newFont, int color) {
        mTypeface = newFont;
        fontColor = color;
    }
    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        textPaint.setTypeface(mTypeface);
        textPaint.setColor(fontColor);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);
        tp.setColor(fontColor);
    }
}
