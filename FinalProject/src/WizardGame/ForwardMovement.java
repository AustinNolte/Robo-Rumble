package WizardGame;

import org.joml.Matrix4f;
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
        float angleSigned;  
        float y;
        Matrix4f rotation = new Matrix4f().identity();

        // multiplying by -1 because -1 is forward on stick and +1 is backwards
        float evtValue = evt.getValue()*-1;
        
        if(evt.getComponent().toString().equalsIgnoreCase("W")){
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add((forwardVec.mul(time*myGame.getSpeed()*.25f)).normalize()));
            // accounting for terrain or stairs
            if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                y = myGame.getStairs1Height(newLocVec.x,newLocVec.z) + myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
            }else{
                y = myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
            }
            
            newLocVec.set(newLocVec.x, y ,newLocVec.z);

            myGame.getAvatar().setLocalLocation(newLocVec);
            myGame.getAvatar().globalYaw(angleSigned*3);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("S")){
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*time;
            newLocVec = (oldLocVec.add((forwardVec.mul(-time*.25f)).normalize()));
            // accounting for terrain or stairs
            if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                y = myGame.getStairs1Height(newLocVec.x,newLocVec.z) + myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
            }else{
                y = myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
            }
            
            newLocVec.set(newLocVec.x, y ,newLocVec.z);

            myGame.getAvatar().setLocalLocation(newLocVec);
            myGame.getAvatar().globalYaw(angleSigned);
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            
        // deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            if(evtValue > .95f){
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add((forwardVec.mul(myGame.getSpeed()*time*evtValue*.25f).normalize())));
                
                // accounting for terrain or stairs
                if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                    y = myGame.getStairs1Height(newLocVec.x,newLocVec.z) + myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
                }else{
                    y = myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
                }
            
                newLocVec.set(newLocVec.x, y ,newLocVec.z);
                myGame.getAvatar().setLocalLocation(newLocVec);
                myGame.getAvatar().globalYaw(angleSigned*3);
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }else{
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add((forwardVec.mul(time*evtValue*.25f)).normalize()));
                // accounting for terrain or stairs
                if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                    y = myGame.getStairs1Height(newLocVec.x,newLocVec.z) + myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
                }else{
                    y = myGame.getTerrainHeight(newLocVec.x, newLocVec.z);
                }
                
                newLocVec.set(newLocVec.x, y ,newLocVec.z);

                myGame.getAvatar().setLocalLocation(newLocVec);
                if(evtValue < 0){
                    myGame.getAvatar().globalYaw(angleSigned*3*-1);

                }else{
                    myGame.getAvatar().globalYaw(angleSigned*3);
                }
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }
        }
    }
}
