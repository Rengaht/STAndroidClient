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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.rengatartgital.photonandroidclient.AGame.GameAView;
import com.rengatartgital.photonandroidclient.BGame.GameBView;
import com.rengatartgital.photonandroidclient.CGame.GameCView;
import com.rengatartgital.photonandroidclient.SoundUtil.BackMusicService;
import com.rengatartgital.photonandroidclient.SoundUtil.SoundPoolHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.BaseGameView;
import com.rengatartgital.photonandroidclient.ViewUtil.MainBackButton;
import com.rengatartgital.photonandroidclient.ViewUtil.SVProgressHUD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.exitgames.client.photon.TypedHashMap;

public class MainActivity extends Activity implements SensorEventListener{
	
	public final boolean EnableLog=false;
	
	
	final String LOG_TAG="STLog";
	final String PARAM_FILE_NAME="stapp_params.json";
	final boolean OFFLINE=false;
	
	private enum GameState {LogIn,Join};
	GameState gstate;
	public int icur_game;
	
	Thread client_thread;
	
	
	PhotonClient photon_client;
	
	TextView hint_text;
	
	String client_id=null;
	
	public Integer side_index=null; // Left or Right

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
	
	Vibrator mvibrator;
	
	
	BaseGameView[] arr_game_view;
	MainBackButton[] arr_game_button;
	
	ToggleButton toggle_sound;
	boolean play_sound=true;
	
	
	SoundPoolHelper mSoundPoolHelper;
//	int sound_id_start,sound_id_button,sound_id_shutter,sound_id_finish,sound_id_alarm;
	int[] arr_sound_id;
	int stream_id_engine=-1;
	
	
	AlertDialog.Builder mdialog_builder;
	Dialog mdialog;
	View dialog_view;
	
	
	
	public final Handler handler=new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		
    		
    		if(msg.what==100){ // message from sub_view!
    			switch(msg.arg1){
    				case 100:
    					addPicToGallery((String)msg.obj);
    					playSound(3);
    					return;
    				case 102: // rotate for finish view
    					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    					return;
    				case 103: // send island trigger
    					sendEvent(GameEventCode.Game_A_Light,new HashMap<Object,Object>());
    					return;
    				case 104:
    					sendEvent(GameEventCode.Game_A_Leave,new HashMap<Object,Object>());
    					return;
    				case 120:
    					setEngineSoundRate(2);
    					return;
    				case 200: // time out
    					showUnavailable(200);
    					break;
    				case 500:
    					showUnavailable(500);
    					return;
    				case 900:// show progress bar
    					SVProgressHUD.showInView(MainActivity.this, "", true);
    			    	return;
    				case 999:
    					showUnavailable(999);
    					return;
    				
    			}
    			// go back to main view
    			if(icur_game>=0 && icur_game<3){
    				arr_game_view[icur_game].End();
    				icur_game=-1;
    			}
    			sendCheckIdEvent();
    			return;
    		}
    		
    		SVProgressHUD.dismiss(MainActivity.this);
    		
