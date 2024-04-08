package WizardGame;

import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.*;
import tage.input.IInputManager.INPUT_ACTION_TYPE;
import tage.input.action.AbstractInputAction;

public class OTSCameraController {
    private Engine engine;
    private Camera camera;
    private GameObject player;
    private float cameraAzimuth;
    private float cameraElevation;
    private float cameraRadius;

    /** Constrctor, give the camera of a specific viewport, the engine, and the gameobject (player) you wish to have the orbit controller focus on */
    public OTSCameraController(Camera camera, Engine engine, GameObject player){
        
        this.camera = camera;
        this.engine = engine;
        this.player = player;

        //starting position for camera
        cameraAzimuth = 0f;
        cameraElevation = 5f;
        cameraRadius = 20f;

        // allowing inputs to be used for controller
        setupInputs();

        // updating default position of camera
        updateCamera();
    }

    private void setupInputs(){

        OrbitAzimuthControl oAC = new OrbitAzimuthControl();
        OrbitElevationnControl oEC = new OrbitElevationnControl();
        OrbitRadiusControl oRC = new OrbitRadiusControl();
        
        (engine.getInputManager()).associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RX, oAC,INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        (engine.getInputManager()).associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RY, oEC,INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        (engine.getInputManager()).associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Z , oRC,INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    }

    /** update method for camera to be called in update method in class extending VariableFrameRateGame */
    public void updateCamera(){

        //-------------- getting camera angle in relation to avatar's --------
        Vector3f playerRot = player.getWorldForwardVector();
        double playerAng = Math.toDegrees((double)playerRot.angleSigned(new Vector3f(0,0,-1),new Vector3f(0,1,0)));
        float totalAz = cameraAzimuth - (float)playerAng;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);

        // ----- getting XYZ coardinates of camera in realtion to avatar -----
        float x = cameraRadius * (float)(Math.cos(phi)*Math.sin(theta));
        float y = cameraRadius * (float)(Math.sin(phi));
        float z = cameraRadius * (float)(Math.cos(phi)*Math.cos(theta));


        camera.setLocation(new Vector3f(x,y,z).add(player.getWorldLocation()));
        camera.lookAt(player);

        
    }

    public Vector3f getCameraN(){
        return camera.getN();
    }

    public Vector3f getCameraU(){
        return camera.getU();
    }
    
    private class OrbitAzimuthControl extends AbstractInputAction{
        
        @Override
        public void performAction(float time, Event evt) {
            //deadzoning for possible stick drift on contorllers
            if(evt.getValue() > .15f || evt.getValue() < -.15f){
                
                cameraAzimuth += evt.getValue()*time*100;
                // modulo 360 to keep it between 0 and 360 degrees
                cameraAzimuth %= 360;
            }
            updateCamera();
        }
    }

    private class OrbitElevationnControl extends AbstractInputAction{
        float newY;
        float newCameraElevation;
        float oldCameraElevation;
        @Override
        public void performAction(float time, Event evt){

            oldCameraElevation = cameraElevation;
            //deadzoning for possible stick drift on controllers
            if(evt.getValue() > .15f || evt.getValue() < -.15f){
                
                
                //multiply by -1 because y rot returns -1 pushing stick up
                //not letting cameraElevation go above 90 degrees or below -90
                if((cameraElevation + (evt.getValue()*-1)) >= 89f || (cameraElevation + (evt.getValue()*-1)) <= -89f){
                    cameraElevation = oldCameraElevation; 

                }else{
                    newCameraElevation = oldCameraElevation + (evt.getValue()*-1*time*100);
                }
                // modulo 360 to keep it between 0 and 360 degrees
                newCameraElevation %= 360;
                newY = (evt.getValue()*-1*time) + newCameraElevation;
                if(newY + player.getWorldLocation().y > 0){
                    cameraElevation = newCameraElevation;
                }
            } 
            updateCamera();
        }
    }

    private class OrbitRadiusControl extends AbstractInputAction{
        
        @Override
        public void performAction(float time, Event evt){

            //radius cannot beomce 5x bigger or .5x smaller than default
            if(((cameraRadius + evt.getValue() ) > 20 ) || ((cameraRadius + evt.getValue()) < 2)){
                cameraRadius += 0;

            }else{
                cameraRadius += evt.getValue()*time*100;  
            }
            updateCamera();
        }
    }    
}
