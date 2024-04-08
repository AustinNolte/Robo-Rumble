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
        
        float evtValue = evt.getValue();
        
        if(evt.getComponent().toString().equalsIgnoreCase("D")){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(rightVec.mul(10*time)));
            // accounting for terrain
            newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("A")){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(rightVec.mul(-10*time)));
            // accounting for terrain
            newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
        
        //deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(rightVec.mul(10*evtValue*time)));
            // accounting for terrain
            newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());;
        }
    }
}