    		GameEventCode action_code=GameEventCode.fromInt(msg.arg1);
    		 if(EnableLog) Log.i(LOG_TAG, "Got Msg: "+action_code.toString());
    		
    		
    		TypedHashMap<Byte,Object> params=(TypedHashMap<Byte,Object>)msg.obj;
    		if(params!=null){
    			 if(EnableLog) Log.i(LOG_TAG,"params_size= "+params.size());
	    		Set<Byte> lkey=params.keySet();
	    		for(Byte k:lkey) Log.i(LOG_TAG,k+"->"+params.get(k));
    		}
    		switch(action_code){
	    		case Server_Disconnected:
	    			if(icur_game>-1 && arr_game_view[icur_game].isFinish()) break;
	    			
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
					
					
					
					if(icur_game>-1){ // if is at finish stage, dont' jump to main view
						if(arr_game_view[icur_game].isFinish()) break;
//						if(arr_game_view[icur_game].getVisibility()==View.VISIBLE) showUnavailable(100);
					}
					initGame(-1);
					
					if(params.containsKey((byte)201)){
						String cur_ver=(String)params.get((byte)201);
						if(!checkAppVersion(cur_ver)){
							showUnavailable(998);
							return;
						}
					}
					
    				if(params.containsKey((byte)1)){
    					int igame=(Integer)params.get((byte)1);
    					if(igame>=0){

    						setupGameButton(igame);
    						if(params.containsKey((byte)100)){
    		    				String get_id=(String)params.get((byte)100);
    		    				setUserId(get_id);
    	    				}
    					}else{
    						 showUnavailable(0);
    					}
    				}
    				
    				
    				break;
    				
    			case Server_Join_Success:
    				
    				if(params.containsKey((byte)101)) side_index=(Integer)params.get((byte)101);
    				if(params.containsKey((byte)102)){
    					waiting_index=(Integer)params.get((byte)102);
    					 if(EnableLog) Log.i(LOG_TAG,"Waiting_Index= "+waiting_index);
    				}
    				if(params.containsKey((byte)103)){
    					waiting_stamp=(String)params.get((byte)103);
    					 if(EnableLog) Log.i(LOG_TAG,"Waiting_Stamp= "+waiting_stamp);
    				}
    				
//    				initGame(icur_game);
    				int istatus=(Integer)params.get((byte)1);
    				if(istatus==1) initGame(icur_game);
    				else{
    					showUnavailable(istatus);
    					setupGameButton(icur_game);
    				}
    					
    				break;

    			default:
    				if(icur_game>-1) arr_game_view[icur_game].HandleMessage(action_code, params);
    				break;
    		}
    		
    	}

	
    };
 
    /* Bgm Service*/
    private boolean mIsBound = false;
    private BackMusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

	        public void onServiceConnected(ComponentName name, IBinder binder){
	        	mServ=((BackMusicService.ServiceBinder)binder).getService();
	        }
	
	        public void onServiceDisconnected(ComponentName name) {
		        mServ = null;
	        }
    };
	private Timer timer_engine_rate;
	private EngineRateTimerTask task_engine_rate;
	
	
    void doBindService(){
	    bindService(new Intent(this,BackMusicService.class),Scon,Context.BIND_AUTO_CREATE);
	    mIsBound = true;
    }

    void doUnbindService(){
	    if(mIsBound){
		        unbindService(Scon);
  		        mIsBound = false;
	    }
    }
    /* End Bgm Service*/

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        
       // View decorView=getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
       // int uiOptions=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN;
        			
       // decorView.setSystemUiVisibility(uiOptions);
        
        setContentView(R.layout.activity_main);
        
        
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
					
					playSound(0);
					HashMap<Object,Object> params=new HashMap<Object,Object>();
					params.put((byte)1,icur_game);
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
        
        toggle_sound=(ToggleButton)findViewById(R.id.Button_Sound);
		toggle_sound.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				if(toggle_sound.isChecked()){
					play_sound=false;
					stopBGM();
					stopAllSoundEffect();
				}
				else{
					play_sound=true;
					startBGM();
					if(icur_game==1 && ((GameBView)arr_game_view[1]).isEngine()) startEngineSound();
				}

			}
			
		});
		FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams)toggle_sound.getLayoutParams();
		params2.width=(int)(params.height*.8f);
		params2.height=(int)(params.height*.8f);
//        params2.leftMargin=size.x-params.height;
//        params2.topMargin=(int)(params.height*.2f);
		
        
       
		
        initMainView();
        
        arr_sound_id=new int[18];
        mSoundPoolHelper=new SoundPoolHelper(5, this);
        arr_sound_id[0]=mSoundPoolHelper.load(this,R.raw.sound_start,1);
        arr_sound_id[1]=mSoundPoolHelper.load(this,R.raw.sound_button,1);
        arr_sound_id[2]=mSoundPoolHelper.load(this, R.raw.sound_shutter,1);
        arr_sound_id[3]=mSoundPoolHelper.load(this, R.raw.sound_finish,1);
        
        arr_sound_id[7]=mSoundPoolHelper.load(this,R.raw.sound_ascore,1);
