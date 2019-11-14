package com.mxgraph.examples.swing.editor;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import com.mxgraph.examples.swing.editor.EditorActions.HistoryAction;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;

import PowerSystemDrawingGUI.DrawNetworkClean;
import PowerSystemDrawingGUI.Generator;
import PowerSystemDrawingGUI.Junction;
import PowerSystemDrawingGUI.Load;
import PowerSystemDrawingGUI.PowerSysActions;

public class EditorPopupMenu extends JPopupMenu
{
	
	private static final long serialVersionUID = -3132749140550242191L;

	public EditorPopupMenu(BasicGraphEditor editor)
	{
		boolean GenSelOn = false;
        boolean LoadSelOn = false;
        boolean isEdge = false;
        
		boolean selected = !editor.getGraphComponent().getGraph()
				.isSelectionEmpty();
		
		final mxCell selCell = (mxCell)editor.getGraphComponent().getGraph().getSelectionCell();
        if (selCell != null) {
            DrawNetworkClean.selJunction = selCell;
            isEdge = DrawNetworkClean.selJunction.isEdge();
            if (selCell.getValue() instanceof Generator) {
                GenSelOn = true;
            }
            else if (selCell.getValue() instanceof Load) {
                LoadSelOn = true;
            }
            else if (selCell.getValue() instanceof Junction) {
                GenSelOn = true;
                LoadSelOn = true;
            }
        }
		//modified for appearing the generator
		add(editor.bind("Generator", PowerSysActions.getaddNewGeneratorAction()));

        add(editor.bind("Generator-VL", PowerSysActions.getaddNewGeneratorVLAction())).setEnabled(LoadSelOn && !isEdge);

		//modified for appearing the Load
		add(editor.bind("Load", PowerSysActions.getaddNewLoadAction()));

		add(editor.bind("Load-VL", PowerSysActions.getaddNewLoadVLAction())).setEnabled(GenSelOn && !isEdge);
        add(editor.bind("Junction", PowerSysActions.getaddNewJunctionAction()));
        add(editor.bind("Add Power Inputs", PowerSysActions.getaddPowerAction())).setEnabled(GenSelOn || LoadSelOn || isEdge);
        add(editor.bind("Add Link Capacitance", PowerSysActions.getaddCapacitanceAction())).setEnabled(GenSelOn || LoadSelOn || isEdge);
        addSeparator();
//		add()
		addSeparator();

		add(editor.bind(mxResources.get("undo"), new HistoryAction(true),
				"/com/mxgraph/examples/swing/images/undo.gif"));

		addSeparator();

		add(
				editor.bind(mxResources.get("cut"), TransferHandler
						.getCutAction(),
						"/com/mxgraph/examples/swing/images/cut.gif"))
				.setEnabled(selected);
		add(
				editor.bind(mxResources.get("copy"), TransferHandler
						.getCopyAction(),
						"/com/mxgraph/examples/swing/images/copy.gif"))
				.setEnabled(selected);
		add(editor.bind(mxResources.get("paste"), TransferHandler
				.getPasteAction(),
				"/com/mxgraph/examples/swing/images/paste.gif"));

		addSeparator();

		add(
				editor.bind(mxResources.get("delete"), mxGraphActions
						.getDeleteAction(),
						"/com/mxgraph/examples/swing/images/delete.gif"))
				.setEnabled(selected);

		addSeparator();

		// Creates the format menu
		JMenu menu = (JMenu) add(new JMenu(mxResources.get("format")));

		EditorMenuBar.populateFormatMenu(menu, editor);

		// Creates the shape menu
		menu = (JMenu) add(new JMenu(mxResources.get("shape")));

		EditorMenuBar.populateShapeMenu(menu, editor);

		addSeparator();

		add(
				editor.bind(mxResources.get("edit"), mxGraphActions
						.getEditAction())).setEnabled(selected);

		addSeparator();

		add(editor.bind(mxResources.get("selectVertices"), mxGraphActions
				.getSelectVerticesAction()));
		add(editor.bind(mxResources.get("selectEdges"), mxGraphActions
				.getSelectEdgesAction()));

		addSeparator();

		add(editor.bind(mxResources.get("selectAll"), mxGraphActions
				.getSelectAllAction()));
	}

}
