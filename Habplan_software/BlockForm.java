import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import figure.*;
import org.xml.sax.Attributes;


/**
*Make an Objective Function BlockSize Component form
*Contains all the methods needed for the Block component.
*@author Paul Van Deusen (NCASI)  1/98
*/
public class BlockForm extends ObjForm {
   JFileButton loadButton;
   JLabelTextField p4,p5,p6,p8;
   JLabelTextScroll p7;
   JCheckBox orderBox; //check for using 1st order nabes only
   JTextArea textBlock; //hold regimes@years that don't contribute to blocksize
   JTextField polyId; //enter an id to check the data
   JLabel outLabel; //output goes here
   JPanel pp; //look at data panel
   JButton killSmall; //button to kill small blocks
   boolean killSmallBlocks=false; //indicates that the user wants to kill small blocks
   
   
   //userInput Blocksize Component Variables
  protected String blockFile;
  protected int nNotBlocks=1;  //n of regime@period pairs
  protected String hiddenNotBlocks; //so user can see all notBlock options
  protected String badNotBlocks; //used to notify users about unknown notBlocks
  protected int[][] notBlocks;  //regime@period pairs
  protected int[] sizeVec; //used in deltaOBJ()
  protected int window;
  protected boolean firstOrderOnly=false; //user wants to use only 1st order nabes
  
  protected int minSize=1, //the current minimum size target
   minTarget, //the ultimate user requested min size target
   minIters,    //the number of iterations since last increase in minSize
   minIncrement=5, //the minSize target increment
   maxSize;
  protected float goal;
   
   //variables related to the objective function
   FlowForm flowform; //the FlowForm parent that holds polygon data
   int xMax, xMin; //min and max years from flowform parent
   FlowData[] flowData; //data in parent flowform
   BlockData[] data;
    int yrs;
   boolean bySkip=false; //for skipping byGone years
   int[] byYrs;  //pointer to byGone yrs
   int nByYrs; //n of byYrs  
   int nPositive=0; //number of obj changes>0 for adjusting weight
   int nTotal=0;  //actual n of polygons available for changing
   static double minValue=1.0E-12;  //dont let scale parameters go below this
   float blockmin[]; //min blocksize by year
   float blockav[]; //ave blocksize by year
   float blockmax[];//max blocksize by year
    float areaWithin[];//area of blocks that are within user limits
    float areaAbove[]; //area of blocks above user max
    float areaBelow[]; //area of blocks below user min
   int maxNabes; //holds max n of nabes for any polygon, computed during data read
   BlockGIS gisData=new BlockGIS();  //holds the block structure for access by other classes

/**
*indicates whether  component has a Graph associated with it
*/
public boolean isGraph(){ return(true);}

public BlockForm(FrameMenu parent, String s, FlowForm flowform){
  super(s, 10, 1);
  this.flowform=flowform;
  graph = new Figure(s,2,1,UserInput.hbpDir);
  getContentPane().setBackground(Color.black);
  p1=new JLabelTextField("BlockSize fileName",20,"str");
  p1.setBackground(Color.getColor("habplan.block.background"));
  p1.text.addCaretListener(new ATextListener());
  
  //TextArea for entering regime@year
  Font font = new Font("Monospaced", Font.BOLD, 12);
  textBlock=new JTextArea("",3,10);
  textBlock.addMouseListener(new BlockListener());
  textBlock.setFont(font);
  textBlock.setBackground(Color.white);
  textBlock.addCaretListener(new ATextListener());
  p4=new JLabelTextField("Green Up",4,"int");
  p4.setBackground(Color.getColor("habplan.block.background"));
  p4.text.addCaretListener(new ATextListener());
  p5=new JLabelTextField("Blocksize:  Min",4,"int");
  p5.setBackground(Color.getColor("habplan.block.background"));
  p5.text.addCaretListener(new ATextListener());
  p6=new JLabelTextField("Max",4,"int");
  p6.setBackground(Color.getColor("habplan.block.background"));
  p6.text.addCaretListener(new ATextListener());
  p7=new JLabelTextScroll("Goal",4,0,100,100);
  p7.setBackground(Color.getColor("habplan.block.background"));
  p7.text.addCaretListener(new ATextListener());
  p8=new JLabelTextField("Weight",8,"float");
  p8.setBackground(Color.getColor("habplan.block.background"));
  p8.text.addCaretListener(new WtTextListener());
  p8.text.setText("1");
  orderBox=new JCheckBox("1st Order Neighbors",false);
  orderBox.setBackground(Color.getColor("habplan.block.background"));
  orderBox.addItemListener(new CheckListener());
  
  //output label
  outLabel=new JLabel(" ");
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  loadButton= new JFileButton("File Selector",parent,p1.text,null,null);
  loadButton.setBackground(Color.getColor("habplan.block.background"));
  
  //create a data verify field
  pp = new JPanel();
  pp.setBackground(Color.getColor("habplan.block.background"));
  JLabel verify = new JLabel("Verify data for Polygon #");
  pp.add(verify);
  polyId = new JTextField(" ",4);
  polyId.setBackground(Color.lightGray);
  polyId.addActionListener(new PolyActionListener());
  pp.add(polyId);
  
  //the kill small blocks button
    killSmall = new JButton("Kill SmallBlocks");
    killSmall.setBackground(Color.getColor("habplan.block.background").brighter());
    killSmall.addActionListener(new ButtonListener());
    killSmall.setActionCommand("killSmall");
    killSmall.setFont(font);
  

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();    
    }
      });

}/*end constructor*/

/**
*This inner class holds data associated with the block component
*/
class BlockData {
   float size;
   int[] nabes; //the neighbors of the polygon
} /*end inner class*/

/**
*This inner class holds data for GIS display of the block structure
*make an array with one per year
*/
class BlockGIS {
   int nBlocks;  //n of blocks for time t
   int[][] polyID; //name of the member polygons
} /*end inner class*/

/**
* Handle button clicks
**/  
public class ButtonListener implements ActionListener{

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
     
