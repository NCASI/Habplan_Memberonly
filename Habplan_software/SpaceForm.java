import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import org.xml.sax.Attributes;

/**
*Make an Objective Function Spatial Model Component form
*Contains all the methods needed for the Spatial component.
*@author Paul Van Deusen (NCASI)  4/98
*/
public class SpaceForm extends ObjForm {
   JFileButton loadButton;
   JLabelTextField p3;
   JLabelTextScroll p2;
   JTextField polyId; //enter an id to check the data
   JTextArea textBeta; //for entering betas
    JScrollPane textPane; //holds textBeta

   JLabel outLabel; //output goes here
   JPanel pp; //look at data panel
    ModelSelectPanel modelSelectPanel; //select spatial model type
    JRadioButton[] model;  //the model selected in the modelSelectPanel
   
   //userInput Spatial Component Variables
  protected String blockFile;
  protected int[][] betas; //matrix of beta values
  protected float[][] cs; //matrix of c values for area model
  
  protected float goal;
   
   BlockData[] data;
   static int yrs;
  
   int nPositive=0; //number of obj changes>0 for adjusting weight
   int nTotal=0;  //actual n of polygons available for changeing when evaluating weight
   int maxNabes; //holds max n of nabes for any polygon, computed during data read
   

/**
*indicates whether  component has a Graph associated with it
*/
//public boolean isGraph(){ return(true);}

public SpaceForm(FrameMenu parent, String s){
  super(s, 10, 1);
  this.parent=parent;
  getContentPane().setBackground(Color.black);
  ATextListener listener=new ATextListener();
  p1=new JLabelTextField("Spatial fileName",20,"str");
  p1.setBackground(Color.getColor("habplan.spacemod.background"));
  p1.text.addCaretListener(listener);
  
  p2=new JLabelTextScroll("Goal",4,0,100,100);
  p2.setBackground(Color.getColor("habplan.spacemod.background"));
  p2.text.addCaretListener(listener);
  p3=new JLabelTextField("Weight",8,"float");
  p3.setBackground(Color.getColor("habplan.spacemod.background"));
  p3.text.addCaretListener(new WtTextListener());
  p3.text.setText("1");
  
  //TextArea for entering betas
  textBeta=new JTextArea("",6,5);
  textBeta.setBackground(Color.white);
  textBeta.addCaretListener(listener);
  textPane=new JScrollPane(textBeta);
  
  //output label
  outLabel=new JLabel(" ");
   outLabel.setForeground(Color.white);
   outLabel.setBackground(Color.black);
  
  loadButton= new JFileButton("File Selector",parent,p1.text,null,null);
  loadButton.setBackground(Color.getColor("habplan.spacemod.background"));
  //create a data verify field
  pp = new JPanel();
  pp.setBackground(Color.getColor("habplan.spacemod.background"));
  JLabel verify = new JLabel("Verify data for Polygon #");
  pp.add(verify);
  polyId = new JTextField(" ",4);
  polyId.setBackground(Color.lightGray);
  polyId.addActionListener(new PolyActionListener());
  pp.add(polyId);
  
  //create the spatial model selector
  modelSelectPanel=new ModelSelectPanel(2);
  

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

    /**
     *Make a panel to allow selection of different spatial model types
     * nModels specifies the number of models
     */
    public class ModelSelectPanel extends JPanel{

	public ModelSelectPanel(int nModels){
	    setBackground(Color.getColor("habplan.spacemod.background"));
	    ButtonGroup group = new ButtonGroup();
	    model=new JRadioButton[nModels];
            boolean status=true;   //first model is selected
	    for(int i=0;i<nModels;i++){       
		model[i]=new JRadioButton("model("+i+")");
		group.add(model[i]);
		add(model[i]);
	    }/*end for*/
            model[0].setSelected(true);
	}/*end constructor*/

    }/*end inner class*/


/**
*This inner class holds data associated with the Spatial component
*/
class BlockData {
   float size;
   int[] nabes; //the neighbors of the polygon
} /*end inner class*/

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
 JPanel outPanel=new JPanel();
 outPanel.setBackground(Color.black);
 outPanel.add(outLabel);
 
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(2,1));
  getContentPane().add(p2); //goal
  getContentPane().add(outPanel);
  pack();
 }/*end if*/
 else{
  getContentPane().removeAll();
  JPanel top=new JPanel();
  top.setBackground(Color.getColor("habplan.spacemod.background"));
  getContentPane().setLayout(new BorderLayout());
  top.setLayout(new GridLayout(7,1));
  top.add(loadButton);
  top.add(p1);top.add(p2);top.add(p3);
  top.add(outPanel);
  top.add(pp);
  JLabel lab1=new JLabel("Add betas as opt(i) opt(j) beta; triplets separated by ;");
  top.add(lab1);
  getContentPane().add(top,BorderLayout.NORTH);
  getContentPane().add(textPane,BorderLayout.CENTER);
  getContentPane().add(modelSelectPanel,BorderLayout.SOUTH);
  pack();
 }/*end else*/
} /*end shrink()*/

   

