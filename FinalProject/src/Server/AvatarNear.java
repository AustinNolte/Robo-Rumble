package Server;

import java.util.ArrayList;

import Server.NPC;
import Server.NPCcontroller;
import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition{


    private ArrayList<NPC> npcList;
    private NPCcontroller npcCon;

    public AvatarNear(ArrayList<NPC> npcList,NPCcontroller npcCon,boolean toNegate) {
        super(toNegate);
        this.npcList = npcList;
        this.npcCon = npcCon;
    }

    @Override
    protected boolean check() {
        return true;
    }
    
}
