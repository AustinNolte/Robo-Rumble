package Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import tage.ai.behaviortrees.BTCompositeType;
import tage.ai.behaviortrees.BTSequence;
import tage.ai.behaviortrees.BehaviorTree;

/*
 * Controller for boid type npc enemies, resides on server side.
 */
public class NPCcontroller {

    private ArrayList<NPC> npcList = new ArrayList<NPC>();
    // this will be for creating random positions of starting locations
    private Random rand = new Random();
    private long thinkStartTime,tickStartTime,lastThinkUpdate,lastTickUpdate, currentTime;
    private float elapsedThinkMiliSec,elapsedTickMiliSec;
    private GameServerUDP server;
    BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);

    // updating NPC locations
    public void updateNPCpos(){
        for(NPC npc : npcList){
            npc.updatePosition(((currentTime/(1000000.0f)) - lastTickUpdate)*10);
        }
    }

    // start method for NPC logic, amount is for the amount of boid enemies
    public void start(GameServerUDP server, int amount){
        this.server = server;
        thinkStartTime = System.nanoTime();
        tickStartTime = System.nanoTime();
        lastThinkUpdate = System.nanoTime();
        lastTickUpdate = System.nanoTime();

        //setupBehaviorTree();
        setupNPCs(amount);



    }

    // setting up the given amount of NPCs
    public void setupNPCs(int amount){
        float x,z;
        int randomI,randomI2;
        for(int i = 0; i < amount; i++){
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
            NPC npc = new NPC(x,z);
            npcList.add(npc);
        }
    }

    // iterator to send details about all npcs to a client that has just joined
    public ArrayList<NPC> getNPCList(){
        return npcList;
    }
    
    private NPC findNPC(UUID id){
        NPC npc;
        Iterator<NPC> it = npcList.iterator();
        while(it.hasNext()){
            npc = it.next();
            if(npc.getId() == id){
                return npc;
            }
        }
        return null;
    }

    public void npcLoop(){
        while(true){
            currentTime = System.nanoTime();
            elapsedThinkMiliSec = (currentTime - lastThinkUpdate)/(1000000.0f);
            elapsedTickMiliSec = (currentTime - lastThinkUpdate)/(1000000.0f);

            if(elapsedTickMiliSec >= 25.0f){
                lastTickUpdate = currentTime;
                updateNPCpos();
                server.sendNPCInfo();
            }
            if(elapsedThinkMiliSec >= 250.0f){
                lastThinkUpdate = currentTime;

            }
            Thread.yield();
        }
    }

    public void setDistance(UUID npcID, UUID clientID, float distance){
        NPC npc = findNPC(npcID);
        npc.setDistances(clientID, distance);



    }

    public void setupBehaviorTree(){
        bt.insertAtRoot(new BTSequence(10));
        bt.insertAtRoot(new BTSequence(20));

        bt.insert(10, new AvatarNear(npcList, this, false));
    }
}
