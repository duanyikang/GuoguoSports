package com.guoguoquan.www.guoguoquan.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.guoguoquan.www.guoguoquan.R;

/**
 * @author 小段果果
 * @time 2016/5/13  17:21
 * @E-mail duanyikang@mumayi.com
 */

public class StopwatchView extends View {

    // Painters
    private Paint markerPaint;
    private Paint textPaint;
    private Paint topTextPaint;
    private Paint secClockCirclePaint;
    private Paint secClockJointPaint;
    private Paint secClockPointerPaint;
    private Paint trianglePaint;
    private Paint lapCirclePaint;

    //painters' alpha
    private final int MARKER_MAX_ALPHA = 140;
    private final int TEXT_MAX_ALPHA = 255;
    private final int SEC_CLOCK_CIRCLE_MAX_ALPHA = 140;
    private final int SEC_CLOCK_JOINT_MAX_ALPHA = 255;
    private final int SEC_CLOCK_POINTER_MAX_ALPHA = 255;
    private final int TRIA_MAX_ALPHA = 255;
    //the variable used to change whole alpha.
    private double alphaFraction = 1.0;

    // for drawing
    private double innerAngle;
    private double outerAngle;
    private int tenthOfSec;
    private int seconds;
    private int minutes;
    private float radiusMarker;
    private final float markerLen = 50.0f;
    // use 1/4 second as marker unit
    private final double deltaAngle = (Math.PI / 30) / 2 / 2;
    private final double rangeAngle = 2 * Math.PI / 3;

    // for lap
    private final float lapRadius = 8.0f;
    private final float lapMargin = 15.0f;
    private float lapExtraMargin = 0f;
    private final float LAP_EXTRA_MARGIN_MAX = 25.0f;
    private double lapAngle = 0;

    // for animation control
    private boolean gradient = false;
    private double oldOuterAngle;
    private ValueAnimator mainAmin;
    private AnimatorSet lapAnimSet;

    //to fix the position of stopwatch
    private float centerX;
    private float centerY;

    //for height control
    private int MAX_HEIGHT;
    private int MIN_HEIGHT;

