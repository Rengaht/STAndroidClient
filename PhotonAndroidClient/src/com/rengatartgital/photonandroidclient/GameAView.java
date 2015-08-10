package com.rengatartgital.photonandroidclient;

import java.util.HashMap;



















import java.util.Timer;
import java.util.TimerTask;

import com.rengatartgital.photonandroidclient.IslandView.IlandMode;
import com.rengatartgital.photonandroidclient.R;

import de.exitgames.client.photon.TypedHashMap;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class GameAView extends BaseGameView implements AnimatorUpdateListener{
	
	private static final int POLL_INTERVAL=800;
	private static final int MSELECTION=4;
	private static final int MHOUSE=5;
	
	
	private static final float SHAKE_THRESHOLD=5;
	private static final int SENSOR_RESOLUTION=30;
	private static final int NAME_MAX_LENGTH=8;
	
//	private Button send_button,left_side_button,right_side_button;
	private EditText name_text;
	
	private enum GameState {SetSide,SetName,SetHouse,SetPart1,SetPart2,SetPart3,SetPart4,SetTrigger,GameA_End};
	GameState game_state,next_state;
	
	ImageView img_back,img_name,img_choose_people,img_step_title;
	Button btn_left,btn_right,btn_next,btn_done,btn_arrow_left,btn_arrow_right;
	IslandView iland_view;
	TextView text_part_title,text_part_score;
	
	FinishImageView img_finish;
	Button btn_home;
	
	ATriggerHintView hint_view;
	
	ACountDownView count_view;
//	TextView notice_view;
	ImageView notice_view;
	
	private String[] arr_part_title;
	
	int itmp_selection;
	int itmp_build,itmp_cat;
	
	private float[] last_sensor_value;
	private int sensor_frame;
	
	boolean lock_trigger,lock_step;
	
	private ValueAnimator anima_score_in,anima_score_out,anima_part_in,anima_part_out;
	
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
		
		
		
		
		img_back=(ImageView)getChildAt(0);
		iland_view=(IslandView)getChildAt(1);
		iland_view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				sendTrigger(GameEventCode.Game_A_Light);
			}
			
		});
		
		
		
		btn_left=(Button)getChildAt(2);
		btn_right=(Button)getChildAt(3);
		
		btn_next=(Button)getChildAt(4);
		btn_done=(Button)getChildAt(5);
		
		img_name=(ImageView)getChildAt(6);
		name_text=(EditText)getChildAt(7);
		
		int maxLength = 8;    
		name_text.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
		name_text.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
				
				/** calculate number of input texts to constrain text length */
				String cur_text=arg0.toString();
				boolean illegal_char=false;
				
				int total_count=0;
				for(int i=0;i<cur_text.length();++i){
					char c=cur_text.charAt(i);

					/** Chinese word as 2, English as 1 */
					if(c>256) total_count+=2;
					else{
						if(c<48 || (c>57 && c<65) || (c>90 && c<97) || (c>122)) illegal_char=true;
						else total_count+=1;
					}
//					Log.i("STLog",c+" #= "+total_count+" "+cur_text.substring(0,i+1));
					
					if(illegal_char){
						//TODO:!!!
						arg0.delete(i,cur_text.length());		
						break;
					}
					
					/** delete overflow texts */
					if(total_count>NAME_MAX_LENGTH){
						arg0.delete(i,cur_text.length());						
						break;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
		});
		Typeface typeface_name=Typeface.createFromAsset(this.getContext().getAssets(),"fonts/combined.otf");
		name_text.setTypeface(typeface_name);
		
		img_choose_people=(ImageView)getChildAt(8);
		
		img_step_title=(ImageView)getChildAt(9);
		text_part_title=(TextView)getChildAt(10);
		text_part_score=(TextView)getChildAt(11);
		
		btn_arrow_left=(Button)getChildAt(12);
		btn_arrow_right=(Button)getChildAt(13);
		
		hint_view=(ATriggerHintView)getChildAt(14);
		count_view=(ACountDownView)getChildAt(15);
		
		
		notice_view=(ImageView)getChildAt(16);
		notice_view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View tview){
				//TODO: fade-out self
				tview.setVisibility(View.INVISIBLE);
//				count_view.start();
			}
			
		});
		
		img_finish=(FinishImageView)getChildAt(17);
		btn_home=(Button)getChildAt(18);
		
		
		guide_view=(TextView)getChildAt(19);
		setupGuideText();
		
		
		
		itmp_selection=0;
		btn_next.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0){
					
					if(!iland_view.animationFinished()) return;
					if(lock_step) return;
					 
					 
					HashMap<Object,Object> params=new HashMap<Object,Object>();
					
					switch(game_state){						
						
						case SetName:
							String str_name=name_text.getText().toString();
							if(str_name.length()<1){ 
								Message msg=Message.obtain(main_activity.handler,100,500,0,null);
						        main_activity.handler.sendMessage(msg);
								return;
							}
							
							String cap_str="";
							for(int i=0;i<str_name.length();++i){
								char _char=str_name.charAt(i);
								if(_char<='z' && _char>='a'){
									_char=Character.toUpperCase(_char);
								}
								cap_str+=_char;
							}
							
							params.put((byte)1, cap_str);
							params.put((byte)2, itmp_selection);
							main_activity.sendEvent(GameEventCode.Game_A_Name,params);
							
							iland_view.setName(cap_str, itmp_selection);
							
							main_activity.playButtonSound();
							
							break;					
						case SetHouse:
//							iland_view.updatePart(itmp_selection,0,itmp_selection);
							itmp_build=itmp_selection;
							Log.i("STLog","set house: "+itmp_build);
//							updateGameState(GameState.SetPart1);
							showPartScore();
							break;
						case SetPart1:
//							iland_view.updatePart(itmp_build,1,itmp_selection);
//							updateGameState(GameState.SetPart2);
							showPartScore();
							break;							
						case SetPart2:
//							iland_view.updatePart(itmp_build,2,itmp_selection);
//							updateGameState(GameState.SetPart3);
							showPartScore();
							break;
						case SetPart3:
//							iland_view.updatePart(itmp_build,3,itmp_selection);
//							updateGameState(GameState.SetPart4);
							showPartScore();
							break;
						case SetPart4:
//							iland_view.updatePart(itmp_build,4,itmp_selection);
//							updateGameState(GameState.SetTrigger);
							showPartScore();
							break;
						
						default:
							break;
					}
					
				}        	
	        });
		btn_done.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 
				 if(!iland_view.animationFinished()) return;
				 if(lock_step) return;
					
				 showPartScore();
