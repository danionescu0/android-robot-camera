package ionescu.dan.rccameracontroller.communication;

import android.view.MotionEvent;
import android.view.View;

public class MoveEventFactory {
    public static MoveEvent createFromMotion(MotionEvent motionEvent,View view) {
        return new MoveEvent(motionEvent.getX(), motionEvent.getY(), view.getHeight(), view.getWidth());
    }
}