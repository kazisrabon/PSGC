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

import javax.swing.*;
import javax.swing.border.Border;
import java.beans.*; //Property change stuff
import java.awt.*;
import java.awt.event.*;

public class Test extends JPanel implements ActionListener{
        JButton go;

        JFileChooser chooser;
        String choosertitle;

  public Test() {
            go = new JButton("Do it");
            go.addActionListener(this);
            add(go);
        }

        public void actionPerformed(ActionEvent e) {
            chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle(choosertitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //
            // disable the "All files" option.
            //
            chooser.setAcceptAllFileFilterUsed(false);
            //
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                System.out.println("getCurrentDirectory(): "
                        +  chooser.getCurrentDirectory());
                System.out.println("getSelectedFile() : "
                        +  chooser.getSelectedFile());
            }
            else {
                System.out.println("No Selection ");
            }
        }

        public Dimension getPreferredSize(){
            return new Dimension(200, 200);
        }

        public static void main(String s[]) {
            JFrame frame = new JFrame("");
            Test panel = new Test();
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            System.exit(0);
                        }
                    }
            );
            frame.getContentPane().add(panel,"Center");
            frame.setSize(panel.getPreferredSize());
            frame.setVisible(true);
        }
}