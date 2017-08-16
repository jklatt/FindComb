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
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.ShareCompat;
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


public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    Activity mact;

    // In case moving is an option
    private Point mPlayer;
    private int mLevelsNumber;

    private Level mLevel;

    // Mechanics
    private boolean[] rbgpy;
    private int mLevelIndicator;
    private int mScore;
    private long mTimestamp;



    //These objects will be used for drawing
    //private Paint paint;
    private Canvas canvas;
    private int width;
    private int height;
    private SurfaceHolder surfaceHolder;
    private Context mContext;

    private final MediaPlayer soundfinal;
    private final MediaPlayer soundright;
    private final MediaPlayer soundwrong;


    public GameView(Context context, Activity act) {
        super(context);
        mact = act;

        // The visuals
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;


        soundright = MediaPlayer.create(context,R.raw.stapler);
        soundwrong = MediaPlayer.create(context,R.raw.buzz);
        soundfinal = MediaPlayer.create(context,R.raw.fanfare);

        //init mechanics
        mScore = 120; //The player has two minutes
        mLevelIndicator = 1;
        mTimestamp = System.currentTimeMillis();
        mLevelsNumber = 7;


        //initializing drawing objects
        surfaceHolder = getHolder();
        mContext = context;


        LevelInit();

        //Stuff that we might need later
        mPlayer = new Point(1500, 300);
        playing = true;


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScore > 0 && mLevelIndicator <= mLevelsNumber) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPlayer.set((int) event.getX(), (int) event.getY());
                    mLevel.checkClicked(mPlayer);
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    playing = false;
            }

        }


        return true;
        //return super.onTouchEvent(event);
    }

    public void Introduction(){
        mact.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "wähle das unterscheidende Merkmal der Patientengruppen";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();
            }
        });
    }

    public void Combination(){
        mact.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "vielleicht sind auch Interaktionen unterscheidend";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();
            }
        });
    }


    public void Logic(){
        mact.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "Unterschiedliche Merkmale koennen den gleichen Effekt haben.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();
            }
        });

    }

    private void update() {
        if (mScore > 0) {

            if ((System.currentTimeMillis() - mTimestamp) > 1000 && mLevelIndicator <= mLevelsNumber) {
                mScore--;
                mTimestamp = System.currentTimeMillis();
            }

            if (mScore < 0) {
                playing = false;
            }

            if (mLevel.clicked == true) {
                //Evaluate configuration
                boolean[] rbgpyPlayer = mLevel.getClicked();
                if (mLevelIndicator <= mLevelsNumber) {
                    if (Arrays.equals(rbgpy, rbgpyPlayer)) {
                        mScore = mScore + 10;
                        if(mLevelIndicator < mLevelsNumber) {
                            soundright.start();
                        }
                        else{
                            soundfinal.start();
                        }
                        mLevelIndicator++;
                        LevelInit();

                    } else {
                        mScore = mScore - 10;
                        soundwrong.start();
                        mLevel.reset();
                    }
                }

                //What happens next?
                //if (mLevelIndicator <= mLevelsNumber) {
                //} else {
                //    mLevelIndicator++;
                //}
            }
        }
    }

    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            Paint paint = new Paint();
            paint.setTextSize(100);
            paint.setColor(Color.BLACK);
            //drawing a background color for canvas
            canvas.drawColor(Color.WHITE);

            if (mScore > 0 && mLevelIndicator <= mLevelsNumber) {
                mLevel.draw(canvas);
                canvas.drawText(String.valueOf(mScore), 27 * width / 30, height / 10, paint);
                //Unlocking the canvas

            } else {
                if (mScore < 0) {
                    canvas.drawText("Game Over", width / 3, height / 2, paint);
                    soundwrong.start();
                } else {
                    canvas.drawText("Your Score: " + String.valueOf(mScore), width / 4, height / 2, paint);
                }

            }
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

            Intent resultIntent = new Intent();
            resultIntent.putExtra("score", mScore);
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

    public int getScore() {
        return mScore;
    }


    // The level architecture

    public void LevelInit() {
        switch (mLevelIndicator) {
            case 1:
                Introduction();
                // Binary indicator of colors in columns: {red, blue, green, pruple, yellow}
                // mLevel = new Level(mContext, width, height,
                // the first column healthy        new boolean[]{false, true, false, false, false},
                // the second column healthy       new boolean[]{false, true, false, false, false},
                //the third column thealthy        new boolean[]{false, true, false, false, false},
                // the first column sick        new boolean[]{false, true, false, false, false},
                // the second column sick       new boolean[]{false, true, false, false, false},
                //the third column sick        new boolean[]{false, true, false, false, false},
                // what colors to we use?        new boolean[]{true, true, false, false, false});
                // The solution rbgpy = new boolean[]{true, false, false, false, false};
                mLevel = new Level(mContext, width, height,
                        new boolean[]{false, true, false, false, false},
                        new boolean[]{false, true, false, false, false},
                        new boolean[]{false, true, false, false, false},
                        new boolean[]{true, false, false, false, false},
                        new boolean[]{true, true, false, false, false},
                        new boolean[]{true, false, false, false, false},
                        new boolean[]{true, true, false, false, false});
                rbgpy = new boolean[]{true, false, false, false, false};
                break;
            case 2:
                rbgpy = new boolean[]{true, false, false, false, false};
                mLevel = new Level(mContext, width, height,
                        new boolean[]{false, true, true, false, false},
                        new boolean[]{false, false, true, false, false},
                        new boolean[]{false, true, true, false, false},
                        new boolean[]{true, false, false, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, false, false, false},
                        new boolean[]{true, true, true, false, false});
                break;
            case 3:
                Combination();
                rbgpy = new boolean[]{true, false, true, false, false};
                mLevel = new Level(mContext, width, height,
                        new boolean[]{false, true, true, false, false},
                        new boolean[]{true, true, false, false, false},
                        new boolean[]{false, true, true, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, true, false, false});

                break;
            case 4:
                rbgpy = new boolean[]{true, false, true, false, false};
                mLevel = new Level(mContext, width, height,
                        new boolean[]{true, true, false, false, false},
                        new boolean[]{false, true, false, false, false},
                        new boolean[]{false, false, true, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, false, true, false, false},
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, true, false, false});
                break;
            case 5:
                mLevel = new Level(mContext, width, height,
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{true, true, false, false, true},
                        new boolean[]{false, true, true, false, true},
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{true, true, true, false, true});
                rbgpy = new boolean[]{true, false, true, false, false};
                break;
            case 6:
                mLevel = new Level(mContext, width, height,
                        new boolean[]{true, true, true, false, false},
                        new boolean[]{true, true, false, false, true},
                        new boolean[]{false, true, true, false, false},
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{false, true, true, false, true},
                        new boolean[]{true, true, true, false, true},
                        new boolean[]{true, true, true, false, true});
                rbgpy = new boolean[]{false, false, true, false, true};
                break;
            case 7:
                Logic();
                mLevel = new Level(mContext, width, height,
                        new boolean[]{false, false, true, false, true},
                        new boolean[]{true, true, false, false, true},
                        new boolean[]{false, true, false, true, false},
                        new boolean[]{true, false, true, false, true},
                        new boolean[]{true, true, false, true, false},
                        new boolean[]{true, false, false, true, true},
                        new boolean[]{true, true, true, true, true});
                rbgpy = new boolean[]{true, false, true, true, false};
                break;

        }


    }
}