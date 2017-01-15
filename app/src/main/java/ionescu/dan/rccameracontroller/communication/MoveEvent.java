package ionescu.dan.rccameracontroller.communication;

public class MoveEvent {
    private float x;
    private float y;
    private float maxX;
    private float maxY;

    public MoveEvent(float x, float y, float maxX, float maxY) {
        this.x = x;
        this.y = y;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }
}