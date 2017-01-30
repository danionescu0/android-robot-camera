package ionescu.dan.rccameracontroller.communication;

public class MotorCommandFormatter {
    private int minimumScale = 0;
    private int maximumScale = 50;
    private float maxX, maxY;

    public String formatDirection(float x, float y, float maxX, float maxY) {
        this.maxX = maxX;
        this.maxY = maxY;

        return String.format("M:%s:%s", this.getScaledX(x), this.getScaledY(y));
    }

    /**
     * @Todo Refactor this duplicate code
     */
    private int getScaledX(float raw) {
        if (raw > this.maxX / 2) {
            return -1 * Math.round(this.getScaledCoordonate(raw, this.maxX / 2, this.maxX));
        }

        return Math.round(this.getScaledCoordonate(raw, this.maxX / 2, 0));
    }

    /**
     * @Todo Refactor this duplicate code
     */
    private int getScaledY(float raw) {
        if (raw > this.maxY / 2) {
            return -1 * Math.round(this.getScaledCoordonate(raw, this.maxY / 2, this.maxY));
        }

        return Math.round(this.getScaledCoordonate(raw, this.maxY / 2, 0));
    }

    private float getScaledCoordonate(float raw, float inputMin, float inputMax) {
        return (raw - inputMin) * (this.maximumScale - this.minimumScale) / (inputMax - inputMin) + this.minimumScale;
    }
}