import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import figure.*;
import org.xml.sax.Attributes;

/**
*Make an Objective Function Flow Component form
*for controlling clearcut flow.  This could actually
*control the flow of any group of management regimes,
*say thinning.
*Contains all the methods needed for the flow component.
*@author Paul Van Deusen (NCASI)  1/97
*/
public class CCFlowForm extends ObjForm {
   JFileButton loadButton;
   JLabelTextField p6;
   JLabelTextScroll p4,p5;
   JTextArea textBlock; //hold regimes@years that  contribute to CC blocksize
   JTextField polyId; //enter an id to check the data
   JLabel outLabel; //output goes here
   JPanel pp, bPanel; //look at data panel, and button Panel
   FlowForm flowform; //the FlowForm parent that holds polygon data
   int xMin, xMax; //min and max for xAxis of graphs
   
   int[] byYrs;  //pointer to byGone yrs
   int nByYrs; //n of byYrs  
   boolean bySkip=false;  //used to decide if a byYear should be skipped in deltaObj()  
   float wtFactor=1000; //used to scale the weight so 1.0 is in ballpark
  
    //userInput CCFlow component variables
  protected String ccFlowFile;
  protected int nCCOpts;
  protected int[][] ccOpts; //regime@period CC block pairs
  protected float  goal;  //preferred yr to yr deviation
  protected float ccRate;  // interest rate for flow trend
   
   //variables related to the objective function
   SubFlowData[] data;  //inner class to hold ccFlow data, easy to expand later
   FlowData[] flowData; //data in parent flowform
   float[] gama, gamaz, g;
   
/**
*indicates whether  component has a Graph associated with it
*/
public boolean isGraph(){ return(true);}

public CCFlowForm(FrameMenu parent, String s, FlowForm flowform){
  super(s, 9, 1);
  this.parent=parent;
  this.flowform=flowform;
  graph = new Figure(s,2,1,UserInput.hbpDir);
  getContentPane().setBackground(Color.black);
  p1=new JLabelTextField("CCFLOW fileName",20,"str");
  p1.setBackground(Color.getColor("habplan.ccflow.background"));
  p1.text.addCaretListener(new ATextListener());
  //TextArea for entering regime@year
  Font font = new Font("Monospaced", Font.BOLD, 12);
  textBlock=new JTextArea("",3,10);
  textBlock.addMouseListener(new BlockListener());
  textBlock.setFont(font);
  textBlock.setBackground(Color.white);
 
  
  p4=new JLabelTextScroll("Goal",4,0,100,100);
  p4.setBackground(Color.getColor("habplan.ccflow.background"));
  p4.text.addCaretListener(new ATextListener());
  p4.text.setText("0");
  p5=new JLabelTextScroll("ccFlow Model Slope Rate",4,-100,100,100);
  p5.setBackground(Color.getColor("habplan.ccflow.background"));
  p5.text.addCaretListener(new ATextListener());
   p5.text.setText("0");
  p6=new JLabelTextField("Weight",8,"float");
  p6.setBackground(Color.getColor("habplan.ccflow.background"));
  p6.text.addCaretListener(new WtTextListener());
  p6.text.setText("1");
  
  //output label
  outLabel=new JLabel();
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  loadButton= new JFileButton("File Selector",parent,p1.text,null,null);
  loadButton.setBackground(Color.getColor("habplan.ccflow.background"));
  
  //create a data verify field
  pp = new JPanel();
  pp.setBackground(Color.getColor("habplan.ccflow.background"));
  JLabel verify = new JLabel("Verify data for Polygon #");
  verify.setBackground(Color.getColor("habplan.ccflow.background"));
  pp.add(verify);
  polyId = new JTextField(" ",4);
  polyId.setBackground(Color.lightGray);
  polyId.addActionListener(new PolyActionListener());
  pp.add(polyId);
  
      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

/**
*This inner class holds data associated with the ccFlow component
*/
class SubFlowData {
   float size;
} /*end inner class*/

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(3,1));
  getContentPane().add(p4);
  getContentPane().add(p5);
  getContentPane().add(outLabel);
  pack();
 }/*end if*/
 else{
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(9,1));
  getContentPane().add(loadButton);
  getContentPane().add(p1);
  JLabel lab1=new JLabel("CCFlow Options: regime@year separated by , or ;");
  JLabel lab2=new JLabel("Alternative approach: )PREFIX");
  JPanel labPanel= new JPanel(new GridLayout(2,1,0,0));
  labPanel.setBackground((Color.getColor("habplan.ccflow.background")).brighter());
  labPanel.add(lab1);
  labPanel.add(lab2);
  getContentPane().add(labPanel);
  JScrollPane sPane=new JScrollPane(textBlock);
  getContentPane().add(sPane);
  getContentPane().add(p4);
  getContentPane().add(p5);
  getContentPane().add(p6);
  getContentPane().add(outLabel);
  getContentPane().add(pp);
  pack();
  shrunk=false;
 }/*end else*/
} /*end shrink()*/


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
  s="Size of Polygon " + s + " = ";
  s=s + Float.toString(data[Id].size);
  parent.console.println(s);
   
 }/*end actionperformed*/
} /*end inner PolyActionListener class*/

 /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){
	vec[0]=p1.text.getText(); //filename
	vec[1]=textBlock.getText(); //CCFlow options
	vec[2]=p4.text.getText(); //goal
	vec[3]=p5.text.getText(); //slope
	vec[4]=p6.text.getText(); //weight
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
	p1.text.setText((String)vec[0]); //filename
	textBlock.setText((String)vec[1]); //CCFlow options
	p4.text.setText((String)vec[2]); //goal
	p5.text.setText((String)vec[3]); //slope
	p6.text.setText((String)vec[4]); //weight
     }

