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

import java.text.*;
import java.util.List;
import java.net.*;

import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.io.*;
import java.awt.*;
import com.mxgraph.util.*;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.examples.swing.editor.EditorActions.ToggleOutlineItem;
import com.mxgraph.examples.swing.editor.*;
import javax.swing.*;

public class PowerSysGUI extends BasicGraphEditor
{
    private static final long serialVersionUID = -4601740824088314699L;
    public static final NumberFormat numberFormat = NumberFormat.getInstance();
    public static URL url;
    
    public PowerSysGUI() {
        this("Power System Graph Editor",
				new PowerSysGraphComponent(new PowerSysGraph()));
    }
    
    public PowerSysGUI(final String appTitle, final PowerSysGraphComponent component) {
        super(appTitle, component);
        final PowerSysGraph graph = (PowerSysGraph) this.graphComponent.getGraph();
		undoManager = createUndoManager();

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener()
		{
			public void invoke(Object source, mxEventObject evt)
			{
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt
						.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph
						.getSelectionCellsForChanges(changes));
				graph.AutoNumberCells();
                graph.refresh();
			}
		};

		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);
        
        mxCodecRegistry.addPackage("PowerSystemDrawingGUI");
        mxCodecRegistry.register(new mxObjectCodec(new Generator()));
        mxCodecRegistry.register(new mxObjectCodec(new Load()));
        mxCodecRegistry.register(new mxObjectCodec(new Junction()));
        mxCodecRegistry.register(new mxObjectCodec(new HorizontalLink()));
    }
    
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        
        mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";
		
        final PowerSysGUI editor = new PowerSysGUI();
        editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
        final mxGraphOutline outline = editor.getGraphOutline();
		outline.setVisible(!outline.isVisible()); // this is false to remove the zoomed pane from the left side pane
		outline.revalidate();
		SwingUtilities.invokeLater(new Runnable()
		{
			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run()
			{
				if (outline.getParent() instanceof JSplitPane)
				{
					if (outline.isVisible())
					{
						((JSplitPane) outline.getParent())
								.setDividerLocation(editor
										.getHeight() - 300);
						((JSplitPane) outline.getParent())
								.setDividerSize(6);
					}
					else
					{
						((JSplitPane) outline.getParent())
								.setDividerSize(0);
					}
				}
			}
		});
    }
}
