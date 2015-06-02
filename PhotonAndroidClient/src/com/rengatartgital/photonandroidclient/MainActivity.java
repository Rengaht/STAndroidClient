package com.rengatartgital.photonandroidclient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.rengatartgital.photonandroidclient.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.exitgames.client.photon.TypedHashMap;

public class MainActivity extends Activity implements SensorEventListener{
	
	final String LOG_TAG="STLog";
	final String PARAM_FILE_NAME="stapp_params.json";
	final boolean OFFLINE=false;
	
	private enum GameState {LogIn,Join};
	GameState gstate;
	int icur_game;
	
	Thread client_thread;
	
	
	PhotonClient photon_client;
	
	TextView hint_text;
	
	String client_id=null;
	
	Integer side_index=null; // Left or Right

	// For GameB
	String waiting_stamp=null; 
	Integer waiting_index=null;

	
	/** sensor */
	SensorManager sensor_manager;
	Sensor acc_sensor;
	float[] last_frame_sensor_value;
	
	long beat_timestamp;
	boolean toSendBeat=false;
	
	long BEAT_RESOLUTION=100;
	float BEAT_STRENGTH_THRESHOLD=3.0f;

	
	BaseGameView[] arr_game_view;
	MainBackButton[] arr_game_button;
	
	
	SoundPoolHelper mSoundPoolHelper;
	int sound_id_start,sound_id_button,sound_id_shutter,sound_id_finish;
	
	
	final Handler handler=new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		
    		
    		if(msg.what==100){
    			switch(msg.arg1){
    				case 100:
    					addPicToGallery((String)msg.obj);
    					playSound(sound_id_finish);
    					return;
    			}
    			// go back to main view
    			if(icur_game>=0 && icur_game<3) arr_game_view[icur_game].End();
    			sendCheckIdEvent();
    			return;
    		}
    		
    		
    		GameEventCode action_code=GameEventCode.fromInt(msg.arg1);
    		Log.i(LOG_TAG, "Got Msg: "+action_code.toString());
    		
    		
    		TypedHashMap<Byte,Object> params=(TypedHashMap<Byte,Object>)msg.obj;
    		if(params!=null){
    			Log.i(LOG_TAG,"params_size= "+params.size());
	    		Set<Byte> lkey=params.keySet();
	    		for(Byte k:lkey) Log.i(LOG_TAG,k+"->"+params.get(k));
    		}
    		switch(action_code){
	    		case Server_Disconnected:
	    			initGame(-1);
					Reconnect();
					break;
					
				case Server_Connected:
    				updateGameState(GameState.Join);
    				break;
				case Server_Login_Success:
					sendCheckIdEvent();
					break;
				case Server_Id_Game_Info:
					initGame(-1);
    				if(params.containsKey((byte)1)){
    					int igame=(Integer)params.get((byte)1);
    					if(igame>=0){
    						setupGameButton(igame);
    						if(params.containsKey((byte)100)){
    		    				String get_id=(String)params.get((byte)100);
    		    				setUserId(get_id);
    	    				}
    					}
    				}
    				
    				
    				break;
    				
    			case Server_Join_Success:
//    				if(params.containsKey((byte)101)) color_id=(Integer)params.get((byte)101);
    				if(params.containsKey((byte)102)){
    					waiting_index=(Integer)params.get((byte)102);
    					Log.i(LOG_TAG,"Waiting_Index= "+waiting_index);
    				}
    				if(params.containsKey((byte)103)){
    					waiting_stamp=(String)params.get((byte)103);
    					Log.i(LOG_TAG,"Waiting_Stamp= "+waiting_stamp);
    				}
    				
    				initGame(icur_game);
    				
    				break;

    			default:
    				if(icur_game>-1) arr_game_view[icur_game].HandleMessage(action_code, params);
    				break;
    		}
    		
    	}
	
    };
 
    
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        
       // View decorView=getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
       // int uiOptions=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN;
        			
       // decorView.setSystemUiVisibility(uiOptions);
        
        setContentView(R.layout.activity_main);
        
