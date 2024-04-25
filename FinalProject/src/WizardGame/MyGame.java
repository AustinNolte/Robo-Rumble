package WizardGame;

import tage.*;
import tage.input.InputManager;
import tage.networking.IGameConnection.ProtocolType;
import tage.shapes.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.lang.Math;

import org.joml.*;



public class MyGame extends VariableFrameRateGame{

    //object notation [...]Obj, shape notation [...]S, texture notation [...]X
    private ObjShape ghostAvS,pAvS,xAxisS,yAxisS,zAxisS,terrainS,stairsS;
    private GameObject ghostAvObj,pAvObj,xAxisObj,yAxisObj,zAxisObj,terrain,stairs1, stairs2;
    private TextureImage ghostAvX,pAvX,terrainX,terrainHeightMap,stairsHeightMap,stairsX;
    
    
    private Light light1;
    private boolean isSprinting = false;

    //game timings
    private double lastFrameTime,currFrameTime,elapsedTime,timeSinceLastFrame;
    private boolean isStaminaZero;
    private float stamina = 100.0f;
    private int speed = 1;

    //pointer for skybox textures
    private int customSkyBox;
    
    private CameraOrbit3D mainCamController;

    private Vector3f forwardVec;
    private float angleSigned;

    private static Engine engine;
    private GhostManager gm;

    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protClient;
    private boolean isClientConneted = false;
    private boolean isSinglePlayer = true;
    private boolean isAiming = false;


    // constructor for if in multiplayer
    public MyGame(String serverAddress, int serverPort, String protocol){
        super();
        gm = new GhostManager(this);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        isSinglePlayer = false;
        if(protocol.compareToIgnoreCase("TCP") == 0){
            this.serverProtocol = ProtocolType.TCP;
        }else{
            this.serverProtocol = ProtocolType.UDP;
        }

    }
    // constructor for if in singleplayer and not given any ip or ports in args
    public MyGame(){
        super();
        isSinglePlayer = true;
    }

    public static void main(String[] args){
        MyGame game;
        if(args.length > 0){
            game = new MyGame(args[0], Integer.parseInt((args[1])), args[2]);
        }else{
            game = new MyGame();
        }
        engine = new Engine(game);
        game.initializeSystem();
        game.game_loop();
    }
    
    @Override
    public void loadSkyBoxes(){
        customSkyBox = (engine.getSceneGraph()).loadCubeMap("customSkybox");
        (engine.getSceneGraph()).setActiveSkyBoxTexture(customSkyBox);
        (engine.getSceneGraph()).setSkyBoxEnabled(true);
    }
    