//				 main_activity.playSound(8);
//				 HashMap<Object,Object> params=new HashMap<Object,Object>();
//				 int[] ipart=iland_view.arr_ipart;
//				 int mpart=ipart.length;
//				 for(int i=0;i<mpart;++i){
//					 params.put((byte)(i+1),ipart[i]);
//				 }
//				 main_activity.sendEvent(GameEventCode.Game_A_House,params);
			 }
		});
		 
		btn_left.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 main_activity.setSideIndex(1);
				 HashMap<Object,Object> params=new HashMap<Object,Object>();
				 main_activity.sendEvent(GameEventCode.Game_A_Side,params);
				 main_activity.playButtonSound();
			 }
		 });
		 

		 btn_right.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 main_activity.setSideIndex(0);
				 HashMap<Object,Object> params=new HashMap<Object,Object>();
				 main_activity.sendEvent(GameEventCode.Game_A_Side,params);
				 main_activity.playButtonSound();
			 }
		 });
		 
		 
		btn_arrow_left.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 
				if(!iland_view.animationFinished()) return;
				if(lock_step) return;
				
				
				if(itmp_cat==0||itmp_cat==5) updatePartSelection((itmp_selection+MHOUSE-1)%MHOUSE);
				else updatePartSelection((itmp_selection+MSELECTION-1)%MSELECTION);
				main_activity.playSound(9);
			 }
		}); 
		
		btn_arrow_right.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 
				if(!iland_view.animationFinished()) return;
				if(lock_step) return;
				
				if(itmp_cat==0||itmp_cat==5) updatePartSelection((itmp_selection+1)%MHOUSE);
				else updatePartSelection((itmp_selection+1)%MSELECTION);
				main_activity.playSound(9);
			 }
		}); 
		
		btn_home.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				switch(game_state){						
					case GameA_End:
						Message msg=Message.obtain(main_activity.handler,100,101,0,null);
				        main_activity.handler.sendMessage(msg);
						break;
					default:
						break;
				}
				main_activity.playButtonSound();
			}
		});
		
		
		last_sensor_value=new float[3];
		for(int i=0;i<3;++i) last_sensor_value[i]=-1;
		
		
		
		
		anima_score_in=ValueAnimator.ofFloat(0.0f,1.0f);
		anima_score_in.setDuration(100);
		anima_score_in.setStartDelay(100);
		anima_score_in.setInterpolator(new AccelerateDecelerateInterpolator());
		anima_score_in.addUpdateListener(this);
		
		anima_score_out=ValueAnimator.ofFloat(1.0f,0.0f);
		anima_score_out.setDuration(100);
		anima_score_out.setStartDelay(200);
		anima_score_out.setInterpolator(new AccelerateDecelerateInterpolator());
		anima_score_out.addUpdateListener(this);

		
		anima_part_in=ValueAnimator.ofFloat(0,1);
		anima_part_in.addUpdateListener(this);
		anima_part_in.setDuration(300);
		
		anima_part_out=ValueAnimator.ofFloat(1,0);
		anima_part_out.addUpdateListener(this);
		anima_part_out.setStartDelay(300);
		anima_part_out.setDuration(300);
		
		
		lock_trigger=false;
		lock_step=false;
		
	}
	@Override
	public void setMainActivity(MainActivity main_act){
		super.setMainActivity(main_act);
		count_view.setHandler(main_act.handler);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		
		
		if(!changed) return;
		
		Resources res=getResources();
		
		Rect full_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b);	
		
		
		Log.i("STLayout","gameb: "+l+" "+t+" "+r+" "+b);
		Log.i("STLayout","gameb full "+full_rect.left+" "+full_rect.top+" "+full_rect.right+" "+full_rect.bottom);
		
		img_back.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		img_finish.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		
		guide_view.layout(full_rect.left,full_rect.top,full_rect.right,full_rect.bottom);
		guide_view.setTextSize(Math.max(full_rect.width()/12,res.getDimension(R.dimen.MIN_TEXT_SIZE)));
		
		
		Rect name_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aname_title_cx),res.getDimension(R.dimen.aname_title_cy),
				res.getDimension(R.dimen.aname_title_width),res.getDimension(R.dimen.aname_title_height));
		img_name.layout(name_rect.left,name_rect.top,name_rect.right,name_rect.bottom);
		
		Rect rname_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aname_region_cx),res.getDimension(R.dimen.aname_region_cy),
				res.getDimension(R.dimen.aname_region_width),res.getDimension(R.dimen.aname_region_height));
		name_text.layout(rname_rect.left,rname_rect.top,rname_rect.right,rname_rect.bottom);
		
		
		name_text.setTextSize(rname_rect.height()*.5f);
		
		
		Rect choose_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.achoose_people_cx),res.getDimension(R.dimen.achoose_people_cy),
				res.getDimension(R.dimen.achoose_people_width),res.getDimension(R.dimen.achoose_people_height));
		img_choose_people.layout(choose_rect.left,choose_rect.top,choose_rect.right,choose_rect.bottom);
		
		Rect step_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.astep_cx),res.getDimension(R.dimen.astep_cy),
				res.getDimension(R.dimen.astep_width),res.getDimension(R.dimen.astep_height));
		img_step_title.layout(step_rect.left,step_rect.top,step_rect.right,step_rect.bottom);
		
		Rect bleft_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aleft_cx),res.getDimension(R.dimen.aleft_cy),
				res.getDimension(R.dimen.aleft_width),res.getDimension(R.dimen.aleft_height));
		btn_left.layout(bleft_rect.left,bleft_rect.top,bleft_rect.right,bleft_rect.bottom);
		
		Rect bright_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aright_cx),res.getDimension(R.dimen.aright_cy),
				res.getDimension(R.dimen.aright_width),res.getDimension(R.dimen.aright_height));
		btn_right.layout(bright_rect.left,bright_rect.top,bright_rect.right,bright_rect.bottom);
		
		Rect bcircle_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.acircle_btn_cx),res.getDimension(R.dimen.acircle_btn_cy),
				res.getDimension(R.dimen.acircle_btn_width),res.getDimension(R.dimen.acircle_btn_height));
		btn_next.layout(bcircle_rect.left,bcircle_rect.top,bcircle_rect.right,bcircle_rect.bottom);
		btn_done.layout(bcircle_rect.left,bcircle_rect.top,bcircle_rect.right,bcircle_rect.bottom);
		
		Rect aleft_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aleft_arrow_cx),res.getDimension(R.dimen.aleft_arrow_cy),
				res.getDimension(R.dimen.aleft_arrow_width),res.getDimension(R.dimen.aleft_arrow_height));
		btn_arrow_left.layout(aleft_rect.left,aleft_rect.top,aleft_rect.right,aleft_rect.bottom);
		
		Rect aright_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.aright_arrow_cx),res.getDimension(R.dimen.aright_arrow_cy),
				res.getDimension(R.dimen.aright_arrow_width),res.getDimension(R.dimen.aright_arrow_height));
		btn_arrow_right.layout(aright_rect.left,aright_rect.top,aright_rect.right,aright_rect.bottom);
		
		Rect iland_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.ailand_cx),res.getDimension(R.dimen.ailand_cy),
				res.getDimension(R.dimen.ailand_width),res.getDimension(R.dimen.ailand_height));
		iland_view.layout(iland_rect.left,iland_rect.top,iland_rect.right,iland_rect.bottom);
		iland_view.setupBitmap(iland_rect.left,iland_rect.top,iland_rect.right,iland_rect.bottom);
		
		Rect home_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.home_cx),res.getDimension(R.dimen.home_cy),
				res.getDimension(R.dimen.home_width),res.getDimension(R.dimen.home_height));
		btn_home.layout(home_rect.left,home_rect.top,home_rect.right,home_rect.bottom);
		
		
		Rect hint_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.ahint_cx),res.getDimension(R.dimen.ahint_cy),
				res.getDimension(R.dimen.ahint_width),res.getDimension(R.dimen.ahint_height));
		hint_view.layout(hint_rect.left,hint_rect.top,hint_rect.right,hint_rect.bottom);
		hint_view.setupBitmap(hint_rect.left,hint_rect.top,hint_rect.right,hint_rect.bottom);
		
		
		Rect count_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.acount_cx),res.getDimension(R.dimen.acount_cy),
				res.getDimension(R.dimen.acount_width),res.getDimension(R.dimen.acount_height));
		count_view.layout(count_rect.left,count_rect.top,count_rect.right,count_rect.bottom);
		
		Rect notice_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.anotice_cx),res.getDimension(R.dimen.anotice_cy),
				res.getDimension(R.dimen.anotice_width),res.getDimension(R.dimen.anotice_height));
		notice_view.layout(notice_rect.left,notice_rect.top,notice_rect.right,notice_rect.bottom);
