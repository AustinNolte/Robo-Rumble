package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;


public class GameServerUDP extends GameConnectionServer<UUID> {
	
	// npc controller
	private NPCcontroller npcCon;

	public GameServerUDP(int localPort, NPCcontroller npcCon) throws IOException {	
		super(localPort, ProtocolType.UDP);
		this.npcCon = npcCon;
	}

	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort){
		String msg = (String)o;
		String[] msgTokens = msg.split(",");


		if(msgTokens.length > 0){
			/*
			 * Handle Join msg -- client has just joined the server
			 * Coming in format: (join,localID)
			 */
			if(msgTokens[0].compareTo("join") == 0){
				try{
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);
					System.out.println("sending createNPC messages to client " + clientID.toString());
					sendCreateNPCMsg(clientID);

					
				} catch (IOException e){
					e.printStackTrace();
				}
			}

			/*
			 * Handle leave msg -- client wants to leave server
			 * Coming in format: (leave,localID)
			 */
			if(msgTokens[0].compareTo("leave") == 0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				System.out.println("Leave request from - " + clientID.toString());
				sendLeaveMessages(clientID);
				removeClient(clientID);
			}

			/*
			 * Handle create msg -- server recieves create msg from a client
			 * Coming in format: (create,localID,x,y,z)
			 */
			if(msgTokens[0].compareTo("create") == 0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2],msgTokens[3],msgTokens[4]};
				sendCreateMessages(clientID, pos);
				sendWantsDetailsMessages(clientID);
			}

			/*
			 * handle details for msg -- client asks for another clients information
			 * Coming in format: (dsfr, remoteID, localID, x,y,z)
			 */
			if(msgTokens[0].compareTo("dsfr") == 0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				UUID remoteID = UUID.fromString(msgTokens[2]);
				String[] pos = {msgTokens[3],msgTokens[4],msgTokens[5]};
				sendDetailsForMessage(clientID, remoteID, pos);
			}

			/*
			 * handle mv msg -- server receives mv msg
			 * Coming in format: (mv,localID,x,y,z)
			 */
			if(msgTokens[0].compareTo("mv") == 0){
				UUID clientId = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2],msgTokens[3],msgTokens[4]};
				sendMoveMessages(clientId, pos);
			}
			/*
			 * handle rot msg -- server recieves rot msg
			 * Coming in format: (rot,remoteID,m00,m10,m20,m01,m11,m21,m02,m12,m22)
			 */
			if(msgTokens[0].compareTo("rot")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] rot = {msgTokens[2],msgTokens[3],msgTokens[4],msgTokens[5],msgTokens[6],msgTokens[7],msgTokens[8],msgTokens[9],msgTokens[10]};
				sendRotateMessages(clientID, rot);
			}
			/*
			 * handle fire msg -- server recieves fire msg
			 * Coming in format: (fire,localGhostID,cameraN.x,cameraN.y,cameraN.z,cameraV.x,cameraV.y,cameraV.z)
			 */
			if(msgTokens[0].compareTo("fire")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] cameraN = {msgTokens[2],msgTokens[3],msgTokens[4]};
				String[] cameraV = {msgTokens[5],msgTokens[5],msgTokens[7]};
				sendFireMessage(clientID, cameraN, cameraV);
			}

			/*
			 * handle near msg -- server receives near msg from client if they are close to an npc
			 * 
			 * Coming in format: (dist,localghostID,npcID,distance(float))
			 */
			if(msgTokens[0].compareTo("dist")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				UUID npcID = UUID.fromString(msgTokens[2]);
				float distance = Float.parseFloat(msgTokens[3]);
				npcCon.setDistance(npcID, clientID, distance);
			}
		}
	}

	/*
	 * Lets client know that was trying to join the server if they were able to successfully
	 * Foramt: (join,succes/failure)
	 */

	public void sendJoinedMessage(UUID clientID, boolean success){
		try{
			System.out.println("trying to confirm join for -> " + clientID.toString());
			String msg = "join,";
			if(success){
				msg += "success";
			}else{
				msg += "failure";
			}
			sendPacket(msg,clientID);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * lets clients know if another player has left
	 * Format: (leave,remoteID)
	 */

	public void sendLeaveMessages(UUID clientID){
		try{
			String msg = "leave," + clientID.toString();
			forwardPacketToAll(msg, clientID);
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * Informs clients that a new clinet has joined the server with a unique remoteID, also triggers wants details for all other clients
	 * Format (create,remoteID,x,y,z)
	 */

	public void sendCreateMessages(UUID clientID, String[] pos){
		try{
			String msg = "create," + clientID.toString();
			msg += "," + pos[0];
			msg += "," + pos[1];
			msg += "," + pos[2];

			forwardPacketToAll(msg, clientID);

		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * Lets clients know that a new player has joined, handles WANTS_DETAILS msg as well
	 * Format: (create,remoteID,x,y,z)
	 */
	
	public void sendDetailsForMessage(UUID clientID, UUID remoteID, String[] pos){
		try{
			String msg = "dsfr," + remoteID.toString();
			msg += "," + pos[0];
			msg += "," + pos[1];
			msg += "," + pos[2];
			sendPacket(msg, clientID);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * Lets clients know that a another client wants to know about your avatar, is to be sent when a new client joins the server
	 * Format (wsds,remoteID)
	 */

	public void sendWantsDetailsMessages(UUID clientID){
		try{
			String msg = "wsds," + clientID.toString();
			System.out.println("sending wants details message from " + clientID.toString());
			forwardPacketToAll(msg, clientID);
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * lets clients know that another remote client has moved
	 * Format (mv,remoteID,x,y,z)
	 */

	public void sendMoveMessages(UUID clientID, String[] pos){
		try{
			String msg = "mv," + clientID.toString();
			msg += "," + pos[0];
			msg += "," + pos[1];
			msg += "," + pos[2];
			forwardPacketToAll(msg, clientID);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * lets other clients know that another remote client has rotated
	 * Format (rot,remoteID,m00,m10,m20,m01,m11,m21,m02,m12,m22)
	 */
	public void sendRotateMessages(UUID clientID, String[] rot){
		try{
			String msg = "rot," + clientID.toString();
			msg += "," + rot[0];
			msg += "," + rot[1];
			msg += "," + rot[2];
			msg += "," + rot[3];
			msg += "," + rot[4];
			msg += "," + rot[5];
			msg += "," + rot[6];
			msg += "," + rot[7];
			msg += "," + rot[8];
			forwardPacketToAll(msg, clientID);
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * lets other clients know that another remote client has fired a laser from their avatar
	 * format: (fire,localGhostID,cameraN.x,cameraN.y,cameraN.z,cameraV.x,cameraV.y,cameraV.z)
	 */
	public void sendFireMessage(UUID clientID, String[] cameraN, String[] cameraV){
		try{	
			String msg = "fire," + clientID.toString();
			msg += "," + cameraN[0];
			msg += "," + cameraN[1];
			msg += "," + cameraN[2];
			msg += "," + cameraV[0];
			msg += "," + cameraV[1];
			msg += "," + cameraV[2];
			forwardPacketToAll(msg, clientID);
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	// ----------------- NPC SECTION ---------------------- //


	/*
	 * Handles when server creates inital npc's, lets client know when the client joins server
	 * format: (createNPC, npcID, x, y, z)
	 */
	public void sendCreateNPCMsg(UUID clientID){
		ArrayList<NPC> npcList = npcCon.getNPCList();
		Iterator<NPC> it = npcList.iterator();
		NPC npc;
		while(it.hasNext()){
			npc = it.next();
			try{
				String msg = "createNPC," + npc.getId();
				msg += "," + npc.getX();
				msg += "," + npc.getY();
				msg += "," + npc.getZ();
				sendPacket(msg, clientID);
			}catch(IOException e){
				e.printStackTrace();
			}

		}
	}
	/*
	 * Handles when server needs to update clients about how npc have moved
	 * format: (npcMov,npcID,x,y,z)
	 */
	public void sendNPCInfo(){
		for(NPC npc: npcCon.getNPCList()){
			try{
				String msg = "npcMov," + npc.getId();
				msg += "," + npc.getX();
				msg += "," + npc.getY();
				msg += "," + npc.getZ();
				sendPacketToAll(msg);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