/**
*Save users settings to an xml file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
  out.println("<ccflow title=\""+getTitle().trim()+"\">");
  out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
  out.println("  <ccOptions value=\""+textBlock.getText().trim()+"\" />");
  out.println("  <goal value=\""+p4.text.getText().trim()+"\" />");
  out.println("  <slope value=\""+p5.text.getText().trim()+"\" />");
  out.println("  <weight value=\""+p6.text.getText().trim()+"\" />");
  out.println("  <title value=\""+graph.getGTitle()+"\" />");
  Rectangle bds=graph.getBounds();
  out.println("  <bounds height=\""+bds.height+"\" width=\""+bds.width+"\" x=\""+bds.x+"\" y=\""+bds.y+"\" />");
  out.println("</ccflow>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("ccOptions"))textBlock.setText(atts.getValue(0));
	if(qName.equals("goal"))p4.text.setText(atts.getValue(0));
	if(qName.equals("slope"))p5.text.setText(atts.getValue(0));
	if(qName.equals("weight"))p6.text.setText(atts.getValue(0));
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
     *Find all Regimes that have the given prefix, begins with )
     *@param prefix - A string containing the regime prefix
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
     *Read the CC regimes from the form
     *
     */
    public void readCCRegimes(){
	 int regime; //holds index of management regimes
  Vector regimeVec=new Vector(10);//temp holder for regime@year strings
        try{
	    //read the CC block regime@period pairs
	String text = textBlock.getText().trim();

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
     try{ //check for integer denoted single year regimes
	  for (int ii=Integer.parseInt(sub);ii<=Integer.parseInt(sub2);ii++){
	   regimeVec.addElement(regimeTable.get(Integer.toString(ii)));
	  } /*end for ii*/
	  }/*end try integer regimes*/
	 catch(Exception e0){ /*must be non-integer name and/or multi-year regime*/
	   sub2=sub.indexOf("@")>0?sub.substring(sub.indexOf("@")+1,sub.length()):"1";
	   sub=sub.indexOf("@")>0?sub.substring(0,sub.indexOf("@")):sub;
	   String temp=(String)regimeTable.get(sub)+"@"+sub2;
	   regimeVec.addElement(temp);
	 } /*end catch of string regime names*/ 
       }/*end for item*/
	 nCCOpts=regimeVec.size();
	 ccOpts=new int[nCCOpts+1][3];
       for (int j=1;j<=nCCOpts;j++){
	sub=(String)regimeVec.elementAt(j-1);
	sub2=sub.indexOf("@")>0?sub.substring(sub.indexOf("@")+1,sub.length()):"1";
	sub=sub.indexOf("@")>0?sub.substring(0,sub.indexOf("@")):sub;
	//parent.console.println("j sub sub2 "+j+" "+sub+" "+sub2);
	ccOpts[j][1]=Integer.parseInt(sub);
	ccOpts[j][2]=Integer.parseInt(sub2);
       }/*end for j*/
       regimeVec=null;
	   textBlock.setBackground(Color.white);
	}
	catch(Exception e){ parent.console.println("ReadCCRegimes error");  }

    }

