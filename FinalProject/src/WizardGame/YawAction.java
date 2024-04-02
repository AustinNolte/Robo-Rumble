package WizardGame;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class YawAction extends AbstractInputAction{

    private MyGame myGame;
    public YawAction(MyGame myGame){
        this.myGame = myGame;
    }
    @Override
    public void performAction(float time, Event evt) {
        //multiplying by time to make the dolphin move in terms of frame timing.
        if(evt.getComponent().toString().equalsIgnoreCase("A")){
            myGame.getAvatar().globalYaw((float)Math.toRadians(90)*time);
            

        }else if(evt.getComponent().toString().equalsIgnoreCase("D")){
            myGame.getAvatar().globalYaw((float)Math.toRadians(-90)*time);

        }else if(evt.getValue() > .15f || evt.getValue() < -.15f){
            //adding deadzoning so the character doesn't always rotate on controllers with stick drift.
            //multiplying by value of stick so you turn harder the more you push the stick
            //multiplying by -1 to make pushing stick left -> turn left, pushing stick right -> turn right
            
            myGame.getAvatar().globalYaw((float)Math.toRadians(90)*time*evt.getValue()*-1);
        } 
    }
}
