package com.rengatartgital.photonandroidclient.BGame;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.rengatartgital.photonandroidclient.GameEventCode;
import com.rengatartgital.photonandroidclient.MainActivity;
import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.anim;
import com.rengatartgital.photonandroidclient.R.dimen;
import com.rengatartgital.photonandroidclient.R.drawable;
import com.rengatartgital.photonandroidclient.R.layout;
import com.rengatartgital.photonandroidclient.ViewUtil.BaseGameView;
import com.rengatartgital.photonandroidclient.ViewUtil.FinishImageView;
import com.rengatartgital.photonandroidclient.ViewUtil.ImageDecodeHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.LayoutHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.AutoResizeTextView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.exitgames.client.photon.TypedHashMap;

public class GameBView extends BaseGameView {
	
	final float BEAT_STRENGTH_THRESHOLD=3.0f;
	
	private enum GameState {None,GameB_Wait,GameB_Ready,Rotate,Win_Lose,GameB_End};
	GameState game_state;
	WheelView wheel_view;
	ImageView img_wait,img_win,img_lose,img_back,img_start;
		
	FinishImageView img_finish;
	Button button_home;
	BCountDownView countdown_view;
	
	
	private ValueAnimator winlose_animator;
	
	private Animation blink_animation;
	
	private int last_car_index;
	private int last_win;
	
	private int last_wheel_pos;
	private Timer timer_engine_rate;
	
	private Handler main_handle;
	
	public GameBView(Context context) {
		super(context);
		setupView(context);
	}
	public GameBView(Context context, AttributeSet attrs) {
		super(context,attrs);
		setupView(context);
	}
	public GameBView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setupView(context);
	}
	private void setupView(Context context){
		LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.game_b_layout,this,true);
		
		
		
		img_back=(ImageView)getChildAt(0);
		wheel_view=(WheelView)getChildAt(1);
		
		img_wait=(ImageView)getChildAt(2);
		countdown_view=(BCountDownView)getChildAt(3);
		
		img_start=(ImageView)getChildAt(4);
		
		img_win=(ImageView)getChildAt(5);
		img_lose=(ImageView)getChildAt(6);
		img_finish=(FinishImageView)getChildAt(7);
		button_home=(Button)getChildAt(8);
		
		guide_view=(AutoResizeTextView)getChildAt(9);
		setupGuideText();
		
		button_home.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				switch(game_state){						
					case GameB_End:
						Message msg=Message.obtain(main_activity.handler,100,101,0,null);
				        main_activity.handler.sendMessage(msg);
						break;
					default:
						break;
				}
				main_activity.playButtonSound();
			}
		});
		
		img_wait.setVisibility(View.VISIBLE);
		img_win.setVisibility(View.INVISIBLE);
		img_lose.setVisibility(View.INVISIBLE);
		
		winlose_animator = ValueAnimator.ofFloat(0,255);
		winlose_animator.setDuration(50);
        winlose_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        winlose_animator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0){
				// show finish view
				updateGameState(GameState.GameB_End);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
        	
        });
        
        blink_animation=AnimationUtils.loadAnimation(this.getContext(),R.anim.blink_ani);
        
	}
	@Override public void setMainActivity(MainActivity main_act){
		super.setMainActivity(main_act);
		main_handle=main_act.handler;
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		
		
		if(!changed) return;
		
		Resources res=getResources();
		
		Rect full_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b);	
		if(img_finish.getVisibility()==View.VISIBLE) full_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b);
		
		if(main_activity.EnableLog) Log.i("STLayout","gameb: "+l+" "+t+" "+r+" "+b);
		if(main_activity.EnableLog) Log.i("STLayout","gameb full "+full_rect.left+" "+full_rect.top+" "+full_rect.right+" "+full_rect.bottom);
		
		img_back.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
