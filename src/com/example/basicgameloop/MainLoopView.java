package com.example.basicgameloop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.basicgameloop.constants.Const;
import com.example.basicgameloop.utils.SpriteHolder;

public class MainLoopView extends SurfaceView implements SurfaceHolder.Callback, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

	// Useful Classes
	private Activity activity;
	private ScaleGestureDetector scaleDetector;
	private GestureDetector gestureDetector;
	
	// Paints
	private Paint textPaint;
	
	// DEBUG vars
	float frameRate = 0;
	int debugUpdate = 0;

	// Info cache
	private int canvasWidth, canvasHeight;
	private float scale = 1;
	
	// Thread cache
	private float cScale = scale;
	private int cxDrawOffset, cyDrawOffset;

	// Anti GP (U/R Thread)
	private Matrix bitmapMatrix;
	
	protected void buildSplashScreen(Canvas c) {
		// Your loading screen

		// record width and height, realistically this shouldn't change
		// unless you disable the rotation lock (AndroidManifest.xml -> android:screenOrientation="[portrait|landscape]")
		canvasWidth = c.getWidth();
		canvasHeight = c.getHeight();
	}

	// load game resources
	protected void initGame() {
		//init useful classes
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { // these are used for detecting touch input, you can delete them if you just want to interpret raw input yourself
				scaleDetector = new ScaleGestureDetector(getContext(), MainLoopView.this);
				gestureDetector = new GestureDetector(getContext(), MainLoopView.this);
			}
		});

		//init Data
		SpriteHolder.loadSprites(getResources());
		
		textPaint = new Paint();
		textPaint.setColor(Color.RED);
		textPaint.setTextSize(80);
		
		bitmapMatrix = new Matrix();
		
		//give the garbage man a chance to clean up while you are still showing the loading screen
		System.gc();
	}

	protected void update(int dt) {
		// Update logic
		if(Const.DEBUG) updateDebugInfo(dt);
		
		updateThreadCache();
	}
	
	/**
	 * Cache the variables before the update loop to prevent threading issues (user input is run on a different thread)
	 */
	protected void updateThreadCache() {
		cScale = scale;
	}
	
	protected void render(Canvas c) { // things will be drawn on top of each other in order that you called the draw functions
		// Clear canvas, otherwise the last frame will show up
		c.drawColor(Color.BLUE);

		// Do render logic here
		c.drawBitmap(SpriteHolder.sprites.get(R.drawable.ball_sprite), 0,  0, null);

		//Show the debug
		if(Const.DEBUG) renderDebugInfo(c);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		
		gestureDetector.onTouchEvent(e);
		scaleDetector.onTouchEvent(e);

		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float factor  = detector.getScaleFactor();
		
		if(factor > 0.01) {
			scale *= factor;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		processTap(e.getX(), e.getY());
		return true;
	}
	
	protected void processTap(float x, float y) {
		//Check for box taps here
		
	}
	
	protected void updateDebugInfo(int dt) {
		debugUpdate -= dt;
		
		if(debugUpdate <= 0) {
			debugUpdate = Const.DEBUG_UPDATE;
		} else {
			return;
		}
		
		frameRate = (1000/dt);
	}

	protected void drawBitmap(Canvas c, int rID, int dx, int dy) {
		drawBitmap(c, rID, cScale, dx, dy, 0, null);
	}
	
	protected void drawBitmap(Canvas c, int rID, float scale, int dx, int dy) {
		drawBitmap(c, rID, scale, dx, dy, 0, null);
	}
	
	protected void drawBitmap(Canvas c, int rID, float scale, int dx, int dy, float rot, Paint paint) {
		drawBitmap(c, SpriteHolder.sprites.get(rID), scale, dx, dy, rot, paint);
	}
	
	protected void drawBitmap(Canvas c, int rID, float scale, int dx, int dy, float rot, Paint paint, boolean ignoreOffset) {
		drawBitmap(c, SpriteHolder.sprites.get(rID), scale, dx, dy, rot, paint, ignoreOffset);
	}
	
	protected void drawBitmap(Canvas c, Bitmap toDraw, float scale, int dx, int dy, float rot, Paint paint) {
		drawBitmap( c, toDraw, scale, dx, dy, rot, paint, false);
	}
	
	protected void drawBitmap(Canvas c, Bitmap toDraw, float scale, int dx, int dy, float rot, Paint paint, boolean ignoreOffset) {
		bitmapMatrix.reset();
		
		int tmpW = toDraw.getWidth();
		int tmpH = toDraw.getHeight();
		
		if(!ignoreOffset) {
			dx += cxDrawOffset;
			dy += cyDrawOffset;
		}
		
		// Only draw what's on the screen
		if(dx >= canvasWidth || dy > canvasHeight || dx+(tmpW*scale) <= 0 || dy+(tmpH*scale) <= 0) return;
		
		if(rot != 0)
			bitmapMatrix.setRotate(rot, tmpW/2, tmpH/2);
		
		bitmapMatrix.postScale(scale, scale);
		bitmapMatrix.postTranslate(dx, dy);
		
		c.drawBitmap(toDraw, bitmapMatrix, paint);
	}
	
	protected void renderDebugInfo(Canvas c) {
		c.drawText("" + frameRate, 5, 60, textPaint);
	}

	private class MainThread extends Thread {
		boolean running = true;
		boolean initialized;
		SurfaceHolder mSurfaceHolder;
		long lastUpdate;
		long currentTime;

		public MainThread(SurfaceHolder holder) {
			this(holder, false);
		}
		
		public MainThread(SurfaceHolder holder, boolean initialized) {
			super("GameThread");
			mSurfaceHolder = holder;
			this.initialized = initialized;
		}

		@Override
		public void run() {
			lastUpdate = System.currentTimeMillis();
			while (running) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						if (initialized) {
							currentTime = System.currentTimeMillis();
							
							update((int) (currentTime - lastUpdate));

							if (c != null)
								render(c);

							lastUpdate = currentTime;
						} else {
							buildSplashScreen(c);
						}
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}

					if (!initialized) {
						initGame();
						initialized = true;
					}
				}
			}
		}

		public void setRunning(boolean run) {
			running = run;
		}
	}

	MainThread thread;
	boolean started = false;

	public MainLoopView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new MainThread(holder);

		setFocusable(true); // make sure we get key events
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		if (!started) {
			thread.start();
			started = true;
		} else {
			thread = new MainThread(holder, true);
			thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public void setActivity(Activity a) {
		activity = a;
	}
}
