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
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math;

import org.joml.*;

import Server.NPC;



public class MyGame extends VariableFrameRateGame{

    //object notation [...]Obj, shape notation [...]S, texture notation [...]X, only one animated shape so it is just pAS for player Animated Shape
    private ObjShape xAxisS,yAxisS,zAxisS,groundPlaneS,laserBeamS, fenceS, largeBoxS, wideBoxS, smallBoxS, npcS, myHouseS,myLampPostS;
    private GameObject ghostAvObj,xAxisObj,yAxisObj,zAxisObj,groundPlaneObj, myHouse, myLampPost1, myLampPost2, myLampPost3, myLampPost4, npcObj; 
    private PlayerCharacter pAvObj;
    private GameObject largeBox1, largeBox2, largeBox3, largeBox4, largeBox5;
    private PhysicsObject largeBox1PhysicsObj, largeBox2PhysicsObj,  largeBox3PhysicsObj,  largeBox4PhysicsObj,  largeBox5PhysicsObj;
    private GameObject smallBox1, smallBox2, smallBox3, smallBox4, smallBox5;
    private PhysicsObject smallBox1PhysicsObject, smallbox2PhysicsObject, smallbox3PhysicsObject, smallbox4PhysicsObject, smallbox5PhysicsObject;
    private GameObject wideBox1, wideBox2, wideBox3, wideBox4, wideBox5;
    private PhysicsObject widebox1PhysicsObject, widebox2PhysicsObject, widebox3PhysicsObject, widebox4PhysicsObject, widebox5PhysicsObject;
    private TextureImage ghostAvX,pAvX,option1,option2,groundPlaneX,groundPlaneHeightMap, laserBeamX, fenceX, boxX, npcX, myHouseX, myLampPostX;
    private AnimatedShape pAS, gAS;
    // to store and iterate through all box physics objects
    private ArrayList<PhysicsObject> boxPhyiscsObjects = new ArrayList<PhysicsObject>();

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
    private Sound laserSound,ghostLaserSound,npcLaserSound,deathSound,bgMusic;

    private float characterAdjust = 4.8f;
    
    private ForwardMovement FM;
    private GameSettingAction GSA;
    private StrafingMovement SM;
    private FireAction fA;
    
    private Light light1,lampPost1Light, lampPost2Light,lampPost3Light,lampPost4Light;
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

    private boolean isDead = false;

    private static Engine engine;
    private GhostManager gm;

    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protClient;
    private boolean isClientConnected = false;
    private boolean isSinglePlayer = true;