//		Rect wheel_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,50,50,100,100);	
//		wheel_view.layout(wheel_rect.left,wheel_rect.top,wheel_rect.right,wheel_rect.bottom);
		wheel_view.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
		guide_view.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		guide_view.setTextSize(Math.max(full_rect.height()/12,res.getDimension(R.dimen.MIN_TEXT_SIZE)));
		
		
		Rect wait_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.wait_cx),res.getDimension(R.dimen.wait_cy),
				res.getDimension(R.dimen.wait_width),res.getDimension(R.dimen.wait_height));
		img_wait.layout(wait_rect.left,wait_rect.top,wait_rect.right,wait_rect.bottom);
		img_start.layout(wait_rect.left,wait_rect.top,wait_rect.right,wait_rect.bottom);
		
		
		Rect waitcount_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.bwaitcount_cx),res.getDimension(R.dimen.bwaitcount_cy),
				res.getDimension(R.dimen.bwaitcount_width),res.getDimension(R.dimen.bwaitcount_height));
		countdown_view.layout(waitcount_rect.left,waitcount_rect.top,waitcount_rect.right,waitcount_rect.bottom);
		countdown_view.setupBitmap(waitcount_rect.left,waitcount_rect.top,waitcount_rect.right,waitcount_rect.bottom);
		
		Rect win_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.win_cx),res.getDimension(R.dimen.win_cy),
				res.getDimension(R.dimen.win_width),res.getDimension(R.dimen.win_height));
		img_win.layout(win_rect.left,win_rect.top,win_rect.right,win_rect.bottom);
		img_lose.layout(win_rect.left,win_rect.top,win_rect.right,win_rect.bottom);
		
		
		Rect finish_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b);
		img_finish.layout(finish_rect.left,finish_rect.top,finish_rect.right,finish_rect.bottom);
		Log.i("STLayout","gameb finish "+finish_rect.left+" "+finish_rect.top+" "+finish_rect.right+" "+finish_rect.bottom);
		
		
		Rect home_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.home_cx),res.getDimension(R.dimen.home_cy),
				res.getDimension(R.dimen.home_width),res.getDimension(R.dimen.home_height));
		button_home.layout(home_rect.left,home_rect.top,home_rect.right,home_rect.bottom);
		
	}
