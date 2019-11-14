package PowerSystemDrawingGUI;

import java.io.*;
import java.util.*;
import com.mxgraph.util.*;

public class HorizontalLink implements Serializable
{
    protected static final long serialVersionUID = 7596037800466105728L;
    protected int count;
    protected String comments;
    
    public HorizontalLink() {
    }
    
    public HorizontalLink(final int count) {
        this.setCount(count);
    }
    
    public HorizontalLink(final int count, final String comments) {
        this.setCount(count);
        this.setComments(comments);
    }
    
    public int getCount() {
        return this.count;
    }
    
    public void setCount(final int count) {
        this.count = count;
    }
    
    public void setComments(final String comments) {
        this.comments = comments;
    }
    
    public String getComments() {
        return this.comments;
    }
    
    public static Map<String, Object> EdgeStyle() {
        final Map<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
        style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_SPACING);
//        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
//		style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
        style.put(mxConstants.STYLE_STROKECOLOR, "#191919");
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        style.put(mxConstants.STYLE_FONTSIZE, 20); //15
//        style.put(mxConstants.STYLE_WHITE_SPACE, "nowrap"); //15
        return style;
    }
    
    @Override
    public String toString() {
        return Integer.toString(this.count);
    }
}