    // for choosing what skin you want to use
    private static String option;


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
        isClientConnected = false;
    }

    public static void main(String[] args){
        MyGame game;
        if(args.length > 1){
            game = new MyGame(args[0], Integer.parseInt((args[1])), args[2]);
            option = args[3];
        }else{
            game = new MyGame();
            option = args[0];
        }
        engine = new Engine(game);
        game.initializeSystem();
        game.game_loop();
    }
    
    @Override
    public void loadSkyBoxes(){
        customSkyBox = (engine.getSceneGraph()).loadCubeMap("customSkybox1");
        (engine.getSceneGraph()).setActiveSkyBoxTexture(customSkyBox);
        (engine.getSceneGraph()).setSkyBoxEnabled(true);
    }

    @Override
    public void loadSounds(){
        AudioResource resource1,resource2,resource3;
        audioMgr = (engine.getAudioManager());
        resource1 = audioMgr.createAudioResource("assets/sounds/computerNoise_000.wav", AudioResourceType.AUDIO_SAMPLE);
        resource2 = audioMgr.createAudioResource("assets/sounds/laserRetro_004.wav",AudioResourceType.AUDIO_SAMPLE);
        resource3 = audioMgr.createAudioResource("assets/sounds/jingles_PIZZI12.wav",AudioResourceType.AUDIO_STREAM);

        laserSound = new Sound(resource2,SoundType.SOUND_EFFECT,50,false);
        ghostLaserSound = new Sound(resource2,SoundType.SOUND_EFFECT,50,false);
        npcLaserSound = new Sound(resource2,SoundType.SOUND_EFFECT,50,false);
        deathSound = new Sound(resource1,SoundType.SOUND_EFFECT,50,false);
        bgMusic = new Sound(resource3,SoundType.SOUND_MUSIC,50,true);

        laserSound.initialize(audioMgr);
        ghostLaserSound.initialize(audioMgr);
        deathSound.initialize(audioMgr);
        npcLaserSound.initialize(audioMgr);
        bgMusic.initialize(audioMgr);


        laserSound.setMaxDistance(10f);
        laserSound.setMinDistance(0.05f);
        laserSound.setRollOff(1);
        
        ghostLaserSound.setMaxDistance(10f);
        ghostLaserSound.setMinDistance(0.05f);
        ghostLaserSound.setRollOff(1);

        npcLaserSound.setMaxDistance(10f);
        npcLaserSound.setMinDistance(0.05f);
        npcLaserSound.setRollOff(1);

        deathSound.setMaxDistance(50f);
        deathSound.setMinDistance(0.05f);
        deathSound.setRollOff(5);

    }
    
    @Override
    public void createViewports(){
        // ----------- remaking main viewport --------- 
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1,1);
    }

    @Override
    public void loadShapes() {
        pAS = new AnimatedShape("robotMeshTest.rkm", "robotSkeletonTest.rks");
        pAS.loadAnimation("WALK", "ForwardWalkTest.rka");

        gAS = new AnimatedShape("robotMesh.rkm", "robotSkeleton.rks");
        gAS.loadAnimation("WALK", "ForwardWalkTest.rka");

        xAxisS = new Line(new Vector3f(0,0,0), new Vector3f(10,0,0));
        yAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,10,0));
        zAxisS = new Line(new Vector3f(0,0,0), new Vector3f(0,0,10));  
        groundPlaneS = new TerrainPlane(1000);   

        npcS = new ImportedModel("siege-ballista.obj");

        myHouseS = new ImportedModel("myHouse.obj");

        myLampPostS = new ImportedModel("myLampPost.obj");

        laserBeamS = new Cube();

        fenceS = new ImportedModel("iron-fence-border.obj");
        
        largeBoxS = new ImportedModel("box-large.obj");
        wideBoxS = new ImportedModel("box-wide.obj");
        smallBoxS = new ImportedModel("box-small.obj");
    }

    @Override
    public void loadTextures() {
        

        option1 = new TextureImage("robot2.png");
        option2 = new TextureImage("robot.png");
        
        groundPlaneX = new TextureImage("coast_land_rocks_01_diff_1k.jpg");
        groundPlaneHeightMap = new TextureImage("coast_land_rocks_01_disp_1k.jpg");

        laserBeamX = new TextureImage("laserBeamTex.png");

        fenceX = new TextureImage("colormapGraveYard.png");

        boxX = new TextureImage("variation-a.png");

        npcX = new TextureImage("colormapCastleKit.png");

        myHouseX = new TextureImage("myHouse.png");
        
        myLampPostX = new TextureImage("myLampPost.png");
    }

    @Override
    public void buildObjects() {
        Matrix4f initTranslation,initScale,initRot;
        
        initTranslation = new Matrix4f().identity();
		initScale = new Matrix4f().identity();
        // making house
        myHouse = new GameObject(GameObject.root(),myHouseS,myHouseX);
        initTranslation = new Matrix4f().translation(0,-4.5f,-300);
        initScale = new Matrix4f().scale(10);
        myHouse.setLocalTranslation(initTranslation);
        myHouse.setLocalScale(initScale);
        myHouse.getRenderStates().setModelOrientationCorrection(new Matrix4f().rotateX((float)Math.toRadians(90)));

        //making x,y,z axis
		xAxisObj = new GameObject(GameObject.root(),xAxisS);
        xAxisObj.getRenderStates().setColor(new Vector3f(1,0,0));

        
		yAxisObj = new GameObject(GameObject.root(),yAxisS);
		yAxisObj.getRenderStates().setColor(new Vector3f(0,1,0));
        
		zAxisObj = new GameObject(GameObject.root(),zAxisS);
		zAxisObj.getRenderStates().setColor(new Vector3f(0,0,1));
        
        //making player obj
        if(option.compareToIgnoreCase("blue")== 0){
            pAvX = option2;
        }else if(option.compareToIgnoreCase("red") == 0){
            pAvX = option1;
        }
        pAvObj = new PlayerCharacter(GameObject.root(), pAS, pAvX, 100.0f);
        initTranslation = new Matrix4f().translation(0,-1.2f,0);
		pAvObj.setLocalTranslation(initTranslation);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        initRot = new Matrix4f().identity();
        
        // making ground plane
        groundPlaneObj = new GameObject(GameObject.root(),groundPlaneS,groundPlaneX);
        initTranslation = new Matrix4f().translation(0,-5,0);
        initScale = new Matrix4f().scale(1000,10,1000);
        groundPlaneObj.setLocalTranslation(initTranslation);
        groundPlaneObj.setLocalScale(initScale);
        groundPlaneObj.setHeightMap(groundPlaneHeightMap);

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
            initTranslation = new Matrix4f().translation(-275,.05f,0 + i*50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(-275,.05f,0 + i*-50);
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
            initTranslation = new Matrix4f().translation(275,.05f,0 + i*50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(275,.05f,0 + i*-50);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
            fence.globalYaw((float)Math.toRadians(90));
        }
        // making world border positive z axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * 50,.05f,275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * -50,.05f,275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }

        // making world border negative z axis
        for(int i = 0; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * 50,.05f,-275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }
        for(int i = 1; i<6; i++){
            initTranslation = new Matrix4f().identity();
            initScale = new Matrix4f().identity();
            
            GameObject fence = new GameObject(GameObject.root(),fenceS,fenceX);
            initTranslation = new Matrix4f().translation(i * -50,.05f,-275);
            initScale = new Matrix4f().scale(50,10,25);
            fence.setLocalTranslation(initTranslation);
            fence.setLocalScale(initScale);
        }

        // making lamp posts, one in each corner of the fence barrier
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        initRot = new Matrix4f().identity();

        GameObject myLampPost1 = new GameObject(GameObject.root(),myLampPostS,myLampPostX);
        initTranslation = new Matrix4f().translation(280,-.75f,280);
        initScale = new Matrix4f().scale(2);
        initRot = new Matrix4f().rotateX((float)Math.toRadians(90));
        myLampPost1.getRenderStates().setModelOrientationCorrection(initRot);
        myLampPost1.setLocalTranslation(initTranslation);
        myLampPost1.setLocalScale(initScale);
        myLampPost1.globalYaw((float)Math.toRadians(135));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        GameObject myLampPost2 = new GameObject(GameObject.root(),myLampPostS,myLampPostX);
        initTranslation = new Matrix4f().translation(-280,-.75f,280);
        initScale = new Matrix4f().scale(2);
        initRot = new Matrix4f().rotateX((float)Math.toRadians(90));
        myLampPost2.getRenderStates().setModelOrientationCorrection(initRot);
        myLampPost2.setLocalTranslation(initTranslation);
        myLampPost2.setLocalScale(initScale);
        myLampPost2.globalYaw((float)Math.toRadians(45));


        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        GameObject myLampPost3 = new GameObject(GameObject.root(),myLampPostS,myLampPostX);
        initTranslation = new Matrix4f().translation(-280,-.75f,-280);
        initScale = new Matrix4f().scale(2);
        initRot = new Matrix4f().rotateX((float)Math.toRadians(90));
        myLampPost3.getRenderStates().setModelOrientationCorrection(initRot);
        myLampPost3.setLocalTranslation(initTranslation);
        myLampPost3.setLocalScale(initScale);
        myLampPost3.globalYaw((float)Math.toRadians(-45));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        GameObject myLampPost4 = new GameObject(GameObject.root(),myLampPostS,myLampPostX);
        initTranslation = new Matrix4f().translation(280,-.75f,-280);
        initScale = new Matrix4f().scale(2);
        initRot = new Matrix4f().rotateX((float)Math.toRadians(90));
        myLampPost4.getRenderStates().setModelOrientationCorrection(initRot);
        myLampPost4.setLocalTranslation(initTranslation);
        myLampPost4.setLocalScale(initScale);
        myLampPost4.globalYaw((float)Math.toRadians(-135));
        
        
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        //----------  making large cardboard boxes in map for cover ------------
        largeBox1 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(141,-.75f,-179);
        initScale = new Matrix4f().scale(5);

        largeBox1.setLocalTranslation(initTranslation);
        largeBox1.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox2 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(-155,-.75f,84);
        initScale = new Matrix4f().scale(5);

        largeBox2.setLocalTranslation(initTranslation);
        largeBox2.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox3 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(145,-.75f,-61);
        initScale = new Matrix4f().scale(6);

        largeBox3.setLocalTranslation(initTranslation);
        largeBox3.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();
        
        largeBox4 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(-67,-.75f,-154);
        initScale = new Matrix4f().scale(4);

        largeBox4.setLocalTranslation(initTranslation);
        largeBox4.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox5 = new GameObject(GameObject.root(), largeBoxS, boxX);
        initTranslation = new Matrix4f().translation(151,-.75f,-180);
        initScale = new Matrix4f().scale(7);

        largeBox5.setLocalTranslation(initTranslation);
        largeBox5.setLocalScale(initScale);
        
        //----------  making wide cardboard boxes with small ones on top in map for cover ------------

        // wide and small box 1
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox1 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(-247,-.75f,-254);
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

        // wide and small box 2
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox2 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(-38,-.75f,-177);
        initScale = new Matrix4f().scale(5);
        wideBox2.setLocalTranslation(initTranslation);
        wideBox2.setLocalScale(initScale);
        wideBox2.globalYaw((float)Math.toRadians(70));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        smallBox2 = new GameObject(wideBox2, smallBoxS, boxX);
        initTranslation = new Matrix4f().translation(1.2f,2.5f,1.3f);
        initScale = new Matrix4f().scale(.25f);

        smallBox2.propagateRotation(false);
        smallBox2.setLocalTranslation(initTranslation);
        smallBox2.setLocalScale(initScale);

        // wide and small box 3
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox3 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(167,-.75f,-117);
        initScale = new Matrix4f().scale(5);
        wideBox3.setLocalTranslation(initTranslation);
        wideBox3.setLocalScale(initScale);
        wideBox3.globalYaw((float)Math.toRadians(110));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        smallBox3 = new GameObject(wideBox3, smallBoxS, boxX);
        initTranslation = new Matrix4f().translation(1.2f,2.5f,1.3f);
        initScale = new Matrix4f().scale(1f);

        smallBox3.propagateRotation(false);
        smallBox3.setLocalTranslation(initTranslation);
        smallBox3.setLocalScale(initScale);

        // wide and small box 4
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox4 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(71,-.75f,-60);
        initScale = new Matrix4f().scale(5);
        wideBox4.setLocalTranslation(initTranslation);
        wideBox4.setLocalScale(initScale);

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        smallBox4 = new GameObject(wideBox4, smallBoxS, boxX);
        initTranslation = new Matrix4f().translation(1.2f,2.5f,1.3f);
        initScale = new Matrix4f().scale(.5f);

        smallBox4.propagateRotation(false);
        smallBox4.setLocalTranslation(initTranslation);
        smallBox4.setLocalScale(initScale);

        // wide and small box 5
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        wideBox5 = new GameObject(GameObject.root(),wideBoxS, boxX);
        initTranslation = new Matrix4f().translation(-250,0,60);
        initScale = new Matrix4f().scale(5);
        wideBox5.setLocalTranslation(initTranslation);
        wideBox5.setLocalScale(initScale);
        wideBox5.globalYaw((float)Math.toRadians(23));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        smallBox5 = new GameObject(wideBox5, smallBoxS, boxX);
        initTranslation = new Matrix4f().translation(1f,2.5f,1.3f);
        initScale = new Matrix4f().scale(.5f);

        smallBox5.propagateRotation(false);
        smallBox5.setLocalTranslation(initTranslation);
        smallBox5.setLocalScale(initScale);

        //----------  making large cardboard boxes in map for cover ------------
        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox1 = new GameObject(GameObject.root(),largeBoxS,boxX);
        initTranslation = new Matrix4f().translation(128, -.75f,83);
        initScale = new Matrix4f().scale(10);

        largeBox1.setLocalTranslation(initTranslation);
        largeBox1.setLocalScale(initScale);
        largeBox1.globalYaw((float)Math.toRadians(50));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox2 = new GameObject(GameObject.root(),largeBoxS,boxX);
        initTranslation = new Matrix4f().translation(240, -.75f,120);
        initScale = new Matrix4f().scale(9);

        largeBox2.setLocalTranslation(initTranslation);
        largeBox2.setLocalScale(initScale);
        largeBox2.globalYaw((float)Math.toRadians(13));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox3 = new GameObject(GameObject.root(),largeBoxS,boxX);
        initTranslation = new Matrix4f().translation(-194, -.75f,94);
        initScale = new Matrix4f().scale(8);

        largeBox3.setLocalTranslation(initTranslation);
        largeBox3.setLocalScale(initScale);
        largeBox3.globalYaw((float)Math.toRadians(70));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox4 = new GameObject(GameObject.root(),largeBoxS,boxX);
        initTranslation = new Matrix4f().translation(79, -.75f,51);
        initScale = new Matrix4f().scale(9);

        largeBox4.setLocalTranslation(initTranslation);
        largeBox4.setLocalScale(initScale);
        largeBox4.globalYaw((float)Math.toRadians(99));

        initTranslation = new Matrix4f().identity();
        initScale = new Matrix4f().identity();

        largeBox5 = new GameObject(GameObject.root(),largeBoxS,boxX);
        initTranslation = new Matrix4f().translation(211, -.75f,20);
        initScale = new Matrix4f().scale(10);

        largeBox5.setLocalTranslation(initTranslation);
        largeBox5.setLocalScale(initScale);
        largeBox5.globalYaw((float)Math.toRadians(58));

    }

    @Override
    public void initializeLights() {
        //global ambient light will be a little less than full white
		Light.setGlobalAmbient(.8f, .8f, .8f);

        // --------- Adding lamp post lights -----------//
        lampPost1Light = new Light();
        lampPost1Light.setLocation(new Vector3f(277,5.2f,277));
        lampPost1Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
        (engine.getSceneGraph()).addLight(lampPost1Light);

        lampPost2Light = new Light();
        lampPost2Light.setLocation(new Vector3f(-277,5.2f,277));
        lampPost2Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
        (engine.getSceneGraph()).addLight(lampPost2Light);

        lampPost3Light = new Light();
        lampPost3Light.setLocation(new Vector3f(-277,5.2f,-277));
        lampPost3Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
        (engine.getSceneGraph()).addLight(lampPost3Light);

        lampPost4Light = new Light();
        lampPost4Light.setLocation(new Vector3f(277,5.2f,-277));
        lampPost4Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
        (engine.getSceneGraph()).addLight(lampPost4Light);
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
        if(!isSinglePlayer){
            setupNetworking();
        }

        // --------- initalize phyiscs system ---------
        float[] gravity = {0f,-5f,0f};
        physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
        physicsEngine.setGravity(gravity);
        
        // --------- Adding Phyiscs Objects  --------------
        float mass = 1.0f;
        float up[] = {0,1,0};
        float[] size = {2,5.5f,1};

        
        Matrix4f translation = new Matrix4f().identity().translate(pAvObj.getLocalLocation().add(0,(float)characterAdjust,0)).mul(pAvObj.getLocalRotation());
        tempTransform = toDoubleArray(translation.get(vals));
        pPhysicsObj = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, size);
        pAvObj.setPhysicsObject(pPhysicsObj);

        translation = new Matrix4f(groundPlaneObj.getLocalTranslation());
        tempTransform = toDoubleArray(translation.get(vals));
        groundPlanePhysicsObj = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform,up,0.0f);
        groundPlaneObj.setPhysicsObject(groundPlanePhysicsObj);
        
        // ----------- Setting up input objects -----------
        FM = new ForwardMovement(this,protClient);
        GSA = new GameSettingAction(this);
        SM = new StrafingMovement(this,protClient);
        fA = new FireAction(this, laserBeamS,laserBeamX, protClient);

        // ------------- setting up camera controller ----------- 
		Camera mainCam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();

		mainCamController = new CameraOrbit3D(mainCam, engine, pAvObj,this);

        // ----------- Forward and backward Movement of avatar ------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, FM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // ----------- Input for hold to sprint ------
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LSHIFT, GSA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._8, GSA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

        // ----------- Strafing left and right Movement of avatar ------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, SM, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // ------------ Fire Laser Beam action ----------
        engine.getInputManager().associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Z, fA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Y, fA, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

        // ------------- setting up sound parameters --------------
        setEarParameters();
        bgMusic.play();
    }

    public void setEarParameters(){
        Camera camera = (engine.getRenderSystem().getViewport("MAIN").getCamera());
        audioMgr.getEar().setLocation(pAvObj.getWorldLocation());
        audioMgr.getEar().setOrientation(camera.getN(),new Vector3f(0.0f,1.0f,0.0f));
    }

    @Override
    public void update() {

        if(!isDead){
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
            // ----- player animation update -----
            pAS.updateAnimation();
            gAS.updateAnimation();
    
            // ---------- Handling phyiscs updates -----------
            checkForCollisions();
    
            aa = new AxisAngle4f();
            mat = new Matrix4f().identity();
            mat2 = new Matrix4f().identity();
            mat3 = new Matrix4f().identity();
            Matrix4f physicsMatrix = new Matrix4f().identity();
            
            physicsEngine.update((float)timeSinceLastFrame*10000);
            
            physicsMatrix = new Matrix4f().identity().translate(pAvObj.getLocalLocation().add(0,2.88f,0)).mul(pAvObj.getLocalRotation());
            tempTransform = toDoubleArray(physicsMatrix.get(vals));
            pAvObj.getPhysicsObject().setTransform(tempTransform);
            // this is purely for updating laser positioning based on phyiscs objects movement
            for(GameObject go: engine.getSceneGraph().getGameObjects()){
                if(gm.getGhostNPC() == null){
                    if(go != pAvObj && go != ghostAvObj && go != groundPlaneObj &&  go.getPhysicsObject() != null){
                        mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
                        mat2.set(3,0,mat.m30());
                        mat2.set(3,1,mat.m31());
                        mat2.set(3,2,mat.m32());
                        go.setLocalTranslation(mat2);
                    }
                }else if (gm.getGhostNPC() != null){
                    if(go != pAvObj && go != ghostAvObj && go != groundPlaneObj &&  go.getPhysicsObject() != null && go != gm.getNPCList().get(0)){
                        mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
                        mat2.set(3,0,mat.m30());
                        mat2.set(3,1,mat.m31());
                        mat2.set(3,2,mat.m32());
                        go.setLocalTranslation(mat2);
                    }
                }
            }
            if(!isSinglePlayer){
                ghostAvObj = gm.getGhostAvatar();
                if(ghostAvObj != null){
                    physicsMatrix = new Matrix4f().identity().translate(ghostAvObj.getLocalLocation().add(0,2.88f,0)).mul(ghostAvObj.getLocalRotation());;
                    tempTransform = toDoubleArray(physicsMatrix.get(vals));
                    ghostAvObj.getPhysicsObject().setTransform(tempTransform);
                }

                npcObj = gm.getGhostNPC();
                if(npcObj != null){
                    physicsMatrix = new Matrix4f().identity().translate(npcObj.getLocalLocation().add(0,2.88f,0)).mul(npcObj.getLocalRotation());;
                    tempTransform = toDoubleArray(physicsMatrix.get(vals));
                    npcObj.getPhysicsObject().setTransform(tempTransform);
                }
            }
            
            if(!isSinglePlayer){
                processNetworking((float)elapsedTime);
            }
        }
        // --------- setting up hud -----------//
        String healthS = Float.toString(pAvObj.getHealth());
        Vector3f healthColor = new Vector3f(255/255,125/255,125/255);
        String firstHudElement = "Health: " + healthS;
        if(pAvObj.getHealth() <=0){
            firstHudElement = "YOU DIED! Try again by restarting game";
        }
        (engine.getHUDmanager()).setHUD1(firstHudElement,healthColor,15,15);


        String staminaS = Float.toString((float)(Math.round(stamina*100.0)/100.0));
        Vector3f staminaColor = new Vector3f(0,1,1);
        String secondHudElement = "Stamina: " + staminaS;
        (engine.getHUDmanager()).setHUD2(secondHudElement,staminaColor,500,15);

        // --------- updating sound ------------ //
        bgMusic.setLocation(pAvObj.getWorldLocation());
        setEarParameters();
		
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

    public TextureImage getNPCTextureImage(){
        return npcX;
    }

    public AnimatedShape getGhostObjShape(){
        return gAS;
    }

    public ObjShape getNPCObjShape(){
        return npcS;
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

    public float getTerrainHeight(float x, float z){
        return groundPlaneObj.getHeight(x, z);
    }  

    public float getCharacterAdjust(){
        return characterAdjust;
    }

    public AnimatedShape getPlayerSkeleton(){
        return pAS;
    }

    public ObjShape getLaserShape(){
        return laserBeamS;
    }

    public TextureImage getLaserImage(){
        return laserBeamX;
    }

    public Sound getLaserSound(){
        return laserSound;
    }
    
    public Sound getGhostLaserSound(){
        return ghostLaserSound;
    }

    public Sound getDeathSound(){
        return deathSound;
    }
    
    public Sound getNpcLaserSound(){
        return npcLaserSound;
    }

    public PhysicsObject getAvatarPhysicsObject(){
        return pPhysicsObj;
    }

    public String getSkinOption(){
        return option;
    }

    public TextureImage getOption1(){
        return option1;
    }

    public TextureImage getOption2(){
        return option2;
    }

    public boolean isClientConnected(){
        return isClientConnected;
    }
    
    // ------------- Networking part ------------

    public void setupNetworking(){
        isClientConnected = false;

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
        this.isClientConnected = isClientConnected;
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyCode()){
            case KeyEvent.VK_1: engine.enablePhysicsWorldRender(); break;
            case KeyEvent.VK_2: engine.disablePhysicsWorldRender(); break;
            // turn off lights
            case KeyEvent.VK_5:{ 
                lampPost1Light.setAmbient(0, 0, 0);
                lampPost1Light.setDiffuse(0, 0, 0);
                lampPost1Light.setSpecular(0, 0, 0);
                lampPost2Light.setAmbient(0, 0, 0);
                lampPost2Light.setDiffuse(0, 0, 0);
                lampPost2Light.setSpecular(0, 0, 0);
                lampPost3Light.setAmbient(0, 0, 0);
                lampPost3Light.setDiffuse(0, 0, 0);
                lampPost3Light.setSpecular(0, 0, 0);
                lampPost4Light.setAmbient(0, 0, 0);
                lampPost4Light.setDiffuse(0, 0, 0);
                lampPost4Light.setSpecular(0, 0, 0);
                break;
            }
            // turn lights back on
            case KeyEvent.VK_6:{
                lampPost1Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
                lampPost1Light.setDiffuse(0.8f, 0.8f, 0.8f);
                lampPost1Light.setSpecular(1.0f, 1.0f, 1.0f);
                lampPost1Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
                lampPost2Light.setDiffuse(0.8f, 0.8f, 0.8f);
                lampPost2Light.setSpecular(1.0f, 1.0f, 1.0f);
                lampPost1Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
                lampPost3Light.setDiffuse(0.8f, 0.8f, 0.8f);
                lampPost3Light.setSpecular(1.0f, 1.0f, 1.0f);
                lampPost1Light.setAmbient(247f/255f, 250f/255f, 187f/255f);
                lampPost4Light.setDiffuse(0.8f, 0.8f, 0.8f);
                lampPost4Light.setSpecular(1.0f, 1.0f, 1.0f);
                break;
            }
            // turn bg music off and on
            case KeyEvent.VK_7: bgMusic.togglePause(); break;

            // start walking animation
            case KeyEvent.VK_8:{
                getPlayerSkeleton().stopAnimation();
                getPlayerSkeleton().playAnimation("WALK",.04f, AnimatedShape.EndType.LOOP,0); 
                if(isClientConnected){
                    protClient.sendAnimateMessage();
                } 
                break;
            }
            // stop walking animation
            case KeyEvent.VK_9:{
                pAS.stopAnimation();
                if(isClientConnected){
                    protClient.sendStopAnimateMessage();
                }
                break;
            } 
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
                    if(obj1 == pPhysicsObj && (obj2 != gm.getGhostPhysicsObject() && obj2 != groundPlanePhysicsObj)){
                        // this means that a laser collided with the avatar or the npcs did, therefore reduce health
                        pAvObj.setHealth((float)(pAvObj.getHealth()-5f));
                        if(pAvObj.getHealth() <= 0){
                            deathSound.setLocation(pAvObj.getWorldLocation());
                            deathSound.play(5,true);
                            if(isClientConnected){
                                protClient.sendDeadMessage();
                                protClient.sendLeaveMessage();
                            }
                            isSinglePlayer = true;
                            isClientConnected = false;
                            isDead = true;
                        }
                    }
                    break;
                }
            }
        }
    }
}