    if (cmd.equals("killSmall")) {
      killSmall.setEnabled(false);  
    }  
    
  }/*end actionperformed()*/
} /*end inner Listener class*/

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
  JPanel killGreen=new JPanel();
  killGreen.setBackground(Color.getColor("habplan.block.background"));
  killGreen.add(killSmall);
  killGreen.add(p4);
  JPanel outPanel=new JPanel();
  outPanel.add(outLabel);
  outPanel.setBackground(Color.black);
  JPanel bp=new JPanel();
  bp.setBackground(Color.getColor("habplan.block.background"));
  bp.add(p5);
  bp.add(p6);
  JPanel orderGoal=new JPanel();
  orderGoal.setBackground(Color.getColor("habplan.block.background"));
  orderGoal.add(orderBox);
  orderGoal.add(p7);
  JPanel midPanel=new JPanel(new BorderLayout());
  midPanel.add(killGreen,BorderLayout.NORTH);
  midPanel.add(bp,BorderLayout.CENTER);
  midPanel.add(orderGoal,BorderLayout.SOUTH);
 

 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new BorderLayout());
  getContentPane().add(midPanel,BorderLayout.NORTH);
  getContentPane().add(outPanel,BorderLayout.SOUTH);
  pack();
 }/*end if*/
 else{
  getContentPane().removeAll();
  getContentPane().setLayout(new BorderLayout());

  JPanel filePanel=new JPanel(new GridLayout(2,1));
  filePanel.add(loadButton);
  filePanel.add(p1);
  JLabel lab1=new JLabel("notBlock Options: regime@period separated by , or ;");
  JPanel blockPanel=new JPanel(new BorderLayout());
  blockPanel.setBackground(Color.getColor("habplan.block.background").brighter());
  blockPanel.add(lab1,BorderLayout.NORTH);
  JScrollPane sPane=new JScrollPane(textBlock);

  blockPanel.add(sPane,BorderLayout.CENTER);
  JPanel topPanel=new JPanel(new BorderLayout());
  topPanel.add(filePanel,BorderLayout.NORTH);
  topPanel.add(blockPanel,BorderLayout.CENTER);

  JPanel botPanel=new JPanel(new BorderLayout());
  botPanel.add(p8,BorderLayout.NORTH);
  botPanel.add(outPanel,BorderLayout.CENTER);
  botPanel.add(pp,BorderLayout.SOUTH);

  getContentPane().add(topPanel,BorderLayout.NORTH);
  getContentPane().add(midPanel,BorderLayout.CENTER);
  getContentPane().add(botPanel,BorderLayout.SOUTH);

  
  pack();
 }/*end else*/
} /*end shrink()*/

/**
*User clicks a checkbox
*/
public class CheckListener implements ItemListener{
 public void itemStateChanged(ItemEvent e){
   firstOrderOnly=orderBox.isSelected();
 }/*end itemStateChanged*/
} /*end inner CheckListener class*/



public class PolyActionListener implements ActionListener{
 public void actionPerformed(ActionEvent event){
   String s;
   int Id=1;
   int j2; //gives the row where the option is found in flow
   parent.console.clear();
    if(!dataRead){
       parent.console.println("First you must enter this component into Objective function");
       return;
   }/*end if !dataRead*/
   
   s=polyId.getText().trim();
  if(!polyTable.containsKey(s)){
      parent.console.println("Can't find this polygon: "+s);
      return;
  }/*fi*/

Id=Integer.parseInt((String)polyTable.get(s));

  s="Block data for Polygon " + s + "\n \n";
   s=s + "Size  Neighbors ... \n"; 
   s=s + data[Id].size + "  ";
   
  for(int j=1; j<data[Id].nabes.length; j++){
    s=s+polyArray.elementAt(data[Id].nabes[j]-1) + " ";   
  }/*end for j*/
  parent.console.println(s);
   
 }/*end actionperformed*/
} /*end inner PolyActionListener class*/

 /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){
	vec[0]=new Boolean(orderBox.isSelected()); //1st order nabes setting
	vec[1]=p1.text.getText(); //filename
	vec[2]=textBlock.getText().trim(); //notBlock options
	vec[3]=p4.text.getText(); //greenup window
	vec[4]=p5.text.getText(); //min size
	vec[5]=p6.text.getText(); //max size
	vec[6]=p7.text.getText();//goal
	vec[7]=p8.text.getText();//weight
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
	orderBox.setSelected( ((Boolean)vec[0]).booleanValue() ); //1st order nabes
	p1.text.setText((String)vec[1]); //filename
	textBlock.setText((String)vec[2]); //notBlock options
	p4.text.setText((String)vec[3]); //greenup
	p5.text.setText((String)vec[4]); //min
	p6.text.setText((String)vec[5]); //max
	p7.text.setText((String)vec[6]);//flow goal
	p8.text.setText((String)vec[7]);//flow weight
     }

