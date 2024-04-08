package WizardGame;

import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class ForwardMovement extends AbstractInputAction{
    
    private MyGame myGame;
    private ProtocolClient p;

    public ForwardMovement(MyGame myGame, ProtocolClient p){
        this.myGame = myGame;
        this.p = p;
    }


    @Override
    public void performAction(float time, Event evt){
        Vector3f oldLocVec, forwardVec, newLocVec;


        // multiplying by -1 because -1 is forward on stick and +1 is backwards
        float evtValue = evt.getValue()*-1;
        
        if(evt.getComponent().toString().equalsIgnoreCase("W")){
            forwardVec = myGame.getAvatar().getLocalForwardVector();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time));
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("S")){
            forwardVec = myGame.getAvatar().getLocalForwardVector();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = oldLocVec.add(forwardVec.mul(-10*time));
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            
        // deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            if(evtValue > .95f){
                forwardVec = myGame.getAvatar().getLocalForwardVector();
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time*evtValue));
                myGame.getAvatar().setLocalLocation(newLocVec);
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }else{
                forwardVec = myGame.getAvatar().getLocalForwardVector();
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = oldLocVec.add(forwardVec.mul(10*time*evtValue));
                myGame.getAvatar().setLocalLocation(newLocVec);
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }
        }
    }
}
