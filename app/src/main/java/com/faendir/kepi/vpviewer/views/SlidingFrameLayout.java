package com.faendir.kepi.vpviewer.views;

import android.content.Context;
import android.support.percent.PercentFrameLayout;
import android.util.AttributeSet;

/**
 * Created by Lukas on 18.01.2016.
 * supports animations by fractions of the display area
 */
@SuppressWarnings("unused")
public class SlidingFrameLayout extends PercentFrameLayout {

    public SlidingFrameLayout(Context context) {
        super(context);
    }

    public SlidingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getXFraction() {
        int width = getRootView().getWidth();
        return (width == 0) ? 0 : getX() / (float) width;
    }

    public void setXFraction(float xFraction) {
        int width = getRootView().getWidth();
        setX((width > 0) ? (xFraction * width) : 0);
    }

    public float getYFraction() {
        int height = getRootView().getHeight();
        return (height == 0) ? 0 : getY() / (float) height;
    }

    public void setYFraction(float yFraction) {
        int height = getRootView().getHeight();
        setY((height > 0) ? (yFraction * height) : 0);
    }
}
