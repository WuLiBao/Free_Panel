package com.example.administrator.free_panel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/8.
 */

public class PanelView extends View {
    private int MyPanleWidth;
    private float MyLineHeight;
    private int maxLine = 10;
    private Paint myPaint;
    private Bitmap myWhitePice;
    private Bitmap myBlackPice;
    private float ratioPieceOfLineHeight = 3 * 1.0f / 4;
    private boolean isOverGame;
    public static int WHITE_WIN = 0;
    public static int BLACK_WIN = 1;
    private boolean isWhite = true;
    private List<Point> MyWhiteArray = new ArrayList<Point>();
    private List<Point> MyBlackArray = new ArrayList<Point>();
    private onGameListener onGameListener;
    private int mUnder;

    public PanelView(Context context) {
        this(context, null);
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public interface onGameListener {
        void onGameOver(int i);

    }

    public void setOnGameListener(PanelView.onGameListener onGameListener) {
        this.onGameListener = onGameListener;
    }

    private void init() {
        myPaint = new Paint();
        myPaint.setColor(0x44ff0000);
        myPaint.setAntiAlias(true);//是否锯齿
        myPaint.setDither(true);//防抖动
        myPaint.setStyle(Paint.Style.STROKE);
        myWhitePice = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        myBlackPice = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isOverGame) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getVaLidPiont(x, y);
            if (MyWhiteArray.contains(p) || MyBlackArray.contains(p)) {
                return false;

            }
            if (isWhite) {
                MyWhiteArray.add(p);
            } else {
                MyBlackArray.add(p);
            }
            invalidate();
            isWhite = !isWhite;
        }
        return true;
    }

    private Point getVaLidPiont(int x, int y) {
        return new Point((int) (x / MyLineHeight), (int) (y / MyLineHeight));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        MyPanleWidth = w;
        MyLineHeight = MyPanleWidth * 1.0f / maxLine;
        mUnder = h - (h - MyPanleWidth) / 2;
        int pieceWidth = (int) (MyLineHeight * ratioPieceOfLineHeight);
        myWhitePice = Bitmap.createScaledBitmap(myWhitePice, pieceWidth, pieceWidth, false);
        myBlackPice = Bitmap.createScaledBitmap(myBlackPice, pieceWidth, pieceWidth, false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBroad(canvas);
        drawPiece(canvas);
        checkGameOver();
    }


    private void drawBroad(Canvas canvas) { //棋盘线
        int w = MyPanleWidth;
        float lineHeight = MyLineHeight;
        int startX = (int) (lineHeight / 2);
        int endX = (int) (w - lineHeight / 2);
        for (int i = 0; i < maxLine; i++) {
            int y = (int) ((i + 1.5) * lineHeight);
            canvas.drawLine(startX, y, endX, y, myPaint);
            canvas.drawLine(y, startX, y, endX, myPaint);

        }
    }


    private void drawPiece(Canvas canvas) { //棋子
        int n1 = MyWhiteArray.size();
        int n2 = MyBlackArray.size();
        for (int i = 0; i < n1; i++) {
            Point whitePoint = MyWhiteArray.get(i);
            canvas.drawBitmap(myWhitePice, (whitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * MyLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * MyLineHeight, null);
        }
        for (int i = 0; i < n2; i++) {
            Point blackPoint = MyBlackArray.get(i);
            canvas.drawBitmap(myBlackPice, (blackPoint.x + (1 - ratioPieceOfLineHeight) / 2) * MyLineHeight,
                    (blackPoint.y + (1 - ratioPieceOfLineHeight) / 2) * MyLineHeight, null);
        }

    }

    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(MyWhiteArray);
        boolean blackWin = checkFiveInLine(MyBlackArray);
        if (whiteWin || blackWin) {
            isOverGame = true;
            if (onGameListener != null) {
                onGameListener.onGameOver(whiteWin ? WHITE_WIN : BLACK_WIN);

            }

        }
    }

    public int getUnder() {
        return mUnder;
    }

    private boolean checkFiveInLine(List<Point> myArray) {
        for (Point p : myArray) {
            int x = p.x;
            int y = p.y;
            boolean win_flag = checkHorizontal(x, y, myArray) || checkVertical(x, y, myArray)
                    || checkLeftDiagonal(x, y, myArray) || checkRightDiagonal(x, y, myArray);
            if (win_flag) {
                return true;
            }
        }
        return false;
    }

    private boolean checkHorizontal(int x, int y, List<Point> myArray) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x + i, y))) {
                count++;

            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x - i, y))) {
                count++;

            } else {
                break;
            }
            if (count == 5) {
                return true;
            }
        }
        return false;
    }

    private boolean checkVertical(int x, int y, List<Point> myArray) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x, y + i))) {
                count++;

            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x, y - i))) {
                count++;

            } else {
                break;
            }
            if (count == 5) {
                return true;
            }
        }

        return false;

    }

    private boolean checkLeftDiagonal(int x, int y, List<Point> myArray) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x - i, y + i))) {
                count++;

            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x + i, y - i))) {
                count++;

            } else {
                break;
            }
            if (count == 5) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRightDiagonal(int x, int y, List<Point> myArray) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x - i, y - i))) {
                count++;

            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < 5; i++) {
            if (myArray.contains(new Point(x + i, y + i))) {
                count++;

            } else {
                break;
            }
            if (count == 5) {
                return true;
            }
        }
        return false;
    }

    protected void restartGame() {
        MyWhiteArray.clear();
        MyBlackArray.clear();
        isOverGame = false;
        isWhite = false;
        invalidate();
    }

}