/**
*Let the user look at the data
*/
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

  s="Spatial data for Polygon " + s + "\n \n";
  s=s+"Size = "+data[Id].size+"\n";
   s=s + "  Neighbors ... \n"; 
   
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
    vec[0]=p1.text.getText(); //filename
    vec[1]=p2.text.getText(); //goal
    vec[2]=p3.text.getText(); //wt
    vec[3]=textBeta.getText().trim(); //betas
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){     
    p1.text.setText((String)vec[0]); //filename
    p2.text.setText((String)vec[1]); //goal
    p3.text.setText((String)vec[2]); //wt
    textBeta.setText((String)vec[3]); //betas
     }

/**
*Save users settings to an xml file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
  out.println("<spatial title=\""+getTitle().trim()+"\">");
  out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
  out.println("  <goal value=\""+p2.text.getText().trim()+"\" />");
  out.println("  <weight value=\""+p3.text.getText().trim()+"\" />");
  out.println("  <betas value=\""+textBeta.getText().trim()+"\" />");
  out.println("  <title value=\""+title+"\" />");
  out.println("</spatial>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("goal"))p2.text.setText(atts.getValue(0));
	if(qName.equals("weight"))p3.text.setText(atts.getValue(0));
	if(qName.equals("betas"))textBeta.setText(atts.getValue(0));
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
  blockFile=p1.text.getText().trim();
  goal=Float.valueOf(p2.text.getText().trim()).floatValue();
  
 //make a betas matrix filled with zeros;
 betas=new int[nextRegime][nextRegime];
 //make a cs matrix filled with infinities;
 cs=new float[nextRegime][nextRegime];
 for(int i=0;i<nextRegime;i++)
     for(int j=0;j<nextRegime;j++)
	 cs[i][j]=Float.MAX_VALUE;
 //check for commas and change to blanks
 String text = textBeta.getText().trim();
  text=text.replace(',',' ');
  String sub,subi,subj,beta,cVal; //use to pull out numbers from the string
	int i=0,j=0, finish=0;
	  while (text.length()>0){
	   textBeta.setBackground(Color.red);
	  finish = text.indexOf(";")+1; //index of next  separator
	  finish=finish>0?finish:text.length();
	  sub=text.substring(0,finish);
	  text=text.substring(finish,text.length()).trim(); //throw out used part;
	  subi=sub.substring(0,sub.indexOf(" ")).trim(); //get i index
	  sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
	  subj=sub.substring(0,sub.indexOf(" ")).trim(); //get j index
	  sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
          cVal="0"; //default c value
          if(sub.indexOf(" ")!=-1){
	   cVal=sub.substring(0,sub.indexOf(" ")).trim(); //get c value;
	   sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
          }
           beta=sub.substring(0,sub.indexOf(";")).trim(); //get beta value; 
        
	  i=Integer.parseInt((String)regimeTable.get(subi));
	  j=Integer.parseInt((String)regimeTable.get(subj));
	  betas[i][j]=Integer.parseInt(beta);
	  betas[j][i]=betas[i][j]; //keep it symetric
          cs[i][j]=(new Float(cVal).floatValue());
          if(cs[i][j]!=0f)
	     cs[j][i]=1f/cs[i][j]; //make inverse-symetric
          else cs[j][i]=Float.MAX_VALUE;
       }/*end while*/

 textBeta.setBackground(Color.white); 
   
   }
 catch(Exception e){
   }

}/*end readTheForm()*/