//	@Override
//	protected void dispatchDraw(Canvas canvas){
//		super.dispatchDraw(canvas);
//		
//		
//		   
//	}
	
	private void showFinishView(){

		
		Message msg=Message.obtain(main_activity.handler,100,102,0,null);
		main_activity.handler.sendMessage(msg);
		
		
		Bitmap car_bmp=createCarBitmap(last_car_index,(int)(this.getWidth()*.8),(int)(this.getWidth()*.8));
		img_finish.setup(1,car_bmp,main_activity.handler);
		img_finish.setVisibility(View.VISIBLE);
		
		button_home.setVisibility(View.VISIBLE);
		
		invalidate();
		//img_finish.invalidate(); 
	}
	
	private void setWaitMode(boolean set_wait){
		wheel_view.setWaitMode(set_wait);
	}
	
	@Override
	public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte, Object> params){

		switch(action_code){
			case Server_GameB_Ready:
				int iside=(Integer)params.get((byte)101);
				main_activity.setSideIndex(iside);
				wheel_view.setColor(iside);

				updateGameState(GameState.GameB_Ready);
				break;
			case Server_GameB_Start:
				updateGameState(GameState.Rotate);
				break;
			case Server_GameB_Eat:
				int eat_type=(Integer)params.get((byte)1);
				if(eat_type==1) main_activity.playSound(15);
				else if(eat_type==2) main_activity.playSound(14);
					
				break;
			case Server_GG:
				last_car_index=(Integer)params.get((byte)3);
				last_win=(Integer)params.get((byte)1);		
				
				updateGameState(GameState.Win_Lose);
				
				break;
				
			default:
				break;
		}
	}

	
	public void updateGameState(GameState set_state){
		game_state=set_state;
		
		int mchild=this.getChildCount();
		for(int i=0;i<mchild;++i) this.getChildAt(i).setVisibility(View.INVISIBLE);
		
		if(!read_guide) guide_view.setVisibility(View.VISIBLE);
		
		switch(game_state){
			case GameB_Wait:
			
				
				img_back.setVisibility(View.VISIBLE);
				img_wait.setVisibility(View.VISIBLE);
				countdown_view.setVisibility(View.VISIBLE);
				
				countdown_view.start();
				
				wheel_view.setVisibility(View.VISIBLE);
				setWaitMode(true);
//				main_activity.playSound(6);
				break;
			case GameB_Ready:
				img_back.setVisibility(View.VISIBLE);
				img_start.setVisibility(View.VISIBLE);
				img_start.startAnimation(blink_animation);
				
				wheel_view.setVisibility(View.VISIBLE);
				setWaitMode(true);
				
				main_activity.playSound(6);
				
				break;
			case Rotate:
				this.main_activity.playSound(4);
				this.main_activity.startEngineSound();
				
				img_back.setVisibility(View.VISIBLE);
				img_start.clearAnimation();
//				img_start.setVisibility(View.VISIBLE);
				wheel_view.setVisibility(View.VISIBLE);
				wheel_view.start();
				setWaitMode(false);
				

				break;
			case Win_Lose:
				
				this.main_activity.stopEngineSound();
				
				img_back.setVisibility(View.VISIBLE);
				setWaitMode(true);
				wheel_view.setVisibility(View.VISIBLE);
				
				this.main_activity.playSound(5);
				
				if(last_win==1){
					img_win.setVisibility(View.VISIBLE);
//					img_win.startAnimation(blink_animation);
				}else{
					img_lose.setVisibility(View.VISIBLE);
//					img_lose.startAnimation(blink_animation);
				}
				
				winlose_animator.start();
				
				break;
			case GameB_End:
				showFinishView();
				//End();
				break;
			default:
				break;
		}
	}

	
	@Override
	public void Init(){
		super.Init();
		updateGameState(GameState.GameB_Wait);
		winlose_animator.cancel();
		wheel_view.reset();
		//wheel_view.setColor(main_activity.side_index);
		
		last_wheel_pos=0;
		
		
		
	}
	@Override
	public void End(){
		super.End();
		//updateGameState(GameState.None);
		game_state=GameState.None;
		
		img_win.clearAnimation();
		img_lose.clearAnimation();

		if(img_finish!=null) img_finish.clear();
		if(wheel_view!=null) wheel_view.clear();

	}
	@Override
	public void HandleSensor(float[] sensor_value){
		
		if(game_state!=GameState.Rotate) return;


		final int axisSwap[][] = {
				{  1,  -1,  0,  1  },     // ROTATION_0
				{-1,  -1,  1,  0  },     // ROTATION_90
				{-1,    1,  0,  1  },     // ROTATION_180
				{  1,    1,  1,  0  }  }; // ROTATION_270


		double delta_strength=sensor_value[0];

		int new_wheel_pos=0;
		if(delta_strength>BEAT_STRENGTH_THRESHOLD) new_wheel_pos=-1;
		else if(delta_strength<-BEAT_STRENGTH_THRESHOLD) new_wheel_pos=1;
		
		if(new_wheel_pos!=last_wheel_pos){
			
			HashMap<Object,Object> params=new HashMap<Object,Object>();

			params.put((byte)1,new_wheel_pos);
			main_activity.sendEvent(GameEventCode.Game_B_Rotate,params,false);
			
//			wheel_view.setRotateAngle((float)(-delta_strength/9.8*90));

			
			last_wheel_pos=new_wheel_pos;
			
			changeEngineSound();
		}
		
		
		if(Math.abs(delta_strength)>BEAT_STRENGTH_THRESHOLD){
				
//			HashMap<Object,Object> params=new HashMap<Object,Object>();
//
//			params.put((byte)1,(delta_strength>0)?-1:1);
//			main_activity.sendEvent(GameEventCode.Game_B_Rotate,params);
			
			wheel_view.setRotateAngle((float)(-delta_strength/9.8*90));
			
			
		}else{
			wheel_view.setRotateAngle(0);
		}

	}
	
	
	public void setWaitingIndex(int set_index,int waiting_state){
		if(main_activity.EnableLog) Log.i("STLog","waiting: "+set_index);
		
	}
	private Bitmap createCarBitmap(int index_car,int width,int height){
		

    	Bitmap oimg_avatar_bmp=null;
		int car_resid;
		switch(index_car){
			case 0: car_resid=R.drawable.gameb_car_1; break;
			case 1: car_resid=R.drawable.gameb_car_2; break;
			case 2: car_resid=R.drawable.gameb_car_3; break;
			case 3: car_resid=R.drawable.gameb_car_4; break;
			case 4: car_resid=R.drawable.gameb_car_5; break;
			case 5: car_resid=R.drawable.gameb_car_6; break;
			case 6: car_resid=R.drawable.gameb_car_7; break;
			case 7: car_resid=R.drawable.gameb_car_8; break;
			case 8: car_resid=R.drawable.gameb_car_9; break;
			case 9: car_resid=R.drawable.gameb_car_10; break;
			default:car_resid=R.drawable.gameb_car_9; break;
		}
		Bitmap scale_bmp= ImageDecodeHelper.decodeImageToSize(getResources(),car_resid,width,height);
		
		return scale_bmp;
	}
	@Override
	public boolean isFinish(){
		return game_state==GameState.GameB_End;
	}
	public boolean isEngine(){
		return game_state==GameState.Rotate;
	}
	
	private void changeEngineSound(){
		
//		main_activity.setEngineSoundRate(5);
		
		Message msg=Message.obtain(main_handle,100,120,0,null);
		main_handle.sendMessage(msg);
	}
	
}