/**
*The reads the user's input on the component form
*It is called when text changes and when the components
*data are read.  This ensures that the input data is read
*even if the form is in shrunk mode
*/
public void readTheForm(){
 
 try{
 ccFlowFile=p1.text.getText().trim();
 goal=Float.valueOf(p4.text.getText().trim()).floatValue();
 ccRate=Float.valueOf(p5.text.getText().trim()).floatValue();
   }
 catch(Exception e){   }
}/*end readTheForm()*/

/**
*This transfers user input to UserInput class
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
        p6.text.setText(String.valueOf(weight));
    }/*end method*/

/**
*This transfers user input to the weight variable
*which is frequently updated
*/
public class WtTextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 
 try{
 weight=Float.valueOf(p6.text.getText().trim()).floatValue();
   }
 catch(Exception e){   }
  
 }/*end textChangedValue*/
} /*end inner WtTextListener class*/



/**
*make sure this form is filled out
*/
public boolean verify(){
  
 if(p1.text.getText().trim().length()<3){
   parent.console.println("No CCFlow datafile given??");
   return(false);
   }/*end if*/
  
try{

 Float.valueOf(p4.text.getText().trim()).floatValue();
 Float.valueOf(p5.text.getText().trim()).floatValue();
 Float.valueOf(p6.text.getText().trim()).floatValue();
 
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the CCFlow form!");
  return(false);
   } 

}/*end verify()*/

/**
*read ccFlow data from a file
*This will read the same file as the Block component if
*necessary.  It will just ignore the neighbors
*/

public boolean readData(){
  readTheForm();  //force reading of the form
  if (dataRead)return(dataRead); //don't read the data unnecessarily
  dataRead=false; //return this at end
   fileName=ccFlowFile;
 
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
  boolean sizeWarning=false; //indicates if a polygon's size is 0
  Float size=0.f; //temp var to hold the size
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
   data=new SubFlowData[nPolysRead+1];
   for (int i=1;i<=nPolysRead;i++)data[i]=new SubFlowData();       
   nPolysRead=0;
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
       
       if(!polyNow.equals(polyBefore)) nPolysRead++; //reading a new polygon
 
      if (pass==2 && polyTable.containsKey(polyNow) && polyFirst==-999)
          polyFirst=flowform.polyFirst;
      //  polyFirst=Integer.parseInt((String)polyTable.get(polyNow)); //get the internal id
	 
     if(pass==2 && !polyTable.containsKey(polyNow)){
	   parent.console.println("Polygon "+polyNow+" not seen in Flow data, \n you must make Block and Flow data conform.");
	 }/*end if !polyTable*/	 

      if (pass==2 & !polyNow.equals(polyBefore)){ //store the data
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  ID=ID-flowform.polyFirst+1; //convert global to local polygon ID
	  data[ID].size=size;
      }/*end if pass==2*/

      polyBefore=polyNow;
	 
	String tempSize=token.nextToken(); //size
	try{size=new Double(tempSize).floatValue();
	}catch(Exception es){parent.console.println("ERROR: Check size after line "+nLines+"\n"+line); size=1.f;}
	if(size==0){sizeWarning=true;size=1.f;}
	
     nLines++;

    }/*end of while loop*/

      if (pass==2){ //store the data from last polygon
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  ID=ID-flowform.polyFirst+1; //convert global to local polygon ID
	  data[ID].size=size;
      }/*end if pass==2*/
    
    if (pass==2){ 
      //for (int i=1;i<nextRegime;i++) parent.console.println("nextRegime i regimeTable regimeArray "+nextRegime+" "+i+" "+regimeTable.get(Double.toString(i))+" "+regimeArray.elementAt(i-1));
      if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     if(sizeWarning)parent.console.println("Warning: Polygons with size=0 were reset to size=1.");
     parent.console.println("Successfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     flowform.nSubComponents++;
     readCCRegimes(); //now read the CC regimes
     dataRead=true; //if you get here the data was read OK
   } /*end if pass==2*/ 
      
      
} /*end try*/
   catch (Exception e) {
    parent.console.println("Error: reading " + fileName + "\n" + e);
    nextPolygon=nextPolygonSave; //restore to old value
    dataRead=false; }
