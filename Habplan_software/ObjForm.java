
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import figure.*;

/**
*Extend this to make an Objective Function Component 
*@author Paul Van Deusen (NCASI)  12/97, modified numerous times
*/
public class ObjForm extends JFrame {
 private static int nextId=1;
    static String fileName; //what file is currently being read
    protected String title=""; //a title for graphs and tool tips
    protected JLabelTextField p1; //holds the data file name
    protected static boolean isWt2big=false; //indicates that wts are too big
 static double minValue=1.0E-6;  //dont let weight parameters go below this
 public  int idNum;  //provide an id for each instance
 protected Figure graph=null;
 protected boolean dataRead=false; //indicates if the data were read yet
 protected boolean isColorsLoaded=false; //indicates if user graph settings were loaded
 protected int nSubComponents=0; //number of subComponents (for Flows mainly)
 int converged=0;  //0=not converged 1=converged 2=overConverged
 protected double goalValue=0; //the attained goal (0 to 1), used in fitness function
 static FrameMenu parent;
 static Hashtable polyTable;
 static Hashtable regimeTable;
    //used when loading a previous run in FrameMenu.readSettings
 static Vector oldSched; //holds an oldSchedule as polyName, RegimeName
 static boolean[] isInComponent;// true if component is in for oldSched
 static Vector polyArray=null;
 static Vector regimeArray=null;
   
 static int[][] unitIndex={{1},{1}};//row i contains member ids of unit i
 
 static int nPolys=0, nOptions=0;
  protected static int[] polySched=null;  //store the current schedule
  static int[] validScheds; //the valid schedules for 1 polygon
  static int nScheds; //number of valid schedules for 1 polygon  
 protected static int nextRegime=1;  //used to index the management regimes
 protected static int nextPolygon=1; //index of polygons
 protected int nextPolygonSave=1; //use to restore nextPolygon if read fails
 int polyFirst, polyLast;  //the first and last indexes of polygons owned by this component
 int regimeLast; //index of last regime known by any component when this component was added
  protected  double weight=1.0;  //component weight.  A flowform uses 2 different weights.
 


public ObjForm(String s, int rows, int cols){
  super(s);
  
  idNum=nextId++;  //provide a unique id for each instance
  getContentPane().setLayout(new GridLayout(rows,cols,0,0));
  int xLoc=300+(idNum%8)*20; //so Forms cascade 
  int yLoc=400-(idNum%8)*20;
  setBounds(xLoc,yLoc,300,400);
  polyTable=new Hashtable(10);  //this gets remade when 1st component added
  regimeTable=new Hashtable(20);
  regimeArray=new Vector(20);
  regimeArray.addElement("0");
  //get 0.0 in position 0 because Biol-1 uses 0 to mean a trainer
     
      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

   

/**
*keep track of problem size changes when user adds objective function components
*@param newPolys is number of polygons
*@param newOptions is number of options=n of regimes
*/
protected static  void problemSize(int newPolys, int newOptions){
   if (nPolys!=0 && polySched.length<=newPolys){
    int[] temp=polySched;//point to current schedule;
    polySched=new int[newPolys+1]; //create a bigger array
    for (int i=1;i<temp.length;i++)polySched[i]=temp[i]; //copy the old schedule
   } else if(polySched==null)
      polySched=new int[newPolys+1]; //create for 1st time
    nPolys=newPolys;  
    nOptions=newOptions; //just in case n of Options has changed

}/*end method*/

/**
*set the weight variable and the form setting
*/    
 public void setWeight(double[] wt){
     String fname=this.getTitle();
     System.out.println("OBJFORM setWeight() was called by "+fname);
 }

    /**
     *get the weight variable value(s)
     *override for multi-weight components
     */
    public double[] getWeight(){
	double[] wt = {weight} ;
	return(wt);
     }

    /**
     *scale the weight(s) by dividing by the supplied value
     *to be consistent. Override this for multi-weight components
     *like Flows
     */
    public void scaleWeight(double factor){
	weight=weight/factor;
    }/*end method*/

/**
*clear all settings.  Done when user wants to load a new problem without quitting
*/    
 protected void clearSettings(){ }

 /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){}

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){}
 
/*
*converged variable lets you know the status of goal attainment
*/  
public int isConverged(){return converged;}

public static int getNextId(){ return(nextId); }

/**tell parent component that you're being taken out of the
*objective function,  must be overridden to decrement 
*nSubComponents
*/
protected void notifyParentOfRemoval(){ int i=1;}

/**
*controls whether display form is shrunk
*/
public void shrink(boolean shrunk){}

/**
*indicates whether a component has a Graph associated with it
*/
public boolean isGraph(){ return(false);}

public int readSettings(LineNumberReader saveInput){
  //place holder for a file reader method
  System.out.println("you called Objform.writesettings()");
  return(0);
}


