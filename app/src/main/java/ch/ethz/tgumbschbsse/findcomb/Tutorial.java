package ch.ethz.tgumbschbsse.findcomb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by tgumbsch on 8/10/17.
 */


public class Tutorial extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    Activity mact;

    public Level mLevel;
    public int Level; //For different tutorials
    private Picture h1;
    private Arrow marrow, m2arrow, m3arrow;

    // In case moving is an option
    private Point mPlayer;

    private int mstage;



    //These objects will be used for drawing
    //private Paint paint;
    private Canvas canvas;
    private int width;
    private int height;
    private SurfaceHolder surfaceHolder;
    private Context mContext;

    public Tutorial(Context context, Activity act) {
        super(context);
        mact = act;

        // The visuals
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;


        //initializing drawing objects
        surfaceHolder = getHolder();
        mContext = context;
        mstage = 1;
        marrow = new Arrow(new int[]{2*width/13,width*2,2*width/13,width*2}, new int[]{3*height/20,width*2, height/2, width*2}, 3, 0.8, 15, context, true);

        int [] x_spacing = new int[] {2*width/13, 2*width/13,5*width/13, 8*width/13,
                2*width/13, 2*width/13,5*width/13,
                2*width/13, 2*width/13,5*width/13, 7*width/13,
                2*width/13, 2*width/13,4*width/13,
                2*width/13, 2*width/13,4*width/13,5*width/13, 6*width/13,
                2*width/13, 2*width/13,4*width/13};
        int[] y_spacing = new int[] {height/20,height/20,height/20,height/20,
                3*height/20,3*height/20,3*height/20,
                5*height/20,5*height/20,5*height/20,5*height/20,
                8*height/20,8*height/20,8*height/20,
                10*height/20,10*height/20,10*height/20,10*height/20,10*height/20,
                12*height/20,12*height/20,12*height/20,};
        m2arrow = new Arrow(x_spacing,y_spacing,3,0.8,30, mContext,false);

        x_spacing = new int[] {4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13,4*width/13};
        y_spacing = new int[] {32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80,32*height/80};

        h1 = new Picture(R.drawable.leftside, mContext,0,0,3*height/7,2*height/3);

        mLevel = new Level(mContext, width, height,false,
                new boolean[]{false, true, false, true, false},
                new boolean[]{false, true, false, false, false},
                new boolean[]{false, true, false, false, true},
                new boolean[]{true, false, false, false, false},
                new boolean[]{true, true, true, false, false},
                new boolean[]{true, false, false, false, false},
                new boolean[]{true, true, false, false, false}, new boolean[]{true, true, false, false, false});
        mLevel.Tut = 1;

        //Stuff that we might need later
        mPlayer = new Point(1500, 300);
        playing = true;


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mstage++;
                if(mstage == 1) {
                }
                else if (mstage == 2){
                    marrow.change_visibility();
                    m2arrow.change_visibility();
                }
                else if (mstage == 3) {
                    mLevel.cs11.clicked = true;
                    mLevel.cs21.clicked = true;
                    mLevel.cs31.clicked = true;
                }
                else if(mstage>5){
                    System.out.println(String.valueOf(playing));
                    playing = false;
                }

            //return super.onTouchEvent(event);
        }
        return true;
    }


    private void update() {
        marrow.update();
        m2arrow.update();
    }

    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            Paint paint = new Paint();
            paint.setTextSize(width/30);
            //paint.setColor(Color.WHITE);
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            //drawing a background color for canvas
            //canvas.drawColor(Color.parseColor("#0099cc"));
            canvas.drawColor(Color.WHITE);

            String text;
            if(mstage==1) {
                h1.draw(canvas);
                text = getContext().getString(R.string.Tut1);
                canvas.drawText(text, width / 20, 9 * height / 12, paint);
            }
            else if(mstage == 2) {
                mLevel.draw(canvas);
                text = getContext().getString(R.string.Tut2);
                canvas.drawText(text,  width / 20, 9 * height / 12, paint);
            }
            else if(mstage ==3) {
                mLevel.draw(canvas);
                text = getContext().getString(R.string.Tut3);
                canvas.drawText(text, width/20, 9 * height / 12, paint);
            }
            else if(mstage == 4){
                text = getContext().getString(R.string.Tut324);
                canvas.drawText(text, width / 20, 4 * height / 12, paint);
            }
            else if(mstage == 5) {
                text = getContext().getString(R.string.Tut324);
                canvas.drawText(text, width / 20, 4 * height / 12, paint);

                text = getContext().getString(R.string.Tut325);
                canvas.drawText(text, width / 20, 6 * height / 12, paint);
            }

            marrow.draw(canvas);
            m2arrow.draw(canvas);


            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }


    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }


    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //System.out.println(String.valueOf(playing));

        if (playing != true) {
            System.out.println(String.valueOf(playing));
            Intent resultIntent = new Intent();
            ((Activity) mContext).setResult(Activity.RESULT_OK, resultIntent);
            ((Activity) mContext).finish();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


}