package RobotGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;
import tage.shapes.AnimatedShape;

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
        // multiplying by -1 because -1 is forward on stick and +1 is backwards
        float evtValue = evt.getValue()*-1;
        
        if(evt.getComponent().toString().equalsIgnoreCase("W")){
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time)));
            // accounting for terrain or stairs
            if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                y = myGame.getStairs1Height(newLocVec.x,newLocVec.z);
            }else{
                y = oldLocVec.y;
            }
            
            newLocVec.set(newLocVec.x, y  ,newLocVec.z);

            myGame.getAvatar().setLocalLocation(newLocVec);
            if(!(myGame.isAiming())){
                myGame.getAvatar().globalYaw(angleSigned*3);
            }
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            myGame.getPlayerSkeleton().playAnimation("Walk", .04f, AnimatedShape.EndType.LOOP, 0);

        }else if(evt.getComponent().toString().equalsIgnoreCase("S")){
            // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
            forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
            angleSigned = (forwardVec.angleSigned((myGame.getAvatar().getLocalForwardVector()).mul(-1), new Vector3f(0,1,0))*-time);
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add(forwardVec.mul(-10*time)));
            
            // accounting for terrain or stairs
            if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                y = myGame.getStairs1Height(newLocVec.x,newLocVec.z);
            }else{
                y = oldLocVec.y;
            }
            
            newLocVec.set(newLocVec.x, y ,newLocVec.z);
            
            myGame.getAvatar().setLocalLocation(newLocVec);
            if(!(myGame.isAiming())){
                myGame.getAvatar().globalYaw(angleSigned*3);
            }
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            
        // deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            if(evtValue > .95f){
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add(forwardVec.mul(10*myGame.getSpeed()*time*evtValue)));
                
                // accounting for terrain or stairs
                if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                    y = myGame.getStairs1Height(newLocVec.x,newLocVec.z);
                }else{
                    y = oldLocVec.y;
                }
            
                newLocVec.set(newLocVec.x, y ,newLocVec.z);
                myGame.getAvatar().setLocalLocation(newLocVec);
                if(!(myGame.isAiming())){
                    myGame.getAvatar().globalYaw(angleSigned*3);
                }
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }else{
                // based on camera heading, cross product between world up vector and cameraU gives correct forward vecotor normalize to make it constant length 1
                forwardVec = ((new Vector3f(0,1,0)).cross(myGame.getCameraU())).normalize();
                if(evtValue > .15f){
                    angleSigned = (forwardVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
                }else if (evtValue < -.15f){
                    angleSigned = (forwardVec.angleSigned((myGame.getAvatar().getLocalForwardVector()).mul(-1), new Vector3f(0,1,0)))*-time;
                }else{
                    angleSigned = 0;
                }
                oldLocVec = myGame.getAvatar().getWorldLocation();
                newLocVec = (oldLocVec.add(forwardVec.mul(10*time*evtValue)));
                // accounting for terrain or stairs
                if(myGame.getStairs1Height(newLocVec.x,newLocVec.z ) > 0.1f){
                    y = myGame.getStairs1Height(newLocVec.x,newLocVec.z);
                }else{
                    y = oldLocVec.y;
                }
                
                newLocVec.set(newLocVec.x, y , newLocVec.z);

                myGame.getAvatar().setLocalLocation(newLocVec);
                if(!(myGame.isAiming())){
                    myGame.getAvatar().globalYaw(angleSigned*3);
                }
                p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
            }
        }
    }
}
