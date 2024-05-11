package RobotGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class StrafingMovement extends AbstractInputAction{
    
    private MyGame myGame;
    private ProtocolClient p;
    float[] rotValues = new float[9];

    public StrafingMovement(MyGame myGame, ProtocolClient p){
        this.myGame = myGame;
        this.p = p;
    }


    @Override
    public void performAction(float time, Event evt){
        
        Vector3f oldLocVec, rightVec, newLocVec;
        float y;
        float angleSigned;
        
        float evtValue = evt.getValue();
        
        if(evt.getComponent().toString().equalsIgnoreCase("D")){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add((rightVec.mul(time*10))));
            angleSigned = (rightVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
   
            // accounting for terrain or stairs
            if(myGame.getTerrainHeight(newLocVec.x,newLocVec.z ) > 0.1f){
            y = myGame.getTerrainHeight(newLocVec.x,newLocVec.z);
            }else{
                y = oldLocVec.y;
            }

            // checking for world boundries
            if(newLocVec.x > 273){
                newLocVec.x = 273;
            }
            if(newLocVec.x < -273){
                newLocVec.x = -273;
            }
            if(newLocVec.z > 273){
                newLocVec.z = 273;
            }
            if(newLocVec.z < -273){
                newLocVec.z = -273;
            }

            newLocVec.set(newLocVec.x, y ,newLocVec.z);
            myGame.getAvatar().setLocalLocation(newLocVec);   
            myGame.getAvatar().globalYaw(angleSigned*3);
            sendRotateMessage();
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());

        }else if(evt.getComponent().toString().equalsIgnoreCase("A")){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add((rightVec.mul(time*-10))));
            angleSigned = (rightVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;
            
            // accounting for terrain or stairs
            if(myGame.getTerrainHeight(newLocVec.x,newLocVec.z ) > 0.1f){
                y = myGame.getTerrainHeight(newLocVec.x,newLocVec.z);
            }else{
                y = oldLocVec.y;
            }
            
            // checking for world boundries
            if(newLocVec.x > 273){
                newLocVec.x = 273;
            }
            if(newLocVec.x < -273){
                newLocVec.x = -273;
            }
            if(newLocVec.z > 273){
                newLocVec.z = 273;
            }
            if(newLocVec.z < -273){
                newLocVec.z = -273;
            }

            newLocVec.set(newLocVec.x, y ,newLocVec.z);

            myGame.getAvatar().setLocalLocation(newLocVec);
            myGame.getAvatar().globalYaw(angleSigned*3);
            sendRotateMessage();

            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
        
        //deadzoning 
        }else if(evtValue > .15f || evtValue < -.15f){
            // based on camera heading, cross product between cameraN and world up gives correct right vecotor normalize to make it constant length 1
            rightVec = (myGame.getCameraN().cross(new Vector3f(0,1,0))).normalize();
            oldLocVec = myGame.getAvatar().getWorldLocation();
            newLocVec = (oldLocVec.add((rightVec.mul(time*evtValue*10f))));
            angleSigned = (rightVec.angleSigned(myGame.getAvatar().getLocalForwardVector(), new Vector3f(0,1,0)))*-time;

            // accounting for terrain or stairs
            if(myGame.getTerrainHeight(newLocVec.x,newLocVec.z ) > 0.1f){
            y = myGame.getTerrainHeight(newLocVec.x,newLocVec.z);
            }else{
                y = oldLocVec.y;
            }

            // checking for world boundries
            if(newLocVec.x > 273){
                newLocVec.x = 273;
            }
            if(newLocVec.x < -273){
                newLocVec.x = -273;
            }
            if(newLocVec.z > 273){
                newLocVec.z = 273;
            }
            if(newLocVec.z < -273){
                newLocVec.z = -273;
            }

            newLocVec.set(newLocVec.x, y ,newLocVec.z);

                           
            myGame.getAvatar().setLocalLocation(newLocVec);
            myGame.getAvatar().globalYaw(angleSigned*3);
            sendRotateMessage();
            p.sendMoveMessage(myGame.getAvatar().getWorldLocation());
        }
    }
    public void sendRotateMessage(){
        rotValues[0] = myGame.getAvatar().getLocalRotation().m00();
        rotValues[1] = myGame.getAvatar().getLocalRotation().m10();
        rotValues[2] = myGame.getAvatar().getLocalRotation().m20();
        rotValues[3] = myGame.getAvatar().getLocalRotation().m01();
        rotValues[4] = myGame.getAvatar().getLocalRotation().m11();
        rotValues[5] = myGame.getAvatar().getLocalRotation().m21();
        rotValues[6] = myGame.getAvatar().getLocalRotation().m02();
        rotValues[7] = myGame.getAvatar().getLocalRotation().m12();
        rotValues[8] = myGame.getAvatar().getLocalRotation().m22();
        p.sendRotateMessage(rotValues);
    }
}
