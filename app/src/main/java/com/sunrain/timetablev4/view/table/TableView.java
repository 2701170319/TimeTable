package com.sunrain.timetablev4.view.table;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.constants.TableConstants;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;

public class TableView extends View {

    private static final int COLOR_WHITE = 0XFFFFFFFF;
    private static final int LINE_WIDTH = 1;

    private final OverScroller mScroller;
    private final int mOverFlingDistance;
    private final GestureDetector mGestureDetector;

    private Paint mLinePaint;
    private Paint mTextPaint;

    private EdgeEffect mEdgeGlowTop;
    private EdgeEffect mEdgeGlowBottom;

    private boolean mIsBeingDragged;

    private static final int WEEK_BOX_HEIGHT = DensityUtil.dip2Px(25);
    private static final int TIME_BOX_WIDTH = DensityUtil.dip2Px(25);

    private int mClassBoxHeight;
    private int mClassBoxWidth;

    private int mWorkdays = 5;
    private int mMorningClasses = 2;
    private int mAfternoonClasses = 2;
    private int mEveningClasses = 1;
    private int mTotalClasses;
    private String[] mWeekArray;
    private int mTextBaseline;
    private String[] mSectionArray;
    private int mTextHeight;
    private int mMorningHeight;
    private int mAfternoonHeight;
    private int mEveningHeight;
    private int mRangeHeight;
    private int mScrollRange;
    private TableData mTableData;
    private OnBoxClickListener mOnBoxClickListener;
    private OnBoxLongClickListener mOnBoxLongClickListener;

    public TableView(Context context) {
        super(context);

        initPaint();
        initText(context);
        refreshConfig();

        mTableData = TableData.getInstance();

        mGestureDetector = new GestureDetector(context, new OnTableViewGestureListener());

        mEdgeGlowTop = new EdgeEffect(context);
        mEdgeGlowBottom = new EdgeEffect(context);
        mEdgeGlowTop.setColor(0x22FFFFFF);
        mEdgeGlowBottom.setColor(0x22FFFFFF);

        mScroller = new OverScroller(getContext());

        mOverFlingDistance = ViewConfiguration.get(context).getScaledOverflingDistance();
    }

    void refreshConfig() {
        mMorningClasses = SharedPreUtils.getInt(SharedPreConstants.MORNING_CLASS_NUMBER, SharedPreConstants.DEFAULT_MORNING_CLASS_NUMBER);
        mAfternoonClasses = SharedPreUtils.getInt(SharedPreConstants.AFTERNOON_CLASS_NUMBER, SharedPreConstants
                .DEFAULT_AFTERNOON_CLASS_NUMBER);
        mEveningClasses = SharedPreUtils.getInt(SharedPreConstants.EVENING_CLASS_NUMBER, SharedPreConstants.DEFAULT_EVENING_CLASS_NUMBER);
        mWorkdays = SharedPreUtils.getInt(SharedPreConstants.WORK_DAY, SharedPreConstants.DEFAULT_WORK_DAY);
    }

    public void setWorkdays(int workdays) {
        mWorkdays = workdays;
    }

    public void setMorningClasses(int morningClasses) {
        mMorningClasses = morningClasses;
    }

    public void setAfternoonClasses(int afternoonClasses) {
        mAfternoonClasses = afternoonClasses;
    }

    public void setEveningClasses(int eveningClasses) {
        mEveningClasses = eveningClasses;
    }

    private void initText(Context context) {
        Resources resources = context.getResources();
        mWeekArray = resources.getStringArray(R.array.week);
        mSectionArray = resources.getStringArray(R.array.section);

        Paint.FontMetricsInt fontMetricsInt = mTextPaint.getFontMetricsInt();
        // eg. FontMetricsInt: top=-38 ascent=-33 descent=9 bottom=10 leading=0
        mTextBaseline = (fontMetricsInt.bottom + fontMetricsInt.top) / 2;
        mTextHeight = Math.abs(fontMetricsInt.bottom + fontMetricsInt.top);
    }

    private void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.setColor(COLOR_WHITE);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(LINE_WIDTH);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(COLOR_WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, getContext().getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTable(canvas);
        drawEdgeEffect(canvas);
    }

