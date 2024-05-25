package Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import tage.ai.behaviortrees.AvatarNear;
import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;
import tage.ai.behaviortrees.NPCRotate;
import tage.ai.behaviortrees.NPCShoot;
import tage.ai.behaviortrees.WaitTime;

/*
 * Controller for a single sentinel npc enemy, resides on server side.
 */
public class NPCcontroller {

    private NPC npc;

    // this will be for creating random positions of starting locations
    private Random rand = new Random();
    private long thinkStartTime,tickStartTime,lastThinkUpdate,lastTickUpdate, currentTime;
    private float elapsedThinkMiliSec,elapsedTickMiliSec;
    private GameServerUDP server;
    BehaviorTree bt = new BehaviorTree(BTCompositeType.SEQUENCE);
    private boolean isNear;
    private UUID clientIDNear;

    //** Starts logic of npcContrller, sets up the one npc */
    public void start(GameServerUDP server){
        this.server = server;
        thinkStartTime = System.nanoTime();
        tickStartTime = System.nanoTime();
        lastThinkUpdate = System.nanoTime();
        lastTickUpdate = System.nanoTime();

        setupBehaviorTree();
        setupNPC();
        npcLoop();


    }

    //** sets up one npc in a random location */
    public void setupNPC(){
        float x,z;
        int randomI,randomI2;
        // random int to randomly multiply by negative 1 to make placement more random
        randomI = rand.nextInt(100)+1;
        randomI2 = rand.nextInt(100)+1;
        // adding 50 to random so there is a 50x50 box around players that are not inhabited by npcs at start
        x = rand.nextFloat(200)+50;
        z = rand.nextFloat(200)+50;
        if(randomI < 50){
            x *= -1;
        }
        if(randomI2 < 50){
            z*= -1;
        }
        npc = new NPC(x,z);
    }
    //** gets the npc */
    public NPC getNpc(){
        return npc;
    } 
    /** npc think and tick loop */
    public void npcLoop(){
        while(true){
            currentTime = System.nanoTime();
            elapsedThinkMiliSec = (currentTime - lastThinkUpdate)/(1000000.0f);
            elapsedTickMiliSec = (currentTime - lastThinkUpdate)/(1000000.0f);

            if(elapsedTickMiliSec >= 25.0f){
                lastTickUpdate = currentTime;
                //updateNPCpos();
                //server.sendNPCInfo();
            }
            if(elapsedThinkMiliSec >= 250.0f){
                lastThinkUpdate = currentTime;
                bt.update(elapsedTickMiliSec);

            }
            Thread.yield();
        }
    }
    //** gets clientID that is near the NPC */
    public UUID getClientNearID(){
        return clientIDNear;
    }
    //** sets the near flag and which client is near it */
    public void setIsNear(boolean isNear,UUID clientID){
        this.isNear = isNear;
        clientIDNear = clientID;
    }
    //** get near flag */
    public boolean isNear(){
        return isNear;
    }
    /** sets up behavior tree for npc logic*/
    public void setupBehaviorTree(){
        bt.insertAtRoot(new BTSequence(10));
        bt.insert(10,new AvatarNear(server, npc, this,false));
        bt.insert(10,new NPCRotate(npc,this, server));
        bt.insert(10,new WaitTime());
        bt.insert(10, new NPCShoot(npc,this,server));

    }
}
