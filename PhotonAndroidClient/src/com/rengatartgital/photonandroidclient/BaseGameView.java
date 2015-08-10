package com.rengatartgital.photonandroidclient;


import de.exitgames.client.photon.TypedHashMap;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseGameView extends FrameLayout{
	
	MainActivity main_activity;
	
	protected TextView guide_view; 
	private String guide_string;
	protected boolean read_guide;
	
	public BaseGameView(Context context){
		super(context);
	}
	public BaseGameView(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public BaseGameView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		
	}
	public void setMainActivity(MainActivity main_act){
		main_activity=main_act;
	}
	
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		 int childCount=getChildCount();        
		 int tmp_position_y=50;
		 
		 for(int i=0;i<childCount;i++){
	        View childView = getChildAt(i);
	        
//	        FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)childView.getLayoutParams();
//	        Log.i("STLog","child at "+i+" : "+params.topMargin+" , "+params.rightMargin);
//	        
//	        Log.i("STLog","child at "+i+" : "+childView.getLeft()+" , "+childView.getTop()+" , "+childView.getRight()+" , "+childView.getBottom()
//	        		+childView.getMeasuredWidth()+" , "+childView.getMeasuredHeight());
	        
	        childView.layout(0,tmp_position_y,childView.getMeasuredWidth(),tmp_position_y+childView.getMeasuredHeight());
	        tmp_position_y+=childView.getMeasuredHeight();
	        
		 }
	}
	public void Init(){
//		if(guide_view==null){
//			guide_view=new TextView(getContext());
//			if(guide_string!=null) guide_view.setText(guide_string);
//			guide_view.setTextColor(0xFFFFFFFF);
//			guide_view.setBackgroundColor(0xAA000000);
//			guide_view.setTextSize(Math.min(getWidth()/12,getResources().getDimension(R.dimen.MIN_TEXT_SIZE)));
//			guide_view.setOnClickListener(new OnClickListener(){
//				@Override
//				public void onClick(View arg0){
//					guide_view.setVisibility(View.INVISIBLE);
//				}
//			});
//			this.addView(guide_view);
//			FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(getWidth(),getHeight());
//			guide_view.setLayoutParams(params);
//						
//		}
		guide_view.setVisibility(View.VISIBLE);
		read_guide=false;
		
	}
	public void End(){
	
	}
	abstract public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte,Object> params);
	abstract public void HandleSensor(float[] sensor_value);
	
	abstract public boolean isFinish();
	
	public void setupGuideText(){
		
		if(guide_view!=null){
			
			guide_view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					guide_view.setVisibility(View.INVISIBLE);
					read_guide=true;
				}
				
			});
		}
	}
	
	
//	Rect getLayoutCoordinate(int pleft,int ptop,int pright,int pbottom){
//		
//		int wwid=pright-pleft;
//		int whei=pbottom-ptop;
//		float wratio=(float)wwid/(float)whei;
//		
//		float cur_wid=wwid;
//		float cur_hei=whei;
//		
//		if(wratio>0.5625) cur_hei=(int)(cur_wid/0.5625);
//		else cur_wid=(int)(cur_hei*0.5625);
//		
//		
//		float nleft=pleft+(wwid-cur_wid)/2;
//		float nright=nleft+cur_wid;
//		
//		float ntop=ptop+(whei-cur_hei)/2;
//		float nbottom=ntop+cur_hei;
//		
//		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
//		
//	}
//	Rect getLayoutCoordinate(int pleft,int ptop,int pright,int pbottom,float lx,float ly,float lwidth,float lheight){
//		
//		float wwid=pright-pleft;
//		float whei=pbottom-ptop;
//		
//		float cur_scale=(float)wwid/1080.0f;
//		boolean y_align=false;
//		if((float)whei/1920.0f<cur_scale){
//			cur_scale=(float)whei/1920.0f;
//			y_align=true;
//			//wwid=(int)(whei/1.778f);
//		}else{
//			//whei=(int)(wwid*1.778f);
//		}
//		
//		float nwidth=lwidth/100*(y_align?whei/1.778f:wwid);
//		float nleft=pleft+(lx/100)*wwid-nwidth/2.0f;
//		float nright=nleft+nwidth;
//		
//		float nheight=lheight/100*(y_align?whei:wwid*1.778f);
//		float ntop=ptop+(ly/100)*whei-nheight/2.0f;
//		float nbottom=ntop+nheight;
//		
//		Log.i("STLayout",pleft+" "+ptop+" "+pright+" "+pbottom+" -> "+lx+" "+ly+" "+lwidth+" "+lheight);
//		Log.i("STLayout"," => "+nleft+" "+ntop+" "+nright+" "+nbottom);
//		
//		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
//		
//	}
}
