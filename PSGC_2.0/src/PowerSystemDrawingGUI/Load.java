/*
 * Copyright (c) 2011 The Florida State University Research Foundation, Inc. All right reserved.
 *
 * Developer:
 * Kazi Solaiman Ahmed
 * Juan Diego Colmenares F.
 * Dominik Neumayr
 * Svetlana V. Poroseva
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package PowerSystemDrawingGUI;

import java.io.*;
import java.util.*;
import com.mxgraph.util.*;

public class Load implements Serializable
{
    protected static final long serialVersionUID = 8638087940933987209L;
    protected int count;
    protected int id_number;
    protected String comments;
    
    public Load() {
    }
    
    public Load(final int count) {
        this.setCount(count);
        this.setIDnumber(-1);
        this.setComments("");
    }
    
    public Load(final int count, final String comments) {
        this.setCount(count);
        this.setIDnumber(-1);
        this.setComments(comments);
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
    
    public void setIDnumber(final int idnumber) {
        this.id_number = idnumber;
    }
    
    public void setComments(final String comments) {
        this.comments = comments;
    }
    
    public String getComments() {
        return this.comments;
    }
    
    public static Map<String, Object> VertexStyle() {
        final Map<String, Object> temp = new Hashtable<String, Object>();
        temp.put(mxConstants.STYLE_SHAPE, "ellipse");
        temp.put(mxConstants.STYLE_OPACITY, 50);
        temp.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        temp.put(mxConstants.STYLE_FONTSIZE, mxConstants.DEFAULT_FONTSIZE+5);
        temp.put(mxConstants.STYLE_STROKECOLOR, "#6482B9");
        temp.put(mxConstants.STYLE_FILLCOLOR, "#99CCFF");
        temp.put(mxConstants.STYLE_PERIMETER, "ellipsePerimeter");
        return temp;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.count);
    }
}
