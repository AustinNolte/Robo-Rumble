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
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time)));
            // accounting for terrain
            newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("S")){
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(forwardVec.mul(-10*time)));
            // accounting for terrain
            newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            
        // deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            if(evtValue > .95f){
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time*evtValue)));
                // accounting for terrain
                newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
                myGame.getAvatar().setLocalLocation(newLocVec);
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }else{
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add(forwardVec.mul(10*time*evtValue)));
                // accounting for terrain
                newLocVec.set(newLocVec.x,(myGame.getTerrainHeight(newLocVec.x, newLocVec.z)),newLocVec.z);
                myGame.getAvatar().setLocalLocation(newLocVec);
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }
        }
    }
}
