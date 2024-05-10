package RobotGame;

import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;
import java.util.UUID;

import org.joml.*;

import tage.Engine;
import tage.GameObject;
import tage.nodeControllers.LaserBeamController;
import tage.physics.PhysicsObject;



public class GhostManager {

    private MyGame game;
    
    // float array vals for physics operations
    private float vals[] = new float[16];
    private double[] tempTransform;
    private PhysicsObject ghostAvPhysicsObj;
    private PhysicsObject laserPhysicsObj;
    private float sizeLaser[] = {1,1,1};
    private float mass = 1.0f;
    private float[] size = {2,5.5f,1};

    private Vector<GhostAvatar> gaVec = new Vector<GhostAvatar>();
    //inital scale for all ghost avatars
    Matrix4f initalScale = new Matrix4f().scaling(1f);
    Matrix4f initTrans,initRot,initScale;


    public GhostManager(MyGame game) {
        this.game = game;
    }
    
    public void createGhostAvatar(UUID id, Vector3f position) throws IOException{
        System.out.println("Creating ghost avatar with ID: " + id);
        
        // creating new ghost avatar
        GhostAvatar newA = new GhostAvatar(id, game.getGhostObjShape(),game.getGhostTextureImage(), position);

        // --------- Adding Phyiscs Objects  --------------
        

        
        Matrix4f translation = new Matrix4f(newA.getLocalTranslation());
        tempTransform = game.toDoubleArray(translation.get(vals));
        ghostAvPhysicsObj = ((game.getEngine()).getSceneGraph()).addPhysicsBox(mass, tempTransform, size);

        newA.setPhysicsObject(ghostAvPhysicsObj);

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
            game.getEngine().getSceneGraph().removePhysicsObject(ga.getPhysicsObject());
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
            // updating rotation
            ga.setLocalRotation(rotation);
        }
    }

    // there is only one ghost avatar as my game is designed to only be 2 player max
    public GhostAvatar getGhostAvatar(){
        if(gaVec.size() == 0){
            return null;
        }else{
            return gaVec.get(0);
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

    public void shootLaser(UUID id, Vector3f cameraN, Vector3f cameraV){
            GhostAvatar ga = findAvatar(id);
            
            if(ga == null){
                System.out.println("Failed to fire laser from GhostAvatar with ID: " + id + " because it does not exist");
            }
            // creating laser object
            GameObject laser = new GameObject(GameObject.root(),game.getLaserShape(),game.getLaserImage());
            initTrans = new Matrix4f().identity();
            initRot = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            initTrans.translate(ga.getWorldLocation().add(game.getAvatar().getLocalForwardVector().x*2,5.5f,game.getAvatar().getLocalForwardVector().z*2));
            initRot.lookAlong(cameraN.mul(-1), cameraV);
            initScale.scale(.05f,.05f,1f);

            laser.setLocalTranslation(initTrans);
            laser.setLocalRotation(initRot);
            laser.setLocalScale(initScale);

            // creating laser physics object
            Matrix4f translation = new Matrix4f(laser.getLocalTranslation());
            translation.mul(initRot);
            tempTransform = game.toDoubleArray(translation.get(vals));
            laserPhysicsObj = (game.getEngine().getSceneGraph()).addPhysicsBox(mass,tempTransform, sizeLaser);
            
            
            laser.setPhysicsObject(laserPhysicsObj);
            laser.getPhysicsObject().applyForce(cameraN.x*-10000, cameraN.y*-10000, cameraN.z*-10000, 0, 0, 0);

            LaserBeamController lbCont = new LaserBeamController(game.getEngine());
            game.getEngine().getSceneGraph().addNodeController(lbCont);
            lbCont.addTarget(laser);
            lbCont.enable();
    }
}
