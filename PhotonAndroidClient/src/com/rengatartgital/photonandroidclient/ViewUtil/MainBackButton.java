package com.rengatartgital.photonandroidclient.ViewUtil;

import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.drawable;
import com.rengatartgital.photonandroidclient.R.styleable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class MainBackButton extends View{

	private Drawable mFrontDrawable;
	private Drawable mBackDrawable;
	boolean is_enabled;
	
	private boolean is_touched;
	private Drawable start_drawable,start_drawable_triggered;
	
	private Rect back_bound,front_bound,text_bound;

	private int cur_display_width,cur_display_height;
	
	Paint disable_paint;
	
	public MainBackButton(Context context) {
		super(context);
		init(null, 0);
	}

	public MainBackButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public MainBackButton(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.MainBackButton, defStyle, 0);


		if (a.hasValue(R.styleable.MainBackButton_frontdrawable)){
			mFrontDrawable=a.getDrawable(R.styleable.MainBackButton_frontdrawable);
			mFrontDrawable.setCallback(this);
		}
		if (a.hasValue(R.styleable.MainBackButton_backdrawable)){
			mBackDrawable=a.getDrawable(R.styleable.MainBackButton_backdrawable);
			mBackDrawable.setCallback(this);
		}

		a.recycle();

		start_drawable=getResources().getDrawable(R.drawable.game_start_1);
		//Log.i("STLog","LOAD TEXT "+start_drawable.getIntrinsicWidth()+" "+start_drawable.getIntrinsicHeight());
		start_drawable_triggered=getResources().getDrawable(R.drawable.game_start_2);
		
		is_enabled=false;
		is_touched=false;
		
		back_bound=new Rect();
		front_bound=new Rect();
		text_bound=new Rect();
		
		
		disable_paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		disable_paint.setColor(0xaa000000);
		
		calculateImageBound();
	}

	private void calculateImageBound(){
		// TODO: consider storing these as member variables to reduce
		// allocations per draw cycle.
		
		
		
		int paddingTop = getPaddingTop();
		int paddingLeft = getPaddingLeft();
		int paddingBottom = getPaddingBottom();
		int paddingRight=getPaddingRight();
		
		int contentHeight=cur_display_height-paddingTop-paddingBottom;
		int contentWidth=cur_display_width-paddingLeft-paddingRight;
		
		
		int back_height=contentHeight;
		int back_width=contentWidth;
		
		float tmp_ratio=(float)contentWidth/(float)contentHeight;
		if(tmp_ratio>1.6875) back_height=(int)((float)contentWidth/1.6875);
		else if(tmp_ratio<1.6875) back_width=(int)((float)contentHeight*1.6875);
		
		int back_left=paddingLeft+contentWidth/2-back_width/2;
		int back_top=paddingTop+contentHeight/2-back_height/2;
		
		//Log.i("STLog","back: "+contentWidth+"  "+back_width+" "+paddingLeft);
		
		back_bound.set(back_left, back_top, back_left+back_width,back_top+back_height);
		mBackDrawable.setBounds(back_bound);
		//Log.i("STLog","back= "+back_bound);
		
		int front_height=(int)((float)contentHeight*0.19);
		int front_width=front_height*(mFrontDrawable.getIntrinsicWidth()/mFrontDrawable.getIntrinsicHeight());
		
		int front_top=(int)(paddingTop+(float)contentHeight*.95-front_height);
		int front_left=paddingLeft+contentWidth/2-front_width/2;
		
		front_bound.set(front_left, front_top, front_left+front_width,front_top+front_height);
		mFrontDrawable.setBounds(front_bound);
		
		int text_height=(int)((float)contentHeight*0.3875);
		int text_width=(int)((float)text_height*3.629);
		
		int text_top=(int)(paddingTop+(float)contentHeight*.5-text_height/2);
		int text_left=paddingLeft+contentWidth/2-text_width/2;
		
		
		text_bound.set(text_left,text_top,text_left+text_width,text_top+text_height);
		
		//Log.i("STLog","text= "+text_bound);
		
		start_drawable.setBounds(text_bound);
		start_drawable_triggered.setBounds(text_bound);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int new_display_width=getWidth();
		int new_display_height=getHeight();
		if(new_display_width!=cur_display_width || new_display_height!=cur_display_width){
		  cur_display_width=new_display_width; 
		  cur_display_height=new_display_height;
		  calculateImageBound();
		}
		
		// calculateImageBound();
		// Draw the example drawable on top of the text.
		if(mBackDrawable!=null){
			
			mBackDrawable.draw(canvas);
		}
		if(mFrontDrawable!=null){
			mFrontDrawable.draw(canvas);
		}
		
		//start_drawable.setBounds(text_bound);
		//start_drawable.draw(canvas);
		
		if(is_enabled){			
			if(is_touched) start_drawable_triggered.draw(canvas);
			else start_drawable.draw(canvas);
		}else{
			//start_drawable_triggered.draw(canvas);
			canvas.drawRect(back_bound,disable_paint);
		}
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	   // Try for a width based on our minimum
//	   int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//	   int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
//
//	   // Whatever the width ends up being, ask for a height that would let the pie
//	   // get as big as it can
//	   int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
//	   int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)mTextWidth, heightMeasureSpec, 0);
//
		
	  
	   setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	 
		
	}	
	
	 @Override
	 public boolean onTouchEvent(MotionEvent event){
		 //Log.i("STLog","touch event!");
		 is_touched=true;
		 invalidate(); 
	    return super.onTouchEvent(event);
	 }
	 
	 @Override
	 public void setEnabled(boolean set_enable){
		 super.setEnabled(set_enable);
		 is_enabled=set_enable;
		 is_touched=false;
		 invalidate(); 
	 }
	
}