finally {  try { if (in != null) {in.close();} } catch (IOException e) {} }
  
  }/*end pass loop*/
  
  return dataRead;
  
 }/*end readData()*/
 
 
/**tell parent component that you're being taken out of the
*objective function
*/
protected void notifyParentOfRemoval(){
 flowform.nSubComponents--;
}/*end notifyParentOfRemoval()*/
 
 /**
*Initialize the  total flow
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
 xMax=flowform.xMax;
 xMin=flowform.xMin;
 int yrs=xMax-xMin+1;
 gama=new float[yrs+1]; //holds current flow
 gamaz=new float[yrs+1];
 g=new float[yrs+1];  //holds modeled flow, i.e. the goal
 flowData=flowform.data; //polygon data in parent flowform
 
 int row; //row in flow for the current schedule;
 int fsize; //number of years of output for this schedule;
 int t; //The year for gama subscript
 int i; //local polygon index
 boolean  ccValid; //indicates its a valid cc management option
 
 for(int ig=polyFirst;ig<=polyLast;ig++){
  i=ig-polyFirst+1; //transform global to local polygon index
   row=flowData[i].flowPtr(polySched[ig]);  /*the correct row in the FLOW matrix*/
   if (row==0){parent.console.println("ccFlow.init(): Invalid management option, error in assigning schedule to polygon " + i);System.exit(1);}
   fsize=(flowData[i].flow[row].length-1)/2;
   
 for (int j=1;j<=fsize;j++){  /*handles multiple output years*/  
  float flowValue=flowData[i].flow[row][fsize+j];
  if(flowValue<=0)continue; //ignore 0 flows
   ccValid=false;
  for (int ii=1; ii<=nCCOpts;ii++)
  if(polySched[ig]==ccOpts[ii][1] && j==ccOpts[ii][2]){ccValid=true;break;}
     if (!ccValid)continue;  //dont waste anymore time on this one  
 
   t=flowData[i].flow[row][j]; /*year under current option*/
  if (t>=xMin && t<=xMax)  /*its not just a place holder in FLOW and its <= yrs*/
     gama[t-xMin+1]=gama[t-xMin+1]+this.data[i].size;
       
 } /*end for j*/
} /*end for i*/

  model(gama,g); /*gama0 is user supplied*/ 
for(t=xMin;t<=xMax;t++)gamaz[t-xMin+1]=gama[t-xMin+1]; /*initialize gamaz*/ 
 
  
  
} /*end init()*/ 

//dont need isValidSched() for ccFlow

