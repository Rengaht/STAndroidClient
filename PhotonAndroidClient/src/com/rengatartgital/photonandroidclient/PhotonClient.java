package com.rengatartgital.photonandroidclient;


import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.exitgames.api.loadbalancing.ClientState;
import de.exitgames.api.loadbalancing.EventCode;
import de.exitgames.api.loadbalancing.LoadBalancingClient;
import de.exitgames.api.loadbalancing.LoadBalancingPeer;
import de.exitgames.client.photon.EventData;
import de.exitgames.client.photon.OperationResponse;
import de.exitgames.client.photon.StatusCode;
import de.exitgames.client.photon.TypedHashMap;
import de.exitgames.client.photon.enums.ConnectionProtocol;



public class PhotonClient extends LoadBalancingClient implements Runnable{
	
	static final String LOG_TAG="STConnect";
//	static final String SERVER_IP="kerkerker.artgital.com:5055";
	static final String SERVER_IP="192.168.2.227:5055";
	static final String SERVER_APP="STGameB";
	
    Handler main_handler;
    boolean is_connected=true;
    
	public PhotonClient(Handler handle){
		super();
		main_handler=handle;
	}
	@Override
	public void run(){
		if(this.connect()){
			Log.i(LOG_TAG,"Start Running!");
			while(true){
				
	           	            
				try{
					
					this.loadBalancingPeer.service();
					 
					Thread.sleep(40);
				}catch (Exception e){
					e.printStackTrace();
				}	
				
				if(!is_connected){
					Log.i(LOG_TAG,"Thread End!! Connect in 5s.....");					
					break;
				}
			}
		}else{
			Log.i(LOG_TAG,"Connection Fail!");
		}
		
	}
	public boolean connect(){
		this.loadBalancingPeer=new LoadBalancingPeer(this,ConnectionProtocol.Udp);
		if(this.loadBalancingPeer.connect(SERVER_IP, SERVER_APP)){
			return true;
		}
		return false;
	}
	/**
     * Sends event 1 for testing purposes. Your game would send more useful events.
     */
    public boolean sendSomeEvent(int event_code,HashMap<Object,Object> event_params)
    {
    	Log.i(LOG_TAG,"Send Event: "+event_code);
    	Set<Object> lkey=event_params.keySet();
    	for(Object k:lkey) Log.i(LOG_TAG,k+"->"+event_params.get(k));
    	
        return this.loadBalancingPeer.opRaiseEvent((byte)event_code, event_params, false, (byte)0);       // this is received by OnEvent()
        
    }
    @Override
    public void onStatusChanged(StatusCode statusCode)
    {
        super.onStatusChanged(statusCode);
        
        Log.i(LOG_TAG,"OnStatusChanged: "+statusCode.name());
        
        switch(statusCode){
            case Connect:
            	is_connected=true;
                Log.i(LOG_TAG, "Connect!");
                sendMessageToMain(0,GameEventCode.Server_Connected.getValue(),null);
                break;
            case Disconnect:
            	is_connected=false;
                Log.i(LOG_TAG, "Disconnect!");
                sendMessageToMain(0,GameEventCode.Server_Disconnected.getValue(),null);
                break;
            default:
            	break;
        }
    }
    /**
     * Uses the photonEvent's provided by the server to advance the internal state and call ops as needed.
     * In this demo client, we check for a particular event (1) and count these. After that, we update the view / gui
     * @param eventData
     */
    @Override
    public void onEvent(EventData eventData)
    {
        super.onEvent(eventData);
        
        Log.i(LOG_TAG,"OnEvent: "+eventData.Code);
        
        TypedHashMap<Byte,Object> params=eventData.Parameters;
        GameEventCode event_code=GameEventCode.fromInt(eventData.Code);
        
        sendMessageToMain(2,event_code.getValue(),params);
        
//        switch (eventData.Code)
//        {
//            case (byte)1:
//                this.m_eventCount++;
//            	
//                break;
//            case EventCode.GameList:
//            case EventCode.GameListUpdate:
//                break;
//            case EventCode.PropertiesChanged:
//                break;
//            case EventCode.Join:
//                break;
//            case EventCode.Leave:
//                break;
//        }

       
    }
    @Override
    public void onOperationResponse(OperationResponse operationResponse){
        
        super.onOperationResponse(operationResponse);
        
        Log.i(LOG_TAG,"OnOperationResponse: "+operationResponse.OperationCode);
        
//        TypedHashMap<Byte,Object> params=operationResponse.Parameters;
        GameEventCode event_code=GameEventCode.fromInt(operationResponse.OperationCode);
        
        sendMessageToMain(1,event_code.getValue(),operationResponse.Parameters);
        
//        
//        switch(event_code){
//            case Server_Game_Info:
//                break;
//            default :
//                Log.i(LOG_TAG,"--------------------\nOperation: "+(int)operationResponse.OperationCode);
//                for(Entry<Byte, Object> entry:params.entrySet()){
//                    Log.i(LOG_TAG,entry.getKey()+" -> "+entry.getValue());
//                }
//                break;    
//        }
        
    }
    private void sendMessageToMain(int what,int arg1,Object obj){
    	 Message msg=Message.obtain(main_handler,what,arg1,0, obj);
         main_handler.sendMessage(msg);
    }
}
