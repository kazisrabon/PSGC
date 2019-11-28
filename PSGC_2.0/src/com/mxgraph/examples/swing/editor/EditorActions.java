/*
 * Copyright (c) 2001-2012, JGraph Ltd
 */
package com.mxgraph.examples.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import PowerSystemDrawingGUI.PowerSysGUI;
import PowerSystemDrawingGUI.PowerSysGraph;
import PowerSystemDrawingGUI.PowerSysGraphComponent;
import org.w3c.dom.Document;

import com.mxgraph.analysis.mxDistanceCostFunction;
import com.mxgraph.analysis.mxGraphAnalysis;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxGdCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxStencilShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.util.png.mxPngTextDecoder;
import com.mxgraph.view.mxGraph;

import PowerSystemDrawingGUI.DrawNetworkClean;

import static PowerSystemDrawingGUI.DrawNetworkClean.changeDiagonals;

/**
 *
 */
public class EditorActions {
	/**
	 * 
	 * @param e
	 * @return Returns the graph for the given action event.
	 */
	public static final BasicGraphEditor getEditor(ActionEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();

			while (component != null && !(component instanceof BasicGraphEditor)) {
				component = component.getParent();
			}

			return (BasicGraphEditor) component;
		}

		return null;
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleRulersItem extends JCheckBoxMenuItem {
		/**
		 * 
		 */
		public ToggleRulersItem(final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(editor.getGraphComponent().getColumnHeader() != null);

			addActionListener(new ActionListener() {
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e) {
					mxGraphComponent graphComponent = editor.getGraphComponent();

					if (graphComponent.getColumnHeader() != null) {
						graphComponent.setColumnHeader(null);
						graphComponent.setRowHeader(null);
					} else {
						graphComponent.setColumnHeaderView(
								new EditorRuler(graphComponent, EditorRuler.ORIENTATION_HORIZONTAL));
						graphComponent
								.setRowHeaderView(new EditorRuler(graphComponent, EditorRuler.ORIENTATION_VERTICAL));
					}
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleGridItem extends JCheckBoxMenuItem {
		/**
		 * 
		 */
		public ToggleGridItem(final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(new ActionListener() {
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e) {
					mxGraphComponent graphComponent = editor.getGraphComponent();
					mxGraph graph = graphComponent.getGraph();
					boolean enabled = !graph.isGridEnabled();

					graph.setGridEnabled(enabled);
					graphComponent.setGridVisible(enabled);
					graphComponent.repaint();
					setSelected(enabled);
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleOutlineItem extends JCheckBoxMenuItem {
		/**
		 * 
		 */
		public ToggleOutlineItem(final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(new ActionListener() {
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e) {
					final mxGraphOutline outline = editor.getGraphOutline();
					outline.setVisible(!outline.isVisible());
					outline.revalidate();

					SwingUtilities.invokeLater(new Runnable() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						public void run() {
							if (outline.getParent() instanceof JSplitPane) {
								if (outline.isVisible()) {
									((JSplitPane) outline.getParent()).setDividerLocation(editor.getHeight() - 300);
									((JSplitPane) outline.getParent()).setDividerSize(6);
								} else {
									((JSplitPane) outline.getParent()).setDividerSize(0);
								}
							}
						}
					});
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ExitAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				editor.exit();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StylesheetAction extends AbstractAction {
		/**
		 * 
		 */
		protected String stylesheet;

		/**
		 * 
		 */
		public StylesheetAction(String stylesheet) {
			this.stylesheet = stylesheet;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxCodec codec = new mxCodec();
				Document doc = mxUtils.loadDocument(EditorActions.class.getResource(stylesheet).toString());

				if (doc != null) {
					codec.decode(doc.getDocumentElement(), graph.getStylesheet());
					graph.refresh();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ZoomPolicyAction extends AbstractAction {
		/**
		 * 
		 */
		protected int zoomPolicy;

		/**
		 * 
		 */
		public ZoomPolicyAction(int zoomPolicy) {
			this.zoomPolicy = zoomPolicy;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.setPageVisible(true);
				graphComponent.setZoomPolicy(zoomPolicy);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class GridStyleAction extends AbstractAction {
		/**
		 * 
		 */
		protected int style;

		/**
		 * 
		 */
		public GridStyleAction(int style) {
			this.style = style;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.setGridStyle(style);
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class GridColorAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("gridColor"),
						graphComponent.getGridColor());

				if (newColor != null) {
					graphComponent.setGridColor(newColor);
					graphComponent.repaint();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ScaleAction extends AbstractAction {
		/**
		 * 
		 */
		protected double scale;

		/**
		 * 
		 */
		public ScaleAction(double scale) {
			this.scale = scale;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				double scale = this.scale;

				if (scale == 0) {
					String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("value"),
							mxResources.get("scale") + " (%)", JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null) {
						scale = Double.parseDouble(value.replace("%", "")) / 100;
					}
				}

				if (scale > 0) {
					graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageSetupAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();
				PageFormat format = pj.pageDialog(graphComponent.getPageFormat());

				if (format != null) {
					graphComponent.setPageFormat(format);
					graphComponent.zoomAndCenter();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PrintAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				PrinterJob pj = PrinterJob.getPrinterJob();

				if (pj.printDialog()) {
					PageFormat pf = graphComponent.getPageFormat();
					Paper paper = new Paper();
					double margin = 36;
					paper.setImageableArea(margin, margin, paper.getWidth() - margin * 2,
							paper.getHeight() - margin * 2);
					pf.setPaper(paper);
					pj.setPrintable(graphComponent, pf);

					try {
						pj.print();
					} catch (PrinterException e2) {
						System.out.println(e2);
					}
				}
			}
		}
	}

	public static class RunAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean showDialog;

		/**
		 * 
		 */
		protected String lastDir = null;
		String outProbabilities = FileSystems.getDefault().getPath("probabilities.out").toAbsolutePath().toString();
		String outScenarios = FileSystems.getDefault().getPath("scenarios.out").toAbsolutePath().toString();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ", ";
		ArrayList<Double> PS = new ArrayList<Double>();
		ArrayList<Double> PR = new ArrayList<Double>();
		ArrayList<Double> PF = new ArrayList<Double>();
		Double[][] probs = new Double[3][PS.size()];
		ArrayList<Double> SS = new ArrayList<Double>();
		ArrayList<Double> SR = new ArrayList<Double>();
		ArrayList<Double> SF = new ArrayList<Double>();
		Double[][] scenarios = new Double[3][PS.size()];

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			final BasicGraphEditor editor = EditorActions.getEditor(e);
			if (editor != null) {
				final mxGraphComponent graphComponent = editor.getGraphComponent();
				final mxGraph graph = graphComponent.getGraph();
				final Path filename = FileSystems.getDefault().getPath("PSGC-adjList.csv").toAbsolutePath();
				DrawNetworkClean.ExportAdjacencyList(graph, filename.toString());
				try {
					Process p;
					if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
					{
						p = Runtime.getRuntime().exec("survivabilitywin.exe -a PSGC-adjList.csv");
					}
					else
					{
						System.out.println("Windows system not detected. Assuming Linux executable will work.");
						p = Runtime.getRuntime().exec("./survivabilitylinux -a PSGC-adjList.csv");
					}
					JFrame running = new JFrame("Running Survivability Analysis...");
					JLabel runlabel = new JLabel("Running Survivability Analysis...");

					running.getContentPane().add(runlabel);
					running.getContentPane().setLayout(new FlowLayout());

					runlabel.setHorizontalAlignment(JLabel.CENTER);
					runlabel.setVerticalAlignment(JLabel.CENTER);
					running.add(runlabel, BorderLayout.CENTER);

					running.setPreferredSize(new Dimension(300, 100));

					running.revalidate();
					running.repaint();
					running.setVisible(true);

					new Thread(new Runnable() {
						public void run() {
							BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
							String line = null;
							try {
								while ((line = input.readLine()) != null)
									System.out.println(line);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					p.waitFor();
					running.setVisible(false);
					running.dispose();

					final Path probOut = FileSystems.getDefault().getPath("probabilities.out").toAbsolutePath();
					final Path scenOut = FileSystems.getDefault().getPath("scenarios.out").toAbsolutePath();
					JFileChooser jfcp = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
					jfcp.setDialogTitle("Choose A Probabilities Output Save Location");
					int returnValue_p = jfcp.showSaveDialog(null);
					if (returnValue_p == JFileChooser.APPROVE_OPTION) {
						File selectedFile = jfcp.getSelectedFile();
						System.out.println("Probabilities output saved at " + selectedFile.getAbsolutePath());
						Files.copy(probOut, Paths.get(selectedFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
					}
					
					JFileChooser jfcs = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
					jfcs.setDialogTitle("Choose A Scenarios Output Save Location");
					int returnValue_s = jfcs.showSaveDialog(null);
					if (returnValue_s == JFileChooser.APPROVE_OPTION) {
						File selectedFile = jfcs.getSelectedFile();
						System.out.println("Scenarios output saved at " + selectedFile.getAbsolutePath());
						Files.copy(scenOut, Paths.get(selectedFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
					}
					
				}

				catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public static class DiagramAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings({})
		public void actionPerformed(ActionEvent e) {
			String outProbabilities = FileSystems.getDefault().getPath("probabilities.out").toAbsolutePath().toString();
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ", ";
			ArrayList<Double> PS = new ArrayList<Double>();
			ArrayList<Double> PR = new ArrayList<Double>();
			ArrayList<Double> PF = new ArrayList<Double>();
			Double[][] probs = new Double[3][PS.size()];

			try {

				br = new BufferedReader(new FileReader(outProbabilities));
				br.readLine();
				while ((line = br.readLine()) != null) {

					// use comma as separator
					String[] Probabilities = line.split(cvsSplitBy);

					PS.add(Double.parseDouble(Probabilities[1]));
					PR.add(Double.parseDouble(Probabilities[2]));
					PF.add(Double.parseDouble(Probabilities[3]));
				}

			} catch (FileNotFoundException ef) {
				ef.printStackTrace();
			} catch (IOException ei) {
				ei.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException ee) {
						ee.printStackTrace();
					}
				}
			}
			probs[0] = PS.toArray(new Double[PS.size()]);
			probs[1] = PR.toArray(new Double[PR.size()]);
			probs[2] = PF.toArray(new Double[PF.size()]);
			System.out.println(probs);
		}
	}

	/**
	 * 
	 * @author JuanDiego
	 *
	 */
	@SuppressWarnings("serial")
	public static class ExportAction extends AbstractAction {
		protected boolean showDialog;
		protected String listOrMatrix;
		protected String lastDir;
		protected boolean withWeight;
		protected ArrayList<Integer> weights;

		public ExportAction(final boolean showDialog, final String defineExport) {
			this.listOrMatrix = null;
			this.lastDir = null;
			this.showDialog = showDialog;
			this.listOrMatrix = defineExport;
			this.withWeight = false;
			this.weights = null;
		}

		public ExportAction(final boolean showDialog, final String defineExport,
							final boolean withWeight, final ArrayList<Integer> weights) {
			this.listOrMatrix = null;
			this.lastDir = null;
			this.showDialog = showDialog;
			this.listOrMatrix = defineExport;
			this.withWeight = withWeight;
			this.weights = weights;
		}


		@Override
		public void actionPerformed(final ActionEvent e) {
			final BasicGraphEditor editor = EditorActions.getEditor(e);
			if (editor != null) {
				final mxGraphComponent graphComponent = editor.getGraphComponent();
				final mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;
				final FileFilter vmlFileFilter = new DefaultFileFilter(".csv", "comma separated values");
				String filename = null;
				String wd;
				if (this.lastDir != null) {
					wd = this.lastDir;
				} else if (editor.getCurrentFile() != null) {
					wd = editor.getCurrentFile().getParent();
				} else {
					wd = System.getProperty("user.dir");
				}
				final JFileChooser fc = new JFileChooser(wd);
				final FileFilter defaultFilter = new DefaultFileFilter(".csv", "comma separated values");
				fc.addChoosableFileFilter(defaultFilter);
				final int rc = fc.showDialog(null, mxResources.get("save"));
				if (rc != 0) {
					return;
				}
				this.lastDir = fc.getSelectedFile().getParent();
				filename = fc.getSelectedFile().getAbsolutePath();
				selectedFilter = fc.getFileFilter();
				if (selectedFilter instanceof DefaultFileFilter) {
					final String ext = ((DefaultFileFilter) selectedFilter).getExtension();
					if (!filename.toLowerCase().endsWith(ext)) {
						filename = String.valueOf(filename) + ext;
					}
				}
				if (new File(filename).exists() && JOptionPane.showConfirmDialog(graphComponent,
						mxResources.get("overwriteExistingFile")) != 0) {
					return;
				}
				try {
					final String ext = filename.substring(filename.lastIndexOf(46) + 1);
					if (ext.equalsIgnoreCase("csv")) {
						if (this.listOrMatrix.equalsIgnoreCase("matrix")) {
							DrawNetworkClean.ExportAdjacencyMatrix(graph, filename, true);
						} else if (this.listOrMatrix.equalsIgnoreCase("list")) {
							DrawNetworkClean.ExportAdjacencyList(graph, filename);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(graphComponent, ex.toString(), mxResources.get("error"), 0);
				}
			}
		}
	}

	public static class SingleExportAction extends AbstractAction {
		protected boolean showDialog;
		protected String listOrMatrix;
		protected String lastDir;
		protected boolean withWeight;
		protected ArrayList<Integer> weights;

		public SingleExportAction(final boolean showDialog, final String defineExport) {
			this.listOrMatrix = null;
			this.lastDir = null;
			this.showDialog = showDialog;
			this.listOrMatrix = defineExport;
			this.withWeight = false;
			this.weights = null;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final BasicGraphEditor editor = EditorActions.getEditor(e);
			if (editor != null) {
				final mxGraphComponent graphComponent = editor.getGraphComponent();
				final mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;
				final FileFilter vmlFileFilter = new DefaultFileFilter(
						".csv",
						"comma separated values");
				String filename = null;
				String wd;
				if (this.lastDir != null) {
					wd = this.lastDir;
				} else if (editor.getCurrentFile() != null) {
					wd = editor.getCurrentFile().getParent();
				} else {
					wd = System.getProperty("user.dir");
				}
				final JFileChooser fc = new JFileChooser(wd);
//				final FileFilter defaultFilter = new DefaultFileFilter(".csv", "comma separated values");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//				final int rc = fc.showDialog(null, mxResources.get("save"));
//				if (rc != 0) {
//					return;
//				}
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): "
							+  fc.getCurrentDirectory());
					System.out.println("getSelectedFile() : "
							+  fc.getSelectedFile());
					DrawNetworkClean.ExportAdjacencyMatrixWithSingleLoad(graph,
							fc.getSelectedFile()+"",
							true);
				}
				else {
					JOptionPane.showMessageDialog(graphComponent,
							"No Selection?",
							mxResources.get("error"),
							0);
					System.out.println("No Selection");
				}

//				this.lastDir = fc.getSelectedFile().getParent();
//				filename = fc.getSelectedFile().getAbsolutePath();
//				selectedFilter = fc.getFileFilter();
//				if (selectedFilter instanceof DefaultFileFilter) {
//					final String ext = ((DefaultFileFilter) selectedFilter).getExtension();
//					if (!filename.toLowerCase().endsWith(ext)) {
//						filename = String.valueOf(filename) + ext;
//					}
//				}
//				if (new File(filename).exists() && JOptionPane.showConfirmDialog(graphComponent,
//						mxResources.get("overwriteExistingFile")) != 0) {
//					return;
//				}
//				try {
//					final String ext = filename.substring(filename.lastIndexOf(46) + 1);
//					if (ext.equalsIgnoreCase("csv")) {
//						if (this.listOrMatrix.equalsIgnoreCase("matrix")) {
//							DrawNetworkClean.ExportAdjacencyMatrixWithSingleLoad(graph, true);
//						} else if (this.listOrMatrix.equalsIgnoreCase("list")) {
//							DrawNetworkClean.ExportAdjacencyList(graph, filename);
//						}
//					}
//				} catch (Throwable ex) {
//					ex.printStackTrace();
//					JOptionPane.showMessageDialog(graphComponent, ex.toString(), mxResources.get("error"), 0);
//				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SaveAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean showDialog;

		/**
		 * 
		 */
		protected String lastDir = null;

		/**
		 * 
		 */
		public SaveAction(boolean showDialog) {
			this.showDialog = showDialog;
		}

		/**
		 * Saves XML+PNG format.
		 */
		protected void saveXmlPng(BasicGraphEditor editor, String filename, Color bg) throws IOException {
			mxGraphComponent graphComponent = editor.getGraphComponent();
			mxGraph graph = graphComponent.getGraph();

			// Creates the image for the PNG file
			BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, bg, graphComponent.isAntiAlias(),
					null, graphComponent.getCanvas());

			// Creates the URL-encoded XML data
			mxCodec codec = new mxCodec();
			String xml = URLEncoder.encode(mxXmlUtils.getXml(codec.encode(graph.getModel())), "UTF-8");
			mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
			param.setCompressedText(new String[] { "mxGraphModel", xml });

			// Saves as a PNG file
			FileOutputStream outputStream = new FileOutputStream(new File(filename));
			try {
				mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);

				if (image != null) {
					encoder.encode(image);

					editor.setModified(false);
					editor.setCurrentFile(new File(filename));
				} else {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noImageData"));
				}
			} finally {
				outputStream.close();
			}
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				mxGraphComponent graphComponent = editor.getGraphComponent();
				mxGraph graph = graphComponent.getGraph();
				FileFilter selectedFilter = null;
				DefaultFileFilter xmlPngFilter = new DefaultFileFilter(".png",
						"PNG+XML " + mxResources.get("file") + " (.png)");
				FileFilter vmlFileFilter = new DefaultFileFilter(".html",
						"VML " + mxResources.get("file") + " (.html)");
				String filename = null;
				boolean dialogShown = false;

				if (showDialog || editor.getCurrentFile() == null) {
					String wd;

					if (lastDir != null) {
						wd = lastDir;
					} else if (editor.getCurrentFile() != null) {
						wd = editor.getCurrentFile().getParent();
					} else {
						wd = System.getProperty("user.dir");
					}

					JFileChooser fc = new JFileChooser(wd);

					// Adds the default file format
					FileFilter defaultFilter = xmlPngFilter;
					fc.addChoosableFileFilter(defaultFilter);

					// Adds special vector graphics formats and HTML
					fc.addChoosableFileFilter(
							new DefaultFileFilter(".mxe", "mxGraph Editor " + mxResources.get("file") + " (.mxe)"));
					fc.addChoosableFileFilter(
							new DefaultFileFilter(".txt", "Graph Drawing " + mxResources.get("file") + " (.txt)"));
					fc.addChoosableFileFilter(
							new DefaultFileFilter(".svg", "SVG " + mxResources.get("file") + " (.svg)"));
					fc.addChoosableFileFilter(vmlFileFilter);
					fc.addChoosableFileFilter(
							new DefaultFileFilter(".html", "HTML " + mxResources.get("file") + " (.html)"));

					// Adds a filter for each supported image format
					Object[] imageFormats = ImageIO.getReaderFormatNames();

					// Finds all distinct extensions
					HashSet<String> formats = new HashSet<String>();

					for (int i = 0; i < imageFormats.length; i++) {
						String ext = imageFormats[i].toString().toLowerCase();
						formats.add(ext);
					}

					imageFormats = formats.toArray();

					for (int i = 0; i < imageFormats.length; i++) {
						String ext = imageFormats[i].toString();
						fc.addChoosableFileFilter(new DefaultFileFilter("." + ext,
								ext.toUpperCase() + " " + mxResources.get("file") + " (." + ext + ")"));
					}

					// Adds filter that accepts all supported image formats
					fc.addChoosableFileFilter(new DefaultFileFilter.ImageFileFilter(mxResources.get("allImages")));
					fc.setFileFilter(defaultFilter);
					int rc = fc.showDialog(null, mxResources.get("save"));
					dialogShown = true;

					if (rc != JFileChooser.APPROVE_OPTION) {
						return;
					} else {
						lastDir = fc.getSelectedFile().getParent();
					}

					filename = fc.getSelectedFile().getAbsolutePath();
					selectedFilter = fc.getFileFilter();

					if (selectedFilter instanceof DefaultFileFilter) {
						String ext = ((DefaultFileFilter) selectedFilter).getExtension();

						if (!filename.toLowerCase().endsWith(ext)) {
							filename += ext;
						}
					}

					if (new File(filename).exists() && JOptionPane.showConfirmDialog(graphComponent,
							mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
						return;
					}
				} else {
					filename = editor.getCurrentFile().getAbsolutePath();
				}

				try {
					String ext = filename.substring(filename.lastIndexOf('.') + 1);

					if (ext.equalsIgnoreCase("svg")) {
						mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer.drawCells(graph, null, 1, null,
								new CanvasFactory() {
									public mxICanvas createCanvas(int width, int height) {
										mxSvgCanvas canvas = new mxSvgCanvas(
												mxDomUtils.createSvgDocument(width, height));
										canvas.setEmbedded(true);

										return canvas;
									}

								});

						mxUtils.writeFile(mxXmlUtils.getXml(canvas.getDocument()), filename);
					} else if (selectedFilter == vmlFileFilter) {
						mxUtils.writeFile(mxXmlUtils.getXml(
								mxCellRenderer.createVmlDocument(graph, null, 1, null, null).getDocumentElement()),
								filename);
					} else if (ext.equalsIgnoreCase("html")) {
						mxUtils.writeFile(mxXmlUtils.getXml(
								mxCellRenderer.createHtmlDocument(graph, null, 1, null, null).getDocumentElement()),
								filename);
					} else if (ext.equalsIgnoreCase("mxe") || ext.equalsIgnoreCase("xml")) {
						mxCodec codec = new mxCodec();
						String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));

						mxUtils.writeFile(xml, filename);

						editor.setModified(false);
						editor.setCurrentFile(new File(filename));
					} else if (ext.equalsIgnoreCase("txt")) {
						String content = mxGdCodec.encode(graph);

						mxUtils.writeFile(content, filename);
					} else {
						Color bg = null;

						if ((!ext.equalsIgnoreCase("gif") && !ext.equalsIgnoreCase("png"))
								|| JOptionPane.showConfirmDialog(graphComponent,
										mxResources.get("transparentBackground")) != JOptionPane.YES_OPTION) {
							bg = graphComponent.getBackground();
						}

						if (selectedFilter == xmlPngFilter
								|| (editor.getCurrentFile() != null && ext.equalsIgnoreCase("png") && !dialogShown)) {
							saveXmlPng(editor, filename, bg);
						} else {
							BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, bg,
									graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

							if (image != null) {
								ImageIO.write(image, ext, new File(filename));
							} else {
								JOptionPane.showMessageDialog(graphComponent, mxResources.get("noImageData"));
							}
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(graphComponent, ex.toString(), mxResources.get("error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SelectShortestPathAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean directed;

		/**
		 * 
		 */
		public SelectShortestPathAction(boolean directed) {
			this.directed = directed;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object source = null;
				Object target = null;

				Object[] cells = graph.getSelectionCells();

				for (int i = 0; i < cells.length; i++) {
					if (model.isVertex(cells[i])) {
						if (source == null) {
							source = cells[i];
						} else if (target == null) {
							target = cells[i];
						}
					}

					if (source != null && target != null) {
						break;
					}
				}

				if (source != null && target != null) {
					int steps = graph.getChildEdges(graph.getDefaultParent()).length;
					Object[] path = mxGraphAnalysis.getInstance().getShortestPath(graph, source, target,
							new mxDistanceCostFunction(), steps, directed);
					graph.setSelectionCells(path);
				} else {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noSourceAndTargetSelected"));
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SelectSpanningTreeAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean directed;

		/**
		 * 
		 */
		public SelectSpanningTreeAction(boolean directed) {
			this.directed = directed;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				mxIGraphModel model = graph.getModel();

				Object parent = graph.getDefaultParent();
				Object[] cells = graph.getSelectionCells();

				for (int i = 0; i < cells.length; i++) {
					if (model.getChildCount(cells[i]) > 0) {
						parent = cells[i];
						break;
					}
				}

				Object[] v = graph.getChildVertices(parent);
				Object[] mst = mxGraphAnalysis.getInstance().getMinimumSpanningTree(graph, v,
						new mxDistanceCostFunction(), directed);
				graph.setSelectionCells(mst);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleDirtyAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.showDirtyRectangle = !graphComponent.showDirtyRectangle;
			}
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleConnectModeAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxConnectionHandler handler = graphComponent.getConnectionHandler();
				handler.setHandleEnabled(!handler.isHandleEnabled());
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleCreateTargetItem extends JCheckBoxMenuItem {
		/**
		 * 
		 */
		public ToggleCreateTargetItem(final BasicGraphEditor editor, String name) {
			super(name);
			setSelected(true);

			addActionListener(new ActionListener() {
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e) {
					mxGraphComponent graphComponent = editor.getGraphComponent();

					if (graphComponent != null) {
						mxConnectionHandler handler = graphComponent.getConnectionHandler();
						handler.setCreateTarget(!handler.isCreateTarget());
						setSelected(handler.isCreateTarget());
					}
				}
			});
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptPropertyAction extends AbstractAction {
		/**
		 * 
		 */
		protected Object target;

		/**
		 * 
		 */
		protected String fieldname, message;

		/**
		 * 
		 */
		public PromptPropertyAction(Object target, String message) {
			this(target, message, message);
		}

		/**
		 * 
		 */
		public PromptPropertyAction(Object target, String message, String fieldname) {
			this.target = target;
			this.message = message;
			this.fieldname = fieldname;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				try {
					Method getter = target.getClass().getMethod("get" + fieldname);
					Object current = getter.invoke(target);

					// TODO: Support other atomic types
					if (current instanceof Integer) {
						Method setter = target.getClass().getMethod("set" + fieldname, new Class[] { int.class });

						String value = (String) JOptionPane.showInputDialog((Component) e.getSource(), "Value", message,
								JOptionPane.PLAIN_MESSAGE, null, null, current);

						if (value != null) {
							setter.invoke(target, Integer.parseInt(value));
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// Repaints the graph component
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class TogglePropertyItem extends JCheckBoxMenuItem {
		/**
		 * 
		 */
		public TogglePropertyItem(Object target, String name, String fieldname) {
			this(target, name, fieldname, false);
		}

		/**
		 * 
		 */
		public TogglePropertyItem(Object target, String name, String fieldname, boolean refresh) {
			this(target, name, fieldname, refresh, null);
		}

		/**
		 * 
		 */
		public TogglePropertyItem(final Object target, String name, final String fieldname, final boolean refresh,
				ActionListener listener) {
			super(name);

			// Since action listeners are processed last to first we add the given
			// listener here which means it will be processed after the one below
			if (listener != null) {
				addActionListener(listener);
			}

			addActionListener(new ActionListener() {
				/**
				 * 
				 */
				public void actionPerformed(ActionEvent e) {
					execute(target, fieldname, refresh);
				}
			});

			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
				 * PropertyChangeEvent)
				 */
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equalsIgnoreCase(fieldname)) {
						update(target, fieldname);
					}
				}
			};

			if (target instanceof mxGraphComponent) {
				((mxGraphComponent) target).addPropertyChangeListener(propertyChangeListener);
			} else if (target instanceof mxGraph) {
				((mxGraph) target).addPropertyChangeListener(propertyChangeListener);
			}

			update(target, fieldname);
		}

		/**
		 * 
		 */
		public void update(Object target, String fieldname) {
			if (target != null && fieldname != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldname);

					if (getter != null) {
						Object current = getter.invoke(target);

						if (current instanceof Boolean) {
							setSelected(((Boolean) current).booleanValue());
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}

		/**
		 * 
		 */
		public void execute(Object target, String fieldname, boolean refresh) {
			if (target != null && fieldname != null) {
				try {
					Method getter = target.getClass().getMethod("is" + fieldname);
					Method setter = target.getClass().getMethod("set" + fieldname, new Class[] { boolean.class });

					Object current = getter.invoke(target);

					if (current instanceof Boolean) {
						boolean value = !((Boolean) current).booleanValue();
						setter.invoke(target, value);
						setSelected(value);
					}

					if (refresh) {
						mxGraph graph = null;

						if (target instanceof mxGraph) {
							graph = (mxGraph) target;
						} else if (target instanceof mxGraphComponent) {
							graph = ((mxGraphComponent) target).getGraph();
						}

						graph.refresh();
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class HistoryAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean undo;

		/**
		 * 
		 */
		public HistoryAction(boolean undo) {
			this.undo = undo;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				if (undo) {
					editor.getUndoManager().undo();
				} else {
					editor.getUndoManager().redo();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class FontStyleAction extends AbstractAction {
		/**
		 * 
		 */
		protected boolean bold;

		/**
		 * 
		 */
		public FontStyleAction(boolean bold) {
			this.bold = bold;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Component editorComponent = null;

				if (graphComponent.getCellEditor() instanceof mxCellEditor) {
					editorComponent = ((mxCellEditor) graphComponent.getCellEditor()).getEditor();
				}

				if (editorComponent instanceof JEditorPane) {
					JEditorPane editorPane = (JEditorPane) editorComponent;
					int start = editorPane.getSelectionStart();
					int ende = editorPane.getSelectionEnd();
					String text = editorPane.getSelectedText();

					if (text == null) {
						text = "";
					}

					try {
						HTMLEditorKit editorKit = new HTMLEditorKit();
						HTMLDocument document = (HTMLDocument) editorPane.getDocument();
						document.remove(start, (ende - start));
						editorKit.insertHTML(document, start,
								((bold) ? "<b>" : "<i>") + text + ((bold) ? "</b>" : "</i>"), 0, 0,
								(bold) ? HTML.Tag.B : HTML.Tag.I);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					editorPane.requestFocus();
					editorPane.select(start, ende);
				} else {
					mxIGraphModel model = graphComponent.getGraph().getModel();
					model.beginUpdate();
					try {
						graphComponent.stopEditing(false);
						graphComponent.getGraph().toggleCellStyleFlags(mxConstants.STYLE_FONTSTYLE,
								(bold) ? mxConstants.FONT_BOLD : mxConstants.FONT_ITALIC);
					} finally {
						model.endUpdate();
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class WarningAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Object[] cells = graphComponent.getGraph().getSelectionCells();

				if (cells != null && cells.length > 0) {
					String warning = JOptionPane.showInputDialog(mxResources.get("enterWarningMessage"));

					for (int i = 0; i < cells.length; i++) {
						graphComponent.setCellWarning(cells[i], warning);
					}
				} else {
					JOptionPane.showMessageDialog(graphComponent, mxResources.get("noCellSelected"));
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class NewAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
						mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {
					mxGraph graph = editor.getGraphComponent().getGraph();

					// Check modified flag and display save dialog
					mxCell root = new mxCell();
					root.insert(new mxCell());
					graph.getModel().setRoot(root);

					editor.setModified(false);
					editor.setCurrentFile(null);
					editor.getGraphComponent().zoomAndCenter();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ImportAction extends AbstractAction {
		/**
		 * 
		 */
		protected String lastDir;

		/**
		 * Loads and registers the shape as a new shape in mxGraphics2DCanvas and adds a
		 * new entry to use that shape in the specified palette
		 * 
		 * @param palette The palette to add the shape to.
		 * @param nodeXml The raw XML of the shape
		 * @param path    The path to the directory the shape exists in
		 * @return the string name of the shape
		 */
		public static String addStencilShape(EditorPalette palette, String nodeXml, String path) {

			// Some editors place a 3 byte BOM at the start of files
			// Ensure the first char is a "<"
			int lessthanIndex = nodeXml.indexOf("<");
			nodeXml = nodeXml.substring(lessthanIndex);
			mxStencilShape newShape = new mxStencilShape(nodeXml);
			String name = newShape.getName();
			ImageIcon icon = null;

			if (path != null) {
				String iconPath = path + newShape.getIconPath();
				icon = new ImageIcon(iconPath);
			}

			// Registers the shape in the canvas shape registry
			mxGraphics2DCanvas.putShape(name, newShape);

			if (palette != null && icon != null) {
				palette.addTemplate(name, icon, "shape=" + name, 80, 80, "");
			}

			return name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				String wd = (lastDir != null) ? lastDir : System.getProperty("user.dir");

				JFileChooser fc = new JFileChooser(wd);

				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				// Adds file filter for Dia shape import
				fc.addChoosableFileFilter(
						new DefaultFileFilter(".shape", "Dia Shape " + mxResources.get("file") + " (.shape)"));

				int rc = fc.showDialog(null, mxResources.get("importStencil"));

				if (rc == JFileChooser.APPROVE_OPTION) {
					lastDir = fc.getSelectedFile().getParent();

					try {
						if (fc.getSelectedFile().isDirectory()) {
							EditorPalette palette = editor.insertPalette(fc.getSelectedFile().getName());

							for (File f : fc.getSelectedFile().listFiles(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return name.toLowerCase().endsWith(".shape");
								}
							})) {
								String nodeXml = mxUtils.readFile(f.getAbsolutePath());
								addStencilShape(palette, nodeXml, f.getParent() + File.separator);
							}

							JComponent scrollPane = (JComponent) palette.getParent().getParent();
							editor.getLibraryPane().setSelectedComponent(scrollPane);

							// FIXME: Need to update the size of the palette to force a layout
							// update. Re/in/validate of palette or parent does not work.
							// editor.getLibraryPane().revalidate();
						} else {
							String nodeXml = mxUtils.readFile(fc.getSelectedFile().getAbsolutePath());
							String name = addStencilShape(null, nodeXml, null);

							JOptionPane.showMessageDialog(editor,
									mxResources.get("stencilImported", new String[] { name }));
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class OpenAction extends AbstractAction {
		/**
		 * 
		 */
		protected String lastDir;

		/**
		 * 
		 */
		protected void resetEditor(BasicGraphEditor editor) {
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}

		/**
		 * Reads XML+PNG format.
		 */
		protected void openXmlPng(BasicGraphEditor editor, File file) throws IOException {
			Map<String, String> text = mxPngTextDecoder.decodeCompressedText(new FileInputStream(file));

			if (text != null) {
				String value = text.get("mxGraphModel");

				if (value != null) {
					Document document = mxXmlUtils.parseXml(URLDecoder.decode(value, "UTF-8"));
					mxCodec codec = new mxCodec(document);
					codec.decode(document.getDocumentElement(), editor.getGraphComponent().getGraph().getModel());
					editor.setCurrentFile(file);
					resetEditor(editor);

					return;
				}
			}

			JOptionPane.showMessageDialog(editor, mxResources.get("imageContainsNoDiagramData"));
		}

		/**
		 * @throws IOException
		 *
		 */
		protected void openGD(BasicGraphEditor editor, File file, String gdText) {
			mxGraph graph = editor.getGraphComponent().getGraph();

			// Replaces file extension with .mxe
			String filename = file.getName();
			filename = filename.substring(0, filename.length() - 4) + ".mxe";

			if (new File(filename).exists() && JOptionPane.showConfirmDialog(editor,
					mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
				return;
			}

			((mxGraphModel) graph.getModel()).clear();
			mxGdCodec.decode(gdText, graph);
			editor.getGraphComponent().zoomAndCenter();
			editor.setCurrentFile(new File(lastDir + "/" + filename));
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
						mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {
					mxGraph graph = editor.getGraphComponent().getGraph();

					if (graph != null) {
						String wd = (lastDir != null) ? lastDir : System.getProperty("user.dir");

						JFileChooser fc = new JFileChooser(wd);

						// Adds file filter for supported file format
						DefaultFileFilter defaultFilter = new DefaultFileFilter(".mxe",
								mxResources.get("allSupportedFormats") + " (.mxe, .png, .vdx)") {

							public boolean accept(File file) {
								String lcase = file.getName().toLowerCase();

								return super.accept(file) || lcase.endsWith(".png") || lcase.endsWith(".vdx");
							}
						};
						fc.addChoosableFileFilter(defaultFilter);

						fc.addChoosableFileFilter(
								new DefaultFileFilter(".mxe", "mxGraph Editor " + mxResources.get("file") + " (.mxe)"));
						fc.addChoosableFileFilter(
								new DefaultFileFilter(".png", "PNG+XML  " + mxResources.get("file") + " (.png)"));

						// Adds file filter for VDX import
						fc.addChoosableFileFilter(
								new DefaultFileFilter(".vdx", "XML Drawing  " + mxResources.get("file") + " (.vdx)"));

						// Adds file filter for GD import
						fc.addChoosableFileFilter(
								new DefaultFileFilter(".txt", "Graph Drawing  " + mxResources.get("file") + " (.txt)"));

						fc.setFileFilter(defaultFilter);

						int rc = fc.showDialog(null, mxResources.get("openFile"));

						if (rc == JFileChooser.APPROVE_OPTION) {
							lastDir = fc.getSelectedFile().getParent();

							try {
								if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".png")) {
									openXmlPng(editor, fc.getSelectedFile());
								} else if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".txt")) {
									openGD(editor, fc.getSelectedFile(),
											mxUtils.readFile(fc.getSelectedFile().getAbsolutePath()));
								} else {
									Document document = mxXmlUtils
											.parseXml(mxUtils.readFile(fc.getSelectedFile().getAbsolutePath()));

									mxCodec codec = new mxCodec(document);
									codec.decode(document.getDocumentElement(), graph.getModel());
									editor.setCurrentFile(fc.getSelectedFile());

									resetEditor(editor);
								}
							} catch (IOException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(editor.getGraphComponent(), ex.toString(),
										mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ToggleAction extends AbstractAction {
		/**
		 * 
		 */
		protected String key;

		/**
		 * 
		 */
		protected boolean defaultValue;

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key) {
			this(key, false);
		}

		/**
		 * 
		 * @param key
		 */
		public ToggleAction(String key, boolean defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null) {
				graph.toggleCellStyles(key, defaultValue);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetLabelPositionAction extends AbstractAction {
		/**
		 * 
		 */
		protected String labelPosition, alignment;

		/**
		 * 
		 * @param
		 */
		public SetLabelPositionAction(String labelPosition, String alignment) {
			this.labelPosition = labelPosition;
			this.alignment = alignment;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.getModel().beginUpdate();
				try {
					// Checks the orientation of the alignment to use the correct constants
					if (labelPosition.equals(mxConstants.ALIGN_LEFT) || labelPosition.equals(mxConstants.ALIGN_CENTER)
							|| labelPosition.equals(mxConstants.ALIGN_RIGHT)) {
						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, alignment);
					} else {
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_LABEL_POSITION, labelPosition);
						graph.setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN, alignment);
					}
				} finally {
					graph.getModel().endUpdate();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class SetStyleAction extends AbstractAction {
		/**
		 * 
		 */
		protected String value;

		/**
		 * 
		 * @param
		 */
		public SetStyleAction(String value) {
			this.value = value;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.setCellStyle(value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class KeyValueAction extends AbstractAction {
		/**
		 * 
		 */
		protected String key, value;

		/**
		 * 
		 * @param key
		 */
		public KeyValueAction(String key) {
			this(key, null);
		}

		/**
		 * 
		 * @param key
		 */
		public KeyValueAction(String key, String value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.setCellStyles(key, value);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PromptValueAction extends AbstractAction {
		/**
		 * 
		 */
		protected String key, message;

		/**
		 * 
		 * @param key
		 */
		public PromptValueAction(String key, String message) {
			this.key = key;
			this.message = message;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				mxGraph graph = mxGraphActions.getGraph(e);

				if (graph != null && !graph.isSelectionEmpty()) {
					String value = (String) JOptionPane.showInputDialog((Component) e.getSource(),
							mxResources.get("value"), message, JOptionPane.PLAIN_MESSAGE, null, null, "");

					if (value != null) {
						if (value.equals(mxConstants.NONE)) {
							value = null;
						}

						graph.setCellStyles(key, value);
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AlignCellsAction extends AbstractAction {
		/**
		 * 
		 */
		protected String align;

		/**
		 * 
		 * @param
		 */
		public AlignCellsAction(String align) {
			this.align = align;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				graph.alignCells(align);
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class AutosizeAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			mxGraph graph = mxGraphActions.getGraph(e);

			if (graph != null && !graph.isSelectionEmpty()) {
				Object[] cells = graph.getSelectionCells();
				mxIGraphModel model = graph.getModel();

				model.beginUpdate();
				try {
					for (int i = 0; i < cells.length; i++) {
						graph.updateCellSize(cells[i]);
					}
				} finally {
					model.endUpdate();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class ColorAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name, key;

		/**
		 * 
		 * @param key
		 */
		public ColorAction(String name, String key) {
			this.name = name;
			this.key = key;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();

				if (!graph.isSelectionEmpty()) {
					Color newColor = JColorChooser.showDialog(graphComponent, name, null);

					if (newColor != null) {
						graph.setCellStyles(key, mxUtils.hexString(newColor));
					}
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundImageAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("backgroundImage"),
						"URL", JOptionPane.PLAIN_MESSAGE, null, null,
						"http://www.callatecs.com/images/background2.JPG");

				if (value != null) {
					if (value.length() == 0) {
						graphComponent.setBackgroundImage(null);
					} else {
						Image background = mxUtils.loadImage(value);
						// Incorrect URLs will result in no image.
						// TODO provide feedback that the URL is not correct
						if (background != null) {
							graphComponent.setBackgroundImage(new ImageIcon(background));
						}
					}

					// Forces a repaint of the outline
					graphComponent.getGraph().repaint();
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class BackgroundAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("background"), null);

				if (newColor != null) {
					graphComponent.getViewport().setOpaque(true);
					graphComponent.getViewport().setBackground(newColor);
				}

				// Forces a repaint of the outline
				graphComponent.getGraph().repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class PageBackgroundAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				Color newColor = JColorChooser.showDialog(graphComponent, mxResources.get("pageBackground"), null);

				if (newColor != null) {
					graphComponent.setPageBackgroundColor(newColor);
				}

				// Forces a repaint of the component
				graphComponent.repaint();
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class StyleAction extends AbstractAction {
		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof mxGraphComponent) {
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				String initial = graph.getModel().getStyle(graph.getSelectionCell());
				String value = (String) JOptionPane.showInputDialog(graphComponent, mxResources.get("style"),
						mxResources.get("style"), JOptionPane.PLAIN_MESSAGE, null, null, initial);

				if (value != null) {
					graph.setCellStyle(value);
				}
			}
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class XMLOpenAction extends AbstractAction {
		/**
		 *
		 */
		protected String lastDir;
		private ArrayList<mxCell> cells;
		mxGraphComponent graphComponent;
		PowerSysGraph graph, newGraph;

		public XMLOpenAction(mxGraphComponent graphComponent) {
			this.graphComponent = graphComponent;
			graph = (PowerSysGraph) graphComponent.getGraph();
		}
		/**
		 *
		 */
		protected void resetEditor(BasicGraphEditor editor) {
			editor.setModified(false);
			editor.getUndoManager().clear();
			editor.getGraphComponent().zoomAndCenter();
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			BasicGraphEditor editor = getEditor(e);

			if (editor != null) {
				if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
						mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {
					mxGraph graph = editor.getGraphComponent().getGraph();

					if (graph != null) {
						String wd = (lastDir != null) ? lastDir : System.getProperty("user.dir");

						JFileChooser fc = new JFileChooser(wd);

						// Adds file filter for supported file format
						DefaultFileFilter defaultFilter = new DefaultFileFilter(".csv",
								mxResources.get("allSupportedFormats") + " (.csv)") {

							public boolean accept(File file) {
								String lcase = file.getName().toLowerCase();

								return super.accept(file) || lcase.endsWith(".png") || lcase.endsWith(".vdx");
							}
						};
						fc.addChoosableFileFilter(defaultFilter);
						fc.setFileFilter(defaultFilter);

						int rc = fc.showDialog(null, mxResources.get("openFile"));

						if (rc == JFileChooser.APPROVE_OPTION) {
							lastDir = fc.getSelectedFile().getParent();

							try {
								if (fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".csv")) {
									String path1 = fc.getSelectedFile().getAbsolutePath();
									String fName = fc.getSelectedFile().getName();
									String thisLine;
									int sub = Integer.parseInt(fName.replaceAll("\\D+",""));
									int i = sub-1;
									if (fName.contains("Sub_")){
										System.out.println("File name Ok");
										FileInputStream fis = new FileInputStream(path1);
										DataInputStream myInput = new DataInputStream(fis);
										List<String[]> lines = new ArrayList<String[]>();
										while ((thisLine = myInput.readLine()) != null) {
											lines.add(thisLine.split(";"));
										}

// convert our list to a String array.
										int[][] array = new int[lines.size()][lines.size()];
										for (int j = 0; j < lines.size(); j++) {
											String string = lines.get(j)[0];
											String[] strings = string.split(",");
											for (int k = 0; k < strings.length; k++) {

												array[j][k] = Integer.parseInt(strings[k]);
											}
										}

										int isContainsSub = 0;
										ArrayList<ArrayList<Integer>> lists = new ArrayList<>();
										cells = PowerSysGraph.GetAllCells(graph.getDefaultParent());

										int id = findID(sub, cells);

										do {
//											get values to evaluate
											graph = editor.getGraphComponent().getGraph();
											cells = PowerSysGraph.GetAllCells(graph.getDefaultParent());
											int[] counts = DrawNetworkClean.countElements(graph);
											int[][] adjacencyMatrix = getAM(graph, counts);
											lists = DrawNetworkClean.getConnectedLinksofLoad(adjacencyMatrix, counts);

											for (int j = 0; j < lists.size(); j++) {
												ArrayList<Integer> list = lists.get(j);
												Object[] removedObj = new Object[1];
												int loadId = findID(list.get(0), cells);
												if (loadId != id) {
													if (list.size() == 3) {
														j = lists.size();
														int loadtoRemove = list.get(0);
														int link1toRemove = list.get(1);
														int link2toRemove = list.get(2);

//													get the cells
														mxCell load_Cell = findMxCell(loadtoRemove, cells);
														mxCell link1_Cell = findMxCell(link1toRemove, cells);
														mxCell link2_Cell = findMxCell(link2toRemove, cells);

														mxCell source_link1 = null,
																target_link1 = null,
																source_link2 = null,
																target_link2 = null;
//													find first link's source and target
														if (link1_Cell != null) {
															source_link1 = findSource(link1_Cell, cells);
															target_link1 = findTarget(link1_Cell, cells);
														}
//													find second link's source and target
														if (link2_Cell != null) {
															source_link2 = findSource(link2_Cell, cells);
															target_link2 = findTarget(link2_Cell, cells);
														}
														mxCell sourceCell = null, targetCell = null;
//													if cells are not null then find the connecting points
														if (source_link1 != null
																&& source_link2 != null
																&& target_link1 != null
																&& target_link2 != null) {
															if (source_link1 == source_link2) {
//															connect target_link1 and target_link2
//															find the cells
																sourceCell = target_link1;
																targetCell = target_link2;
																removedObj[0] = source_link1;
															} else if (source_link1 == target_link2) {
//															connect target_link1 and source_link2
																sourceCell = target_link1;
																targetCell = source_link2;
																removedObj[0] = source_link1;
															} else if (target_link1 == source_link2) {
//															connect source_link1 and target_link2
																sourceCell = source_link1;
																targetCell = target_link2;
																removedObj[0] = target_link1;
															} else if (target_link1 == target_link2) {
//															connect source_link1 and source_link2
																sourceCell = source_link1;
																targetCell = source_link2;
																removedObj[0] = target_link1;
															}
														}
//													remove load and links
//													connect two points
														if (sourceCell != null && targetCell != null) {
//														removedObj[0] = load_Cell;
//														removedObj[1] = link1_Cell;
//														removedObj[2] = link2_Cell;

															graph.removeCells(removedObj);
															graph.insertEdge(graph.getDefaultParent(),
																	null, "Edge",
																	sourceCell, targetCell);
														}
													}
													else {
														j = lists.size();
														for (int k = 0; k < list.size(); k++) {
															mxCell cell = findMxCell(list.get(k), cells);
															if (cell != null
																	&& cell.getValue().getClass().toString()
																	.contains("Load")) {
																removedObj[0] = cell;
																graph.removeCells(removedObj);
															}
														}
													}
												}
											}

//											get new values to continue the loop/ process
											graph = editor.getGraphComponent().getGraph();
											cells = PowerSysGraph.GetAllCells(graph.getDefaultParent());
											counts = DrawNetworkClean.countElements(graph);
											adjacencyMatrix = getAM(graph, counts);
											lists = DrawNetworkClean.getConnectedLinksofLoad(adjacencyMatrix, counts);
											for (int j = 0; j < lists.size(); j++) {
												if (lists.get(j).get(0) == sub)
													isContainsSub = 1;
											}

										}while (lists.size() > isContainsSub);

//										add 'A' to each numeration
                                        graph = editor.getGraphComponent().getGraph();
                                        cells = PowerSysGraph.GetAllCells(graph.getDefaultParent());
                                        cells = (ArrayList<mxCell>) cells.stream()
                                                .distinct()
                                                .collect(Collectors.toList());
										for (int i1 = 0; i1 < cells.size(); i1++) {
											mxCell cell = cells.get(i1);
											Object object = cell.getValue();
											String s = object.toString();
//											String id2 = ((Element) object).getAttribute("id");
											if (!s.equalsIgnoreCase("")) {
												int value = Integer.parseInt(s);
												cell.setValue("A"+value);
											}
										}
										System.out.println("test");
									}
								} else {
									JOptionPane.showMessageDialog(editor.getGraphComponent(), "Select Sub_*.csv file",
											mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
								}
							} catch (IOException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(editor.getGraphComponent(), ex.toString(),
										mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}

		private int findID(int cellValue, ArrayList<mxCell> cells) {
			int id = -1;
			for (int i = 0; i < cells.size(); i++) {
				mxCell cell = cells.get(i);
				String value = cell.getValue().toString();
				if(!value.isEmpty()){
					if(Integer.parseInt(value) == cellValue) {
						id = Integer.parseInt(cell.getId());
					}
				}
			}
			return id;
		}

		private mxCell findMxCell(int loadtoRemove, ArrayList<mxCell> cells) {
			for (int i = 0; i < cells.size(); i++) {
				mxCell cell = cells.get(i);
				if (!cell.getValue().toString().isEmpty()
						&& cell.getValue().toString() != null) {
					int cell_value = Integer.parseInt(cell.getValue().toString());
					if (loadtoRemove == cell_value)
						return cell;
				}
			}
			return null;
		}

		private mxCell findSource(mxCell link, ArrayList<mxCell> cells){
			mxCell cell = (mxCell) link.getSource();
			return cell;
		}

		private mxCell findTarget(mxCell link, ArrayList<mxCell> cells){
			mxCell cell = (mxCell) link.getTarget();
			return cell;
		}

		private int[][] getAM(mxGraph graph, int[] counts) {
			int[][] adjacencyMatrix = DrawNetworkClean.CreateAdjacencyMatrix(graph);
			adjacencyMatrix[0][0] = counts[0];
			adjacencyMatrix[1][1] = counts[1];
			adjacencyMatrix[2][2] = counts[2];
			adjacencyMatrix =
					changeDiagonals(adjacencyMatrix, counts[0], counts[1], counts[2]);
			return adjacencyMatrix;

		}
	}

	/**
	 *
	 */
	@SuppressWarnings("serial")
	public static class OpenNewPowerGraph extends AbstractAction {
		private ArrayList<mxCell> cells;
		mxGraphComponent graphComponent;
		PowerSysGraph graph, newGraph;

		public OpenNewPowerGraph(mxGraphComponent graphComponent) {
			this.graphComponent = graphComponent;
			graph = (PowerSysGraph) graphComponent.getGraph();
		}

		/**
		 *
		 */
		public void actionPerformed(ActionEvent e) {
			int x = 3;
			Object[] objects = new Object[1000];
			cells = PowerSysGraph.GetAllCells(graph.getDefaultParent());
			int[] counts = DrawNetworkClean.countElements(graph);
			int gen = counts[0];
			int genLoad = counts[0]+counts[1];
			for (int i = 0; i < cells.size(); i++) {
				mxCell cell = cells.get(i);
				String style = cell.getStyle();
				String id = cell.getId();
				Object object = cell.getValue();
				String s = object.toString();
				if (!s.equalsIgnoreCase("")) {
					int value = Integer.parseInt(s);
					if (value > gen
							&& value <= genLoad
							&& value != x
							&& style.equalsIgnoreCase("VerticalLink")) {
						objects[0] = cell;
					}
				}
				System.out.println(id+" "+style);

			}
//			PowerSysGUI editor = new PowerSysGUI("test", (PowerSysGraphComponent) graphComponent);
			PowerSysGUI editor = new PowerSysGUI("test", new PowerSysGraphComponent(graph));
			newGraph = new PowerSysGraph();
			newGraph = (PowerSysGraph) editor.getGraphComponent().getGraph();
//			PowerSysGUI editor = new PowerSysGUI();
			editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
			mxGraphOutline outline = editor.getGraphOutline();
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
			graph.removeCells(objects);
//			newGraph.removeCells(objects);

		}
	}
}
