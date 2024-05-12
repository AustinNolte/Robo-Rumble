package Server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class NPCShoot extends BTAction{

    private NPC npc;
    private NPCcontroller npcCon;
    private GameServerUDP server;

    public NPCShoot(NPC npc, NPCcontroller npcCon, GameServerUDP server){
        this.npc = npc;
        this.npcCon = npcCon;
        this.server = server;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        
        server.npcShootLaser();

        return BTStatus.BH_SUCCESS;
        
    }
    
}
