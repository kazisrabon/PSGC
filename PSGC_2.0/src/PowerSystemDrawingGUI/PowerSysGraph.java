package PowerSystemDrawingGUI;

import java.text.*;
// import PowerSystemDrawing.*; JDCF
import com.mxgraph.util.*;
import com.mxgraph.model.*;
import com.mxgraph.view.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class PowerSysGraph extends mxGraph
{
    public static final NumberFormat numberFormat;
    public static ArrayList<mxCell> allCells;
    public static mxGraphModel powerSysGraphModel;
    
    static {
        numberFormat = NumberFormat.getInstance();
    }
    
    public PowerSysGraph() {
        allCells = new ArrayList<>();
        this.setCellsEditable(false);
        this.setCellsResizable(false);
        this.setAllowDanglingEdges(true);
        this.setCellsCloneable(true);
        this.setKeepEdgesInForeground(true);
        this.setAllowLoops(false);
        final mxStylesheet stylesheet = this.getStylesheet();
        stylesheet.putCellStyle("Generator", Generator.VertexStyle());
        stylesheet.putCellStyle("Load", Load.VertexStyle());
        stylesheet.putCellStyle("Junction", Junction.VertexStyle());
        stylesheet.setDefaultEdgeStyle(HorizontalLink.EdgeStyle());
        stylesheet.putCellStyle("VerticalLink", this.EdgeStyle());
        this.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
            @Override
            public void invoke(final Object arg0, final mxEventObject arg1) {
                final Map<String, Object> temp = arg1.getProperties();
                for (final Map.Entry<String, Object> element : temp.entrySet()) {
                    if (element.getValue().getClass() == mxCell.class) {
                        System.out.println("Cells Added Event");
                        final mxCell addedCell = (mxCell) element.getValue();
                        System.out.println(addedCell.toString());
                    }
                }
                PowerSysGraph.this.AutoNumberCells();
                PowerSysGraph.this.refresh();
            }
        });
        this.addListener(mxEvent.CELL_CONNECTED, new mxIEventListener() {
            @Override
            public void invoke(final Object arg0, final mxEventObject arg1) {
                PowerSysGraph.this.AutoNumberCells();
                PowerSysGraph.this.refresh();
            }
        });
        this.addListener(mxEvent.SPLIT_EDGE, new mxIEventListener() {
            @Override
            public void invoke(final Object arg0, final mxEventObject arg1) {
                PowerSysGraph.this.AutoNumberCells();
                PowerSysGraph.this.refresh();
            }
        });
        this.addListener(mxEvent.CELLS_REMOVED, new mxIEventListener() {
            @Override
            public void invoke(final Object arg0, final mxEventObject arg1) {
                Object[] deletedCells = null;
                mxCell delCell = null;
                mxCell tempCell = null;
                int countDelCells = 0;
                final List<Map.Entry<String, Object>> mapEntries = new LinkedList<Map.Entry<String, Object>>(arg1.getProperties().entrySet());
                deletedCells = (Object[]) mapEntries.iterator().next().getValue();
                for (int ii = 0; ii < deletedCells.length; ++ii) {
                    if (deletedCells[ii] != null) {
                        delCell = (mxCell)deletedCells[ii];
                    }
                    for (int jj = ii + 1; jj < deletedCells.length; ++jj) {
                        if (delCell == deletedCells[jj]) {
                            deletedCells[jj] = null;
                        }
                    }
                }
                for (int ii = 0; ii < deletedCells.length; ++ii) {
                    if (deletedCells[ii] != null) {
                        tempCell = (mxCell)deletedCells[ii];
                        if (tempCell.getValue() instanceof Generator || tempCell.getValue() instanceof Load || tempCell.getValue() instanceof HorizontalLink) {
                            ++countDelCells;
                        }
                    }
                }
                PowerSysGraph.this.AutoNumberCells();
                PowerSysGraph.this.refresh();
            }
        });
    }
    
    public void AutoNumberCells() {
        int number = 0;
        PowerSysGraph.allCells = new ArrayList<mxCell>();
        final Object parent = this.getDefaultParent();
        GetAllCells(parent);
        final mxCell[] allCellsArray = PowerSysGraph.allCells.toArray(new mxCell[PowerSysGraph.allCells.size()]);
        //System.out.println("Autonumber cells"); //JDCF
        //System.out.println(allCellsArray.length);
        for (int ii = 0; ii < allCellsArray.length; ++ii) {
            if (allCellsArray[ii].getValue() instanceof Generator) {
                ++number;
                ((Generator)allCellsArray[ii].getValue()).setCount(number);
            }
        }
        for (int ii = 0; ii < allCellsArray.length; ++ii) {
            if (allCellsArray[ii].getValue() instanceof Load) {
                ++number;
                ((Load)allCellsArray[ii].getValue()).setCount(number);
            }
        }
        for (int ii = 0; ii < allCellsArray.length; ++ii) {
            if (allCellsArray[ii].getValue() instanceof HorizontalLink) {
                ++number;
                ((HorizontalLink)allCellsArray[ii].getValue()).setCount(number);
            }
        }
        DrawNetworkClean.itemCounter = number;
    }
    
    public static ArrayList<mxCell> GetAllCells(final Object parent) {
        int countChilds = 0;
        final mxGraphModel graphModel = new mxGraphModel(parent);
        countChilds = ((mxCell)parent).getChildCount();
        for (int ii = 0; ii < countChilds; ++ii) {
            final mxCell tempCell = (mxCell)graphModel.getChildAt(parent, ii);
            if (tempCell.getChildCount() > 0) {
                GetAllCells(tempCell);
            }
            else {
                PowerSysGraph.allCells.add(tempCell);
            }
        }
        return PowerSysGraph.allCells;
    }

