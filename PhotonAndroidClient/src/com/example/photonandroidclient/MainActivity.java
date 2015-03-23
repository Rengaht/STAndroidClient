package com.example.photonandroidclient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.exitgames.client.photon.TypedHashMap;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity implements SensorEventListener{
	
	final String LOG_TAG="STLog";
	
	enum GameState {LogIn,Join,SetName,Blow,GameA_End,
					GameB_Wait,Rotate,GameB_End,
					Face,GameC_End};
	GameState gstate;
	int icur_game;
	
	Thread client_thread;
	
	Button send_button,connect_button;
	PhotonClient photon_client;
	EditText name_text;
	TextView hint_text;
	
	String client_id;
	int color_id;
	
	/** sensor */
	SensorManager sensor_manager;
	Sensor acc_sensor;
	private float[] sensor_value=new float[3];
	long beat_timestamp;
	boolean toSendBeat=false;
	
	long BEAT_RESOLUTION=100;
	float BEAT_STRENGTH_THRESHOLD=3.0f;

	
	
	Handler handler=new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		
    		
    		GameEventCode action_code=GameEventCode.fromInt(msg.arg1);
    		Log.i(LOG_TAG, "Got Msg: "+action_code.toString());
    		
    		
    		TypedHashMap<Byte,Object> params=(TypedHashMap<Byte,Object>)msg.obj;
    		
    		switch(action_code){
    			case Server_Connected:
    				updateGameState(GameState.Join);
    				break;
    			case Server_Disconnected:
    				Reconnect();
    				break;
    			case Server_Id:
    				client_id=(String)params.get((byte)1);
    				color_id=(Integer)params.get((byte)2);
    				switch(icur_game){
    					case 0:
    						updateGameState(GameState.SetName);
    						break;
    					case 1:
    						updateGameState(GameState.GameB_Wait);
    						break;
    					case 2:
    						updateGameState(GameState.Face);
    						break;
    				}
    				
    				break;
    			case Server_GG:
    				setGG(params);
    				switch(icur_game){
						case 0:
							updateGameState(GameState.GameA_End);
							break;
						case 1:
							updateGameState(GameState.GameB_End);
							break;
						case 2:
							updateGameState(GameState.GameC_End);
							break;
    				}
    				break;
    			case Server_Name_Success:
    				updateGameState(GameState.Blow);
    				break;
    			case Server_GameB_Start:
    				updateGameState(GameState.Rotate);
    				break;
    			case Server_Game_Info:
    				int igame=(Integer)params.get((byte)1);
    				initGame(igame);
    				break;
    			case Server_Face_Success:
    				Log.i(LOG_TAG,"Send Face Success");
    				break;
    			default:
    				break;
    		}
    		
    	}    	
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initSensor();
        
        ConnectServer();
      
        
        name_text=(EditText)findViewById(R.id.NameText);
        hint_text=(TextView)findViewById(R.id.HintView);
        
        send_button=(Button)findViewById(R.id.SendButton);
        send_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				HashMap<Object,Object> params=new HashMap<Object,Object>();
				params.put((byte)100, client_id);
				switch(gstate){
					
					case Join:
						params.put((byte)1,0);
						photon_client.sendSomeEvent(GameEventCode.UJoin.getValue(),params);						
						break;
					case SetName:
						params.put((byte)100,client_id);
						params.put((byte)1, name_text.getText().toString());
						photon_client.sendSomeEvent(GameEventCode.Game_A_Name.getValue(),params);
						break;					
					case Blow:
						params.put((byte)100,client_id);
						photon_client.sendSomeEvent(GameEventCode.Game_A_Blow.getValue(),params);
						break;
					case Face:
						params.put((byte)100, client_id);
						params.put((byte)1, "name");
						params.put((byte)2, getEncodedImage());
						params.put((byte)3, 0);
						photon_client.sendSomeEvent(GameEventCode.Game_C_Face.getValue(),params);
						break;
					
					default:
						break;
				}
				
			}        	
        });
        
       
       
    }
    public void Reconnect(){
    	
    	hint_text.setText("Reconnect...");
    	Timer timer=new Timer();
    	TimerTask task=new TimerTask(){
			@Override
			public void run(){
				ConnectServer();
			}
    	};
    	timer.schedule(task, 5000);
    	
    }
    private void ConnectServer(){
    		
    	  
    	  photon_client=new PhotonClient(handler);
          client_thread=new Thread(photon_client);
          client_thread.start();
          
    }
    private void initGame(int game_index){
    	
    	icur_game=game_index;
    	updateGameState(GameState.Join);
    	
    }
    private void updateGameState(GameState new_gstate){
    	
    	gstate=new_gstate;
    	send_button.setEnabled(true);
    	name_text.setEnabled(false);
		hint_text.setText("Now "+gstate.toString()+" Game "+icur_game);
    	
    	switch(new_gstate){
    		case Join:
    			send_button.setText("Join Game");
    			break;
    		case SetName:
    			send_button.setText("Send Name");
    			name_text.setEnabled(true);
    			break;
    		case Blow:
    			send_button.setText("Send Blow");
    			break;
    		case Rotate:
    			send_button.setEnabled(false);
    			break;
    		case Face:
    			send_button.setText("Send Face");
    			break;
			default:
				break;
    	}
    }
    private void setGG(TypedHashMap<Byte,Object> params){
    	
//    	hint_text.setText("GAME OVER");
    	
    }
    private void initSensor(){
    	
    	Log.i(LOG_TAG,"Init Sensor");
    	
    	sensor_manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	List<Sensor> sensors=sensor_manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
    	
    	if(sensors.size()>0){
//    		acc_sensor=sensors.get(0);
    		for(Sensor sensor:sensors)
    			sensor_manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
    		
    		Log.i(LOG_TAG,"Sensor ready!");
    		
    		beat_timestamp=-BEAT_RESOLUTION;
    				
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(gstate!=GameState.Rotate) return;
		
		double delta_strength=event.values[1];
		
		if(Math.abs(delta_strength)>BEAT_STRENGTH_THRESHOLD){
				
//				Log.i(LOG_TAG,"Send Rotate!");
				
				HashMap<Object,Object> params=new HashMap<Object,Object>();
				params.put((byte)100, client_id);				
				params.put((byte)1, color_id);
				params.put((byte)2,(delta_strength>0)?1:-1);
				photon_client.sendSomeEvent(GameEventCode.Game_B_Rotate.getValue(),params);
				
		}
		
		
		Log.i("Sensor",event.values[0]+" , "+event.values[1]+" , "+event.values[2]+" strength= "+delta_strength);
	}
	private String getEncodedImage(){
		
		byte[] abyte=openFile();
		return Base64.encodeToString(abyte,Base64.DEFAULT);
		
	}
	private byte[] openFile(){
		
		
		try{
			InputStream input=getAssets().open("Unihorse.png");
			int input_size=input.available();
			byte[] abyte=new byte[input_size];
			BufferedInputStream buf=new BufferedInputStream(input);
			buf.read(abyte,0,abyte.length);
			buf.close();
			
			return abyte;
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
		
	}
	

}
