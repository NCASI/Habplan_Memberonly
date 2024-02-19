
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.xml.sax.Attributes;
import java.beans.*;

/**
*Make a Frame with menubar
*@author Paul Van Deusen (NCASI)  12/97
*/
public class FrameMenu extends JFrame {
    int iter=0; //put iter into Algorithm class
  Version version;     // lets user see the version
  License license;     //shows license information
  HoldState holdState; //for saving state, initialized in createComponents()
  SetState setState; //for manipulating holdState
  RemoteControlTable remoteControlTable; //for remote control
  BestSchedule bestSchedule;
    LpBuild lpBuild;  //for creating MPS files for an LP run
  GISViewer gisViewer;
    RegimeEditor regimeEditor;
  protected String passWord="bogus";  //holds problem size password
  protected String limit0="bigForester"; //no limit
  protected String limit1="timberCruiser"; //1050 polygon limit
  protected String limit2="paintGun"; //2000 polygon limit
  protected String limit3="cruiseVest"; //3000 polygon limit
    public String descriptiveData="";//used to hold some info about the problem
  OutputDialog outputDialog;  //lets user control output
  ImportDialog importDialog; //lets user import saved schedule
  ProjectDialog projectDialog; //lets users read/write a project text file
  ConfigDialog configDialog; //lets user change the configeration
  NewConfigDialog newConfigDialog; //gives user option to reconfigure for new dataset
  JDataViewer console;  //use this as the Habplan console
    UnitForm unitForm;//entry form for Unit data info
    DataReadThread dataThread=null;
  static String nrowInFile, configInFile; //the configuration values stored with the saved oldjobs
  static int nOldJobs=0;  //actual number of oldjobs read from file up to max
  static int maxOldJobs=UserInput.maxOldJobs; //only allow this many oldjobs to show in fileMenu
  static String[] oldJobs=new String[maxOldJobs+1];  //holds filenames of old jobs to put in fileMenu
  //n of Polygons and regimes (options)
  static int nPolys=0, nOptions=0;
 
  // when creating a component edit createComponents()
  protected static int nComponents=UserInput.nComponents;  //n of components included
  //list the names of ALL available components
  static String[] componentName=new String[nComponents+1];
 
    
    JCheckBox unitBox; //checkbox to indicate management units are enforced
    CheckListener checkListener;
  JLabelTextField p2; 
  JLabel labelcomp, outLabel;  //says add components
  
    boolean isReInit=false; //true means its in reInitialize() mode
  boolean isImport=false; //remember if using an imported schedule
  boolean isImportRun=false; //user imported from importDialog
    boolean isSaveImport=false; //indicates if user wants to save as an import
  boolean shrunk=true;  //remember if shrunk
  boolean[] isAdded = new boolean[nComponents+1]; //keep trach of components added to obj function
 
  FrameMenu myFrame; //parent frame for dialogs
  static ObjForm[] form = new ObjForm[nComponents+1];
  JCheckBox[] compBox = new JCheckBox[nComponents+1]; //check to add component
  JPanel lp1;  //button panel, checkboxes and outlabel
  JPanel bPanel, checkPanel;  //button panel, checkboxes and outlabel
  JButton start, suspend, stop; //thread control buttons
  
  Metropolis met=null;  // thread for running metropolis algorithm
  boolean runRunning=false, //keep track of thread status;
	  runSuspend=true, //keep track of suspend requests;
	  runStop=true,    //keep track of met Stop requests;
	  runHistory=false; //indicates that a run was made
  
  protected JMenuBar mbar;
  protected  JMenu fileMenu, editMenu, graphMenu, optionsMenu, toolMenu, helpMenu;
  protected  JMenuItem[] oldItem=new JMenuItem[maxOldJobs+1];  //for adding old user jobs
  protected JMenuItem[] editItem=new JMenuItem[nComponents+1];
  protected JMenuItem[] graphItem=new JMenuItem[nComponents+1];
    protected JMenuItem unitItem,titleItem,initItem;
    protected JMenuItem shrinkItem,configItem,versionItem,
   licenseItem,remoteItem, bestItem, mpsItem, gisItem, wtItem;  //options menu
  protected JMenuItem[]  helpItem;  //for adding things to helpmenu
  protected String[] helpFiles;  //holds names of helpfiles
  protected JMenuItem openItem,saveItem,outputItem,quitItem; 
		  				  
