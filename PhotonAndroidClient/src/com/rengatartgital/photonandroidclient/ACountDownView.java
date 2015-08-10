package com.rengatartgital.photonandroidclient;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Shader.TileMode;

public class ACountDownView extends View implements AnimatorUpdateListener{
	final int COUNT_TIME=120000;
	private int time_to_count;
	private ValueAnimator anima_timer;
	
	private Paint paint_circle,paint_shadow;
	private RectF rect_circle;
	private float cur_ang;
	
	private Handler main_handle;
	
	public ACountDownView(Context context){
		super(context);
		setup(COUNT_TIME);
	}

	public ACountDownView(Context context, AttributeSet attrs){
		super(context, attrs);
		setup(COUNT_TIME);
	}

	public ACountDownView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		setup(COUNT_TIME);
	}
	void setup(int set_time){
		
		time_to_count=set_time;
		
		anima_timer=ValueAnimator.ofFloat(0,360);
		anima_timer.addUpdateListener(this);
		anima_timer.setDuration(time_to_count);
		anima_timer.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0){
				Message msg=Message.obtain(main_handle,100,104,0,null);
				main_handle.sendMessage(msg);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
		
		});
		
		
		paint_circle=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint_circle.setARGB(255,255,255,0);
		paint_circle.setStyle(Paint.Style.FILL);
//		paint_circle.setShadowLayer(8,-5,5,0xffc1c1c1);
		
		paint_shadow=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint_shadow.setARGB(255,193,193,193);
		paint_shadow.setStyle(Paint.Style.FILL);
		
		// Important for certain APIs 
        setLayerType(LAYER_TYPE_SOFTWARE, paint_circle);
		
	}
	void setHandler(Handler set_handle){
		main_handle=set_handle;
	}
	void start(){
		cur_ang=0;
		anima_timer.start();
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
	public void layout(int l,int t,int r,int b){
		super.layout(l, t, r, b);
		
		float cw=r-l;
		
		rect_circle=new RectF();
		rect_circle.left=-(int)(cw*.4f);
		rect_circle.right=(int)(cw*.4f);
		rect_circle.top=-(int)(cw*.4f);
		rect_circle.bottom=(int)(cw*.4f);
		
	}
	
	
	
	@Override
	public void onDraw(Canvas canvas){
		
		super.onDraw(canvas);
		
		float twid=this.getWidth();
		float thei=this.getHeight();
//		canvas.drawRect(0, 0, twid, thei, paint_circle);
		
		canvas.translate(twid/2, thei/2);
		canvas.rotate(-90);		
		
		canvas.translate(-twid*.02f, thei*.02f);
		canvas.drawArc(rect_circle,cur_ang,360-cur_ang,true,paint_shadow);
		
		canvas.translate(twid*.02f, -thei*.02f);
		canvas.drawArc(rect_circle,cur_ang,360-cur_ang,true,paint_circle);
		
		
	}
//	
	@Override
	public void onAnimationUpdate(ValueAnimator animator){
		
		cur_ang=(Float)animator.getAnimatedValue();
		
		
		postInvalidate();
	}

}
