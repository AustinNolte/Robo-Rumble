package RobotGame;

import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;

// texture and shape are loaded in during startup

public class GhostNPC extends GameObject{
    private UUID id;
    public GhostNPC(UUID id, ObjShape s, TextureImage t, Vector3f p){
        super(GameObject.root(),s,t);
        this.id = id;
        setLocalLocation(p);
        setLocalScale(new Matrix4f().scale(10));
    }

    public UUID getId(){
        return id;
    }
}