//        back_img_view=(ImageView)findViewById(R.id.BackView);       
//        Display display = getWindowManager().getDefaultDisplay(); 
//		Point sizee=new Point(0,0);
//		display.getSize(sizee);
//		float hei=sizee.y;
//		float scale_y=hei/1920.0f;
//        Matrix back_matrix=new Matrix();
//        back_matrix.setScale(scale_y,scale_y);
//        back_img_view.setImageMatrix(back_matrix);
        
        readParameterFile();
        
        initSensor();
        
        if(!OFFLINE) ConnectServer();
      
        arr_game_view=new BaseGameView[3];
        arr_game_view[0]=(GameAView)findViewById(R.id.GameA);
        arr_game_view[1]=(GameBView)findViewById(R.id.GameB);
        arr_game_view[2]=(GameCView)findViewById(R.id.GameC);
        arr_game_view[0].setMainActivity(this);
        arr_game_view[1].setMainActivity(this);
        arr_game_view[2].setMainActivity(this);
        

		
        arr_game_button=new MainBackButton[3];
        arr_game_button[0]=(MainBackButton)findViewById(R.id.Button_GameA);
        arr_game_button[1]=(MainBackButton)findViewById(R.id.Button_GameB);
        arr_game_button[2]=(MainBackButton)findViewById(R.id.Button_GameC);
        
        for(int i=0;i<3;++i){
        	final int index=i;
        	MainBackButton game_button=arr_game_button[i];
        	game_button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					playSound(sound_id_start);
					HashMap<Object,Object> params=new HashMap<Object,Object>();
					params.put((byte)1,index);
					if(index==1 && waiting_index!=null && waiting_stamp!=null){
						params.put((byte)102, waiting_index);
						params.put((byte)103, waiting_stamp);
					}
					sendEvent(GameEventCode.UJoin,params);						
				}
        		
        	});
        }
        
        hint_text=(TextView)findViewById(R.id.HintView);
        
        ImageView img_logo=(ImageView)findViewById(R.id.Image_Logo);
        FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)img_logo.getLayoutParams();
        
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        
        params.width=size.x/2;
        params.height=(int)(size.x/2*.1724f);
        img_logo.setLayoutParams(params);
        
        
        initMainView();
        
        
        mSoundPoolHelper=new SoundPoolHelper(1, this);
        sound_id_start=mSoundPoolHelper.load(this,R.raw.sound_start,1);
        sound_id_button=mSoundPoolHelper.load(this,R.raw.sound_button,1);
        sound_id_shutter=mSoundPoolHelper.load(this, R.raw.sound_shutter,1);
        sound_id_finish=mSoundPoolHelper.load(this, R.raw.sound_finish,1);
        
        
    }
    
    public void playButtonSound(){
    	playSound(sound_id_button);
    }
    public void playShutterSound(){
    	playSound(sound_id_shutter);
    }
