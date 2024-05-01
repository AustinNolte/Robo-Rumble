package RobotGame;

import java.util.UUID;

import tage.GameObject;
import tage.*;
import org.joml.*;

// Ghost avatars shapes and textures must be loaded with all other textures and shapes during startup

public class GhostAvatar extends GameObject{
    private UUID uuid;

    public GhostAvatar(UUID uuid, ObjShape s, TextureImage t, Vector3f p){
        super(GameObject.root(),s,t);
        this.uuid = uuid;
        setLocalLocation(p);
    }

    public UUID getID(){
        return uuid;
    }
}