    private void drawEdgeEffect(Canvas canvas) {
        if (!mEdgeGlowTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(0, Math.min(0, getScrollY()));
            mEdgeGlowTop.setSize(getWidth(), getHeight());
            if (mEdgeGlowTop.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeGlowBottom.isFinished()) {
            final int restoreCount = canvas.save();
            final int width = getWidth();
            final int height = getHeight();
            canvas.translate(-width, Math.max(mScrollRange, getScrollY()) + height);
            canvas.rotate(180, width, 0);
            mEdgeGlowBottom.setSize(width, height);
            if (mEdgeGlowBottom.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount);
        }
    }

    private void drawTable(Canvas canvas) {
        drawEdge(canvas);
        drawSplit(canvas);
        drawTableText(canvas);
        drawClasses(canvas);
    }

    private void drawClasses(Canvas canvas) {
        SparseArray<ClassBean> classes = mTableData.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            int key = classes.keyAt(i);
            ClassBean classBean = classes.get(key);

            int x = TIME_BOX_WIDTH + mClassBoxWidth * classBean.week + mClassBoxWidth / 2;
            int y = WEEK_BOX_HEIGHT + mClassBoxHeight * classBean.time + mClassBoxHeight / 2 - mTextBaseline;
            if (classBean.section == 1) {
                y += mMorningHeight;
            } else if (classBean.section == 2) {
                y += mAfternoonHeight;
            }
            canvas.drawText(classBean.course, x, y - mTextHeight, mTextPaint);
            canvas.drawText(classBean.classroom, x, y + mTextHeight, mTextPaint);
        }
    }

    private void drawTableText(Canvas canvas) {
        // 星期
        for (int i = 0, width = TIME_BOX_WIDTH + mClassBoxWidth / 2; i < mWorkdays; i++, width += mClassBoxWidth) {
            canvas.drawText(mWeekArray[i], width, WEEK_BOX_HEIGHT / 2 - mTextBaseline, mTextPaint);
        }

        // 上午
        canvas.drawText(mSectionArray[0], 0, 1, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight / 2 - mTextBaseline - 2 *
                mTextHeight, mTextPaint);
        canvas.drawText(mSectionArray[0], 1, 2, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight / 2 - mTextBaseline + 2 *
                mTextHeight, mTextPaint);

        // 下午
        canvas.drawText(mSectionArray[1], 0, 1, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight + mAfternoonHeight / 2 -
                mTextBaseline - 2 * mTextHeight, mTextPaint);// 下
        canvas.drawText(mSectionArray[1], 1, 2, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight + mAfternoonHeight / 2 -
                mTextBaseline + 2 * mTextHeight, mTextPaint);// 午

        // 晚上
        if (mEveningClasses > 0) {
            final int splitHeight = mEveningClasses == 1 ? mTextHeight : 2 * mTextHeight;
            canvas.drawText(mSectionArray[2], 0, 1, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight + mAfternoonHeight +
                    mEveningHeight / 2 - mTextBaseline - splitHeight, mTextPaint);
            canvas.drawText(mSectionArray[2], 1, 2, TIME_BOX_WIDTH / 2, WEEK_BOX_HEIGHT + mMorningHeight + mAfternoonHeight +
                    mEveningHeight / 2 - mTextBaseline + splitHeight, mTextPaint);
        }
    }

    private void drawSplit(Canvas canvas) {
        // 星期分割线
        canvas.drawLine(0, WEEK_BOX_HEIGHT, canvas.getWidth(), WEEK_BOX_HEIGHT, mLinePaint);

        // 时间分割线
        canvas.drawLine(TIME_BOX_WIDTH, 0, TIME_BOX_WIDTH, mRangeHeight, mLinePaint);

        // 上午和下午之间的分割线
        final int morningSplitHeight = WEEK_BOX_HEIGHT + mMorningHeight;
        canvas.drawLine(0, morningSplitHeight, TIME_BOX_WIDTH, morningSplitHeight, mLinePaint);

        // 下午和晚上之间的分割线
        if (mEveningClasses > 0) {
            int afternoonSplitHeight = mAfternoonClasses * mClassBoxHeight + morningSplitHeight;
            canvas.drawLine(0, afternoonSplitHeight, TIME_BOX_WIDTH, afternoonSplitHeight, mLinePaint);
        }

        // 课程分割横线
        for (int i = 0, height = WEEK_BOX_HEIGHT + mClassBoxHeight; i < mTotalClasses - 1; i++, height += mClassBoxHeight) {
            canvas.drawLine(TIME_BOX_WIDTH, height, canvas.getWidth(), height, mLinePaint);
        }

        // 课程分割竖线
        for (int i = 0, width = TIME_BOX_WIDTH + mClassBoxWidth; i < mWorkdays - 1; i++, width += mClassBoxWidth) {
            canvas.drawLine(width, 0, width, mRangeHeight, mLinePaint);
        }
    }

