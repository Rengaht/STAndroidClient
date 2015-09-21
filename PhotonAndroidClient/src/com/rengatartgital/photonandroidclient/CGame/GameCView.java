package com.rengatartgital.photonandroidclient.CGame;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.rengatartgital.photonandroidclient.GameEventCode;
import com.rengatartgital.photonandroidclient.R;
import com.rengatartgital.photonandroidclient.R.dimen;
import com.rengatartgital.photonandroidclient.R.drawable;
import com.rengatartgital.photonandroidclient.R.layout;
import com.rengatartgital.photonandroidclient.ViewUtil.BaseGameView;
import com.rengatartgital.photonandroidclient.ViewUtil.FinishImageView;
import com.rengatartgital.photonandroidclient.ViewUtil.ImageDecodeHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.LayoutHelper;
import com.rengatartgital.photonandroidclient.ViewUtil.SVProgressHUD;
import com.rengatartgital.photonandroidclient.ViewUtil.AutoResizeTextView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.exitgames.client.photon.TypedHashMap;

public class GameCView extends BaseGameView{
	
	final int MAVATAR=12;
	
	
	private enum GameState {None,Claim,Take_Photo,Adjust_Face,Preview,GameC_End}
	protected static final String TAG = null;;
	GameState game_state;
	
	private Camera camera;
	private CameraPreview cam_preview;
	static boolean is_front_cam;
	
	//private byte[] byte_saved_img;
	
	ImageButton button_agree,button_disagree;
	Button button_yes,button_no,button_home,button_upload;
	ImageView img_grass_left,img_grass_right;
	
	ScaleSeekBar seekbar_adjust;
	
	
	ImageView img_back,img_page1,img_picture_frame,img_photo;
	ImageView img_avatar;//,img_avface;
	FinishImageView img_finish;
	
	FrameLayout camera_frame;
	Rect frame_rect;
	
	Bitmap photo_mask,saved_bitmap;
	Bitmap photo_bmp,avatar_bmp;
	
	int iavatar;
	
	float adjust_pos_x,adjust_pos_y;
	float adjust_orig_x,adjust_orig_y;
	float adjust_scale,adjust_orig_scale;
	
	
	boolean camera_initiated=false;
	
	public GameCView(Context context) {
		super(context);
		setupView(context);
	}
	public GameCView(Context context, AttributeSet attrs) {
		super(context,attrs);
		setupView(context);
	}
	public GameCView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setupView(context);
	}
	private void setupView(Context context){
		
//		Log.i("STLog","Game C SetUp!!");
		
		
		LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.game_c_layout,this,true);
		
		
