package WizardGame;


import net.java.games.input.*;
import tage.input.action.*;

public class GameSettingAction extends AbstractInputAction {
    private MyGame myGame;

    public GameSettingAction(MyGame myGame) {
        this.myGame = myGame;
    }

    @Override
    public void performAction(float time, Event evt) {
        myGame.setIsSprinting(!myGame.getIsSprinting());
    }
}