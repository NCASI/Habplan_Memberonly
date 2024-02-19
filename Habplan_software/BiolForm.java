
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import linear_algebra.*;
import org.xml.sax.Attributes;

/**
*Make an Objective Function Biological Component form
*Contains all the methods needed for the Biological component.
*This is the complicated approach that requires training data.
*@author Paul Van Deusen (NCASI)  12/97
*/
public class BiolForm extends ObjForm {
   JFileButton loadButton;
   JLabelTextField p4;
   JLabelTextScroll p3;
   JTextField polyId; //enter an id to check the data
   JLabel outLabel; //write output here;
   JPanel pp; //look at data panel
   boolean graph=false;
   FrameMenu parent;
   
   //User input variables
  protected String biolFile;
  protected float goal;  //preferred acceptance rate for biol options
  protected int nBiol;  //number of biological vars

   //variables related to the objective function
   float[][] biolData; //holds biological data from the file
   BiolData[] data;  //inner class to hold BofX data
   int[] train;    //holds training option
   int[] trainCount; // keep track of n of obs per option in train
   int nPositive=0; //number of obj changes>0 for adjusting RdBiol
   int nTotal=0;  //actual n of polygons available for changeing
   double sumB=0.0, //hold the sum of biological variable here
	  tempSumB, //hold sumB for adjustment after z Proposal is accepted
	  maxBio=0.0; //the maximum if all polys are at highest rank regime
   int inComponents=0;//number of components currently in the obj function	  
		
   JPanel rankPanel; //hold rankChoice and goal
   private int rank=1; //user specified target rank for regime assignment
   protected JComboBox rankChoice; //lets user select rank

public BiolForm(FrameMenu parent, String s){
  super(s, 7, 1);
  this.parent=parent;
  getContentPane().setBackground(Color.black);
  p1=new JLabelTextField("Biological fileName",20,"str");
  p3=new JLabelTextScroll("Goal",4,0,100,100);
  p4=new JLabelTextField("Weight",6,"float");
  p1.setBackground(Color.getColor("habplan.biol.background"));
  p3.setBackground(Color.getColor("habplan.biol.background"));
  p4.setBackground(Color.getColor("habplan.biol.background"));
  p1.text.addCaretListener(new ATextListener());
  p3.text.addCaretListener(new ATextListener());
  p4.text.addCaretListener(new WtTextListener());
  p4.text.setText("1");
  
  //Choice for rank
  
  rankChoice=new JComboBox();
  rankChoice.setBackground(Color.getColor("habplan.biol.background"));
   rankChoice.addItem("max");
   for (int i=1; i<=9; i++)
    rankChoice.addItem(Integer.toString(i));
   for (int i=10; i<=30; i+=2)
    rankChoice.addItem(Integer.toString(i)); 
    rankChoice.setSelectedItem("1");  //default is 1 rather than max as in Bio-2
  rankChoice.addItemListener(new Listener());  
  
  JLabel lab1=new JLabel("Goal Kind");
  rankPanel=new JPanel();
  rankPanel.setBackground(Color.getColor("habplan.biol.background"));
  rankPanel.add(lab1);
  rankPanel.add(rankChoice);
  rankPanel.add(p3);
  
  loadButton= new JFileButton("File Selector",parent,p1.text,null,null);
  loadButton.setBackground(Color.getColor("habplan.biol.background"));
  
  //output label
  outLabel=new JLabel();
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  //create a data verify field
  pp = new JPanel();
  pp.setBackground(Color.getColor("habplan.biol.background"));
  JLabel verify = new JLabel("Verify data for Polygon #");
  pp.add(verify);
  polyId = new JTextField(" ",4);
  polyId.setBackground(Color.lightGray);
  polyId.addActionListener(new PolyActionListener());
  pp.add(polyId);
  pack();

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
   }catch(Exception exc){rank=-1; return;}  //this means user selected max option
   //parent.console.println("selection = " + rank);
 }
}//end inner Listener class

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
 setBackground(Color.getColor("habplan.biol.background"));
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(2,1));
  getContentPane().add(rankPanel);
  getContentPane().add(outLabel);
  pack();
 }/*end if*/
 else{
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(6,1));
  getContentPane().add(loadButton);
  getContentPane().add(p1);add(rankPanel);add(p4);
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
   parent.console.clear();
     if(!dataRead){
       parent.console.println("First you must enter this component into Objective function");
       return;
   }/*end if !dataRead*/
    
  try{
    try{Id=Integer.parseInt(polyId.getText().trim()); s=Double.toString(Id);}
    catch(Exception e){s=polyId.getText().trim();}  //handle integer polygon names by converting ot doubles.
    Id = Integer.parseInt((String)polyTable.get(s));
   }
  catch(Exception e){
   parent.console.println("Need to enter an Integer polygon ID within the proper range");
   return;
  } 

   s="Biological data for Polygon " + polyId.getText().trim() + "\n \n";
   s=s+ " Training Option = 0 means this is not used as training data \n";
   s=s + "Train      Variables ...  \n"; 
   s=s+ regimeArray.elementAt(train[Id]) + "          ";
  for(int j=1; j<=nBiol; j++){
    s=s+Float.toString(biolData[Id][j]) + " ";   
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
	vec[2]=p3.text.getText(); //goal
	vec[3]=p4.text.getText(); //wt
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){   
	p1.text.setText((String)vec[0]); //filename
	rankChoice.setSelectedItem((String)vec[1]); //goal kind
	p3.text.setText((String)vec[2]); //goal
	p4.text.setText((String)vec[3]); //wt
     }