//		inflate(getContext(), R.layout.game_c_layout, this);
		
		img_back=(ImageView)getChildAt(0);
		img_page1=(ImageView)getChildAt(1);
		img_photo=(ImageView)getChildAt(2);
		
		camera_frame=(FrameLayout)getChildAt(3);
		
		img_picture_frame=(ImageView)getChildAt(4);
		
		seekbar_adjust=(ScaleSeekBar)getChildAt(5);
		
		
		button_agree=(ImageButton)getChildAt(6);
		button_disagree=(ImageButton)getChildAt(7);

		img_finish=(FinishImageView)getChildAt(8);
		
		
		img_grass_left=(ImageView)getChildAt(9);
		img_grass_right=(ImageView)getChildAt(10);
		
		button_yes=(Button)getChildAt(11);
		button_no=(Button)getChildAt(12);
		
		button_upload=(Button)getChildAt(13);
		button_home=(Button)getChildAt(14);
		
		
		//img_avface=(ImageView)getChildAt(11);
		img_avatar=(ImageView)getChildAt(15);
		
		//send_button=(Button)getChildAt(0);
		//game_over_view=(ImageView)getChildAt(1);
		
		guide_view=(AutoResizeTextView)getChildAt(16);
		setupGuideText();
		
		
		
		button_yes.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				switch(game_state){						
					case Take_Photo:
						if(camera!=null){
							camera.takePicture(null, null, mPicture);

							updateGameState(GameState.Adjust_Face);
						}
						main_activity.playShutterSound();
						break;						
					case Adjust_Face:			
						updateGameState(GameState.Preview);
						main_activity.playButtonSound();
						break;
					default:
						break;
				}
				
			}        	
	    });
		
		button_upload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				main_activity.playButtonSound();
				switch(game_state){						
					case Preview:
						Bitmap face_bmp=createFaceBitmap(saved_bitmap);
						byte[] byte_saved_img=createUploadPhoto(face_bmp);
						
						uploadImage(byte_saved_img);
						face_bmp.recycle();

						//updateGameState(GameState.GameC_End);
						break;
					default:
						break;						
				}
			}
		});
		button_no.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				switch(game_state){						
					case Take_Photo:
						updateGameState(GameState.Claim);							
						break;						
					case Adjust_Face:			
						updateGameState(GameState.Take_Photo);
						break;
					case Preview:
						updateGameState(GameState.Adjust_Face);
						break;
//					case GameC_End:
//						Message msg=Message.obtain(main_activity.handler,100,101,0,null);
//				        main_activity.handler.sendMessage(msg);
//						break;
					default:
						break;
				}
				main_activity.playButtonSound();
			}        	
	    });
		button_home.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				switch(game_state){						
					case GameC_End:
						Message msg=Message.obtain(main_activity.handler,100,101,0,null);
				        main_activity.handler.sendMessage(msg);
						break;
					default:
						break;
				}
				main_activity.playButtonSound();
			}
		});
		
		button_agree.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				updateGameState(GameState.Take_Photo);
				main_activity.playButtonSound();
			}
		});
		button_disagree.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				//TODO: go back!
				Message msg=Message.obtain(main_activity.handler,100,101,0,null);
		        main_activity.handler.sendMessage(msg);
		        main_activity.playButtonSound();
			}
		});
		
		
		photo_mask=BitmapFactory.decodeResource(context.getResources(),R.drawable.gamec_mask_2);
		img_picture_frame.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View arg0, MotionEvent event){
				
				if(game_state!=GameState.Adjust_Face) return false;
				
				float act_x=event.getX();
	        	float act_y=event.getY();
	            
				
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						adjust_orig_x=act_x;
						adjust_orig_y=act_y;
		                return true;
		        	    
	                case MotionEvent.ACTION_MOVE:
	                	upadteAdjustPosition(act_x-adjust_orig_x,act_y-adjust_orig_y);
	                	adjust_orig_x=act_x;
						adjust_orig_y=act_y;
	                    return true;
	            }
	            return false;
				
			}
			
		});
		
		
		seekbar_adjust.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View view, MotionEvent event){
				

				if(game_state!=GameState.Adjust_Face) return false;
				
				float act_x=event.getX();
	        	
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						adjust_orig_scale=act_x;
						//Log.i("STLayout","seekbar touched!");
						return true;
		        	    
	                case MotionEvent.ACTION_MOVE:
	                	updateAdjustScale(act_x-adjust_orig_scale);
	                	adjust_orig_scale=act_x;
						return true;
	            }
	            return false;
				
			}
			
		});
		
		
		
	}
	
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		
		Resources res=getResources();
		
		frame_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.frame_cx),res.getDimension(R.dimen.frame_cy),
				res.getDimension(R.dimen.frame_width),res.getDimension(R.dimen.frame_height));
		camera_frame.layout(frame_rect.left,frame_rect.top,frame_rect.right,frame_rect.bottom);
		
		
		
		if(!changed) return;
		
		
		Rect full_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b);
		
		img_back.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
		guide_view.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		guide_view.setTextSize(Math.max(full_rect.width()/12,res.getDimension(R.dimen.MIN_TEXT_SIZE)));
		
		//Log.i("STLayout","claim");
		Rect claim_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.claim_cx),res.getDimension(R.dimen.claim_cy),
				res.getDimension(R.dimen.claim_width),res.getDimension(R.dimen.claim_height));
		img_page1.layout(claim_rect.left,claim_rect.top,claim_rect.right,claim_rect.bottom);
		
		img_photo.layout(frame_rect.left,frame_rect.top,frame_rect.right,frame_rect.bottom);
		
		Rect out_frame_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.out_frame_cx),res.getDimension(R.dimen.out_frame_cy),
				res.getDimension(R.dimen.out_frame_width),res.getDimension(R.dimen.out_frame_height));
		img_picture_frame.layout(out_frame_rect.left,out_frame_rect.top,out_frame_rect.right,out_frame_rect.bottom);
		
		
