package RobotGame;

import tage.*;
import tage.audio.*;
import tage.input.InputManager;
import tage.networking.IGameConnection.ProtocolType;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.shapes.*;
import java.awt.event.*;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.lang.Math;

import org.joml.*;



public class MyGame extends VariableFrameRateGame{

    //object notation [...]Obj, shape notation [...]S, texture notation [...]X, only one animated shape so it is just pAS for player Animated Shape
    private ObjShape ghostAvS,pAvS,xAxisS,yAxisS,zAxisS,groundPlaneS,stairsS,laserBeamS, fenceS, largeBoxS, wideBoxS, longBoxS, smallBoxS;
    private GameObject ghostAvObj,pAvObj,xAxisObj,yAxisObj,zAxisObj,groundPlaneObj,stairs1,stairs2; 
    private GameObject largeBox1, largeBox2, largeBox3, largeBox4, largeBox5;
    private GameObject smallBox1, smallBox2, smallBox3, smallBox4, smallBox5;
    private GameObject wideBox1, wideBox2, wideBox3, wideBox4, wideBox5;
    private GameObject longBox1, longBox2, longBox3, longBox4, longBox5;
    private TextureImage ghostAvX,pAvX,groundPlaneX,stairsHeightMap,stairsX, laserBeamX, fenceX, boxX;
    private AnimatedShape pAS;

    // phyiscs objects and engine
    private PhysicsEngine physicsEngine;
    private PhysicsObject pPhysicsObj, ghostAvPhysicsObj, groundPlanePhysicsObj;

    // float array vals for physics operations
    private float vals[] = new float[16];

    // phyiscs updates variables
    private Matrix4f mat,mat2,mat3,tempMove;
    private AxisAngle4f aa;
    private double[] tempTransform;

    private IAudioManager audioMgr;


    private float characterAdjust = 2.88f;
    
    
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
    public void loadSounds(){

    }
    
