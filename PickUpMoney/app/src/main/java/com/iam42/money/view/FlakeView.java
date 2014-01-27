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
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.iam42.money.R;
import com.iam42.money.model.Flake;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is the custom view where all of the Droidflakes are drawn. This class has
 * all of the logic for adding, subtracting, and rendering Droidflakes.
 */
public class FlakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private SurfaceHolder mHolder; // 用于控制SurfaceView
    private Thread t; // 声明一条线程
    private volatile boolean flag; // 线程运行的标识，用于控制线程
    private Canvas mCanvas; // 声明一张画布

    private final int FLAKECOUNT = 30;

    ArrayList<Bitmap> droids;       // The bitmap that all flakes use
    ArrayList<Flake> flakes = new ArrayList<Flake>(); // List of current flakes
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    long startTime, prevTime; // Used to track elapsed time for animations and fps

    Matrix m = new Matrix(); // Matrix used to translate/rotate each flake during rendering

    StoppedListener mStoppedListener;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        t = new Thread(this); // 创建一个线程对象
        flag = true; // 把线程运行的标识设置成true
        t.start(); // 启动线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false; // 把线程运行的标识设置成false
        mHolder.removeCallback(this);
    }

    @Override
    public void run() {

        while (flag) {
            try {
                synchronized (mHolder) {
                    Thread.sleep(1); // 让线程休息1毫秒
                    Draw(); // 调用自定义画画方法
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public interface StoppedListener {
        public void onStoppedEaster(int count);
    }

    public void setFlakeStoppedListener(StoppedListener stoppedListener) {
        mStoppedListener = stoppedListener;
    }

    public FlakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder(); // 获得SurfaceHolder对象
        mHolder.addCallback(this); // 为SurfaceView添加状态监听
        setFocusable(true); // 设置焦点

        initWidget();
        initFlakes(FLAKECOUNT);

    }


    private void initWidget() {
        droids = new ArrayList<Bitmap>();
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter1));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter2));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter3));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter4));
    }

    void initFlakes(int quantity) {
        for (int i = 0; i < quantity; ++i) {
            flakes.add(Flake.createFlake(getWidth(), droids));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        flakes.clear();
        initFlakes(FLAKECOUNT);
        startTime = System.currentTimeMillis();
        prevTime = startTime;
    }

    private void Draw() {

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
            flake.rotation = flake.rotation + (flake.rotationSpeed * secs);
        }

        mCanvas = mHolder.lockCanvas(); // 获得画布对象，开始对画布画画
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < flakes.size(); ++i) {
            Flake flake = flakes.get(i);
            m.setTranslate(-flake.width/2, -flake.height/2);
            m.postRotate(flake.rotation);
            m.postTranslate(flake.width/2 + flake.x, flake.height/2 + flake.y);
            mCanvas.drawBitmap(flake.bitmap, m, null);
        }
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void pause() {
        animator.cancel();
    }

    public void resume() {
        animator.start();
    }
}
