package com.robosoft.archana.rangeseekbarassign;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.robosoft.archana.rangeseekbarassign.Util.BitmapUtil;
import com.robosoft.archana.rangeseekbarassign.Util.PixelUtil;

import java.math.BigDecimal;

/**
 * Created by archana on 23/6/16.
 */

public class RangeSeekBar<T extends Number> extends ImageView {
    /**
     * Default color of a {@link RangeSeekBar}, #FF33B5E5. This is also known as "Ice Cream Sandwich" blue.
     */
    public static final int ACTIVE_COLOR = Color.parseColor("#040504");
    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    // Localized constants from MotionEvent for compatibility
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    public static Integer DEFAULT_MINIMUM = 0;
    public static Integer DEFAULT_MAXIMUM = 100;
    public static final int HEIGHT_IN_DP = 30;
    public static final int TEXT_LATERAL_PADDING_IN_DP = 3;

    private static final int INITIAL_PADDING_IN_DP = 8;
    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 8;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 8;

    private static final int LINE_HEIGHT_IN_DP = 15;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint();

    private Bitmap thumbImage;
    private Bitmap thumbPressedImage;
    private Bitmap thumbDisabledImage;

    private Bitmap mRightThumbNormalBitmap;
    private Bitmap mRightThumbPressedBitmap;
    private Bitmap mRightThumbDisableBitmap;



    private float mThumbHalfWidth;
    private float mThumbHalfHeight;

    private float padding;
    private T absoluteMinValue, absoluteMaxValue;
    private NumberType numberType;
    private double absoluteMinValuePrim, absoluteMaxValuePrim;
    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 1d;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener<T> listener;

    private float mDownMotionX;

    private int mActivePointerId = INVALID_POINTER_ID;

    private int mScaledTouchSlop;

    private boolean mIsDragging;

    private int mTextOffset;
    private int mTextSize;
    private int mDistanceToTop;
    private RectF mRect;
    private RectF mUnCoveredRect;

    private boolean mSingleThumb;
    private boolean mAlwaysActive;
    private boolean mShowLabels;
    private boolean mShowTextAboveThumbs;
    private float mInternalPad;
    private int mActiveColor;
    private int mDefaultColor;
    private int mTextAboveThumbsColor;

    private boolean mThumbShadow;
    private int mThumbShadowXOffset;
    private int mThumbShadowYOffset;
    private int mThumbShadowBlur;
    private Path mThumbShadowPath;
    private Path mTranslatedThumbShadowPath = new Path();
    private Matrix mThumbShadowMatrix = new Matrix();


