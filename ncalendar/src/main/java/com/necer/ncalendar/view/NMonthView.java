package com.necer.ncalendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.necer.ncalendar.listener.OnClickMonthViewListener;
import com.necer.ncalendar.utils.Utils;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by 闫彬彬 on 2017/8/25.
 * QQ:619008099
 */

public class NMonthView extends NCalendarView {

    private List<String> lunarList;
    private List<String> localDateList;

    private int mRowNum;
    private OnClickMonthViewListener mOnClickMonthViewListener;


    public NMonthView(Context context, DateTime dateTime, OnClickMonthViewListener onClickMonthViewListener) {
        super(context);
        this.mInitialDateTime = dateTime;
        Utils.NCalendar nCalendar2 = Utils.getMonthCalendar2(dateTime, 0);

        mOnClickMonthViewListener = onClickMonthViewListener;

        lunarList = nCalendar2.lunarList;
        localDateList = nCalendar2.localDateList;
        dateTimes = nCalendar2.dateTimeList;

        mRowNum = dateTimes.size() / 7;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        //为了6行时，绘制农历不至于太靠下，绘制区域网上压缩一下
        mHeight = (int) (getHeight() - Utils.dp2px(getContext(), 10));
        mRectList.clear();
        for (int i = 0; i < mRowNum; i++) {
            for (int j = 0; j < 7; j++) {
                Rect rect = new Rect(j * mWidth / 7, i * mHeight / mRowNum, j * mWidth / 7 + mWidth / 7, i * mHeight / mRowNum + mHeight / mRowNum);
                mRectList.add(rect);
                DateTime dateTime = dateTimes.get(i * 7 + j);
                Paint.FontMetricsInt fontMetrics = mSorlarPaint.getFontMetricsInt();

                int baseline;//让6行的第一行和5行的第一行在同一直线上，处理选中第一行的滑动
                if (mRowNum == 5) {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                } else {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2 + (mHeight / 10 - mHeight / 12);
                }

                //当月和上下月的颜色不同
                if (Utils.isEqualsMonth(dateTime, mInitialDateTime)) {
                    //当天和选中的日期不绘制农历
                    if (Utils.isToday(dateTime)) {
                        mSorlarPaint.setColor(mSelectCircleColor);
                        int radius = Math.min(Math.min(rect.width() / 2, rect.height() / 2), mSelectCircleRadius);
                        int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 10 - mHeight / 12));
                        canvas.drawCircle(rect.centerX(), centerY, radius, mSorlarPaint);
                        mSorlarPaint.setColor(Color.WHITE);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    } else if (mSelectDateTime != null && dateTime.toLocalDate().equals(mSelectDateTime.toLocalDate())) {
                        mSorlarPaint.setColor(mSelectCircleColor);
                        int radius = Math.min(Math.min(rect.width() / 2, rect.height() / 2), mSelectCircleRadius);
                        int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 10 - mHeight / 12));
                        canvas.drawCircle(rect.centerX(), centerY, radius, mSorlarPaint);
                        mSorlarPaint.setColor(mHollowCircleColor);
                        canvas.drawCircle(rect.centerX(), centerY, radius - mHollowCircleStroke, mSorlarPaint);
                        mSorlarPaint.setColor(mSolarTextColor);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    } else {
                        mSorlarPaint.setColor(mSolarTextColor);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                        drawLunar(canvas, rect,baseline, mLunarTextColor, i, j);
                    }

                } else {
                    mSorlarPaint.setColor(mHintColor);
                    canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    drawLunar(canvas, rect, baseline,mHintColor, i, j);
                }

            }
        }

    }



    public int getMonthHeight() {
        return mHeight;
    }


    private void drawLunar(Canvas canvas, Rect rect, int baseline, int color, int i, int j) {
        if (isShowLunar) {
            mLunarPaint.setColor(color);
            String lunar = lunarList.get(i * 7 + j);
            canvas.drawText(lunar, rect.centerX(), baseline + Utils.dp2px(getContext(), 13), mLunarPaint);
        }
    }



    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < mRectList.size(); i++) {
                Rect rect = mRectList.get(i);
                if (rect.contains((int) e.getX(), (int) e.getY())) {
                    DateTime selectDateTime = dateTimes.get(i);
                    if (Utils.isLastMonth(selectDateTime, mInitialDateTime)) {
                        mOnClickMonthViewListener.onClickLastMonth(selectDateTime);
                    } else if (Utils.isNextMonth(selectDateTime, mInitialDateTime)) {
                        mOnClickMonthViewListener.onClickNextMonth(selectDateTime);
                    } else {
                        mOnClickMonthViewListener.onClickCurrentMonth(selectDateTime);
                    }
                    break;
                }
            }
            return true;
        }
    });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public int getRowNum() {
        return mRowNum;
    }
    public int getSelectRowIndex() {

  /*      int index =0;
        for (int i = 0; i < dateTimes.size(); i++) {
            if (mSelectDateTime.toLocalDate().toString().equals(dateTimes.get(i).toLocalDate().toString())) {
                index = i;
            }
        }*/
        int indexOf = localDateList.indexOf(mSelectDateTime.toLocalDate().toString());
        return indexOf / 7;

    }

}
