package com.dmedia.dlimited.widget;

/**
 * Created by xema0 on 2016-11-14.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GrayScaleImageView extends android.support.v7.widget.AppCompatImageView {
    public GrayScaleImageView(Context context) {
        super(context);
    }

    public GrayScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GrayScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            //drawable.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);//채도 0이면 grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            //drawable.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY); //더 어둡게
            drawable.setColorFilter(cf);
        }
        super.onDraw(canvas);
    }

}