//        arr_sound_id[8]=mSoundPoolHelper.load(this,R.raw.sound_afinish,1);
        arr_sound_id[9]=mSoundPoolHelper.load(this,R.raw.sound_button_short,1);
        
        arr_sound_id[10]=mSoundPoolHelper.load(this,R.raw.sound_atrigger_1,1);
        arr_sound_id[11]=mSoundPoolHelper.load(this,R.raw.sound_atrigger_2,1);
        arr_sound_id[12]=mSoundPoolHelper.load(this,R.raw.sound_atrigger_3,1);
        arr_sound_id[13]=mSoundPoolHelper.load(this,R.raw.sound_atrigger_4,1);
        
        arr_sound_id[4]=mSoundPoolHelper.load(this,R.raw.sound_car_start,2);
        arr_sound_id[5]=mSoundPoolHelper.load(this,R.raw.sound_car_end,2);
        arr_sound_id[14]=mSoundPoolHelper.load(this,R.raw.sound_bbump,2);
        arr_sound_id[15]=mSoundPoolHelper.load(this,R.raw.sound_bpickup,2);
        arr_sound_id[6]=mSoundPoolHelper.load(this,R.raw.sound_bready,2);
        
        arr_sound_id[16]=mSoundPoolHelper.load(this,R.raw.sound_bengine_loop_low,1);
//        arr_sound_id[17]=mSoundPoolHelper.load(this,R.raw.bgm_gaming,3);
        
        mSoundPoolHelper.setLoop(arr_sound_id[6],-1);
//        mSoundPoolHelper.setLoop(arr_sound_id[16],-1);
//        mSoundPoolHelper.setLoop(arr_sound_id[17],-1);
        
        
        // alert dialog
        
      dialog_view=this.getLayoutInflater().inflate(R.layout.message_dialog_layout,null);
//		mdialog.setContentView(R.layout.message_dialog_layout);
//		mdialog.setCanceledOnTouchOutside(true);
//		Window window = mdialog.getWindow();
//		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		window.setGravity(Gravity.CENTER);
		
		mvibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
		
