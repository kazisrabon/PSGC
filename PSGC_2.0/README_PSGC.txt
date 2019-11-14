++====POWER SYSTEMS GRAPH CREATOR SETUP GUIDE FOR USERS AND DEVELOPERS====++

==Installation==

The Power System Graph Creator is currently functional when

opened as an Eclipse project. Installation instructions are
as follows:


1) Download Eclipse IDE for Java Developers 2019-03 R or newer from
https://www.eclipse.org/downloads/packages/


2) Extract the zipfile obtained from the Eclipse website and

run the 'eclipse' executable located in the directory to

which you extracted the zipfile.


3) Accept the default location for the Eclipse workspace directory.

Close the 'Welcome' tab and go to File -> Import Projects from

File System...


4) In the 'Import Projects from File System or Archive' wizard, click

on 'Directory...' located on the 'Import source:' row. Choose the

'Version 1.1' directory as the import source folder and click OK.


5) Click 'Finish' to import the project. Wait for the project to import

completely, as it may take a few minutes.


6) In order to run the Power System Graph Creator, highlight the 'PSD'

folder in the folder selection pane and click the green play button.

Under the 'Matching items:' list, select PowerSysGUI and click OK. The

Power System Graph Creator will then be launched.



==Using the Power System Graph Creator==


To begin drafting a power system, right-click anywhere on the dotted 
workspace area and select "Junction" to create a junction.

Right click on the junction and choose "Generator-VL" or "Load-VL" 
to fix a generator or load vertical link to a junction.

To create a horizontal link, click on a junction and drag. A junction 
will be automatically created on the other end of the horizontal link.

Adjacency-list and matrix representations of your created power system
can be exported and saved using the "Matrix Export" and "Adjacency-List 
Export" options under the File menu.


Your created power system may be saved in the form of a PNG image coupled 
with an XML file. Use the "Save" option under the File menu to save your power system.

In order to run survivability analysis, create or import a power system 
and click the white triangle (play) button on the toolbar, which is the fourth button from the left.

After the survivability analysis has run, you will be prompted with Save
dialogs to choose a location to save the results in probability and scenario totals.



==For Developers==


In order to make changes to the C++ code it is necessary to edit and compile both the "survivabilitywin.cpp"
and "survivabilitylinux.cpp" files separately. 
Refer to "TODO.txt" for an explanation for why this is.


As of this writing, the Linux executable was created using g++ and the Windows executable was created

using Visual Studio 2019. 

A Makefile is included to compile the Linux code. Assuming you have make and g++ installed, simply type 'make'

to compile the Linux code. 

In order to compile the Windows code, download and install Visual Studio 2019 and follow the instructions

found here: https://docs.microsoft.com/en-us/cpp/windows/walkthrough-creating-a-standard-cpp-program-cpp?view=vs-2019

The 'survivabilitywin.cpp' and 'survivabilitywin.h' files can be dragged-and-dropped into Visual Studio. Create a C++ project as
described in the link above, and drag and drop 'survivabilitywin.h' and 'survivabilitywin.cpp' into the 'Header Files' and 'Source Files'
folders in Solution Explorer dialog respectively.

Currently, it is necessary to include _CRT_SECURE_NO_WARNINGS in the preprocessor definitions. See the top-rated answer on this Stackoverflow
thread for instructions on how to do this:
https://stackoverflow.com/questions/22450423/how-to-use-crt-secure-no-warnings

After doing this, press the green play button on the toolbar to build. The executable can be found in `User`\source\repos\`Project Name`\Debug.