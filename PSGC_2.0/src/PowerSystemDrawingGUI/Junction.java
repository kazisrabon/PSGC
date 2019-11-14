package PowerSystemDrawingGUI;

import java.io.*;
import java.util.*;
import com.mxgraph.util.*;

public class Junction implements Serializable
{
    protected static final long serialVersionUID = -7040320563905849408L;
    protected int id_number;
    
    public Junction() {
        this.setIDnumber(0);
    }
    
    public int getIDnumber() {
        return this.id_number;
    }
    
    public void setIDnumber(final int idnumber) {
        this.id_number = idnumber;
    }
    
    public static Map<String, Object> VertexStyle() {
        final Map<String, Object> temp = new Hashtable<String, Object>();
        temp.put(mxConstants.STYLE_SHAPE, "ellipse");
        temp.put(mxConstants.STYLE_OPACITY, 50); // 0-100
        temp.put(mxConstants.STYLE_FONTCOLOR, "#000000"); //774400
        temp.put(mxConstants.STYLE_STROKECOLOR, "#1f1f1f"); //6482B9
        temp.put(mxConstants.STYLE_FILLCOLOR, "#1f1f1f"); //E0E0E0
        temp.put(mxConstants.STYLE_PERIMETER, "ellipsePerimeter");
        return temp;
    }
    
    @Override
    public String toString() {
        return "";
    }
}
