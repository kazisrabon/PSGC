package PowerSystemDrawingGUI;

import java.io.*;
import java.util.*;
import com.mxgraph.util.*;

public class Generator implements Serializable
{
    protected static final long serialVersionUID = 7373796271204172523L;
    protected int count;
    protected int id_number;
    protected String comments;
    protected int capacity; //capacity of providing power into the system
    
    public Generator() {
    }
    
    public Generator(final int count) {
        this.setCount(count);
        this.setIDnumber(1);
        this.setComments("");
    }
    
    public Generator(final int count, final String comments) {
        this.setCount(count);
        this.setIDnumber(1);
        this.setComments(comments);
        this.setCapacity(1);
    }
    
    public int getCount() {
        return this.count;
    }
    
    public void setCount(final int count) {
        this.count = count;
    }
    
    public int getIDnumber() {
        return this.id_number;
    }
    
    public void setIDnumber(final int number) {
        this.id_number = number;
    }
    
    public void setComments(final String comments) {
        this.comments = comments;
    }
    
    public String getComments() {
        return this.comments;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public static Map<String, Object> VertexStyle() {
        final Map<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, "ellipse");
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        style.put(mxConstants.STYLE_FONTSIZE, mxConstants.DEFAULT_FONTSIZE+7);
        style.put(mxConstants.STYLE_STROKECOLOR, "#236c34");
        style.put(mxConstants.STYLE_FILLCOLOR, "#42c260");
        style.put(mxConstants.STYLE_PERIMETER, "ellipsePerimeter");
        return style;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.count);
    }
}
