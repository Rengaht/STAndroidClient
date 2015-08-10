package com.rengatartgital.photonandroidclient;

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
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class BCountDownView extends View implements AnimatorUpdateListener{
	
	private ValueAnimator number_animator;
	private Bitmap[] arr_bmp;
	private int cur_num;
	private Paint paint_bmp;
	
	public BCountDownView(Context context){
		super(context);
		setup();
	}
	public BCountDownView(Context context, AttributeSet attrs){
		super(context, attrs);
		setup();
	}
	public BCountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setup();
	}
	
	private void setup(){
		number_animator=ValueAnimator.ofInt(60,0);
		number_animator.setDuration(60000);
		
		number_animator.setInterpolator(new LinearInterpolator());
		number_animator.addUpdateListener(this);
		number_animator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0){
				
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
	public void onAnimationUpdate(ValueAnimator animator){
		
		cur_num=(Integer)animator.getAnimatedValue();
		postInvalidate();
	}
	
	void setupBitmap(int l,int t,int r,int b){
		
		arr_bmp=new Bitmap[10];
		for(int i=0;i<10;++i){
			Bitmap obmp=BitmapFactory.decodeResource(getResources(),
									getResources().getIdentifier("drawable/gameb_countdown_"+i, null, getContext().getPackageName()));
			arr_bmp[i]=Bitmap.createScaledBitmap(obmp,(r-l)/2,(b-t),true);
//			obmp.recycle();
		}
		
	}
	@Override
	public void onDraw(Canvas canvas){
		
		int n1=cur_num/10;
		int n2=cur_num%10;
		float numwid=arr_bmp[n1].getWidth();
		if(arr_bmp[n1]!=null) canvas.drawBitmap(arr_bmp[n1],this.getWidth()/2-numwid*.9f,0,paint_bmp);
		if(arr_bmp[n2]!=null) canvas.drawBitmap(arr_bmp[n2],this.getWidth()/2-numwid*.1f,0,paint_bmp);
		
	}
	
	public void start(){
		
		if(number_animator.isRunning()) number_animator.end();
		number_animator.start();
	}
	
}