    @Override
    public void createViewports(){
        // ----------- remaking main viewport --------- 
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1,1);
    }

    @Override
    public void loadShapes() {
        pAS = new AnimatedShape("robotMesh.rkm", "robotSkeleton.rks");
        pAS.loadAnimation("Walk", "ForwardWalk.rka");

        ghostAvS = new Cube();
        xAxisS = new Line(new Vector3f(0,0,0), new Vector3f(10,0,0));
        yAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,10,0));
        zAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,0,10));  
        groundPlaneS =  new Plane();
        stairsS = new TerrainPlane(1000);   

        laserBeamS = new Cube();

        fenceS = new ImportedModel("iron-fence-border.obj");

        largeBoxS = new ImportedModel("box-large.obj");
        wideBoxS = new ImportedModel("box-wide.obj");
        smallBoxS = new ImportedModel("box-small.obj");
        longBoxS = new ImportedModel("box-large.obj");
    }

    @Override
    public void loadTextures() {
        
        pAvX = new TextureImage("robot.png");
        ghostAvX = new TextureImage("CustomTexture2 - Camoflage.png");
        
        groundPlaneX = new TextureImage("test2.png");

        stairsX = new TextureImage("CustomTexture1 - Cracked bricks.png");
        stairsHeightMap = new TextureImage("StairsheightMap.png");

        laserBeamX = new TextureImage("laserBeamTex.png");

        fenceX = new TextureImage("colormapGraveYard.png");

        boxX = new TextureImage("variation-a.png");
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
        pAvObj = new GameObject(GameObject.root(), pAS, pAvX);
        initTranslation = new Matrix4f().translation(0,-4.8f,0);
		pAvObj.setLocalTranslation(initTranslation);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        initRot = new Matrix4f().identity();
        
        // making ground plane
        groundPlaneObj = new GameObject(GameObject.root(),groundPlaneS,groundPlaneX);
        initTranslation = new Matrix4f().translation(0,-5,0);
        initScale = new Matrix4f().scale(1000);
        groundPlaneObj.setLocalTranslation(initTranslation);
        groundPlaneObj.setLocalScale(initScale);

        // tilling ground plane
        groundPlaneObj.getRenderStates().setTiling(1);
        groundPlaneObj.getRenderStates().setTileFactor(10);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        initRot = new Matrix4f().identity();

        // making world border on negative x axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(-275,-4.9f,0 + i*50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(-275,-4.9f,0 + i*-50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }

        // making world border positive x axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(275,-4.9f,0 + i*50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(275,-4.9f,0 + i*-50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        // making world positive z axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * 50,-4.9f,275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * -50,-4.9f,275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }

        // making world negative z axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * 50,-4.9f,-275);
            initScale = new Matrix4f().scale(100,10,50);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * -50,-4.9f,-275);
            initScale = new Matrix4f().scale(100,10,50);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }
        
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        //----------  making cardboard boxes in map for cover ------------
        largeBox1 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(141,-4.9f,179);
        initScale = new Matrix4f().scale(5);

        largeBox1.setLocalTranslation(initTranslation);
        largeBox1.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox2 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(-155,-4.9f,84);
        initScale = new Matrix4f().scale(5);

        largeBox2.setLocalTranslation(initTranslation);
        largeBox2.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox3 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(145,-4.9f,-61);
        initScale = new Matrix4f().scale(6);

        largeBox3.setLocalTranslation(initTranslation);
        largeBox3.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox4 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(-67,-4.9f,-154);
        initScale = new Matrix4f().scale(4);

        largeBox4.setLocalTranslation(initTranslation);
        largeBox4.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox5 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(-151,-4.9f,-180);
        initScale = new Matrix4f().scale(7);

        largeBox5.setLocalTranslation(initTranslation);
        largeBox5.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox1 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(-247,-4.9f,-254);
        initScale = new Matrix4f().scale(5);
        wideBox1.setLocalTranslation(initTranslation);
        wideBox1.setLocalScale(initScale);
        wideBox1.globalYaw((float)Math.toRadians(50));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        smallBox1 = new GameObject(wideBox1, smallBoxS, boxX);
        initTranslation = new Matrix4f().translation(1.2f,2.5f,1.3f);
        initScale = new Matrix4f().scale(.5f);

        smallBox1.propagateRotation(false);
        smallBox1.setLocalTranslation(initTranslation);
        smallBox1.setLocalScale(initScale);
        
        //// making first set of staris
        //stairs1 = new GameObject(GameObject.root(),stairsS,stairsX);
        //initTranslation = new Matrix4f().translation(100,1.75f,10);
        //initScale = new Matrix4f().scale(10,10,10);
        //stairs1.setLocalTranslation(initTranslation);
        //stairs1.setLocalScale(initScale);
        //stairs1.setHeightMap(stairsHeightMap);
        //
        ////tilling stairs
        //stairs1.getRenderStates().setTiling(1);
        //stairs1.getRenderStates().setTileFactor(10);

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

        for(GameObject go: engine.getSceneGraph().getGameObjects()){
            System.out.println(go);
        }
        // --------- initalize phyiscs system ---------
        float[] gravity = {0f,0f,0f};
        physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
        physicsEngine.setGravity(gravity);
        
        // --------- Adding Phyiscs Objects  --------------
        float mass = 1.0f;
        float up[] = {0,1,0};
        float[] size = {2,5.5f,1};

        
        Matrix4f translation = new Matrix4f(pAvObj.getLocalTranslation());
        tempTransform = toDoubleArray(translation.get(vals));
        pPhysicsObj = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);

        pAvObj.setPhysicsObject(pPhysicsObj);

        translation = new Matrix4f(groundPlaneObj.getLocalTranslation());
        tempTransform = toDoubleArray(translation.get(vals));
        groundPlanePhysicsObj = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform,up,0.0f);

        groundPlaneObj.setPhysicsObject(groundPlanePhysicsObj);
        
        // ----------- Setting up input objects -----------
        ForwardMovement FM = new ForwardMovement(this,protClient);
        GameSettingAction GSA = new GameSettingAction(this);
        StrafingMovement SM = new StrafingMovement(this,protClient);
        FireAction fA = new FireAction(this, laserBeamS,laserBeamX, protClient);

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

        // ------------ Fire Laser Beam action ----------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Z, fA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Y, fA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

        // ------------- setting up sound parameters --------------


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
        System.out.println(pAvObj.getWorldLocation().x + ", " + pAvObj.getWorldLocation().z);
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
        
        // ----- player animation update -----
        pAS.updateAnimation();

        // ---------- Handling phyiscs updates -----------
        checkForCollisions();

        aa = new AxisAngle4f();
        mat = new Matrix4f().identity();
        mat2 = new Matrix4f().identity();
        mat3 = new Matrix4f().identity();
        Matrix4f translation = new Matrix4f().identity();
        
        physicsEngine.update((float)timeSinceLastFrame*1000);
        
        translation = new Matrix4f(pAvObj.getLocalTranslation());
        tempTransform = toDoubleArray(translation.get(vals));
        pAvObj.getPhysicsObject().setTransform(tempTransform);
                
        // this is purely for updating laser positioning based on phyiscs objects movement
        for(GameObject go: engine.getSceneGraph().getGameObjects()){
            if(go != pAvObj && go != ghostAvObj && go != groundPlaneObj && go.getPhysicsObject() != null){
                go.getPhysicsObject().applyForce(getCameraN().x*1000, getCameraN().y*1000, getCameraN().z*1000, 0, 0, 0);
                mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
                mat2.set(3,0,mat.m30());
                mat2.set(3,1,mat.m31());
                mat2.set(3,2,mat.m32());
                go.setLocalTranslation(mat2);
            }
        }
        ghostAvObj = gm.getGhostAvatar();
        if(ghostAvObj != null){
            translation = new Matrix4f(ghostAvObj.getLocalTranslation());
            tempTransform = toDoubleArray(translation.get(vals));
            ghostAvObj.getPhysicsObject().setTransform(tempTransform);
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
    
    public Vector3f getCameraV(){
        return mainCamController.getCameraV();
    }

    public float getStairs1Height(float x, float z){
        return -5;
        //return stairs1.getHeight(x, z);
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

    public float getCharacterAdjust(){
        return characterAdjust;
    }

    public AnimatedShape getPlayerSkeleton(){
        return pAS;
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
    
    @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyCode()){
            case KeyEvent.VK_1: engine.enablePhysicsWorldRender(); break;
            case KeyEvent.VK_2: engine.disablePhysicsWorldRender(); break;
        }
        super.keyPressed(e);
    }

    // ---------- Phyiscs World Utility Functions ---------
    public float[] toFloatArray(double[] arr){
        if(arr == null){
            return null;
        }else{
            float[] ret = new float[arr.length];
            for (int i = 0; i<arr.length; i++){
                ret[i] = (float)arr[i];
            }
            return ret;
        }
    }
    
    public double[] toDoubleArray(float[] arr){
        if(arr == null){
            return null;
        }else{
            double[] ret = new double[arr.length];
            for (int i = 0; i<arr.length; i++){
                ret[i] = (double)arr[i];
            }
            return ret;
        }
    }

    private void checkForCollisions(){
        com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
        com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
        com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
        com.bulletphysics.dynamics.RigidBody object1,object2;
        com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

        dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
        dispatcher = dynamicsWorld.getDispatcher();
        for(int i = 0; i<dispatcher.getNumManifolds(); i++){
            manifold = dispatcher.getManifoldByIndexInternal(i);
            object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
            object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
            JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
            JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);

            for(int j =0; j<manifold.getNumContacts(); j++){
                contactPoint = manifold.getContactPoint(j);
                if(contactPoint.getDistance() < 0.0f){
                    //System.out.println("---- hit between " + obj1 + " and " + obj2);
                    break;
                }
            }


        }
    }
}