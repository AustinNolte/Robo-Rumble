package WizardGame;

import tage.*;
import tage.input.InputManager;
import tage.shapes.*;
import org.joml.*;

import net.java.games.input.Keyboard;

public class MyGame extends VariableFrameRateGame{
    
    //object notation [...]Obj, shape notation [...]S, texture notation [...]X
    private ObjShape ghostAvS,pAvS,xAxisS,yAxisS,zAxisS;
    private GameObject ghostAvObj,pAvObj,xAxisObj,yAxisObj,zAxisObj;
    private TextureImage ghostAvX,pAvX;
    private Light light1;
    private boolean isSprinting = false;

    //game timings
    private double lastFrameTime,currFrameTime,elapsedTime,timeSinceLastFrame;
    private boolean isStaminaZero;
    private float stamina = 100.0f;
    private int speed = 1;
    
    private CameraOrbit3D mainCamController;


    private static Engine engine;

    public MyGame(){
        super();
    }

    public static void main(String[] args){
        MyGame game = new MyGame();
        engine = new Engine(game);
        game.initializeSystem();
        game.game_loop();
    }

    @Override
    public void createViewports(){
        // ----------- remaking main viewport --------- 
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1,1);
    }

    @Override
    public void loadShapes() {
        pAvS = new Cube();
        ghostAvS = new Cube();
        xAxisS = new Line(new Vector3f(0,0,0), new Vector3f(10,0,0));
        yAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,10,0));
        zAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,0,10));     
    }

    @Override
    public void loadTextures() {
        pAvX = new TextureImage("CustomTexture1 - Cracked red bricks.png");
        ghostAvX = new TextureImage("CustomTexture2 - Camoflage.png");
    }

    @Override
    public void buildObjects() {
        Matrix4f initTranslation,initScale;
        
        initTranslation = new Matrix4f().identity();
		initScale = new Matrix4f().identity();

        //making x,y,z axis
		xAxisObj = new GameObject(GameObject.root(),xAxisS);
		initTranslation = new Matrix4f().translation(0,2,0);

		yAxisObj = new GameObject(GameObject.root(),yAxisS);
		yAxisObj.getRenderStates().setColor(new Vector3f(0,1,0));

		zAxisObj = new GameObject(GameObject.root(),zAxisS);
		zAxisObj.getRenderStates().setColor(new Vector3f(0,0,1));

        //making temp player obj
        pAvObj = new GameObject(GameObject.root(), pAvS, pAvX);
        initTranslation = new Matrix4f().translation(0,1.25f,0);
		initScale = new Matrix4f().scale(.5f);
		pAvObj.setLocalTranslation(initTranslation);
		pAvObj.setLocalScale(initScale);
    }

    @Override
    public void initializeLights() {
        //global ambient light will be a little less than full white
		Light.setGlobalAmbient(.8f, .8f, .8f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		//positional ambient light will be set to a custom rgb value I made, the RGB value in ints is 171,188,237
		light1.setAmbient(.67f, .73f, .93f);
		(engine.getSceneGraph()).addLight(light1);
    }

    @Override
    public void initializeGame() {

        // ----------- Setting up frame timings -----------
        lastFrameTime = System.currentTimeMillis();
        currFrameTime = System.currentTimeMillis();
        timeSinceLastFrame = 0.0f;
        elapsedTime = 0.0f;

        (engine.getRenderSystem()).setWindowDimensions(1280,720);
        
        // ----------- Setting up input objects -----------
        ForwardMovement FM = new ForwardMovement(this);
        GameSettingAction GSA = new GameSettingAction(this);
        StrafingMovement SM = new StrafingMovement(this);

        // ------------- setting up camera controller ----------- 
		Camera mainCam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		mainCamController = new CameraOrbit3D(mainCam, engine, pAvObj);

        // ----------- Forward and backward Movement of avatar ------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // ----------- Input for hold to sprint ------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Key.LSHIFT, GSA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._8, GSA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

        // ----------- Strafing left and right Movement of avatar ------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);


    }

    @Override
    public void update() {


		// ----------- Updating frame timings -----------
        lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();

		//overall elapsed game time in seconds
		elapsedTime += (currFrameTime - lastFrameTime)/1000;
		//time between last frame and this frame in seconds
		timeSinceLastFrame = (currFrameTime - lastFrameTime)/1000;   
        
        //updating camera and input manager
        engine.getInputManager().update((float)timeSinceLastFrame);
        mainCamController.updateCamera(); 
        
        // -----------  Handling sprinting and stamina usage -----------
        if(!isStaminaZero){
			//If you are sprinting it costs stamina and it regenerates half as fast as it is used. cannot go higher than 100 or less than 0
			if(isSprinting && stamina > 0.0){
				speed = 5;
				stamina -= 20*timeSinceLastFrame;
			}
			if(!(isSprinting) && stamina < 100.0){
				speed = 1;
				stamina += 5*timeSinceLastFrame;
			}
			if(stamina <= 0){
				isStaminaZero = true;
			}
		}else if(isStaminaZero){
			if(stamina < 20.0){
				speed = 1;
				stamina += 5*timeSinceLastFrame;
			}else{
				isStaminaZero = false;
			}
		}
    }
    
    public GameObject getAvatar(){
        return pAvObj;
    }

    public boolean getIsSprinting(){
        return isSprinting;
    }
    public void setIsSprinting(boolean isSprinting){
        this.isSprinting = isSprinting;
    }
    public int getSpeed(){
        return speed;
    }
}