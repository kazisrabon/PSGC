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

import java.util.ArrayList;

public class TestFunction {
    public static void main(String[] args) {
        int rows = 5;
        int columns = 6;
        int[][] sourcearr = {
                {9, 2, 3, 6, 7, 8},
                {4, 7, 3, 5, 1, 9},
                {9, 2, 3, 6, 7, 8},
                {4, 7, 3, 5, 1, 9},
                {9, 2, 3, 6, 7, 8},
        };
        int[][] destinationarr = new int[rows - 2][columns - 2];

        int REMOVE_ROW_1 = 2; // 2 is 3rd row
        int REMOVE_ROW_2 = 3;
//        int REMOVE_COLUMN = 3;
        ArrayList<Integer> REMOVE = new ArrayList<>();
        REMOVE.add(2);
        REMOVE.add(3);
        int p = 0;
        for( int i = 0; i < rows; ++i)
        {
            if (REMOVE.contains(i))
                continue;


            int q = 0;
            for( int j = 0; j < columns; ++j)
            {
                if ( REMOVE.contains(j))
                    continue;

                destinationarr[p][q] = sourcearr[i][j];
                ++q;
            }

            ++p;
        }
        System.out.println(destinationarr);
    }
}