/**
*Compute Objective Function change when one unit changes
*its management regime.  This one is combining flow and a link
*to time 0, so it's almost like 2 components in one.
*@param members contains indices of member polys, z is the new regime
*/
public double deltaObj(int[] members, int z){
   
 double Fdelta=0.0;  //holds the change related to flow
 for(int t=xMin;t<=xMax;t++) gamaz[t-xMin+1]=gama[t-xMin+1];/*init gamaz*/  

  for(int mem=0;mem<members.length;mem++){ //loop through the members
	int ig=members[mem]; //process one member of this unit at a time
	if(ig==0)continue; //no data for this member

  if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever

  int i=ig-polyFirst+1; //transform global to local polygon index
 byYrs=flowform.byYrs;  //pointer to byGone yrs
 nByYrs=flowform.nByYrs; //n of byYrs  
 boolean bySkip=false;  
 
  int row=flowData[i].flowPtr(polySched[ig]);
  int rowz=flowData[i].flowPtr(z);
  int fsizeX=(flowData[i].flow[row].length-1)/2;
  int fsizeZ=(flowData[i].flow[rowz].length-1)/2; 
  int fsize=Math.max(fsizeX,fsizeZ);	
  int i1,t,t2;
  boolean xIsValid, zIsValid;
  
          
    for (int j=1;j<=fsize;j++){ /*make needed changes to gamaz*/
	xIsValid=false;
	zIsValid=false;
      //check if x and z are cc options
     for (int ii=1; ii<=nCCOpts;ii++){
      if(polySched[ig]==ccOpts[ii][1] && j==ccOpts[ii][2]){xIsValid=true; if(zIsValid)break;}
      if(z==ccOpts[ii][1] && j==ccOpts[ii][2]){  zIsValid=true; if(xIsValid)break;}
     }/*end for ii*/
     if (j<=fsizeX){//handle variable period regimes
      boolean isFlowX=(flowData[i].flow[row][fsizeX+j]>0);
      i1=flowData[i].flow[row][j]-xMin+1;  /*the scaled current management year*/
      i1=(i1>0)?i1:0;  //make sure doNothings stay at 0, not negative indices
      if(xIsValid && isFlowX) gamaz[i1]=gamaz[i1]-data[i].size;
     } //end if fsizeX 
     if (j<=fsizeZ){//handle variable period regimes
	 boolean isFlowZ=flowData[i].flow[rowz][fsizeZ+j]>0;
      i1=flowData[i].flow[rowz][j]-xMin+1;  /*the scaled z management year*/
      i1=(i1>0)?i1:0;  //make sure doNothings stay at 0, not negative indices
      if(zIsValid && isFlowZ) gamaz[i1]=gamaz[i1]+data[i].size;
     } //end if fsizeZ 
    } /*end for j*/
         /*COMPUTE F(X)-F(Z)*/
       model(gama,g); 
       //Fdelta=0.;   
    for (t=xMin;t<=xMax;t++){
      t2=t-xMin+1;
      bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
      if(!bySkip)Fdelta=Fdelta+Math.pow(gama[t2]-g[t2],2); /*F(X)*/
    } /*end for t*/
      model(gamaz,g);   
    for (t=xMin;t<=xMax;t++){
      t2=t-xMin+1;
      bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
      if(!bySkip)Fdelta=Fdelta-Math.pow(gamaz[t2]-g[t2],2); /*F(X)-F(Z)*/
    } /*end for t*/

 }/*end for mem loop*/ 

     Fdelta=Fdelta*weight;
  //divide by yrs and gama[1] so similar weight will work for different problems
 return (Fdelta/(xMax-xMin+1)/(wtFactor+.01));
}/*end deltaObj()*/

 /**
*adjustments to be made during a metropolis iteration
*if a change is made
*/
public void duringIter(){
 for(int t=1;t<=xMax-xMin+1;t++) gama[t]=gamaz[t];
}/*end duringIter()*/

 /**
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){
  if (g[2]<=0)init(); //crude way to reinit when on the fly entry failed
    int count=0;
  for(int t=1;t<=xMax-xMin+1;t++){
    if (gama[t]>0){ wtFactor+=gama[t]; count++;}
  }/*end for i*/
  if(count>0)wtFactor/=count;
  
  if (graph.isVisible()){
    int yrs=xMax-xMin+1;
    float[] xx=new float[yrs+1];
    float[] fcr=fcRatio(); //compute the mean of flow/ccFlow ratios
    for(int t=xMin; t<=xMax; t++){
	xx[t-xMin+1]=t; //x-axis
    }
     graph.ag[1].plot(xx,gama,"Actual ccFlow",.05,.85);
     graph.ag[1].plotHold(xx,g,"Target ccFlow",.05,.65);   
      graph.ag[2].plot(xx,fcr,"Flow/ccFlow ratio",.05,.8); //graph has only 1 line
      if (!isColorsLoaded){
	  isColorsLoaded=true; graph.loadColors(); //make sure it looks like user wants
	  if (xMax-xMin<20){
	      graph.ag[1].xWrite.setTics(5); graph.ag[2].xWrite.setTics(5);
	      graph.ag[1].xTics.setTics(5);graph.ag[2].xTics.setTics(5);
	      }
      }/*end if isColorsLoaded*/
     graph.repaint();
   }/*end if*/
  weight=adjustWeight((float)weight);
  p6.text.setText(String.valueOf((float)weight));
  
}/*end postIter()*/

