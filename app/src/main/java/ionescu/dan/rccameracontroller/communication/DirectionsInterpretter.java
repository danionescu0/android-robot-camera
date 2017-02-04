package ionescu.dan.rccameracontroller.communication;

public class DirectionsInterpretter {
    private int minimumScale = 0;
    private int maximumScale = 50;

    /**
     * @Todo Refactor this duplicate code
     */
    public int getScaledX(MoveEvent moveEvent) {
        if (moveEvent.getX() > moveEvent.getMaxX() / 2) {
            return Math.round(this.getScaledCoordonate(moveEvent.getX(), moveEvent.getMaxX() / 2, moveEvent.getMaxX()));
        }

        return -1 * Math.round(this.getScaledCoordonate(moveEvent.getX(), moveEvent.getMaxX() / 2, 0));
    }

    public int getScaledY(MoveEvent moveEvent) {
        if (moveEvent.getY() > moveEvent.getMaxY() / 2) {
            return -1 * Math.round(this.getScaledCoordonate(moveEvent.getY(), moveEvent.getMaxY() / 2, moveEvent.getMaxY()));
        }

        return Math.round(this.getScaledCoordonate(moveEvent.getY(), moveEvent.getMaxY() / 2, 0));
    }

    private float getScaledCoordonate(float raw, float inputMin, float inputMax) {
        return (raw - inputMin) * (this.maximumScale - this.minimumScale) / (inputMax - inputMin) + this.minimumScale;
    }
}
