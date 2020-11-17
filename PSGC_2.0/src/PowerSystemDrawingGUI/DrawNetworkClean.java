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

import java.awt.*;
import java.awt.geom.*;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.*;
import com.mxgraph.model.*;

import java.text.NumberFormat;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

import com.Ostermiller.util.ExcelCSVPrinter;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Multigraph;

import javax.swing.*;
import javax.swing.text.*;

public class DrawNetworkClean extends JApplet{
    static final long serialVersionUID = -2764911804288120883L;
    static int itemCounter;

    public static int getSub() {
        return sub;
    }

    public static void setSub(int sub) {
        DrawNetworkClean.sub = sub;
    }

    public static int sub = 0;
    public static mxCell selJunction;
    static ArrayList<mxICell> connVertices;
    public static Point2D mousePos;
    static Comparator<mxCell> comparator;
    private static final int OK = 0;
    private static final int INFO = 1;
    private static final int CANCEL = 2;
    private static final int SAVE = 3;
    private static final int REFRESH = 0;
    private static ArrayList<Integer> powers = null;
    private static ArrayList<Integer> capacitances = null;
    private static final ArrayList<Integer> zeros = new ArrayList<>(Arrays.asList(0, 0, 0));
    private static ArrayList<ArrayList<Integer>> connectedListArrayList;

