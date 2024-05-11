package Server;

import java.util.HashMap;
import java.util.UUID;

import org.joml.Vector3f;


    //
public class NPC {
    private float x,y,z;
    private Vector3f pos,forward;

    private float dirX,dirZ;
    private UUID id;

    private boolean isNear = false;

    //hash table to keep distances to each player for AI purposes
	private HashMap<UUID,Float> distances = new HashMap<UUID,Float>();

    public NPC(float x, float z){
        this.x = x;
        this.z = z;
        y = -4.9f;
        pos = new Vector3f(x,y,z);
        forward = new Vector3f(1,0,0);
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

    public HashMap<UUID,Float> getDistanceHash(){
        return distances;
    }

    public void setDistances(UUID id, float distance){
        distances.put(id,distance);
    }
}