//		notice_view.setTextSize(Math.max(notice_rect.width()/15,res.getDimension(R.dimen.MIN_TEXT_SIZE)));
//		notice_view.invalidate();
		
//		int pad=(int)((float)notice_rect.width()*.2f);
//		notice_view.setPadding(pad,pad,pad,pad);
		
		
		Rect text_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.apart_title_cx),res.getDimension(R.dimen.apart_title_cy),
				res.getDimension(R.dimen.apart_title_width),res.getDimension(R.dimen.apart_title_height));
		text_part_title.layout(l,text_rect.top,r,text_rect.bottom);
		text_part_title.setTextSize(Math.max(text_rect.height()*.8f,res.getDimension(R.dimen.MIN_TEXT_SIZE)));
		
		
		Rect score_rect=LayoutHelper.getLayoutCoordinate(l,t,r,b,res.getDimension(R.dimen.apart_score_cx),res.getDimension(R.dimen.apart_score_cy),
				res.getDimension(R.dimen.apart_score_width),res.getDimension(R.dimen.apart_score_height));
		text_part_score.layout(l,score_rect.top,r,score_rect.bottom);
		text_part_score.setTextSize(score_rect.height()*.85f);
		
	}
	@Override
	public void HandleMessage(GameEventCode action_code,TypedHashMap<Byte,Object> params){
		switch(action_code){
		
			case Server_Name_Success:
				int res_status=(Integer)params.get((byte)1);
				if(res_status==1){
					Log.i("STLog","Send Name Success");
//					updateGameState(GameState.SetHouse);
					next_state=GameState.SetHouse;
					goNextState();
				}else if(res_status==2){
					Message msg=Message.obtain(main_activity.handler,100,500,0,null);
			        main_activity.handler.sendMessage(msg);				
				}else{
					Message msg=Message.obtain(main_activity.handler,100,999,0,null);
			        main_activity.handler.sendMessage(msg);
				}
				break;
			case Server_GG:
			case Server_Leave_Success:
				Log.i("STLog","Game A End");
				if(game_state==GameState.SetTrigger) updateGameState(GameState.GameA_End);
				else{ // if not finish,jump to main
					Message msg=Message.obtain(main_activity.handler,100,200,0,null);
			        main_activity.handler.sendMessage(msg);
				}
				break;
			case Server_Set_Side_Success:
				if((Integer)params.get((byte)1)==1){
					updateGameState(GameState.SetName);
					main_activity.setSideIndex((Integer)params.get((byte)101));
					iland_view.setSide((Integer)params.get((byte)101));
				}else{
					Message msg=Message.obtain(main_activity.handler,100,999,0,null);
			        main_activity.handler.sendMessage(msg);
				}
				break;
			case Server_House_Success:
				if((Integer)params.get((byte)1)==1){
					updateGameState(GameState.SetTrigger);
				}else{
					Message msg=Message.obtain(main_activity.handler,100,999,0,null);
			        main_activity.handler.sendMessage(msg);
				}	
				break;
			default:
				break;
		
		}
	}
	
	
	private void showPartScore(){
		String str_score="";
		switch(itmp_cat){
			case 4:
				switch(itmp_selection){
					case 0: str_score="+200"; break;
					case 1: str_score="+250"; break;
					default: str_score="+300"; break;
				}
				break;
			default:
				str_score="+300";
				break;
		}
		text_part_score.setText(str_score);
		
		switch(game_state){
			case SetHouse: next_state=GameState.SetPart1; break;
			case SetPart1: next_state=GameState.SetPart2; break;
			case SetPart2: next_state=GameState.SetPart3; break;
			case SetPart3: next_state=GameState.SetPart4; break;
			case SetPart4: next_state=GameState.SetTrigger; break;
		}
		
		btn_next.setEnabled(false);
		btn_done.setEnabled(false);
		btn_arrow_left.setEnabled(false);
		btn_arrow_right.setEnabled(false);
		
		lock_step=true;
		anima_score_in.start();
	}
	private void goNextState(){
		if(next_state!=null){
			if(next_state==GameState.SetTrigger){
				// send to server
				HashMap<Object,Object> params=new HashMap<Object,Object>();
				 int[] ipart=iland_view.arr_ipart;
				 int mpart=ipart.length;
				 for(int i=0;i<mpart;++i){
					 params.put((byte)(i+1),ipart[i]);
				 }
				 main_activity.sendEvent(GameEventCode.Game_A_House,params);
			}else{
				
				updateGameState(next_state);
				anima_part_in.start();
				
			}
			
			btn_next.setEnabled(true);
			btn_done.setEnabled(true);
			
		}
	}
	public void updateGameState(GameState set_state){
		
		Log.i("STLog","--- Game A State: "+set_state.toString());
		
		
		int mchild=this.getChildCount();
		for(int i=0;i<mchild;++i) this.getChildAt(i).setVisibility(View.INVISIBLE);

		img_back.setVisibility(View.VISIBLE);
		iland_view.setVisibility(View.VISIBLE);
		
		
		
		switch(set_state){
			case SetSide:
				guide_view.setVisibility(View.VISIBLE);
				
				btn_left.setVisibility(View.VISIBLE);
				btn_right.setVisibility(View.VISIBLE);
				break;
			case SetName:
				name_text.setVisibility(View.VISIBLE);
				name_text.setText("");
				
				img_choose_people.setVisibility(View.VISIBLE);
				img_name.setVisibility(View.VISIBLE);
				
				btn_next.setVisibility(View.VISIBLE);
				
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				
				initPartSelection(5);
				
				break;
			case SetHouse:
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				btn_next.setVisibility(View.VISIBLE);
				
				text_part_title.setVisibility(View.VISIBLE);
				text_part_score.setVisibility(View.VISIBLE);
				
				img_step_title.setImageDrawable(getResources().getDrawable(R.drawable.gamea_step1));
				img_step_title.setVisibility(View.VISIBLE);
				
				initPartSelection(0);
				break;
			case SetPart1:
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				btn_next.setVisibility(View.VISIBLE);
				
				text_part_title.setVisibility(View.VISIBLE);
				text_part_score.setVisibility(View.VISIBLE);
				
				img_step_title.setImageDrawable(getResources().getDrawable(R.drawable.gamea_step2));
				img_step_title.setVisibility(View.VISIBLE);
				
				initPartSelection(1);
				break;
			case SetPart2:
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				btn_next.setVisibility(View.VISIBLE);
				
				text_part_title.setVisibility(View.VISIBLE);
				text_part_score.setVisibility(View.VISIBLE);
				
				img_step_title.setImageDrawable(getResources().getDrawable(R.drawable.gamea_step3));
				img_step_title.setVisibility(View.VISIBLE);
				
				initPartSelection(2);
				break;
			case SetPart3:
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				btn_next.setVisibility(View.VISIBLE);
				
				text_part_title.setVisibility(View.VISIBLE);
				text_part_score.setVisibility(View.VISIBLE);
				
				img_step_title.setImageDrawable(getResources().getDrawable(R.drawable.gamea_step4));
				img_step_title.setVisibility(View.VISIBLE);
				
				initPartSelection(3);
				break;
			case SetPart4:
				btn_arrow_left.setVisibility(View.VISIBLE);
				btn_arrow_right.setVisibility(View.VISIBLE);
				btn_done.setVisibility(View.VISIBLE);
				
				img_step_title.setImageDrawable(getResources().getDrawable(R.drawable.gamea_step5));
				img_step_title.setVisibility(View.VISIBLE);
				
				text_part_score.setVisibility(View.VISIBLE);
				
				text_part_title.setVisibility(View.VISIBLE);
				
				initPartSelection(4);
				break;
			case SetTrigger:
				
				hint_view.setVisibility(View.VISIBLE);
				count_view.setVisibility(View.VISIBLE);
				
				notice_view.setVisibility(View.VISIBLE);
//				notice_view.setText(getResources().getString(R.string.text_gamea_notice));
				
				iland_view.setIlandMode(IlandMode.FINAL);
				startBlowSensor();
				
				count_view.start();
				
				break;
				
			case GameA_End:
				showFinishView();
				End();
				break;
				
			default:
				break;
		}
		
		game_state=set_state;
		
		
	}
	private void initPartSelection(int set_cat){
		
		text_part_score.setAlpha(0);
		anima_score_in.cancel();
		next_state=null;
		
		itmp_cat=set_cat;
		
		arr_part_title=null;
		switch(itmp_cat){
			case 0:
				arr_part_title=this.getResources().getStringArray(R.array._ahouse_title);
				break;
			case 1:
				arr_part_title=this.getResources().getStringArray(R.array._apart1_title);
				break;
			case 2:
				arr_part_title=this.getResources().getStringArray(R.array._apart2_title);
				break;
			case 3:
				arr_part_title=this.getResources().getStringArray(R.array._apart3_title);
				break;
			case 4:
				arr_part_title=this.getResources().getStringArray(R.array._apart4_title);
				break;
		}
		
		updatePartSelection(0);
	}
	private void updatePartSelection(int set_sel){
		
		itmp_selection=set_sel;
		
		if(arr_part_title!=null && arr_part_title[itmp_selection]!=null){
			text_part_title.setText(arr_part_title[itmp_selection]);
		}
		
		if(itmp_cat==0 || itmp_cat==5){
			 iland_view.updatePart(itmp_selection,itmp_cat,itmp_selection);
		}else{
			 iland_view.updatePart(itmp_build,itmp_cat,itmp_selection);
		}
	}
	private void showFinishView(){
		
		float wid=this.getWidth()*.7f;
		Bitmap build_bmp=Bitmap.createScaledBitmap(iland_view.mbmp,(int)wid,(int)(wid*1.407f),true);
		img_finish.setup(0,build_bmp,main_activity.handler);
		img_finish.setVisibility(View.VISIBLE);
		
		btn_home.setVisibility(View.VISIBLE);
		
		invalidate();
	}


	@Override
	public void Init() {
		super.Init();
		updateGameState(GameState.SetSide);
		iland_view.reset();
		iland_view.setHandler(main_activity.handler);
		
		for(int i=0;i<3;++i) last_sensor_value[i]=0;
		
		lock_step=false;
		btn_arrow_left.setEnabled(true);
		btn_arrow_right.setEnabled(true);
		
	}
	@Override
	public void End() {
		super.End();
		blow_handler.removeCallbacks(pollTask);
		if(blow_sensor!=null) blow_sensor.Stop();
		

	}
	
	// Region -- Handle Blow Sensor
	private void startBlowSensor(){
		
		if(blow_sensor==null) blow_sensor=new BlowSensor();
		
		blow_sensor.Start();		
		
		blow_handler.postDelayed(pollTask,POLL_INTERVAL);
	}
	
	private void updateBlowStatus(){
//		Log.i("STLog","!!! Blow Detected !!!");
		
		sendTrigger(GameEventCode.Game_A_Blow);
	}
	
	// EndRegion
	
	@Override
	public void HandleSensor(float[] sensor_value){
		
		
		if(game_state!=GameState.SetTrigger) return;
		
		if(last_sensor_value[0]==-1 && last_sensor_value[1]==-1 && last_sensor_value[2]==-1){
			for(int i=0;i<3;++i) last_sensor_value[i]=sensor_value[i];
			return;
		}
		
		sensor_frame=(sensor_frame+1)%SENSOR_RESOLUTION;
		if(sensor_frame!=0) return;
		
		Log.i("Sensor","strength= "+sensor_value[0]+","+sensor_value[1]+","+sensor_value[2]);
		
		float delta_strength=0;
		for(int i=0;i<3;++i) delta_strength+=Math.abs(sensor_value[i]-last_sensor_value[i]);
		Log.i("Sensor","delta_strength= "+delta_strength);
		
		
		if(delta_strength>SHAKE_THRESHOLD){
			sendTrigger(GameEventCode.Game_A_Shake);
			sensor_frame=0;
		}
		
		for(int i=0;i<3;++i) last_sensor_value[i]=sensor_value[i];
		
	}
	
	private void sendTrigger(GameEventCode ev_code){
		
		
		if(game_state!=GameState.SetTrigger) return;
		if(lock_trigger) return;
		if(main_activity.icur_game!=0) return;
		
    	
		main_activity.sendEvent(ev_code,new HashMap<Object,Object>());
		switch(ev_code){
			case Game_A_Light:
				if(iland_view.arr_ipart[3]>1) main_activity.playSound(11);
				else main_activity.playSound(10);
				break;
			case Game_A_Blow:
				if(iland_view.arr_ipart[2]>1) main_activity.playSound(13);
				else main_activity.playSound(11);
				break;
			case Game_A_Shake:
				main_activity.playSound(12);
				break;	
		}
		
		
		lock_trigger=true;
		Timer timer=new Timer();
    	TimerTask task=new TimerTask(){
			@Override
			public void run(){
				lock_trigger=false;
			}
    	};
    	timer.schedule(task, 3000);
    	
	}
	
	
	@Override
	public void onAnimationUpdate(ValueAnimator animator){
		
		if(animator.equals(anima_score_in)){
			
			text_part_score.setAlpha((Float)animator.getAnimatedValue());
			if(animator.getAnimatedFraction()==1){
				anima_score_out.start();
				main_activity.playSound(7);
			}
			
		}else if(animator.equals(anima_score_out)){
			
			text_part_score.setAlpha((Float)animator.getAnimatedValue());
			if(animator.getAnimatedFraction()==1){
				//fade out part
				anima_part_out.start();	
			}
		}else if(animator.equals(anima_part_in)){
			
			float _val=(Float)animator.getAnimatedValue();
//			text_part_score.setAlpha(_val);
			text_part_title.setAlpha(_val);
			img_step_title.setAlpha(_val);
			
			// fade in part!!
			
			if(animator.getAnimatedFraction()==1){
				//go next_state
//				goNextState();
				lock_step=false;
				btn_arrow_left.setEnabled(true);
				btn_arrow_right.setEnabled(true);
			}
			
		}else if(animator.equals(anima_part_out)){
			float _val=(Float)animator.getAnimatedValue();
			text_part_title.setAlpha(_val);
			img_step_title.setAlpha(_val);
			
			if(animator.getAnimatedFraction()==1){
				//go next_state
				goNextState();
			}
		}
		
		postInvalidate();
	}
	@Override
	public boolean isFinish(){
		return game_state==GameState.GameA_End;
	}
}
