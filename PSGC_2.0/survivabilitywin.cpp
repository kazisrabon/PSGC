#define _CRT_SECURE_NO_DEPRECATE
#include <iostream>
#include <cctype>
#include <string>
#include <fstream>
#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <climits>
#include <vector>
#include "survivabilitywin.h"

/* 
   This code performs survivability analysis on power systems created
   in the Power Systems Graph Editor.

USAGE: ./survivability -(a/m) [filename]
where '-a' switch used to indicate the input file is an adjacency list,
and '-m' switch used to indicate the input file is a matrix. 
Examples:
./survivability -a AdjacencyList.csv 
./survivability -m Matrix.csv
*/

int inf = INT_MAX;

// Print correct usage and exit.
void usageErr()
{
  std::cout << "Incorrect usage.\n\nThis code performs survivability analysis on power systems created\nin the Power System Graph Editor.\n\nUSAGE: ./Survivability -(a/m) [filename]\nwhere '-a' switch used to indicate the input file is an adjacency list,\nand '-m' switch used to indicate the input file is a matrix.\nExamples:\n./Survivability -a AdjacencyList.csv\n./ Survivability -m Matrix.csv\n";
  exit(0);
}

// Reads in contents of input file into relevant arrays.
// char* inputFile: The name of the input file.
int readInput(char* inputFile)
{
  if (inputFormat == 'a')
  {
    inputStream = fopen(inputFile,"r");
    // Read in nvt, nvb, nh
    fscanf_s(inputStream,"%d",&nvt);
    fscanf_s(inputStream,"%d",&nvb);
    fscanf_s(inputStream,"%d",&nh);
    nm = nvt + nvb + nh;
    fgetc(inputStream);
    // Read the coordinate line to build Y
    YC = new int[nm*sizeof(int)];
    for (i=0;i<nm;i++)
    {
      fscanf_s(inputStream,"%d%*c",&YC[i]);
    }
    // Read connection data into Y from last line
    Y = new int[nm*nm*sizeof(int)];
    YB = new int[YC[nm-1]*sizeof(int)];
    j=0;
    for (i=0;i<YC[nm-1];i++)
    {
      fscanf_s(inputStream,"%d%*c",&YB[i]); 
    }
    *Y = nvt;
    srcs = new int[nvt*sizeof(int)];
    for (i=0;i<nvt;i++) srcs[i] = i;
    *(Y + nm + 1) = nvb;
    *(Y + 2*nm + 2) = nh;
    for (i=0;i<nm;i++)
    {
      while (j<YC[i])
      {
        *(Y + i*nm + (YB[j]-1)) = 1;
        j++;
      }
    }
  }

  // Handle a matrix adjacency list as input
  else if (inputFormat == 'm')
  {
    std::filebuf fb;
    if (fb.open(inputFile,std::ios::in))
    {
      std::string str;
      str = '!'; // To get into the loop below
      int k = 0;
      while (str.size()!=0) 
      {
        std::istream is(&fb);
        std::getline(is,str);
        int j = 0;

        // Figure out size of adjacency matrix
        if (k==0 && j==0)
        {
          for (int i = 0; i < str.length(); i++)
          {
            if (isdigit(str[i]))
            {
              j++;
            }
          }
          nm = j;
          Y = new int[nm*nm*sizeof(int)];
        }

        // Fill in the connection matrix Y
        j = 0;
        for (int i = 0; i < str.length(); i++)
        {
          if (isdigit(str[i]))
          {
            *(Y + k*nm + j) = str[i] - '0';
            j++;
          }
        }
        k++;
      }

      nvt = *Y;
      nvb = *(Y + nm + 1);
      nh = *(Y + 2*nm + 2);
      fb.close();
    }
  }
  return 0;
}

//Dijkstra algorithm
int dijk(std::vector<std::vector<int>> Z, int src)
{
  int* dist = new int[Z.size()*sizeof(int)]; // The output array.  dist[i] will hold the shortest

  bool* sptSet = new bool[Z.size()*sizeof(bool)]; // sptSet[i] will be true if vertex i is included in shortest
  // path tree or shortest distance from src to i is finalized

  // Initialize all distances as INFINITE and sptSet[] as false
  for (k = 0; k < Z.size(); k++)
  {
    dist[k] = inf;
    sptSet[k] = false;
  }
  // Distance of source vertex from itself is always 0
  dist[src] = 0;

  // Find shortest path for all vertices
  for (int count = 0; count < Z.size()-1; count++)
  {
    // Pick the minimum distance vertex from the set of vertices not
    // yet processed. u is always equal to src in the first iteration.
    // Initialize min value 
    int min = inf, min_index, u; 

    for (l = 0; l < Z.size(); l++) 
      if (sptSet[l] == false && dist[l] <= min) 
        min = dist[l], u = l; 

    // Mark the picked vertex as processed
    sptSet[u] = true;

    // Update dist value of the adjacent vertices of the picked vertex.
    for (int v = 0; v < Z.size(); v++)
    {
      // Update dist[v] only if is not in sptSet, there is an edge from
      // u to v, and total weight of path from src to  v through u is
      // smaller than current value of dist[v]
      if (!sptSet[v] && Z[u][v] && dist[u] != inf
          && dist[u]+Z[u][v] < dist[v])
        dist[v] = dist[u] + Z[u][v];
    }
    for (int v=0;v<nvt;v++)
    {
      if (dist[v] < inf)
        return 1;
    }
  }
  return 0;
}

