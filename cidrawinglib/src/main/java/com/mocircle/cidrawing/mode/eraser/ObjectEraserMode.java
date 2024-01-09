package com.mocircle.cidrawing.mode.eraser;

import android.view.MotionEvent;

import com.mocircle.cidrawing.PaintBuilder;
import com.mocircle.cidrawing.board.ElementManager;
import com.mocircle.cidrawing.core.CiPaint;
import com.mocircle.cidrawing.element.AdornmentObjectEraserElement;
import com.mocircle.cidrawing.element.DrawElement;
import com.mocircle.cidrawing.mode.BasePointMode;
import com.mocircle.cidrawing.operation.OperationManager;
import com.mocircle.cidrawing.operation.RemoveElementOperation;

public class ObjectEraserMode extends BasePointMode {

    private ElementManager elementManager;
    private OperationManager operationManager;
    protected CiPaint selectionPaint;
    protected DrawElement selectionElement;

    @Override
    public void setDrawingBoardId(String boardId) {
        super.setDrawingBoardId(boardId);
        elementManager = drawingBoard.getElementManager();
        operationManager = drawingBoard.getOperationManager();
        PaintBuilder paintBuilder = drawingBoard.getPaintBuilder();
        selectionPaint = paintBuilder.createReferenceObjectEraserPaint();
    }

    @Override
    protected void onOverPoint(float x, float y) {
        DrawElement element = elementManager.getFirstHitElement(x, y);
        if (element != null) {
            operationManager.executeOperation(new RemoveElementOperation(element));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectionElement = createSelectionElement();
                selectionElement.setPaint(selectionPaint);
                elementManager.addAdornmentToCurrentLayer(selectionElement);
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectionElement instanceof AdornmentObjectEraserElement) {
                    ((AdornmentObjectEraserElement) selectionElement).setPoint(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                elementManager.removeAdornmentFromCurrentLayer(selectionElement);
                break;
        }
        return super.onTouchEvent(event);
    }

    private DrawElement createSelectionElement() {
        return new AdornmentObjectEraserElement();
    }
}
