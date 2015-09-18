package com.rengatartgital.photonandroidclient;

public enum GameEventCode{
	
	
	UCheckId(51),
	UJoin(52),
	
	Game_A_Side(60),
	Game_A_Name(61),
	Game_A_House(62),
	
	Game_A_Blow(63),
	Game_A_Light(64),
	Game_A_Shake(65),
	Game_A_Leave(66),
	
	Game_B_Rotate(71),
	
	Game_C_Face(81),
	
	
	Server_Login_Success(150),
	Server_Id_Game_Info(151),
	Server_Join_Success(152),
	Server_GG(153),
	Server_Change_Game(154),
	
	Server_Set_Side_Success(160),
	Server_Name_Success(161),
	Server_House_Success(162),
	Server_Leave_Success(163),
	
	Server_GameB_Ready(171),
	Server_GameB_Start(172),
	Server_GameB_Eat(173),
	
	Server_Face_Success(181),
	
	Server_Connected(191),
	Server_Disconnected(192); //only used locally
	
	
	private final int value;
	GameEventCode(int value){ this.value=value; }
	public int getGameEvent(){ return value; }
	
	public static GameEventCode fromInt(int x){
		
		x=x&0xFF;
		
		switch(x){
			case 51:
				return UCheckId;
			case 52:
				return UJoin;
			case 60:
				return Game_A_Side;
			case 61:
				return Game_A_Name;
			case 62:
				return Game_A_House;
			case 63:
				return Game_A_Blow;
			case 64:
				return Game_A_Light;
			case 65:
				return Game_A_Shake;
			case 66:
				return Game_A_Leave;
			
			case 71:
				return Game_B_Rotate;
			
			case 81:
				return Game_C_Face;
				
			case 150:
				return Server_Login_Success;
			case 151:
				return Server_Id_Game_Info;
			case 152:
				return Server_Join_Success;
			case 153:
				return Server_GG;
			case 154:
				return Server_Change_Game;

			
			case 160:
				return Server_Set_Side_Success;
			case 161:
				return Server_Name_Success;
			case 162:
				return Server_House_Success;
			case 163:
				return Server_Leave_Success;
				
			case 171:
				return Server_GameB_Ready;
			case 172:
				return Server_GameB_Start;
			case 173:
				return Server_GameB_Eat;
				
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
