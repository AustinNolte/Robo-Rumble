package Server;

import java.util.ArrayList;

import Server.NPC;
import Server.NPCcontroller;
import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition{


    private NPC npc;
    private NPCcontroller npcCon;
    private GameServerUDP server;

    public AvatarNear(GameServerUDP server, NPC npc,NPCcontroller npcCon,boolean toNegate) {
        super(toNegate);
        this.npc = npc;
        this.npcCon = npcCon;
        this.server = server;
    }

    @Override
    protected boolean check() {
        server.sendCheckForAvatar();
        return npcCon.isNear();
    }
    
}
