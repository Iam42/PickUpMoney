package com.iam42.money.view;

/**
 * Created by a42 on 14-1-7.
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.iam42.money.R;
import com.iam42.money.model.Flake;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is the custom view where all of the Droidflakes are drawn. This class has
 * all of the logic for adding, subtracting, and rendering Droidflakes.
 */
public class FlakeView extends View implements View.OnTouchListener{

    private final int FLAKECOUNT = 30;
    private final int LASTTIME = 10;

    ArrayList<Bitmap> droids;       // The bitmap that all flakes use
    ArrayList<Flake> flakes = new ArrayList<Flake>(); // List of current flakes
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    long startTime, prevTime; // Used to track elapsed time for animations and fps
    Paint textScore;    // Used for rendering score text
    Paint textTime;    // Used for rendering time text
    Matrix m = new Matrix(); // Matrix used to translate/rotate each flake during rendering
    String timeString = "";
    String numFlakesString = "";
    float mTouchX = 0f;
    float mTouchY = 0f;
    StoppedListener mStoppedListener;

    public interface StoppedListener {
        public void onStoppedEaster(int count);
    }

    public void setFlakeStoppedListener(StoppedListener stoppedListener) {
        mStoppedListener = stoppedListener;
    }

    public FlakeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnTouchListener(this);

        initWidget();
        initFlakes(FLAKECOUNT);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                long nowTime = System.currentTimeMillis();
                float secs = (float) (nowTime - prevTime) / 1000f;
                prevTime = nowTime;
                Iterator<Flake> it = flakes.iterator();
                while (it.hasNext()) {
                    Flake flake = it.next();
                    flake.y += (flake.speed * secs);
                    if (flake.y > getHeight()) {
                        flake.y = 0 - flake.height;
                    }
                    if (Math.abs(flake.x - mTouchX) < 10 && Math.abs(flake.y - mTouchY) < 10) {
                        it.remove();
                    }
                    flake.rotation = flake.rotation + (flake.rotationSpeed * secs);
                }
                setNumFlakes(FLAKECOUNT);
                invalidate();
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(3000);
    }


    private void setNumFlakes(int quantity) {
        if (flakes != null) {
            numFlakesString = "获得元宝: " + (quantity - flakes.size());
        } else {
            numFlakesString = "获得元宝: " + 0;
        }
    }

    private void initWidget() {
        droids = new ArrayList<Bitmap>();
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter1));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter2));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter3));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter4));
        textScore = new Paint(Paint.ANTI_ALIAS_FLAG);
        textScore.setColor(Color.WHITE);
        textScore.setTextSize(30);
        textTime = new Paint(Paint.ANTI_ALIAS_FLAG);
        textTime.setColor(Color.WHITE);
        textTime.setTextSize(60);
    }

    void initFlakes(int quantity) {
        for (int i = 0; i < quantity; ++i) {
            flakes.add(Flake.createFlake(getWidth(), droids));
        }
        setNumFlakes(quantity);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        flakes.clear();
        initFlakes(FLAKECOUNT);
        animator.cancel();
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < flakes.size(); ++i) {
            Flake flake = flakes.get(i);
            m.setTranslate(-flake.width/2, -flake.height/2);
            m.postRotate(flake.rotation);
            m.postTranslate(flake.width/2 + flake.x, flake.height/2 + flake.y);
            canvas.drawBitmap(flake.bitmap, m, null);
        }

        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - startTime;
        if (deltaTime > 1000) {
            int secs = (int) (deltaTime / 1000);
            timeString = "时间: " + secs;
            if (secs >= LASTTIME) {
                mStoppedListener.onStoppedEaster(FLAKECOUNT - flakes.size());
                pause();
            }
        }
        canvas.drawText(numFlakesString, 20, getHeight() - 50, textScore);
        canvas.drawText(timeString, getWidth() - 240, 80, textTime);
    }

    public void pause() {
        animator.cancel();
    }

    public void resume() {
        animator.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouchX = event.getX();
            mTouchY = event.getY();
        }
        return false;
    }
}
