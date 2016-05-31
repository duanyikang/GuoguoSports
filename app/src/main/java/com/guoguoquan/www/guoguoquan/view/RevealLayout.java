package com.guoguoquan.www.guoguoquan.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * @author 小段果果
 * @time 2016/5/13  17:21
 * @E-mail duanyikang@mumayi.com
 */

public class RevealLayout extends FrameLayout {

    private Path mClipPath;
    private float mClipRadius = 0;
    private int mClipCenterX, mClipCenterY = 0;
    private Animator mAnimator;
    private static final int DEFAULT_DURATION = 600;
    private boolean mIsContentShown = true;

    public RevealLayout(Context context) {
        this(context, null);
    }

    public RevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public RevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mClipCenterX = w / 2;
        mClipCenterY = h / 2;
        if (!mIsContentShown) {
            mClipRadius = 0;
        } else {
            mClipRadius = (float) (Math.sqrt(w * w + h * h) / 2);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    public float getClipRadius() {
        return mClipRadius;
    }

    public void setClipRadius(float clipRadius) {
        mClipRadius = clipRadius;
        invalidate();
    }

    public boolean isContentShown() {
        return mIsContentShown;
    }

    public void setContentShown(boolean isContentShown) {
        mIsContentShown = isContentShown;
        if (mIsContentShown) {
            mClipRadius = 0;
        } else {
            mClipRadius = getMaxRadius(mClipCenterX, mClipCenterY);
        }
        invalidate();
    }

    public void show() {
        show(DEFAULT_DURATION);
    }

    public void hide() {
        hide(DEFAULT_DURATION);
    }

    public void next() {
        next(DEFAULT_DURATION);
    }

    public void show(int duration) {
        show(getWidth() / 2, getHeight() / 2, duration);
    }

    public void hide(int duration) {
        hide(getWidth() / 2, getHeight() / 2, duration);
    }

    public void next(int duration) {
        next(getWidth() / 2, getHeight() / 2, duration);
    }

    public void show(int x, int y) {
        show(x, y, DEFAULT_DURATION);
    }

    public void hide(int x, int y) {
        hide(x, y, DEFAULT_DURATION);
    }

    public void next(int x, int y) {
        next(x, y, DEFAULT_DURATION);
    }

    public void show(int x, int y, int duration) {
        if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
            throw new RuntimeException("Center point out of range or call method when View is not initialed yet.");
        }

        mClipCenterX = x;
        mClipCenterY = y;
        float maxRadius = getMaxRadius(x, y);

        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(this, "clipRadius", 0f, maxRadius);
        mAnimator.setInterpolator(new BakedBezierInterpolator());
        mAnimator.setDuration(duration);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsContentShown = true;
            }
        });
        mAnimator.start();
    }

    public void hide(int x, int y, int duration) {
        if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
            throw new RuntimeException("Center point out of range or call method when View is not initialed yet.");
        }

        if (x != mClipCenterX || y != mClipCenterY) {
            mClipCenterX = x;
            mClipCenterY = y;
            mClipRadius = getMaxRadius(x, y);
        }

        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(this, "clipRadius", 0f);
        mAnimator.setInterpolator(new BakedBezierInterpolator());
        mAnimator.setDuration(duration);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsContentShown = false;
            }
        });
        mAnimator.start();
    }

    public void next(int x, int y, int duration) {
        final int childCount = getChildCount();
        if (childCount > 1) {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (i == 0) {
                    bringChildToFront(child);
                }
            }
            show(x, y, duration);
        }
    }

    private float getMaxRadius(int x, int y) {
        int h = Math.max(x, getWidth() - x);
        int v = Math.max(y, getHeight() - y);
        return (float) Math.sqrt(h * h + v * v);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (indexOfChild(child) == getChildCount() - 1) {
            boolean result;
            mClipPath.reset();
            mClipPath.addCircle(mClipCenterX, mClipCenterY, mClipRadius, Path.Direction.CW);
//            Log.d("RevealLayout", "ClipRadius: " + mClipRadius);
            canvas.save();
            canvas.clipPath(mClipPath);
            result = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return result;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

}


class BakedBezierInterpolator implements Interpolator {

    /**
     * Lookup table values.
     * Generated using a Bezier curve from (0,0) to (1,1) with control points:
     * P0 (0,0)
     * P1 (0.4, 0)
     * P2 (0.2, 1.0)
     * P3 (1.0, 1.0)
     * <p/>
     * Values sampled with x at regular intervals between 0 and 1.
     */
    private static final float[] VALUES = new float[]{
            0.0f, 0.0002f, 0.0009f, 0.0019f, 0.0036f, 0.0059f, 0.0086f, 0.0119f, 0.0157f, 0.0209f,
            0.0257f, 0.0321f, 0.0392f, 0.0469f, 0.0566f, 0.0656f, 0.0768f, 0.0887f, 0.1033f, 0.1186f,
            0.1349f, 0.1519f, 0.1696f, 0.1928f, 0.2121f, 0.237f, 0.2627f, 0.2892f, 0.3109f, 0.3386f,
            0.3667f, 0.3952f, 0.4241f, 0.4474f, 0.4766f, 0.5f, 0.5234f, 0.5468f, 0.5701f, 0.5933f,
            0.6134f, 0.6333f, 0.6531f, 0.6698f, 0.6891f, 0.7054f, 0.7214f, 0.7346f, 0.7502f, 0.763f,
            0.7756f, 0.7879f, 0.8f, 0.8107f, 0.8212f, 0.8326f, 0.8415f, 0.8503f, 0.8588f, 0.8672f,
            0.8754f, 0.8833f, 0.8911f, 0.8977f, 0.9041f, 0.9113f, 0.9165f, 0.9232f, 0.9281f, 0.9328f,
            0.9382f, 0.9434f, 0.9476f, 0.9518f, 0.9557f, 0.9596f, 0.9632f, 0.9662f, 0.9695f, 0.9722f,
            0.9753f, 0.9777f, 0.9805f, 0.9826f, 0.9847f, 0.9866f, 0.9884f, 0.9901f, 0.9917f, 0.9931f,
            0.9944f, 0.9955f, 0.9964f, 0.9973f, 0.9981f, 0.9986f, 0.9992f, 0.9995f, 0.9998f, 1.0f, 1.0f
    };

    private static final float STEP_SIZE = 1.0f / (VALUES.length - 1);

    @Override
    public float getInterpolation(float input) {
        if (input >= 1.0f) {
            return 1.0f;
        }

        if (input <= 0f) {
            return 0f;
        }

        int position = Math.min(
                (int) (input * (VALUES.length - 1)),
                VALUES.length - 2);

        float quantized = position * STEP_SIZE;
        float difference = input - quantized;
        float weight = difference / STEP_SIZE;

        return VALUES[position] + weight * (VALUES[position + 1] - VALUES[position]);
    }
}

