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
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.iam42.money.R;
import com.iam42.money.model.Flake;

import java.util.ArrayList;
import java.util.Iterator;

public class FlakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private SurfaceHolder mHolder; // 用于控制SurfaceView
    private Thread t; // 声明一条线程
    private volatile boolean flag; // 线程运行的标识，用于控制线程
    private Canvas mCanvas; // 声明一张画布

    private final int INITFLAKECOUNT = 30;
    private final int ADDFLAKECOUNT = 5;
    private final int PERADDTIME = 1;

    ArrayList<Bitmap> droids;       // The bitmap that all flakes use
    ArrayList<Flake> flakes = new ArrayList<Flake>(); // List of current flakes
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    long startTime, prevTime, addFlakeTime;

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
            Draw(); // 调用自定义画画方法
        }
    }

    public FlakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder(); // 获得SurfaceHolder对象
        mHolder.addCallback(this); // 为SurfaceView添加状态监听
        setFocusable(true); // 设置焦点

        initWidget();
        addFlakes(INITFLAKECOUNT);

    }


    private void initWidget() {
        droids = new ArrayList<Bitmap>();
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter1));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter2));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter3));
        droids.add(BitmapFactory.decodeResource(getResources(), R.drawable.easter4));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        flakes.clear();
        addFlakes(INITFLAKECOUNT);
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        addFlakeTime = startTime;
    }

    private void Draw() {

        long nowTime = System.currentTimeMillis();
        float secs = (float) (nowTime - prevTime) / 1000f;
        adjustFlakeStatus(secs);
        prevTime = nowTime;

        if ((nowTime - addFlakeTime) / 1000f > PERADDTIME) {
            addFlakes(ADDFLAKECOUNT);
            addFlakeTime = nowTime;
        }

        drawOnCanvas(flakes);
    }

    private void adjustFlakeStatus(float timeInterval) {
        Iterator<Flake> it = flakes.iterator();
        while (it.hasNext()) {
            Flake flake = it.next();
            flake.y += (flake.speed * timeInterval);
            if (flake.y > getHeight()) {
                it.remove();
            }
            flake.rotation = flake.rotation + (flake.rotationSpeed * timeInterval);
        }
    }

    private void drawOnCanvas(ArrayList<Flake> flakes) {
        mCanvas = mHolder.lockCanvas(); // 获得画布对象，开始对画布画画
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < flakes.size(); ++i) {
            Flake flake = flakes.get(i);
            m.setTranslate(-flake.width/2, -flake.height/2);
            m.postRotate(flake.rotation);
            m.postTranslate(flake.width/2 + flake.x, flake.height/2 + flake.y);
            mCanvas.drawBitmap(flake.bitmap, m, null);
        }
        mHolder.unlockCanvasAndPost(mCanvas); //画完显示
    }

    private void addFlakes(int count) {
        if (flakes == null) {
            return;
        }
        for (int i = 0; i < count; i++){
            flakes.add(Flake.createFlake(getWidth(), droids));
        }
    }

    public void pause() {
        animator.cancel();
    }

    public void resume() {
        animator.start();
    }

    public interface StoppedListener {
        public void onStoppedEaster(int count);
    }

    public void setFlakeStoppedListener(StoppedListener stoppedListener) {
        mStoppedListener = stoppedListener;
    }
}
