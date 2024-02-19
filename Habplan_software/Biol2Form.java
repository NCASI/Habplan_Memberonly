import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.xml.sax.Attributes;

/**
*Make an Objective Function Biological Type 2 Component form
*Type 2 doesn't require training data
*Contains all the methods needed for the Biological2 component.
*This is the simple approach that requires no training data.
*@author Paul Van Deusen (NCASI)  1/98
*/
public class Biol2Form extends ObjForm {
   JFileButton loadButton;
   JLabelTextField p3;
   JLabelTextScroll p2;
   JTextField polyId; //enter an id to check the data
   JLabel outLabel; //write output here;
   JPanel pp; //look at data panel
   boolean graph=false;
   
    //Biological Component (Type 2) userInput Variables
  protected String biol2File;
  protected float goal;  //preferred acceptance rate for biol options 
   
   //variables related to the objective function
   Biol2Data[] data;  //class to hold BofX data
   int nPositive=0; //number of obj changes>0 for adjusting weight
   int nTotal=0;  //actual n of polygons available for changing regime
   static double logDelta=.0000001;  //add to input probabilities to avoid taking log of 0.0
   double sumB=0.0, //hold the sum of biological variable here
	  tempSumB, //hold sumB for adjustment after z Proposal is accepted
	  maxBio=0.0; //the maximum if all polys are at highest rank regime
   int inComponents=0;//number of components currently in the obj function	  
	
   JPanel rankPanel; //hold rankChoice and goal
   private int rank=-1; //user specified target rank for regime assignment
   protected JComboBox rankChoice; //lets user select rank

public Biol2Form(FrameMenu parent, String s){
  super(s, 6, 1);
  this.parent=parent;
  getContentPane().setBackground(Color.black);
  p1=new JLabelTextField("Type 2 Biological file",20,"str");
  p2=new JLabelTextScroll("Goal",4,0,100,100);
  p3=new JLabelTextField("Weight",6,"float");
  p1.setBackground(Color.getColor("habplan.biol2.background"));
  p2.setBackground(Color.getColor("habplan.biol2.background"));
  p3.setBackground(Color.getColor("habplan.biol2.background"));
  p1.text.addCaretListener(new ATextListener());
  p2.text.addCaretListener(new ATextListener());
  p3.text.addCaretListener(new WtTextListener());
  p3.text.setText("1");
  
 //Choice for rank
  
  rankChoice=new JComboBox();
  rankChoice.setBackground(Color.getColor("habplan.biol2.background"));
   rankChoice.addItem("max");
   for (int i=1; i<=9; i++)
    rankChoice.addItem(Integer.toString(i));
   for (int i=10; i<=30; i+=2)
    rankChoice.addItem(Integer.toString(i)); 
  rankChoice.addItemListener(new Listener());  
  
  JLabel lab1=new JLabel("Goal Kind");
  rankPanel=new JPanel();
  rankPanel.setBackground(Color.getColor("habplan.biol2.background"));
  rankPanel.add(lab1);
  rankPanel.add(rankChoice);
  rankPanel.add(p2);
  
  loadButton= new JFileButton("File Selector",parent,p1.text,null,null);
  loadButton.setBackground(Color.getColor("habplan.biol2.background"));
  //output label
  outLabel=new JLabel(" Goal:      Sum:      Max:    ");
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  
  //create a data verify field
  pp = new JPanel();
  pp.setBackground(Color.getColor("habplan.biol2.background"));
  JLabel verify = new JLabel("Verify data for Polygon #");
  verify.setBackground(Color.getColor("habplan.biol2.background"));
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


public class Listener implements ItemListener {
 public void itemStateChanged(ItemEvent e){
   try{
   rank=Integer.parseInt((String)rankChoice.getSelectedItem());
   }catch(Exception exc){rank=-1; return;}
   // parent.console.println("selection = " + rank);
 }
}//end inner Listener class

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
 setBackground(Color.getColor("habplan.biol2.background"));
 JPanel outPanel=new JPanel();
 outPanel.add(outLabel);
 outPanel.setBackground(Color.black);
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new BorderLayout());
  getContentPane().add(rankPanel,BorderLayout.NORTH);
  getContentPane().add(outPanel,BorderLayout.SOUTH);
  pack();
 }/*end if*/
 else{
    getContentPane().removeAll();
    getContentPane().setLayout(new BorderLayout());
   JPanel northPanel=new JPanel(new BorderLayout());
  northPanel.add(loadButton,BorderLayout.NORTH);
  northPanel.add(p1,BorderLayout.CENTER);
  northPanel.add(rankPanel,BorderLayout.SOUTH);
   JPanel southPanel=new JPanel(new BorderLayout());
  southPanel.add(p3,BorderLayout.NORTH);
  southPanel.add(outPanel,BorderLayout.CENTER);
  southPanel.add(pp,BorderLayout.SOUTH);
    getContentPane().add(northPanel,BorderLayout.NORTH);
    getContentPane().add(southPanel,BorderLayout.SOUTH);
   pack();
  shrunk=false;
 }/*end else*/
} /*end shrink()*/


