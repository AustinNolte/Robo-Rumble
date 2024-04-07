package WizardGame;

import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class StrafingMovement extends AbstractInputAction{
    
    private MyGame myGame;
    private ProtocolClient p;

    public StrafingMovement(MyGame myGame, ProtocolClient p){
        this.myGame = myGame;
        this.p = p;
    }


    @Override
    public void performAction(float time, Event evt){
        
        Vector3f oldLocVec, rightVec, newLocVec;
        
        // multiplying by -1 because -1 is forward on stick and +1 is backwards
        float evtValue = evt.getValue()*-1;
        
        if(evt.getComponent().toString().equalsIgnoreCase("D")){
            rightVec = myGame.getAvatar().getLocalRightVector();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = oldLocVec.add(rightVec.mul(10*time));
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("A")){
            rightVec = myGame.getAvatar().getLocalRightVector();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = oldLocVec.add(rightVec.mul(-10*time));
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
        
        //deadzoning 
        }else if(evtValue > .15f || evtValue < .15f){
            rightVec = myGame.getAvatar().getLocalRightVector();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = oldLocVec.add(rightVec.mul(10*evtValue*time));
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());;
        }
    }
}
