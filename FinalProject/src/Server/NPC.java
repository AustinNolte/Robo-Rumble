package Server;

import java.util.HashMap;
import java.util.UUID;

import org.joml.Vector3f;


    //
public class NPC {
    private float x,y,z;
    private Vector3f pos,forward;

    private UUID id;
    //hash table to keep distances to each player for AI purposes

    public NPC(float x, float z){
        this.x = x;
        this.z = z;
        y = 2.0f;
        pos = new Vector3f(x,y,z);
        forward = new Vector3f(1,0,0);
        // id to find NPC
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
        updateLocalLocation();
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setZ(float z) {
        this.z = z;
        updateLocalLocation();
    }

    private void updateLocalLocation(){
        pos.set(x,y,z);
    }

    public void updatePosition(float amt){
        pos.add(forward.mul(amt));
    }
}
