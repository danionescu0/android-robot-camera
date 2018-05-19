package ionescu.dan.rccameracontroller.listeners;


import android.widget.CompoundButton;

public class LightSwitchListener implements CompoundButton.OnCheckedChangeListener {
    private boolean lastSwitchState;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean lastSwitchState) {
        this.lastSwitchState = lastSwitchState;
    }

    public boolean getLastSwitchState() {
        return lastSwitchState;
    }
}