    public RangeSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }



    private T extractNumericValueFromAttributes(TypedArray a, int index, int defaultValue) {
        TypedValue tv = a.peekValue(index);
        if (tv == null) {
            return (T) Integer.valueOf(defaultValue);
        }

        int type = tv.type;
        if (type == TypedValue.TYPE_FLOAT) {
            return (T) Float.valueOf(a.getFloat(index, defaultValue));
        } else {
            return (T) Integer.valueOf(a.getInteger(index, defaultValue));
        }
    }

    private void init(Context context, AttributeSet attrs) {
        Log.i("Hello","I am in init Method");
      //  Log.i("Hello","Attrs is"+attrs);
        float barHeight;

        int rightThumbNormalImg = R.drawable.rightnormal;
        int rightThumbDisableImg = R.drawable.rightdisable;
        int rightThumbPressedImg = R.drawable.rightpressed;

        int thumbNormalImg =  R.drawable.leftnormal;
        int thumbPressedImg = R.drawable.leftpressed;
        int thumbDisabledImg = R.drawable.leftdisable;

        int thumbShadowColor;
        int defaultShadowColor = Color.argb(75, 0, 0, 0);
        int defaultShadowYOffset = PixelUtil.dpToPx(context, 2);
        int defaultShadowXOffset = PixelUtil.dpToPx(context, 0);
        int defaultShadowBlur = PixelUtil.dpToPx(context, 2);
        if (attrs == null) {
            setRangeToDefaultValues();
            mInternalPad = PixelUtil.dpToPx(context, INITIAL_PADDING_IN_DP);
            barHeight = PixelUtil.dpToPx(context, LINE_HEIGHT_IN_DP);
            mActiveColor = ACTIVE_COLOR;
            mDefaultColor = Color.parseColor("#E8E8E8");
            mAlwaysActive = false;
            mShowTextAboveThumbs = true;
            mTextAboveThumbsColor = Color.parseColor("#BA599C");
            thumbShadowColor = defaultShadowColor;
            mThumbShadowXOffset = defaultShadowXOffset;
            mThumbShadowYOffset = defaultShadowYOffset;
            mThumbShadowBlur = defaultShadowBlur;
        } else {
          //  Log.i("Hello","********************Else Part**************");
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0);
            try {
                setRangeValues(
                        extractNumericValueFromAttributes(a, R.styleable.RangeSeekBar_absoluteMinValue, DEFAULT_MINIMUM),
                        extractNumericValueFromAttributes(a, R.styleable.RangeSeekBar_absoluteMaxValue, DEFAULT_MAXIMUM)
                );
                mShowTextAboveThumbs = a.getBoolean(R.styleable.RangeSeekBar_valuesAboveThumbs, true);
                mTextAboveThumbsColor = a.getColor(R.styleable.RangeSeekBar_textAboveThumbsColor, Color.parseColor("#BA599C"));
                mSingleThumb = a.getBoolean(R.styleable.RangeSeekBar_singleThumb, false);
                mShowLabels = a.getBoolean(R.styleable.RangeSeekBar_showLabels, true);
                mInternalPad = a.getDimensionPixelSize(R.styleable.RangeSeekBar_internalPadding, INITIAL_PADDING_IN_DP);
                barHeight = a.getDimensionPixelSize(R.styleable.RangeSeekBar_barHeight, LINE_HEIGHT_IN_DP);
                mActiveColor = a.getColor(R.styleable.RangeSeekBar_activeColor, ACTIVE_COLOR);
                Log.i("Hello","Active color is***************************"+mActiveColor);
                mDefaultColor = a.getColor(R.styleable.RangeSeekBar_defaultColor, Color.parseColor("#E8E8E8"));
                mAlwaysActive = a.getBoolean(R.styleable.RangeSeekBar_alwaysActive, false);

                Drawable normalDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbNormal);
                Drawable rightThumbnormalDrawable = a.getDrawable(R.styleable.RangeSeekBar_rightThumbNormal);
             //   Log.i("Hello","Normal Drawabel Object is"+normalDrawable);
                if(rightThumbnormalDrawable!= null){
                    mRightThumbNormalBitmap = BitmapUtil.drawableToBitmap(rightThumbnormalDrawable);
                }
                if (normalDrawable != null) {
                 //   Log.i("Hello","I am if of Normal Draaaaa********");
                    thumbImage = BitmapUtil.drawableToBitmap(normalDrawable);
                }

                Drawable disabledDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbDisabled);
                Drawable rightThumbdisablDrawable = a.getDrawable(R.styleable.RangeSeekBar_rightThumbDisable);
                if(rightThumbdisablDrawable!=null){
                    mRightThumbDisableBitmap = BitmapUtil.drawableToBitmap(rightThumbdisablDrawable);
                }
                //  Log.i("Hello","Disable Drawable is"+disabledDrawable);
                if (disabledDrawable != null) {
                    thumbDisabledImage = BitmapUtil.drawableToBitmap(disabledDrawable);
                }

                Drawable rightThumbPressedDrawable = a.getDrawable(R.styleable.RangeSeekBar_rightThumbPressed);
                Drawable pressedDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbPressed);
                if(rightThumbPressedDrawable!=null){
                    mRightThumbPressedBitmap = BitmapUtil.drawableToBitmap(rightThumbPressedDrawable);
                }

              //  Log.i("Hello","Pressed Drawable is"+pressedDrawable);
                if (pressedDrawable != null) {
                    thumbPressedImage = BitmapUtil.drawableToBitmap(pressedDrawable);
                }
                mThumbShadow = a.getBoolean(R.styleable.RangeSeekBar_thumbShadow, false);
              //  Log.i("Hello","ThumbShadow is"+mThumbShadow);
                thumbShadowColor = a.getColor(R.styleable.RangeSeekBar_thumbShadowColor, defaultShadowColor);
                mThumbShadowXOffset = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowXOffset, defaultShadowXOffset);
                mThumbShadowYOffset = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowYOffset, defaultShadowYOffset);
                mThumbShadowBlur = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowBlur, defaultShadowBlur);
            } finally {
                a.recycle();
            }
        }

        if ( mRightThumbNormalBitmap == null) {
            mRightThumbNormalBitmap  = BitmapFactory.decodeResource(getResources(), rightThumbNormalImg);
            //    Log.i("Hello","Thumb Image is"+thumbImage);
        }
        if (mRightThumbPressedBitmap == null) {
            mRightThumbPressedBitmap = BitmapFactory.decodeResource(getResources(),rightThumbPressedImg);
            //  Log.i("Hello","ThumbPressedImage  is"+thumbPressedImage);
        }
        if (mRightThumbDisableBitmap == null) {
            mRightThumbDisableBitmap = BitmapFactory.decodeResource(getResources(), rightThumbDisableImg);
            // Log.i("Hello","thumbDisabledImage  is"+thumbDisabledImage);
        }

        if (thumbImage == null) {
            thumbImage = BitmapFactory.decodeResource(getResources(), thumbNormalImg);
        //    Log.i("Hello","Thumb Image is"+thumbImage);
        }
        if (thumbPressedImage == null) {
            thumbPressedImage = BitmapFactory.decodeResource(getResources(), thumbPressedImg);
          //  Log.i("Hello","ThumbPressedImage  is"+thumbPressedImage);
        }
        if (thumbDisabledImage == null) {
            thumbDisabledImage = BitmapFactory.decodeResource(getResources(), thumbDisabledImg);
           // Log.i("Hello","thumbDisabledImage  is"+thumbDisabledImage);
        }

        // To do for left
        mThumbHalfWidth = 0.5f * thumbImage.getWidth();
       // Log.i("Hello","mThumbHalWidth"+mThumbHalfWidth);
        mThumbHalfHeight = 0.5f * thumbImage.getHeight();
      //  Log.i("Hello"," mThumbHalfHeight"+mThumbHalfWidth);

        setValuePrimAndNumberType();

        mTextSize = PixelUtil.dpToPx(context, DEFAULT_TEXT_SIZE_IN_DP);
        mDistanceToTop = PixelUtil.dpToPx(context, DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP);
        mTextOffset = !mShowTextAboveThumbs ? 0 : this.mTextSize + PixelUtil.dpToPx(context,
                DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP) + this.mDistanceToTop;
      //  Log.i("Hello","mTextOffset is"+mTextOffset);
        mRect = new RectF(padding,
                mTextOffset + mThumbHalfHeight - barHeight / 2,
                getWidth() - padding,
                mTextOffset + mThumbHalfHeight + barHeight / 2);
        Log.i("Hello","Rectanlge is"+mRect);
        mUnCoveredRect =  new RectF();
        Log.i("Hello","New Rectangle is"+mUnCoveredRect);

        // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScrollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        // unuse .............
        if (mThumbShadow) {
         //   Log.i("Hello","I am in If Block of mThumbShadow");
            // We need to remove hardware acceleration in order to blur the shadow
           setLayerType(LAYER_TYPE_SOFTWARE, null);
            shadowPaint.setColor(thumbShadowColor);
           shadowPaint.setMaskFilter(new BlurMaskFilter(mThumbShadowBlur, BlurMaskFilter.Blur.NORMAL));
            mThumbShadowPath = new Path();
           mThumbShadowPath.addCircle(0,
                    0,
                    mThumbHalfHeight,
                    Path.Direction.CW);
        }
    }

    public void setRangeValues(T minValue, T maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        setValuePrimAndNumberType();
    }

    public void setTextAboveThumbsColor(int textAboveThumbsColor) {
        this.mTextAboveThumbsColor = textAboveThumbsColor;
        invalidate();
       // Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
    }

    public void setTextAboveThumbsColorResource(@ColorRes int resId) {
        setTextAboveThumbsColor(getResources().getColor(resId));
    }

    @SuppressWarnings("unchecked")
    // only used to set default values when initialised from XML without any values specified
    private void setRangeToDefaultValues() {
        this.absoluteMinValue = (T) DEFAULT_MINIMUM;
        this.absoluteMaxValue = (T) DEFAULT_MAXIMUM;
        setValuePrimAndNumberType();
    }

    private void setValuePrimAndNumberType() {
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
    }

    @SuppressWarnings("unused")
    public void resetSelectedValues() {
        setSelectedMinValue(absoluteMinValue);
        setSelectedMaxValue(absoluteMaxValue);
    }

    @SuppressWarnings("unused")
    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
     */
    @SuppressWarnings("unused")
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    /**
     * Returns the absolute minimum value of the range that has been set at construction time.
     *
     * @return The absolute minimum value of the range.
     */
    public T getAbsoluteMinValue() {
        return absoluteMinValue;
    }

    /**
     * Returns the absolute maximum value of the range that has been set at construction time.
     *
     * @return The absolute maximum value of the range.
     */
    public T getAbsoluteMaxValue() {
        return absoluteMaxValue;
    }

    /**
     * Returns the currently selected min value.
     *
     * @return The currently selected min value.
     */
    public T getSelectedMinValue() {
        return normalizedToValue(normalizedMinValue);
    }

    /**
     * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMinValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    /**
     * Returns the currently selected max value.
     *
     * @return The currently selected max value.
     */
    public T getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValue);
    }

    /**
     * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMaxValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    /**
     * Registers given listener callback to notify about changed selected values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    @SuppressWarnings("unused")
    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Set the path that defines the shadow of the thumb. This path should be defined assuming
     * that the center of the shadow is at the top left corner (0,0) of the canvas. The
     * {@link #drawThumbShadow(float, Canvas)} method will place the shadow appropriately.
     *
     * @param thumbShadowPath The path defining the thumb shadow
     */
    @SuppressWarnings("unused")
    public void setThumbShadowPath(Path thumbShadowPath) {
        this.mThumbShadowPath = thumbShadowPath;
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
     //   Log.i("Hello","onTouchEvent is called");
        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
              //  Log.i("Hello","I am in MotionEvent.ACTION_DOWN");
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
              //  Log.i("Hello","mDownMotionX is"+mDownMotionX);
                pressedThumb = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
           //     Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
             //   Log.i("Hello","I am in MotionEvent.ACTION_MOVE");
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                          // Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
              //  Log.i("Hello","I am in MotionEvent.ACTION_UP");
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
             //   Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                if (listener != null) {
                    listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
               // Log.i("Hello","I am in .ACTION_POINTER_DOWN");
            {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
             //   Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
           //     Log.i("Hello","I am in .MotionEvent.ACTION_POINTER_UP");
                onSecondaryPointerUp(event);
                invalidate();
            //    Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                break;
            case MotionEvent.ACTION_CANCEL:
          //      Log.i("Hello","I am in . MotionEvent.ACTION_CANCEL");
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
              //  Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);

        if (Thumb.MIN.equals(pressedThumb) && !mSingleThumb) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
    //    Log.i("Hello","i AM IN onStartTrackingTouch()");
     //  paint.setColor(mActiveColor);
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        //Log.i("Hello","mThumbShadowwwwwwww is"+mThumbShadow);
        int height = thumbImage.getHeight()
                + (!mShowTextAboveThumbs ? 0 : PixelUtil.dpToPx(getContext(), HEIGHT_IN_DP))
                + (mThumbShadow ? mThumbShadowYOffset + mThumbShadowBlur : 0);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Draws the widget on the given canvas.
     */
    int count = 0;
    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
       //Log.i("Hello","No of times onDraw() is called"+count);
        count++;
        paint.setTextSize(mTextSize);


        // Initial color
        // for border
     //   paint.setStyle(Paint.Style.FILL_AND_STROKE);
      //  paint.setStyle(Paint.Style.FILL);
        paint.setColor(mActiveColor);


        paint.setAntiAlias(true);
        float minMaxLabelSize = 0;

        /*if (mShowLabels) {
            // draw min and max labels
            String minLabel = getContext().getString(R.string.demo_min_label);
            String maxLabel = getContext().getString(R.string.demo_max_label);
            minMaxLabelSize = Math.max(paint.measureText(minLabel), paint.measureText(maxLabel));
            float minMaxHeight = mTextOffset + mThumbHalfHeight + mTextSize / 3;
            canvas.drawText(minLabel, 0, minMaxHeight, paint);
            canvas.drawText(maxLabel, getWidth() - minMaxLabelSize, minMaxHeight, paint);
        }*/
        padding = mInternalPad + minMaxLabelSize + mThumbHalfWidth;

        // draw seek bar background line
        mRect.left = padding;
      //  Log.i("Hello","mRect.Left"+mRect.left);
        mRect.right = getWidth() - padding;

        //mRect.right = 200; just for checking width of rectangle
        //Log.i("Hello","mRect.Right"+mRect.right);
       // Log.i("Hello","mRect.Bottm"+mRect.bottom);
       // Log.i("Hello","mRect.Top"+mRect.top);
       canvas.drawRect(mRect, paint);
        Log.i("Hello","Rect is in DEFAULT area"+mRect);
        boolean selectedValuesAreDefault = (getSelectedMinValue().equals(getAbsoluteMinValue()) &&
                getSelectedMaxValue().equals(getAbsoluteMaxValue()));
        Log.i("Hello","selectedValuesAreDefault************"+selectedValuesAreDefault);

        int colorToUseForButtonsAndHighlightedLine = !mAlwaysActive && selectedValuesAreDefault ?
                mDefaultColor: // default values
                mDefaultColor;   // non default, filter is active
         Log.i("Hello","Default color is********************"+mDefaultColor);
         Log.i("Hello","Active color is*********************"+mActiveColor);
         Log.i("Hello","colorToUseForButtonsAndHighlightedLine******************************"+colorToUseForButtonsAndHighlightedLine);
        // draw seek bar active range line
        mRect.left = normalizedToScreen(normalizedMinValue);
       // Log.i("Hello","Rect_left is"+mRect.left);
        mRect.right = normalizedToScreen(normalizedMaxValue);
        mRect.top = 72;
        mRect.bottom= 200;

        paint.setColor(colorToUseForButtonsAndHighlightedLine);
       // paint.setStyle(Paint.Style.STROKE);

       // paint.setStrokeWidth(3);
       // draw for selected area// draw black color for uncovered area
        canvas.drawRect(mRect, paint);
        mUnCoveredRect.left = mRect.left;
        mUnCoveredRect.bottom = mRect.bottom;
        mUnCoveredRect.right = mRect.right;
        mUnCoveredRect.top = mRect.top;
        Paint paintNew = new Paint();
        paintNew.setColor(getResources().getColor(R.color.colorAccent));
        paintNew.setStyle(Paint.Style.STROKE);
        paintNew.setStrokeWidth(5);

        canvas.drawRect(mUnCoveredRect,paintNew);
        Log.i("Hello","Rect is in active area"+mRect);

        // draw minimum thumb (& shadow if requested) if not a single thumb control
        if (!mSingleThumb) {
            if (mThumbShadow) {
                drawThumbShadow(normalizedToScreen(normalizedMinValue), canvas);
            }
            // for(min) left thumb
       //    Log.i("Hello","Left Thumb value is bEFORE "+Thumb.MIN.equals(pressedThumb));
            drawThumbRight(normalizedToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas,
                    selectedValuesAreDefault);
          // Log.i("Hello","Left Thumb value is aFTER"+Thumb.MIN.equals(pressedThumb));
        }

        // draw maximum thumb & shadow (if necessary)
        if (mThumbShadow) {
            drawThumbShadow(normalizedToScreen(normalizedMaxValue), canvas);
        }
        // for right thumb
       drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas,
                selectedValuesAreDefault);

        // draw the text if sliders have moved from default edges
        if (mShowTextAboveThumbs && !selectedValuesAreDefault) {
            paint.setTextSize(mTextSize);
            paint.setColor(mTextAboveThumbsColor);
            // give text a bit more space here so it doesn't get cut off
            int offset = PixelUtil.dpToPx(getContext(), TEXT_LATERAL_PADDING_IN_DP);

            String minText = String.valueOf(getSelectedMinValue());
            String maxText = String.valueOf(getSelectedMaxValue());
            float minTextWidth = paint.measureText(minText) + offset;
            float maxTextWidth = paint.measureText(maxText) + offset;

            if (!mSingleThumb) {
                canvas.drawText(minText,
                        normalizedToScreen(normalizedMinValue) - minTextWidth * 0.5f,
                        mDistanceToTop + mTextSize,
                        paint);

            }

            canvas.drawText(maxText,
                    normalizedToScreen(normalizedMaxValue) - maxTextWidth * 0.5f,
                    mDistanceToTop + mTextSize,
                    paint);
        }

    }

    /**
     * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method. Other members of this class than the normalized min and max values don't need to be saved.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    /**
     * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param pressed     Is the thumb currently in "pressed" state?
     * @param canvas      The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
      //  Log.i("Hello","ScreenCoord is"+screenCoord+"An");
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw = thumbDisabledImage;

        } else {
            buttonToDraw = pressed ? thumbPressedImage : thumbImage;
        }
     //   Log.i("Hello","mTextOffset in leftThumb is"+mTextOffset+"And Left is"+(screenCoord-mThumbHalfWidth));
      /*  canvas.drawBitmap(buttonToDraw, screenCoord - mThumbHalfWidth,
                mTextOffset,
                paint);*/
        canvas.drawBitmap(buttonToDraw, screenCoord - mThumbHalfWidth,
                58,
                paint);

    }
    private void drawThumbRight(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
     //   Log.i("Hello","Screen Coord is"+screenCoord);
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw =mRightThumbDisableBitmap;

        } else {
            buttonToDraw = pressed ?mRightThumbPressedBitmap :mRightThumbNormalBitmap ;
        }
     //   Log.i("Hello","mTextOffset in rightThumb is"+mTextOffset+"And Left is"+(screenCoord-mThumbHalfWidth));
        canvas.drawBitmap(buttonToDraw, screenCoord - mThumbHalfWidth,
               58,
               paint);

    }


    /**
     * Draws a drop shadow beneath the slider thumb.
     *
     * @param screenCoord the x-coordinate of the slider thumb
     * @param canvas      the canvas on which to draw the shadow
     */
    private void drawThumbShadow(float screenCoord, Canvas canvas) {
        mThumbShadowMatrix.setTranslate(screenCoord + mThumbShadowXOffset, mTextOffset + mThumbHalfHeight + mThumbShadowYOffset);
        mTranslatedThumbShadowPath.set(mThumbShadowPath);
        mTranslatedThumbShadowPath.transform(mThumbShadowMatrix);
        canvas.drawPath(mTranslatedThumbShadowPath, shadowPaint);
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX) {
        Log.i("Hello","I am in evalPressedThumb()***************");
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
          //  Log.i("Hello","I AM IN ifffffffffffffff****************");
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
           Log.i("Hello","Result is"+touchX / getWidth() +"And getWidth() is"+getWidth());
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;

        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= mThumbHalfWidth;
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    static int countInvalidate = 0;
    private void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
      //  Log.i("Hello","NormalisedMinValue is called"+normalizedMinValue);
        invalidate();
    //    Log.i("Hello","No. Of invalidate() is called"+countInvalidate);
     //   countInvalidate++;
        // for anlysis
      //  requestLayout();

    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
      //  Log.i("Hello","normalizedMaxValue is called"+normalizedMaxValue);
        invalidate();
      //  Log.i("Hello","No. Of invalidate() is called"+countInvalidate++);
    }

    /**
     * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
     */
    @SuppressWarnings("unchecked")
    private T normalizedToValue(double normalized) {
        double v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim);
        // TODO parameterize this rounding to allow variable decimal points
        return (T) numberType.toNumber(Math.round(v * 100) / 100d);
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */
    private double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    /**
     * Thumb constants (min and max).
     */
    private enum Thumb {
        MIN, MAX
    }

    /*
     * Utility enumeration used to convert between Numbers and doubles.
     */
    private enum NumberType {
        LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (this) {
                case LONG:
                    return (long) value;
                case DOUBLE:
                    return value;
                case INTEGER:
                    return (int) value;
                case FLOAT:
                    return (float) value;
                case SHORT:
                    return (short) value;
                case BYTE:
                    return (byte) value;
                case BIG_DECIMAL:
                    return BigDecimal.valueOf(value);
            }
            throw new InstantiationError("can't convert " + this + " to a Number object");
        }
    }

    /**
     * Callback listener interface to notify about changed range values.
     * @param <T> The Number type the RangeSeekBar has been declared with.
     */
    public interface OnRangeSeekBarChangeListener<T> {

        void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, T minValue, T maxValue);
    }

}