    @Override
    public void createViewports(){
        // ----------- remaking main viewport --------- 
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1,1);
    }

    @Override
    public void loadShapes() {
        pAvS = new ImportedModel("robot.obj");
        ghostAvS = new Cube();
        xAxisS = new Line(new Vector3f(0,0,0), new Vector3f(10,0,0));
        yAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,10,0));
        zAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,0,10));  
        terrainS = new TerrainPlane(1000);
        stairsS = new TerrainPlane(1000);   
    }

    @Override
    public void loadTextures() {
        
        pAvX = new TextureImage("robot.png");
        ghostAvX = new TextureImage("CustomTexture2 - Camoflage.png");
        
        terrainX = new TextureImage("test2.png");
        terrainHeightMap = new TextureImage("test.png");

        stairsX = new TextureImage("CustomTexture1 - Cracked bricks.png");
        stairsHeightMap = new TextureImage("StairsheightMap.png");
    }

    @Override
    public void buildObjects() {
        Matrix4f initTranslation,initScale,initRot;
        
        initTranslation = new Matrix4f().identity();
		initScale = new Matrix4f().identity();

        //making x,y,z axis
		xAxisObj = new GameObject(GameObject.root(),xAxisS);
        xAxisObj.getRenderStates().setColor(new Vector3f(1,0,0));

        
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
        initRot = new Matrix4f().identity();
        initRot.rotate((float)Math.toRadians(90), 1,0,0);
        pAvObj.getRenderStates().setModelOrientationCorrection(initRot);
        
        
        // making terrain
        terrain = new GameObject(GameObject.root(),terrainS,terrainX);
        initTranslation = new Matrix4f().translation(0,0,0);
        terrain.setLocalTranslation(initTranslation);
        initScale = new Matrix4f().scale(100,5,100);
        terrain.setLocalScale(initScale);
        terrain.setHeightMap(terrainHeightMap);

        // tilling terrain
        terrain.getRenderStates().setTiling(1);
        terrain.getRenderStates().setTileFactor(10);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        initRot = new Matrix4f().identity();
        
        // making first set of staris
        stairs1 = new GameObject(GameObject.root(),stairsS,stairsX);
        initTranslation = new Matrix4f().translation(10,1.75f,10);
        initScale = new Matrix4f().scale(10,10,10);
        stairs1.setLocalTranslation(initTranslation);
        stairs1.setLocalScale(initScale);
        stairs1.setHeightMap(stairsHeightMap);
        
        //tilling stairs
        stairs1.getRenderStates().setTiling(1);
        stairs1.getRenderStates().setTileFactor(10);

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

        // ------ setting up networking before making input objects etc -----------
        setupNetworking();
        
        
        // ----------- Setting up input objects -----------
        ForwardMovement FM = new ForwardMovement(this,protClient);
        GameSettingAction GSA = new GameSettingAction(this);
        StrafingMovement SM = new StrafingMovement(this,protClient);

        // ------------- setting up camera controller ----------- 
		Camera mainCam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		mainCamController = new CameraOrbit3D(mainCam, engine, pAvObj,this);

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
        
       // System.out.println(getStairs1Height(pAvObj.getLocalLocation().x, pAvObj.getLocalLocation().z));
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
        
        if(isAiming){
            forwardVec = ((new Vector3f(0,1,0)).cross(getCameraU())).normalize();
            angleSigned = (float) (forwardVec.angleSigned((getAvatar().getLocalForwardVector()).mul(-1), new Vector3f(0,1,0))*timeSinceLastFrame);
            pAvObj.globalYaw(angleSigned*3);
        }

        processNetworking((float)elapsedTime);
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

    public GhostManager getGhostManager() {
        return this.gm;
    }

    public TextureImage getGhostTextureImage(){
        return ghostAvX;
    }

    public ObjShape getGhostObjShape(){
        return ghostAvS;
    }

    public Engine getEngine(){
        return engine;
    }

    public Vector3f getCameraN(){
        return mainCamController.getCameraN();
    }

    public Vector3f getCameraU(){
        return mainCamController.getCameraU();
    }
    
    public float getTerrainHeight(float x, float z){
        return terrain.getHeight(x, z);
    }

    public float getStairs1Height(float x, float z){
        return stairs1.getHeight(x, z);
    }

    public float getStairs2Height(float x, float z){
        return stairs2.getHeight(x, z);
    }
    
    public void setIsAiming(boolean isAiming){
        this.isAiming = isAiming;
    }

    public boolean isAiming(){
        return isAiming;
    }
    // ------------- Networking part ------------

    public void setupNetworking(){
        isClientConneted = false;

        try{
            protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
        
        }catch(UnknownHostException e){
            e.printStackTrace();
        
        }catch(IOException e){
            e.printStackTrace();
        }
        if(protClient == null){
            System.out.println("missing protocol host");
        }else{
            System.out.println("sending join message");
            protClient.sendJoinMessage();
        }
    }

    protected void processNetworking(float elapsTime){
        if(protClient != null){
            protClient.processPackets();
        }
    }

    public void setIsConnected(boolean isClientConnected){
        this.isClientConneted = isClientConnected;
    }
}