/**
*compute the ratio of flow units per ccFlow units
*Usually this would be ave(Flow per acre).  The is
*computed as a mean of ratios NOT a ratio of means.
*/
public float[] fcRatio(){
 
 flowData=flowform.data; //polygon data in parent flowform
 
 int row; //row in flow for the current schedule;
 int fsize; //number of years of output for this schedule;
 int t; //The year for gama subscript
 int yrs=xMax-xMin+1;
 float[] fcr=new float[yrs+1];
 int[] count=new int[yrs+1];
 for(int i=0;i<=yrs;i++){ fcr[i]=0.f; count[i]=0;}//initialize
  boolean  ccValid; //indicates its a valid cc management option
 
  try{
 for(int ig=polyFirst;ig<=polyLast;ig++){
   int i=ig-polyFirst+1; //ig is global polygon index, i is local
   row=flowData[i].flowPtr(polySched[ig]);  /*the correct row in the FLOW matrix*/
   if (row==0){parent.console.println("ccFlowForm.fcRatio(): Invalid management option, error in assigning schedule to polygon " + i);System.exit(1);}
   fsize=(flowData[i].flow[row].length-1)/2;
 for (int j=fsize;j>=1;j--){  /*handles multiple output years*/ 
     float flowValue=flowData[i].flow[row][fsize+j];
     if(flowValue<=0)continue; //ignore 0 outputs
     ccValid=false;
  for (int ii=1; ii<=nCCOpts;ii++)
   if(polySched[ig]==ccOpts[ii][1] && j==ccOpts[ii][2]){ccValid=true;break;}
     if (!ccValid)continue;  //dont waste anymore time on this one
  
   t=flowData[i].flow[row][j]-xMin+1; /*scaled year under current option*/
  if (t>=1 && t<=yrs){  /*its not just a place holder in FLOW and its <= yrs*/
      //parent.console.println("polygon regime period flow size "+polyArray.elementAt(i-1)+" "+regimeArray.elementAt(polySched[ig])+" "+j+" "+flowData[i].flow[row][fsize+j]+" "+data[i].size );
     fcr[t]+=flowValue/data[i].size;
     count[t]++;
   }/*end if t>0...*/  
 } /*end for j*/
} /*end for ig*/

  for (int j=1;j<=yrs;j++){ fcr[j]/=count[j]; //get the means of the ratios
    if(Float.isNaN(fcr[j]) || Float.isInfinite(fcr[j]))fcr[j]=0;
  }
  
    }catch(Exception e){
      parent.console.println("CCFlowForm.fcRatio():" +e);
    }

return(fcr); 

} /*end fcRatio()*/

