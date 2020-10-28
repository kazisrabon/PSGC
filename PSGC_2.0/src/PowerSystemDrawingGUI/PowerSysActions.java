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

import java.awt.event.*;
import java.util.ArrayList;

import com.mxgraph.view.*;
import com.mxgraph.swing.*;
import javax.swing.*;
import com.mxgraph.examples.swing.editor.*;
import com.mxgraph.util.*;
import com.mxgraph.model.*;

public class PowerSysActions
{
    static final Action addNewGeneratorAction;
    static final Action addNewLoadAction;
    static final Action addNewJunctionAction;
    static final Action addNewGeneratorVLAction;
    static final Action addNewLoadVLAction;
    static final Action addNewPowerAction;
    static final Action addNewCapacitanceAction;

    static {
        addNewGeneratorAction = new AddNewElement("Generator");
        addNewLoadAction = new AddNewElement("Load");
        addNewJunctionAction = new AddNewElement("Junction");
        addNewGeneratorVLAction = new AddNewElement("Generator-VL");
        addNewLoadVLAction = new AddNewElement("Load-VL");
        addNewPowerAction = new AddNewElement("Power");
        addNewCapacitanceAction = new AddNewElement("Capacitance");
    }

    public static Action getaddNewGeneratorAction() {
        return PowerSysActions.addNewGeneratorAction;
    }

    public static Action getaddNewLoadAction() {
        return PowerSysActions.addNewLoadAction;
    }

    public static Action getaddNewJunctionAction() {
        return PowerSysActions.addNewJunctionAction;
    }

    public static Action getaddNewGeneratorVLAction() {
        return PowerSysActions.addNewGeneratorVLAction;
    }

    public static Action getaddNewLoadVLAction() {
        return PowerSysActions.addNewLoadVLAction;
    }

    public static final mxGraph getGraph(final ActionEvent e) {
        final Object source = e.getSource();
        if (source instanceof mxGraphComponent) {
            return ((mxGraphComponent)source).getGraph();
        }
        return null;
    }

    public static Action getaddPowerAction() {
        return PowerSysActions.addNewPowerAction;
    }

    public static Action getaddCapacitanceAction() {
        return PowerSysActions.addNewCapacitanceAction;
    }

    /**
     *
     * @author juandiego
     * This function is called when element is added from
     * the editor pop-up menu.
     */
    public static class AddNewElement extends AbstractAction
    {
        private static final long serialVersionUID = 6501585024845668187L;

        public AddNewElement(final String name) {
            super(name);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final mxGraph graph = PowerSysActions.getGraph(e);
            if (graph != null) {
                final double scale = graph.getView().getScale();
                final mxPoint backgroundoffset = graph.getView().getTranslate();
                final String name = this.getValue("Name").toString();
                graph.getModel().beginUpdate();
                try
                {
                    if (name.equalsIgnoreCase("Generator")) {
                        ++DrawNetworkClean.itemCounter;
                        // System.out.println("adding generator from action"); //JDCF
                        final Generator newGen = new Generator(DrawNetworkClean.itemCounter, "A new Generator");
                        final mxCell v1 = (mxCell)graph.insertVertex(graph.getDefaultParent(), null, newGen, BasicGraphEditor.mousePos.getX() / scale - backgroundoffset.getX(), BasicGraphEditor.mousePos.getY() / scale - backgroundoffset.getY(), 35.0, 35.0, "Generator");
                    }
                    else if (name.equalsIgnoreCase("Load")) {
                        ++DrawNetworkClean.itemCounter;
                        final Load newLoad = new Load(DrawNetworkClean.itemCounter, "A new Load");
                        final mxCell v1 = (mxCell)graph.insertVertex(graph.getDefaultParent(), null, newLoad, BasicGraphEditor.mousePos.getX() / scale - backgroundoffset.getX(), BasicGraphEditor.mousePos.getY() / scale - backgroundoffset.getY(), 25.0, 25.0, "Load");
                    }
                    else if (name.equalsIgnoreCase("Junction")) {
                        final Junction newJunc = new Junction();
                        graph.insertVertex(graph.getDefaultParent(), null, newJunc, BasicGraphEditor.mousePos.getX() / scale - backgroundoffset.getX(), BasicGraphEditor.mousePos.getY() / scale - backgroundoffset.getY(), 15.0, 15.0, "Junction");
                    }
                    else if (name.equalsIgnoreCase("Generator-VL")) {
                        //++DrawNetworkClean.itemCounter;
                        /** JDCF
                         * itemCounter increased in insertEdge()
                         */
                        final Object myEdge = graph.insertEdge(graph.getDefaultParent(), null, null, null, null, "VerticalLink");
                        final Generator newGen2 = new Generator(DrawNetworkClean.itemCounter, "A new Generator");
                        ((mxCell)myEdge).setValue(newGen2, DrawNetworkClean.itemCounter);
                        final mxGeometry geo = graph.getCellGeometry(myEdge);
                        geo.setTerminalPoint(new mxPoint(DrawNetworkClean.selJunction.getGeometry().getPoint()), false);
                        geo.setTerminalPoint(new mxPoint(DrawNetworkClean.selJunction.getGeometry().getPoint().getX(), DrawNetworkClean.selJunction.getGeometry().getPoint().getY() - 50.0), true);
                        graph.connectCell(myEdge, DrawNetworkClean.selJunction, false);
                    }
                    else if (name.equalsIgnoreCase("Load-VL")) {
                        //++DrawNetworkClean.itemCounter;
                        /** JDCF
                         * itemCounter increased in insertEdge()
                         */
                        final Object tempEdge = graph.insertEdge(graph.getDefaultParent(), null, "Load-VL", null, null, "VerticalLink");
                        final Load newLd = new Load(DrawNetworkClean.itemCounter, "A new Load");
                        ((mxCell)tempEdge).setValue(newLd, DrawNetworkClean.itemCounter);
                        final mxGeometry geo = graph.getCellGeometry(tempEdge);
                        geo.setTerminalPoint(new mxPoint(DrawNetworkClean.selJunction.getGeometry().getPoint()), true);
                        geo.setTerminalPoint(new mxPoint(DrawNetworkClean.selJunction.getGeometry().getPoint().getX(), DrawNetworkClean.selJunction.getGeometry().getPoint().getY() + 60.0), false);
                        graph.connectCell(tempEdge, DrawNetworkClean.selJunction, true);
                    }
//                    add action for weight
                    else if (name.equalsIgnoreCase("Power")){
                        ArrayList<Integer> powers = DrawNetworkClean.getPowers();
                        DrawNetworkClean.AddPowerInputs(graph, powers);
                    }

                    else if (name.equalsIgnoreCase("Capacitance")){
                        DrawNetworkClean.AddPowerCapacitance(graph, DrawNetworkClean.getCapacitances());
                    }
                }
                finally
                {
                    graph.getModel().endUpdate();
                }
            }
        }
    }
}