/**
*display the input data that was read
*/
public class PolyActionListener implements ActionListener{
 public void actionPerformed(ActionEvent event){
   String s;
   int Id=1;
   parent.console.clear();
    if(!dataRead){
       parent.console.println("First you must enter this component into Objective function");
       return;
   }/*end if !dataRead*/
   
   s=polyId.getText().trim();
  if(!polyTable.containsKey(s)){
      parent.console.println("Can't find polygon: "+s);
      return;
  }/*fi*/
  
  Id = Integer.parseInt((String)polyTable.get(s));

   s="Type 2 Biological data for Polygon " + polyId.getText().trim() + "\n \n";
   s=s + "Option Value ...  \n"; 
  for(int j=1; j<data[Id].regime.length; j++){
    s=s+regimeArray.elementAt(j) + "    " + Double.toString(data[Id].BofX(j)) + "\n";   
  }/*end for j*/
  
  parent.console.println(s);
   
 }/*end actionperformed*/
} /*end inner PolyActionListener class*/

 /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){
	vec[0]=p1.text.getText(); //filename
	vec[1]=rankChoice.getSelectedItem(); //goal kind
	vec[2]=p2.text.getText(); //goal
	vec[3]=p3.text.getText(); //wt
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
    p1.text.setText((String)vec[0]); //filename
    rankChoice.setSelectedItem((String)vec[1]); //goal kind
    p2.text.setText((String)vec[2]); //goal
    p3.text.setText((String)vec[3]); //wt
     }

/**
*Save users settings to a file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
  out.println("<biol2 title=\""+getTitle().trim()+"\">");
  out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
  out.println("  <goalKind value=\""+rankChoice.getSelectedItem()+"\" />");
  out.println("  <goal value=\""+p2.text.getText().trim()+"\" />");
  out.println("  <weight value=\""+p3.text.getText().trim()+"\" />");
  out.println("  <sum value=\""+sumB+"\" />");
  out.println("  <max value=\""+maxBio+"\" />");
  out.println("  <title value=\""+title+"\" />");
  out.println("</biol2>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("goalKind"))rankChoice.setSelectedItem(atts.getValue(0));
	if(qName.equals("goal"))p2.text.setText(atts.getValue(0));
	if(qName.equals("weight"))p3.text.setText(atts.getValue(0));
	if(qName.equals("title"))title=atts.getValue(0);
	}catch(Exception e){parent.console.println(getTitle()+".readSettings() "+e);}
    }

/**
*The reads the user's input on the component form
*It is called when text changes and when the components
*data are read.  This ensures that the input data is read
*even if the form is in shrunk mode
*/
public void readTheForm(){

try{
 biol2File=p1.text.getText().trim();
 goal=Float.valueOf(p2.text.getText().trim()).floatValue();
 rank=Integer.parseInt((String)rankChoice.getSelectedItem());
   }
 catch(Exception e){
   }

}/*end readTheForm*/

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
        p3.text.setText(String.valueOf(weight));
    }/*end method*/

