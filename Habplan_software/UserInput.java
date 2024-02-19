/**
Written by Paul Van Deusen (NCASI) 9/97
*/

import java.util.*;
import java.io.*;
import java.awt.*;
/**
*
*This class holds user input settings of the variables that drive
*the HABPLAN harvest scheduler. There are methods for saving the
* current GUI settings
* reading the data needed to do harvest scheduling, and looking at
*the data you read.
*this will probably be the place for methods that allow the user
*to reconfigure habplan to offer different objective function
*components.  This is a legacy class that is hard to get rid of.
*Otherwise it just holds useful information.
*<p>
*@author Paul Van Deusen 11/97
*/

  class UserInput{
  static String HabplanDir;  //home directory for class files
  static String hbpDir;   //where .hbp files are
  static String projectDir; //where project xml  files are
  static String SoundDir;  //where the sounds are
  static String HelpDir; //help files directory
  static String ExampleDir; //example data directory
  static String TempDir; //holds some temp files
   static String LPDir;// where LP related files are
  static int maxOldJobs; //only allow this many oldjobs to show in fileMenu
  static String oldJobs; //file for saving old jobsettings
  static String saveFile="saveInput.hbp"; //settings for old-style savefiles
      static String projectFile="project.xml"; //new style save files
      static boolean isBuffered=true; //state of double buffering
      static boolean isCommandLine=false; //were there command line args
 //mainForm variables
 
  static String unitFile=null; //name of management unit file, full path
  static int iterate; //n of iterations
 
  // when creating a new kind of component 
  // edit FrameMenu.createComponents()
  // and also edit Habplan1.configurator()
  // then change properties.hbp 

  static int nFlow=1, nBioI=1, nBioII=1, nSMod=1;
  static int[] nCofF;
  static int[] nBKofF;
  //nComponents is computed in Habplan1.configurator()
   static int nComponents=6; //total number of components in the configuration
 


 
} /*end UserInput class*/