 private MenuItemListener menuItemListener = new MenuItemListener();
 
public static void main (String args[]) {  
   FrameMenu test=new FrameMenu ("File Menu Test");
   test.setLocation(200,200);
   test.pack();
}/*end main*/

public FrameMenu(String s){
  super(s);
  createComponents();
  myFrame=this;
  console=new JDataViewer("Habplan Console"," ");
  outputDialog=new OutputDialog(myFrame,"Habplan Output Control");
  importDialog=new ImportDialog(myFrame,"Habplan Import Control");
  projectDialog=new ProjectDialog(myFrame,"Habplan Project Text File");
  configDialog=new ConfigDialog(myFrame,"Configuration Control");
  newConfigDialog=new NewConfigDialog(myFrame,"Objective Function Reconfiguration Required!");
  version = new Version(myFrame);
  license = new License(myFrame);
  remoteControlTable=new RemoteControlTable(this);
  bestSchedule=new BestSchedule(this);
  lpBuild=new LpBuild(this);
  gisViewer=new GISViewer(this);
  regimeEditor=new RegimeEditor(this);
  unitForm=new UnitForm(this);
  
  //main window form layout
   setBackground(Color.getColor("habplan.main.background"));
   
   checkListener=new CheckListener();
   unitBox=new JCheckBox("Use Units",false);
   unitBox.addItemListener(checkListener);
   unitBox.setEnabled(false);
   unitBox.setBackground(Color.getColor("habplan.main.background"));
   unitBox.setToolTipText("Enforce Management Units");

   p2=new JLabelTextField("N of Iterations",4,"int");
   p2.text.addCaretListener(new ATextListener());
   p2.text.setText("1000");
   p2.setBackground(Color.getColor("habplan.main.background"));
    
   labelcomp=new JLabel("Add Objective Function Components");
   labelcomp.setForeground(Color.white);
   labelcomp.setBackground(Color.darkGray);
 
  lp1=new JPanel();
  lp1.add(labelcomp);
  checkPanel=new JPanel();
  checkPanel.setBackground(Color.getColor("habplan.main.background"));
  int cols=4;
  try{cols=Integer.parseInt(System.getProperty("habplan.config.nrow").trim());}
  catch(Exception e){ /*leave cols=4*/};
  int rows=(int)(Math.ceil((float)nComponents/cols)); 
  checkPanel.setLayout(new GridLayout(rows,cols,1,1));
   //add Component checkboxes
  for (int i=1; i<=nComponents; i++){
   compBox[i]=new JCheckBox(componentName[i],false);
   compBox[i].setBackground(Color.getColor("habplan.main.background"));
   compBox[i].addItemListener(checkListener);
   compBox[i].setEnabled(false);
   checkPanel.add(compBox[i]);
  } /*end for i*/
  
  
  //output label for printing output on main form when running
  outLabel=new JLabel(" ");
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  //set up the control buttons
   bPanel=new JPanel(); 
   bPanel.setBackground(Color.getColor("habplan.main.background"));
    bPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
   Font font = new Font("SansSerif", Font.BOLD, 14);
 start = new JButton("Start");
    start.addActionListener(new Listener());
    start.setEnabled(false);
    start.setActionCommand("start");
    start.setFont(font);
    start.setToolTipText("Start or Restart Iterations");
 suspend = new JButton("Suspend");
    suspend.addActionListener(new Listener());
    suspend.setActionCommand("suspend");
    suspend.setFont(font);
    suspend.setEnabled(false);
    suspend.setToolTipText("Suspend Iterations for later continuation");
 stop = new JButton("Stop");
    stop.addActionListener(new Listener());
    stop.setActionCommand("stop");
    stop.setFont(font);
    stop.setEnabled(false); 
    stop.setToolTipText("Stop Iterations");
  
  bPanel.add(start);bPanel.add(suspend);bPanel.add(stop);
  shrink(true);  //add components to frame and pack;
  //set up menus
  mbar = new JMenuBar();
  
  //font = new Font("SanSerif", Font.PLAIN, 12);
  //mbar.setFont(font);
 fileMenu = new JMenu("File",true);
 //add items to fileMenu
 this.updateFileMenu();
 
 //make edit menu
editMenu = new JMenu("Edit",true);
editMenu.add(unitItem=new JMenuItem("Units"));
unitItem.addActionListener(menuItemListener);
for (int i=1;i<=nComponents;i++){
 editMenu.add(editItem[i] = new JMenuItem(componentName[i]));
 editItem[i].addActionListener(menuItemListener);
}/*end for i*/ 

//make graph Menu
graphMenu = new JMenu("Graph",true);
//add items to graph menu
this.updateGraphMenu();

//make options Menu
optionsMenu = new JMenu("Misc",true);
optionsMenu.add(shrinkItem = new JMenuItem("Shrink..."));
shrinkItem.addActionListener(menuItemListener);
optionsMenu.add(configItem = new JMenuItem("Config..."));
configItem.addActionListener(menuItemListener);
optionsMenu.add(versionItem = new JMenuItem("Version"));
versionItem.addActionListener(menuItemListener);
optionsMenu.add(licenseItem = new JMenuItem("License"));
licenseItem.addActionListener(menuItemListener);

//make tool Menu
toolMenu = new JMenu("Tool",true);

toolMenu.add(remoteItem = new JMenuItem("Remote"));
remoteItem.addActionListener(menuItemListener);
toolMenu.add(bestItem = new JMenuItem("BestSchedule"));
bestItem.addActionListener(menuItemListener);
toolMenu.add(mpsItem = new JMenuItem("LpMPS"));
mpsItem.addActionListener(menuItemListener);
toolMenu.add(gisItem = new JMenuItem("GIS_View"));
gisItem.addActionListener(menuItemListener);
toolMenu.add(wtItem = new JMenuItem("Weight=1"));
wtItem.addActionListener(menuItemListener);
//create the help menu from files in help directory
this.updateHelpMenu();

mbar.add(fileMenu);
mbar.add(editMenu);
mbar.add(graphMenu);
mbar.add(optionsMenu);
mbar.add(toolMenu);
mbar.add(helpMenu);
setJMenuBar(mbar);
setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
	 int result=JOptionPane.showConfirmDialog(start,"Exit Habplan Now?","Really Quit?",JOptionPane.YES_NO_OPTION);
      if(result==JOptionPane.YES_OPTION){
	  //dispose(); //dispose of windows.  Time consuming.
         System.exit(1);  
      }
    }
      });   
      /*this starts a project that was entered on the command line*/
      if(UserInput.isCommandLine){
      projectDialog.readProjectFile(UserInput.projectDir+UserInput.projectFile);
      }
}/*end constructor*/

    /**
     *User clicks a checkbox
     */
    public class CheckListener implements ItemListener{
  
	public void itemStateChanged(ItemEvent e){
	     if(isImport)return; //dont launch threads for import request
	    JCheckBox box=(JCheckBox) e.getSource();
	    DataWaitThread waitThread=null;
	    if(dataThread!=null){//should allow previous thread to finish first
		if(dataThread.isAlive()){
		   waitThread=new DataWaitThread(dataThread);
		   waitThread.start();
		}
	    }
	    if(box == unitBox){
		dataThread=new DataReadThread("UNITDATA",waitThread); 
		dataThread.start();
		JProgressMeter progress=new JProgressMeter(myFrame,dataThread,box.getText());
		progress.work();
	    }else{
		dataThread=new DataReadThread("FORMDATA",waitThread);
		dataThread.start();
		JProgressMeter progress=new JProgressMeter(myFrame,dataThread,box.getText());
		progress.work();
	        if(!isReInit)unitBox.setEnabled(true);
	    }
	}/*end itemStateChanged*/
    } /*end inner CheckListener class*/

 