/**
*Save users settings to an xml file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
  out.println("<block title=\""+getTitle().trim()+"\">");
  out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
  out.println("  <notBlock value=\""+textBlock.getText().trim()+"\" />");
  out.println("  <greenUp value=\""+p4.text.getText().trim()+"\" />");
  out.println("  <min value=\""+p5.text.getText().trim()+"\" />");
  out.println("  <max value=\""+p6.text.getText().trim()+"\" />");
  out.println("  <goal value=\""+p7.text.getText().trim()+"\" />");
  out.println("  <weight value=\""+p8.text.getText().trim()+"\" />");
  out.println("  <title value=\""+graph.getGTitle()+"\" />");
  Rectangle bds=graph.getBounds();
  out.println("  <bounds height=\""+bds.height+"\" width=\""+bds.width+"\" x=\""+bds.x+"\" y=\""+bds.y+"\" />");
  out.println("</block>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("notBlock"))textBlock.setText(atts.getValue(0));
	if(qName.equals("greenUp"))p4.text.setText(atts.getValue(0));
	if(qName.equals("min"))p5.text.setText(atts.getValue(0));
	if(qName.equals("max"))p6.text.setText(atts.getValue(0));
	if(qName.equals("goal"))p7.text.setText(atts.getValue(0));
	if(qName.equals("weight"))p8.text.setText(atts.getValue(0));
	if(qName.equals("title"))title=atts.getValue(0);
	try{
	    if(qName.equals("bounds") && atts.getLength()>=4){
		int height=Integer.parseInt(atts.getValue(0));
		int width=Integer.parseInt(atts.getValue(1));
		int x=Integer.parseInt(atts.getValue(2));
		int y=Integer.parseInt(atts.getValue(3));
		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
	        if(x>(screen.width-width))x=screen.width-width;
                if(x<1)x=1;
	        if(y>(screen.height-height))y=screen.height-height;
	        if(y<1)y=1;
		graph.setBounds(x,y,width,height);
	    }
	
	    }catch(Exception e){
	    System.out.println("setBounds in GISViewer.readSettings():"+e);
	}
	}catch(Exception e){parent.console.println(getTitle()+".readSettings() "+e);}
    }



 /**
*clear all settings.  Done when user wants to load a new problem without quitting
*/    
 protected void clearSettings(){     
   nNotBlocks=1;  //n of regime@period pairs
   hiddenNotBlocks=""; //so user can see all notBlock options
   badNotBlocks=""; //used to notify users about unknown notBlocks
   notBlocks=null;  //regime@period pairs
 }/*end clearSettings()*/

 /**
     *Find all Regimes that have the given prefix
     *@param prefix - A string containing the regime prefix, begins with )
     *@param regimeVec - Holds the regime@period combinations
     */
    public void getRegimesByPrefix(String prefix, Vector regimeVec){
        prefix=prefix.substring(1); //get rid of leading character
	String[] part=prefix.split("@");
	 prefix=part[0];
	boolean isPeriod=true; //true if a user requested period
	
	String s="";
	for(int j=1; j<=regimeLast; j++){
	    s=(String)(regimeArray.elementAt(j));
	    if(!s.startsWith(prefix))continue;
	    int actions=flowform.regimeActions[j];
	    for(int j2=1;j2<=actions;j2++){
		//parent.console.println(s+"@"+j2);
		if(part.length>1)isPeriod=false;//user has asked for a period
		for(int p=1;p<part.length;p++){
		    if(Integer.parseInt(part[p])==j2){
			isPeriod=true;
			break;
		    }
		}
                  
         	 if(isPeriod) regimeVec.addElement(j+"@"+j2);
           }/*end for j2*/
	}//end for j
    }

 
 /**
 *Read the notblocks from a file or directly from the form
 *at the same time the data are read
 */
 public void readNotBlocks(){
 int regime;  //hold index of regime 
 badNotBlocks="";
 Vector regimeVec=new Vector(10);//temp holder for regime@year strings
 flowData=flowform.data;
 try{
   //read the notblock regime@period pairs
	String text = textBlock.getText().trim();
	//first check to see if this is a file name
	text=readNotBlockFile(text);
	if(text.equals("fail"))text = textBlock.getText().trim(); //it wasn't a file name
	
	String sub="",sub2=""; //use to pull out regimes from the string
	//check for different separators and change to blanks
	if (text.indexOf(";")>0 ||text.indexOf(",")>0){
	 text=text.replace(';',' '); text=text.replace(',',' ');
	 }
	
	String[] item=text.split("\\s+"); //split on blank spaces
	
        for(int i=0;i<item.length;i++){
	  textBlock.setBackground(Color.red);
	  sub=item[i];
	   if(sub.startsWith(")")){//expand regimes that match this pattern
	    getRegimesByPrefix(sub,regimeVec);
	    continue;
	  }

	  sub2=sub.indexOf("-")>0?sub.substring(sub.indexOf("-")+1,sub.length()):sub;
	  sub=sub.indexOf("-")>0?sub.substring(0,sub.indexOf("-")):sub;
   
	   sub2=sub.indexOf("@")>0?sub.substring(sub.indexOf("@")+1,sub.length()):"1";
	   sub=sub.indexOf("@")>0?sub.substring(0,sub.indexOf("@")):sub;
	   
	   String temp=(String)regimeTable.get(sub);
	   if (temp==null)badNotBlocks+=" "+sub+"@"+sub2;
	   else {   
	    temp+="@"+sub2;
	    regimeVec.addElement(temp);
	   } /*fi*/
       }/*end for item*/
       
       int nFromUser=regimeVec.size(); //n of user specified options
       //add the doNothings for year 0 from the flow parent
	 for (int i=0; i<flowform.doNothings.size();i++)
	  regimeVec.addElement(flowform.doNothings.elementAt(i));
	  
	 hiddenNotBlocks = ""; //used to hold regime@period pairs
	 nNotBlocks=regimeVec.size();
	 notBlocks=new int[nNotBlocks+1][3];
       for (int j=1;j<=nNotBlocks;j++){
	sub=(String)regimeVec.elementAt(j-1);
	sub2=sub.indexOf("@")>0?sub.substring(sub.indexOf("@")+1,sub.length()):"1";
	sub=sub.indexOf("@")>0?sub.substring(0,sub.indexOf("@")):sub;
	notBlocks[j][1]=Integer.parseInt(sub);
	notBlocks[j][2]=Integer.parseInt(sub2);
	
	//get pairs ready to be displayed on the console for user to inspect
	sub=(String)regimeArray.elementAt(Integer.parseInt(sub));

	if (j==nFromUser)sub2+="\n"+"autodetected: "; 
	hiddenNotBlocks+=" "+sub+"@"+sub2;//user can view by clicking on notBlock TextArea
       }/*end for j*/ 
       regimeVec=null;
      textBlock.setBackground(Color.white);
      
 }catch(Exception e){}
         
}/*end readNotBlocks()*/

/**
*Try to use the notBlock text as a file name and then
*read the notBlock options from a file.  If this fails
*return text=fail and assume the user entered notBlocks on a form
*@param a possible file name
*@return a string of notBlocks 
*/
private String readNotBlockFile(String fname){
     String text="fail";
     if (fname.equals(""))return text;
    fname=findFile(fname);
    FileReader fin=null;
    LineNumberReader inLine=null;
    
  
try{
  fin =  new FileReader(fname);
  inLine = new LineNumberReader(fin);
  text=inLine.readLine().trim()+";";
   //read to end of file
  while (1==1) text=text+inLine.readLine().trim()+";";
}catch(Exception e){
    if(e instanceof java.lang.NullPointerException) //this happens at end of file
     parent.console.println("Read notBlocks from: " + fname);
     }
finally {  try { if (fin != null) {fin.close();} } catch (IOException e) {} }  
    
    return text;
}/*end readNotBlockFile()*/


/**
*Read the user's input on the component form
*Called when text changes and when the components
*data are read.  This ensures that the input data is read
*even if the form is in shrunk mode
*The notBlock data is read only when the block data file is read
*/
public void readTheForm(){
 
  try{
 blockFile=p1.text.getText().trim();
 window=Integer.parseInt(p4.text.getText().trim());
 minTarget=Integer.parseInt(p5.text.getText().trim());
 maxSize=Integer.parseInt(p6.text.getText().trim());
 goal=Float.valueOf(p7.text.getText().trim()).floatValue();
 firstOrderOnly=orderBox.isSelected();
   }
 catch(Exception e){
   }
}/*end readTheForm()*/


/**
*This transfers GUI input to variablesText
*/
public class ATextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 
  readTheForm();
 
 }/*end textChangedValue*/
} /*end inner ATextListener class*/

    /**
     *set the weight variable and show new setting on form
     */
    public void setWeight(double[] wt){
	weight=wt[0];
        p8.text.setText(String.valueOf(weight));
    }/*end method*/

/**
*This transfers GUI input to weight variable
*which is frequently updated
*/
public class WtTextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 int regime;  //hold index of regime
 
 try{
 weight=Float.valueOf(p8.text.getText().trim()).floatValue();
   }
 catch(Exception e){ 
   }
  
 }/*end textChangedValue*/
} /*end inner WtTextListener class*/

/**
*this lets you click on the notBlock textArea to
*see all the current notBlock options, since
* many may be done automatically and not obvious to user
*/
public class BlockListener implements MouseListener{
 public void mouseClicked(MouseEvent event){
  if (dataRead)readNotBlocks();//reread notBlocks just in case
  else {parent.console.println("Read the Block input data first!");
	 return;
       }
  
    parent.console.println("notBlock options:\n"+hiddenNotBlocks);
    if (!badNotBlocks.equals(""))parent.console.println("\nWARNING: notBlocks absent in Flow Data: "+badNotBlocks);
  }
 public void mousePressed(MouseEvent event){}
 public void mouseReleased(MouseEvent event){}
 public void mouseEntered(MouseEvent event){}
 public void mouseExited(MouseEvent event){}
}/*end inner BlockListener*/



/**
*make sure this form is filled out
*/
public boolean verify(){
  
 if(p1.text.getText().trim().length()<3){
   parent.console.println("No BlockSize datafile given??");
   return(false);
   }/*end if*/
  
try{

 Integer.parseInt(p4.text.getText().trim());
 Integer.parseInt(p5.text.getText().trim());
 Integer.parseInt(p6.text.getText().trim());
 Float.valueOf(p7.text.getText().trim()).floatValue();
 Float.valueOf(p8.text.getText().trim()).floatValue();
 //if (textBlock.getText().equals(""))Integer.parseInt("no Block information");
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the BlockSize form!");
  return(false);
   } 

}/*end verify()*/

