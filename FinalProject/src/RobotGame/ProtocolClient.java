package RobotGame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

import org.joml.Vector3f;

import Server.NPC;
import tage.networking.client.GameConnectionClient;

// Messages are case sensitive

public class ProtocolClient extends GameConnectionClient{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	private ArrayList<NPC> npcList = new ArrayList<NPC>();	
	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException {	
        super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	}

	public UUID getID(){
		return id;
	}

	@Override
	protected void processPacket(Object message){
		if(message == null){
			System.out.println("message was null");
			return;
		}
		String msg = (String)message;

		// seperating msg into tokens
		String[] msgTokens = msg.split(",");
		
		if(msgTokens.length > 0){

			/*
			 * Join requset
			 * Format: (join,success/failure)
			 */
			if(msgTokens[0].compareTo("join") == 0){
				if(msgTokens[1].compareTo("success") == 0){

					System.out.println("join success");
					game.setIsConnected(true);
					sendCreateMessage(game.getAvatar().getWorldLocation());

				}else if(msgTokens[1].compareTo("failure") == 0){

					System.out.println("join failure");
					game.setIsConnected(false);
				}
			}

			/*
			 * Leave request
			 * Format: (leave,ghostID)
			*/
			if(msgTokens[0].compareTo("leave") == 0){
				// remove ghost avatar with given id
				UUID ghostID = UUID.fromString(msgTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}

			/*
			 * Handle Create message and details for message
			 * Format: (create, ghostID, x,y,z)  OR (dsfr, ghostID, x,y,z)
			 * x,y,z world location
			 */
			if((msgTokens[0].compareTo("create") == 0) || (msgTokens[0].compareTo("dsfr") == 0)){
				UUID ghostID = UUID.fromString(msgTokens[1]);
				Vector3f ghostPos = new Vector3f(
					Float.parseFloat(msgTokens[2]),
					Float.parseFloat(msgTokens[3]),
					Float.parseFloat(msgTokens[4]));
					System.out.println("Sending create message from id " + ghostID.toString());
				try{
					ghostManager.createGhostAvatar(ghostID, ghostPos);
				} catch (IOException e){
					System.out.println("error creating ghost avatar with ID:" + ghostID);
				}
			}

			/*
			 * Handle wants details msg
			 * Format: (wsds,ghostID)
			 */
			if(msgTokens[0].compareTo("wsds") == 0){
				UUID ghostID = UUID.fromString(msgTokens[1]);
				System.out.println("sending details for message from " + ghostID.toString());
				sendDetailsForMessage(ghostID,game.getAvatar().getWorldLocation());
			}

			/*
			 * Handle move msg
			 * Format: (mv,ghostID,x,y,z)
			 */
			if(msgTokens[0].compareTo("mv")== 0){
				UUID ghostID = UUID.fromString(msgTokens[1]);
				
				Vector3f ghostPos = new Vector3f(
					Float.parseFloat(msgTokens[2]),
					Float.parseFloat(msgTokens[3]),
					Float.parseFloat(msgTokens[4]));
				
				ghostManager.updateGhostAvatarPosition(ghostID, ghostPos);
			}
			/*
			 * Handle createNPC messages
			 * Format (createNPC,npcID,x,y,z)
			 */
			if(msgTokens[0].compareTo("createNPC")==0){
				
				UUID npcID = UUID.fromString(msgTokens[1]);
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(msgTokens[2]),
					Float.parseFloat(msgTokens[3]),
					Float.parseFloat(msgTokens[4])
				);
				createGhostNPC(npcID,ghostPosition);
			}
		}
	}

	/*
	 * Initial message from the game client in order to join the server
	 * Format: (join,localGhostID)
	*/

	public void sendJoinMessage(){
		try{
			sendPacket("join," + id.toString());
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * Lets server know of clients position and the server will distrubute that information to all other clients
	 * Format: (create,localGhostID,x,y,z)
	 */

	public void sendCreateMessage(Vector3f position){
		try{
			String msg = "create," + id.toString();
			msg += "," + position.x();
			msg += "," + position.y();
			msg += "," + position.z();
		
			sendPacket(msg);

		}catch(IOException e){
			System.out.println("error creating npc");
		}
	}

	/*
	 * Informs server of avatar's position, server will then send to client that requested it.
	 * Format: (dsfr,remoteID,localGhostID,x,y,z)
	 */

	public void sendDetailsForMessage(UUID remoteID, Vector3f position){
		try{
			String msg = "dsfr," + remoteID.toString() + "," + id.toString();
			msg += "," + position.x();
			msg += "," + position.y();
			msg += "," + position.z();
		
			sendPacket(msg);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * Informs server that client avatar has moved.
	 * Format: (mv,localGhostID,x,y,z)
	 */
	
	public void sendMoveMessage(Vector3f position){
		try{
			String msg = "mv," + id.toString();
			msg += "," + position.x();
			msg += "," + position.y();
			msg += "," + position.z();
		
			sendPacket(msg);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	public void createGhostNPC(UUID id, Vector3f pos){
		GhostNPC ghostNPC = new GhostNPC(id,game.getNPCObjShape(),game.getNPCTextureImage(),pos);
	}
}
