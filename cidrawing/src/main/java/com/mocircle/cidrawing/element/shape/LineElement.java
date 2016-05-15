package com.mocircle.cidrawing.element.shape;

import android.graphics.Path;

public class LineElement extends BoxShapeElement {

    public LineElement() {
    }

    @Override
    public Object clone() {
        LineElement element = new LineElement();
        cloneTo(element);
        return element;
    }

    @Override
    protected Path createShapePath() {
        Path path = new Path();
        path.moveTo(shapeVector.getPoint1().x, shapeVector.getPoint1().y);
        path.lineTo(shapeVector.getPoint2().x, shapeVector.getPoint2().y);
        return path;
    }

}
