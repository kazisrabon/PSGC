++==TODO: POWER SYSTEMS GRAPH CREATOR==++

==Java Code==

-Executable JAR file creation containing C++ executables
Currently, the PSGC can only execute the C++ survivability
analysis code if run within Eclipse. When creating an executable
JAR file and including the C++ executables within the JAR file,
the Runtime.exec commands in the RunAction method do not work.
Since asking every end user to download Eclipse and import
the project files for the sake of merely using the software is
not ideal, so figuring out how to execute the C++ code from a
JAR file is a priority.

-Diagram plots for results
A method, DiagramAction, in EditorActions.java is tied to the
diagram button next to the Run button on the toolbar. This
should be extended to show plots of success, reconfigure, fail
scenario probabilities. JFreeChart and XChart libraries are
included as they may prove useful in order to implement
this functionality, but may be safely removed if they are 
not necessary for this purpose.

-Dialog box while running survivability analysis
Survivability analysis does take time to run, and while running
from the PSGC, the PSGC can appear to hang when it is merely
busy running the survivability code. Something like a progress
bar or a spinning ball should be displayed to indicate to the 
user along with verbiage that the analysis is being performed
and the program has not frozen.

==C++ Code==

-Unification of C++ codes for Windows and Linux
Currently, the Windows and Linux codes for the survivability
analysis executable are split up due to an include that
Visual Studio requires for use of fscan and fopen functions
(#define _CRT_SECURE_NO_DEPRECATE), but this does not exist
when compiling with g++. The easiest ways to accomplish this
are either to use a more 'C++-like' approach to file reading,
or to use a different compiler to create the Windows executable,
perhaps such as the Eclipse C++ IDE, or MinGW-w64.

-Parallelism
The algorithm currently is sequential, and even a naive approach
to parallelization with OpenMP pragmas is likely to be an easy
task that yields speedup. OpenMP and MPI parallelism is necessary
to run analysis on topologies of any significant size.