/**
*This transfers beta TextArea input to betas matrix
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
*This transfers GUI input to weight variable
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
   parent.console.println("No Spatial Model datafile given??");
   return(false);
   }/*end if*/
  
try{

 Float.valueOf(p2.text.getText().trim()).floatValue();
 Float.valueOf(p3.text.getText().trim()).floatValue();
 
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the Spatial Model form!");
  return(false);
   } 
}/*end verify()*/


/**
*read flow data from a file
*This is nearly identical to BlockForm.readData
*this excludes references to parent FlowForm
*because its not a subcomponent
*/

public boolean readData(){

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
         if (pass==1) maxNabes=(maxNabes>(tempNabes))?maxNabes:(tempNabes); //get max n of columns
         tempNabes=0;
	 nPolysRead++;} /*fi*/

        tempNabes+=n-2; //count neighbors over multi-line input
      
      if (pass==2 && polyTable.containsKey(polyNow) && polyFirst==-999)
	  polyFirst=Integer.parseInt((String)polyTable.get(polyNow)); //get the internal id

       if (pass==2 & !polyNow.equals(polyBefore)){//store data before reading next Polygon
          
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  data[ID].size=size;
	  data[ID].nabes = new int[nabeCnt+1];
	    for (int i=1; i<=nabeCnt; i++){
	    polyNabe=tempNames[i]; 
	    int nabeID=Integer.parseInt((String)polyTable.get(polyNabe));
	    data[ID].nabes[i]=nabeID;    
	  }/*end for i*/
	   nabeCnt=0; //reset before reading nabes from next polygon
	}/*end if pass==2 &tempNabes==0*/

       polyBefore=polyNow; //remember polygon name
	 
	String tempSize=token.nextToken(); //size
	try{size=new Double(tempSize).floatValue();
	}catch(Exception es){parent.console.println("ERROR: Check size after line "+nLines+"\n"+line); size=1;}
	if(size==0){sizeWarning=true;size=1;}
    
      if (pass==2){ //read and count non-zero nabes
	  for (int i=1; i<n-1; i++){
	   polyNabe=token.nextToken(); //skip if its a 0
	   if(polyNabe.equals("0.") | polyNabe.equals("0.0") | polyNabe.equals("0")) continue;
	 if(!polyTable.containsKey(polyNabe)) continue;
            nabeCnt++;
	    tempNames[nabeCnt]=polyNabe;  //only retain non-zero legitimate nabes 
	  }/*end for i*/  

      }/*end if pass==2*/
	
     nLines++;

    }/*end of while loop*/

    if (pass==2 ){//store data from the last polygon
          
	  int ID=Integer.parseInt((String)polyTable.get(polyBefore));
	  data[ID].size=size;
	  data[ID].nabes = new int[nabeCnt+1];
	  
	  for (int i=1; i<=nabeCnt; i++){
	    polyNabe=tempNames[i]; 
	    int nabeID=Integer.parseInt((String)polyTable.get(polyNabe));
	   data[ID].nabes[i]=nabeID;     
	  }/*end for i*/
     
	}/*end if pass==2 */
    
    if (pass==2){ 
      //for (int i=1;i<nextRegime;i++) parent.console.println("nextRegime i regimeTable regimeArray "+nextRegime+" "+i+" "+regimeTable.get(Double.toString(i))+" "+regimeArray.elementAt(i-1));
      if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     if(sizeWarning)parent.console.println("Warning: Polygons with size=0 were reset to size=1.");
     parent.console.println("Successfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast);
     dataRead=true; //if you get here the data was read OK
   } /*end if pass==2*/ 
      
} /*end try*/
   catch (Exception e) {
    parent.console.println("Error: reading " + fileName + "\n" + e);
    nextPolygon=nextPolygonSave; //restore to old value
    dataRead=false; }