/**
*compute the predicted ccFLOW under a user specified model
*change this function to use different models.  The vector g
*is returned with the modeled values
*/
void model(float gama[], float g[])  
{
 float rate=ccRate;
  int t,j,t2;
 int yrs=xMax-xMin+1;
  for( t=1;t<=yrs;t++) g[t]=0; /*initialize g to 0's*/ 
    g[1]=gama[1];
  
 for(t=xMin+1;t<=xMax;t++){ 
    bySkip=false; for (j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
    t2=t-xMin+1;
    if (bySkip){g[t2]=gama[t2];continue;} //set target to actual byGone value
    if (t>xMin+1) g[t2]=(gama[t2-1]+(t2-2)*g[t2-1])*(1+rate)/(t2-1);
    else g[t2]=gama[t2-1]*(1+rate);         
 }/*end for t*/     
  
 g[1]=2*g[2]-g[3]; /*an ad-hoc value for filler*/
 bySkip=false; for (j=1;j<=nByYrs;j++) if(byYrs[j]==xMin){bySkip=true; break;}
 if (bySkip)g[1]=gama[1];
} /************************end model function************************/


/**
*adjust p-parameter to keep yearly relative flow change between
*rate and rate/2
*/

protected final float adjustWeight(float wt)
{
double delta=1.,wtNew,rdif=1., yDelta=1.0, yRdif=1.0;
double upperF=1,lowerF=0;
double aFactor=.99; //how fast to adjust the weight
if(parent.nPolys>200)aFactor=.95;
if(parent.nPolys>500)aFactor=.9;
int t2;
 
 upperF=(goal+.05);
 upperF=(upperF<1.)?upperF:1.;
 lowerF=(goal-.05);
 lowerF=(lowerF>0.)?lowerF:0.;
 
 for (int t=xMin;t<=xMax;t++){
   t2=t-xMin+1;
   bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
   if (bySkip)continue; //this is a byGone year so skip it
   rdif=1. - Math.abs((gama[t2]-g[t2]))/(g[t2]+ minValue); 
   if (rdif<delta) delta=rdif;
   if(delta<0)delta=0.;
   if(t2>1)yRdif=1.-Math.abs((gama[t2]-gama[t2-1])/(g[t2]+ minValue)); 
   if (yRdif<yDelta && yRdif>=0) yDelta=yRdif;
 } /*end for t*/
 
 wtNew=wt;
   //too much variation about line or yr to yr, then increase weight
 wtNew=(yDelta>upperF&&delta>upperF)?wtNew*aFactor:wtNew;  
 wtNew=(yDelta<lowerF||delta<lowerF)?wtNew/aFactor:wtNew; //too little variation then decrease weight
 if (wtNew<minValue)wtNew=minValue; /*keep p in bounds, display will only show 3 decimals*/
 else if(wtNew>Float.MAX_VALUE){
     isWt2big=true;
     wtNew=Float.MAX_VALUE;
 }
 
 if ((float)wtNew==wt)converged=1; //indicates convergence
 else if ((float)wtNew<wt)converged=2; //overConverged
 else converged=0;   //underConverged
 
  //write some output before returning
 delta=Math.floor(delta*100)/100.;
 yDelta=Math.floor(yDelta*100)/100.;
 outLabel.setText("Actual Goals: 1-y-yHat=" + delta + "  1-yr to yr=" + yDelta);
 goalValue=(yDelta+delta)/2.; //used by fitness function
return((float)wtNew);
} /*******************end adjustWeight*******************/ 


/**
*this lets you click on the  textArea to
*see all the current CC options, since
* many may be done automatically and not obvious to user
*/
public class BlockListener implements MouseListener{
 public void mouseClicked(MouseEvent event){
     if (dataRead)readCCRegimes();//reread CC's just in case
     parent.console.println("========Begin CC Regimes========");
     String ccRegime="";
     for (int j=1;j<=nCCOpts;j++){
	 ccRegime=regimeArray.elementAt(ccOpts[j][1])+"@"+ccOpts[j][2];
	parent.console.println(ccRegime);
     }/*end for j*/
     parent.console.println("========End CC Regimes========");
  }
 public void mousePressed(MouseEvent event){}
 public void mouseReleased(MouseEvent event){}
 public void mouseEntered(MouseEvent event){}
 public void mouseExited(MouseEvent event){}
}/*end inner BlockListener*/
 
} /*end CCFlowForm class*/