/**
*read data from a file
*/

public boolean readData(){ 

   try{ FlowData dummy=(FlowData)flowform.data[1];}
  catch(Exception e){
   parent.console.println("Read the parent flow data first!");
   return(false);
  } /*end catch FlowData*/
  
  
  if (flowform.isSuperFlow){
   parent.console.println("SuperFlow components can't have Block Components!");
   return(false);
  }/*end if isSuperFlow*/
  
  flowData=flowform.data;
  
  //From here down is identical to SpaceForm.readData()

  readTheForm(); //force reading of form settings just in case.
  if (dataRead)return(dataRead); //don't read the data unnecessarily
  dataRead=false; //return this at end
  fileName=blockFile;
  boolean sizeWarning=false; //indicates if a polygon's size is 0
  float size=0; //temp var to hold the size
  String regimeName=""; //hold a regime name
  String element=""; //converted to an integer for the hashTables
  
  int[] temp; //holds data temporarily
  temp=new int[2]; temp[1]=0; //defined here to fool compiler
  nextPolygonSave=nextPolygon; //use to reset polygon numbers if read fails
  polyFirst=-999; polyLast=-999;
  regimeLast=-999;
  
  fileName=findFile(fileName); //class function to filter filenames
  parent.console.println("Reading "+fileName);

   int nLines=0;     // n of lines in file;
   int nPolysRead=0; //n of polys read in file
   maxNabes=0; // max n of neighbors
   int tempNabes=0; // keep track of nabes on multi-line polygon input
   int nabeCnt=0;  //temporarily keep track of n of neighbors within a polygon
   String polyNabe; //holds a nabe name temporarily
   String polyBefore="one", polyNow=""; //previous and current polygonID
   String[] tempNames={"0"}; //need this to hold nabe names below
    

String delimiters=" \t\n\r,;";   
       
  StringTokenizer token;
  BufferedReader in=null;
  String line="";
  
  for (int pass=1;pass<=2;pass++){ //make 2 passes over the data
     
   if (pass==2){
   int deltaPolys=flowform.polyLast-flowform.polyFirst+1 - nPolysRead;
   if (deltaPolys>0)parent.console.println("Error: N of Polys in FlowData - BlockData = " + deltaPolys);
   data=new BlockData[nPolysRead+1];
   for (int i=1;i<=nPolysRead;i++)data[i]=new BlockData();       
   nPolysRead=0;
   tempNames=new String[maxNabes+1];
 }/*end if pass==2*/

  
      nLines=0;
  try{	 
      in = new BufferedReader(new FileReader(fileName),1024);
      
      
    while ((line=in.readLine())!=null){ 
      token=new StringTokenizer(line,delimiters); //all legit delimeters
      int n=token.countTokens(); //n of tokens on this line
      if(n==0)continue; //must be a blank line so skip it
      if(n<2){parent.console.println("ERROR: Too few items after Line "+nLines+"\n"+line);continue;}
      
       polyNow=token.nextToken(); //current polygon id

       if(!polyNow.equals(polyBefore)){ //reading a  new polygon

	 if(pass==1 && !polyTable.containsKey(polyNow)){
	   parent.console.println("Polygon "+polyNow+" not seen in Flow data, \n you must make Block and Flow data conform.");
	 }/*end if !polyTable*/	

         if (pass==1) maxNabes=(maxNabes>(tempNabes))?maxNabes:(tempNabes); //get max n of columns
         tempNabes=0;
	 nPolysRead++;} /*fi*/

        tempNabes+=n-2; //count neighbors over multi-line input
      
	if (pass==2 && polyTable.containsKey(polyNow) && polyFirst==-999){
	    polyFirst=flowform.polyFirst;
	    // polyFirst=Integer.parseInt((String)polyTable.get(polyNow)); //get the internal id
	}
	 

       if (pass==2 & !polyNow.equals(polyBefore)){//store data before reading next Polygon
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  ID=ID-flowform.polyFirst+1; //convert global to local polygon ID
	  data[ID].size=size;
	  data[ID].nabes = new int[nabeCnt+1];
	  
	  for (int i=1; i<=nabeCnt; i++){
	    polyNabe=tempNames[i]; 
	    int nabeID=Integer.parseInt((String)polyTable.get(polyNabe));
	    if(nabeID<flowform.polyFirst | nabeID>flowform.polyLast)parent.console.println("Neighbor on line "+nLines+" Not part of this sub Region\n"+line);
	   data[ID].nabes[i]=nabeID-flowform.polyFirst+1; //local polyId for nabes    
	  }/*end for i*/
	  
	   nabeCnt=0; //reset before reading nabes from next polygon

	}/*end if pass==2 &tempNabes==0*/

       polyBefore=polyNow; //remember polygon name

	 
	String tempSize=token.nextToken(); //size
	try{size=new Double(tempSize).floatValue();
	}catch(Exception es){parent.console.println("ERROR: Check size after line "+nLines+"\n"+line); size=1;}
	if(size==0){sizeWarning=true;size=1;}
    
      if (pass==2){ //read and count non-zero nabes on this line
	  for (int i=1; i<n-1; i++){
	   polyNabe=token.nextToken(); //skip if its a 0
	   if(polyNabe.equals("0.") | polyNabe.equals("0.0") | polyNabe.equals("0")) continue;
	   if(!polyTable.containsKey(polyNabe)){
	       parent.console.println("ERROR: FlowData has no reference to neighbor="+polyNabe+" of Polygon="+polyNow);
	       continue;
	     }/*fi*/
	    nabeCnt++;
	    tempNames[nabeCnt]=polyNabe;  //only retain non-zero legitimate nabes 
	  }/*end for i*/  
	 
      }/*end if pass==2*/

     nLines++;

    }/*end of while loop*/

      if (pass==2 ){//store data from the last polygon
          
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  ID=ID-flowform.polyFirst+1; //convert global to local polygon ID
	  data[ID].size=size;
	  data[ID].nabes = new int[nabeCnt+1];
	  
	  for (int i=1; i<=nabeCnt; i++){
	    polyNabe=tempNames[i]; 
	    int nabeID=Integer.parseInt((String)polyTable.get(polyNabe));
	    if(nabeID<flowform.polyFirst | nabeID>flowform.polyLast)parent.console.println("Neighbor on line "+nLines+" Not part of this sub Region\n"+line);
	   data[ID].nabes[i]=nabeID-flowform.polyFirst+1; //local polyId for nabes    
	  }/*end for i*/
     
	}/*end if pass==2 */
    
    if (pass==2){ 
      //for (int i=1;i<nextRegime;i++) parent.console.println("nextRegime i regimeTable regimeArray "+nextRegime+" "+i+" "+regimeTable.get(Double.toString(i))+" "+regimeArray.elementAt(i-1));
      if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     if(sizeWarning)parent.console.println("Warning: Polygons with size=0 were reset to size=1.");
     parent.console.println("Successfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     flowform.nSubComponents++;
     readNotBlocks(); //now read the notBlocks
     dataRead=true; //if you get here the data was read OK
   } /*end if pass==2*/ 
      
} /*end try*/
   catch (Exception e) {
    parent.console.println("Error: At line "+nLines + ", reading " + fileName + "\n" + e);
    nextPolygon=nextPolygonSave; //restore to old value
    dataRead=false; }
finally {  try { if (in != null) {in.close();} } catch (IOException e) {} }
  
  }/*end pass loop*/
  parent.console.println("Maximum neighbors per polygon = "+maxNabes);
  
  return dataRead;
 }/*end readData()*/
 


/**tell parent component that you're being taken out of the
*objective function
*/
protected void notifyParentOfRemoval(){
 flowform.nSubComponents--;
}/*end notifyParentOfRemoval()*/

 /**
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
 byYrs=flowform.byYrs;  //pointer to byGone yrs
 nByYrs=flowform.nByYrs; //n of byYrs  
 xMin=flowform.xMin;
 xMax=flowform.xMax;
 yrs=xMax-xMin+1;
 if (goal==1) minSize=minIncrement; //make sure min block size is reset
 else minSize=minTarget;  //don't worry too much if goal ne 1
 blockav=new float[yrs+1];  //holds stuff for graphs
 blockmin=new float[yrs+1];
 blockmax=new float[yrs+1];
 areaWithin=new float[yrs+1];
 areaAbove=new float[yrs+1];
 areaBelow=new float[yrs+1];

 sizeVec=new int[3];
 nPositive=0;
 if (nNotBlocks==0)parent.console.println("BlockSizes can't be adjusted without notBlock Options!");
} /*end init()*/ 
/**
*Compute Objective Function change when one unit changes
*its management regime.  This one is combining flow and a link
*to time 0, so it's almost like 2 components in one.
*@param members contains indices of member polys, z is the new regime
*/
public double deltaObj(int[] members, int z){
   
    double delta=0.0;

    for(int mem=0;mem<members.length;mem++){ //loop through the members
	int ig=members[mem]; //process one member of this unit at a time
	if(ig==0)continue; //no data for this member

	if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
   int i=ig-polyFirst+1; //transform global to local polygon index
   int  xmax=0,xmin=1000000,zmax=0,zmin=1000000;//too bad this looks like xMin xMax
   float xdev=0, zdev=0; 
   int sched=0, t;
   boolean isNothing,//true means a non-Block option
	   isZNothing; //z proposal is non-Block option if true   
  int row=flowData[i].flowPtr(polySched[ig]);
  int rowz=flowData[i].flowPtr(z);
  int fsizeX=(flowData[i].flow[row].length-1)/2;
  int fsizeZ=(flowData[i].flow[rowz].length-1)/2;
  int fsize=Math.max(fsizeX,fsizeZ);	   
	  
   
    
   /*xmax and zmax are size of biggest block containing i under current or z*/
  for (int j=1;j<=fsize;j++){ /*do this for each output year*/
     isNothing=false;
     isZNothing=false; 
     sched=polySched[ig]; //use global index for schedule
      
    for(int j2=1;j2<=nNotBlocks;j2++){
      if (polySched[ig]==notBlocks[j2][1] && j==notBlocks[j2][2])
	       { sched=z; isNothing=true;} //use t  for z if x is notblock
      if (z==notBlocks[j2][1] && j==notBlocks[j2][2])isZNothing=true;
     /*end for j2*/} 

    //whenever year=0, treat it as a notBlock 
    //this is useful for habread generated data
    //also when regimes have different meanings for each polygon  
    if(j<=fsizeX && flowData[i].flow[row][j]==0)isNothing=true;
    if(j<=fsizeZ && flowData[i].flow[rowz][j]==0)isZNothing=true;

    if (isZNothing && isNothing)continue; //z and x are not-Blocks, so skip

    
     if ((!isNothing && j<=fsizeX) || (isNothing && j<=fsizeZ)){ //handle variable period regimes     
     //Get xmin and xmax under x at time t(x) 	       
     row=flowData[i].flowPtr(sched);  /*the correct row in the FLOW matrix*/
     t=flowData[i].flow[row][j]-xMin+1; /*scaled year*/
     t=(t>0)?t:0;  //make sure doNothings stay at 0, not negative indices

     //handle preCut stand
     if(flowData[i].flow[row][j]<0)t=xMin+flowData[i].flow[row][j];
     blocksize(i, polySched[ig],t,isNothing,sizeVec); 
      if (sizeVec[1]==0)sizeVec[1]=minSize; //lone doNothing is not a minSize violation
      xmin=(xmin<sizeVec[1])?xmin:sizeVec[1];
      xmax=(xmax>sizeVec[2])?xmax:sizeVec[2];
     }//end if variable multiperiod regimes 
      
     //Get zmin and zmax after going to z at t(z)      
     int xold=polySched[ig];
     polySched[ig]=z;
     sched=z;
     if (isZNothing)sched=xold; //use t(x) if z is notblock
     if ((!isZNothing && j<=fsizeZ) || (isZNothing && j<=fsizeX)){ //handle variable period regimes      
      row=flowData[i].flowPtr(sched);  /*the correct row in the FLOW matrix*/
      t=flowData[i].flow[row][j]-xMin+1; /*year*/ 
      t=(t>0)?t:0;  //make sure doNothings stay at 0, not negative indices 

      //handle preCut stand
     if(flowData[i].flow[row][j]<0)t=xMin+flowData[i].flow[row][j];
      blocksize(i,polySched[ig],t,isZNothing,sizeVec);
      if (sizeVec[1]==0)sizeVec[1]=minSize; //lone doNothing is not a minSize violation
      zmin=(zmin<sizeVec[1])?zmin:sizeVec[1];
      zmax=(zmax>sizeVec[2])?zmax:sizeVec[2];
     }//end if variable multiperiod regimes 
   
     // Get zmin zmax at t(x) after switching to z
    if (!isZNothing && j<=fsizeX){ 
      sched=xold;  
      row=flowData[i].flowPtr(sched);  /*the correct row in the FLOW matrix*/
      t=flowData[i].flow[row][j]-xMin+1; /*scaled year*/ 
      t=(t>0)?t:0;  //make sure doNothings stay at 0, not negative indices   
      blocksize(i,polySched[ig],t,false,sizeVec);
      if (sizeVec[1]==0)sizeVec[1]=minSize; //lone doNothing is not a minSize violation
      zmin=(zmin<sizeVec[1])?zmin:sizeVec[1];
      zmax=(zmax>sizeVec[2])?zmax:sizeVec[2];
    } /*end if !isNothing*/ 
      polySched[ig]=xold;  //restore the x schedule
       
     
//if(parent.iter>2)parent.console.println("j x z xmax xmin zmax zmin "+j+" "+polySched[ig]+" "+z+" "+xmax+" "+xmin+" "+zmax+" "+zmin);     
   } /*end for j=1*/ 
   
    //If x and z blocks exceed the max, take the smaller one.
    //If x and z blocks are under the min then take either.
    //this allows for easier elimination of isolated clusters
    //of small stands
   
    //this stuff should work with more than 1 output year
   xdev=(xmax>maxSize)?xmax-maxSize:0;
   zdev=(zmax>maxSize)?zmax-maxSize:0;
   
   
   if (1==0 && converged==1){ //take x or z if they're both under the min
    xdev+=((xmin<minSize)?(1):0);
    zdev+=((zmin<minSize)?(1):0);
   }
   else{ //when not converged take the one closest to the min
   xdev+=((xmin<minSize)?(minSize-xmin):0);
   zdev+=((zmin<minSize)?(minSize-zmin):0); 
   }
   
   //This will force a change to doNothing on the second pass from
   //FrameMenu.metropolis() even if zmin<minSize. If zmin>minSize 
   //on pass 2, then the usual processes will try to make the change.
   if (killSmallBlocks && xmin<minSize && zmin<minSize){ //when user pushs kill button
       if( makeDoNothing(ig)) return(Double.MAX_VALUE); //set to doNothing if valid    
   }/*end if killSmallBlocks*/
   
   if (xdev==zdev)delta=delta;
   else if (xdev>zdev)delta+=weight;  //z better than x
   else if (xdev<zdev)delta-=weight; //x better than z
   
   

//if (delta==0.)parent.console.println("i xdev zdev "+i+" "+xdev+" "+zdev);

 if (delta>0.)nPositive++; //use for adjusting weight;
 nTotal++;
 }/*end for mem loop*/

 return (delta);
}/*end deltaObj()*/

/**
*Set polygon ig to doNothing if its a valid regime
*Set zProp in FrameMenu.metropolis() to doNothing, then make
*a second pass thru all components
*This is used to eliminate blocks smaller than minSize
*and is called from this.deltaObj()
*@param ig is the global polygon id
*@return true if polygon ig was set to doNothing
*/
public boolean makeDoNothing(int ig){
 //determine if theDoNothing option is valid
 for (int i=1;i<=nScheds;i++)
   if (validScheds[i]==flowform.theDoNothing){
	parent.met.setZProp(flowform.theDoNothing); //change proposal schedule in parent.metropolis() via bad programming style, note: met extends Algorithm
        return true;
   } /*end if*/	
   
 //try a second pass for polygons that don't allow theDoNothing option
 //In particular, polygons with multiperiod regimes that include a byGone year 
 //would not allow all periods of doNothing but could be part of small blocks  
 int polyNum=ig-polyFirst+1,  //adjust from global to local polygon numbering system
     aDoNothing=1, //this will be set to a regime that is doNothing for some periods if possible
     ind=0; // indicates if all periods are doNothing 
search2:     
for (int i=1;i<=nScheds;i++){

 for(int j=1; j<=regimeLast; j++){ 
  ind=0; 
  if(j!=validScheds[i])continue;   
  int row=flowform.data[polyNum].flowPtr(j);  /*the correct row in the FLOW data matrix for regime j*/
  if (row==0)continue; //this polygon doesn't have this option, but it should since its valid
   int fsize=(flowform.data[polyNum].flow[row].length-1)/2; //n of yrs of output
      
  for (int j2=1;j2<=fsize;j2++){
   int t=flowform.data[polyNum].flow[row][j2];
   if(t==0){ind=1; aDoNothing=j; break search2;} //lets hope that this doNothing period fixes the small block
  }/*end for j2*/ 
 }/*end for j*/
}/*end for i*/
 
   if (ind==1){
	parent.met.setZProp(aDoNothing); //change proposal schedule in parent.metropolis() via bad programming style.
        return true;
   } /*end if*/	

    //let the user know if some polygons cant be set to doNothing
   String s1=(String)ObjForm.polyArray.elementAt(ig-1);
       try{
	 if (Double.valueOf(s1).intValue()==Double.valueOf(s1).doubleValue()) //check if integer
	  s1=Integer.toString(Double.valueOf(s1).intValue());
       } catch (Exception es){/*not a number*/;} 
   String s2=(String)regimeArray.elementAt(polySched[ig]); 
   String s3=(String)regimeArray.elementAt(flowform.theDoNothing);
  parent.console.println("Small Block Polygon: "+s1+" with regime "+s2+" doesn't allow regime "+s3); 
 return false; 
}/*end makeDoNothing(i)*/

 /**
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){
  float Rate=goal;
 double Scale=weight;
 
 if (!killSmall.isEnabled() && !killSmallBlocks){
    killSmallBlocks=true;  killSmall.setLabel("WORKING"); minIters=1;}
 else {
  killSmallBlocks=false; killSmall.setEnabled(true); killSmall.setLabel("Kill SmallBlocks");}
 
 double Low=0.,Hi=1.,ratio=0.;
  ratio=(1.-(float)nPositive/nTotal);
  //parent.console.println("nPositive nTotal ratio "+nPositive+" "+nTotal+" "+ratio);
  nPositive=0; //reset for next iteration
  nTotal=0;
  
 Hi=(Rate+.05);
 Hi=(Hi<1.)?Hi:1.;
 Low=(Rate==1)?Rate:(Rate-.05);  //special consideration when user wants Rate=1
 Low=(Low>0.)?Low:0.;
  
 Scale=(float)((ratio>Hi)?Scale*.9:Scale); 
 Scale=(float)((ratio<Low)?Scale/.9:Scale);
 
 //for BlockForm, converged is set to 1 if z was never better than x
 //even if the block size was out of bounds to avoid increasing the
 //weight when if wont help.  However, at end of postIter(), 
 //converged is set to 0 when blocks are out of bounds to make fitness
 //function perform properly
 if (Scale==weight)converged=1; //indicates convergence
 else if (Scale<weight)converged=2; //overConverged
 else converged=0;   //underConverged  
  
  if(Scale<minValue)Scale=minValue;
 else if(Scale>Float.MAX_VALUE){
     isWt2big=true;
     Scale=Float.MAX_VALUE;
 }
 
  weight=Scale; 
  goalValue=ratio; //used by fitness function
  ratio=Math.floor(ratio*100)/100.;
    
    int globalMin=10000000, globalMax=0; 
    float[] xx=new float[yrs+1];
  for (int t=1;t<=yrs;t++){ /*recompute the blocksizes and stats*/
    hbp_blkt(t);
    xx[t]=t+xMin-1; 
    bySkip=false;for (int b=1;b<=nByYrs;b++)if(t==byYrs[b])bySkip=true; //Is it a byGone yr
     if(bySkip)continue; //dont count byGone years for mins and maxes
    if(blockmin[t]<globalMin)globalMin=(int)blockmin[t];
    if(blockmax[t]>globalMax)globalMax=(int)blockmax[t];
  }//end for t
  
   if(globalMin>=minSize && minSize<minTarget){
    minSize+=minIncrement; //incrementally increase the min blocksize
    if (parent.isImport)minSize=globalMin+1; //adjust minSize for imported schedules
   }else minIters++;
   if (minSize>minTarget)minSize=minTarget;//minSize shouldn't be larger than target
   if (goal<1)minSize=minTarget; //user isn't that concerned with blockSize
   if (minIters%50==0 && globalMin<minSize && minSize<minTarget)killSmall.setEnabled(false); //kill blocks less than current min
   
  //write some output 
  outLabel.setText(" Actual Goal: " + ratio + "  Min: "+globalMin+" Max: "+globalMax+"  Target Min:"+minSize); 
  p8.text.setText(String.valueOf((float)weight));
  
  if (graph.isVisible()){   
   graph.ag[1].plot(xx,blockmax,"Max",.05,.8);
   graph.ag[1].plotHold(xx,blockmin,"Min",.35,.8); 
   graph.ag[1].plotHold(xx,blockav,"Mean",.65,.8); 
   graph.ag[2].plot(xx,areaWithin,"Within",.05,.8);
   graph.ag[2].plotHold(xx,areaAbove,"Above",.35,.8); 
   graph.ag[2].plotHold(xx,areaBelow,"Below",.65,.8); 

    if (!isColorsLoaded){
	  isColorsLoaded=true; graph.loadColors(); //make sure it looks like user wants
	  if (xMax-xMin<20){
	   graph.ag[1].xWrite.setTics(5); graph.ag[2].xWrite.setTics(5);
	   graph.ag[1].xTics.setTics(5); graph.ag[2].xTics.setTics(5);
	   }
      }/*end if isColorsLoaded*/
     graph.repaint();   
  }/*end if graph.isVisible()*/ 
  
  if (globalMin<minSize || globalMax>maxSize)converged=0;
}/*end postIter()*/