finally {  try { if (in != null) {in.close();} } catch (IOException e) {} }
  
  }/*end pass loop*/
    parent.console.println("Maximum neighbors per polygon = "+maxNabes);
  return dataRead;
 }/*end readData()*/
 
 
 /**
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
 nPositive=0;
 /**uncomment to see the betas or cs
 for (int i=1;i<=nOptions;i++){
   parent.console.print("\n");
 for (int j=1;j<=nOptions;j++)
  parent.console.print(cs[i][j]+" ");
 }
 */
 
} /*end init()*/ 

    /**
     *Compute Objective Function change when one unit changes
     *its management regime.  
     *@param members contains indices of member polys, z is the new regime
     */
    public double deltaObj(int[] members, int z){
	
		double delta=0,scale=0.;
  for(int mem=0;mem<members.length;mem++){ //loop through the members
	int ig=members[mem]; //process one member of this unit at a time
	if(ig==0)continue; //no data for this member

      if (ig<polyFirst || ig>polyLast)continue; //not a local polygon so do whatever
      int i=ig-polyFirst+1; //transform global to local polygon index 

	int x=polySched[ig];

	if(model[0].isSelected()) //model that counts n of h nabes
	 for(int h=1;h<=regimeLast;h++){
	    int f=nOfNabes(i,h);
	    delta+=(betas[x][h]-betas[z][h])*f;
	 }/*end for h*/
        else if(model[1].isSelected()) //model looks for enough area in h nabes
	  for(int h=1;h<=regimeLast;h++){
	    int f1=isNabeArea(i,h,cs[x][h]);
	    int f2=isNabeArea(i,h,cs[z][h]);           
	    // System.out.println("x z h f1 f2 "+x+" "+z+" "+h+" "+f1+" "+f2);
	    delta+=betas[x][h]*f1-betas[z][h]*f2;
	  }/*end for h*/

	if (delta>0.0)nPositive++;
	nTotal++;
  }/*end for mem*/

	delta*=weight;
	//parent.console.println("i,x,z,delta "+i+" "+x+" "+z+" "+delta);
	return (delta);
    }/*end deltaObj()*/


 /**
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){

   float Rate=goal;
 double Scale=weight;
 double Low=0.,Hi=1.,ratio=0.;
  ratio=(1.-(float)nPositive/nTotal);
  nPositive=0; //reset for next iteration
  nTotal=0;
  
 Hi=(Rate+.05);
 Hi=(Hi<1.)?Hi:1.;
 Low=(Rate==1)?Rate:(Rate-.05);  //special consideration when user wants Rate=1
 Low=(Low>0.)?Low:0.;
  
 Scale=(float)((ratio>Hi)?Scale*.9:Scale); 
 Scale=(float)((ratio<Low)?Scale/.9:Scale);
 
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
 outLabel.setText(" Actual Goal: " + ratio); 
 
  
}/*end postIter()*/

   /**
    *Find the number of neighbors for polygon polyId managed under
    *regime (option) h
    *@param polyId is the polygon ID
    *@param h is the option of interest
    */
    final int nOfNabes(int polyId, int h){

	int count=0;
	int nnabes=data[polyId].nabes.length-1;
	if (nnabes==0) return(0); /*no neighbors*/
	int[] nabesi=data[polyId].nabes;//points to neighbor vector

	for (int i=1;i<=nnabes;i++)if(polySched[nabesi[i]]==h)count++;
	return(count);

    }/*end nOfNabes()*/

    /**
     * @return 1 if cumulative area of the neighbors of polygon polyId
     * managed under regime (option) h is greater than proportion c.
     *@return 0 otherwise
     *  Added August 2001
     *@param polyId is the polygon ID
     *@param h is the option of interest
     *@parem c is the proportion
    */
    final int isNabeArea(int polyId, int h, float c){
	float cumArea=0f; //hold cum area of nabes with regime h
	int nnabes=data[polyId].nabes.length-1;
	if (nnabes==0) return(0); /*no neighbors*/
	int[] nabesi=data[polyId].nabes;//points to neighbor vector

	for (int i=1;i<=nnabes;i++)
	    if(polySched[nabesi[i]]==h)cumArea+=data[nabesi[i]].size;
        if(cumArea>c*data[polyId].size)return(1);  
	else return(0);

    }/*end method()*/

} /*end SpaceForm class*/
