package RobotGame;

import net.java.games.input.Event;
import tage.*;
import tage.input.action.*;
import tage.nodeControllers.LaserBeamController;
import tage.physics.*;

import org.joml.*;

public class FireAction extends AbstractInputAction{

    private ObjShape laserShape;
    private TextureImage laserImage;
    private PhysicsObject laserPhysicsObj;
    private MyGame game;
    private ProtocolClient p;
    private Matrix4f initTrans,initRot,initScale;
    private float mass = 1.0f;
    private float size[] = {1,1,1};
    private double[] tempTransform;
    private float vals[] = new float[16];
    private float cameraNArr[] = new float[3];
    private float cameraVArr[] = new float[3];
    


    public FireAction(MyGame game, ObjShape laserShape, TextureImage laserImage, ProtocolClient p){
        this.game = game;
        this.laserImage = laserImage;
        this.laserShape = laserShape;
        this.p = p;
        initTrans = new Matrix4f().identity();
        initRot = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
    }
    @Override
    public void performAction(float time, Event evt) {
        float evtValue = evt.getValue() * -1;
       

        // adding deadzoning to right trigger, only works with right trigger
        if(evtValue >= 0.99f || evt.getComponent().toString().equalsIgnoreCase("Y")){
            // creating laser object
            GameObject laser = new GameObject(GameObject.root(),laserShape,laserImage);
            initTrans = new Matrix4f().identity();
            initRot = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            initTrans.translate(game.getAvatar().getLocalLocation().add(game.getAvatar().getLocalForwardVector().x*2,5.5f,game.getAvatar().getLocalForwardVector().z*2));
            initRot.lookAlong(game.getCameraN().mul(-1), game.getCameraV());
            initScale.scale(.05f,.05f,1f);

            laser.setLocalTranslation(initTrans);
            laser.setLocalRotation(initRot);
            laser.setLocalScale(initScale);

            // creating laser physics object
            Matrix4f translation = new Matrix4f(laser.getLocalTranslation());
            translation.mul(initRot);
            tempTransform = game.toDoubleArray(translation.get(vals));
            laserPhysicsObj = (game.getEngine().getSceneGraph()).addPhysicsBox(mass,tempTransform, size);
            
            
            laser.setPhysicsObject(laserPhysicsObj);
            laser.getPhysicsObject().applyForce(game.getCameraN().x*10000, game.getCameraN().y*10000, game.getCameraN().z*10000, 0, 0, 0);

            LaserBeamController lbCont = new LaserBeamController(game.getEngine());
            game.getEngine().getSceneGraph().addNodeController(lbCont);
            lbCont.addTarget(laser);
            lbCont.enable();
            sendFireMessage();
            
        }
    }

    public void sendFireMessage(){
        cameraNArr[0] = game.getCameraN().x;
        cameraNArr[1] = game.getCameraN().y;
        cameraNArr[2] = game.getCameraN().z;
        cameraVArr[0] = game.getCameraV().x;
        cameraVArr[1] = game.getCameraV().y;
        cameraVArr[2] = game.getCameraV().z;
        p.sendFireMessage(cameraNArr, cameraVArr);
    }
    
}