    private void drawEdge(Canvas canvas) {
        // 顶边
        canvas.drawLine(0, 0, canvas.getWidth(), 0, mLinePaint);
        // 左侧边
        canvas.drawLine(0, 0, 0, mRangeHeight, mLinePaint);
        // 右侧边
        canvas.drawLine(canvas.getWidth() - LINE_WIDTH, 0, canvas.getWidth() - LINE_WIDTH, mRangeHeight, mLinePaint);
        // 底边
        canvas.drawLine(0, mRangeHeight - LINE_WIDTH, canvas.getWidth(), mRangeHeight - LINE_WIDTH, mLinePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mClassBoxHeight = (getHeight() - WEEK_BOX_HEIGHT) / 4;
        mClassBoxWidth = (getMeasuredWidth() - TIME_BOX_WIDTH) / mWorkdays;

        mTotalClasses = mMorningClasses + mAfternoonClasses + mEveningClasses;

        mRangeHeight = mTotalClasses * mClassBoxHeight + WEEK_BOX_HEIGHT;
        mScrollRange = mRangeHeight - getHeight();

        mMorningHeight = mClassBoxHeight * mMorningClasses;
        mAfternoonHeight = mClassBoxHeight * mAfternoonClasses;
        mEveningHeight = mClassBoxHeight * mEveningClasses;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    endDrag();
                }
                break;
        }
        return true;
    }

    private class OnTableViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnBoxClickListener != null) {
                int[] index = new int[3];
                getClickClass(e, index);
                mOnBoxClickListener.onBoxClick(index[0], index[1], index[2]);
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mIsBeingDragged = true;
            final int pulledToY = getScrollY() + (int) distanceY;

            if (pulledToY < 0) {
                scrollTo(0, 0);
                mEdgeGlowTop.onPull(distanceY / getHeight(), e2.getX() / getWidth());
                if (!mEdgeGlowBottom.isFinished()) {
                    mEdgeGlowBottom.onRelease();
                }
            } else if (pulledToY > mScrollRange) {
                scrollTo(0, mScrollRange);
                mEdgeGlowBottom.onPull(distanceY / getHeight(), 1f - e2.getX() / getWidth());
                if (!mEdgeGlowTop.isFinished()) {
                    mEdgeGlowTop.onRelease();
                }
            } else {
                scrollBy(0, (int) distanceY);
            }
            if (!mEdgeGlowTop.isFinished() || !mEdgeGlowBottom.isFinished()) {
                postInvalidateOnAnimation();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mOnBoxLongClickListener != null) {
                int[] index = new int[3];
                getClickClass(e, index);
                mOnBoxLongClickListener.onBoxLongClick(index[0], index[1], index[2]);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (getScrollY() == 0 || getScrollY() == mScrollRange) {
                // 已到达顶部或底部 不执行 fling 操作
                return false;
            }

            mScroller.fling(0, getScrollY(), 0, -(int) velocityY, 0, 0, 0, Math.max(0, mScrollRange), 0, mOverFlingDistance);
            postInvalidateOnAnimation();
            return true;
        }

        private void getClickClass(MotionEvent e, int[] index) {
            index[0] = ((int) e.getX() - TIME_BOX_WIDTH) / mClassBoxWidth;
            int boxIndex = ((int) e.getY() - WEEK_BOX_HEIGHT) / mClassBoxHeight;

            if (boxIndex < mMorningClasses) {
                index[1] = TableConstants.MORNING;
                index[2] = boxIndex;
                return;
            }
            boxIndex -= mMorningClasses;

            if (boxIndex < mAfternoonClasses) {
                index[1] = TableConstants.AFTERNOON;
                index[2] = boxIndex;
                return;
            }

            boxIndex -= mAfternoonClasses;
            index[1] = TableConstants.EVENING;
            index[2] = boxIndex;
        }

    }

    public void setOnBoxClickListener(OnBoxClickListener onBoxClickListener) {
        mOnBoxClickListener = onBoxClickListener;
    }

    public void setOnBoxLongClickListener(OnBoxLongClickListener onBoxLongClickListener) {
        mOnBoxLongClickListener = onBoxLongClickListener;
    }

    public interface OnBoxClickListener {

        void onBoxClick(int week, int section, int time);
    }

    public interface OnBoxLongClickListener {

        void onBoxLongClick(int week, int section, int time);

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldY = getScrollY();
            int y = mScroller.getCurrY();

            if (oldY != y) {
                scrollTo(0, y);
                if (y < 0 && oldY >= 0) {
                    mEdgeGlowTop.onAbsorb((int) mScroller.getCurrVelocity());
                } else if (y > mScrollRange && oldY <= mScrollRange) {
                    mEdgeGlowBottom.onAbsorb((int) mScroller.getCurrVelocity());
                }
                postInvalidateOnAnimation();
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        mEdgeGlowTop.onRelease();
        mEdgeGlowBottom.onRelease();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTableData.bindTableView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mTableData.unBindTableView();
        super.onDetachedFromWindow();
    }
}