//    public void playFinishSound(){
//    	playSound(sound_id_finish);
//    }
    private void playSound(int soundId){
        mSoundPoolHelper.play(soundId);
    }
    
    public void Reconnect(){
    	
    	hint_text.setText("Reconnect...");
    	Log.i("STConnect","Reconnect....");
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
    		
    	  if(photon_client!=null){
    		  if(photon_client.is_connected) photon_client.disconnect();
    	  }
    	  //if(client_thread!=null) client_thread.
    	  photon_client=new PhotonClient(handler);
          client_thread=new Thread(photon_client);
          client_thread.start();
          
    }
    
    public void sendEvent(GameEventCode send_event_code,HashMap<Object,Object> params){
		if(params==null) params=new HashMap<Object,Object>();
		if(client_id!=null) params.put((byte)100, client_id);
		if(side_index!=null) params.put((byte)101,side_index);
		
		photon_client.sendSomeEvent(send_event_code.getValue(),params);
	}
	
    public void sendCheckIdEvent(){
    	HashMap<Object,Object> params=new HashMap<Object,Object>();
    	params.put((byte)100, client_id);
    	
    	sendEvent(GameEventCode.UCheckId,params);
    }
    
    
	String getEncodedImage(byte[] abyte){
		String encode=Base64.encodeToString(abyte,Base64.DEFAULT);
		Log.i("STLog","encoded string length= "+encode.length());
		return encode;
	}
	

	
	
    // Region -- Handle UI
    
    private void setupGameButton(int igame){
		icur_game=igame;
		
		initMainView();
		if(igame>-1) arr_game_button[2-igame].setEnabled(true);
		
	}   
    public void initGame(int game_index){
    	
    	
    	if(game_index==-1){
    	
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    		initMainView();
    		setupGameButton(-1);
    		return;
    	}
    	
    	
    	Log.i(LOG_TAG,"Init Game "+game_index);
    	
    	icur_game=game_index;
    	    	
    	for(MainBackButton game_button:arr_game_button) game_button.setVisibility(View.GONE);
    	
    	arr_game_view[icur_game].setVisibility(View.VISIBLE);
    	arr_game_view[icur_game].Init();
    	
    	if(game_index==1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	
    	
    }
    private void initMainView(){
    	for(MainBackButton game_button:arr_game_button){ 
    		game_button.setVisibility(View.VISIBLE);
    		game_button.setEnabled(false);
    	}
    	for(BaseGameView game_view:arr_game_view){
    		game_view.setVisibility(View.INVISIBLE);
    	}
    }
    
    // EndRegion
    
    public void setUserId(String set_client){
		client_id=set_client;
//		color_id=set_color;
		writeParameterFile(false);
	}
    public void setSideIndex(int set_side){
    	side_index=set_side;
    	writeParameterFile(false);
    }
    
    private void updateGameState(GameState new_gstate){
    	
    	gstate=new_gstate;
		hint_text.setText("Now "+gstate.toString()+" Game "+icur_game);
    	
    	switch(new_gstate){
    		case Join:
    			hint_text.setText("Ready to Join Game");
    			break;
    		default:
				break;
    	}
    }
    
    // Region -- AccSensor
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
    	
    	last_frame_sensor_value=new float[3];
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
//		float[] delta_value=new float[3];
//		for(int i=0;i<3;++i) delta_value[i]=event.values[i];//-last_frame_sensor_value[i];
//		Log.i("Sensors","sensor: "+delta_value[0]+","+delta_value[1]+","+delta_value[2]);
		
//		for(int i=0;i<3;++i) last_frame_sensor_value[i]=event.values[i];
		
		if(icur_game<0) return;
		
		arr_game_view[icur_game].HandleSensor(event.values);
		arr_game_view[icur_game].HandleSensor(event.values);

	
	}
	// EndRegion
		
	
	
	
	
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
	
	// Region - Save Image
	
	
	public void addPicToGallery(String file_path){
		
		Log.i("STLog","Add to gallery: "+file_path);
//	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//	    File f = new File(file_path);
//	    Uri contentUri = Uri.fromFile(f);
//	    mediaScanIntent.setData(contentUri);
//	    
//	    this.sendBroadcast(mediaScanIntent);
		
		ContentValues values = new ContentValues();

	    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
	    values.put(Images.Media.MIME_TYPE, "image/png");
	    values.put(MediaStore.MediaColumns.DATA, file_path);

	    this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
	    
	}
	
	
	// Region -- Parameter Data
	
	private void readParameterFile(){
		
//		File root=android.os.Environment.getExternalStorageDirectory(); 
		File file=new File(getApplicationContext().getFilesDir(),PARAM_FILE_NAME);
		Log.i(LOG_TAG,"read params file: "+file.getAbsolutePath());
		
		/** create default if not found */
		if(!file.exists()) writeParameterFile(true);
		
		file=new File(getApplicationContext().getFilesDir(),PARAM_FILE_NAME);
		JsonReader reader;
		
		try{
			FileInputStream in=new FileInputStream(file);
			reader=new JsonReader(new InputStreamReader(in,"UTF-8"));
			
			reader.beginObject();
			while(reader.hasNext()){
				String name=reader.nextName();
				if(name.equals("CLIENT_ID")) client_id=reader.nextString();
				else if(name.equals("WAITING_INDEX")) waiting_index=reader.nextInt();
				else if(name.equals("WAITING_STAMP")) waiting_stamp=reader.nextString();
				else reader.skipValue();
			}
			reader.endObject();
			
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(client_id.length()<1) client_id=null;
		if(waiting_index<0) waiting_index=null;
		if(waiting_stamp.length()<1) waiting_stamp=null;
		
		if(client_id!=null) Log.i(LOG_TAG,">>> USER:�@"+client_id);
		if(waiting_index!=null && waiting_stamp!=null) Log.i(LOG_TAG,">>> WAITING:�@"+waiting_index+" "+waiting_stamp);
		
		
	}
	
	private void writeParameterFile(boolean write_default){
		
		File file=new File(getApplicationContext().getFilesDir(),PARAM_FILE_NAME);
		Log.i(LOG_TAG,"write params file: "+file.getAbsolutePath());
		
		FileOutputStream out=null;
		JsonWriter writer;
		try{
			
			out=new FileOutputStream(file);
			writer=new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
			writer.setIndent("  ");
			writer.beginObject();
			
			if(write_default){
				writer.name("CLIENT_ID").value("222");
				writer.name("WAITING_INDEX").value(1);
				writer.name("WAITING_STAMP").value("222");
				
			}else{

				writer.name("CLIENT_ID").value(client_id);
				writer.name("WAITING_INDEX").value(waiting_index);
				writer.name("WAITING_STAMP").value(waiting_stamp);
			}
			writer.endObject();
			writer.close();
			
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	   
	}
	
	
	//EndRegion
	
	
	@Override
	public void onPause() {
	    super.onPause();  
	    
	    Log.i("STLog","---- PAUSE ----");
	     //TODO: save tmp data
	    arr_game_view[icur_game].End();
	    //initGame(-1);
	    //((GameCView)arr_game_view[2]).stopCamera();
	    
	}
	@Override
	public void onResume() {
	    super.onResume();  
	    
	    Log.i("STLog","---- RESUME ----");
	    
	    // TODO: recover tmp-saved data
	    readParameterFile();
	    
	    //arr_game_view[icur_game].Init();
	    initGame(-1);
	    sendCheckIdEvent();
	    //setupGameButton(icur_game);
	}
	
}