    public static ArrayList<ArrayList<Integer>> getConnectedLinksofLoad(int[][] adjacencyMatrix, int[] counts) {
        int generatorsCount = counts[0];
        int loadsCount = counts[1];
        ArrayList<ArrayList<Integer>> lists = new ArrayList<>();
//        row
        for (int i = generatorsCount; i < generatorsCount + loadsCount; i++) {
            ArrayList<Integer> connectedList = new ArrayList<>();
            connectedList.add(i+1);
            for (int j = generatorsCount+loadsCount; j < adjacencyMatrix.length; j++) {
//                connections
                if (adjacencyMatrix[i][j] == 1) {
                    connectedList.add(j+1);
                }
            }
            lists.add(connectedList);
        }
        return lists;
    }

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);
    private JGraphXAdapter<String, RelationshipEdge> jgxAdapter;

    private static ArrayList<ArrayList<Integer>> getConnectedListArrayList() {
        return connectedListArrayList;
    }

    public static void setConnectedListArrayList(ArrayList<ArrayList<Integer>> connectedListArrayList) {
        DrawNetworkClean.connectedListArrayList = connectedListArrayList;
    }

    public DrawNetworkClean() {
        DrawNetworkClean.selJunction = null;
        DrawNetworkClean.itemCounter = 0;
        DrawNetworkClean.mousePos = new Point2D.Double(0.0, 0.0);
    }

    public static void ExportAdjacencyList(final mxGraph expGraph, final String filename) {
        final int[][] adjacencyList = CreateAdjacencyList(expGraph);
        try {
            WriteToFile(adjacencyList, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    export matrices with single load
    public static void ExportAdjacencyMatrixWithSingleLoad(final mxGraph expGraph, String directory, final Boolean isWritetoFile) {
        directory = directory+"\\";
        int[][] adjacencyMatrix = CreateAdjacencyMatrix(expGraph);
        int[] counts = countElements(expGraph);
        adjacencyMatrix[0][0] = counts[0];
        adjacencyMatrix[1][1] = counts[1];
        adjacencyMatrix[2][2] = counts[2];
        adjacencyMatrix = changeDiagonals(adjacencyMatrix, counts[0], counts[1], counts[2]);
        int generatorsCount = counts[0];
        int loadsCount = counts[1];
        int linksCount = counts[2];
//       a list of all loads
        ArrayList<Integer> multiLinkLoads;
        ArrayList<TwoInputs> twoInputsLoads = getTwoInputsLoads(adjacencyMatrix, counts);
        ArrayList<ArrayList<Integer>> connList1 = new ArrayList<>(getConnectedListArrayList());
//      if there is only one load
        if (counts[1] <= 1) {
            System.out.println("No modification available");
            ArrayList<ArrayList<String>> compare_matrix = new ArrayList<>();
            for (int i = 1; i <= generatorsCount+loadsCount+linksCount; i++) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(""+i);
                strings.add("A"+i);
                compare_matrix.add(strings);
            }
            try {
                int j = generatorsCount+loadsCount;
                WriteToFile(compare_matrix,
                        directory +"compare_matrix_" + j + ".csv",
                        1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        if all the loads are connected with more than two links
        else if (connList1.size()==0){
            for (int i = generatorsCount; i < generatorsCount + loadsCount; i++) {
                multiLinkLoads = new ArrayList<>();
                for (int j = generatorsCount; j < generatorsCount+loadsCount; j++) {
                    if (j!=i)
                        multiLinkLoads.add(j);
                }
//            create new matrix for modification
                int[][] new_adjacency_matrix = new int[adjacencyMatrix.length]
                        [adjacencyMatrix[0].length];
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    for (int k = 0; k < adjacencyMatrix[0].length; k++) {
                        new_adjacency_matrix[j][k] = adjacencyMatrix[j][k];
                    }
                }
                int[][] minimized_matrix = createReducedMatrix(new_adjacency_matrix, multiLinkLoads);
//                compare matrix
                ArrayList<ArrayList<String>> compare_matrix = new ArrayList<>();
//                for generators
                for (int k = 1; k <= generatorsCount; k++) {
                    ArrayList<String> strings = new ArrayList<>();
                    strings.add(""+k);
                    strings.add("A"+k);
                    compare_matrix.add(strings);
                }
//                for single load
                ArrayList<String> singleLoad = new ArrayList<>();
                int j = i+1;
                int loadNumber = generatorsCount+1;
                int remain = loadsCount-1;
                singleLoad.add(""+j);
                singleLoad.add("A"+loadNumber);
                compare_matrix.add(singleLoad);
//                for generators
                for (int k = generatorsCount+loadsCount; k <= generatorsCount+loadsCount+linksCount; k++) {
                    ArrayList<String> strings = new ArrayList<>();
                    int i1 = k - remain;
                    strings.add(""+k);
                    strings.add("A"+i1);
                    compare_matrix.add(strings);
                }
                try {
                    WriteToFile(minimized_matrix,
                            directory
                                    +"Sub_" + j + ".csv");
                    WriteToFile(compare_matrix,
                            directory +"compare_matrix_" + j + ".csv",
                            1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                initSubGraph(minimized_matrix, i);
            }
        }
//        if there has some scope to reduce links means there are some Links which has two links
        else {
            for (int i = generatorsCount; i < generatorsCount + loadsCount; i++) {
                multiLinkLoads = new ArrayList<>();
                for (int j = generatorsCount; j < generatorsCount+loadsCount; j++) {
                    if (j != i)
                        multiLinkLoads.add(j);
                }
//                create a new list for modification and calculation
                ArrayList<ArrayList<Integer>> connList2 = new ArrayList<>();
                for (int j = 0; j < connList1.size(); j++) {
                    ArrayList<Integer> list = connList1.get(j);
                    ArrayList<Integer> list1 = new ArrayList<>();
                    for (int k = 0; k < list.size(); k++) {
                        list1.add(list.get(k));
                        multiLinkLoads.remove(list.get(k));
                    }
                    connList2.add(list1);
                }
//                Reform connected List
//                TODO first check if it is in the connectedList or not
                for (int j = 0; j < connList2.size(); j++) {
                    ArrayList<Integer> list = connList2.get(j);
//                Todo Second if yes then remove the element from ConnectedList
                    if (i == list.get(0))
                        connList2.remove(j);
                }
                try {
                    int j = i+1;
                    WriteToFile(connList2,
                            directory
                                    +"reformed_conn_list_for_load_"
                                    + j
                                    + ".csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                make a connected connected list
//                Todo create a new node for connectedConnectedList
                for (int j = 0; j < connList2.size(); j++) { //j = 1
                    ArrayList<Integer> list = connList2.get(j);
                    if (list.get(0) != 0) {
                        ArrayList<Integer> list1;
                        ArrayList<Integer> list3;
                        ArrayList<Integer> list2;
                        for (int k = 0; k < connList2.size(); k++) { //k = 0
                            if (j != k && connList2.get(k).get(0) != 0) {
                                if (connList2.get(k).size() > connList2.get(j).size()) {
                                    list1 = connList2.get(k);
                                    list3 = new ArrayList<>(list1);
                                    list2 = list;
                                    connList2 = modifyConnectedList(connList2, list1, list2, list3, k, j);
                                } else if (connList2.get(j).size() > connList2.get(k).size()) {
                                    list1 = connList2.get(j);
                                    list3 = new ArrayList<>(list1);
                                    list2 = connList2.get(k);
                                    connList2 = modifyConnectedList(connList2, list1, list2, list3, j, k);
                                } else {
                                    list1 = list;
                                    list3 = new ArrayList<>(list1);
                                    list2 = connList2.get(k);
                                    connList2 = modifyConnectedList(connList2, list1, list2, list3, j, k);
                                }
                            }
                        }
                    }
                }
                connList2 = removeZeros(connList2);

                try {
                    int j = i+1;
                    WriteToFile(connList2,
                            directory
                            +"reformed_conn_conn_list_for_load_" + j + ".csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                draw or save single load matrix/ network for ith load
                int[][] new_adjacency_matrix = new int[adjacencyMatrix.length + connList2.size()]
                        [adjacencyMatrix.length + connList2.size()];
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    for (int k = 0; k < adjacencyMatrix[0].length; k++) {
                        new_adjacency_matrix[j][k] = adjacencyMatrix[j][k];
                    }
                }
                int rowcol = adjacencyMatrix.length;
                ArrayList<ArrayList<Integer>> connConnGl = new ArrayList<>();
                for (int j = 0; j < connList2.size(); j++) {
                    ArrayList<Integer> list = connList2.get(j);
                    ArrayList<Integer> connGL = new ArrayList<>();;
                    for (int k = 0; k < list.size(); k++) {
                        if (adjacencyMatrix[list.get(k)]
                                [list.get(k)] != -1) {

                            for (int l = 0; l < adjacencyMatrix.length; l++) {

                                if (adjacencyMatrix[list.get(k)][l] == 1 &&
                                        adjacencyMatrix[l][list.get(k)] == 1) {
//                                  connected generators or load with these links
                                    connGL.add(k);
                                    new_adjacency_matrix[rowcol][l] = 1;
                                    new_adjacency_matrix[l][rowcol] = 1;
                                }
                            }
                        }
                    }
                    connConnGl.add(connGL);
                    rowcol += 1;
                }
//                connect new nodes
                for (int j = 0; j < connConnGl.size(); j++) {
                    ArrayList<Integer> list1 = connConnGl.get(j);
                    ArrayList<Integer> list3 = new ArrayList<>(list1);
                    int x = adjacencyMatrix.length+j;
                    for (int k = 0; k < connConnGl.size(); k++) {
                        if (j != k){
                            int y = adjacencyMatrix.length+k;
                            ArrayList<Integer> list2 = connConnGl.get(j);
                            list3.retainAll(list2);
                            if (list3.size() > 0) {
                                new_adjacency_matrix[x][y] = 1;
//                                new_adjacency_matrix[y][x] = 1
                            }
                        }
                    }
                }

//                Todo making connected node 0
                ArrayList<Integer> allConnected = new ArrayList<>();
                for (int j = 0; j < connList2.size(); j++) {
                    ArrayList<Integer> list = connList2.get(j);
                    allConnected.addAll(list);
                    for (int old_value : list) {
                        for (int k = 0; k < new_adjacency_matrix.length; k++) {
                            new_adjacency_matrix[old_value][k] = 0;
                            new_adjacency_matrix[k][old_value] = 0;
                        }
                    }
                }
                if (multiLinkLoads.size()>0)
                    allConnected.addAll(multiLinkLoads);
                allConnected = (ArrayList<Integer>) allConnected.stream()
                        .distinct()
                        .collect(Collectors.toList());
//                Todo reduce row and column
                int[][] minimized_matrix = createReducedMatrix(new_adjacency_matrix, allConnected);
                try {
                    int j = i+1;
                    WriteToFile(new_adjacency_matrix,
                            directory
                            +"new_adjacency_matrix_for_load_" + j + ".csv");
                    WriteToFile(minimized_matrix,
                            directory
                            +"Sub_" + j + ".csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                we are creating a library to map the new value with the old value
//                "compare_matrix_x.csv" contains the mapping value for
//                "sub_x.csv"
//                format: 1st column is the old value, 2nd column is new value
                int minMatLength = minimized_matrix.length;
                int connListSize = connList2.size();
                int singleLoad = 1;
//                ArrayList<ArrayList<Integer>> compare_matrix = new ArrayList<>();
                ArrayList<ArrayList<String>> compare_matrix = new ArrayList<>();
                int links_unchanged_size = minMatLength-(generatorsCount+singleLoad+connListSize);
                ArrayList<Integer> links_changed = new ArrayList<>();
                for (int j = 0; j < connListSize; j++) {
                    links_changed.addAll(connList2.get(j));
                }
                for (int j = 0; j < links_changed.size(); j++) {
                    if (links_changed.get(j) <= generatorsCount+loadsCount){
                        links_changed.remove(j);
                    }
                }
                ArrayList<Integer> links_unchanged = new ArrayList<>();
                for (int j = generatorsCount+loadsCount; j < generatorsCount+loadsCount+linksCount; j++) {
                    if (!links_changed.contains(j)){
                        links_unchanged.add(j);
                    }
                }
//                loop for comparing generators and loads
                for (int j = 1; j <= generatorsCount; j++) {
//                    int i1 = j+1;
                    ArrayList<String> rows = new ArrayList<>();
                    rows.add(""+j);
                    rows.add("A"+j);
                    compare_matrix.add(rows);
                }
//              comparing for the single load
                int single_load_number = i+1;
                int new_load_number = generatorsCount+singleLoad;
                ArrayList<String> row = new ArrayList<>();
                row.add(""+single_load_number);
                row.add("A"+new_load_number);
                compare_matrix.add(row);

//              loop for comparing not minimized links
                if (links_unchanged_size > 0 && links_unchanged.size() > 0){
                    int ii = 1;
                    for (int j = 0; j < links_unchanged.size() ; j++) {
                        ArrayList<String> rows = new ArrayList<>();
                        int i1 = links_unchanged.get(j)+1;
                        rows.add(""+i1);
                        i1 = generatorsCount+singleLoad+ii;
                        rows.add("A"+i1);
                        compare_matrix.add(rows);
                        ++ii;
                    }
                }

//                loop for comparing minimized links
                for (int j = 0; j < connList2.size(); j++) {
                    for (int k = 0; k < connList2.get(j).size(); k++) {
                        ArrayList<String> rows = new ArrayList<>();
                        if (connList2.get(j).get(k) >= generatorsCount+loadsCount){
                            int i1 = connList2.get(j).get(k)+1;
                            rows.add(""+i1);
                            i1 = minMatLength - connListSize + 1;
                            rows.add("A"+i1);
                            compare_matrix.add(rows);
                        }
                    }
                    --connListSize;
                }
                try {
                    int j = i+1;
                    WriteToFile(compare_matrix,
                            directory +"compare_matrix_" + j + ".csv",
                            1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                initSubGraph(minimized_matrix, i);
            }
        }
    }

    public static void initSubGraph(int[][] minimized_matrix, int sub_Graph_name) {
        ArrayList<ArrayList<Integer>> listArrayList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> listArrayList2 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> listArrayList3 = new ArrayList<>();
        int gen = 0;
        int load = 0;
        int link = 0;
        int rm = 0;
        for (int j = 0; j < minimized_matrix.length; j++) {
            ArrayList<Integer> arrayList = new ArrayList<>();
            if (minimized_matrix[j][j] == 1){
                ++gen;
//                arrayList.add(j);
            }
            else if (minimized_matrix[j][j] == -1){
                ++load;
//                arrayList.add(j);
            }
            else if (minimized_matrix[j][j] == 0) {
                ++link;
                for (int k = 0; k < gen + load; k++) {
                    if (minimized_matrix[j][k] == 1)
                        arrayList.add(k);
                }
            }
//            removed load, rm
            ArrayList<Integer> arrayList2 = new ArrayList<>();
            if (arrayList.size() == 1) {
                arrayList2.add(j);
                for (int k = gen+load; k < minimized_matrix[0].length; k++) {
                    if (minimized_matrix[j][k] == 1) {
                        if (checkLinkforOneGL(k, minimized_matrix, gen+load))
                            arrayList2.add(k);
                    }
                }
            }
            Collections.sort(arrayList2);
            if (arrayList2.size()>0)
                listArrayList2.add(arrayList2);
//            if (arrayList.size()>0)
            listArrayList.add(arrayList);
        }
//        find matches
        for (int i = 0; i < listArrayList2.size(); i++) {
            ArrayList<Integer> list1 = listArrayList2.get(i);
//            ArrayList<Integer> arrayList = new ArrayList<>();
//            for (int j = 0; j < list1.size(); j++) {
//                int i1 = list1.get(j);
//                arrayList.add(i1);
//            }
            for (int j = 0; j < listArrayList2.size(); j++) {
                ArrayList<Integer> arrayList = new ArrayList<>(list1);
                ArrayList<Integer> list2 = listArrayList2.get(j);
                if (i != j){
                    arrayList.retainAll(list2);
//                    match 1
                    if ((arrayList.size() == 2 && list1.size() == 2 && list2.size() == 2)
                            ||(arrayList.size()>2)){
                        listArrayList3.add(arrayList);
                    }
                }
            }
        }
        listArrayList3 = (ArrayList<ArrayList<Integer>>) listArrayList3
                .stream()
                .distinct()
                .collect(Collectors.toList());
        int counter = 0;
        for (int i = 0; i < listArrayList3.size(); i++) {
            ArrayList<Integer> list = listArrayList3.get(i);
            for (int j = 0; j < list.size(); j++) {
                int pos = list.get(j);
                ArrayList<Integer> list2 = listArrayList.get(pos);
                if (list2.size() == 1){
                    list2.add(gen+load+link+counter);
                }
                listArrayList.remove(pos);
                listArrayList.add(pos, list2);
            }
            ++counter;
        }
        viewSubgraph(listArrayList, gen, load, link, counter, sub_Graph_name);
    }

    private static boolean checkLinkforOneGL(int pos, int[][] minimized_matrix, int limit) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (minimized_matrix[pos][i] == 1)
                arrayList.add(i);
        }
        return arrayList.size() == 1;
    }

    private static void viewSubgraph(ArrayList<ArrayList<Integer>> listArrayList, int gen, int load, int link, int rmCounter, int sub) {
        int subNo = sub+1;
        DrawNetworkClean applet = new DrawNetworkClean();
        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Sub "+subNo+" Graph");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        ListenableGraph<String, RelationshipEdge> g = new DefaultListenableGraph(
                new Multigraph(RelationshipEdge.class));

        applet.jgxAdapter = new JGraphXAdapter(g);
        applet.setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(applet.jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        applet.getContentPane().add(component);
        applet.resize(DEFAULT_SIZE);

        ArrayList<Integer> stringGen = new ArrayList<>();
        for (int i = 1; i <= gen ; i++) {
            stringGen.add(i);
            g.addVertex("G"+i);
        }
        ArrayList<Integer> stringLd = new ArrayList<>();
        for (int i = 1; i <= load ; i++) {
            int i1 = gen+i;
            stringLd.add(i1);
            g.addVertex("L"+i1);
        }
        ArrayList<Integer> stringRMLd = new ArrayList<>();
        for (int i = 1; i <= rmCounter; i++){
            int i1 = gen+load+link+i;
            stringRMLd.add(i1);
            g.addVertex("RM"+i1);
        }
        for (int i = 0; i < listArrayList.size(); i++) {
            if (listArrayList.get(i).size() == 2){
                int a = listArrayList.get(i).get(0) + 1;
                int b = listArrayList.get(i).get(1) + 1;
                String v1 = "",v2 = "";
//                s1
                if (stringGen.contains(a))
                    v1 = "G"+a;
                else if (stringLd.contains(a))
                    v1 = "L"+a;
                else if (stringRMLd.contains(a))
                    v1 = "RM"+a;
//                s2
                if (stringGen.contains(b))
                    v2 = "G"+b;
                else if (stringLd.contains(b))
                    v2 = "L"+b;
                else if (stringRMLd.contains(b))
                    v2 = "RM"+b;

                if (!v1.isEmpty() && !v2.isEmpty())
                    g.addEdge(v1, v2, new RelationshipEdge("link"));
            }
        }

        mxCircleLayout layout = new mxCircleLayout(applet.jgxAdapter);
        int radius = 100;
        layout.setX0((double)DEFAULT_SIZE.width / 2.0D - (double)radius);
        layout.setY0((double)DEFAULT_SIZE.height / 2.0D - (double)radius);
        layout.setRadius((double)radius);
        layout.setMoveCircle(true);
        layout.execute(applet.jgxAdapter.getDefaultParent());
    }

    private static int[][] createReducedMatrix(int[][] new_adjacency_matrix, ArrayList<Integer> allConnected) {
        int[][] minimized_matrix = new int[new_adjacency_matrix.length - allConnected.size()]
                [new_adjacency_matrix.length - allConnected.size()];
        Collections.sort(allConnected);
        int p = 0;
        for (int j = 0; j < new_adjacency_matrix.length; ++j) {
            if (allConnected.contains(j))
                continue;


            int q = 0;
            for (int k = 0; k < new_adjacency_matrix[0].length; ++k) {
                if (allConnected.contains(k))
                    continue;

                minimized_matrix[p][q] = new_adjacency_matrix[j][k];
                ++q;
            }

            ++p;
        }
        return minimized_matrix;
    }

    private static ArrayList<ArrayList<Integer>> removeZeros(ArrayList<ArrayList<Integer>> connList1) {
        for (int i = 0; i < connList1.size(); i++) {
            ArrayList<Integer> list = connList1.get(i);
            if (list.get(0) == 0 && list.get(1) == 0 && list.get(2) == 0)
                connList1.remove(list);
        }
        return connList1;
    }

    private static ArrayList<ArrayList<Integer>> modifyConnectedList(
            ArrayList<ArrayList<Integer>> connList1,
            ArrayList<Integer> list1,
            ArrayList<Integer> list2,
            ArrayList<Integer> list3,
            int first,
            int second) {
        list3.retainAll(list2);
        if (list3.size() > 0) {
            list1.addAll(list2);
            list1 = (ArrayList<Integer>) list1.stream()
                    .distinct()
                    .collect(Collectors.toList());
            connList1.remove(first);
            connList1.add(first, list1);
            connList1.remove(second);
            connList1.add(second, zeros);
        }
        return connList1;
    }

    //    export adjacency matrix with weight
    public static void ExportAdjacencyMatrix(final mxGraph expGraph, String filename,
                                             final boolean isWritetoFile,
                                             final ArrayList<Integer> weights,
                                             final int countLink) {
        int[][] adjacencyMatrix = CreateAdjacencyMatrix(expGraph);
//        adjacencyMatrix.length = # of rows
//        adjacencyMatrix[0].length = # of cols
        if ((weights.size() == adjacencyMatrix.length
                && weights.size() == adjacencyMatrix[0].length)
                ||(weights.size() == adjacencyMatrix.length-countLink
                && weights.size() == adjacencyMatrix[0].length-countLink)) {
            for (int i = 0; i < weights.size(); i++) {
                adjacencyMatrix[i][i] = weights.get(i);
            }
            //        write to file

            String wd = System.getProperty("user.dir");
            final JFileChooser fc = new JFileChooser(wd);
            final DefaultFileFilter defaultFilter =  new DefaultFileFilter(
                    ".csv",
                    "comma separated values");
            fc.addChoosableFileFilter(defaultFilter);
            final int rc = fc.showDialog(null, mxResources.get("save"));
            if (rc != 0) {
                return;
            }
            String lastDir = fc.getSelectedFile().getParent();
            filename = fc.getSelectedFile().getAbsolutePath();
            javax.swing.filechooser.FileFilter selectedFilter = fc.getFileFilter();
            if (selectedFilter instanceof DefaultFileFilter) {
                final String ext = ((DefaultFileFilter) selectedFilter).getExtension();
                if (!filename.toLowerCase().endsWith(ext)) {
                    filename = String.valueOf(filename) + ext;
                }
            }
            if (new File(filename).exists() && JOptionPane.showConfirmDialog(null,
                    mxResources.get("overwriteExistingFile")) != 0) {
                return;
            }
            try {
                final String ext = filename.substring(filename.lastIndexOf(46) + 1);
                if (ext.equalsIgnoreCase("csv")) {
                    WriteToFile(adjacencyMatrix, filename);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        ex.toString(),
                        mxResources.get("error"),
                        0);
            }


//            if (isWritetoFile) {
//                try {
//                    WriteToFile(adjacencyMatrix, filename);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(null,
//                            "" + e.getMessage(),
//                            "Save Error",
//                            JOptionPane.ERROR_MESSAGE);
//                }
//            }
        }
//        size of weight is mismatched to length of matrix's row&col
        else
            JOptionPane.showMessageDialog(null,
                    "Number of capacity and elements mismatched",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
    }

    public static void ExportAdjacencyMatrix(final mxGraph expGraph, final String filename, final Boolean isWritetoFile) {
        int[][] adjacencyMatrix = CreateAdjacencyMatrix(expGraph);
        int[] counts = countElements(expGraph);
        adjacencyMatrix[0][0] = counts[0];
        adjacencyMatrix[1][1] = counts[1];
        adjacencyMatrix[2][2] = counts[2];
        adjacencyMatrix = changeDiagonals(adjacencyMatrix, counts[0], counts[1], counts[2]);

        if (isWritetoFile) {
            try {
                WriteToFile(adjacencyMatrix, filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<TwoInputs> getTwoInputsLoads(int[][] adjacencyMatrix, int[] counts) {
        int countLinks;
        int generatorsCount = counts[0];
        int loadsCount = counts[1];
        ArrayList<TwoInputs> twoInputsArrayList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> arrayListArrayList = new ArrayList<>();
//        row
        for (int i = generatorsCount; i < generatorsCount + loadsCount; i++) {
            countLinks = 0;
            ArrayList<Integer> connectionNum = new ArrayList<>();
            ArrayList<Integer> connectedList = new ArrayList<>();
            connectedList.add(i);
            for (int j = i; j < adjacencyMatrix.length; j++) {
//                connections
                if (adjacencyMatrix[i][j] == 1) {
                    connectionNum.add(j);
                    connectedList.add(j);
                    countLinks += 1;
                }
            }
            if (countLinks == 2) {
                twoInputsArrayList.add(new TwoInputs(i, connectionNum));
                arrayListArrayList.add(connectedList);
            }
        }
        setConnectedListArrayList(arrayListArrayList);
//        try {
//            WriteToFile(arrayListArrayList, "connectedListArrayList.csv");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return twoInputsArrayList;
    }

    public static int[] countElements(mxGraph expGraph) {
        int[] counts = new int[]{0, 0, 0};
        expGraph.home();
        final Object parent = expGraph.getDefaultParent();
        PowerSysGraph.allCells = new ArrayList<mxCell>();
        PowerSysGraph.GetAllCells(parent);
        DrawNetworkClean.comparator = new Comparator<mxCell>() {
            @Override
            public int compare(final mxCell o1, final mxCell o2) {
                int numo1 = 0;
                int numo2 = 0;
                if (o1.getValue() instanceof Generator) {
                    final Generator gen = (Generator) o1.getValue();
                    numo1 = gen.getCount();
                } else if (o1.getValue() instanceof Load) {
                    final Load ld = (Load) o1.getValue();
                    numo1 = ld.getCount();
                } else if (o1.getValue() instanceof HorizontalLink) {
                    final HorizontalLink lk = (HorizontalLink) o1.getValue();
                    numo1 = lk.getCount();
                }
                if (o2.getValue() instanceof Generator) {
                    final Generator gen = (Generator) o2.getValue();
                    numo2 = gen.getCount();
                } else if (o2.getValue() instanceof Load) {
                    final Load ld = (Load) o2.getValue();
                    numo2 = ld.getCount();
                } else if (o2.getValue() instanceof HorizontalLink) {
                    final HorizontalLink lk = (HorizontalLink) o2.getValue();
                    numo2 = lk.getCount();
                }
                if (numo1 > numo2) {
                    return 1;
                }
                if (numo1 < numo2) {
                    return -1;
                }
                return 0;
            }
        };
        Collections.sort(PowerSysGraph.allCells, DrawNetworkClean.comparator);
        final mxCell[] allCellsArray = PowerSysGraph.allCells.toArray(new mxCell[PowerSysGraph.allCells.size()]);
        for (int ll = 0; ll < allCellsArray.length; ++ll) {
            final mxCell element = allCellsArray[ll];
            if (element != null && element.getValue() != null) {
                if (!(element.getValue() instanceof Junction)) {
                    if (element.getValue() instanceof Generator) {
                        ++counts[0];
                    } else if (element.getValue() instanceof Load) {
                        ++counts[1];
                    } else if (element.getValue() instanceof HorizontalLink) {
                        ++counts[2];
                    }
                }
            }
        }
        return counts;
    }

    private static int[][] CreateAdjacencyList(final mxGraph expGraph) {
        final ArrayList<mxCell> connCells = new ArrayList<mxCell>();
        int countGen = 0;
        int countLd = 0;
        int countHL = 0;
        final ArrayList<Integer> cellType = new ArrayList<Integer>();
        final ArrayList<Integer> countConnElements = new ArrayList<Integer>();
        int oldVal = 0;
        expGraph.home();
        final Object parent = expGraph.getDefaultParent();
        final mxGraphModel expGraphModel = new mxGraphModel(parent);
        PowerSysGraph.allCells = new ArrayList<mxCell>();
        PowerSysGraph.GetAllCells(parent);
        DrawNetworkClean.comparator = new Comparator<mxCell>() {
            @Override
            public int compare(final mxCell o1, final mxCell o2) {
                int numo1 = 0;
                int numo2 = 0;
                if (o1.getValue() instanceof Generator) {
                    final Generator gen = (Generator) o1.getValue();
                    numo1 = gen.getCount();
                } else if (o1.getValue() instanceof Load) {
                    final Load ld = (Load) o1.getValue();
                    numo1 = ld.getCount();
                } else if (o1.getValue() instanceof HorizontalLink) {
                    final HorizontalLink lk = (HorizontalLink) o1.getValue();
                    numo1 = lk.getCount();
                }
                if (o2.getValue() instanceof Generator) {
                    final Generator gen = (Generator) o2.getValue();
                    numo2 = gen.getCount();
                } else if (o2.getValue() instanceof Load) {
                    final Load ld = (Load) o2.getValue();
                    numo2 = ld.getCount();
                } else if (o2.getValue() instanceof HorizontalLink) {
                    final HorizontalLink lk = (HorizontalLink) o2.getValue();
                    numo2 = lk.getCount();
                }
                if (numo1 > numo2) {
                    return 1;
                }
                if (numo1 < numo2) {
                    return -1;
                }
                return 0;
            }
        };
        Collections.sort(PowerSysGraph.allCells, DrawNetworkClean.comparator);
        final mxCell[] allCellsArray = PowerSysGraph.allCells.toArray(new mxCell[PowerSysGraph.allCells.size()]);
        for (int ll = 0; ll < allCellsArray.length; ++ll) {
            final mxCell element = allCellsArray[ll];
            if (element != null && element.getValue() != null) {
                System.out.println(" Value: " + element.getValue().toString());
                if (!(element.getValue() instanceof Junction)) {
                    if (element.getValue() instanceof Generator) {
                        ++countGen;
                        cellType.add(1);
                    } else if (element.getValue() instanceof Load) {
                        ++countLd;
                        cellType.add(-1);
                    } else if (element.getValue() instanceof HorizontalLink) {
                        ++countHL;
                        cellType.add(0);
                    }
                    final Object[] CountAndCells = AddCellsAndCountConnections(element, expGraphModel);
                    countConnElements.add((int) CountAndCells[0] + oldVal);
                    connCells.addAll((Collection<? extends mxCell>) CountAndCells[1]);
                    oldVal += (int) CountAndCells[0];
                }
            }
        }
        final int[] connElements = ConvertCellToNumber(connCells);
        final Integer[] cellTypeArrayTemp = new Integer[cellType.size()];
        System.arraycopy(cellType.toArray(), 0, cellTypeArrayTemp, 0, cellType.size());
        final int[] cellTypeArray = new int[cellTypeArrayTemp.length];
        for (int ii = 0; ii < cellTypeArrayTemp.length; ++ii) {
            cellTypeArray[ii] = cellTypeArrayTemp[ii];
        }
        final Integer[] countConnElementsArrayTemp = new Integer[countConnElements.size()];
        System.arraycopy(countConnElements.toArray(), 0, countConnElementsArrayTemp, 0, countConnElements.size());
        final int[] countConnElementsArray = new int[countConnElementsArrayTemp.length];
        for (int ii2 = 0; ii2 < countConnElementsArrayTemp.length; ++ii2) {
            countConnElementsArray[ii2] = countConnElementsArrayTemp[ii2];
        }
        return new int[][]{{countGen}, {countLd}, {countHL}, countConnElementsArray, connElements};
    }

    public static int[][] CreateAdjacencyMatrix(final mxGraph expGraph) {
        int countJunctions = 0;
        expGraph.home();
        final Object parent = expGraph.getDefaultParent();
        final mxGraphModel expGraphModel = new mxGraphModel(parent);
        PowerSysGraph.allCells = new ArrayList<mxCell>();
        PowerSysGraph.GetAllCells(parent);
        final mxCell[] allCellsArray = PowerSysGraph.allCells.toArray(new mxCell[PowerSysGraph.allCells.size()]);
        final int countItems = allCellsArray.length;
        final int[][] adjacencyMatrix = new int[countItems][countItems];
        for (final mxCell selectedCell : allCellsArray) {
            if (selectedCell.getValue() != null) {
//                generator
                if (selectedCell.getValue().getClass() == Generator.class) {
                    final Generator gen = (Generator) selectedCell.getValue();
                    final Object[] allEdges = mxGraphModel.getEdges(expGraphModel, selectedCell);
                    for (int ii = 0; ii < allEdges.length; ++ii) {
                        if (((mxCell) allEdges[ii]).getValue() instanceof HorizontalLink) {
                            final HorizontalLink horizLkCOL = (HorizontalLink) ((mxCell) allEdges[ii]).getValue();
                            adjacencyMatrix[gen.getCount() - 1][horizLkCOL.getCount() - 1] = 1;
                            adjacencyMatrix[horizLkCOL.getCount() - 1][gen.getCount() - 1] = 1;
                        } else if (((mxCell) allEdges[ii]).getValue() instanceof Load) {
                            final Load ld = (Load) ((mxCell) allEdges[ii]).getValue();
                            adjacencyMatrix[gen.getCount() - 1][ld.getCount() - 1] = 1;
                            adjacencyMatrix[ld.getCount() - 1][gen.getCount() - 1] = 1;
                        }
                    }
                }
//                load
                else if (selectedCell.getValue().getClass() == Load.class) {
                    final Load ld = (Load) selectedCell.getValue();
                    final Object[] allEdges = mxGraphModel.getEdges(expGraphModel, selectedCell);
                    for (int ii = 0; ii < allEdges.length; ++ii) {
                        if (((mxCell) allEdges[ii]).getValue() instanceof HorizontalLink) {
                            final HorizontalLink horizLkCOL = (HorizontalLink) ((mxCell) allEdges[ii]).getValue();
                            adjacencyMatrix[ld.getCount() - 1][horizLkCOL.getCount() - 1] = 1;
                            adjacencyMatrix[horizLkCOL.getCount() - 1][ld.getCount() - 1] = 1;
                        } else if (((mxCell) allEdges[ii]).getValue() instanceof Generator) {
                            final Generator gen = (Generator) ((mxCell) allEdges[ii]).getValue();
                            adjacencyMatrix[ld.getCount() - 1][gen.getCount() - 1] = 1;
                            adjacencyMatrix[gen.getCount() - 1][ld.getCount() - 1] = 1;
                        }
                    }
                }
//                junction
                else if (selectedCell.getValue().getClass() == Junction.class) {
                    ++countJunctions;
                    final Object[] allEdges = mxGraphModel.getEdges(expGraphModel, selectedCell);
                    for (int ii = 0; ii < allEdges.length; ++ii) {
                        int indexROW;
                        if (((mxCell) allEdges[ii]).getValue() instanceof Generator) {
                            indexROW = ((Generator) ((mxCell) allEdges[ii]).getValue()).getCount();
                        } else if (((mxCell) allEdges[ii]).getValue() instanceof Load) {
                            indexROW = ((Load) ((mxCell) allEdges[ii]).getValue()).getCount();
                        } else {
                            indexROW = ((HorizontalLink) ((mxCell) allEdges[ii]).getValue()).getCount();
                        }
                        for (int jj = 0; jj < allEdges.length; ++jj) {
                            if (ii != jj) {
                                int indexCOL;
                                if (((mxCell) allEdges[jj]).getValue() instanceof Generator) {
                                    indexCOL = ((Generator) ((mxCell) allEdges[jj]).getValue()).getCount();
                                } else if (((mxCell) allEdges[jj]).getValue() instanceof Load) {
                                    indexCOL = ((Load) ((mxCell) allEdges[jj]).getValue()).getCount();
                                } else {
                                    indexCOL = ((HorizontalLink) ((mxCell) allEdges[jj]).getValue()).getCount();
                                }
                                adjacencyMatrix[indexROW - 1][indexCOL - 1] = 1;
                            }
                        }
                    }
                }
            }
        }
        if (countJunctions > 0) {
            final int[][] result = new int[countItems - countJunctions][countItems - countJunctions];
            for (int ii = 0; ii < result.length; ++ii) {
                for (int jj = 0; jj < result.length; ++jj) {
                    result[ii][jj] = adjacencyMatrix[ii][jj];
                }
            }
            return result;
        }
        return adjacencyMatrix;
    }

    public static ArrayList<Integer> getPowers() {
        return powers;
    }

    public static void setPowers(ArrayList<Integer> powers) {
        DrawNetworkClean.powers = powers;
    }

    public static ArrayList<Integer> getCapacitances() {
        return capacitances;
    }

    //    this method calculate the #of different elements
//    create a popup to put weight into elements
    public static void AddPowerInputs(final mxGraph graph, ArrayList<Integer> list) {
        int[] counts = countElements(graph);
        int countGen = counts[0];
        int countLd = counts[1];
        int totalCounts = counts[0] + counts[1];
        int pos = getSub();
        setSub(0);
//        if (pos > 0){
//            pos = pos-1;
////        remove the unwanted components from list
//            if (list != null) {
//                for (int i = list.size() - 1; i >= countGen; i--) {
//                    if (i != pos)
//                        list.remove(i);
//                }
//            }
//        }

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);

        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);

        ArrayList<JFormattedTextField> textFields = new ArrayList<>();
        for (int i = 0; i < totalCounts; i++) {
//            when there is pre-power values
            if (list != null && list.size() == totalCounts) {
                textFields.add(new JFormattedTextField(formatter));
                if (i < countGen){
                    textFields.get(i).setValue(list.get(i));
                }
                else if (i < countGen + countLd){
                    textFields.get(i).setValue((list.get(i))*(-1));
                }

            } else {
                if (i < countGen) {
//                    generator
                    textFields.add(new JFormattedTextField(formatter));
                    textFields.get(i).setValue(1);
                } else if (i < countGen + countLd) {
//                    load
                    textFields.add(new JFormattedTextField(formatter));
                    textFields.get(i).setValue(1);
                }
            }
        }

        Object[] messages = new Object[totalCounts];
        for (int i = 0; i < totalCounts; i++) {
            int j = i + 1;
            if (i < countGen) {
                messages[i] = new Object[]{"Power through Generator Link " + j + " (kW):", textFields.get(i)};
                textFields.get(i).setToolTipText("Please put number greater than 1(Default is 1kW)");
            } else if (i < countGen + countLd) {
                messages[i] = new Object[]{"Power through Load Link " + j + " (kW):", textFields.get(i)};
                textFields.get(i).setToolTipText("Please put number greater than 1(Default is 1kW)"
                        + " and make sure the total of generator's capacity is Load's capacity");
            }
        }
        String[] options = new String[]{"OK",
                "INFO",
                "Cancel"};

        JOptionPane jOptionPane = new JOptionPane();
        jOptionPane.setIcon(null);
        jOptionPane.setOptions(new String[]{"REFRESH"});
        jOptionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
        jOptionPane.setMessage(messages);
        jOptionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);

//        int option = JOptionPane.showOptionDialog(null, messages, "Enter Input for system elements",
//                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        JScrollPane scrollPane = new JScrollPane(jOptionPane);
        scrollPane.setPreferredSize(new Dimension(500, 500));

        int option = JOptionPane.showOptionDialog(null, scrollPane,
                "Enter Inputs for Power elements",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        powers = new ArrayList<>();
        Object obj = jOptionPane.getValue();
//        if (Objects.equals((String) obj, "REFRESH")){
//            AddPowerInputs(graph, powers);
//        }

        if (option == OK) {
//            get the values from JTextField
            int totalGeneratorCapacity = 0;
            int totalLoadCapacity = 0;
            //int totalLinkCapacity = 0;
            for (int i = 0; i < totalCounts; i++) {
                String s = textFields.get(i).getText().trim();
                if (s.equals(""))
                    s = "1";
                int value = Integer.parseInt(s);
                if (i < countGen) {
                    totalGeneratorCapacity += value;
                    powers.add(value);
                }
                else if (i < countGen + countLd) {
                    totalLoadCapacity += value;
                    powers.add(value*(-1));
                }
            }
//            errors
//            if (totalGeneratorCapacity != totalLoadCapacity) {
//                JOptionPane.showMessageDialog(null,
//                        "Generators' power input has to be equal to Loads' power input.",
//                        "Power Input error",
//                        JOptionPane.ERROR_MESSAGE);
//                AddPowerInputs(graph, powers);
//            }
            System.out.println(powers);
            ExportAdjacencyMatrix(graph, "Adjacency_Matrix_with_power_inputs.csv",
                    true, powers, counts[2]);
        }
//        info option
        else if (option == INFO) {
            for (int i = 0; i < totalCounts; i++) {
                String s = textFields.get(i).getText().trim();
                if (s.equals(""))
                    s = "1";
                int value = Integer.parseInt(s);
                powers.add(value);
            }
            int n = JOptionPane.showConfirmDialog(null,
                    "Initially all Power Inputs are 1kw. " +
                            "If necessary a user can change the values to integer values greater than 1\n",
                    "Power Input's Information", JOptionPane.OK_CANCEL_OPTION);
            if (n == JOptionPane.OK_OPTION)
                AddPowerInputs(graph, powers);
        }

        else if (option == CANCEL){
            for (int i = 0; i < totalCounts; i++) {
                String s = textFields.get(i).getText().trim();
                if (s.equals(""))
                    s = "1";
                int value = Integer.parseInt(s);
                if (i < countGen) {
                    powers.add(value);
                }
                else if (i < countGen + countLd) {
                    powers.add(value*(-1));
                }
            }

        }
//        save option : save matrix with powers
//        else if (option == SAVE) {
//            ExportAdjacencyMatrix(graph, "Adjacency_Matrix_with_weight.csv",
//                    true, powers, counts[2]);
//
//            new EditorActions.ExportAction(false, "matrix", true, powers);
//        }

    }

    public static void AddPowerCapacitance(final mxGraph graph, ArrayList<Integer> list) {
        int[] counts = countElements(graph);
        int countGen = counts[0];
        int countLd = counts[1];
        int countHL = counts[2];
        int totalCounts = counts[0] + counts[1] + counts[2];

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(100);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);

        ArrayList<JFormattedTextField> textFields = new ArrayList<>();
        for (int i = 0; i < totalCounts; i++) {
            if (list != null && list.size() == totalCounts) {
                textFields.add(new JFormattedTextField(formatter));
                textFields.get(i).setValue(list.get(i));
            } else {
                textFields.add(new JFormattedTextField(formatter));
                textFields.get(i).setValue(100);
            }
        }

        Object[] messages = new Object[totalCounts];
        for (int i = 0; i < totalCounts; i++) {
            int j = i + 1;
            if (i < countGen) {
                messages[i] = new Object[]{"Input Capacitance in % for Generator Link " + j + ":", textFields.get(i)};
                textFields.get(i).setToolTipText("Please put number greater than 1(Default is 1kW)");
            } else if (i < countGen + countLd) {
                messages[i] = new Object[]{"Input Capacitance in % for Load Link " + j + ":", textFields.get(i)};
                textFields.get(i).setToolTipText("Please put number greater than 1(Default is 1kW)"
                        + " and make sure the total of generator's capacity is Load's capacity");
            } else {
                messages[i] = new Object[]{"Input Capacitance in % for Link" + j + ":", textFields.get(i)};
                textFields.get(i).setToolTipText("Please put number lower or equal than 100" +
                        "(Default is 100% means infinite capacity)");
            }
        }
        String[] options = new String[]{"OK",
                "INFO",
                "Cancel"};

        JOptionPane jOptionPane = new JOptionPane();
        jOptionPane.setIcon(null);
        jOptionPane.setOptions(new String[]{"REFRESH"});
        jOptionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
        jOptionPane.setMessage(messages);
        jOptionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);

//        int option = JOptionPane.showOptionDialog(null, messages, "Enter Input for system elements",
//                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        JScrollPane scrollPane = new JScrollPane(jOptionPane);
        scrollPane.setPreferredSize(new Dimension(500, 500));

        int option = JOptionPane.showOptionDialog(null, scrollPane,
                "Enter Input for Link capacitance",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        capacitances = new ArrayList<>();
        Object obj = jOptionPane.getValue();
//        if (Objects.equals((String) obj, "REFRESH")){
//            AddPowerInputs(graph, capacitances);
//        }

        if (option == OK) {
//            get the values from JTextField
            for (int i = 0; i < totalCounts; i++) {
                String s = textFields.get(i).getText().trim();
                if (s.equals(""))
                    s = "100";
                int value = Integer.parseInt(s);
                capacitances.add(value);
            }
//            errors
//            if (totalGeneratorCapacity!=totalLoadCapacity){
//                JOptionPane.showMessageDialog(null,
//                        "Generators' Input has to be equal to Loads' Input.",
//                        "Input error",
//                        JOptionPane.ERROR_MESSAGE);
//                AddPowerCapacitance(graph, capacitances);
//            }
            System.out.println(capacitances);
            ExportAdjacencyMatrix(graph, "Adjacency_Matrix_with_link_capacitance.csv",
                    true, capacitances, counts[2]);
        }
//        info option
        else if (option == INFO) {
            for (int i = 0; i < totalCounts; i++) {
                String s = textFields.get(i).getText().trim();
                if (s.equals(""))
                    s = "100";
                int value = Integer.parseInt(s);
                capacitances.add(value);
            }
            int n = JOptionPane.showConfirmDialog(null,
                    "Initially all Inputs are 100%." +
                            " If necessary a user can change this vlaues to positive integer values greater than 0 and less than 100.\n"
                            + " Link capacity 100% means infinite capacity.\n",
                    "Information", JOptionPane.OK_CANCEL_OPTION);
            if (n == JOptionPane.OK_OPTION)
                AddPowerCapacitance(graph, capacitances);
        }
    }

    public void AddVertices(final String cellID, final mxGraphModel graphModel) {
        final Object[] edges = mxGraphModel.getEdges(graphModel, graphModel.getCell(cellID));
        for (int ii = 0; ii < edges.length; ++ii) {
            mxICell targetCell = ((mxCell) edges[ii]).getTarget();
            if (targetCell.getId() == cellID) {
                targetCell = ((mxCell) edges[ii]).getSource();
            }
            if (targetCell.getValue().getClass() == Generator.class || targetCell.getValue().getClass() == Load.class) {
                DrawNetworkClean.connVertices.add(targetCell);
                System.out.println("Item added");
            } else if (targetCell.getValue().getClass() == Junction.class) {
                System.out.println("Recursive Call");
                this.AddVertices(targetCell.getId(), ((mxCell) edges[ii]).getId(), graphModel);
            }
        }
    }

    public void AddVertices(final String cellID, final String edgeID, final mxGraphModel graphModel) {
        final Object[] edges = mxGraphModel.getEdges(graphModel, graphModel.getCell(cellID));
        for (int ii = 0; ii < edges.length; ++ii) {
            if (((mxCell) edges[ii]).getId() != edgeID) {
                mxICell targetCell = ((mxCell) edges[ii]).getTarget();
                if (targetCell.getId() == cellID) {
                    targetCell = ((mxCell) edges[ii]).getSource();
                }
                if (targetCell.getValue().getClass() == Generator.class || targetCell.getValue().getClass() == Load.class) {
                    DrawNetworkClean.connVertices.add(targetCell);
                    System.out.println("Item added");
                } else if (targetCell.getValue().getClass() == Junction.class) {
                    System.out.println("Recursive Call");
                    this.AddVertices(targetCell.getId(), ((mxCell) edges[ii]).getId(), graphModel);
                }
            } else {
                System.out.println("Detected Edge to Parent");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Object[] AddCellsAndCountConnections(final mxCell cell, final mxGraphModel graphModel) {
        int countConnEdges = 0;
        final ArrayList<mxCell> connCells = new ArrayList<mxCell>();
        if (cell.isVertex()) {
            final Object[] edges = mxGraphModel.getEdges(graphModel, cell);
            countConnEdges = edges.length;
            for (int ii = 0; ii < edges.length; ++ii) {
                connCells.add((mxCell) edges[ii]);
            }
        } else if (!cell.isVertex() && cell.getValue() instanceof HorizontalLink) {
            mxCell tempCell = (mxCell) cell.getSource();
            if (tempCell.getValue() instanceof Junction) {
                final Object[] tempObject = AddEdgesOfJunction(cell, tempCell, graphModel);
                connCells.addAll(0, (Collection<? extends mxCell>) tempObject[1]);
                countConnEdges += (int) tempObject[0];
            } else {
                connCells.add(tempCell);
                ++countConnEdges;
            }
            tempCell = (mxCell) cell.getTarget();
            if (tempCell.getValue() instanceof Junction) {
                final Object[] tempObject = AddEdgesOfJunction(cell, tempCell, graphModel);
                connCells.addAll(0, (Collection<? extends mxCell>) tempObject[1]);
                countConnEdges += (int) tempObject[0];
            } else {
                connCells.add(tempCell);
                ++countConnEdges;
            }
        } else if (!cell.isVertex() && cell.getValue() instanceof Generator) {
            final mxCell tempCell = (mxCell) cell.getTarget();
            if (tempCell.getValue() instanceof Junction) {
                final Object[] tempObject = AddEdgesOfJunction(cell, tempCell, graphModel);
                connCells.addAll(0, (Collection<? extends mxCell>) tempObject[1]);
                countConnEdges = (int) tempObject[0];
            } else if (tempCell.getValue() instanceof Load) {
                connCells.add(tempCell);
                countConnEdges = 1;
            }
        } else if (!cell.isVertex() && cell.getValue() instanceof Load) {
            final mxCell tempCell = (mxCell) cell.getSource();
            if (tempCell.getValue() instanceof Junction) {
                final Object[] tempObject = AddEdgesOfJunction(cell, tempCell, graphModel);
                connCells.addAll(0, (Collection<? extends mxCell>) tempObject[1]);
                countConnEdges = (int) tempObject[0];
            } else if (tempCell.getValue() instanceof Generator) {
                connCells.add(tempCell);
                countConnEdges = 1;
            }
        }
        return new Object[]{countConnEdges, connCells};
    }

    public static int[] ConvertCellToNumber(final ArrayList<mxCell> myCells) {
        final int[] intArray = new int[myCells.size()];
        final mxCell[] arrayCell = new mxCell[myCells.size()];
        System.arraycopy(myCells.toArray(), 0, arrayCell, 0, myCells.size());
        for (int ii = 0; ii < arrayCell.length; ++ii) {
            if (arrayCell[ii].getValue() instanceof Generator) {
                intArray[ii] = ((Generator) arrayCell[ii].getValue()).getCount();
            } else if (arrayCell[ii].getValue() instanceof Load) {
                intArray[ii] = ((Load) arrayCell[ii].getValue()).getCount();
            } else if (arrayCell[ii].getValue() instanceof HorizontalLink) {
                intArray[ii] = ((HorizontalLink) arrayCell[ii].getValue()).getCount();
            }
        }
        return intArray;
    }

    public static Object[] AddEdgesOfJunction(final mxCell origEdge, final mxCell junc, final mxGraphModel graphModel) {
        final ArrayList<mxCell> cells = new ArrayList<mxCell>();
        int countConnEdges = 0;
        final Object[] allEdges = mxGraphModel.getEdges(graphModel, junc);
        countConnEdges = allEdges.length - 1;
        for (int ii = 0; ii < allEdges.length; ++ii) {
            if (((mxCell) allEdges[ii]).getId() != origEdge.getId()) {
                cells.add((mxCell) allEdges[ii]);
            }
        }
        return new Object[]{countConnEdges, cells};
    }

    public String GetCellIDofElements(final int number, final mxGraphModel graphModel) {
        final Map<String, Object> allCells = graphModel.getCells();
        Object obj = new Object();
        String cellID = "";
        for (final Map.Entry<String, Object> element : allCells.entrySet()) {
//            obj = element.getValue().getValue();
//            cellID = element.getValue().getId();
            obj = element.getValue();  // JDCF
            cellID = element.getKey();
            if (obj.getClass() == Generator.class) {
                final Generator gen = (Generator) obj;
                if (gen.getCount() == number) {
                    break;
                }
                continue;
            } else {
                if (obj.getClass() != Load.class) {
                    continue;
                }
                final Load ld = (Load) obj;
                if (ld.getCount() == number) {
                    break;
                }
                continue;
            }
        }
        return cellID;
    }

    public static void WriteToFile(final int[][] adjacencyMatrix, final String fileName) throws IOException {
        final File file = new File(fileName);
        final FileOutputStream fileStream = new FileOutputStream(file, false);
        final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(fileStream));
        try {
            final ExcelCSVPrinter ecsvp = new ExcelCSVPrinter(output);
            for (int ii = 0; ii < adjacencyMatrix.length; ++ii) {
                for (int jj = 0; jj < adjacencyMatrix[ii].length; ++jj) {
                    ecsvp.print(Integer.toString(adjacencyMatrix[ii][jj]));
                }
                ecsvp.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (output != null) {
                output.close();
            }
        }
        if (output != null) {
            output.close();
        }
    }

    public static void WriteToFile(final ArrayList<ArrayList<Integer>> listArrayList, final String fileName) throws IOException {
        final File file = new File(fileName);
        final FileOutputStream fileStream = new FileOutputStream(file, false);
        final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(fileStream));
        try {
            final ExcelCSVPrinter ecsvp = new ExcelCSVPrinter(output);
            for (int ii = 0; ii < listArrayList.size(); ++ii) {
                ArrayList<Integer> lists = listArrayList.get(ii);
                for (int jj = 0; jj < lists.size(); ++jj) {
                    ecsvp.print(Integer.toString(lists.get(jj)));
                }
                ecsvp.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (output != null) {
                output.close();
            }
        }
        if (output != null) {
            output.close();
        }
    }

    public static void WriteToFile(final ArrayList<ArrayList<String>> listArrayList, final String fileName, int i) throws IOException {
        final File file = new File(fileName);
        final FileOutputStream fileStream = new FileOutputStream(file, false);
        final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(fileStream));
        try {
            final ExcelCSVPrinter ecsvp = new ExcelCSVPrinter(output);
            for (int ii = 0; ii < listArrayList.size(); ++ii) {
                ArrayList<String> lists = listArrayList.get(ii);
                for (int jj = 0; jj < lists.size(); ++jj) {
                    ecsvp.print(lists.get(jj));
                }
                ecsvp.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (output != null) {
                output.close();
            }
        }
        if (output != null) {
            output.close();
        }
    }

    //    this method is to calculate adjacency matrix(AM) in new way
//    where in the diagonals we put value according to it's type
//    for generator = 1, load = -1 and horizontal link = 0
//    we also make horizontal links connected
    public static int[][] changeDiagonals(int[][] oldAdjacencyMatrix, int countGen, int countLd, int countHL) {
        final int rows = oldAdjacencyMatrix.length; // calculate the rows of the previous AM
        final int cols = oldAdjacencyMatrix[0].length; // calculate the columns of previous AM
        //        check whether it's empty or not
        if (rows == 0 || cols == 0) {
            System.err.println("Adjacency matrix is null");
            return oldAdjacencyMatrix;
        } else if (rows != cols) {
            System.err.println("Adjacency matrix's size mis-match");
            return oldAdjacencyMatrix;
        } else {
            if (powers == null) {
                for (int i = 0; i < rows; i++) {
                    int j = i + 1;
                    //  change the diagonal value
                    if (j <= countGen) oldAdjacencyMatrix[i][i] = 1;
                    else if (j <= countGen + countLd) oldAdjacencyMatrix[i][i] = -1;
                    else oldAdjacencyMatrix[i][i] = 0;
                }
            }
            else{
                for (int i = 0; i < rows; i++) {
                    int j = i + 1;
                    //  change the diagonal value
                    if (j <= countGen + countLd) oldAdjacencyMatrix[i][i] = powers.get(i);
                    else oldAdjacencyMatrix[i][i] = 0;
                }
            }

            return oldAdjacencyMatrix;
        }
    }
}

class NumberFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        } else {
            // warn the user and don't allow the insert
        }
    }

    private boolean test(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            // warn the user and don't allow the insert
        }

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (test(sb.toString())) {
            super.remove(fb, offset, length);
        } else {
            // warn the user and don't allow the insert
        }

    }
}