/**
*This transfers user input to the weight
*which is frequently updated
*/
public class WtTextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){

 try{
 weight=Float.valueOf(p3.text.getText().trim()).floatValue();
   }
 catch(Exception e){  
   }
  
 }/*end textChangedValue*/
} /*end inner WtTextListener class*/


/**
*make sure this form is filled out
*/
public boolean verify(){
  
 if(p1.text.getText().trim().length()<3){
   parent.console.println("No Type 2 Biological datafile given??");
   return(false);
   }/*end if*/
  
try{
  //check number entries here
 Float.valueOf(p2.text.getText().trim()).floatValue();
 Float.valueOf(p3.text.getText().trim()).floatValue();
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the Type 2 Biological form!");
  return(false);
   } 

}/*end verify()*/

/**
*read data from a file
*/

public boolean readData(){ 

  readTheForm(); //force reading of form settings just in case.
  if (dataRead)return(dataRead); //don't read the data unnecessarily
  dataRead=false; //return this at end
  fileName=biol2File;
  int regime; //temp var to hold regime index
  String regimeName=""; //hold a regime name
  double BValue=0.; //holds a BofX
  String element=""; //converted to an integer for the hashTables
  
  double[][] tempFlow; //holds data temporarily
  tempFlow=new double[2][2]; tempFlow[1][1]=0; //defined here to fool compiler
  nextPolygonSave=nextPolygon; //use to reset polygon numbers if read fails
  polyFirst=-999; polyLast=-999;
  regimeLast=-999;
  boolean isDoNothingRegime=false;  //set this to true if there are doNothing regimes
  
  fileName=findFile(fileName); //class function to filter filenames
  parent.console.println("Reading "+fileName);

   int nLines=0;     // n of lines in file;
   int nPolysRead=0; //n of polys read in file
   int regimeMax=0, nRegimes=0; //max n of regimes read per polygon
   String polyBefore="one", polyNow=""; //previous and current polygonID
    

String delimiters=" \t\n\r,;";   
       
  StringTokenizer token;
  BufferedReader in=null;
  String line="";
  
  for (int pass=1;pass<=2;pass++){ //make 2 passes over the data
  
    nRegimes=1;//n of regimes within current polygon
  
  if (pass==2){
  
   if (polyTable.isEmpty()){
    polyFirst=1; polyLast=nPolysRead;
    polyTable=new Hashtable(nPolysRead+1,(float)0.9);
    polyArray=new Vector(nPolysRead+1);
    nextPolygon=1; //start new polygon count, this is a class variable
   }/*end if polyTable.isEmpty*/
   
    tempFlow = new double[nextRegime+1][4]; 
    data=new Biol2Data[nPolysRead+1];
    for (int i=1;i<=nPolysRead; i++)data[i]=new Biol2Data();
    nPolysRead=0;
  }/*end if pass==2*/
  
   
   nLines=0;
  try{	 
      in = new BufferedReader(new FileReader(fileName),1024);
      
      
    while ((line=in.readLine())!=null){ 
      token=new StringTokenizer(line,delimiters); //all legit delimeters
      int n=token.countTokens(); //n of tokens on this line
      if(n==0)continue; //must be a blank line
       if(n!=3){parent.console.println("ERROR: Wrong number of items after line "+nLines);
       continue;}/*fi*/
      
       polyNow=token.nextToken(); //current polygon id
       
       if(!polyNow.equals(polyBefore)){nPolysRead++;nRegimes=1;} //reading a new polygon
	  else nRegimes++; //reading another regime for same polygon
	polyBefore=polyNow;  
       
      if (pass==1) regimeMax=(regimeMax>nRegimes)?regimeMax:nRegimes; //get max n of regimes per polygon
      
      if(pass==2 & nRegimes==1 & nLines!=0){//must have just finished a polygon
	 int pIndex=(int)tempFlow[0][0]; //the internal polygon id
	 int nregs=(int)tempFlow[0][1]; //n of regimes for this polygon
	  data[pIndex].regime=new int[nregs+1];
	  data[pIndex].BofX=new double[nregs+1];
	 for(int c=1;c<=nregs;c++){
	     data[pIndex].regime[c]=(int)tempFlow[c][1];
	     data[pIndex].BofX[c]=tempFlow[c][2];
        }/*end for c*/
      }/*fi pass==2*/
      
      if (pass==2 && polyTable.containsKey(polyNow) && polyFirst==-999)
	  polyFirst=Integer.parseInt((String)polyTable.get(polyNow)); //get the internal id

	 if(pass==2 && !polyTable.containsKey(polyNow)){ //haven't seen this polygon before
	   if (polyFirst==-999)polyFirst=nextPolygon; //based on condition that components must own all new polygons or all old ones
	   nextPolygon++;	   
	   element=Integer.toString(nextPolygon-1);
	   polyTable.put(polyNow,element);  //assign a polygon Id and index number
	   polyArray.addElement(polyNow);
	 }/*end if !polyTable*/	 
	 
	regimeName=token.nextToken(); //current regime name
	  //now get BofX
	try{BValue=(double)new Double(token.nextToken()).doubleValue();
	}catch(Exception eb){parent.console.println("Bad rank after line "+nLines+"\n"+line);
	 BValue=0;
	  }/*end try catch*/
	
	 
	 if (!regimeTable.containsKey(regimeName)){
	   nextRegime++;
	   element=Integer.toString(nextRegime-1);
	   regimeTable.put(regimeName,element);
	   regimeArray.addElement(regimeName);
	 } /*end if pass==2 &&*/ 
	 
	 if (pass==2){ //now capture the data into tempFlow
	  int polyIndex=Integer.parseInt((String)polyTable.get(polyNow));
	  if(nRegimes==1)tempFlow=new double[regimeMax+1][3];//first line for this polygon
	  tempFlow[0][0]=polyIndex; 
	  tempFlow[0][1]=nRegimes;//will retain the nRegimes for this polygon
	  tempFlow[nRegimes][1]=(double) Integer.parseInt((String)regimeTable.get(regimeName));
	  tempFlow[nRegimes][2]=BValue;    
	 }/*fi pass==2*/
     
     nLines++;
    }/*end of while loop*/
    
    
      if(pass==2){//capture data from last polygon in file
	 int pIndex=(int)tempFlow[0][0]; //the internal polygon id
	 int nregs=(int)tempFlow[0][1]; //n of regimes for this polygon
	 int yrsOut=2; //n of years and outputs for each regime
	  data[pIndex].regime=new int[nregs+1];
	  data[pIndex].BofX=new double[nregs+1];
	 for(int c=1;c<=nregs;c++){
	     data[pIndex].regime[c]=(int)tempFlow[c][1];
	     data[pIndex].BofX[c]=tempFlow[c][2];
        }/*end for c*/
      }/*fi pass==2*/
   
    
     if (pass==2){ 
       if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 
     dataRead=true; //if you get here the data was read OK
     applyRegimeEdits(parent,fileName,this); //read regime edit file if exists

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
      parent.console.println("Successfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     parent.console.println("The current objective function evaluates "+regimeLast+" regimes.");
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

/**
*Used by metropolis algorithm to
*determine the valid schedules for polygon i.
*@param i indicates the polygon,
*@param nScheds is the current number of valid schedules.
*/
public int getValidScheds(int ig, int nScheds){
  if (ig<polyFirst || ig>polyLast)return(nScheds); //not a local polygon so do whatever
  int i=ig-polyFirst+1; //transform global to local polygon index
   int[] temp; //holds the schedules temporarily
     if (nScheds==-1) temp=new int[data[i].BofX.length+1];
     else temp=new int[nScheds+1]; //temp storage for validScheds
     int count=0; //count valid scheds
     boolean allZeros=true; //make sure that there is at least 1 valid sched
     //get current schedule in position 1
     if (polySched[ig]!=0){count=1; temp[1]=polySched[ig];} 
     
      int z=0; //holds a schedule
     if (nScheds==-1){ //if this is first component added
        for (int k=1; k<data[i].regime.length; k++){
	  z=data[i].getSched(k);
	  if(z!=polySched[ig]) temp[++count]=z;  //only use current sched in position 1
	  allZeros=false;
	}/*end for k*/
      } /*end if nScheds==-1*/
     else {
    
       for (int k=1;k<=nScheds;k++){
        z=validScheds[k]; 
	if (Double.isNaN(data[i].BofX(z)))continue; /*not a valid option so don't change to it*/
	allZeros=false;
	if(z!=polySched[ig]) temp[++count]=z; //only allow current sched in position 1   
       }/*end for k*/ 
   
      }/*end if else nScheds==-1*/ 
    System.arraycopy(temp,1,validScheds,1,count);
    if (allZeros)parent.console.println("The Bio-2 component has no valid regimes for polygon " + (String)ObjForm.polyArray.elementAt(ig-1));
  return(count);
}/*end isValidSched()*/ 

  
 /**
*Compute the maximum attainment, i.e. if all polys are set to
*the highest ranking option
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
 maxBio=0.0; sumB=0.0; nPositive=0;
 for (int ig=1;ig<=parent.nPolys;ig++){
    if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
    int i=ig-polyFirst+1; //transform global to local polygon index 
    int z=Algorithm.randomSchedule(ig); //set validScheds correctly 
    maxBio+=data[i].getMaxBio(validScheds,nScheds);
    double tsb0=data[i].BofX(polySched[ig]);
    if(Double.isNaN(tsb0))continue;
     sumB+=tsb0;//only use a valid sched
 }/*end for ig*/
 maxBio=Math.round(maxBio*10.)/10.;
 inComponents=0;
 for (int j=1;j<=parent.nComponents;j++)
    if(parent.isAdded[j])inComponents++; //current n of components in obj function
 
 //parent.console.println("maxBio= "+maxBio+ " current= "+sumB); 
}/*end init()*/ 
 
/**
*Compute Objective Function change when one unit changes
*its management regime.  
*@param members contains indices of member polys, z is the new regime
*/

public double deltaObj(int[] members, int z){

    double Bdelta=0.0;  //holds the change related to flow
    double goalBio=maxBio*goal; //goal for sumB
    double Brank=0.0; //holds BofX for the specified rank (below)
    tempSumB=sumB;

 for(int mem=0;mem<members.length;mem++){ //loop through the members
	int ig=members[mem]; //process one member of this unit at a time
	if(ig==0)continue; //no data for this member

 if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
  int i=ig-polyFirst+1; //transform global to local polygon index 

  double Bx=data[i].BofX(polySched[ig]);
  double Bz=data[i].BofX(z);
  if(Double.isNaN(Bx))Bx=0.0; //try to deal with invalid regimes
   if(Double.isNaN(Bx))Bz=0.0;
             
     tempSumB=tempSumB-Bx+Bz; //use in duringIter() if z proposal is accepted
if (rank==-1){   //the goal is relative to maxBio  
  if (sumB<goalBio && Bx>Bz)Bdelta+=-weight;//x is better than z
  else if(sumB<goalBio && Bz>Bx)Bdelta+=weight; //z better than x
   else Bdelta=Bdelta; //indifferent to x or z
 }/*rank ==-1 block*/
 else{  //the goal is relative to a rank
  
if (Bx>=Bz) //x better than z
     Bdelta+=-weight; 
else if (Bz>Bx)//z better than x
     Bdelta+=weight;   
else Bdelta=Bdelta; //both are bad

  Brank=data[i].BofXByRank(rank,validScheds,nScheds);
  if (Bx>=Brank)nPositive++;
  
   nTotal++;
  } /*end of relative to a rank block*/
 }/*end for mem loop*/
 
 return (Bdelta);
 
}/*end deltaObj()*/

 /**
*adjustments to be made during a metropolis iteration
*if a change is made
*/
public void duringIter(){
 sumB=tempSumB;  //change sumB since z proposal has been accepted
}/*end duringIter()*/

 /**
*adjustments to be made after each metropolis iteration
*/
public  void postIter(){
 double lower=0.,upper=1.,ratio=0.;
 double Scale=weight; //used to determine if weight has converged
 double aFactor=.98; //determines how fast the weight adjusts
 int i,nPolys=parent.nPolys,
    tempComponents=0; //temp holder of current inComponents

 if (rank==-1 ){ //in case validScheds changes, redo this occassionally  
   for (int j=1;j<=parent.nComponents;j++)
     if(parent.isAdded[j])tempComponents++; //current n of components in obj function
   if (tempComponents!=inComponents){ 
     maxBio=0.0; sumB=0.0;
     inComponents=tempComponents; tempComponents=-1;} /*end if*/
   for (int ig=1;ig<=parent.nPolys;ig++){
    if (tempComponents!=-1)break; // valid scheds haven't changed
    if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
    i=ig-polyFirst+1; //transform global to local polygon index
    int z=Algorithm.randomSchedule(ig); //set validScheds correctly  
    maxBio+=data[i].getMaxBio(validScheds,nScheds);
    double tsb0=data[i].BofX(polySched[ig]);
    if(!Double.isNaN(tsb0))sumB+=tsb0;//only use a valid sched
       }/*end for ig*/ 
 
   maxBio=Math.round(maxBio*10.)/10.;
   ratio=sumB/maxBio;
  } /*end if rank==-1*/
 else {  
  ratio=(float)nPositive/nTotal;
  nPositive=0; //reset for next iteration
  nTotal=0;
 } /*end else block*/ 
 
 upper=(goal+.05);
 upper=(upper<1.)?upper:1.;
 lower=(goal-.05);
 lower=(lower>0.)?lower:0.;
 Scale=(float)((ratio>upper)?Scale*aFactor:Scale); 
 Scale=(float)((ratio<lower)?Scale/aFactor:Scale);
 
 if(Scale<minValue)Scale=(float)minValue;
 else if(Scale>Float.MAX_VALUE)Scale=Float.MAX_VALUE;
 
 if (Scale==weight)converged=1; //indicates convergence
 else if (Scale<weight)converged=2; //overConverged
 else converged=0;   //underConverged  
 
  if(Scale<minValue)Scale=minValue;
 else if(Scale>Float.MAX_VALUE){
     isWt2big=true;
     Scale=Float.MAX_VALUE;
 }
  weight=Scale;  
 
   p3.text.setText(String.valueOf((float)weight));
   //write some output
   goalValue=ratio; //used by fitness function
   ratio=Math.floor(ratio*100)/100.;
   sumB=Math.floor(Math.round(sumB*10.))/10.; //get rid of excess decimal places
   outLabel.setText(" Goal: " + ratio + " Sum: "+sumB+" Max: "+maxBio); 
   parent.compBox[idNum].setToolTipText(title+"-Goal: " + ratio); 
   
}/*end postIter()*/

/**
*Save the sum of the biological data from the current
*schedule to a user specified file
*this is mainly used for PVD's internal purposes,
*i.e. research
*/
 public void saveBiol(String fileName){
  int nPolys=parent.nPolys;
  RandomAccessFile fout=null;
  String s;
  int i=0, x;
  
  try {
    fout =  new RandomAccessFile(fileName,"rw");
    fout.seek(fout.length());
     s=Double.toString(sumB)+"\n";
      fout.writeBytes(s);
     //parent.console.println("just did a saveBiol");
 }/*end try*/
  catch(Exception e){
   parent.console.println("Error saving the Bio2 data for polygon: i= " +i);
   parent.console.println(e.toString());
  } /*end catch*/
   finally { try { if (fout != null) fout.close(); } catch (IOException e) {} }      
 } /*end saveBiol()*/  

} /*end Biol2Form class*/
