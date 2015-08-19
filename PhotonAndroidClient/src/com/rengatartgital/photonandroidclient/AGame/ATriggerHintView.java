package com.rengatartgital.photonandroidclient.AGame;

import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.drawable;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ATriggerHintView  extends View  implements AnimatorUpdateListener{
	
	final int MBMP=3;
	int icur_bmp;
	Bitmap[] arr_bmp;
	Paint paint_bmp;
	
	private ValueAnimator fadein_animator,fadeout_animator;
	
	public ATriggerHintView(Context context) {
		super(context);
		setup();
	}
	public ATriggerHintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}
	public ATriggerHintView(Context context, AttributeSet attrs,int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setup();
	}
	void setup(){
		fadein_animator = ValueAnimator.ofFloat(0,255);
		fadein_animator.setDuration(250);
		fadein_animator.setStartDelay(1000);
        fadein_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadein_animator.addUpdateListener(this);
        fadein_animator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0){
				fadeout_animator.start();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
        	
        });
        
        fadeout_animator = ValueAnimator.ofFloat(255,0);
		fadeout_animator.setDuration(250);
		fadeout_animator.setStartDelay(2500);
        fadeout_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeout_animator.addUpdateListener(this);
        fadeout_animator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
				
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				
				icur_bmp=(icur_bmp+1)%MBMP;
//				Log.i("STLog","Update Hint: "+icur_bmp);
				fadein_animator.start();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				
			}
        	
        });
        
	
        paint_bmp=new Paint();
	}
	@Override
	public void setVisibility(int visibility){
		super.setVisibility(visibility);
		if(visibility==View.VISIBLE){
			fadein_animator.start();
		}else{
			fadein_animator.end();
			fadeout_animator.end();
		}
	}
	void setupBitmap(int l,int t,int r,int b){
		
		arr_bmp=new Bitmap[MBMP];
		for(int i=0;i<MBMP;++i){
			Bitmap obmp=null;
			switch(i){
				case 0:
					obmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamea_please_1);
					break;
				case 1:
					obmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamea_please_2);
					break;
				case 2:
					obmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamea_please_3);
					break;
			}
			arr_bmp[i]=Bitmap.createScaledBitmap(obmp,(r-l),(b-t),true);
			obmp.recycle();
		}
		icur_bmp=0;
	}
	@Override
	public void onDraw(Canvas canvas){
		
		if(arr_bmp[icur_bmp]!=null) canvas.drawBitmap(arr_bmp[icur_bmp],0,0,paint_bmp);
		
	}
	@Override
	public void onAnimationUpdate(ValueAnimator animator) {
		float _alpha=(Float)animator.getAnimatedValue();
		paint_bmp.setAlpha((int)_alpha);
		invalidate();
	}
	
}
