package com.fhx.microphone;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by fhx on 2/3/16.
 */
public class RecordButton extends ImageView {

    private int mCX;
    private int mCY;

    private int mButtonRadius;
    private int mButtonBorderRadius;
    private int mMaxRadius;

    private Paint mButtonPaint;
    private Paint mButtonBorderPaint;
    private Paint mLevelIndicatorPaint;

    private ObjectAnimator mButtonPressAnimator;
    private float mButtonPressAnimationProgress;

    private ObjectAnimator mLevelIndicatorAnimator;
    private float mLevelIndicatorAnimationProgress;

    static final int BUTTON_PRESS_ANIMATION_DURATION = 100;
    static final int LEVEL_INDICATOR_ANIMATION_DURATION = 50;

    private boolean mRecording;

    public RecordButton(Context context) {
        super(context);
        this.prepare(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.prepare(context, attrs);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.prepare(context, attrs);
    }

    private void prepare(Context context, AttributeSet attrs){
        this.setFocusable(true);
        this.setScaleType(ScaleType.CENTER_INSIDE);
        setClickable(true);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // avoid click event being triggered for whole area
                // instead of just inside button radius
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    if (((mCX - x) * (mCY - x) + (mCY - y) * (mCY - y)) > mButtonBorderRadius * mButtonBorderRadius)
                        return true;
                }
                return false;
            }
        });

        int buttonColor = Color.parseColor("#E74C3C");
        int buttonBorderColor = buttonColor;
        int levelIndicatorColor = buttonColor;
        int buttonRadius = 24;
        int buttonBorderRadius = 30;
        int buttonBorderWidth = 6;
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecordButton);
            buttonColor = a.getColor(R.styleable.RecordButton_buttonColor, buttonColor);
            buttonBorderColor = a.getColor(R.styleable.RecordButton_buttonColor, buttonBorderColor);
            levelIndicatorColor = a.getColor(R.styleable.RecordButton_levelIndicatorColor, levelIndicatorColor);
            buttonRadius = (int) a.getDimension(R.styleable.RecordButton_buttonRadius, buttonRadius);
            buttonBorderRadius = (int) a.getDimension(R.styleable.RecordButton_buttonBorderRadius, buttonBorderRadius);
            buttonBorderWidth = (int) a.getDimension(R.styleable.RecordButton_buttonBorderWidth, buttonBorderWidth);
            a.recycle();
        }

        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(buttonColor);

        mButtonBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonBorderPaint.setStyle(Paint.Style.STROKE);
        mButtonBorderPaint.setStrokeWidth(buttonBorderWidth);
        mButtonBorderPaint.setColor(buttonBorderColor);

        mLevelIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLevelIndicatorPaint.setStyle(Paint.Style.STROKE);
        mLevelIndicatorPaint.setStrokeWidth(1.0f);
        mLevelIndicatorPaint.setColor(levelIndicatorColor);

        mButtonPressAnimator = ObjectAnimator.ofFloat(this, "ButtonPressAnimationProgress", 0f, 0f);
        mButtonPressAnimator.setDuration(BUTTON_PRESS_ANIMATION_DURATION);

        mLevelIndicatorAnimator = ObjectAnimator.ofFloat(this, "LevelIndicatorAnimationProgress", 0f, 0f);
        mLevelIndicatorAnimator.setDuration(LEVEL_INDICATOR_ANIMATION_DURATION);

        mButtonRadius = buttonRadius;
        mButtonBorderRadius = buttonBorderRadius;
        mButtonPressAnimationProgress = mButtonBorderRadius;
        mLevelIndicatorAnimationProgress = mButtonBorderRadius;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCX = w / 2;
        mCY = h / 2;
        mMaxRadius = Math.min(w, h) / 2;
    }

    public void setButtonPressAnimationProgress(float progress){
        mButtonPressAnimationProgress = progress;
        this.invalidate();
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if(pressed){
            mButtonPressAnimator.setFloatValues(mButtonPressAnimationProgress, mButtonBorderRadius + 10);
        }else{
            mButtonPressAnimator.setFloatValues(mButtonPressAnimationProgress, mButtonBorderRadius);
        }
        mButtonPressAnimator.start();
    }

    public void setLevelIndicatorAnimationProgress(float progress){
        mLevelIndicatorAnimationProgress = progress;
        this.invalidate();
    }

    public void setIndicatorLevel(float level){
        float target = mButtonBorderRadius + (mMaxRadius-mButtonBorderRadius)*(float)Math.sqrt(level);
        mLevelIndicatorAnimator.setFloatValues(mLevelIndicatorAnimationProgress, target);
        mLevelIndicatorAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Draw button border
        canvas.drawCircle(mCX, mCY, mButtonPressAnimationProgress, mButtonBorderPaint);

        // Draw button according to state
        if(mRecording){
            // Draw the level indicator
            canvas.drawCircle(mCX, mCY, mLevelIndicatorAnimationProgress, mLevelIndicatorPaint);

            // Rectangular shape for stop
            canvas.drawRect(mCX-mButtonRadius,  mCY-mButtonRadius,
                    mCX+mButtonRadius, mCY+mButtonRadius, mButtonPaint);
        }else{
            // Circular shape for record
            canvas.drawCircle(mCX, mCY, mButtonRadius, mButtonPaint);
        }
        super.onDraw(canvas);
    }

    public void setIsRecording(boolean isRecording) {
        this.mRecording = isRecording;
    }
}