//		startBGM();
		
		
		/* setup bgm */
		doBindService();
		Intent music = new Intent();
		music.setClass(this,BackMusicService.class);
		startService(music);
		
    }
    
    private void startBGM(){
    	if(mServ!=null) mServ.resumeMusic();

    }
    private void stopBGM(){
//    	backsound.cancel(true);   
    	
    	if(mServ!=null)  mServ.pauseMusic();
    }
    
    public void startEngineSound(){
//    	if(!play_sound) return;
//    	
//    	stopBGM();
//    	if(!(enginesound.getStatus()==Status.RUNNING)){
//    		enginesound=new BackgroundSound(R.raw.sound_bengine);
//    		enginesound.execute();
//    	}
    	playSound(16);
    	
    }
    public void stopAllSoundEffect(){
//    	enginesound.cancel(true);
//    	if(play_sound) startBGM();
    	
    	mSoundPoolHelper.autoPause();
    	
    }
    public void stopEngineSound(){
    	mSoundPoolHelper.stop(stream_id_engine);
    }
    
    private void setEngineSoundRate(float set_rate){
    	
    	mSoundPoolHelper.setRate(stream_id_engine, set_rate);
    	
    	if(set_rate!=1){
    		if(timer_engine_rate!=null){
    			timer_engine_rate.cancel();
    			task_engine_rate.cancel();
    		}
    		
    		timer_engine_rate=new Timer(true);
    		task_engine_rate=new EngineRateTimerTask();
    		timer_engine_rate.schedule(task_engine_rate,800);
    	}
    }
    
	
	
    public void playButtonSound(){
    	playSound(1);
    }
    public void playShutterSound(){
    	playSound(2);
    }
    
    

    public void playSound(int isound){
    	
    	if(!play_sound) return;
    	
        if(isound!=16) mSoundPoolHelper.play(arr_sound_id[isound],1.0f,1.0f,1,0,1.0f);
        else stream_id_engine=mSoundPoolHelper.play(arr_sound_id[isound],1.0f,1.0f,1,-1,1.0f);
        
        if(isound==14 || isound==15) mvibrator.vibrate(100);
    }
    
  
    public void Reconnect(){
        Reconnect(true);
    }
    public void Reconnect(boolean delay){
    	
//    	if(icur_game>-1 && arr_game_view[icur_game].isFinish()) return;
    	SVProgressHUD.showInView(MainActivity.this, "", true);
    	
    	if(!delay){
    		
    		ConnectServer();
    		return;
    	}
    	
    	
    	
    	hint_text.setText("Reconnect...");
    	 if(EnableLog) Log.i("STConnect","Reconnect....");
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
    	  photon_client=new PhotonClient(handler,EnableLog);
          client_thread=new Thread(photon_client);
          client_thread.start();
          
      	
          
    }
    public void sendEvent(GameEventCode send_event_code,HashMap<Object,Object> params){
    	sendEvent(send_event_code,params,true);
    }
    public void sendEvent(GameEventCode send_event_code,HashMap<Object,Object> params,boolean show_progress){
		if(params==null) params=new HashMap<Object,Object>();
		if(client_id!=null) params.put((byte)100, client_id);
		if(side_index!=null) params.put((byte)101,side_index);
		
		photon_client.sendSomeEvent(send_event_code.getValue(),params);
		
		if(show_progress) SVProgressHUD.showInView(MainActivity.this, "", true);
	}
	
    public void sendCheckIdEvent(){
    	
    	
    	if(!photon_client.is_connected) Reconnect(false);
    	
    	HashMap<Object,Object> params=new HashMap<Object,Object>();
    	params.put((byte)100, client_id);
    	
    	sendEvent(GameEventCode.UCheckId,params);
    	
    	
    	
    }
    
    
	public String getEncodedImage(byte[] abyte){
		String encode=Base64.encodeToString(abyte,Base64.DEFAULT);
		 if(EnableLog) Log.i("STLog","encoded string length= "+encode.length());
		return encode;
	}
	

	
	
    // Region -- Handle UI
    
    private void setupGameButton(int igame){
    	
    	if(mdialog!=null && mdialog.isShowing()) mdialog.dismiss();
    	
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
    	
    	
    	 if(EnableLog) Log.i(LOG_TAG,"Init Game "+game_index);
    	
    	icur_game=game_index;
    	    	
    	for(MainBackButton game_button:arr_game_button) game_button.setVisibility(View.GONE);
    	
    	arr_game_view[icur_game].setVisibility(View.VISIBLE);
    	arr_game_view[icur_game].Init();
    	
    	if(game_index==1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	
    	stream_id_engine=-1;
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
    
    

	private void showUnavailable(int istatus){
		
//		if(mdialog!=null && mdialog.isShowing())  mdialog.dismiss();
		
		String text_show="";
		switch(istatus){
			case 0:
			case 2:
				text_show="稍後再試";
				break;
			case 100:
//				text_show="開始新遊戲";
				return;
			case 200:
//				text_show="遊戲結束";
				return;				
			case 500:
				text_show="你可以取更好的名字";
				break;
			case 998:
				text_show="請下載新版本";
				break;
			case 999:
				text_show="發生問題";
				break;
		}
//		Builder alert_builder=new AlertDialog.Builder(MainActivity.this);
//		alert_builder.setMessage(text_show);
//		alert_builder.create().show();
//		alert_builder.setView(dialog_view);
//		
//		TextView _text=(TextView)dialog_view.findViewById(R.id.text_message);
//		_text.setText(text_show);
		
//		alert_builder.create();
//		mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		
//		mdialog=alert_builder.show();
		
		
		final Dialog dialog=new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.message_dialog_layout);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xCC000000));
		
		Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width=(int)(size.x*.75f);
		params.height=(int)(size.x*.25f);
		dialog.getWindow().setAttributes(params);
		
		TextView _text=(TextView)dialog.findViewById(R.id.text_message);
		_text.setText(text_show);
		
		
		dialog.show();
		
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
    	
    	 if(EnableLog) Log.i(LOG_TAG,"Init Sensor");
    	
    	sensor_manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	List<Sensor> sensors=sensor_manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
    	
    	if(sensors.size()>0){
//    		acc_sensor=sensors.get(0);
    		for(Sensor sensor:sensors)
    			sensor_manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
    		
    		 if(EnableLog) Log.i(LOG_TAG,"Sensor ready!");
    		
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

	
	}
	// EndRegion
		
	
	
	
	