/**
*this is called when a check box changes its state.
* in some cases this is called manually, in others when an event is fired
*/
public  void manualItemStateChanged(){  

    
     for(int i=1; i<=nComponents; i++){
   if (compBox[i].isSelected()==true && isAdded[i]==false && form[i].verify()==true ){
       isAdded[i]=form[i].readData();//only add component if data read successfuly
       holdState.setEntryOrder(i); //record the entry order
       holdState.setObjCheck(i,isAdded[i]); //duplicates and stores isAdded


       if (runRunning)form[i].init(); //make sure an init gets done
       }
    else if (compBox[i].isSelected()==false && isAdded[i]==true){
     form[i].notifyParentOfRemoval();   //tell parent that a subcomponent is gone
     isAdded[i]=false;   	
     holdState.setObjCheck(i,isAdded[i]); //duplicates and stores isAdded
    }
   
   if (compBox[i].isSelected()==false && form[i].nSubComponents>0){
    console.println("Can't remove a component \nwhen it has active subComponents!");
   compBox[i].setSelected(true); isAdded[i]=true;
   }  
    	 
   }//end for i*/
   
   nPolys=ObjForm.nextPolygon-1;
   nOptions=ObjForm.nextRegime-1;
   
      ObjForm.problemSize(nPolys,nOptions); //update ObjForm parent class
      unitForm.prepUnits();
       if(!isImport)BestSchedule.resetBestFitness(); //set bestFitness to 0
   updateGraphMenu(); 
   
    
}/*end method*/


/**
* Handle button clicks
**/
   
public class Listener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
 
    
    if (cmd.equals("start") && verify()==true && !runRunning) {
	File file1=new File(License.fileName); //is license accepted
	if (!file1.exists()){console.clear();console.println("You must accept the license agreement first!");license.setVisible(true);return;}
	else {
	    try{
	      FileReader fin =  new FileReader(License.fileName);
	      LineNumberReader passFile = new LineNumberReader(fin);
	      passWord=passFile.readLine().trim();
	    }/*end try*/
	     catch(Exception exc){
	     console.println("Error reading problem size password: " + exc);
	    }
	}  /*end else file1.exists()*/
    int permitID=-1;
    if (!passWord.equals(limit0))permitID=0;
    if (passWord.equals(limit1))permitID=1;
    if (passWord.equals(limit2))permitID=2;
    if (passWord.equals(limit3))permitID=3;

    switch (permitID){
	case 1:
	 if (nPolys>1050){console.println("\nYour password only allows 1050 polygons.");license.setVisible(true);return;}	
	 break;
	case 2: 
	 if (nPolys>2000){console.println("\nYour password only allows 2000 polygons.");license.setVisible(true);return;}
	 break;
	case 3: 
	 if (nPolys>3000){console.println("\nYour password only allows 3000 polygons.");license.setVisible(true);return;} 	
	 break;
	case 0:
	 if (nPolys>500){console.println("\nYou need a password for more than 500 polygons.");license.setVisible(true);return;}	
    }/*end switch*/
    
     startTheRun(); //this can be called from elsewhere
     return;  
    }  
   else if (cmd.equals("start") && met!=null &&  met.isAlive()) {  //user is unsuspending
       unSuspendTheRun();
    }
   if (cmd.equals("suspend")) {suspendTheRun();} 
   if (cmd.equals("stop")) { stopTheRun();} //can be called remotely    
              
  }/*end actionperformed()*/
} /*end inner Listener class*/

/**
*UNsuspend Habplan. Can be called remotely or by pushing a button
*/
protected void unSuspendTheRun(){
    gisViewer.setBuffering(true);
    start.setEnabled(false);
    suspend.setEnabled(true);
    stop.setEnabled(true);
    runSuspend=false;
}/*end method*/

/**
*Suspend Habplan. Can be called remotely or by pushing a button
*/
protected void suspendTheRun(){
    gisViewer.setBuffering(false);//handle gray rect problem
 start.setEnabled(true);
 suspend.setEnabled(false);
 runSuspend=true; //hold the computations until runSuspend=false;
}/*end method*/

/**
*Stop habplan running.  Can be called remotely
*/
protected void stopTheRun(){
    gisViewer.setBuffering(false);
    suspend.setEnabled(false);
     stop.setEnabled(false);
     if(met==null)return;  //useful when called remotely as a check
     runRunning=false;
     runSuspend=false;
     runStop=true;
}/*end method*/

/**
*Start habplan running.  Can be called remotely
*/
protected void startTheRun(){
    gisViewer.setBuffering(true);
     runHistory=true;  //indicates that a run was made so there should be a valid schedule
     suspend.setEnabled(true);
     stop.setEnabled(true);
     start.setEnabled(false);
     UserInput.iterate=Integer.parseInt(p2.text.getText().trim()); //get n of iterations
     if(runRunning){runSuspend=false;runStop=true;}   
     met=new Metropolis(this);
     met.setPriority(Thread.MIN_PRIORITY); 
     runRunning=true;
     runSuspend=false;
     runStop=false;
     if (isImportRun){//user imported a run, so stop before iteration 2 so user can view it
      start.setEnabled(true);
      suspend.setEnabled(false);
      runSuspend=true; //hold the computations until runSuspend=false;
      isImportRun=false;
     }   
     met.start(); 
}/*end method*/
  

