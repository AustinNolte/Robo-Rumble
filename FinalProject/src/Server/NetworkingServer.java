package Server;

import java.io.IOException;

import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer 
{
	private GameServerUDP thisUDPServer;
	private NPCcontroller npcCon;

	public NetworkingServer(int serverPort, String protocol) {	
		
		npcCon = new NPCcontroller();

		try{
			thisUDPServer = new GameServerUDP(serverPort,npcCon);
		} catch (IOException e) {	
			e.printStackTrace();
		}
		npcCon.start(thisUDPServer);
	}

	public static void main(String[] args) {	
		if(args.length > 1){	
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
	}
}