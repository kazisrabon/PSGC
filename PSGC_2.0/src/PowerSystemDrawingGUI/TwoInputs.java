package PowerSystemDrawingGUI;

import java.util.ArrayList;

public class TwoInputs {
    int loadNum;
    ArrayList<Integer> connections;

    public TwoInputs(int loadNum, ArrayList<Integer> connections) {
        this.loadNum = loadNum;
        this.connections = connections;
    }

    public int getLoadNum() {
        return loadNum;
    }

    public void setLoadNum(int loadNum) {
        this.loadNum = loadNum;
    }

    public ArrayList<Integer> getConnections() {
        return connections;
    }

    public void setConnections(ArrayList<Integer> connections) {
        this.connections = connections;
    }
}