//    public static void GetAllCells(final Object parent) {
//        int countChilds = 0;
//        final mxGraphModel graphModel = new mxGraphModel(parent);
//        countChilds = ((mxCell)parent).getChildCount();
//        for (int ii = 0; ii < countChilds; ++ii) {
//            final mxCell tempCell = (mxCell)graphModel.getChildAt(parent, ii);
//            if (tempCell.getChildCount() > 0) {
//                GetAllCells(tempCell);
//            }
//            else {
//                PowerSysGraph.allCells.add(tempCell);
//            }
//        }
//    }
    
    public Map<String, Object> EdgeStyle() {
        final Map<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, "connector");
        style.put(mxConstants.STYLE_ENDARROW, "classic");
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, "middle");
        style.put(mxConstants.STYLE_ALIGN, "center");
//        style.put(mxConstants.STYLE_STROKECOLOR, "#6482B9");
//        style.put(mxConstants.STYLE_FONTCOLOR, "#446299");
        style.put(mxConstants.STYLE_STROKECOLOR, "#191919");
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        style.put(mxConstants.STYLE_FONTSIZE, 20);
        return style;
    }
    
    @Override
    public String getToolTipForCell(final Object cell) {
        String tip = "<html>";
        final mxGeometry geo = this.getModel().getGeometry(cell);
        final mxCellState state = this.getView().getState(cell);
        if (this.getModel().isEdge(cell)) {
            tip = String.valueOf(tip) + "points={";
            if (geo != null) {
                final List<mxPoint> points = geo.getPoints();
                if (points != null) {
                    for (final mxPoint point : points) {
                        tip = String.valueOf(tip) + "[x=" + PowerSysGraph.numberFormat.format(point.getX())
                                + ",y=" + PowerSysGraph.numberFormat.format(point.getY()) + "],";
                    }
                    tip = tip.substring(0, tip.length() - 1);
                }
            }
            tip = String.valueOf(tip) + "}<br>";
            tip = String.valueOf(tip) + "absPoints={";
            if (state != null) {
                for (int i = 0; i < state.getAbsolutePointCount(); ++i) {
                    final mxPoint point2 = state.getAbsolutePoint(i);
                    tip = String.valueOf(tip) + "[x=" + PowerSysGraph.numberFormat.format(point2.getX())
                            + ",y=" + PowerSysGraph.numberFormat.format(point2.getY()) + "],";
                }
                tip = tip.substring(0, tip.length() - 1);
            }
            tip = String.valueOf(tip) + "}";
        }
        else {
            tip = String.valueOf(tip) + "geo=[";
            if (geo != null) {
                tip = String.valueOf(tip) + "x=" + PowerSysGraph.numberFormat.format(geo.getX()) + ",y=" + PowerSysGraph.numberFormat.format(geo.getY()) + ",width=" + PowerSysGraph.numberFormat.format(geo.getWidth()) + ",height=" + PowerSysGraph.numberFormat.format(geo.getHeight());
            }
            tip = String.valueOf(tip) + "]<br>";
            tip = String.valueOf(tip) + "state=[";
            if (state != null) {
                tip = String.valueOf(tip) + "x=" + PowerSysGraph.numberFormat.format(state.getX()) + ",y=" + PowerSysGraph.numberFormat.format(state.getY()) + ",width=" + PowerSysGraph.numberFormat.format(state.getWidth()) + ",height=" + PowerSysGraph.numberFormat.format(state.getHeight());
            }
            tip = String.valueOf(tip) + "]";
        }
        final mxPoint trans = this.getView().getTranslate();
        tip = String.valueOf(tip) + "<br>scale=" + PowerSysGraph.numberFormat.format(this.getView().getScale()) + ", translate=[x=" + PowerSysGraph.numberFormat.format(trans.getX()) + ",y=" + PowerSysGraph.numberFormat.format(trans.getY()) + "]";
        tip = String.valueOf(tip) + "</html>";
        return tip;
    }
    
    /*@Override
    public Object[] moveCells(Object[] cells, final double dx, final double dy, final boolean clone, Object target, final Point location) {
        if (cells != null && (dx != 0.0 || dy != 0.0 || clone || target != null)) {
            model.beginUpdate();
            try {
                if (clone) 
                {
                    cells = cloneCells(cells, isCloneInvalidEdges());
                    if (target == null) 
                    {
                        target = this.getDefaultParent();
                    }
                }
                cellsMoved(cells, dx, dy, !clone && isDisconnectOnMove() && isAllowDanglingEdges(), target == null);
                if (target != null) {
                    final Integer index = model.getChildCount(target);
                    this.cellsAdded(cells, target, index, null, null, true);
                }
                fireEvent(new mxEventObject("moveCells", new Object[] { "cells", cells, "dx", dx, "dy", dy, "clone", clone, "target", target, "location", location }));
            }
            finally 
            {
                model.endUpdate();
            }
            //this.model.endUpdate();
        }
        System.out.println("moveCells being called");
        return cells;
    }*/
    
    @Override
    public Object[] cloneCells(final Object[] cells, final boolean allowInvalidEdges) {
        Object[] clones = null;
        System.out.println("cloneCells being called");
        if (cells != null) {
            final Collection<Object> tmp = new LinkedHashSet<Object>(cells.length);
            tmp.addAll(Arrays.asList(cells));
            if (!tmp.isEmpty()) {
                final double scale = this.view.getScale();
                final mxPoint trans = this.view.getTranslate();
                clones = this.model.cloneCells(cells, true);
                for (int i = 0; i < cells.length; ++i) {
                    if (!allowInvalidEdges && this.model.isEdge(clones[i]) && this.getEdgeValidationError(clones[i], this.model.getTerminal(clones[i], true), this.model.getTerminal(clones[i], false)) != null) {
                        clones[i] = null;
                    }
                    else {
                        final mxGeometry g = this.model.getGeometry(clones[i]);
                        if (g != null) {
                            final mxCellState state = this.view.getState(cells[i]);
                            final mxCellState pstate = this.view.getState(this.model.getParent(cells[i]));
                            if (state != null && pstate != null) {
                                final double dx = pstate.getOrigin().getX();
                                final double dy = pstate.getOrigin().getY();
                                if (this.model.isEdge(clones[i])) {
                                    Object src;
                                    for (src = this.model.getTerminal(cells[i], true); src != null && !tmp.contains(src); src = this.model.getParent(src)) {}
                                    if (src == null) {
                                        final mxPoint pt = state.getAbsolutePoint(0);
                                        g.setTerminalPoint(new mxPoint(pt.getX() / scale - trans.getX(), pt.getY() / scale - trans.getY()), true);
                                    }
                                    Object trg;
                                    for (trg = this.model.getTerminal(cells[i], false); trg != null && !tmp.contains(trg); trg = this.model.getParent(trg)) {}
                                    if (trg == null) {
                                        final mxPoint pt2 = state.getAbsolutePoint(state.getAbsolutePointCount() - 1);
                                        g.setTerminalPoint(new mxPoint(pt2.getX() / scale - trans.getX(), pt2.getY() / scale - trans.getY()), false);
                                    }
                                    final List<mxPoint> points = g.getPoints();
                                    if (points != null) {
                                        for (final mxPoint pt3 : points) {
                                            pt3.setX(pt3.getX() + dx);
                                            pt3.setY(pt3.getY() + dy);
                                        }
                                    }
                                }
                                else {
                                    g.setX(g.getX() + dx);
                                    g.setY(g.getY() + dy);
                                }
                            }
                        }
                    }
                    if (((mxCell)clones[i]).getValue() instanceof Generator) {
                        ++DrawNetworkClean.itemCounter;
                        ((mxCell)clones[i]).setValue(new Generator(DrawNetworkClean.itemCounter, "A new Generator"),
                                DrawNetworkClean.itemCounter);
                    }
                    else if (((mxCell)clones[i]).getValue() instanceof Load) {
                        ++DrawNetworkClean.itemCounter;
                        ((mxCell)clones[i]).setValue(new Load(DrawNetworkClean.itemCounter, "A new Load"),
                                DrawNetworkClean.itemCounter);
                    }
                    else if (((mxCell)clones[i]).getValue() instanceof Junction) {
                        ((mxCell)clones[i]).setValue(new Junction());
                    }
                    else if (((mxCell)clones[i]).getValue() instanceof HorizontalLink) {
                        ++DrawNetworkClean.itemCounter;
                        ((mxCell)clones[i]).setValue(new HorizontalLink(DrawNetworkClean.itemCounter));
                    }
                }
            }
            else {
                clones = new Object[0];
            }
        }
        return clones;
    }
    
    @Override
    public Object insertVertex(final Object parent, final String id, final Object value, final double x, final double y, final double width, final double height, final String style) {
        final Object vertex = this.createVertex(parent, id, value, x, y, width, height, style);
        return this.addCell(vertex, parent);
    }
    
    @Override
    public Object createEdge(final Object parent, final String id, final Object value, final Object source, final Object target, final String style) {
        final mxCell edge = new mxCell(value, new mxGeometry(), style);
        edge.setId(id);
        edge.setEdge(true);
        edge.getGeometry().setRelative(true);
        //System.out.println(DrawNetworkClean.itemCounter);
        ++DrawNetworkClean.itemCounter;
        edge.setValue(new HorizontalLink(DrawNetworkClean.itemCounter));
        final mxGeometry geom = edge.getGeometry();
        geom.setOffset(new mxPoint(15.0, 0.0));
        edge.setGeometry(geom);
        System.out.println("createEdge being called");
        return edge;
    }
    
    @Override
    public Object splitEdge(final Object edge, final Object[] cells, Object newEdge, final double dx, final double dy) {
        if (((mxCell)edge).getValue() instanceof HorizontalLink && !((mxCell)cells[0]).isEdge()) {
            if (newEdge == null) {
                ++DrawNetworkClean.itemCounter;
                newEdge = this.cloneCells(new Object[] { edge })[0];
                ((mxCell)newEdge).setValue(new HorizontalLink(DrawNetworkClean.itemCounter));
            }
            final Object parent = this.model.getParent(edge);
            final Object source = this.model.getTerminal(edge, true);
            this.model.beginUpdate();
            try {
                this.cellsMoved(cells, dx, dy, false, false);
                this.cellsAdded(cells, parent, this.model.getChildCount(parent), null, null, true);
                this.cellsAdded(new Object[] { newEdge }, parent, this.model.getChildCount(parent), source, cells[0], false);
                this.cellConnected(edge, cells[0], true, null);
                this.fireEvent(new mxEventObject("splitEdge", new Object[] { "edge", edge, "cells", cells, "newEdge", newEdge, "dx", dx, "dy", dy }));
            }
            finally {
                this.model.endUpdate();
            }
            this.model.endUpdate();
        }
        System.out.println("splitEdge being called");
        return newEdge;
    }

    @Override
    public Object connectCell(final Object edge, final Object terminal, final boolean source) {
        this.model.beginUpdate();
        try 
        {
            if ((!(((mxCell)edge).getValue() instanceof Generator) || !source) && (!(((mxCell)edge).getValue() instanceof Load) || source)) {
                this.cellConnected(edge, terminal, source, null);
                this.fireEvent(new mxEventObject("connectCell",
                        new Object[] { "edge", edge, "terminal", terminal,
                                "source", source }));
            }
        }
        finally 
        {
            this.model.endUpdate();
        }
        //this.model.endUpdate();
        System.out.println("connectCell being called");
        return edge;
    }
    
    @Override
    public void enterGroup(Object cell) {
        if (cell == null) {
            cell = this.getSelectionCell();
        }
        if (cell != null && this.isValidRoot(cell)) {
            this.view.setCurrentRoot(cell);
            this.clearSelection();
        }
        this.AutoNumberCells();
        this.refresh();
    }
    
    @Override
    public void exitGroup() {
        final Object root = this.model.getRoot();
        final Object current = this.getCurrentRoot();
        if (current != null) {
            Object next;
            for (next = this.model.getParent(current); next != root && !this.isValidRoot(next) && this.model.getParent(next) != root; next = this.model.getParent(next)) {}
            if (next == root || this.model.getParent(next) == root) {
                this.view.setCurrentRoot(null);
            }
            else {
                this.view.setCurrentRoot(next);
            }
            final mxCellState state = this.view.getState(current);
            if (state != null) {
                this.setSelectionCell(current);
            }
        }
        this.AutoNumberCells();
        this.refresh();
    }
    
    @Override
    public void home() {
        final Object current = this.getCurrentRoot();
        if (current != null) {
            this.view.setCurrentRoot(null);
            final mxCellState state = this.view.getState(current);
            if (state != null) {
                this.setSelectionCell(current);
            }
        }
        this.AutoNumberCells();
        this.refresh();
    }
}
