package com.rengatartgital.photonandroidclient;

import com.rengatartgital.photonandroidclient.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WheelView extends View{

	Bitmap center_wheel;
	Bitmap left_wheel,left_point;
	Bitmap right_wheel,right_point;
	
	float center_rotate_angle;
	float dest_center_rotate_angle;
	float t_center_angle;
	float left_rotate_angle,right_rotate_angle;
	
	
	public WheelView(Context context){
		super(context);
		setupBitmap();
	}
	public WheelView(Context context, AttributeSet attrs){
		super(context, attrs);
		setupBitmap();
	}
	public WheelView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		setupBitmap();
	}
	
	void setupBitmap(){
		center_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.wheel);
		left_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.left_wheel);
		left_point=BitmapFactory.decodeResource(getResources(),R.drawable.left_point);
		right_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.right_wheel);
		right_point=BitmapFactory.decodeResource(getResources(),R.drawable.right_point);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	   // Try for a width based on our minimum
	   int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
	   int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

	   // Whatever the width ends up being, ask for a height that would let the pie
	   // get as big as it can
	   int minh = MeasureSpec.getSize(w)+ getPaddingBottom() + getPaddingTop();
	   int h = resolveSizeAndState(MeasureSpec.getSize(w),heightMeasureSpec,0);

	   setMeasuredDimension(w, h);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		
		super.onDraw(canvas);
//		Log.i("STLog","Draw Wheel!");
		
		canvas.drawBitmap(left_point,750,100,null);
				
		Matrix matrix_left=new Matrix();
		matrix_left.preTranslate(750,100);
		matrix_left.preRotate((float)(180*Math.sin(left_rotate_angle)),left_point.getWidth()/2,left_point.getHeight()/2);
		
		canvas.drawBitmap(left_wheel,matrix_left,null);
		left_rotate_angle+=.1;
		
		
		canvas.drawBitmap(right_wheel,50,120,null);
		
		Matrix matrix_right=new Matrix();
		matrix_right.preTranslate(50,120);
		matrix_right.preRotate((float) (40*Math.sin(right_rotate_angle)),360,436);
		
		canvas.drawBitmap(right_point,matrix_right,null);
		right_rotate_angle+=.05;
		
		
		
		
		
		if(t_center_angle<1) t_center_angle+=.1;
		Matrix matrix_center=new Matrix();
		matrix_center.preTranslate(400,50);
		matrix_center.preRotate(getCurrentCenterAngle(),center_wheel.getWidth()/2,center_wheel.getHeight()/2);
		
		canvas.drawBitmap(center_wheel,matrix_center,null);
		
//		Log.i("Sensor","->"+getCurrentCenterAngle());
		
	}
	public void setRotateAngle(float set_angle){
		
		if(set_angle!=dest_center_rotate_angle){
//			Log.i("Sensor",center_rotate_angle+"->"+dest_center_rotate_angle);
			center_rotate_angle=getCurrentCenterAngle();
			dest_center_rotate_angle=set_angle;
			t_center_angle=0;
//			Log.i("Sensor",center_rotate_angle+"->"+dest_center_rotate_angle);
			
		}
		
		invalidate();
		
	}
	private float getCurrentCenterAngle(){
		
		return dest_center_rotate_angle*(t_center_angle)+center_rotate_angle*(1-t_center_angle);
	}
	 
}