class MenuItemListener implements ActionListener {
    public void actionPerformed(ActionEvent event){
     JMenuItem item = (JMenuItem)event.getSource();
     
     if (item == openItem){
	 JFileChooser fc = new JFileChooser();
         fc.setSelectedFile(new File(UserInput.projectDir,UserInput.projectFile));
	 HBPFilter filter = new HBPFilter("hbp","Habplan old style (hbp) Files");
	 HBPFilter filter2 = new HBPFilter("xml","Habplan Project Files (xml)");
	 String sep=System.getProperty("file.separator");
         fc.setFileFilter(filter);
	 fc.setFileFilter(filter2);
	 File prjFile;
	 int returnVal = fc.showOpenDialog(myFrame); 
	 prjFile = fc.getSelectedFile();
	 if(shrunk)shrink(shrunk);  //unshrink the forms if needed
	 if (prjFile != null && returnVal == JFileChooser.APPROVE_OPTION) {
	     UserInput.projectFile=prjFile.getName();
             if(UserInput.projectFile.endsWith("hbp")){
		 UserInput.hbpDir=prjFile.getParent()+sep;
	     } else UserInput.projectDir=prjFile.getParent()+sep;
	     int p2=UserInput.projectDir.lastIndexOf(System.getProperty("file.separator"));
       if (p2 > 0) UserInput.projectDir=UserInput.projectDir.substring(0,p2+1);
	     int result=JOptionPane.showConfirmDialog(start,"Load: "+UserInput.projectFile,"Really Load?",JOptionPane.OK_CANCEL_OPTION);
	  if(result!=JOptionPane.CANCEL_OPTION && UserInput.projectFile.endsWith("hbp")){
	       reInitialize(); //reInit and then load new settings
	       setState.loadState(UserInput.hbpDir+UserInput.projectFile);
	       setState.unsetEntryOrder();
	       gisViewer.loadState(UserInput.hbpDir+UserInput.projectFile);
	     }/*fi !CANCEL*/
	  else if(result!=JOptionPane.CANCEL_OPTION && UserInput.projectFile.endsWith("xml")){
	      projectDialog.readProjectFile(UserInput.projectDir+UserInput.projectFile);
	  }
	 }
    }/*end if openItem*/
    
    if (item == saveItem){
	if(UserInput.projectFile.endsWith("hbp")){
	    if(gisViewer.regimeColorTable.data.color.length==2)
	    console.println("Note: GIS Viewer settings won't be completely saved to the \n new xml file unless you start the run, then open the GIS Viewer and press draw.");
              UserInput.projectFile=UserInput.projectFile.replace("hbp","xml");
	    }
	JFileChooser fc = new JFileChooser(UserInput.projectDir);
	fc.setSelectedFile(new File(UserInput.projectDir,UserInput.projectFile));
	 HBPFilter filter = new HBPFilter("xml","Habplan project files"); //only show .xml files
	 fc.setFileFilter(filter);
	 File prjFile;
	 int returnVal = fc.showSaveDialog(myFrame);
	  prjFile = fc.getSelectedFile();
     if (prjFile!=null && returnVal == JFileChooser.APPROVE_OPTION) {
	 UserInput.projectFile=prjFile.getName();
	 UserInput.projectDir=prjFile.getParent()+System.getProperty("file.separator");
	 int p2=UserInput.projectDir.lastIndexOf(System.getProperty("file.separator"));
        if (p2 > 0) UserInput.projectDir=UserInput.projectDir.substring(0,p2+1); 
     int result=JOptionPane.showConfirmDialog(start,"Save Current Schedule for Later Import?","Save To: "+UserInput.projectFile,JOptionPane.YES_NO_CANCEL_OPTION);
     isSaveImport=false; //should the save be for later import
     if(result==JOptionPane.NO_OPTION) resetWeights();
     if(result==JOptionPane.YES_OPTION) isSaveImport=true;
     if(result!=JOptionPane.CANCEL_OPTION)
	 // setState.saveState(UserInput.hbpDir+UserInput.saveFile);//old style
     // gisViewer.saveState(UserInput.saveFile);//do manually at gisViewer
    projectDialog.writeProjectFile(UserInput.projectDir+UserInput.projectFile);
     saveOldJobs();// see if this needs to be appended to bottom of file menu for future reference
     }
    }/*end if saveItem*/
    
     if (item == outputItem){
      outputDialog.setVisible(!outputDialog.isVisible());
    }/*end if outputItem*/

     if (item == initItem){
	  int result=JOptionPane.showConfirmDialog(start,"Prepare For a New Job?","Really ReInitialize?",JOptionPane.YES_NO_OPTION);
      if(result==JOptionPane.YES_OPTION){
	   reInitialize();
      }/*fi !CANCEL*/ 
      return;
    }/*end if Item*/
    
    
    if (item == configItem){
       configDialog.setVisible(!configDialog.isVisible());
    }/*end if outputItem*/
    
    if (item == versionItem){
       version.setVisible(!version.isVisible());
    }/*end if versionItem*/

    if (item == licenseItem){
       license.setVisible(!license.isVisible());
    }/*end if licenseItem*/

    if (item == remoteItem){
       remoteControlTable.setVisible(!remoteControlTable.isVisible());
    }/*end if Item*/
        
    if (item == bestItem){
       bestSchedule.setVisible(!bestSchedule.isVisible());
    }/*end if Item*/	

    if (item == mpsItem){
       lpBuild.setVisible(!lpBuild.isVisible());
       if(!lpBuild.isVisible())lpBuild.closeMpsWindows();
    }/*end if Item*/	
    
     if (item == gisItem){
       gisViewer.setVisible(!gisViewer.isVisible());
       gisViewer.makeRelatedWindowsInvisible(); //there are multiple windows
    }/*end if Item*/
   
    if (item == wtItem){
	resetWeights();
    }/*end if Item*/	
 
    if (item == unitItem){//management unit form
	unitForm.setVisible(!unitForm.isVisible());
    }/*end if Item*/

    if(item == titleItem){//auto add titles to graphs
	autoTitles();
    }
	
    //deal with edit Items for showing component forms
    for (int i=1; i<=nComponents; i++){
      if (item == editItem[i])
       form[i].setVisible(!form[i].isVisible());
    }/*end for i*/

    //deal with Graph Items for showing component graphs
    for (int i=1; i<=nComponents; i++){
      if (form[i].isGraph() && item == graphItem[i] && compBox[i].isSelected())
       form[i].graph.setVisible(!form[i].graph.isVisible());
    }/*end for i*/
    
     if (item == shrinkItem){
      shrink(shrunk);
    }/*end if shrinkItem*/

    if(item == quitItem){
       int result=JOptionPane.showConfirmDialog(start,"Exit Habplan Now?","Really Quit?",JOptionPane.YES_NO_OPTION);
      if(result==JOptionPane.YES_OPTION){
	  //dispose(); //dispose of windows.  Time consuming.
         System.exit(1);  
      }/*fi !CANCEL*/ 
    }/*end if quitItem*/
      
    for(int j=1;j<=nOldJobs;j++){
	if(item == oldItem[j]){
         int result=JOptionPane.showConfirmDialog(start,"Load: "+oldJobs[j],"Really Load?",JOptionPane.YES_NO_OPTION);
      if(result==JOptionPane.YES_OPTION){
	if (shrunk)shrink(shrunk);
	reInitialize();
        UserInput.projectFile=oldJobs[j];
	if(oldJobs[j].endsWith("hbp")){

	 //if no path, assume its in hbpDir
	 if (oldJobs[j].indexOf(System.getProperty("file.separator"))<0){
	  UserInput.projectFile=UserInput.hbpDir+oldJobs[j];
	 }
         setState.loadState(UserInput.projectFile);
	 setState.unsetEntryOrder();
        }else{ //assume its a new project file in xml format
          if (oldJobs[j].indexOf(System.getProperty("file.separator"))<0){
	   UserInput.projectFile=UserInput.projectDir+oldJobs[j];
	  }
	        projectDialog.readProjectFile(UserInput.projectFile);
	   }

	gisViewer.loadState(UserInput.projectFile);
      }/*fi cancel*/
        break;
       }/*end if item*/
    }/*endfor*/
    
    
    for (int j=0; j<helpFiles.length;j++){
     if (item == helpItem[j]){
      JFileViewer fv = new JFileViewer(UserInput.HelpDir,helpFiles[j]);
      fv.setVisible(true);
    }/*end if biolItem*/
    }/*endfor*/
      
    } /*end actionPerformed*/
}/*end MenuItemListener*/



 public class ATextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
     JTextField field=(JTextField)event.getSource();
 try{
     if(field==p2.text)
	 UserInput.iterate=Integer.parseInt(p2.text.getText().trim()); 
   }/*end try*/
 catch(Exception e){ } 
 }/*end textChangedValue*/
} /*end inner ATextListener class*/

    /**
     *reset all objective function component weights to 1.0
     *Useful when saving current settings or when you just want
     *to re-initialize the run
     */
    public void resetWeights(){
	for(int i=1; i<=nComponents; i++){
            double[] wt={1.0,1.0};
	    form[i].setWeight(wt);
	}/*end for i*/	

    }/*end method*/

