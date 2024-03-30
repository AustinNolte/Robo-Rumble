package WizardGame;

import tage.*;

public class MyGame extends VariableFrameRateGame{

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

    }

    @Override
    public void loadShapes() {

    }

    @Override
    public void loadTextures() {

    }

    @Override
    public void buildObjects() {

    }

    @Override
    public void initializeLights() {

    }

    @Override
    public void initializeGame() {

    }

    @Override
    public void update() {

    }
    
}