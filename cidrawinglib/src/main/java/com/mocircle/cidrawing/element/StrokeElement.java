package com.mocircle.cidrawing.element;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.mocircle.cidrawing.persistence.ConvertUtils;
import com.mocircle.cidrawing.persistence.PersistenceException;
import com.mocircle.cidrawing.utils.DrawUtils;
import com.mocircle.cidrawing.utils.ShapeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StrokeElement extends BasePathElement {

    private static final String KEY_POINTS = "points";
    private static final String KEY_CLOSE_STROKE = "closeStroke";

    private List<PointF> points = new ArrayList<>();
    private boolean closeStroke;

    public StrokeElement() {
    }

    public boolean isCloseStroke() {
        return closeStroke;
    }

    public void setCloseStroke(boolean closeStroke) {
        this.closeStroke = closeStroke;
    }

    @Override
    public Object clone() {
        StrokeElement element = new StrokeElement();
        cloneTo(element);
        return element;
    }

    public void addPoint(float x, float y) {
        points.add(new PointF(x, y));

        // Sync to path
        if (elementPath == null) {
            elementPath = new Path();
        }
        if (points.size() == 1) {
            elementPath.moveTo(x, y);
        } else {
            elementPath.lineTo(x, y);
        }
    }

    public void doneEditing() {
        elementPath = createStrokePath();
        updateBoundingBox();
    }

    @Override
    public JSONObject generateJson() {
        JSONObject object = super.generateJson();
        try {
            object.put(KEY_POINTS, ConvertUtils.pointsToJson(points));
            object.put(KEY_CLOSE_STROKE, closeStroke);
        } catch (JSONException e) {
            throw new PersistenceException(e);
        }
        return object;
    }

    @Override
    public void loadFromJson(JSONObject object, Map<String, byte[]> resources) {
        super.loadFromJson(object, resources);
        if (object != null) {
            try {
                points = ConvertUtils.pointsFromJson(object.optJSONArray(KEY_POINTS));
                closeStroke = object.optBoolean(KEY_CLOSE_STROKE, false);
            } catch (JSONException e) {
                throw new PersistenceException(e);
            }
        }
    }

    @Override
    public void afterLoaded() {
        elementPath = createStrokePath();
        updateBoundingBox();
    }

    @Override
    public void applyMatrixForData(Matrix matrix) {
        super.applyMatrixForData(matrix);

        applyMatrixForPoints(matrix);
        elementPath = createStrokePath();
    }

    @Override
    protected void cloneTo(BaseElement element) {
        super.cloneTo(element);
        if (element instanceof StrokeElement) {
            StrokeElement obj = (StrokeElement) element;
            if (points != null) {
                obj.points = new ArrayList<>();
                for (PointF p : points) {
                    obj.points.add(new PointF(p.x, p.y));
                }
            }
            obj.closeStroke = closeStroke;
        }
    }

    private Path createStrokePath() {
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            if (i == 0) {
                path.moveTo(p.x, p.y);
            } else {
                path.lineTo(p.x, p.y);
            }
        }
        if (closeStroke) {
            path.close();
        }
        return path;
    }

    private void applyMatrixForPoints(Matrix matrix) {
        float[] newPoints = new float[points.size() * 2];
        int i = 0;
        for (PointF p : points) {
            newPoints[i] = p.x;
            newPoints[i + 1] = p.y;
            i += 2;
        }
        matrix.mapPoints(newPoints);
        for (int j = 0; j < newPoints.length; j += 2) {
            points.get(j / 2).set(newPoints[j], newPoints[j + 1]);
        }
    }

    @Override
    public boolean hitTestForSelection(float x, float y) {
        if (!isSelectionEnabled()) {
            return false;
        }

        if (getBoundingBox() != null) {
            float[] movePoint = new float[2];
            getInvertedDisplayMatrix().mapPoints(movePoint, new float[]{x, y});

            Path path = getTouchableArea();
            RectF box = new RectF();
            if (path.isRect(box)) {
                // Quick check if path is rectangle
                return box.contains(movePoint[0], movePoint[1]);
            } else {
                Rect touchSquare = DrawUtils.createTouchSquare(drawingView.getContext(), (int) movePoint[0], (int) movePoint[1]);

                for (PointF p : points) {
                    if (touchSquare.contains((int) p.x, (int) p.y)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean hitTestForSelection(Path path) {
        if (!isSelectionEnabled()) {
            return false;
        }

        RectF box = new RectF();
        if (path.isRect(box)) {
            // Quick check if path is rectangle
            return box.contains(getOuterBoundingBox());
        } else {
            Path drawPathClone = new Path(path);
            drawPathClone.transform(getInvertedDisplayMatrix());
            Region r1 = ShapeUtils.createRegionFromPath(drawPathClone);
            Region r2 = ShapeUtils.createRegionFromPath(getTouchableArea());
            if (r1.quickReject(r2)) {
                // Quick check for not intersect case
                return false;
            }
            return r2.op(r1, Region.Op.INTERSECT);
        }
    }
}