    // initialize private resources
    private void initialize() {
        Resources resources = getResources();
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(resources.getColor(R.color.marker_color));
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(3.0f);
        markerPaint.setAlpha(MARKER_MAX_ALPHA);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(resources.getColor(R.color.time_text_color));
        textPaint.setTextSize(resources.getInteger(R.integer.text_size));
        textPaint.setAlpha(TEXT_MAX_ALPHA);

        topTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topTextPaint.setColor(resources.getColor(R.color.time_text_color));
        topTextPaint.setTextSize(resources.getInteger(R.integer.text_size));
        topTextPaint.setAlpha(0);

        secClockCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secClockCirclePaint.setColor(resources.getColor(R.color.sec_clock_circle_color));
        secClockCirclePaint.setStyle(Paint.Style.STROKE);
        secClockCirclePaint.setStrokeWidth(3.0f);
        secClockCirclePaint.setAlpha(SEC_CLOCK_CIRCLE_MAX_ALPHA);

        secClockJointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secClockJointPaint.setColor(resources.getColor(R.color.sec_clock_inner_color));
        secClockJointPaint.setStyle(Paint.Style.STROKE);
        secClockJointPaint.setStrokeWidth(4.0f);
        secClockJointPaint.setAlpha(SEC_CLOCK_JOINT_MAX_ALPHA);

        secClockPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secClockPointerPaint.setColor(resources.getColor(R.color.sec_clock_inner_color));
        secClockPointerPaint.setStyle(Paint.Style.STROKE);
        secClockPointerPaint.setStrokeWidth(2.0f);
        secClockPointerPaint.setAlpha(SEC_CLOCK_POINTER_MAX_ALPHA);

        trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setColor(resources.getColor(R.color.triangle_indicator_color));
        trianglePaint.setAlpha(TRIA_MAX_ALPHA);

        lapCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lapCirclePaint.setColor(resources.getColor(R.color.lap_circle_color));
        lapCirclePaint.setAlpha(0);

        //initialize lapAnimSet
        //  translate animator
        ValueAnimator transAnim = ValueAnimator.ofFloat(0, LAP_EXTRA_MARGIN_MAX);
        transAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lapExtraMargin = (Float) animation.getAnimatedValue();
                StopwatchView.this.invalidate();
            }
        });
        transAnim.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                if (input <= 0.5f) {
                    return input * 2;
                } else {
                    return 1.0f - (input - 0.5f);
                }
            }
        });
        transAnim.setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime));
        //  fade in animator
        ValueAnimator fadeInAnim = ValueAnimator.ofInt(0, 255);
        fadeInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (Integer) animation.getAnimatedValue();
                lapCirclePaint.setAlpha(alpha);
                StopwatchView.this.invalidate();
            }
        });
        fadeInAnim.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime));
        //  fade out animator
        ValueAnimator fadeOutAnim = ValueAnimator.ofInt(255, 0);
        fadeOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (Integer) animation.getAnimatedValue();
                lapCirclePaint.setAlpha(alpha);
                StopwatchView.this.invalidate();
            }
        });
        fadeOutAnim.setInterpolator(new AccelerateInterpolator());
        fadeOutAnim.setDuration(resources.getInteger(android.R.integer.config_longAnimTime));
        //  the animator set
        lapAnimSet = new AnimatorSet();
        lapAnimSet.play(transAnim).with(fadeInAnim);
        lapAnimSet.play(fadeOutAnim).after(transAnim);
    }

    public StopwatchView(Context context) {
        super(context);
        initialize();
    }

    public StopwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public StopwatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getMeasuredValue(widthMeasureSpec);
        int measuredHeight = getMeasuredValue(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    //helper function which calculates width and height.
    private int getMeasuredValue(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;
        if (specMode == MeasureSpec.AT_MOST) {
            // parent wish us be as big as possible
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            // parent wish exactly layout this control.
            result = specSize;
        } else {
            // not specified
            result = 200;
        }

        return result;
    }

    //helper function to set alpha value by the layout size
    private void refreshAlpha() {
        int height = getHeight();
        alphaFraction = (height - MIN_HEIGHT) * 1.0 / (MAX_HEIGHT - MIN_HEIGHT);
        //set all painters' alpha (except marker's painter)
        textPaint.setAlpha((int) (TEXT_MAX_ALPHA * alphaFraction));
        topTextPaint.setAlpha((int) (TEXT_MAX_ALPHA * (1 - alphaFraction)));
        secClockCirclePaint.setAlpha((int) (SEC_CLOCK_CIRCLE_MAX_ALPHA * alphaFraction));
        secClockJointPaint.setAlpha((int) (SEC_CLOCK_JOINT_MAX_ALPHA * alphaFraction));
        secClockPointerPaint.setAlpha((int) (SEC_CLOCK_POINTER_MAX_ALPHA * alphaFraction));
        trianglePaint.setAlpha((int) (TRIA_MAX_ALPHA * alphaFraction));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        centerX = width / 2;
        centerY = MAX_HEIGHT / 2;
        radiusMarker = Math.min(centerX, centerY) * 2 / 3;

        //set alpha values
        refreshAlpha();

        //Draw markers.
        double angle = 0.0;
        // calculate the angle bounds. This is for alpha changing animation
        // the alpha changing range in radius
        double rightAngle = outerAngle % (2 * Math.PI);
        double leftAngle = (rightAngle + 2 * Math.PI - rangeAngle) % (2 * Math.PI);
        for (int i = 0; i < 2 * Math.PI / deltaAngle; i++) {
            float startX = (float) (centerX + radiusMarker * Math.sin(angle));
            float startY = (float) (centerY - radiusMarker * Math.cos(angle));
            float endX = (float) (centerX + (radiusMarker - markerLen) * Math.sin(angle));
            float endY = (float) (centerY - (radiusMarker - markerLen) * Math.cos(angle));
            // set alpha of the marker
            markerPaint.setAlpha((int) (alphaFraction * calMarkerAlpha(angle, leftAngle, rightAngle, rangeAngle)));
            canvas.drawLine(startX, startY, endX, endY, markerPaint);
            angle += deltaAngle;
        }

        // Draw lap circle
        float lapLayoutRadius = radiusMarker - markerLen - lapMargin - lapExtraMargin;
        float lapX = (float) (centerX + lapLayoutRadius * Math.sin(lapAngle));
        float lapY = (float) (centerY - lapLayoutRadius * Math.cos(lapAngle));
        canvas.drawCircle(lapX, lapY, lapRadius, lapCirclePaint);

        // Draw the triangle indicator
        float tgLen = 40.0f;
        float tgRadius = radiusMarker + 10.0f;
        Path tgPath = new Path();
        float pX = (float) (centerX + tgRadius * Math.sin(outerAngle));
        float pY = (float) (centerY - tgRadius * Math.cos(outerAngle));
        tgPath.moveTo(pX, pY);
        pX = (float) (pX + tgLen * Math.sin(outerAngle - Math.PI / 6));
        pY = (float) (pY - tgLen * Math.cos(outerAngle - Math.PI / 6));
        tgPath.lineTo(pX, pY);
        pX = (float) (pX + tgLen * Math.cos(outerAngle));
        pY = (float) (pY + tgLen * Math.sin(outerAngle));
        tgPath.lineTo(pX, pY);
        canvas.drawPath(tgPath, trianglePaint);

        // Draw time text
        String timeText = String.format("%02d:%02d.%01d", minutes, seconds, tenthOfSec);
        float textWidth = textPaint.measureText(timeText);
        canvas.drawText(timeText, centerX - textWidth / 2, centerY, textPaint);
        // Also draw it on the top of stopwatch.

        float textWidth2 = textPaint.measureText("果果圈-跑步");
        Rect bounds = new Rect();
        topTextPaint.getTextBounds(timeText, 0, timeText.length(), bounds);
        canvas.drawText("果果圈-跑步", centerX - textWidth2 / 2, bounds.height() / 2 + MIN_HEIGHT / 2, topTextPaint);

        // Draw small clock for seconds
        float innerCenterX = centerX;
        float innerCenterY = centerY + radiusMarker / 2;
        float radiusSec = radiusMarker / 6;
        // Draw outline circle
        canvas.drawCircle(innerCenterX, innerCenterY, radiusSec, secClockCirclePaint);
        // Draw inner joint
        float radiusJoint = radiusSec / 10;
        canvas.drawCircle(innerCenterX, innerCenterY, radiusJoint, secClockJointPaint);
        // Draw pointer
        float startX = (float) (innerCenterX + radiusJoint * Math.sin(innerAngle));
        float startY = (float) (innerCenterY - radiusJoint * Math.cos(innerAngle));
        float endX = (float) (innerCenterX + radiusSec * Math.sin(innerAngle));
        float endY = (float) (innerCenterY - radiusSec * Math.cos(innerAngle));
        canvas.drawLine(startX, startY, endX, endY, secClockPointerPaint);

    }

    // helper function to calculate marker's alphaValue, the inputs must be in 2*PI.
    private int calMarkerAlpha(double angle, double left, double right, double range) {
        final int RIGHT_ALPHA = 255;
        final int LEFT_ALPHA = 140;
        // set to no gradient
        if (gradient == false) {
            return LEFT_ALPHA;
        }
        // case 1: left is bigger than right
        // add 2*PI to right
        if (left > right) {
            // add 2*PI to angle if angle < right(original)
            if (angle <= right) {
                angle += 2 * Math.PI;
            }
            right += 2 * Math.PI;
        }
        if (angle <= right && angle >= left) {
            // get fraction
            double fraction = (right - angle) / range;
            return (int) Math.round(RIGHT_ALPHA - (RIGHT_ALPHA - LEFT_ALPHA) * fraction);
        } else {
            return LEFT_ALPHA;
        }
    }

    public void setMaxHeight(int height) {
        MAX_HEIGHT = height;
    }

    public void setMinHeight(int height) {
        MIN_HEIGHT = height;
    }

    public void start() {
        TypeEvaluator<Double> evaluator = new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + (endValue - startValue) * fraction;
            }
        };
        mainAmin = ValueAnimator.ofObject(evaluator, 0.0, 2 * Math.PI);
        mainAmin.setDuration(1000);
        mainAmin.setInterpolator(new LinearInterpolator());
        mainAmin.setRepeatCount(ValueAnimator.INFINITE);
        mainAmin.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                innerAngle = (Double) animation.getAnimatedValue();
                outerAngle = oldOuterAngle + innerAngle / 60;
                // to get the right tenthOfSec
                float fraction = animation.getAnimatedFraction();
                tenthOfSec = (int) (fraction * 10);

                // force to re-draw stopwatch
                StopwatchView.this.invalidate();
            }
        });
        mainAmin.addListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {
                                     oldOuterAngle = outerAngle;
                                     gradient = true;
                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     // reset
                                     tenthOfSec = 0;
                                     seconds = 0;
                                     minutes = 0;
                                     innerAngle = 0;
                                     outerAngle = 0;
                                     oldOuterAngle = 0;
                                     gradient = false;
                                     StopwatchView.this.invalidate();
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {
                                     ++seconds;
                                     if (seconds >= 60) {
                                         ++minutes;
                                         seconds = 0;
                                     }
                                     innerAngle = 0;
                                     tenthOfSec = 0;
                                     // to fix some precision problems.
                                     outerAngle = oldOuterAngle + Math.PI * 2 / 60;
                                     oldOuterAngle = outerAngle;
                                     StopwatchView.this.invalidate();
                                 }
                             }
        );
        mainAmin.start();
    }

    public void pause() {
        if (mainAmin != null) {
            mainAmin.pause();
        }
    }

    public void resume() {
        if (mainAmin != null) {
            mainAmin.resume();
        }
    }

    public void reset() {
        if (mainAmin != null) {
            mainAmin.end();
        }
    }

    // return the record for now
    public Period getPeriod() {
        if (lapAnimSet.isRunning()) {
            lapAnimSet.cancel();
        }
        lapAngle = outerAngle;
        lapAnimSet.start();
        return new Period(minutes, seconds, tenthOfSec);
    }
}

