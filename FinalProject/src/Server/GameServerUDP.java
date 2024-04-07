package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;


public class GameServerUDP extends GameConnectionServer<UUID> {
	public GameServerUDP(int localPort) throws IOException {	
		super(localPort, ProtocolType.UDP);
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
	 * Lets celitns know that a new player has joined, handles WANTS_DETAILS msg as well
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
}
