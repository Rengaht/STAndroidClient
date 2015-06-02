package com.rengatartgital.photonandroidclient;

import java.util.HashMap;






import com.rengatartgital.photonandroidclient.R;

import de.exitgames.client.photon.TypedHashMap;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class GameAView extends BaseGameView{
	
	private static final int POLL_INTERVAL=300;
	
	
	private Button send_button,left_side_button,right_side_button;
	private EditText name_text;
	
	private enum GameState {SetSide,SetName,Blow,GameA_End};
	GameState game_state;
	
	
	BlowSensor blow_sensor;
	private Handler blow_handler=new Handler();
	private Runnable pollTask=new Runnable(){
        public void run(){
                
                if(blow_sensor.isBlow()) updateBlowStatus();
                
                // Runnable(mPollTask) will again execute after POLL_INTERVAL
                blow_handler.postDelayed(pollTask, POLL_INTERVAL);
                
        }
	};

	public GameAView(Context context) {
		super(context);
		setupView(context);
	}
	public GameAView(Context context, AttributeSet attrs) {
		super(context,attrs);
		setupView(context);
	}
	public GameAView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setupView(context);
	}
	
	
	
	private void setupView(Context context){
		LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.game_a_layout,this,true);
		
		name_text=(EditText)getChildAt(0);
		send_button=(Button)getChildAt(1);
		left_side_button=(Button)getChildAt(2);
		right_side_button=(Button)getChildAt(3);
		
		game_over_view=(ImageView)getChildAt(4);
		
		 send_button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0){
					HashMap<Object,Object> params=new HashMap<Object,Object>();
					
					switch(game_state){						
						
						case SetName:
							params.put((byte)1, name_text.getText().toString());
							main_activity.sendEvent(GameEventCode.Game_A_Name,params);
							break;					
						case Blow:
							main_activity.sendEvent(GameEventCode.Game_A_Blow,params);
							break;						
						default:
							break;
					}
					
				}        	
	        });
		 
		 left_side_button.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 HashMap<Object,Object> params=new HashMap<Object,Object>();
				 params.put((byte)101, 1);
				 main_activity.sendEvent(GameEventCode.Game_A_Side,params);
			
			 }
		 });
		 

		 right_side_button.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 HashMap<Object,Object> params=new HashMap<Object,Object>();
				 params.put((byte)101, 0);
				 main_activity.sendEvent(GameEventCode.Game_A_Side,params);
			
			 }
		 });
		 
		 
		 	name_text.setEnabled(false);
		 	send_button.setEnabled(false);
		 	left_side_button.setEnabled(false);
		 	right_side_button.setEnabled(false);
		 	
	}

	@Override
	public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte,Object> params){
		switch(action_code){
			case Server_Name_Success:
				if((Integer)params.get((byte)1)==1){
					Log.i("STLog","Send Name Success");
					updateGameState(GameState.Blow);
				}
				break;
			case Server_GG:
				Log.i("STLog","Game A End");
				updateGameState(GameState.GameA_End);
				break;
			case Server_Set_Side_Success:
				if((Integer)params.get((byte)1)==1){
					updateGameState(GameState.SetName);
					main_activity.setSideIndex((Integer)params.get((byte)101));
				}
				break;
			default:
				break;
		
		}
	}

	
	public void updateGameState(GameState set_state){
		
		game_state=set_state;
		
		
		name_text.setEnabled(false);
		send_button.setEnabled(false);
		left_side_button.setEnabled(false);
		right_side_button.setEnabled(false);
		
		switch(set_state){
			case SetSide:
				left_side_button.setEnabled(true);
				right_side_button.setEnabled(true);
				break;
			case SetName:
				name_text.setEnabled(true);
				name_text.setText("");
				send_button.setEnabled(true);
				break;
			case Blow:
				send_button.setEnabled(true);
				startBlowSensor();
				break;
			case GameA_End:
				End();
				break;
			default:
				break;
		}
		
	}



	@Override
	public void Init() {
		super.Init();
		updateGameState(GameState.SetSide);
		
		
	
	}
	@Override
	public void End() {
		super.End();
		blow_handler.removeCallbacks(pollTask);
		if(blow_sensor!=null) blow_sensor.Stop();
		
//		main_activity.initGame(-1);
		
	}
	
	// Region -- Handle Blow Sensor
	private void startBlowSensor(){
		if(blow_sensor==null) blow_sensor=new BlowSensor();
		blow_sensor.Start();		
		
		blow_handler.postDelayed(pollTask,POLL_INTERVAL);
	}
	
	private void updateBlowStatus(){
		Log.i("STLog","!!! Blow Detected !!!");
		
		HashMap<Object,Object> params=new HashMap<Object,Object>();
		main_activity.sendEvent(GameEventCode.Game_A_Blow,params);
	}
	
	// EndRegion
	
	@Override
	public void HandleSensor(float[] sensor_value) {
		// TODO Auto-generated method stub
		
	}
	
}