/**
BLOCKSIZE returns the size of the largest and smallest blocks
that polygon i is a member of under the current schedule in SCHED
for year t which is indexed by j.  This involves calling hbp_blk1
for years t thru t+window.  This is used to find the impact of
changing the management option of polygon i over the entire greenup
window on max and min block size
*@param i = polygon, sched1 = management option, t is the year
*@param isNothing tells if a notBlock option, sizeVec is the return
*/

protected final void blocksize(int i, int sched, int t, boolean isNothing, int[] sizeVec)
{

int tHi=Math.min(t+window,yrs);

int max=0, min=1000000;

   for (int t2=t; t2<=tHi; t2++){  /*get all blocks containing this polygon*/
    hbp_blk1(i,sched,t2,isNothing,sizeVec); //for notBlocks t2 comes from sched2
   
    //sizeVec=0 when its an isolated do-Nothing polygon
    if (sizeVec[1]!=0){
     min=(sizeVec[1]<min)?sizeVec[1]:min;
     max=(sizeVec[2]>max)?sizeVec[2]:max;
    }/*end if*/
   // parent.console.println("i sched1, t2 min max "+i+" "+sched1+" "+t2+" "+min+" "+max);
    }/*end for t2*/
    sizeVec[1]=(min==1000000)?0:min;
    sizeVec[2]=max; 
   // parent.console.println("final min max "+sizeVec[1]+" "+sizeVec[2]);
 
return;

}/********************end blocksize()*******************/


