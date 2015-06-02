package com.rengatartgital.photonandroidclient;

import java.util.HashMap;

import com.rengatartgital.photonandroidclient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.exitgames.client.photon.TypedHashMap;

public class GameBView extends BaseGameView {
	
	final float BEAT_STRENGTH_THRESHOLD=3.0f;
	
	private enum GameState {GameB_Wait,Rotate,GameB_End};
	GameState game_state;
	WheelView wheel_view;
	TextView waiting_text;
	
	
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
		
		wheel_view=(WheelView)getChildAt(2);
		game_over_view=(ImageView)getChildAt(0);
		waiting_text=(TextView)getChildAt(1);
		
	}

	@Override
	public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte, Object> params){

		switch(action_code){
			case Server_GameB_Start:
				main_activity.setSideIndex((Integer)params.get((byte)101));
				updateGameState(GameState.Rotate);
				break;
			case Server_GG:
				updateGameState(GameState.GameB_End);
				break;
			default:
				break;
		}
	}

	
	public void updateGameState(GameState set_state){
		game_state=set_state;
		switch(game_state){
			case GameB_Wait:
			case Rotate:
				break;
			case GameB_End:
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

			params.put((byte)1,(delta_strength>0)?1:-1);
			main_activity.sendEvent(GameEventCode.Game_B_Rotate,params);
			
			wheel_view.setRotateAngle((float)(-delta_strength/9.8*90));
		}else{
			wheel_view.setRotateAngle(0);
		}

	}
	
	
	public void setWaitingIndex(int set_index,int waiting_state){
		waiting_text.setText("waiting: "+set_index);
		
	}
	
}