/**
*this allows the user to run another job without
*exiting the program to reset things 
*/
public void reInitialize(){
 
    unitBox.setEnabled(false);
    unitBox.setSelected(false);
    unitForm.isUnitDataRead=false;
    unitForm.isPrepUnits=false;

    isReInit=true; //indicates that calls are coming from reInit method
    
  ObjForm.nextRegime=1;
  ObjForm.nextPolygon=1;
  ObjForm.polySched=null;
  BestSchedule.resetBestFitness();
  try{ObjForm.polyTable.clear(); ObjForm.regimeTable.clear();
     ObjForm.regimeArray.clear();
     ObjForm.regimeArray.addElement("0");//get 0 in position 0 because Biol1 uses 0
   }
   catch (Exception e){}//dont clear them
  isAdded = new boolean[nComponents+1]; //keep track of components added to obj function
  
  for(int i=1; i<=nComponents; i++){   
    compBox[i].setEnabled(true);//enables compBoxes   
   isAdded[i]=false; compBox[i].setSelected(false);
   compBox[i].setForeground(Color.black);
   form[i].clearSettings();
   form[i].dataRead=false; //read the data again
  }/*end for i*/		   
   updateGraphMenu();

   start.setEnabled(true);
 
   if (runRunning) {
     suspend.setEnabled(false);
     stop.setEnabled(false);
     runRunning=false;
     runSuspend=false;
     runStop=true;
   }/*end if metRunnin*/ 
     
   Algorithm.isWaiting=true;
   isReInit=false; 
}/*end reInitialize()*/

/**
*Shrink or unshrink the main menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){

    JPanel unitPanel=new JPanel();
    unitPanel.add(unitBox);
    unitPanel.add(p2);
    unitPanel.setBackground(Color.getColor("habplan.main.background").darker());
   JPanel topPanel=new JPanel(new BorderLayout());
   topPanel.add(unitPanel,BorderLayout.SOUTH);
   topPanel.setBackground(Color.getColor("habplan.main.background"));
  JPanel botPanel=new JPanel();
  botPanel.setLayout(new BorderLayout());
  botPanel.setBackground(Color.black);
  botPanel.add(outLabel,BorderLayout.CENTER);
  botPanel.add(bPanel,BorderLayout.SOUTH);
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().add(checkPanel,BorderLayout.CENTER);
  getContentPane().add(botPanel,BorderLayout.SOUTH);
  pack();
  for (int j=1;j<=nComponents;j++)form[j].shrink(shrunk);
  this.shrunk=true;
 }/*end if*/
 else{
  getContentPane().removeAll();
  getContentPane().add(topPanel,BorderLayout.NORTH);
  getContentPane().add(checkPanel,BorderLayout.CENTER);
  getContentPane().add(botPanel,BorderLayout.SOUTH);
  for (int j=1;j<=nComponents;j++)form[j].shrink(shrunk);
  this.shrunk=false;
  pack();
  
 }/*end else*/
} /*end shrink()*/

