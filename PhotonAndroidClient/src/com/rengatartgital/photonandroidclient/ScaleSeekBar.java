package com.rengatartgital.photonandroidclient;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ScaleSeekBar extends View{
	
	float MAX_POSITION=10;
	float MIN_POSITION=0;
	
	private Bitmap thumb_bmp;
	private float thumb_position; 
	Paint paint;
	
	public ScaleSeekBar(Context context){
		super(context);
		setupView();
	}
	public ScaleSeekBar(Context context, AttributeSet attrs){
		super(context, attrs);
		setupView();
	}
	public ScaleSeekBar(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		setupView();
	}
	private void setupView(){		
		thumb_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamec_bar_point);
		thumb_position=0;
		paint=new Paint();
	}
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
//		int display_width=getWidth();
//		int display_height=getHeight();
//		
		
		
		//canvas.drawARGB(120,0,0,255);
		//canvas.save();
		canvas.translate(thumb_position-thumb_bmp.getWidth()/2,0);
			canvas.drawBitmap(thumb_bmp,0,0,paint);
		//canvas.restore();
		
		
		
	}
	@Override
	protected void onLayout(boolean changed,int l,int t,int r,int b){
		if(!changed) return;
		
		int hei=getHeight();
		thumb_bmp=Bitmap.createScaledBitmap(thumb_bmp,(int)(hei*.78f),hei,true);
		Log.i("STLog","set thumb size= "+thumb_bmp.getHeight());
		
		MAX_POSITION=getWidth()-hei*.78f/2-getWidth()*0.138f;
		MIN_POSITION=hei*.78f/2+getWidth()*0.138f;
	}
	
	public float getPosition(){
		return thumb_position;
	}
	public void resetPosition(float pos){
		
		if(pos>1 || pos<0) return;
		
		thumb_position=(MAX_POSITION-MIN_POSITION)*pos+MIN_POSITION;
		this.invalidate();
	}
	public float setPosition(float delta_pos){
		if(Math.abs(delta_pos)>1){
			thumb_position+=delta_pos;
			if(thumb_position>MAX_POSITION) thumb_position=MAX_POSITION;
			if(thumb_position<MIN_POSITION) thumb_position=MIN_POSITION;
		}
		
		Log.i("STLog","seek pos= "+thumb_position);
		this.invalidate();
		
		return getScale(thumb_position);
	}
	private float getScale(float pos){
		
		float portion=(pos-MIN_POSITION)/(MAX_POSITION-MIN_POSITION);
		if(portion>.5) return (portion-.5f)/.5f*3.0f+1;
		else return (1-(.5f-portion));
		
	}
}
