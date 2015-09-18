package com.rengatartgital.photonandroidclient.BGame;

import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.dimen;
import com.rengatartgital.photonandroidclient.R.drawable;
import com.rengatartgital.photonandroidclient.ViewUtil.ImageDecodeHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.LayoutHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WheelView extends View{

	boolean wait_mode=true;
	Paint wait_paint;
	
	Bitmap center_wheel;
	Bitmap left_wheel,left_point;
	Bitmap right_wheel,right_point;
	
	float center_rotate_angle;
	float dest_center_rotate_angle;
	float t_center_angle;
	float left_rotate_angle,right_rotate_angle;
	
	int wwid,whei;
	Rect  wheel_rect,left_wheel_rect,right_wheel_rect,left_point_rect,right_point_rect;
	
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
//		center_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_blue);
//		left_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_left_wheel);
//		left_point=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_left_point);
//		right_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_right_wheel);
//		right_point=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_right_point);
		
		//setupRect();
	}
	void clear(){
		if(center_wheel!=null) center_wheel.recycle();
		if(left_wheel!=null) left_wheel.recycle();
		if(right_wheel!=null) right_wheel.recycle();
		if(left_point!=null) left_point.recycle();
		if(right_point!=null) right_point.recycle();

	}
	void setupRect(){

		//clear();

		wwid=this.getWidth();
		whei=this.getHeight();


		Resources res=getResources();
		wheel_rect=LayoutHelper.getLandscapeLayoutCoordinate(0, 0, wwid, whei,
				res.getDimension(R.dimen.wheel_cx), res.getDimension(R.dimen.wheel_cy),
				res.getDimension(R.dimen.wheel_width), res.getDimension(R.dimen.wheel_height));
		center_wheel=ImageDecodeHelper.decodeImageToSize(getResources(), drawable.gameb_blue, wheel_rect.width(), wheel_rect.height());

		left_wheel_rect=LayoutHelper.getLandscapeLayoutCoordinate(0, 0, wwid, whei,
				res.getDimension(R.dimen.left_wheel_cx), res.getDimension(R.dimen.left_wheel_cy),
				res.getDimension(R.dimen.left_wheel_width), res.getDimension(R.dimen.left_wheel_height));
//		left_wheel=Bitmap.createScaledBitmap(left_wheel,left_wheel_rect.width(),left_wheel_rect.height(), true);
		left_wheel=ImageDecodeHelper.decodeImageToSize(getResources(), drawable.gameb_left_wheel, left_wheel_rect.width(), left_wheel_rect.height());
		
		right_wheel_rect=LayoutHelper.getLandscapeLayoutCoordinate(0, 0, wwid, whei,
				res.getDimension(R.dimen.right_wheel_cx), res.getDimension(R.dimen.right_wheel_cy),
				res.getDimension(R.dimen.right_wheel_width), res.getDimension(R.dimen.right_wheel_height));
//		right_wheel=Bitmap.createScaledBitmap(right_wheel,right_wheel_rect.width(),right_wheel_rect.height(), true);
		right_wheel=ImageDecodeHelper.decodeImageToSize(getResources(), drawable.gameb_right_wheel, right_wheel_rect.width(), right_wheel_rect.height());


		left_point_rect=LayoutHelper.getLandscapeLayoutCoordinate(0, 0, wwid, whei,
				res.getDimension(R.dimen.left_point_cx), res.getDimension(R.dimen.left_point_cy),
				res.getDimension(R.dimen.left_point_width), res.getDimension(R.dimen.left_point_height));
//		left_point=Bitmap.createScaledBitmap(left_point, left_point_rect.width(), left_point_rect.height(), true);
		left_point=ImageDecodeHelper.decodeImageToSize(getResources(), drawable.gameb_left_point, left_point_rect.width(), left_point_rect.height());


		right_point_rect=LayoutHelper.getLandscapeLayoutCoordinate(0, 0, wwid, whei,
				res.getDimension(R.dimen.right_point_cx), res.getDimension(R.dimen.right_point_cy),
				res.getDimension(R.dimen.right_point_width), res.getDimension(R.dimen.right_point_height));
//		right_point=Bitmap.createScaledBitmap(right_point, right_point_rect.width(), right_point_rect.height(), true);
		right_point=ImageDecodeHelper.decodeImageToSize(getResources(), drawable.gameb_right_point, right_point_rect.width(), right_point_rect.height());

		wait_paint=new Paint();
		wait_paint.setARGB(120,0,0,0);
		
	}
	void reset(){
		center_rotate_angle=0;
		dest_center_rotate_angle=0;
		t_center_angle=0;
		left_rotate_angle=0;
		right_rotate_angle=-(float)Math.PI/2;

		//setColor(0);
		setupRect();


		this.invalidate();
	}
	void start(){
//		left_rotate_angle=.5f;
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
	   
	 
//	   Log.i("STLayout","wheel view: "+wwid+" x "+whei);
	   
//	   rect_canvas=LayoutHelper.getLayoutCoordinate(0, 0,wwid,whei);
	   
	   setMeasuredDimension(w, h);
	   
	  
	}
	
	@Override
	public void onDraw(Canvas canvas){
		
		super.onDraw(canvas);
		if(this.getWidth()!=wwid || this.getHeight()!=whei) setupRect();
		
		
		
		canvas.drawBitmap(left_wheel,left_wheel_rect.left,left_wheel_rect.top,null);
				
		Matrix matrix_left=new Matrix();
		matrix_left.preTranslate(left_point_rect.left,left_point_rect.top);
		if(wait_mode) matrix_left.preRotate((float)(-68),(int)(left_point_rect.width()*0.857),(int)(left_point_rect.height()*0.75));
		else matrix_left.preRotate((float)(40+80*(left_rotate_angle)),(int)(left_point_rect.width()*0.857),(int)(left_point_rect.height()*0.75));
		
		
		canvas.drawBitmap(left_point,matrix_left,null);
		
		left_rotate_angle+=.08*(Math.random()*2-1);
		if(left_rotate_angle<-1) left_rotate_angle=-1;
		else if(left_rotate_angle>1) left_rotate_angle=1;
		
		
		canvas.drawBitmap(right_wheel,right_wheel_rect.left,right_wheel_rect.top,null);
		
		Matrix matrix_right=new Matrix();
		matrix_right.preTranslate(right_point_rect.left,right_point_rect.top);
		matrix_right.preRotate((float) (-60+40*(right_rotate_angle)),(int)(right_point_rect.width()*0.2273),(int)(right_point_rect.height()*0.6667));
		
		canvas.drawBitmap(right_point, matrix_right,null);
		right_rotate_angle+=.01*(Math.random()*2);
		
		
		
		
		
		if(t_center_angle<1) t_center_angle+=.1;
		Matrix matrix_center=new Matrix();
		matrix_center.preTranslate(wheel_rect.left,wheel_rect.top);
		matrix_center.preRotate(getCurrentCenterAngle(), wheel_rect.width() / 2, wheel_rect.height()/2);
		
		if(center_wheel!=null && !center_wheel.isRecycled()) canvas.drawBitmap(center_wheel,matrix_center,null);
		
		if(wait_mode) 
			canvas.drawRect(0,0,this.getWidth(),this.getHeight(),wait_paint);
	
	}
	public void setWaitMode(boolean set_wait){
		wait_mode=set_wait;
		invalidate();
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
	public void setColor(Integer icolor){

		if(center_wheel!=null) center_wheel.recycle();

		int resid;
		switch(icolor){
			case 1:
//				center_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_red);
				resid=drawable.gameb_red;
				break;
			case 0:
			default:
				//center_wheel=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_blue);
				resid= drawable.gameb_blue;
				break;

		}

		center_wheel= ImageDecodeHelper.decodeImageToSize(getResources(), resid,wheel_rect.width(),wheel_rect.height());

		postInvalidate();
	}
	 
}
