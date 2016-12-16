package ionescu.dan.rccameracontroller.communication;

public class MotorCommandFormatter {
    public String get(float x, float y) {
        String xCoordonate = Float.toString(x);
        String yCoordonate = Float.toString(y);
        String command = String.format("M:%s:%s", xCoordonate, yCoordonate);

        return command;
    }
}
