package Server;

import tage.ai.behaviortrees.BTAction;
import tage.ai.behaviortrees.BTStatus;

public class WaitTime extends BTAction{
    private float timeStart;
    public WaitTime() {
        this.timeStart = 0;
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        timeStart += elapsedTime;
        if(timeStart >= 2000){
            return BTStatus.BH_SUCCESS;
        }else{
            return BTStatus.BH_RUNNING;
        }
    }
    
}