/**
*Save users settings to a file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
  out.println("<biol title=\""+getTitle().trim()+"\">");
  out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
  out.println("  <goalKind value=\""+rankChoice.getSelectedItem()+"\" />");
  out.println("  <goal value=\""+p3.text.getText().trim()+"\" />");
  out.println("  <weight value=\""+p4.text.getText().trim()+"\" />");
  out.println("  <title value=\""+title+"\" />");
  out.println("</biol>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("goalKind"))rankChoice.setSelectedItem(atts.getValue(0));
	if(qName.equals("goal"))p3.text.setText(atts.getValue(0));
	if(qName.equals("weight"))p4.text.setText(atts.getValue(0));
	if(qName.equals("title"))title=atts.getValue(0);
    }


/**
*The reads the user's input on the component form
*It is called when text changes and when the components
*data are read.  This ensures that the input data is read
*even if the form is in shrunk mode
*/
public void readTheForm(){
 try{
biolFile=p1.text.getText().trim();
goal=Float.valueOf(p3.text.getText().trim()).floatValue();
rank=Integer.parseInt((String)rankChoice.getSelectedItem());
   }
 catch(Exception e){
   }
}/*end readTheForm()*/

/**
*This transfers user input to variables
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
        p4.text.setText(String.valueOf(weight));
    }/*end method*/

/**
*This transfers user input to the weight
*which is frequently updated
*/
public class WtTextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 
 try{
weight=Float.valueOf(p4.text.getText().trim()).floatValue();
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
   parent.console.println("No Biological datafile given??");
   return(false);
   }/*end if*/
  
