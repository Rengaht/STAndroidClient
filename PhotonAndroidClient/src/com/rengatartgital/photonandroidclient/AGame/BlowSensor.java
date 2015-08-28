package com.rengatartgital.photonandroidclient.AGame;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class BlowSensor {
	
	 static final private double EMA_FILTER = 0.6;
	 static final private double BLOW_THRES = 0.85;
	 
	 private MediaRecorder recorder = null;
	 private double mEMA = 0.0;
	 
	 public void Start(){
		 if(recorder==null){
			 recorder=new MediaRecorder();
			 recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			 recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
             recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
             recorder.setOutputFile("/dev/null"); 
             
             try{
                 recorder.prepare();
             }catch(IllegalStateException e){
                 e.printStackTrace();
             }catch(IOException e){
                 e.printStackTrace();
             }
              
            recorder.start();
            //Log.i("STLog","Start audio recorder!");
            mEMA = 0.0;
		 }
	 }
	 public void Stop(){
		 if(recorder!=null){
			 recorder.stop();
			 recorder.release();
			 recorder=null;
		 }
	 }
	 
	 private double getAmp(){
		 double amp=0;
		 if(recorder!=null)
			 amp=recorder.getMaxAmplitude()/27000.0;
//		 Log.i("STLog","amp= "+amp);
		 return amp;
	 }
	 
	 private double getAmpEMA(){
		 double amp=getAmp();
		 mEMA=EMA_FILTER*amp+(1.0-EMA_FILTER)*mEMA;
		 //Log.i("Sensor","mEMA= "+mEMA);
         return mEMA;
	 }
	 
	 public boolean isBlow(){
		 return getAmpEMA()>BLOW_THRES;
	 }
	 
	 
}
