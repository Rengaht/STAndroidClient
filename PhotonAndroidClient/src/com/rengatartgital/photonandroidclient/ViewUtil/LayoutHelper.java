package com.rengatartgital.photonandroidclient.ViewUtil;

import android.graphics.Rect;
import android.util.Log;

public class LayoutHelper {
	static public Rect getLayoutCoordinate(int pleft,int ptop,float wwid2,float whei2,float lx,float ly,float lwidth,float lheight){
		
		float wwid=wwid2-pleft;
		float whei=whei2-ptop;
		
		float cur_scale=(float)wwid/1080.0f;
		boolean y_align=false;
		if((float)whei/1920.0f<cur_scale){
			cur_scale=(float)whei/1920.0f;
			y_align=true;
			//wwid=(int)(whei/1.778f);
		}else{
			//whei=(int)(wwid*1.778f);
		}
		
		float nwidth=lwidth/100*(y_align?whei/1.778f:wwid);
		float nleft=pleft+(lx/100)*wwid-nwidth/2.0f;
		float nright=nleft+nwidth;
		
		float nheight=lheight/100*(y_align?whei:wwid*1.778f);
		float ntop=ptop+(ly/100)*whei-nheight/2.0f;
		float nbottom=ntop+nheight;
		
//		Log.i("STLayout",pleft+" "+ptop+" "+wwid2+" "+whei2+" -> "+lx+" "+ly+" "+lwidth+" "+lheight);
//		Log.i("STLayout"," => "+nleft+" "+ntop+" "+nright+" "+nbottom);
		
		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
		
	}
	static public Rect getLandscapeLayoutCoordinate(int pleft,int ptop,float wwid2,float whei2,float lx,float ly,float lwidth,float lheight){
		
		float wwid=wwid2-pleft;
		float whei=whei2-ptop;
		
		float cur_scale=(float)wwid/1920.0f;
		boolean y_align=false;
		if((float)whei/1080.0f<cur_scale){
			cur_scale=(float)whei/1080.0f;
			y_align=true;
			//wwid=(int)(whei/1.778f);
		}else{
			//whei=(int)(wwid*1.778f);
		}
		
		float nwidth=lwidth/100*(y_align?whei*1.778f:wwid);
		float nleft=pleft+(lx/100)*wwid-nwidth/2.0f;
		float nright=nleft+nwidth;
		
		float nheight=lheight/100*(y_align?whei:wwid/1.778f);
		float ntop=ptop+(ly/100)*whei-nheight/2.0f;
		float nbottom=ntop+nheight;
		
//		Log.i("STLayout",pleft+" "+ptop+" "+wwid2+" "+whei2+" -> "+lx+" "+ly+" "+lwidth+" "+lheight);
//		Log.i("STLayout"," => "+nleft+" "+ntop+" "+nright+" "+nbottom);
		
		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
		
	}
	static public Rect getLayoutCoordinate(int pleft,int ptop,int pright,int pbottom){
		
		int wwid=pright-pleft;
		int whei=pbottom-ptop;
		float wratio=(float)wwid/(float)whei;
		
		float cur_wid=wwid;
		float cur_hei=whei;
		
		if(wratio>0.5625) cur_hei=(int)(cur_wid/0.5625);
		else cur_wid=(int)(cur_hei*0.5625);
		
		
		float nleft=pleft+(wwid-cur_wid)/2;
		float nright=nleft+cur_wid;
		
		float ntop=ptop+(whei-cur_hei)/2;
		float nbottom=ntop+cur_hei;
		
		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
		
	}
	static public Rect getLandscapeLayoutCoordinate(int pleft,int ptop,int pright,int pbottom){
		
		int wwid=pright-pleft;
		int whei=pbottom-ptop;
		float wratio=(float)wwid/(float)whei;
		
		float cur_wid=wwid;
		float cur_hei=whei;
		
		if(wratio<0.5625) cur_hei=(int)(cur_wid*0.5625);
		else cur_wid=(int)(cur_hei/0.5625);
		
		
		float nleft=pleft+(wwid-cur_wid)/2;
		float nright=nleft+cur_wid;
		
		float ntop=ptop+(whei-cur_hei)/2;
		float nbottom=ntop+cur_hei;
		
		return new Rect((int)nleft,(int)ntop,(int)nright,(int)nbottom);
		
	}
}
