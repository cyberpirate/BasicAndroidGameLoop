package com.example.basicgameloop.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.SparseArray;

import com.example.basicgameloop.R;

public class SpriteHolder {
	
	// put your sprite's to load in here
	// use SimpleSprite(rid) for a single sprite
	// use AnimatedSprite to cut up a sprite sheet
	private static final SpriteLoader[] SPRITE_LIST = new SpriteLoader[] { 
		new SimpleSprite(R.drawable.ball_sprite),
		new SimpleSprite(R.drawable.ic_launcher),
	};
	
	public static SparseArray<Bitmap> sprites = new SparseArray<Bitmap>();
	
	public static Point getDimen(int rID) {
		Bitmap bit = sprites.get(rID);
		if(bit == null) return null;
		
		return new Point(bit.getWidth(), bit.getHeight());
	}

	/**
	 * Load bitmaps into SpriteHolder.sprites
	 * 
	 * @param res the resources, call getResources() to get this
	 * 
	 * @return returns success/fail
	 */
	public static boolean loadSprites(Resources res) {
		boolean ret = true;
		for (SpriteLoader sprite : SPRITE_LIST) {
			ret = ret && sprite.load(res, sprites);
		}
		
		return ret;
	}

	private static interface SpriteLoader {
		public abstract boolean load(Resources res, SparseArray<Bitmap> dataHolder);
	}

	private static class SimpleSprite implements SpriteLoader {
		private int resId;

		public SimpleSprite(int rid) {
			resId = rid;
		}

		public boolean load(Resources res, SparseArray<Bitmap> dataHolder) {
			Bitmap tmpBMP = BitmapUtils.decodeBitmap(res, resId);
			
			if(tmpBMP != null) {
				dataHolder.put(resId, tmpBMP);
				return true;
			}
			
			return false;
		}
	}

	private static class AnimatedSprite implements SpriteLoader {
		private int resID;
		private int frames;
		private int[] frameID;
		private boolean horiz;
		
		public AnimatedSprite(int resID, int frames, int[] frameID, boolean horizontal) {
			this.resID = resID;
			this.frames = frames;
			this.frameID = frameID;
			this.horiz = horizontal;
			
			if(frameID.length != frames)
				throw new IndexOutOfBoundsException();
		}
		
		@Override
		public boolean load(Resources res, SparseArray<Bitmap> dataHolder) {
			Bitmap tmpBMP = BitmapUtils.decodeBitmap(res, resID);
			
			if(tmpBMP == null) return false;
			
			int cut = 0;
			if(horiz) {
				cut = tmpBMP.getWidth()/frames;
				int height = tmpBMP.getHeight();
				Bitmap cutBMP;
				
				for(int i = 0; i < frames; i++) {
					cutBMP = Bitmap.createBitmap(tmpBMP, cut*i, 0, cut, height);
					if(cutBMP == null) return false;
					dataHolder.put(frameID[i], cutBMP);
				}
			} else {
				cut = tmpBMP.getHeight()/frames;
				int width = tmpBMP.getWidth();
				Bitmap cutBMP;
				
				for(int i = 0; i < frames; i++) {
					cutBMP = Bitmap.createBitmap(tmpBMP, 0, cut*i, width, cut);
					if(cutBMP == null) return false;
					dataHolder.put(frameID[i], cutBMP);
				}
			}
			
			tmpBMP.recycle();
			return true;
		}
		
	}
}