try{
  //check number entries here
 Float.valueOf(p3.text.getText().trim()).floatValue();
 Float.valueOf(p4.text.getText().trim()).floatValue();
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the Biological Type1 form!");
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
   fileName=biolFile;
  int regime; //temp var to hold regime index
  String regimeName=""; //hold a regime name
  String element=""; //converted to an integer for the hashTables
  
  nextPolygonSave=nextPolygon; //use to reset polygon numbers if read fails
  polyFirst=-999; polyLast=-999;
  regimeLast=-999;
  
  fileName=findFile(fileName); //class function to filter filenames
  parent.console.println("Reading "+fileName);

   int nLines=0;     // n of lines in file;
   int nPolysRead=0; //n of polys read in file
   int cntMax=0; // max n of elements per line
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
   
      nBiol=cntMax-2;
      parent.console.println("N of Biological Variables = "+nBiol);
      biolData=new float[nPolysRead+1][nBiol+1];
      train = new int[nPolysRead+1];
      trainCount=new int[nextRegime+1]; //can hold the max possible
      nPolysRead=0;
  }/*end if pass==2*/
  
   
   nLines=0;
  try{	 
      in = new BufferedReader(new FileReader(fileName),1024);
      
      
    while ((line=in.readLine())!=null){ 
      token=new StringTokenizer(line,delimiters); //all legit delimeters
      int n=token.countTokens(); //n of tokens on this line
      if(n==0)continue; //must be a blank line
      
       polyNow=token.nextToken(); //current polygon id
       
       if(!polyNow.equals(polyBefore)){nPolysRead++;nRegimes=1;} //reading a new polygon
	  else nRegimes++; //reading another regime for same polygon
	polyBefore=polyNow;  
       
      if (pass==1){
	  cntMax=(cntMax>n)?cntMax:n; //get max n of columns
	  regimeMax=(regimeMax>nRegimes)?regimeMax:nRegimes; //get max n of regimes per polygon
      }/*fi pass=1*/
      
    
      
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
	
	 
	 if (!regimeTable.containsKey(regimeName)){
	   if (regimeName.equals("0"))//its not a new regime if 0, it's a trainer
	    regimeTable.put("0","0");
	   else{
	    nextRegime++; 
	    element=Integer.toString(nextRegime-1);
	    regimeTable.put(regimeName,element);
	    regimeArray.addElement(regimeName);
	   }  
	 } /*end if !regimeTable*/
	 
	 if (pass==2){ //now capture the data into tempFlow
	  int polyIndex=Integer.parseInt((String)polyTable.get(polyNow));
	  int regimeIndex=0;
	  if(!regimeName.equals("0"))regimeIndex=Integer.parseInt((String)regimeTable.get(regimeName));
	  train[polyIndex]=regimeIndex;
	  if(regimeIndex>0)trainCount[regimeIndex]++;
	   float bvar=0;
	  for(int b=1;b<nBiol+1;b++){
	   try{bvar=new Double(token.nextToken()).floatValue();   
	   }catch(Exception e){parent.console.println("Biological variable error after line "+nLines);}
	   biolData[polyIndex][b]=bvar;
	  }/*end for*/
	 }/*fi pass==2*/
     
     nLines++;
    }/*end of while loop*/
   
    
     if (pass==2){ 
       if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 
     dataRead=true; //if you get here the data was read OK

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     parent.console.println("Successsfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     parent.console.println("The current objective function evaluates "+regimeLast+" regimes.");
     //make sure n of polygons is correct
     if (polyFirst==1)
       for (int j=1;j<=parent.nComponents;j++)
	if(parent.isAdded[j] && parent.form[j].polyFirst==1 && (parent.form[j].polyLast != this.polyLast))
	parent.console.println("\7Fatal Error!: \nN of Polygons mismatch between "+ this.getTitle() +" and "+ parent.form[j].getTitle()); 
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
*old read biological data from a file
*/

public boolean oldreadData(){
  readTheForm(); //force reading the form
  if (dataRead)return(dataRead); //don't read the data unnecessarily
  dataRead=false; //return this at end
  String fileName=biolFile;
  int regime; //temp var to hold regime index
  String key="",element;
  int trueCount=1; //be sure of how many polys youve read 
  boolean nextPoly=false; //use to know if on the next polygon
  float[] tempBiol; //holds data temporarily
  tempBiol=new float[2]; tempBiol[1]=0; //fool compiler by initializing here
 
  nextPolygonSave=nextPolygon; //use to reset polygon numbers if read fails
  polyFirst=-999; polyLast=-999;
  regimeLast=-999;
  
   fileName=findFile(fileName); //class function to filter filenames
   
   parent.console.println("Reading " + fileName); 
  dataRead=false; //return this at end
  BufferedReader fin=null;
  StreamTokenizer in=null;
  
  for (int pass=1;pass<=2;pass++){ //make 2 passes over file 	 
  // parent.console.println("pass trueOptions trueCount nOptions"+pass+" "+trueOptions+" "+trueCount);
   
  if (pass==2){
   trueCount--;
      biolData=new float[trueCount+1][nBiol+1];
      train = new int[trueCount+1];
      trainCount=new int[nextRegime+1]; //can hold the max possible
      tempBiol = new float[nBiol+3]; 
 
      if (polyTable.isEmpty()){
	parent.console.println("Note: Running with only a Bio-1 may not be very useful");
	polyFirst=1; polyLast=trueCount;
        polyTable=new Hashtable(trueCount+1,(float)0.9);
        polyArray=new Vector(trueCount+1);
	nextPolygon=1; //start new polygon count, this is a class variable
      }	/*end if polyTable.isEmpty*/
	   trueCount=1;
  }/*end if pass==2*/
  
      try{
      fin = new BufferedReader(new FileReader(fileName), 1024);
      in = new StreamTokenizer(fin);
      in.eolIsSignificant(true);
      in.commentChar('#'); //ignore lines beginning with #
      
       
       int cnt=1, cnt2;
       
    while (in.nextToken() != StreamTokenizer.TT_EOF){
    
	if(cnt==1 && (in.ttype==StreamTokenizer.TT_WORD || in.ttype==StreamTokenizer.TT_NUMBER)){
	 if (in.ttype==StreamTokenizer.TT_WORD)key=in.sval;
	 if (in.ttype==StreamTokenizer.TT_NUMBER)key=Double.toString(in.nval);  
	 
	  if (pass==2 && polyTable.containsKey(key) && polyFirst==-999)
	  polyFirst=Integer.parseInt((String)polyTable.get(key));
	 
	  if(pass==2 && !polyTable.containsKey(key)){
	  parent.console.println("didn't contain polygon "+key);
	   nextPolygon++;
	   if (polyFirst==-999)polyFirst=nextPolygon-1; //based on condition that components must own all new polygons or all old ones
	   element=Integer.toString(nextPolygon-1);
	   polyTable.put(key,element);  //assign a polygon Id and index number
	   polyArray.addElement(key);
	 }/*end if !polyTable*/	  
		 
	 if (pass==2)tempBiol[cnt++]=(float)trueCount; //local polygon index
	 else cnt++;
	 continue;
	} /*end if cnt==1*/ 
    
	
	//get the string regime name
	if(cnt==2 && (in.ttype==StreamTokenizer.TT_WORD || in.ttype==StreamTokenizer.TT_NUMBER)){
	 if (in.ttype==StreamTokenizer.TT_WORD)key=in.sval;
	 else if (in.ttype==StreamTokenizer.TT_NUMBER)key=Double.toString(in.nval);  
	 
	 if (!regimeTable.containsKey(key)){
	   if (key.equals("0.0"))//its not a new regime if 0.0, it's a trainer
	    regimeTable.put("0.0","0");
	   else{
	    nextRegime++; 
	    element=Integer.toString(nextRegime-1);
	    regimeTable.put(key,element);
	    regimeArray.addElement(key);
	   }  
	 } /*end if !regimeTable*/
	 
	   if (pass==2){ 
	   tempBiol[cnt++]=Float.valueOf((String)regimeTable.get(key)).floatValue();
	    }
	   else cnt++;
	 continue;  
	} /*end if cnt==2*/ 
	    
	if (cnt>=3 && in.ttype==StreamTokenizer.TT_NUMBER){
	 if (pass==2)tempBiol[cnt++]=(float) in.nval;  //the variables to classify with
	 else cnt++;
	  continue; 
       }/*end if a NUMBER*/ 
	       
       
	if (pass==2 && in.ttype==StreamTokenizer.TT_EOL){
	 trueCount++; //count the polygons as they're read
	 cnt2=(int)tempBiol[1]; //local polyId 
	 train[cnt2]=(int)tempBiol[2];//0 means not training data, else its the option this represents
	  if (train[cnt2]>0)trainCount[train[cnt2]]++;
	 for (int i=1; i<=nBiol; i++)
	  biolData[cnt2][i]=tempBiol[i+2];  
	 cnt=1; 
	} /*end if EOL*/    
	
	if (pass==1 && in.ttype==StreamTokenizer.TT_EOL){trueCount++; cnt=1;} 
	      
     }/*end while != EOF*/
     
   if (pass==2){ 
      //for (int i=1;i<nextRegime;i++) parent.console.println("nextRegime i regimeTable regimeArray "+nextRegime+" "+i+" "+regimeTable.get(Double.toString(i))+" "+regimeArray.elementAt(i));
      if (polyLast==-999)polyLast=polyFirst+trueCount-2;
      regimeLast=nextRegime-1; 
     dataRead=true; //if you get here the data was read OK

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     parent.console.println("Successsfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     parent.console.println("The current objective function evaluates "+regimeLast+" regimes.");
     //make sure n of polygons is correct
     if (polyFirst==1)
       for (int j=1;j<=parent.nComponents;j++)
	if(parent.isAdded[j] && parent.form[j].polyFirst==1 && (parent.form[j].polyLast != this.polyLast))
	parent.console.println("\7Fatal Error!: \nN of Polygons mismatch between "+ this.getTitle() +" and "+ parent.form[j].getTitle());  
   } /*end if pass==2*/ 
} /*end try*/
   catch (Exception e) {
    parent.console.println("Error: reading " + fileName + "\n" + e);
    nextPolygon=nextPolygonSave; //restore to old value
    dataRead=false; }
finally {  try { if (fin != null) {fin.close();} } catch (IOException e) {} }
  
  } //end of file reading pass loop
 
  return(dataRead);
 }/*end readData()*/
 
 /**
*Do the MLE computations for the biological component
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
 int nPolys=polyLast-polyFirst+1;
 data=new BiolData[nPolys+1];
 for (int i=1;i<=nPolys;i++)data[i]=new BiolData(); 
double cov[][]=new double[nBiol+1][nBiol+1];
double mean[]=new double[nBiol+1];
double det;
double w[]=new double[nBiol+1]; //used in mle computation
double x[][];  //holds training data for each option

//count the total number of regimes that have trainers
int nRegimesTrained=0;
  for (int j=1; j<=regimeLast; j++)
   if(trainCount[j]>0)nRegimesTrained++;
 for (int j=1; j<=nPolys;j++)//initialize BofX
  data[j].BofX=new double[nRegimesTrained+1];
  data[1].regime=new int[nRegimesTrained+1]; //static vector to point to regimes

  int sequence=0; // keep track of the current trained regimes sequence number
for (int i=1; i<=regimeLast; i++){
     //put training data into x matrix
  if (trainCount[i]==0)continue; //0 means no trainers for this regime  
   sequence++;
     x=new double[trainCount[i]+1][nBiol+1];
   int j2=0;  
   for (int j=1; j<=nPolys; j++){
     if(train[j]==i){
      j2++; //increment for each training observation
   for (int k=1; k<=nBiol; k++)
     x[j2][k]=(double) biolData[j][k];
     } /*end if train[j]==i*/  
  }/*end for j*/
   
   Matrix.covar(x,cov);
   Matrix.mean(x,mean);
   det=Matrix.det(cov);
   Matrix.inv(cov,cov);
   
    for (int j=1;j<=nPolys;j++){
      for (int k=1; k<=nBiol; k++)
       w[k]=biolData[j][k]-mean[k];
        //BofX is the normal pdf value for this regime 
        data[j].BofX[sequence]=1./(2*Math.pow(Math.PI,nBiol/2.)*Math.sqrt(det))*
		  Math.exp(-Matrix.VtxAxV(w,cov)/2) + Math.log(det)/2;     
    } /*end for j*/
    data[1].regime[sequence]=i;//so we know where regime i is stored in BofX
 } /*end for i*/  
 
  maxBio=0.0; sumB=0.0; nPositive=0;
 for (int ig=1;ig<=nPolys;ig++){
    if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
    int i=ig-polyFirst+1; //transform global to local polygon index 
    int z=Algorithm.randomSchedule(ig); //set validScheds correctly 
    maxBio+=data[i].BofXByRank(1,validScheds,nScheds);
    sumB+=data[i].BofX(polySched[ig]);
 }/*end for ig*/

inComponents=0;
 for (int j=1;j<=parent.nComponents;j++)
    if(parent.isAdded[j])inComponents++; //current n of components in obj function
    
 //for (int j=1;j<=nPolys;j++){ //to look at the BofX values
 //    for (int i=1; i<=regimeLast; i++)
  //   parent.console.println("poly regime BofX "+j+" "+i+" "+data[i].BofX(i));
  //}/*end for polys*/
 
} /*end init()*/ 

/**
*Compute Objective Function change when one polygon changes
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

  double  Bx=data[i].BofX(polySched[ig]);
  double  Bz=data[i].BofX(z);

   tempSumB=tempSumB-Bx+Bz; //use in duringIter() if z proposal is accepted   
if (rank==-1){   //the goal is relative to maxBio, 
 
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
  
  if (Bx==-1 || Bz==-1)Bdelta=Bdelta;  //should have no influence if there was no training data for either regime  
   if (Bx>=Brank)nPositive++;
 nTotal++;
} /*end of relative to rank block*/
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
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){
 
 double lower=0.,upper=1.,ratio=0.;
 double Scale=weight; //used to determine if weight has converged
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
    maxBio+=data[i].BofXByRank(1,validScheds,nScheds);
    sumB+=data[i].BofX(polySched[ig]);
   }/*end for ig*/ 
 
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
 Scale=(float)((ratio>upper)?Scale*.9:Scale); 
 Scale=(float)((ratio<lower)?Scale/.9:Scale);
 
 
 if (Scale==weight)converged=1; //indicates convergence
 else if (Scale<weight)converged=2; //overConverged
 else converged=0;   //underConverged  
  
  if(Scale<minValue)Scale=minValue;
 else if(Scale>Float.MAX_VALUE){
     isWt2big=true;
     Scale=Float.MAX_VALUE;
 }
 weight=Scale;  
 
 p4.text.setText(String.valueOf((float)weight));
  //write some output
  goalValue=ratio; //used by fitness function
  ratio=Math.floor(ratio*100)/100.;
 outLabel.setText(" Actual goal: " + ratio); 
}/*end postIter()*/


} /*end BiolForm class*/
