package ionescu.dan.rccameracontroller.communication;

public class MotorCommandFormatter {
    private int minimumScale = 0;
    private int maximumXScale = 100;
    private int maximumYScale = 50;
    private float maxX, maxY;

    public String formatDirection(float x, float y, float maxX, float maxY) {
        this.maxX = maxX;
        this.maxY = maxY;

        return String.format("M:%s:%s", this.getScaledX(x), this.getScaledY(y));
    }

    private int getScaledX(float raw) {
        return Math.round(this.getScaledCoordonate(raw, 0, this.maxX, this.maximumXScale));
    }

    private int getScaledY(float raw) {
        if (raw > this.maxY / 2) {
            return -1 * Math.round(this.getScaledCoordonate(raw, this.maxY / 2, this.maxY, this.maximumYScale));
        }

        return Math.round(this.getScaledCoordonate(raw, this.maxY / 2, 0, this.maximumYScale));
    }

    private float getScaledCoordonate(float raw, float inputMin, float inputMax, int maxScale) {
        return (raw - inputMin) * (maxScale - this.minimumScale) / (inputMax - inputMin) + this.minimumScale;
    }
}