/**
* save the current filename to appear later at the bottom of the file menu
*if it isn't already there
*/
public void saveOldJobs(){
 
 //now save the oldjobs in oldJobs.hbp
 //but dont include path if its in hbpDir to increase portability across machine types
  int last=UserInput.projectFile.lastIndexOf(System.getProperty("file.separator"));
  String direct=UserInput.projectFile.substring(0,last+1);
  if (direct.toLowerCase().equals(UserInput.projectDir.toLowerCase()))
    UserInput.projectFile=UserInput.projectFile.substring(last+1,UserInput.projectFile.length());
   
   FileOutputStream fout=null;
   boolean unique=true;
   for (int i=1;i<=nOldJobs;i++)if (oldJobs[i].equals(UserInput.projectFile)) unique=false;
  if (unique){  //skip the rest unless its a new job
    nOldJobs=(maxOldJobs>nOldJobs)?++nOldJobs:maxOldJobs;
  try{ 
   fout =  new FileOutputStream(UserInput.oldJobs);
    PrintWriter out = new PrintWriter(fout,true);
     out.println(UserInput.projectFile);//put the new job first in the queue
    for (int i=1;i<nOldJobs;i++){
     out.println(oldJobs[i]);
   } /*endfor i*/
   
   }/*end try*/
    catch(Exception e){
   
  } /*end catch*/
   finally { try { if (fout != null) fout.close(); } catch (IOException e) {} }
   this.updateFileMenu();
 }/*endif unique*/   
 
} /*end method*/


/**
*make sure this form is filled out
*/
public boolean verify(){
    boolean componentAdded=false;
 for (int i=1; i<=nComponents;i++)
   if (compBox[i].isSelected()==true)componentAdded=true;
 if(componentAdded==false){
   console.println("No Objective Function Components??");
   runSuspend=true;
   return(false);
   }/*end if*/
  
try{
 Integer.parseInt(p2.text.getText().trim());
 return(true);
   }
 catch(Exception e){
  console.println("Fill out the main form!");
  return(false);
   } 

}/*end verify()*/


/**
*Add the currently valid graphs to the graphMenu
*/
protected void updateGraphMenu(){
  graphMenu.removeAll();
  boolean isAGraph=false;
 for (int i=1;i<=nComponents;i++){
  if (form[i].isGraph() && compBox[i].isSelected()){
      isAGraph=true;
   graphMenu.add(graphItem[i] = new JMenuItem(componentName[i]));
   graphItem[i].addActionListener(menuItemListener);
  }/*end if*/
 }/*end for i*/ 
     if(isAGraph){
      titleItem=new JMenuItem("AutoTitle");
      titleItem.setToolTipText("Add Titles Derived from Data File Names");
      graphMenu.add(titleItem);
      titleItem.addActionListener(menuItemListener);
	 }
}/*end updateGraphMenu()*/

    /**
     *Add titles to graphs derived from the data file name for that component
     **/
    protected void autoTitles(){
	String sep=System.getProperty("file.separator");

	for(int i=1;i<=nComponents;i++){
	    //set up titles if needed
	    if(form[i].title.equals("")){
		 form[i].title=form[i].p1.text.getText();
		 int slash=form[i].title.lastIndexOf(sep);
                 int len=form[i].title.length();
		 if(slash>0 && len>slash+1){
                     form[i].title=form[i].title.substring(slash+1);
		 }
	    }
	    compBox[i].setToolTipText(form[i].title);

	    if (form[i].isGraph() && compBox[i].isSelected()){

		form[i].graph.setVisible(true);
		if(!form[i].graph.getGTitle().equals(""))
		    form[i].title=form[i].graph.getGTitle();
		form[i].graph.setGTitle(form[i].title);
		
	    }else if(form[i].isGraph()){
		form[i].graph.setVisible(false);
	    }
	}
    }

/**
*add oldJobs to lower part of fileMenu
*/
protected void updateFileMenu(){
 BufferedReader in=null;
 //now read oldJobs.hbp for previous saved files
 try{	 
     in = new BufferedReader(new FileReader(UserInput.oldJobs),1024);     
    for(int i=1;i<=maxOldJobs;i++){ oldJobs[i]=in.readLine().trim();
			        nOldJobs=i;}
   } /*end try*/ 
   catch(Exception e){    
      
   }
   finally { try{if(in!=null)in.close();} catch (IOException e2){ } }
   
   fileMenu.removeAll();
   fileMenu.add(openItem = new JMenuItem("Open..."));
   fileMenu.add(saveItem = new JMenuItem("Save..."));
   fileMenu.add(outputItem = new JMenuItem("Output..."));
   fileMenu.add(initItem = new JMenuItem("reInit"));
   fileMenu.add(quitItem = new JMenuItem("Quit..."));
   openItem.addActionListener(menuItemListener);
   openItem.setToolTipText("Open a habplan project file");
   saveItem.addActionListener(menuItemListener);
   saveItem.setToolTipText("Save current settings to a habplan project file");
   outputItem.addActionListener(menuItemListener);
   outputItem.setToolTipText("output schedule and flows to files");
   initItem.addActionListener(menuItemListener);
   initItem.setToolTipText("Start a new project");
   quitItem.addActionListener(menuItemListener);
   quitItem.setToolTipText("Exit Habplan");
    //now add ability to open old jobs as a convenience
 if(nOldJobs>0){
   fileMenu.addSeparator();
   
   String shortLabel;
  for (int j=1;j<=nOldJobs;j++){
   shortLabel=oldJobs[j];
   shortLabel=shortLabel.substring(shortLabel.lastIndexOf(System.getProperty("file.separator"))+1);
   fileMenu.add(oldItem[j] = new JMenuItem(shortLabel));
   oldItem[j].addActionListener(menuItemListener);
  }/*end for*/
}/*end if nOldJobs>0*/ 
   
}/*end updateFileMenu()*/