/** hbp_blk1:
Compute the size of the block that contains polygon
target (given by polyId) for year t.  The target's schedule
could involve management in more than one year, so call
this routine for each possible t.  The block consists of
all stands linked to target either directly or by
neighbors, all of which were managed in years
t-window,...,t under an option that contributes to
blocksize.  The blocksize for target is returned, given the
current management schedule for the neighbors.
*/

protected final  void hbp_blk1(int polyId, int sched, int t, boolean isNothing, int[] sizeVec)
 /*compute and return the average blocksize for year t */
{

int i,j,j2, k,ls,lb,size;
int nnabes;
int[] nabesi=new int[maxNabes+1]; //vector of neighbors for poly i
     
ls=(int)(polyLast-polyFirst+1)/3;  /*hope no more than this many in the block*/
int block1[]=new int[ls*3];  //list of all polys in the block

  block1[1]=polyId;
  lb=1; /*initial length of block1*/
  i=1;
 while(i<=lb){  /*this loop gets all polygons in block*/
  nnabes=validNabes(block1[i],t,nabesi); /*check window and NOTBLOCK*/
  i=i+1;
  lb=unique(block1, nabesi,lb, nnabes); /*add new polys to block1*/
  if(firstOrderOnly)break;  //only use first order nabes
 } /*end while i<=lb*/
// parent.console.println("i block1[1] block1.length "+polyId+" "+block1[1]+" "+lb);

 
 if (!isNothing){//only 1 block if target is not a do-nothing option 
  size=0; for (i=1;i<=lb;i++){size+=data[block1[i]].size;} /*size of block*/ 
  sizeVec[1]=size; sizeVec[2]=size;
  return;
  } 
  
  if (lb==1 && isNothing){sizeVec[1]=0; sizeVec[2]=0; return;}//a do-nothing with no neighbors
  
  //only go here if target could have multiple blocks like for a doNothing
   int min=10000000, max=0;
  int s1=lb; 
  int block2[]=new int[s1+maxNabes];  //I added the maxNabes so it works with firstOrderOnly, even when it shouldn't be used, since a user might do it anyway.
  while (s1>1){
   block2[1]=block1[2]; //first neighbor of target, excludes doNothing target
   lb=1; /*initial length of block2*/
   i=1;
 while(i<=lb){  /*this loop gets all polygons in block*/
  nnabes=validNabes(block2[i],t,nabesi); /*check window and NOTBLOCK*/
  i=i+1;
  lb=unique(block2, nabesi,lb, nnabes); /*add new polys to block2*/
   if(firstOrderOnly)break;  //only use first order nabes
 } /*end while i<=lb*/
 
  size=0; for (i=1;i<=lb;i++) size+=data[block2[i]].size;
  
  min=(min>size)?size:min;
  max=(max<size)?size:max;   
  if(firstOrderOnly)break;
  s1=notunique(block1,block2,s1,lb);
   } /*end while(s1>1)*/
 sizeVec[1]=min;
 sizeVec[2]=max;
  return;
} /**************end of hbp_block1*****************/
    