//		Rect avface_rect=this.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.avface_cx),res.getDimension(R.dimen.avface_cy),
//				res.getDimension(R.dimen.avface_width),res.getDimension(R.dimen.avface_height));
//		img_avface.layout(avface_rect.left,avface_rect.top,avface_rect.right,avface_rect.bottom);
		
		//Log.i("STLog",">>> Frame onLayout");
		//camera_frame.layout(frame_rect.left,frame_rect.top,frame_rect.right,frame_rect.bottom);
		//camera_frame.layout(0,0,camera_frame.getMeasuredWidth(),camera_frame.getMeasuredHeight());
		
		
		//Log.i("STLayout","agree");
		Rect agree_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.agree_cx),res.getDimension(R.dimen.agree_cy),
				res.getDimension(R.dimen.agree_width),res.getDimension(R.dimen.agree_height));
		button_agree.layout(agree_rect.left,agree_rect.top,agree_rect.right,agree_rect.bottom);
		
		//Log.i("STLayout","disagree");
		Rect disagree_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.disagree_cx),res.getDimension(R.dimen.disagree_cy),
				res.getDimension(R.dimen.disagree_width),res.getDimension(R.dimen.disagree_height));
		button_disagree.layout(disagree_rect.left,disagree_rect.top,disagree_rect.right,disagree_rect.bottom);
		
		
		Rect grass_left_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.grasss_left_cx),res.getDimension(R.dimen.grasss_left_cy),
				res.getDimension(R.dimen.grasss_width),res.getDimension(R.dimen.grasss_height));
		img_grass_left.layout(0,grass_left_rect.top,grass_left_rect.width(),grass_left_rect.bottom);
		
		Rect grass_right_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.grasss_right_cx),res.getDimension(R.dimen.grasss_right_cy),
				res.getDimension(R.dimen.grasss_width),res.getDimension(R.dimen.grasss_height));
		img_grass_right.layout(r-grass_right_rect.width(),grass_right_rect.top,r,grass_right_rect.bottom);
		
		
		Rect yes_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.yes_cx),res.getDimension(R.dimen.yes_cy),
				res.getDimension(R.dimen.yes_width),res.getDimension(R.dimen.yes_height));
		button_yes.layout(yes_rect.left,yes_rect.top,yes_rect.right,yes_rect.bottom);
		
		
		Rect no_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.no_cx),res.getDimension(R.dimen.no_cy),
				res.getDimension(R.dimen.no_width),res.getDimension(R.dimen.no_height));
		button_no.layout(no_rect.left,no_rect.top,no_rect.right,no_rect.bottom);
		
		Rect upload_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.upload_cx),res.getDimension(R.dimen.upload_cy),
				res.getDimension(R.dimen.upload_width),res.getDimension(R.dimen.upload_height));
		button_upload.layout(upload_rect.left,upload_rect.top,upload_rect.right,upload_rect.bottom);
	
		
		Rect avatar_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.avatar_cx),res.getDimension(R.dimen.avatar_cy),
				res.getDimension(R.dimen.avatar_width),res.getDimension(R.dimen.avatar_height));
		img_avatar.layout(avatar_rect.left,avatar_rect.top,avatar_rect.right,avatar_rect.bottom);
		
		//send_button=(Button)getChildAt(0);
		//game_over_view=(ImageView)getChildAt(1);
		
		Rect bar_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.scalebar_cx),res.getDimension(R.dimen.scalebar_cy),
				res.getDimension(R.dimen.scalebar_width),res.getDimension(R.dimen.scalebar_height));
		seekbar_adjust.layout(bar_rect.left,bar_rect.top,bar_rect.right,bar_rect.bottom);
		

		img_finish.layout(full_rect.left, full_rect.top, full_rect.right, full_rect.bottom);
		
		Rect home_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.home_cx),res.getDimension(R.dimen.home_cy),
				res.getDimension(R.dimen.home_width),res.getDimension(R.dimen.home_height));
		button_home.layout(home_rect.left,home_rect.top,home_rect.right,home_rect.bottom);
		
	}
	
	
	@Override
	public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte, Object> params){
		switch(action_code){
			case Server_Face_Success:
				if(main_activity.EnableLog) Log.i("STLog","Send Face Success");
				boolean success=((Integer)params.get((byte)1)==1);
				if(success){
					updateGameState(GameState.GameC_End);
				}else{
					Message msg=Message.obtain(main_activity.handler,100,999,0,null);
			        main_activity.handler.sendMessage(msg);
				}
				//End();
				break;
			case Server_GG:
				if(main_activity.EnableLog) Log.i("STLog","Game C GG");

				Message msg=Message.obtain(main_activity.handler,100,200,0,null);
				main_activity.handler.sendMessage(msg);
				break;
			default:
				break;	
		}
	}

	
	public void updateGameState(GameState set_state) {
		game_state=set_state;
		
		int mchild=this.getChildCount();
		for(int i=1;i<mchild;++i) this.getChildAt(i).setVisibility(View.INVISIBLE);
		
		
		switch(game_state){
			case Claim:
				guide_view.setVisibility(View.VISIBLE);
				
				img_page1.setVisibility(View.VISIBLE);
				button_agree.setVisibility(View.VISIBLE);
				button_disagree.setVisibility(View.VISIBLE);
				break;
			case Take_Photo:
				//if(camera==null) 
				initCamera();
				//clear adjust
				updatePhotoView(null);
				
				img_picture_frame.setVisibility(View.VISIBLE);
				camera_frame.setVisibility(View.VISIBLE);
//				button_camera.setVisibility(View.VISIBLE);
				button_yes.setVisibility(View.VISIBLE);
				button_yes.setBackgroundResource(R.drawable.gamec_camera);
				
				button_no.setVisibility(View.VISIBLE);
				img_grass_left.setVisibility(View.VISIBLE);
				img_grass_right.setVisibility(View.VISIBLE);
				
				adjust_scale=1;
				adjust_pos_x=adjust_pos_y=adjust_orig_x=adjust_orig_y=0;
				
				break;
			case Adjust_Face:
				//stopCamera();
				//drawBitmap();
				
				
				img_picture_frame.setVisibility(View.VISIBLE);
				img_photo.setVisibility(View.VISIBLE);
				
				button_yes.setVisibility(View.VISIBLE);
				button_yes.setBackgroundResource(R.drawable.gamec_yes);
				
				button_no.setVisibility(View.VISIBLE);
				img_grass_left.setVisibility(View.VISIBLE);
				img_grass_right.setVisibility(View.VISIBLE);
				
				
				seekbar_adjust.setVisibility(View.VISIBLE);
				seekbar_adjust.resetPosition(.5f);
				break;
			case Preview:
				//drawAvatarFace(saved_bitmap);
				avatar_bmp=createAvatarBitmap(saved_bitmap,iavatar,img_avatar.getWidth(),img_avatar.getHeight());
		        img_avatar.setImageBitmap(avatar_bmp); 
				
		        //img_avface.setVisibility(View.VISIBLE);
				img_avatar.setVisibility(View.VISIBLE);
				img_grass_left.setVisibility(View.VISIBLE);
				img_grass_right.setVisibility(View.VISIBLE);
				
				button_upload.setVisibility(View.VISIBLE);
				//button_yes.setBackgroundResource(R.drawable.gamec_upload);
				
				button_no.setVisibility(View.VISIBLE);
				break;
			case GameC_End:
				//Bitmap favatar_bmp=createAvatarBitmap(saved_bitmap,iavatar,img_avatar.getWidth(),img_avatar.getHeight());
			   showFinishView();
				
				break;
		}
	}
	private void showFinishView(){

		img_finish.setup(2,avatar_bmp,main_activity.handler);

		img_finish.setVisibility(View.VISIBLE);

		button_home.setVisibility(View.VISIBLE);

		invalidate();

	}
	private void upadteAdjustPosition(float set_delat_x,float set_delta_y){
	   adjust_pos_x+=set_delat_x;
       adjust_pos_y+=set_delta_y;
       updatePhotoView(saved_bitmap);
	}
	private void updateAdjustScale(float set_scale){
		
		adjust_scale=seekbar_adjust.setPosition(set_scale);
		
		updatePhotoView(saved_bitmap);
	}
	private void updatePhotoView(Bitmap orig_bitmap){
	   
		//if(orig_bitmap==null) return;
		
		try{
	       photo_bmp=createPhotoBitmap(orig_bitmap);
	       img_photo.setImageBitmap(photo_bmp);
			
	       this.invalidate();
		}catch(Exception e){
			if(main_activity.EnableLog) Log.i("STLog","PhotoView Exception!!");
			e.printStackTrace();
		}
	}
	private Bitmap createPhotoBitmap(Bitmap draw_bmp){
		
		if(draw_bmp==null){
			if(main_activity.EnableLog) Log.i("STLog","bitmap null!");
			
			Bitmap altered_bitmap = Bitmap.createBitmap(frame_rect.width(),frame_rect.height(),Bitmap.Config.ARGB_8888);
			Canvas canvas=new Canvas(altered_bitmap);
	        Paint paint=new Paint();
	        paint.setFilterBitmap(false);
	        
	        
	       // photo_mask=Bitmap.createScaledBitmap(photo_mask,frame_rect.width(),frame_rect.height(),true);
			
	        canvas.drawARGB(255,0,0,0);
	        
			return altered_bitmap;
		}
		
		if(main_activity.EnableLog) Log.i("STLog","bitmap draw!");
		Bitmap altered_bitmap = Bitmap.createBitmap(draw_bmp.getWidth(),draw_bmp.getHeight(),Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(altered_bitmap);
        Paint paint=new Paint();
        paint.setFilterBitmap(false);
        
        
       // photo_mask=Bitmap.createScaledBitmap(photo_mask,frame_rect.width(),frame_rect.height(),true);
		
        canvas.drawARGB(255,0,0,0);
        if(draw_bmp!=null){
        	
        	
        	canvas.save();
            canvas.translate(adjust_pos_x,adjust_pos_y);
        
	        canvas.translate(draw_bmp.getWidth()/2,draw_bmp.getHeight()/2);
	        canvas.scale(adjust_scale,adjust_scale);
	        canvas.translate(-draw_bmp.getWidth()/2,-draw_bmp.getHeight()/2);
	        
	        	canvas.drawBitmap(draw_bmp,0, 0, paint);
	        
	        canvas.restore();
	   }
        
        return altered_bitmap;
       
	}
	private Bitmap createFaceBitmap(Bitmap draw_bmp){
		
		if(draw_bmp==null){
			if(main_activity.EnableLog) Log.i("STLog","face null!");
			return null;
		}
		
		if(main_activity.EnableLog) Log.i("STLog","face draw!");
		Bitmap altered_bitmap = Bitmap.createBitmap(draw_bmp.getWidth(),draw_bmp.getHeight(),Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(altered_bitmap);
        Paint paint=new Paint();
        paint.setFilterBitmap(false);
        
        canvas.drawARGB(255,0,0,0);
        canvas.save();
        canvas.translate(adjust_pos_x,adjust_pos_y);
        
        canvas.translate(draw_bmp.getWidth()/2,draw_bmp.getHeight()/2);
        canvas.scale(adjust_scale,adjust_scale);
        canvas.translate(-draw_bmp.getWidth()/2,-draw_bmp.getHeight()/2);
        
        	canvas.drawBitmap(draw_bmp, 0, 0, paint);
        canvas.restore();
        
        
    	paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY));
    	canvas.drawBitmap(photo_mask,new Rect(0,0,photo_mask.getWidth(),photo_mask.getHeight()),
	 			 new Rect(0,0,altered_bitmap.getWidth(),altered_bitmap.getHeight()),paint);

    	paint.setXfermode(null);
    	
    	float crop_width=altered_bitmap.getWidth()*.88f;
        Bitmap crop_bmp=Bitmap.createBitmap(altered_bitmap,(int)(altered_bitmap.getWidth()/2-crop_width/2),(int)(altered_bitmap.getHeight()*0.44-crop_width/2),
        									(int)crop_width,(int)crop_width);
        
        altered_bitmap.recycle();
        
    	return crop_bmp;
	
//        img_avface.setImageBitmap(altered_bitmap);    
//        createUploadPhoto(altered_bitmap);
        
//        this.invalidate();

		
	}
	private Bitmap createAvatarBitmap(Bitmap orig_bmp,int index_avatar,int width,int height){
		if(orig_bmp==null){
			if(main_activity.EnableLog) Log.i("STLog","face null!");
			return null;
		}
		
		if(main_activity.EnableLog) Log.i("STLog","face draw!");

//    	Bitmap oimg_avatar_bmp=null;
		int resid=0;
		switch(index_avatar){
			case 0: resid=R.drawable.gamec_man001; break;
			case 1: resid=R.drawable.gamec_woman001; break;
			case 2: resid=R.drawable.gamec_man002; break;
			case 3: resid=R.drawable.gamec_woman002; break;
			case 4: resid=R.drawable.gamec_man003; break;
			case 5: resid=R.drawable.gamec_man004; break;
			case 6: resid=R.drawable.gamec_man005; break;
			case 7: resid=R.drawable.gamec_man006; break;
			case 8: resid=R.drawable.gamec_woman003; break;
			case 9: resid=R.drawable.gamec_woman004; break;
			case 10: resid=R.drawable.gamec_dog; break;
			case 11: resid=R.drawable.gamec_cat; break;
		}

		Bitmap img_avatar_bmp= ImageDecodeHelper.decodeImageToSize(getResources(),resid,width,height);

		Bitmap face_bmp=createFaceBitmap(orig_bmp);
		face_bmp=Bitmap.createScaledBitmap(face_bmp,(int)(width*.69),(int)(width*.69),true);
		
		Bitmap created_bmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(created_bmp);
        Paint paint=new Paint();
        
        
        if(face_bmp!=null) canvas.drawBitmap(face_bmp,(int)(width*0.1446),(int)(height*.088),paint);
		if(img_avatar_bmp!=null) canvas.drawBitmap(img_avatar_bmp,0,0,paint);
		
		
		face_bmp.recycle();
		img_avatar_bmp.recycle();
		
    	return created_bmp; 
	
	
	}
	
	private byte[] createUploadPhoto(Bitmap unscaled_bitmap){

//        float crop_width=altered_bitmap.getWidth()*.88f;
//        Bitmap crop_bmp=Bitmap.createBitmap(altered_bitmap,(int)(altered_bitmap.getWidth()/2-crop_width/2),(int)(altered_bitmap.getHeight()*0.44-crop_width/2),
//        									(int)crop_width,(int)crop_width);
        Bitmap resize_bmp = Bitmap.createScaledBitmap(unscaled_bitmap,104,104, true);
        
//        altered_bitmap.setHasAlpha(true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	resize_bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
    	
    	byte[] abyte=stream.toByteArray();
    	resize_bmp.recycle();
    	try{
    		stream.close();
    	}catch(Exception e){
    		if(main_activity.EnableLog) Log.e("STLog",e.toString());
    	}
    	
    	return abyte;
		
	}

	@Override
	public void Init(){
		
		super.Init();
		
		if(main_activity.EnableLog) Log.i("STLog","Game C Init!");
		updateGameState(GameState.Claim);
		//initCamera();
		
		
		// choose random avatar
		Random r = new Random();
		iavatar=r.nextInt(MAVATAR);

		
	}
	
	@Override
	public void End(){
		super.End();
		game_state=GameState.None;

		stopCamera();
		
		if(main_activity.EnableLog) Log.i("STLog","Game C End!!");

		if(img_finish!=null) img_finish.clear();

		img_photo.setImageResource(0);
		img_avatar.setImageResource(0);

		if(saved_bitmap!=null) saved_bitmap.recycle();
		if(photo_bmp!=null) photo_bmp.recycle();
		//avatar_bmp.recycle();

	}
	
	// Region -- Handle Camera
	
	private void initCamera(){
		if(!checkCameraHardware(getContext())){
			if(main_activity.EnableLog) Log.i("STLog","No camera!!");
			return;
		}else{
			//if(camera==null){
				
			if(main_activity.EnableLog) Log.i("STLog","Init camera!");
				
				camera=getCameraInstance();
				
				if(camera==null){
					if(main_activity.EnableLog) Log.i("STLog","NULL camera!");
					return;
				}
				
				Size optimal_size=getOptimalPreviewSize(camera_frame.getHeight(),camera_frame.getWidth());
				
				try{
					Camera.Parameters parameters=camera.getParameters();
				
					parameters.setPreviewSize(optimal_size.width, optimal_size.height);
					parameters.setPictureSize(optimal_size.width, optimal_size.height);
					camera.setParameters(parameters);
				
				}catch(Exception e){
					if(main_activity.EnableLog) Log.e("STLog",e.toString());
				}
				
				if(camera_frame.getChildCount()>0) camera_frame.removeAllViews();
				
				
				cam_preview=new CameraPreview(getContext(),camera,optimal_size,frame_rect,main_activity.EnableLog);
				camera_frame.addView(cam_preview);
				
				
//			}else{ // camera!=null
//				
//			}
			
		}
	}
	public void stopCamera(){
		if(main_activity.EnableLog) Log.i("STLog","Stop Camera!!");
		if(cam_preview!=null){
			if(camera!=null) camera.stopPreview();
			//camera.setPreviewCallback(null);
			cam_preview.stop();

		}
		if(camera!=null){
            camera.release();        // release the camera for other applications
            camera=null;
        }
		
		if(camera_frame.getChildCount()>0) camera_frame.removeAllViews();
		
	}
	private boolean checkCameraHardware(Context context){
	    if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        return true;
	    }else{
	        return false;
	    }
	}
	private static Camera getCameraInstance(){
	    Camera cam=null;
	    try{
	        //c=Camera.open(); // attempt to get a Camera instance
	    	int cameraCount = 0;
	        
	    	Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	        cameraCount = Camera.getNumberOfCameras();
	        for(int camIdx = 0; camIdx < cameraCount; camIdx++){
	            Camera.getCameraInfo(camIdx, cameraInfo);
	            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
	                try{
	                    cam = Camera.open(camIdx);
	                }catch(RuntimeException e){
	                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
	                }
	            }
	        }
	        if(cam==null){ // if no front camera
	        	//Log.i("STLog","No Front camera available!!");
	            cam = Camera.open();
		        is_front_cam=false;
	        }else{
	        	//Log.i("STLog","Front Camera available!!");
	        	is_front_cam=true;
	        }
	         
		     
		       
	    }catch(Exception e){
	       Log.i("STLog","No camera available!!");
	    }
	    return cam; // returns null if camera is unavailable
	}
	private Size getOptimalPreviewSize(int w, int h) {
		
		List<Size> sizes=camera.getParameters().getSupportedPreviewSizes();
    	final double ASPECT_TOLERANCE=0.1;
    	double targetRatio=(double)w/h;
    	if(sizes==null) return null;
    	 
    	Size optimalSize=null;
    	double minDiff=Double.MAX_VALUE;
    	 
    	int targetHeight=h;
    	 
    	// Try to find an size match aspect ratio and size
    	for(Size size:sizes){
	    	double ratio = (double) size.width / size.height;
	    	if(Math.abs(ratio-targetRatio)>ASPECT_TOLERANCE) continue;
	    	if(Math.abs(size.height-targetHeight)<minDiff){
		    	optimalSize = size;
		    	minDiff = Math.abs(size.height-targetHeight);
	    	}
    	}
    	 
    	// Cannot find the one match the aspect ratio, ignore the requirement
    	if(optimalSize==null){
	    	minDiff=Double.MAX_VALUE;
	    	for(Size size:sizes){
		    	if(Math.abs(size.height-targetHeight)<minDiff){
			    	optimalSize=size;
			    	minDiff=Math.abs(size.height-targetHeight);
		    	}
	    	}
    	}
    	
    	if(main_activity.EnableLog) Log.i("STLog","get optimal size: "+w+" "+h+" -> "+optimalSize.width+" -> "+optimalSize.height);
    	
    	return optimalSize;
    }
	
	private PictureCallback mPicture=new PictureCallback(){

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera){

	        //main_activity.saveImage(data);
	        //saved_img=data;
	        Bitmap orig_bitmap=BitmapFactory.decodeByteArray(data,0,data.length);

	        Matrix matrix=new Matrix();
	        matrix.postRotate(90);
	        if(android.os.Build.VERSION.SDK_INT>13 && is_front_cam){
	            float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
	            matrix=new Matrix();
	            Matrix matrixMirrorY = new Matrix();
	            matrixMirrorY.setValues(mirrorY);

	            matrix.postConcat(matrixMirrorY);

	            matrix.preRotate(270);

	        }
	        
	        saved_bitmap=Bitmap.createBitmap(orig_bitmap,0,0,orig_bitmap.getWidth(),orig_bitmap.getHeight(),matrix,true);
	        
	        orig_bitmap.recycle();
	        
	        if(main_activity.EnableLog) Log.i("STLog","save to bitmap "+saved_bitmap.getWidth()+" x "+saved_bitmap.getHeight());
	        
	        updatePhotoView(saved_bitmap);
	        img_photo.setVisibility(View.VISIBLE);
	        
	        
	        stopCamera();
	    }
	};

	private void uploadImage(byte[] data){
		
		HashMap<Object,Object> params=new HashMap<Object,Object>();
		params.put((byte)1, "name");
		
		//byte[] resized_byte=resizeImage(data);
		
		params.put((byte)2, main_activity.getEncodedImage(data));
		params.put((byte)3, iavatar);
		main_activity.sendEvent(GameEventCode.Game_C_Face,params);
		
		
		
		//End();
	
	}
//	byte[] resizeImage(byte[] input) {
//		
//		if(input.length<1){
//			Log.e("STLog","input byte null");
//			return null;
//		}
//		
//	    Bitmap original = BitmapFactory.decodeByteArray(input,0,input.length);
//	    if(original==null){
//	    	Log.e("STLog","decode null");
//			return null;
//	    }
//	    Bitmap resized = Bitmap.createScaledBitmap(original, original.getWidth()/4,original.getHeight()/4, true);
//	         
//	    Log.i("STLog",original.getByteCount()+" -> "+resized.getByteCount());
//	    
//	    // rotate 90
//	    Matrix matrix = new Matrix();
//	    matrix.postRotate(90);
//	    Bitmap rotatedBitmap = Bitmap.createBitmap(resized,0,0,resized.getWidth(),resized.getHeight(),matrix,true);
//	    
//	    ByteArrayOutputStream blob = new ByteArrayOutputStream();
//	    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);
//	 
//	    return blob.toByteArray();
//	}
	
	// EndRegion
	
	
	@Override
	public void HandleSensor(float[] sensor_value){
		
	}
	@Override
	public boolean isFinish(){
		return game_state==GameState.GameC_End;
	}
}