/**
*each file in the help directory will
*get an entry in the help menu.  This will
*encourage providing lots of help
*/
protected void updateHelpMenu(){
 File f=null;
 String[] files;
 //now read oldJobs.hbp for previous saved files
 try{	 
     f=new File(UserInput.HelpDir);
   } /*end try*/ 
   catch(Exception e){          
   }
  
   if (!f.isDirectory())return;
   helpFiles=f.list();
   helpItem=new JMenuItem[helpFiles.length];
   helpMenu = new JMenu("?",true);
   
   String shortLabel;
   int end=0;
   for(int i=0;i<helpFiles.length;i++){
    shortLabel=helpFiles[i];
    end=shortLabel.lastIndexOf(".");
    if (end<=0)end=shortLabel.length();
    shortLabel=shortLabel.substring(0,end);
    helpMenu.add(helpItem[i] = new JMenuItem(shortLabel));
    helpItem[i].addActionListener(menuItemListener);
    }/*endfor i*/
   
}/*end updateHelpMenu()*/


/**
*set isImport which indicates if the current schedule was just imported
*note that the other variable isImportRun must also be set
@param isImport is true or false
*/
public void setIsImport(boolean isImport){
    this.isImport=isImport;
    this.isImportRun=isImport;
}/*end method*/

/**
*make a new HoldState instance
*useful after calling getHoldState, if you want to 
*make sure future references to holdState don't write over older instances
*/
public void newHoldState(){
    holdState=new HoldState(UserInput.nFlow,UserInput.nBKofF,UserInput.nCofF,
       UserInput.nBioI,UserInput.nBioII,UserInput.nSMod);
       }/*end method*/

/**
*return the current holdState instance
*/
public HoldState getHoldState(){return holdState;}

/**
*set the holdState value
*/
public void setHoldState(HoldState holdState){this.holdState=holdState;}

/**
*capture settings into Object vector to be saved into HoldState Object
*/
protected void captureHoldStateSettings(Object[] vec){
     vec[0]=new Integer(ObjForm.nPolys);
     vec[1]=ObjForm.polySched;
     vec[2]=System.getProperty("habplan.nrow");
     vec[3]=System.getProperty("habplan.config"); 
     /*comBine n of iterations and unitFile name
       this allows for backward compatibility in HoldState*/
     vec[4]=p2.text.getText()+"%%%"+unitForm.unitField.getText();
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
    ObjForm.nPolys=((Integer)vec[0]).intValue();
    ObjForm.polySched=(int[])vec[1];
    nrowInFile=(String)vec[2];
    configInFile=(String)vec[3];
    if (!configInFile.equals(System.getProperty("habplan.config")))
    newConfigDialog.setVisible(true);
    String[] field = {((String)vec[4])}; //handle combined iter and unitField
    if(field[0].contains("%%%")){
	field=field[0].split("%%%");//if no unitfile, length=1
	if(field.length>1)unitForm.unitField.setText(field[1]);
    }
    p2.text.setText(field[0]); //iterations
 }
   /**
     *Save current schedule settings to xml2 file for easy initialization and portability
     *
     **/
    public void writeScheduleSettings(PrintWriter out){
	String id,regime,added="";
	if(ObjForm.polySched==null)return;
       	
	out.println("  <config value=\""+System.getProperty("habplan.config")+"\" />");
	out.println("  <npolys value=\""+ObjForm.nPolys+"\" />");


	for(int i=1;i<=nComponents;i++){
	    int add=0;
	    if(isAdded[i])add=1;
	    added+=add+",";  
	}
	added=added.substring(0,added.length()-1); //get rid of last ","
	out.println("  <objective value=\""+added+"\" />");
	if(ObjForm.polyArray==null || ObjForm.regimeArray==null){
	    isSaveImport=false;
	    return;//user didn't load any data
	}

	for(int i=1;i<ObjForm.polySched.length;i++){
          id=(String)ObjForm.polyArray.elementAt(i-1);
          regime=(String)ObjForm.regimeArray.elementAt(ObjForm.polySched[i]);
          if(ObjForm.polySched[i]==0)isSaveImport=false; //invalid schedule
          out.println("  <poly id=\""+id+"\" regime=\""+regime+"\" />");
	}
	out.println("  <import value=\""+isSaveImport+"\" />");
       
    }

    /**
     *Save settings to xml file for easy initialization and portability
     *
     **/
    public void writeSettings(PrintWriter out){
        out.println("  <![CDATA[ \n"+descriptiveData+"\n  ]]>");
	out.println("  <iters value=\""+p2.text.getText()+"\" />");
	out.println("  <units value=\""+unitForm.unitField.getText()+"\" />"); 
	out.println("  <useunits value=\""+unitBox.isSelected()+"\" />");
        out.println("  <home value=\""+UserInput.HabplanDir+"\" />");
    }
	
     /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("iters"))p2.text.setText(atts.getValue(0));
	if(qName.equals("units"))unitForm.unitField.setText(atts.getValue(0));
	if(qName.equals("useunits")){
	    if(atts.getValue(0).equals("true"))unitBox.setSelected(true);
	}
	if(qName.equals("config")){
	    configInFile=atts.getValue(0);
	    if (!configInFile.equals(System.getProperty("habplan.config")))
	              newConfigDialog.setVisible(true);
	}
	
	if(qName.equals("import") && (atts.getValue(0)).toLowerCase().equals("true"))
	 isSaveImport=true;
       
	if(qName.equals("npolys")){
	    try{
		ObjForm.nPolys=Integer.parseInt(atts.getValue(0));
		ObjForm.polySched=new int[ObjForm.nPolys+1];
		ObjForm.oldSched=new Vector(ObjForm.nPolys);
	    }catch(Exception e){
		console.println("Bad npolys tag");
		return;
	    }
	}
     
	if(qName.equals("objective")){
	    String[] added=(atts.getValue(0)).split(",");
	    ObjForm.isInComponent=new boolean[nComponents];
	    for(int i=0;i<nComponents;i++){
		if(added[i].equals("1"))ObjForm.isInComponent[i]=true;
	    }
	}

	if(qName.equals("poly") && atts.getLength()==2 ){
	    String[] names=new String[2];
	    names[0]=atts.getValue(0);//polyName
	    names[1]=atts.getValue(1);//regimeName
	    ObjForm.oldSched.add(names);
	}

	}catch(Exception e){console.println("FrameMenu.readSettings() "+e);}
 
    }


