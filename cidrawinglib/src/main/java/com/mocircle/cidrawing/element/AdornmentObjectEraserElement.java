package com.mocircle.cidrawing.element;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.mocircle.cidrawing.core.CiPaint;

public class AdornmentObjectEraserElement extends DrawElement {
    private float downX = -1;
    private float downY = -1;

    public AdornmentObjectEraserElement() {
    }

    @Override
    public void updateBoundingBox() {

    }

    @Override
    public Object clone() {
        AdornmentObjectEraserElement element = new AdornmentObjectEraserElement();
        cloneTo(element);
        return element;
    }

    public void setPoint(float x, float y) {
        downX = x;
        downY = y;
    }

    @Override
    public void drawElement(Canvas canvas) {
        if (downX != -1 && downY != -1) {
            CiPaint paint2 = new CiPaint(paint);
            paint2.setAlpha(50);
            paint2.setStyle(Paint.Style.FILL);
            canvas.drawCircle(downX, downY, 25, paint);
            canvas.drawCircle(downX, downY, 25, paint2);
        }
    }
}
