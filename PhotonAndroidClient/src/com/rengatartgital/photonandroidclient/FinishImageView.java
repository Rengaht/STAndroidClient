package com.rengatartgital.photonandroidclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.rengatartgital.photonandroidclient.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

public class FinishImageView extends ImageView implements AnimatorUpdateListener{
	
	
	final int min_date_size=32;
	
	private Bitmap front_bmp;
	private Bitmap back_bmp;
	private Bitmap icon_bmp;
	private Bitmap notice_bmp;
	
	private ValueAnimator fadein_animator,fadeout_animator;
	float alpha_notice;
	private boolean alpha_in_finish;
	
	private int cur_width,cur_height;
	private Paint mfill_paint,mstroke_paint,mpaint,mnotice_paint;
	String date_str;
	
	
	Timer count_timer;
	
	Handler main_handle;
	boolean save_finish;
	
	int index_game;
	
	
	
	public FinishImageView(Context context){
		super(context);
	}
	public FinishImageView(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	public FinishImageView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}
	
	public void setup(int igame,Bitmap set_front,Handler set_handle){
		
		front_bmp=set_front;
		main_handle=set_handle;
		
		save_finish=false;
		
		cur_width=getWidth();
		cur_height=getHeight();
		
		index_game=igame;
		
		switch(igame){
			case 0:
				back_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamea_end_bg_3);
				notice_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_notice);
				break;
			case 1:
				back_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_end_bg_3);
				notice_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gameb_notice);
				break;
			case 2:
				back_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamec_end_bg_3);
				notice_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.gamec_notice);
				break;
		}
		
		back_bmp=Bitmap.createScaledBitmap(back_bmp,cur_width,cur_height,true);
		
		icon_bmp=BitmapFactory.decodeResource(getResources(),R.drawable.home_logo);
		icon_bmp=Bitmap.createScaledBitmap(icon_bmp,(int)(cur_width*.445f*.8f),(int)(cur_width*.081f*.8f),true);
		
		
		notice_bmp=Bitmap.createScaledBitmap(notice_bmp,cur_width,(int)(cur_width*0.0833f),true);
		
		
		mpaint=new Paint();
		mstroke_paint=new Paint();
		mstroke_paint.setColor(Color.RED);
		mstroke_paint.setTextSize(min_date_size);
		mstroke_paint.setTextAlign(Align.RIGHT);
		mstroke_paint.setAntiAlias(true);
		mstroke_paint.setStyle(Style.STROKE);
		mstroke_paint.setShadowLayer(6.5f, 1.0f, 1.0f, Color.BLACK);
		
		mfill_paint=new Paint();
		mfill_paint.setColor(Color.YELLOW);
		mfill_paint.setTextSize(min_date_size);
		mfill_paint.setTextAlign(Align.RIGHT);
		mfill_paint.setAntiAlias(true);
		mfill_paint.setStyle(Style.FILL);
		
		
		mnotice_paint=new Paint();
		
		
		Time cur_day=new Time();
		cur_day.setToNow();
		int myear=cur_day.year-1911;
		date_str=myear+"/"+String.format("%02d",cur_day.month+1)+"/"+String.format("%02d",cur_day.monthDay);
		
		
		
		