/**
*creates the components 
*must be edited everytime a new component is made available
*or it must be overridden in an extension
*/
protected void createComponents(){
  
  String formName;
  int cnt=0; //count the components as they're made
  int flowId; //remember the id of the last flowform
	      //each form remembers this in its idNum variable
	//holdState holds the current state of Habplan to saving or rmi use      
  newHoldState();
   setState=new SetState(this);    	      
  
     //add Flow and Flow subcomponents
   for (int j=1;j<=UserInput.nFlow;j++){
     cnt++;componentName[cnt]="F" + j;
     formName=componentName[cnt] + " Component";
     form[cnt] = new FlowForm(this, formName);
     flowId=cnt;
    for (int j1=1; j1<=UserInput.nCofF[j];j1++){
     cnt++;componentName[cnt]="C" + j1 + "("+ j + ")";
     formName=componentName[cnt] + " Component";
     form[cnt] = new CCFlowForm(this, formName,(FlowForm)form[flowId]);
    }/*end for j1*/ 
    for (int j2=1; j2<=UserInput.nBKofF[j];j2++){
     cnt++;componentName[cnt]="BK" + j2 + "("+ j + ")";
     formName=componentName[cnt] + " Component";
     form[cnt] = new BlockForm(this, formName,(FlowForm)form[flowId]);
    }/*end for j1*/ 
   }/*end for j*/
   
    //Add Biological type 1 components
    for (int j=1;j<=UserInput.nBioI;j++){
     cnt++;componentName[cnt]="Bio1-" + j;
     formName=componentName[cnt] + " Component";
     form[cnt] = new BiolForm(this, formName);
    }/*end for j*/
   
    //Add Biological type 2 components
    for (int j=1;j<=UserInput.nBioII;j++){
     cnt++;componentName[cnt]="Bio2-" + j;
     formName=componentName[cnt] + " Component";
     form[cnt] = new Biol2Form(this, formName);
    }/*end for j*/
    
    //Add Spatial Model  components
    for (int j=1;j<=UserInput.nSMod;j++){
     cnt++;componentName[cnt]="SMod" + j;
     if (cnt>nComponents)System.out.println("Error: nComponents = "+nComponents+" cnt= "+cnt);
     formName=componentName[cnt] + " Component";
     form[cnt] = new SpaceForm(this, formName);
    }/*end for j*/
}/*end createComponents()*/

    
      /**
      *Shows message dialogs without killing the thread that calls it
      *
      **/
     void showMessageDialog(final Component myParent,final String message, final String title,final int option,final Icon icon)  {
	 try{
	 Runnable showModalDialog = new Runnable() {
		 public void run() {
		     JOptionPane.showMessageDialog(myParent,
						      message,title,option);
		 }
	     };
	 SwingUtilities.invokeAndWait(showModalDialog);
	 }catch(Exception e){
	     System.out.println("Error in showMessageDialog: "+e);
	 }
     }    
 


    /**
     *class to do the time consuming readData operation
     *called when user clicks a check box on main menu
     *this will put the algorithm thread into suspend mode
     *and restart it if necessary.  This prevents the algorithm
     *thread from trying to access objects that are in transition.
     */  
    class DataReadThread extends Thread{
	String type;
      	Thread waitThread;
	boolean saveRunSuspend;

	public DataReadThread(String type,Thread waitThread){
	    this.type=type;
	    this.waitThread=waitThread;
	}
	public void run(){
	   
	    if(isReInit)return; //don't do this if called from reInitialize()
	    saveRunSuspend=runSuspend;
	    runSuspend=true;
	   try{

	       if(waitThread!=null){
		    while(waitThread.isAlive())this.sleep(10);
	       }
	       while(!Algorithm.isWaiting){
		   this.sleep(10);//wait for algoritm to suspend	     
	       }
	   }catch(InterruptedException e){
	       System.out.println("DataReadThread: "+type+" "+e);
	   }
	   start.setEnabled(false);
	     if(type.equals("FORMDATA")) manualItemStateChanged();
	     if(type.equals("UNITDATA")){
		 boolean doPrep=unitForm.readUnitData();
		   if(doPrep) unitForm.prepUnits();
		   else{
		       unitBox.removeItemListener(checkListener);
		       unitBox.setSelected(false);
		       unitBox.addItemListener(checkListener);
		   }
 
	     }
	     //if user suspended in middle of this thread this could 
	     //cause problems.  Seems unlikely that this would happen.
	     runSuspend=saveRunSuspend;//reset runSuspend
	     if(runSuspend || runStop)start.setEnabled(true);
	     return;
	}/*end method*/  
    }/*end Read Thread class*/

    /**
     *class to wait for previous dataRead thread to finish
     *
     **/
    class DataWaitThread extends Thread{
	Thread thread;
	public DataWaitThread(Thread thread){
	    this.thread=thread;
	}
	public void run(){
	    try{ thread.join();
	    }catch(Exception e)	{System.out.println("DataWaitThread:"+e);}
	}
    }/*end inner class*/

} /*end FrameMenu class*/
