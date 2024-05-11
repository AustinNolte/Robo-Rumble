package RobotGame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import org.joml.*;

import Server.NPC;
import tage.networking.client.GameConnectionClient;

// Messages are case sensitive

public class ProtocolClient extends GameConnectionClient{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;

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
				try{
					ghostManager.createGhostNPC(npcID, ghostPosition);
				}catch (IOException e){
					System.out.println("error creating ghost NPC with ID: " + npcID);
				}
			}
			/*
			 * Handle rotate messages
			 * 
			 * Format: (rot,localGhostID,m00,m10,m20,m01,m11,m21,m02,m12,m22)
			 */
			if(msgTokens[0].compareTo("rot")==0){
				UUID ghostID = UUID.fromString(msgTokens[1]);
				
				Matrix4f ghostRot = new Matrix4f().identity();
				ghostRot.m00(Float.parseFloat(msgTokens[2]));
				ghostRot.m10(Float.parseFloat(msgTokens[3]));
				ghostRot.m20(Float.parseFloat(msgTokens[4]));
				ghostRot.m01(Float.parseFloat(msgTokens[5]));
				ghostRot.m11(Float.parseFloat(msgTokens[6]));
				ghostRot.m21(Float.parseFloat(msgTokens[7]));
				ghostRot.m02(Float.parseFloat(msgTokens[8]));
				ghostRot.m12(Float.parseFloat(msgTokens[9]));
				ghostRot.m22(Float.parseFloat(msgTokens[10]));
				ghostManager.updateGhostAvatarRotation(ghostID, ghostRot);
			}
			/*
			 * Handle fire message
			 * 
			 * Format: (fire,localGhostID,cameraN.x,cameraN.y,cameraN.z,cameraV.x,cameraV.y,cameraV.z)
			 */
			if(msgTokens[0].compareTo("fire")==0){
				UUID ghostID = UUID.fromString(msgTokens[1]);

				Vector3f cameraN = new Vector3f(
					Float.parseFloat(msgTokens[2]),
					Float.parseFloat(msgTokens[3]),
					Float.parseFloat(msgTokens[4])
				);

				Vector3f cameraV = new Vector3f(
					Float.parseFloat(msgTokens[5]),
					Float.parseFloat(msgTokens[6]),
					Float.parseFloat(msgTokens[7])
				);

				ghostManager.shootLaser(ghostID, cameraN, cameraV);
			}
			/*
			 * Handle isNear npc check
			 * 
			 * Format(isNear)
			 */
			if(msgTokens[0].compareTo("isNear")==0){
				sendIsNearMessage();
			}
			/*
			 * Handle ghostNPC moved from server
			 * 
			 * Format(npcMov, npcID, x,y,z)
			 */
			if(msgTokens[0].compareTo("npcMov")==0){
				UUID npcID = UUID.fromString(msgTokens[1]);
				Vector3f pos = new Vector3f(
					Float.parseFloat(msgTokens[2]),
					Float.parseFloat(msgTokens[3]),
					Float.parseFloat(msgTokens[4])
				);

				ghostManager.updateNPCLocation(npcID,pos);

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
	/*
	 * Inform server that client avatar has rotated
	 * Format: (rot,localGhostID,m00,m10,m20,m01,m11,m21,m02,m12,m22)
	 */
	public void sendRotateMessage(float[] rot){
		try{
			String msg = "rot," + id.toString();
			msg += "," + rot[0];
			msg += "," + rot[1];
			msg += "," + rot[2];
			msg += "," + rot[3];
			msg += "," + rot[4];
			msg += "," + rot[5];
			msg += "," + rot[6];
			msg += "," + rot[7];
			msg += "," + rot[8];
			sendPacket(msg);

		}catch (IOException e){
			e.printStackTrace();
		}
	}
	/*
	 * Inform server that another client has fired a laser
	 * Format: (fire,localGhostID,cameraN.x,cameraN.y,cameraN.z,cameraV.x,cameraV.y,cameraV.z)
	 */
	public void sendFireMessage(float[] cameraN, float[] cameraV){
		try{
			String msg = "fire," + id.toString();
			msg+= "," + cameraN[0];
			msg+= "," + cameraN[1];
			msg+= "," + cameraN[2];
			msg+= "," + cameraV[0];
			msg+= "," + cameraV[1];
			msg+= "," + cameraV[2];
			sendPacket(msg);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	public void sendIsNearMessage(){
		Vector<GhostNPC> NPCList = ghostManager.getNPCList();
		GhostNPC npc;
		Iterator<GhostNPC> it = NPCList.iterator();

		while(it.hasNext()){
			npc = it.next();
			
			String msg = "dist," + id.toString();
			float distance = npc.getWorldLocation().distance(game.getAvatar().getWorldLocation());
			if(distance < 40){
				msg += "," + npc.getId();
				msg += "," + distance; 
				
				try{
					sendPacket(msg);
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
}
