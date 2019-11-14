package PowerSystemDrawingGUI;

import java.util.ArrayList;

public class ConnectedList {
    private ArrayList<Integer> loads;
    private ArrayList<Integer> links;

    public ConnectedList() {
        loads = new ArrayList<>();
        links = new ArrayList<>();
    }

    public ArrayList<Integer> getLoads() {
        return loads;
    }

    public void setLoads(ArrayList<Integer> loads) {
        this.loads = loads;
    }

    public ArrayList<Integer> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Integer> links) {
        this.links = links;
    }
}
