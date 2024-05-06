package Server;

import java.util.UUID;

//
public class NPC {
    private float x,y,z;
    private float dirX,dirZ;
    private UUID id;
    public NPC(float x, float z){
        this.x = x;
        this.z = z;
        y = -4.9f;
        // starting with npc's moving in posivtive direction
        dirX = .5f;
        dirZ = .5f;
        // id to find NPC's in list of NPC's
        id = UUID.randomUUID();
    }
    public UUID getId() {
        return id;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setZ(float z) {
        this.z = z;
    }

    public void updatePosition(){
        // npcs will be in a 100 x 100 box surrounding player. If they start to move to far they will turn around
        if(x > 100){
            dirX = -dirX;
        }else if(z > 100){
            dirZ = -dirZ;
        }
        this.x += dirX;
        this.z += dirZ;
    }
}
