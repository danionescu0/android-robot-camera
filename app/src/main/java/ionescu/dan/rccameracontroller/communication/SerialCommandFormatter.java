package ionescu.dan.rccameracontroller.communication;

public class SerialCommandFormatter {
    public String formatDirection(int x, int y) {
        return String.format("M:%s:%s", x, y);
    }

    public String formatLights(boolean state) {
        return "L:%s" + (state ? "1" : "0");
    }
}