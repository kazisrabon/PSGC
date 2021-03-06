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

import com.mxgraph.swing.*;
import com.mxgraph.view.*;
import java.awt.*;
import com.mxgraph.model.*;

public class PowerSysGraphComponent extends mxGraphComponent
{
    private static final long serialVersionUID = -6833603133512882012L;
    
    public PowerSysGraphComponent(final mxGraph graph) {
        super(graph);
        this.setPageVisible(true);
        this.setToolTips(true);
        this.getConnectionHandler().setCreateTarget(true);
        this.getViewport().setOpaque(false);
        this.setBackground(Color.WHITE);
        this.setConnectable(true);
        this.setGridVisible(true);
        this.setDragEnabled(false);
    }
    
    @Override
    public Object[] importCells(final Object[] cells, final double dx, final double dy, Object target, final Point location) {
        if (target == null && cells.length == 1 && location != null) 
        {
            target = this.getCellAt(location.x, location.y);
            if (target instanceof mxICell && cells[0] instanceof mxICell) {
                final mxICell targetCell = (mxICell)target;
                final mxICell dropCell = (mxICell)cells[0];
                if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
                    final mxIGraphModel model = this.graph.getModel();
                    model.setStyle(target, model.getStyle(cells[0]));
                    this.graph.setSelectionCell(target);
                    return null;
                }
            }
        }
        return super.importCells(cells, dx, dy, target, location);
    }
}