//		TimerTask task_autosave=new TimerTask(){
//			@Override
//			public void run(){
//				saveImage();
//			}
//		};
//		if(count_timer!=null) count_timer.cancel();
//		count_timer=new Timer();
//		count_timer.schedule(task_autosave,1000);
		
		
		fadein_animator = ValueAnimator.ofFloat(0,255);
		fadein_animator.setDuration(50);
        fadein_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadein_animator.addUpdateListener(this);
        fadein_animator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0){
				Log.i("STLog","Fade in end!!");
				fadeout_animator.start();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
        	
        });
        
        fadeout_animator = ValueAnimator.ofFloat(255,0);
		fadeout_animator.setDuration(200);
		fadeout_animator.setStartDelay(2000);
        fadeout_animator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeout_animator.addUpdateListener(this);
        
        
        saveImage();
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		drawOnCanvas(canvas);
		
	}
	private void drawOnCanvas(Canvas canvas){
		
		
		Log.i("STLog","Draw Finish View! "+cur_width+" x "+cur_height);
		
		if(back_bmp.getWidth()!=cur_width){
			back_bmp=Bitmap.createScaledBitmap(back_bmp,cur_width,cur_height,true);
			notice_bmp=Bitmap.createScaledBitmap(notice_bmp,cur_width,(int)(cur_width*0.0833f),true);
		}
		
		if(back_bmp!=null) canvas.drawBitmap(back_bmp,0,0,mpaint);
		if(front_bmp!=null && !front_bmp.isRecycled()){
			canvas.save();
			
			if(index_game==2) canvas.translate(cur_width*.47f,(int)(cur_height*.38));
			else if(index_game==1) canvas.translate(cur_width*.53f,(int)(cur_height*.42f));
			else canvas.translate(cur_width/2,(int)(cur_height*.28));
			
			
			canvas.scale(.75f,.75f);
			canvas.drawBitmap(front_bmp,(int)(-front_bmp.getWidth()/2),(int)(-front_bmp.getHeight()/2),mpaint);
			canvas.restore();
		}
		
		/* draw icon*/
		int marg=(int)(cur_width*.02);
		canvas.save();
		canvas.translate(marg,(int)(cur_height*0.59+marg));
			canvas.drawBitmap(icon_bmp,0,0,mpaint);
		canvas.restore();
		
		
		if(date_str!=null){
			float text_size=Math.min(cur_width, cur_height)/15;
			if(mstroke_paint!=null) mstroke_paint.setTextSize(Math.max(min_date_size,text_size));
			if(mfill_paint!=null) mfill_paint.setTextSize(Math.max(min_date_size,text_size));
			
			canvas.drawText(date_str,cur_width*.98f,cur_height*.57f,mstroke_paint);
			canvas.drawText(date_str,cur_width*.98f,cur_height*.57f,mfill_paint);
		}
		if(save_finish)
			canvas.drawBitmap(notice_bmp,0,(int)(cur_height*0.48), mnotice_paint);
		
	}
	
	@Override
	protected void onLayout(boolean changed,int l,int t,int r,int b){
		super.onLayout(changed,l,t,r,b);
		cur_width=getWidth();
		cur_height=getHeight();
		
	
	}
	
	
	// Region - Save Image
	void saveImage(){
		Log.i("STLog","Save Image!!!");
		Bitmap bmp_tosave=Bitmap.createBitmap(cur_width,cur_height,Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(bmp_tosave);
		drawOnCanvas(canvas);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	bmp_tosave.compress(Bitmap.CompressFormat.PNG,100,stream);
    	
		saveImage(stream.toByteArray());
		
	}
	
	void saveImage(byte[] image_data){
        File pictureFile=getOutputMediaFile();
        if(pictureFile==null) Log.i("STLog","Error creating media file, check storage permissions");
        //else 
        Log.i("STLog","Image Path: "+pictureFile.getAbsolutePath());
       
        try{
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(image_data);
            fos.close();
            
            
        }catch(FileNotFoundException e){
            Log.d("STLog","File not found: " + e.getMessage());
        }catch(IOException e){
            Log.d("STLog","Error accessing file: " + e.getMessage());
        }
        
        save_finish=true;
        beginAnimation();
        
        Message msg=Message.obtain(main_handle,100,100,0,pictureFile.getAbsolutePath());
        main_handle.sendMessage(msg);

	}
	
	
	/** Create a File for saving an image */
	private File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Artgital_STApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("STLog", "failed to create directory");
	            return null;
	        }else{
	        	//addPicToGallery(mediaStorageDir.getAbsolutePath());
	        	
	        }
	    }
	    
	    // Create a media file name
	    String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    
	    mediaFile=new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
	    
	    
	    
	    return mediaFile;
	    
	}
	public void reset(){
		beginAnimation();
	}
	private void beginAnimation(){
		
		alpha_notice=0;
		mnotice_paint.setAlpha((int)alpha_notice);
		invalidate();
		
		alpha_in_finish=false;
        fadein_animator.start();
	}
	
	@Override
	public void onAnimationUpdate(ValueAnimator animator) {
		
		
		
		alpha_notice=(Float)animator.getAnimatedValue();
		mnotice_paint.setAlpha((int)alpha_notice);
		invalidate();
		
		
	}
	
	// EndRegion
	
	
}
