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

#include <vector>
#ifndef SURVIVABILITY_H_ 
#define SURVIVABILITY_H_
// Stores format of input (adjacency list or matrix)
char inputFormat;

// nvt: Number of source links
// nvb: Number of sink links
// nh: Number of horizontal links
// nm: Total number of nodes
int nvt;
int nvb;
int nh;
int nm;

// Y: Map of the graph describes the connection between
// different elements. Y(i,j)=1 means elements i,j are
// connected, and Y(i,j)=0 means they are not.
int* Y;
int* YC;
int* YB;

// P: P(i)=1 if i-th element is available, P(i)=0 if unavailable.
char* P;

// nt: Total number of combinations of available/unavailable elements.
// m: Number of damaged (unavailable) elements.
// etot: Power produced initially by all available generators.
// eact: Power available to the rest of the IPS after damage.
// nc: No-connection switch
int nt;
int m;
int etot;
int eact;
char nc;

// dec2bin variables
int v;
int sw;

unsigned long* TotConfigs;
unsigned long* IsPath;

// F(m): Number of combinations (scenarios) leading to eact=0
// S(m): Number of combinations (scenarios) leading to eact=etot
// R(m): Number of combinations (scenarios) leading to eact<etot
// N(m): Total number of combinations (scenarios) at a given m (1 runs from 1 to nm)
int* F;
int* S;
int* R;
int* N;

// Meaning of these not immediately obvious
int in;
int l;
int snm;
char* O;
char* C;

// Probability variables.
// ps: Probability Stable, no need for reconfiguration.
// pr: Probability Reconfigure, survives with reconfiguration.
// pf: Probability Failure, cannot survive, even with reconfiguration.
double ps,pr,pf,p1,p2,p3,pt;

// Prints usage info and exits program.
void usageErr(); 

// To read input stream
FILE* inputStream;
char ch;

// Looping integers
int i;
int j;
int ja;
int jb;
int jj;
int k;
int ka;
int kb;

// Reads contents of input file.
int readInput(char* inputFile);

// Main function for performing the survivability algorithm
int survivability();

// dijk: Implementation of Dijkstra algorithm that returns if a path exists
// srcs: Array of indices corresponding to our generators
int dijk(std::vector<std::vector<int>> Z, int cur_sink);
int* srcs;

// Output streams
FILE* scenarios;
FILE* probabilities;

#endif 