// The main function for the survivability analysis algorithm.
int survivability()
{
  std::vector<std::vector <int>> B;
  std::vector<std::vector <int>> Z;

  std::ofstream scenarios;
  std::ofstream probabilities;

  // Open the output files
  scenarios.open("scenarios.out");
  probabilities.open("probabilities.out");

  probabilities << "m P(S) P(R) P(F)\n";
  scenarios << "m S R F\n";

  for (i=nvt;i<nvt+nvb;i++) 
  {
    // Annihilate all but one row/column corresponding to sink
    B.resize(nm,std::vector<int>(nm,0));
    // Number of deletions at current i
    int nd=0;
    for (j=0;j<nm;j++)
    {
      B[j].resize(nm);
    }
    for (j=0;j<nm;j++)
    {
      for (k=0;k<nm;k++)
      {
        B[j][k]=0;
        B[j][k] = *(Y + j*nm + k);
      }
    }
    for (j=nvt;j<nvt+nvb;j++)
    {
      if (j!=i)
      {
        B.erase(B.begin()+j-nd);
        std::for_each(B.begin(),B.end(),[&](std::vector<int>& row) {
            row.erase(std::next(row.begin(),j-nd));
            });
        nd++;
      }
    }

    // Initialize count arrays for total possible configurations, total paths
    TotConfigs = new unsigned long[(int)(B.size()+1)*sizeof(unsigned long)];
    IsPath = new unsigned long[(int)(B.size()+1)*sizeof(unsigned long)];
    for (j=0; j<=(int)B.size(); j++)
    {
      TotConfigs[j]=0;
      IsPath[j]=0;
    }

    // Perform decimal-to-binary count up to nt to represent all scenarios
    nt = (int)pow(2.,(double)B.size());
    for (j=0; j<nt; j++)
    {
      int Psum=0;
      P = new char[(int)B.size()*sizeof(char)];
      v = j;
      for (k=0; k<(int)B.size(); k++)
      {
        sw = v%2;
        if (sw==1) 
        {
          Psum++;
          P[k]=1;
        }
        else P[k]=0;
        v/=2;
      }

      if (P[i]==0) continue;
      TotConfigs[Psum]++;

      // Store 'damaged' B in Z
      Z.resize((int)B.size(),std::vector<int>((int)B.size(),0));
      for (k=0;k<(int)Z.size();k++)
      {
        for (l=0;l<(int)Z.size();l++)
        {
          Z[l][k]=0;
        }
      }
      for (k=0;k<(int)Z.size();k++)
      {
        for (l=k+1;l<(int)Z.size();l++)
        {
          Z[k][l]=B[k][l]*P[k]*P[l];
          Z[l][k]=Z[k][l];
        }
      }
      if(dijk(Z,i))
      {
        int Psum=0;
        for (k=0;k<(int)Z.size();k++) if (P[k]) Psum++;
        IsPath[Psum]++;
      }
    }
    for (j=(int)B.size()-1; j>=0; j--)
    {
        probabilities << (int)B.size()-1-j << ", " << (double)IsPath[j+1]/(double)TotConfigs[j+1] << ", 0, " << (1. -
                (double)IsPath[j+1]/(double)TotConfigs[j+1]) << "\n";
        scenarios << (int)B.size()-1-j  << ", " << IsPath[j+1] << ", 0, " << TotConfigs[j+1] - IsPath[j+1] << "\n"; 
        printf("%d: %d / %d  = %f\n", (int)B.size()-1-j, IsPath[j + 1], TotConfigs[j + 1], (double)IsPath[j + 1]/(double)TotConfigs[j + 1]);
    }
  }

  // Close the output files
  std::cout << "Closing the file scenarios.out\n";
  scenarios.close();

  std::cout << "Closing the file probabilities.out\n";
  probabilities.close();

  return 0;
}

int main(int argc, char* argv[]) 
{
  // Make sure the command-line convention is followed.
  if (argc != 3) usageErr(); 
  if (argv[1][0] == '-')
  {
    if (argv[1][1] == 'a') inputFormat = 'a';
    else if (argv[1][1] == 'm') inputFormat = 'm';
    else usageErr();
  }
  else usageErr();

  // Call function to read the input file.
  readInput(argv[2]);

  // Call survivability analysis function.
  survivability();

  return 0;
}