//	private byte[] openFile(){
//		
//		
//		try{
//			InputStream input=getAssets().open("Unihorse.png");
//			int input_size=input.available();
//			byte[] abyte=new byte[input_size];
//			BufferedInputStream buf=new BufferedInputStream(input);
//			buf.read(abyte,0,abyte.length);
//			buf.close();
//			
//			return abyte;
//			
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		return null;
//		
//	}
	
	// Region - Save Image
	
	
	public void addPicToGallery(String file_path){
		
		if(EnableLog) Log.i("STLog","Add to gallery: "+file_path);
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
		 if(EnableLog) Log.i(LOG_TAG,"read params file: "+file.getAbsolutePath());
		
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
		
		if(client_id!=null && client_id.length()<1) client_id=null;
		if(waiting_index!=null && waiting_index<0) waiting_index=null;
		if(waiting_stamp!=null && waiting_stamp.length()<1) waiting_stamp=null;
		
		if(client_id!=null) Log.i(LOG_TAG,">>> USER:　"+client_id);
		if(waiting_index!=null && waiting_stamp!=null) Log.i(LOG_TAG,">>> WAITING:　"+waiting_index+" "+waiting_stamp);
		
		
	}
	
	private void writeParameterFile(boolean write_default){
		
		File file=new File(getApplicationContext().getFilesDir(),PARAM_FILE_NAME);
		 if(EnableLog) Log.i(LOG_TAG,"write params file: "+file.getAbsolutePath());
		
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
	    
	    stopBGM();
	    stopAllSoundEffect();
	    
	    if(EnableLog) Log.i("STLog","---- PAUSE ----");
	     //TODO: save tmp data
	    if(icur_game>-1) arr_game_view[icur_game].End();
	    //initGame(-1);
	    //((GameCView)arr_game_view[2]).stopCamera();
	    
	}
	@Override
	public void onResume() {
	    super.onResume();  
	    
	    if(play_sound) startBGM();
	    
	    if(EnableLog) Log.i("STLog","---- RESUME ----");
	    
	    // TODO: recover tmp-saved data
	    readParameterFile();
	    
	    //arr_game_view[icur_game].Init();
	    initGame(-1);
	    sendCheckIdEvent();
	    //setupGameButton(icur_game);
	}
	
	@Override
	public void onDestroy(){
	    super.onDestroy();  
	    
	    doUnbindService();
	    
	}
	
	public class BackgroundSound extends AsyncTask<Void, Void, Void> {

		
	

		int sound_id;
		
		BackgroundSound(int set_id){
			super();
			sound_id=set_id;
		}
		
	    @Override
	    protected Void doInBackground(Void... params) {
	        MediaPlayer player = MediaPlayer.create(MainActivity.this, sound_id); 
	        player.setLooping(true); // Set looping 
	        player.setVolume(50,50); 
	        player.start(); 
	        
	        while(!isCancelled()){
	        	try{
	                //do something
	                if(EnableLog) Log.i(LOG_TAG, "Sleeping...");
	                Thread.sleep(500);
	            }catch(InterruptedException e){
	            	 if(EnableLog) Log.i(LOG_TAG, "Task was inturrupted");
	                player.stop();
	            }catch(Exception e){
	            	 if(EnableLog) Log.e(LOG_TAG, e.toString(), e);
	            }   
	        }
	        
	        return null;
	    }

	}

	private class EngineRateTimerTask extends TimerTask{
		@Override
		public void run(){
			setEngineSoundRate(1);
		}
	}
	
	
	boolean checkAppVersion(String check_ver){
		String appVersion="";
		PackageManager manager=this.getPackageManager();
		try{ 
			PackageInfo info=manager.getPackageInfo(this.getPackageName(),0);
			appVersion = info.versionName; 
		}catch(NameNotFoundException e){
			e.printStackTrace();
		}
		
		return appVersion.equals(check_ver);
		
	}
	
}
