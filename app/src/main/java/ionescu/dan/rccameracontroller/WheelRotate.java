package ionescu.dan.rccameracontroller;

import android.graphics.Matrix;
import android.widget.ImageView;

import ionescu.dan.rccameracontroller.communication.DirectionsInterpretter;
import ionescu.dan.rccameracontroller.communication.MoveEvent;

public class WheelRotate {
    private DirectionsInterpretter directionsInterpretter;

    public WheelRotate(DirectionsInterpretter directionsInterpretter) {
        this.directionsInterpretter = directionsInterpretter;
    }

    public ImageView getRotated(ImageView steeringWheel, MoveEvent lastMoveEvent) {
        Matrix matrix = new Matrix();
        steeringWheel.setScaleType(ImageView.ScaleType.MATRIX);
        int rotateWheel = this.directionsInterpretter.getScaledX(lastMoveEvent) * 2;
        matrix.postRotate(rotateWheel, steeringWheel.getDrawable().getBounds().width() / 2,
                steeringWheel.getDrawable().getBounds().height() / 2);
        matrix.postScale(0.415f, 0.415f);
        steeringWheel.setImageMatrix(matrix);

        return steeringWheel;
    }
}