/**
*hbp_blkt: compute all the block sizes for year t.  These are used for
*	    computing min, max, & ave blocksize by year.  Blocks are uniquely
*	    identified by the lowest numbered member polygon  that was
*	    cut in year t, say polygon i. The block consists of all stands
*            linked to i either directly or by neighbors, all of which were
*            managed in years t-window,...,t under an option that contributes
*            to blocksize.
*            Average, max, & min blocksize for year t are stored in
*	     blockav[], blockmax[], blockmin[];
*            Areas above below and within user specified min and max are 
*            stored in areaAbove[], areaBelow[] and areWithin[]
*/


protected final void hbp_blkt(int t) {

float size; 
int fsize;  //n of yrs of output;
int i,k,ls,lb=0,skip,row,
y,          /*holds management year*/
nnabes;    /*n of neighbors of polygon i returned by getnabes*/
int[] nabesi=new int[maxNabes+1]; //vector of neighbors for poly i

ls=(int)(polyLast-polyFirst+1)/1;  /*hope no more than this many cut in yr t*/
int stds[]=new int[ls+1]; //stands cut on year t;
int[] blockinc=new int[ls+1];
float sizelist[]=new float[ls+1];
blockav[t]=0f;
blockmin[t]=0f;
blockmax[t]=0f;

/*get an ordered list of stands cut in year t and not a notblock option*/
  k=1;
for (int ig=polyFirst;ig<=polyLast;ig++){
   i=ig-polyFirst+1;
   row=flowData[i].flowPtr(polySched[ig]);  /*the correct row in the FLOW matrix*/
   fsize=(flowData[i].flow[row].length-1)/2;
 for (int j=fsize;j>=1;j--){ 
       y=flowData[i].flow[row][j]-xMin+1; /*scaled management year*/
   if(y==t){
      skip=0; for(int j2=nNotBlocks;j2>=1;j2--)
	if (polySched[ig]==notBlocks[j2][1] && j==notBlocks[j2][2]){ skip=1;break;}
      if (skip==0){ stds[k]=i; k++;}
   } /*end if y==t*/
  if (k>=ls)parent.console.println("hbp_blockt: too many stands cut in year "+t+" call a Habplan programmer!");
 } /*end for j*/
}  /*end for i*/

ls=k-1;  /*number of stands cut in year t*/
if (ls==0){return;}  /*then there's nothing to do*/

 gisData.polyID=new int[ls+1][]; //max number of possible blocks in year t
 k=1; /*count the number of blocks*/

while (ls>0) {
  blockinc[1]=stds[1];
  lb=1; /*initial length of blockinc*/
  i=1;
 while(i<=lb){  /*this loop gets all polygons in block indexed by stds[1]*/
  nnabes=validNabes(blockinc[i],t,nabesi); /*check window and NOTBLOCK and return valid neighbors in nabesi*/
  i=i+1;
  lb=unique(blockinc, nabesi,lb, nnabes); /*add new polys to blockinc*/
  if(firstOrderOnly)break;  //only use first order nabes
 } /*end while i<=lb*/
   gisData.polyID[k]=new int[lb+1]; //hold members of block k
   size=0; for (i=lb;i>=1;i--){
   size=size+data[blockinc[i]].size; /*size of block*/
   gisData.polyID[k][i]=blockinc[i];
   //parent.console.print(blockinc[i]+" ");
   }
   //parent.console.println(" stds[1] t size "+stds[1]+" "+t+" "+size);
   sizelist[k]=size; k++;
   ls=notunique(stds,blockinc,ls,lb); /*remove polygons that cant be anchors*/
} /*end while ls>0*/

float blockt=0; /*holds average block size*/
float bmax=0; /*temp var to hold max block size*/
float bmin=Float.MAX_VALUE;
k=k-1; /*the number of unique blocks in year t*/

gisData.nBlocks=k;

areaAbove[t]=0;//zero the area of blocks above limits
 areaBelow[t]=0;
 areaWithin[t]=0;

 for(i=1;i<=k;i++){ /*stat the block sizes*/
  blockt=blockt+sizelist[i];
  if (sizelist[i]>bmax)bmax=sizelist[i];
  if (sizelist[i]<bmin)bmin=sizelist[i];
   if(sizelist[i]>maxSize)areaAbove[t]+=sizelist[i];
  else if(sizelist[i]<minTarget)areaBelow[t]+=sizelist[i];
  else areaWithin[t]+=sizelist[i];
 }/*end for i*/
this.blockav[t]=blockt/k; /*average block size*/
this.blockmax[t]=bmax; /*max block size*/
this.blockmin[t]=bmin; /*max block size*/

return;
} /*******************end of hbp_blockt*******************/


