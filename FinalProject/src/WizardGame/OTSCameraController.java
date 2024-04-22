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

    /** Constrctor, give the camera of a specific viewport, the engine, and the gameobject (player) you wish to have the orbit controller focus on */
    public OTSCameraController(Camera camera, Engine engine, GameObject player){
        
        this.camera = camera;
        this.engine = engine;
        this.player = player;

        // allowing inputs to be used for controller
        setupInputs();

        // updating default position of camera
        updateCamera();
    }

    private void setupInputs(){

    }
    
    /** update method for camera to be called in update method in class extending VariableFrameRateGame */
    public void updateCamera(){
    }

    public Vector3f getCameraN(){
        return camera.getN();
    }

    public Vector3f getCameraU(){
        return camera.getU();
    }
}
