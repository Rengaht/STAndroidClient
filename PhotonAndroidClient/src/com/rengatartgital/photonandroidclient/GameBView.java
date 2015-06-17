package com.rengatartgital.photonandroidclient;

import java.util.HashMap;

import com.rengatartgital.photonandroidclient.R;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.exitgames.client.photon.TypedHashMap;

public class GameBView extends BaseGameView {
	
	final float BEAT_STRENGTH_THRESHOLD=3.0f;
	
	private enum GameState {GameB_Wait,Rotate,Win_Lose,GameB_End};
	GameState game_state;
	WheelView wheel_view;
	ImageView img_wait,img_win,img_lose,img_back,img_start,img_noway;
		
	FinishImageView img_finish;
	Button button_home;
	
	private ValueAnimator winlose_animator;
	
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
		img_noway=(ImageView)getChildAt(3);
		img_start=(ImageView)getChildAt(4);
		
		img_win=(ImageView)getChildAt(5);
		img_lose=(ImageView)getChildAt(6);
		img_finish=(FinishImageView)getChildAt(7);
		button_home=(Button)getChildAt(8);
		
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
        
        
        
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		
		
		if(!changed) return;
		
		Resources res=getResources();
		
		Rect full_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b);	
		if(img_finish.getVisibility()==View.VISIBLE) full_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b);
		
		Log.i("STLayout","gameb: "+l+" "+t+" "+r+" "+b);
		Log.i("STLayout","gameb full "+full_rect.left+" "+full_rect.top+" "+full_rect.right+" "+full_rect.bottom);
		
		img_back.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
//		Rect wheel_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,50,50,100,100);	
//		wheel_view.layout(wheel_rect.left,wheel_rect.top,wheel_rect.right,wheel_rect.bottom);
		wheel_view.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
		Rect wait_rect=LayoutHelper.getLandscapeLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.wait_cx),res.getDimension(R.dimen.wait_cy),
				res.getDimension(R.dimen.wait_width),res.getDimension(R.dimen.wait_height));
		img_wait.layout(wait_rect.left,wait_rect.top,wait_rect.right,wait_rect.bottom);
		
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
		
		
		
		img_finish.setup(1,null,main_activity.handler);
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
			case Server_GameB_Start:
				main_activity.setSideIndex((Integer)params.get((byte)101));
				updateGameState(GameState.Rotate);
				break;
			case Server_GG:
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
		
	
		
		switch(game_state){
			case GameB_Wait:
				img_back.setVisibility(View.VISIBLE);
				img_wait.setVisibility(View.VISIBLE);
				wheel_view.setVisibility(View.VISIBLE);
				setWaitMode(true);
				break;
			case Rotate:
				img_back.setVisibility(View.VISIBLE);
				img_start.setVisibility(View.VISIBLE);
				wheel_view.setVisibility(View.VISIBLE);
				setWaitMode(false);
				
				break;
			case Win_Lose:
				img_back.setVisibility(View.VISIBLE);
				setWaitMode(true);
				wheel_view.setVisibility(View.VISIBLE);
				img_win.setVisibility(View.VISIBLE);
				winlose_animator.start();
				
				break;
			case GameB_End:
				showFinishView();
				End();
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
	}
	@Override
	public void End(){
		super.End();
//		main_activity.initGame(-1);
	}
	@Override
	public void HandleSensor(float[] sensor_value){
		
		if(game_state!=GameState.Rotate) return;
		
	
		double delta_strength=sensor_value[0];
		if(Math.abs(delta_strength)>BEAT_STRENGTH_THRESHOLD){
				
			HashMap<Object,Object> params=new HashMap<Object,Object>();

			params.put((byte)1,(delta_strength>0)?-1:1);
			main_activity.sendEvent(GameEventCode.Game_B_Rotate,params);
			
			wheel_view.setRotateAngle((float)(-delta_strength/9.8*90));
		}else{
			wheel_view.setRotateAngle(0);
		}

	}
	
	
	public void setWaitingIndex(int set_index,int waiting_state){
		Log.i("STLog","waiting: "+set_index);
		
	}
	
}
