package com.rengatartgital.photonandroidclient.AGame;

import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.drawable;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class IslandView extends View implements AnimatorUpdateListener{
	
	final int MPART=6;
	final int[] ORDER_PART={0,1,3,2,4,5}; 
			
	enum IlandMode {PEOPLE,BUILD,FINAL};
	private IlandMode imode;
	
	String str_name;
	int[] arr_ipart;
	Bitmap[] arr_bmp_part;
	
	
	Bitmap bmp_land;
	Paint paint_bmp,paint_text,paint_stroke,paint_uppest;
	
	Rect rect_build,rect_build_high,rect_choose_people,rect_final_people;
	Rect rect_img_build,rect_img_people;
	
	Handler main_handle;
	
	Bitmap mbmp=null;
	Canvas mcanvas=null;
	Matrix mat_identity;
	
	private ValueAnimator anima_part_in,anima_part_out;
	private int iupp_part;
	private Bitmap bmp_dest_part;
	
	private boolean final_out_finish=false; 
	
	private boolean left_right=true;
	
	private boolean is_changing=false;
	
	Typeface typeface_name;
	
	public IslandView(Context context){
		super(context);
		setup();
	}
	public IslandView(Context context, AttributeSet attrs){
		super(context, attrs);
		setup();
	}
	public IslandView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		setup();
	}
	
	
	
	void setup(){
		
		typeface_name=Typeface.createFromAsset(this.getContext().getAssets(),"fonts/combined.otf");

		
		paint_bmp=new Paint();
		paint_uppest=new Paint();
		
		paint_text=new Paint();
		paint_text.setARGB(255,5,103,177);
		paint_text.setAntiAlias(true);
		paint_text.setTypeface(typeface_name);
		
		paint_stroke= new Paint();
		paint_stroke.setStyle(Style.STROKE);
		paint_stroke.setStrokeWidth(5);
		paint_stroke.setColor(Color.WHITE);
		paint_stroke.setAntiAlias(true);
		paint_stroke.setTypeface(typeface_name);
		
		arr_ipart=new int[MPART];
		arr_bmp_part=new Bitmap[MPART];
		
		imode=IlandMode.PEOPLE;
		
		
		anima_part_in=ValueAnimator.ofFloat(0,1);
		anima_part_in.addUpdateListener(this);
		anima_part_in.setDuration(300);
		
		anima_part_out=ValueAnimator.ofFloat(1,0);
		anima_part_out.addUpdateListener(this);
		anima_part_out.setDuration(300);
	
		bmp_dest_part=null;
		
		
		
		
	}
	
	void setHandler(Handler set_handle){
		main_handle=set_handle;
	}
	
	
		
	public void setupBitmap(int l,int t,int r,int b){
		Bitmap obmp_land=BitmapFactory.decodeResource(getResources(),R.drawable.gamea_build_land);
		bmp_land=Bitmap.createScaledBitmap(obmp_land,(r-l),(int)((float)(r-l)*0.5867),true);
		obmp_land.recycle();
		
		float twid=r-l;
		float thei=b-t;

//		mat_build=new Matrix();
//		mat_build.postTranslate(px,0);
//		mat_build.preScale(pscale,pscale);
//		
//		mat_final_people=new Matrix();
//		mat_final_people.postTranslate((r-l)*.1189f,(b-t)*.72f);
//		mat_final_people.preScale(.87f*pscale,.87f*pscale);
		
		float scale_build=.8f;
		
		rect_build=new Rect();
		rect_build.left=(int)(twid*(.5f-.78f*scale_build/2)); rect_build.top=(int)(thei*(.72f-.7242f*scale_build));
		rect_build.right=(int)(twid*(.5f+.78f*scale_build/2)); rect_build.bottom=(int)(thei*.72f);
		
		
		rect_build_high=new Rect();
		rect_build_high.left=(int)(twid*(.5f-.78f*scale_build/2)); rect_build_high.top=(int)(thei*(.72f-.79f*scale_build));
		rect_build_high.right=(int)(twid*(.5f+.78f*scale_build/2)); rect_build_high.bottom=(int)(thei*.72f);
		
		
		rect_final_people=new Rect();
		rect_final_people.left=(int)(twid*.12); rect_final_people.top=(int)(thei*.72f);
		rect_final_people.right=(int)(twid*.352f); rect_final_people.bottom=(int)(thei*.9945f);
		
		float pscale=.88f;
		
		rect_choose_people=new Rect();
		rect_choose_people.left=(int)(twid*(.5f-.133f*pscale)); rect_choose_people.top=(int)(thei*(.62f-.295f*pscale));
		rect_choose_people.right=(int)(twid*(.5f+.133f*pscale)); rect_choose_people.bottom=(int)(thei*.62f);
		
		paint_text.setTextSize(twid*.12f);
		paint_stroke.setTextSize(twid*.12f);
		
		
		mbmp=Bitmap.createBitmap((int)twid,(int)thei,Bitmap.Config.ARGB_8888);
		mcanvas=new Canvas();
		mcanvas.setBitmap(mbmp);
		mat_identity= new Matrix();
		   
	}
	
	
	void reset(){
		for(int i=0;i<MPART;++i){
			arr_ipart[i]=-1;
			arr_bmp_part[i]=null;
		}
		imode=IlandMode.PEOPLE;
		postInvalidate();
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
		
//		super.onDraw(canvas);
		
		mbmp.eraseColor(android.graphics.Color.TRANSPARENT);
		
		int twid=this.getWidth();
		int thei=this.getHeight();
		
		if(bmp_land!=null) mcanvas.drawBitmap(bmp_land,0,thei*0.4810f,paint_bmp);
		
		// people
		if(arr_bmp_part[MPART-1]!=null){
			if(imode==IlandMode.PEOPLE){
				if(iupp_part==5) mcanvas.drawBitmap(arr_bmp_part[MPART-1],null,rect_choose_people,paint_uppest);
				else mcanvas.drawBitmap(arr_bmp_part[MPART-1],null,rect_choose_people,paint_bmp);
				canvas.drawBitmap(mbmp,mat_identity,paint_bmp);
				return;
			} 			
		}
		
		
		// draw building
		
//		mcanvas.save();
//		mcanvas.scale(.75f,.75f);
		
		for(int x=0;x<MPART-1;++x){
			int i=ORDER_PART[x];
			if(arr_bmp_part[i]!=null){
				
				if(i==2 && arr_ipart[0]==4 && arr_ipart[2]==1){
					if(i==iupp_part) mcanvas.drawBitmap(arr_bmp_part[i],null,rect_build_high,paint_uppest);
					else mcanvas.drawBitmap(arr_bmp_part[i],null,rect_build_high,paint_bmp);
				}else{
					if(i==iupp_part) mcanvas.drawBitmap(arr_bmp_part[i],null,rect_build,paint_uppest);
					else mcanvas.drawBitmap(arr_bmp_part[i],null,rect_build,paint_bmp);
				}
				
			}
		}
//		mcanvas.restore();
		
		
		
		// final people
		if(arr_bmp_part[MPART-1]!=null){				
			if(imode==IlandMode.FINAL && final_out_finish){
				mcanvas.drawBitmap(arr_bmp_part[MPART-1],null,rect_final_people,paint_bmp);				
				
				mcanvas.save();
				mcanvas.translate(twid*.3444f,thei*.95f);
				
				float[] arr_wid=new float[str_name.length()];
				paint_stroke.getTextWidths(str_name,arr_wid);
				
				float total_wid=0;
				for(float wid:arr_wid) total_wid+=wid;
				float tscale=(total_wid<twid*.5f)?1:(twid*.5f/total_wid);
				mcanvas.scale(tscale,2.5f);
				
				if(str_name!=null){
					
					mcanvas.drawText(str_name,0,0,paint_stroke);
					mcanvas.drawText(str_name,0,0,paint_text);
				}
				mcanvas.restore();
			}
		}
		
		canvas.drawBitmap(mbmp,mat_identity,paint_bmp);
		
	}
	public void setSide(int set_left_right){
		left_right=(set_left_right==1);
		
		if(!left_right) paint_text.setARGB(255,239,74,82);
		else paint_text.setARGB(255,5,103,177);
	}
	public void setName(String set_name, int set_people){
		str_name=set_name;
		str_name.toUpperCase();
		
//		updatePart(set_people,5,set_people);
	}
	public void updatePart(int ibuild,int icat,int ipart){
		
		if(is_changing){
			return;
		}
		
		if(icat==5) imode=IlandMode.PEOPLE;
		else imode=IlandMode.BUILD;
		
		
		
		arr_ipart[icat]=ipart;
		String str="";
		for(int i=0;i<6;++i) str+=(arr_ipart[i]+",");
		//Log.i("STLog","update part: "+str);
		
		char cbuild=(char)(97+ibuild);
		char ccat=(char)(97+icat-1);
		
		String res_uri="drawable/gamea_build_";
		if(icat==5) res_uri+=("people_"+(ipart+1));
		else if(icat==0) res_uri+=(cbuild+"_main");
		else res_uri+=(cbuild+"_"+ccat+"_"+(ipart+1));
		
		//Log.i("STLog","part name: "+res_uri);
		
		int res_id=getResources().getIdentifier(res_uri, null, getContext().getPackageName());
		
//		arr_bmp_part[icat]=BitmapFactory.decodeResource(getResources(),res_id);
		
//		postInvalidate();
	
		
		iupp_part=icat;
		bmp_dest_part=BitmapFactory.decodeResource(getResources(),res_id);
		
//		anima_part_out.setDuration(300);
		
		
		is_changing=true;
		anima_part_out.start();
		
		
	}
	
	
	
	public void setIlandMode(IlandMode set_mode){
		
		if(set_mode==IlandMode.FINAL){
//			anima_part_out.setDuration(300);
//			
//			paint_stroke.setAlpha(0);
//			paint_text.setAlpha(0);	
//			
//			postInvalidate();
			
			anima_part_out.start();
			iupp_part=MPART;
			
			final_out_finish=false; 
		}
		imode=set_mode;
		
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event){
//		
//		if(event.getAction()==MotionEvent.ACTION_DOWN){
//			
//			if(imode==IlandMode.FINAL){
//				// send trigger
//				if(main_handle!=null){
//					Message msg=Message.obtain(main_handle,100,103,0,null);
//					main_handle.sendMessage(msg);
//				}
//			}
//			return true;
//		}
//		
//		return false;
//	}
	@Override
	public void onAnimationUpdate(ValueAnimator animator){
		
		float _val=(Float)animator.getAnimatedValue();
		if(imode==IlandMode.FINAL){
			
//			if(final_out_finish){
				paint_stroke.setAlpha((int)(_val*255));
				paint_text.setAlpha((int)(_val*255));				
//			}
			paint_bmp.setAlpha((int)(_val*255));
			
		}else paint_uppest.setAlpha((int)(_val*255));
		
		
		if(animator.equals(anima_part_in)){
			if(animator.getAnimatedFraction()==1){
				is_changing=false;
			}
		}else if(animator.equals(anima_part_out)){
			if(animator.getAnimatedFraction()==1){
				
				
				if(imode==IlandMode.FINAL){
					anima_part_in.setDuration(300);
					final_out_finish=true; 
				}else{
					anima_part_in.setDuration(300);
					arr_bmp_part[iupp_part]=bmp_dest_part;					
				}
				
				anima_part_in.start();
				
			}	
		}
		postInvalidate();
		
	}
	
	public boolean animationFinished(){
		return !is_changing;
	}
	
}
