package ionescu.dan.rccameracontroller.communication;

import android.graphics.Point;
import android.view.Display;

public class MotorCommandFormatter {
    private Display display;
    private int minimumScale = 0;
    private int maximumScale = 100;

    public String formatDirection(float x, float y, Display display) {
        this.display = display;

        return  String.format("M:%s:%s", this.getScaledX(x), this.getScaledY(y));
    }

    private int getScaledX(float raw) {
        Point size = new Point();
        this.display.getSize(size);

        return Math.round(this.getScaledCoordonate(raw, 0, size.x));
    }

    private int getScaledY(float raw) {
        Point size = new Point();
        this.display.getSize(size);

        return Math.round(this.getScaledCoordonate(raw, size.y, 0));
    }

    private float getScaledCoordonate(float raw, float inputMin, float inputMax) {
        return (raw - inputMin) * (this.maximumScale - this.minimumScale) / (inputMax - inputMin) + this.minimumScale;
    }
}