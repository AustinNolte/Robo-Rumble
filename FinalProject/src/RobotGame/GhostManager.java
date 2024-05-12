package RobotGame;

import java.io.IOException;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.lang.Math;

import org.joml.*;

import tage.Engine;
import tage.GameObject;
import tage.TextureImage;
import tage.nodeControllers.LaserBeamController;
import tage.physics.PhysicsObject;
import tage.shapes.AnimatedShape;



public class GhostManager {

    private MyGame game;
    
    // float array vals for physics operations
    private float vals[] = new float[16];
    private double[] tempTransform;
    private PhysicsObject ghostAvPhysicsObj;
    private PhysicsObject npcPhysicsObject;
    private PhysicsObject laserPhysicsObj;
    private float sizeLaser[] = {1,1,1};
    private float mass = 1.0f;
    private float[] size = {2,5.5f,1};
    private TextureImage skin;

    private Vector<GhostAvatar> gaVec = new Vector<GhostAvatar>();
    private Vector<GhostNPC> gnpcVec = new Vector<GhostNPC>();

    //inital scale for all ghost avatars
    Matrix4f initalScale = new Matrix4f().scaling(1f);
    Matrix4f initTrans,initRot,initScale;


    public GhostManager(MyGame game) {
        this.game = game;
    }
    
    public void createGhostAvatar(UUID id, Vector3f position, String skinOption) throws IOException{
        System.out.println("Creating ghost avatar with ID: " + id);
        
        // creating new ghost avatar
        if(skinOption.compareToIgnoreCase("red")== 0){
            skin = game.getOption1();
        }else if(skinOption.compareToIgnoreCase("blue")==0){
            skin = game.getOption2();
        }
        GhostAvatar newA = new GhostAvatar(id, game.getGhostObjShape(),skin, position);
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

    public void createGhostNPC(UUID id, Vector3f position)throws IOException{
        System.out.println("Creating a ghostNPC with ID: " + id);

        Vector3f newPos = new Vector3f(position.x,game.getTerrainHeight(position.x,position.z)-4.5f,position.z);
        // creating ghost NPC
        GhostNPC newGhostNPC = new GhostNPC(id,game.getNPCObjShape(),game.getNPCTextureImage(),newPos);

        // --------- Adding Phyiscs Objects  --------------
        

        
        Matrix4f translation = new Matrix4f(newGhostNPC.getLocalTranslation());
        tempTransform = game.toDoubleArray(translation.get(vals));
        npcPhysicsObject = ((game.getEngine()).getSceneGraph()).addPhysicsBox(mass, tempTransform, size);

        newGhostNPC.setPhysicsObject(npcPhysicsObject);

        newGhostNPC.getRenderStates().setModelOrientationCorrection(new Matrix4f().rotateY((float)Math.toRadians(-90)));
        //adding to the vector of ga
        gnpcVec.add(newGhostNPC);
    }

    public Vector<GhostNPC> getNPCList(){
        return gnpcVec;
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

    // there is only one ghost avatar as my game is designed to only be 1 npc
    public GhostNPC getGhostNPC(){
        if(gnpcVec.size() == 0){
            return null;
        }else{
            return gnpcVec.get(0);
        }
    }

    public GhostAvatar findAvatar(UUID id){
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

    public void updateNPCLocation(UUID id, Vector3f position){
        GhostNPC ga = findNPC(id);
        
        if(ga == null){
            System.out.println("Failed to udpate ghostNPC with ID: " + id + " because it does not exist");
        }else{
            // updating position
            Vector3f newPos = new Vector3f(position.x,game.getTerrainHeight(position.x, position.z),position.z);
            ga.setLocalLocation(newPos);
        }
    }

    public void updateNpcRoation(UUID id, Vector3f position){
        GhostNPC ga = findNPC(id);

        if(ga == null){
            System.out.println("Failed to rotate ghost NPC with ID " + id + " because it does not exist");
        }
        else{
        }
        ga.lookAt(position);
    }

    public GhostNPC findNPC(UUID id){
        GhostNPC ga;
        Iterator<GhostNPC> it = gnpcVec.iterator();
        while(it.hasNext()){
            ga = it.next();
            if(ga.getId().compareTo(id) == 0){
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

            game.getGhostLaserSound().setLocation(ga.getWorldLocation());
            game.getGhostLaserSound().play();
    }

    public PhysicsObject getGhostPhysicsObject(){
        return ghostAvPhysicsObj;
    }

    public void handleDeadMessage(UUID id){
        GhostAvatar ga = findAvatar(id);
        if(ga == null){
            System.out.println("Failed to find GhostAvatar with ID: " + id + " because it does not exist");
        }

        ga.setLocalRotation(new Matrix4f().rotateX((float)Math.toRadians(90)));
        ga.setLocalLocation(new Vector3f(ga.getWorldLocation().x,game.getTerrainHeight(ga.getWorldLocation().x, ga.getWorldLocation().z),ga.getWorldLocation().z));

        game.getDeathSound().setLocation(ga.getWorldLocation());
        game.getDeathSound().play();

    }

    public void startAnimation(UUID id){
        GhostAvatar ga = findAvatar(id);
        if(ga == null){
            System.out.println("Failed to start animation for GhostAvatar with ID " + id + " Because it does not exist");
        }

        game.getGhostObjShape().playAnimation("WALK",.04f, AnimatedShape.EndType.LOOP,0);
    }

    public void stopAnimation(UUID id){
        GhostAvatar ga = findAvatar(id);
        if(ga == null){
            System.out.println("Failed to stop animation for GhostAvatar with ID " + id + " Because it does not exist");
        }

        game.getGhostObjShape().stopAnimation();
    }

    public void npcShootLaser(){
        GhostNPC ga = getGhostNPC();
            
        // creating laser object
        GameObject laser = new GameObject(GameObject.root(),game.getLaserShape(),game.getLaserImage());
        initTrans = new Matrix4f().identity();
        initRot = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        initTrans.translate(ga.getWorldLocation().add(ga.getLocalForwardVector().x*4,2.5f,game.getAvatar().getLocalForwardVector().z*4));
        initRot.lookAlong(ga.getLocalForwardVector().mul(-1), ga.getLocalUpVector());
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
        laser.getPhysicsObject().applyForce(ga.getLocalForwardVector().x()*10000, ga.getLocalForwardVector().y()*10000, ga.getLocalForwardVector().z()*10000, 0, 0, 0);

        LaserBeamController lbCont = new LaserBeamController(game.getEngine());
        game.getEngine().getSceneGraph().addNodeController(lbCont);
        lbCont.addTarget(laser);
        lbCont.enable();

        game.getGhostLaserSound().setLocation(ga.getWorldLocation());
        game.getGhostLaserSound().play();
    }
}