/**
put the unique elements of vec1 and vec2 into vec1, vec1 is size s1,
and vec2 is size s2, note that vec1  must be long enough to hold the 
extra elements from vec2.  The new length of vec1 is returned.
*/

protected final int unique(int vec1[], int vec2[],int s1, int s2){
int test,news1;
 int j2=1;
for (int i=1;i<=s2;i++){
  test=0; for (int j=1;j<=s1;j++) if(vec1[j]==vec2[i]) test=1;
  if (test==0){ vec1[s1+j2]=vec2[i]; j2++;}
}/*end for i*/
news1=s1+j2-1;

return(news1);
}/**********************end unique********************/


/**
remove non-unique elements of vec1 that are in vec2, vec1 is size s1,
and vec2 is size s2.  The new length of vec1 is returned.
*/

protected final int notunique(int vec1[], int vec2[],int s1, int s2){

int test,news1;
int[] temp=new int[s1+1]; /*temporarily holds the unique elements*/
 
 int j2=1;
for (int i=1;i<=s1;i++){
  test=0; for (int j=1;j<=s2;j++) if(vec1[i]==vec2[j]) test=1;
  if (test==0){ temp[j2]=vec1[i]; j2++;}
}/*end for i*/
news1=j2-1; /*the new size of vec1*/
 /**now put temp into vec1**/
for (int i=1;i<=news1;i++)vec1[i]=temp[i];

return(news1);
}/*******************end notunique************************/


/**
*keep only the valid neighbors in nabesi, i.e. they must be cut
*within the years t-window thru t, and not in NOTBLOCKS with is of
*length nNotBlocks, return nnabes
*/
protected final int validNabes(int polyId, int t, int nabesi[])
{
int row, skip=1, skipt=1,
y; /*the management year*/
int nnabes=data[polyId].nabes.length-1;
if (nnabes==0) return(0); /*no neighbors*/
int temp[]=new int[nnabes+1]; /*temp storage for the good neighbors*/
int fsize; //n of yrs of output;
System.arraycopy(data[polyId].nabes,1,nabesi,1,nnabes);

 int k=1;
  for (int i=1;i<=nnabes;i++){
      row=flowData[nabesi[i]].flowPtr(polySched[nabesi[i]+polyFirst-1]);  /*the correct row in the FLOW matrix*/
      fsize=(flowData[nabesi[i]].flow[row].length-1)/2;
 jLoop:     
  for (int j=1;j<=fsize;j++){ 
        y=flowData[nabesi[i]].flow[row][j]-xMin+1; /*scaled management year*/
       skipt=1; for (int time=t-window;time<=t;time++)
                if(y==time){skipt=0;  break;}  /*its in the time window*/ 
       if(y+xMin-1==0)skipt=1; //anything with year=0 is a notBlock

   if (skipt==0){
      skip=0;
       for(int j2=nNotBlocks;j2>=1;j2--)
	if (polySched[nabesi[i]+polyFirst-1]==notBlocks[j2][1] && j==notBlocks[j2][2])
	 {skip=1; break;}             
      if (skip==0){ temp[k]=nabesi[i]; k++; break jLoop;}//not a notBlock
   } /*end if skipt==0*/
 } /*end for j*/ 
}/*end for i*/
nnabes=k-1;
System.arraycopy(temp,1,nabesi,1,nnabes);

return(nnabes);
}/*******************end validNabes()************************/

 
} /*end BlockForm class*/
