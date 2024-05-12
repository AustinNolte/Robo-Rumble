package Server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class NPCRotate extends BTAction{

    private NPC npc;
    private NPCcontroller npcCon;
    private GameServerUDP server;

    public NPCRotate(NPC npc, NPCcontroller npcCon, GameServerUDP server){
        this.npc = npc;
        this.npcCon = npcCon;
        this.server = server;
    }

    
    @Override
    protected BTStatus update(float elapsedTime) {
        server.sendLookAtMessage(npcCon.getClientNearID());
        
        return BTStatus.BH_SUCCESS;
        
    }
}