public void writeSettings(PrintWriter fout){
  //place holder for a file writer method
  System.out.println("you called Objform.writesettings()");
}

/**
*make sure this form is filled out
*/
public boolean verify(){
  
  return(false);
    
}/*end verify()*/

/**
*Check to see if the file is under the example directory
*if not check elsewhere
*@return the file path and name
*/
    public static String findFile(String fname, String dummy){
  String sep=System.getProperty("file.separator");
  //first try to find in example dir with no changes to path
   File file1=new File(UserInput.ExampleDir + fname); 
 if (file1.exists()) return(UserInput.ExampleDir + fname); 
   //if that fails see if the path separator is wrong
  if (sep.equals("/")) 
	fname=fname.replace('\\','/'); //its unix
  else	fname=fname.replace('/','\\'); //its windows   
    file1=new File(UserInput.ExampleDir + fname); 
 if (file1.exists()) return(UserInput.ExampleDir + fname);
    //if that fails dont assume its in example dir 
    file1=new File(fname); 
 if (file1.exists()) return(fname);
   //now give up, you cant find the gosh-darn file.   
   //return the original fname and let it abort!
 return(fname);
}/*end findFile()*/

/**
*Check to see if the file is under the example directory
*if not check elsewhere. Pre-pend example dir if requested
*not matter what happens.  This is useful if you want to
*create the file in the example dir if it doesn't exist.
*OutputDialog uses this.
*@param isExample - if true, prePend example directory to path
*@return the file path and name
*/
    public static String findFile(String fname){
  String sep=System.getProperty("file.separator");
  String os=System.getProperty("os.name");
  boolean isWindows=false;
  boolean isExample=true; //this is an abbreviated path in example dir
  if(os.toLowerCase().contains("windows"))isWindows=true;
  if(isWindows && fname.contains(":"))isExample=false;
  if(!isWindows && fname.startsWith(sep))isExample=false;

  //first try to find in example dir with no changes to path
   File file1=new File(UserInput.ExampleDir + fname); 
 if (file1.exists()) return(UserInput.ExampleDir + fname); 
   //if that fails see if the path separator is wrong
  if (!isWindows) 
	fname=fname.replace('\\','/'); //its unix
  else	fname=fname.replace('/','\\'); //its windows   
    file1=new File(UserInput.ExampleDir + fname); 
 if (file1.exists()) return(UserInput.ExampleDir + fname);
    //if that fails dont assume its in example dir 
    file1=new File(fname); 
 if (file1.exists()) return(fname);
   //now give up, you cant find the gosh-darn file.   
   //return the original fname with example dir pre-pended
  if(isExample)return(UserInput.ExampleDir + fname);
  return(fname); //give up and return original name
}/*end findFile()*/




/**
*read the data
*/
public boolean readData(){
  return(false);
}/*end readData()*/

/**
*read the form
*/
public void readTheForm(){
  System.out.println("ObjForm.readTheForm() was called");
}/*end readData()*/

 /**
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
}/*end init()*/

/**
*Puts the valid schedules in FrameMenu vector validScheds
*and returns the new length of validScheds.   The default
*is to do nothing to validScheds and return nScheds unchanged
*@param poly is the polygon id
*@param nScheds is n of valid schedules
*/
public int getValidScheds(int poly, int nScheds){
return(nScheds);
}/*end isValidSched()*/



/**
*Compute Objective Function change when one polygon changes
*@param i is the index of the polygon, z is the new regime
*/
public double deltaObj(int[] members, int z){
return(0.0);
}/*end delta()*/

/**
*adjustments to be made during a metropolis iteration
*if a change is made
*/
public void duringIter(){
}/*end duringIter()*/

 /**
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){
}/*end postIter()*/


    /**
     *apply regime edits from file to component data in memory
     *the work is done by the RegimeEditor class
     *@param parent - FrameMenu so can access regimeEditor
     *@param fName - the file name of the original data
     *@param form - the calling Obj Component (Flow or Bio2)
     **/
    public void applyRegimeEdits(FrameMenu parent, String fName, ObjForm form){
	fName=fName+".edits";
	File file=new File(fName);
	if(!file.exists())return;
	String[][] edits; //the edit data goes here temporarily
	if(form.dataRead){
	    edits=parent.regimeEditor.readData.read(fName);//get the edits
	    parent.console.println("**EDITS IN: "+fName);
	    if(form instanceof FlowForm){
		parent.regimeEditor.applyFlowEdits(edits,(FlowForm)form);
	    }
	    else if(form instanceof Biol2Form){
		parent.regimeEditor.applyBio2Edits(edits,(Biol2Form)form);
	    }
	     else if(form instanceof BlockForm){
		 return;
	    }

	}/*fi dataRead*/
    }

} /*end ObjForm class*/
