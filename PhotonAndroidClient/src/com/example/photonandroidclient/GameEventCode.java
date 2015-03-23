package com.example.photonandroidclient;

public enum GameEventCode{
	
	
//	ULogIn(51),
	UJoin(51),
	
	Game_A_Name(61),
	Game_A_Blow(62),
	
	Game_B_Rotate(71),
	
	Game_C_Face(81),
	
	Server_Game_Info(151),
	Server_Id(152),
	Server_GG(153),
	Server_Join_Success(154),
	
	Server_Name_Success(161),
	Server_GameB_Start(171),
	Server_Face_Success(181),
	
	Server_Connected(191),
	Server_Disconnected(192); //only used locally
	
	
	private final int value;
	GameEventCode(int value){ this.value=value; }
	public int getGameEvent(){ return value; }
	
	public static GameEventCode fromInt(int x){
		
		x=x&0xFF;
		
		switch(x){
//			case 51:
//				return ULogIn;
			case 51:
				return UJoin;
			case 61:
				return Game_A_Name;
			case 62:
				return Game_A_Blow;
			case 71:
				return Game_B_Rotate;
			case 81:
				return Game_C_Face;
			case 151:
				return Server_Game_Info;
			case 152:
				return Server_Id;
			case 153:
				return Server_GG;
			case 154:
				return Server_Join_Success;
			case 161:
				return Server_Name_Success;
			case 171:
				return Server_GameB_Start;
			case 181:
				return Server_Face_Success;
			case 191:
				return Server_Connected;
			case 192:
				return Server_Disconnected;
				
			 
		}
		return null;
	}
	public int getValue(){
		return value;
	}
	
}
