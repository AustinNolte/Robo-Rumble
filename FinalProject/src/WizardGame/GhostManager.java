package WizardGame;

import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;
import java.util.UUID;

import org.joml.*;

public class GhostManager {

    private MyGame game;

    private Vector<GhostAvatar> gaVec = new Vector<GhostAvatar>();
    //inital scale for all ghost avatars
    Matrix4f initalScale = new Matrix4f().scaling(.5f);

    public GhostManager(MyGame game) {
        this.game = game;
    }
    
    public void createGhostAvatar(UUID id, Vector3f position) throws IOException{
        System.out.println("Creating ghost avatar with ID: " + id);
        
        // creating new ghost avatar
        GhostAvatar newA = new GhostAvatar(id, game.getGhostObjShape(),game.getGhostTextureImage(), position);

        // scaling to be same scaling as player avatar
        newA.setLocalScale(initalScale);

        //adding to the vector of ga
        gaVec.add(newA);
    }

    public void removeGhostAvatar(UUID id){
        GhostAvatar ga = findAvatar(id);
        if(ga == null){
            System.out.println("error removing ghost avatar with id " + id.toString() + " because it does not exist");
        }else{
            game.getEngine().getSceneGraph().removeGameObject(ga);
            gaVec.remove(ga);
        }
    }


    public void updateGhostAvatarPosition(UUID id, Vector3f position){
        GhostAvatar ga = findAvatar(id);
        
        if(ga == null){
            System.out.println("Failed to udpate GhostAvatar with ID: " + id + " because it does not exist");
        }else{
            // updating position
            ga.setLocalLocation(position);
        }
    }

    public void updateGhostAvatarRotation(UUID id, Matrix4f rotation){
        GhostAvatar ga = findAvatar(id);

        if(ga == null){
            System.out.println("Failed to rotate GhostAvatar with ID: " + id + " because it does not exist");
        }else{
            // updating position
            ga.setLocalRotation(rotation);
        }
    }

    private GhostAvatar findAvatar(UUID id){
        GhostAvatar ga;
        Iterator<GhostAvatar> it = gaVec.iterator();
        while(it.hasNext()){
            ga = it.next();
            if(ga.getID().compareTo(id) == 0){
                return ga;
            }
        }
        return null;
    }
}
