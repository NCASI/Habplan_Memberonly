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
*Contains all the methods needed for the flow component.
*@author Paul Van Deusen (NCASI)  12/97
*/
public class FlowForm extends ObjForm {
     ATextListener aTextListener;
    JMenuBar menuBar;
    FlowLinkMenu flowLinkMenu=null;
   JFileButton loadButton;
   JLabelTextField byPanel,p2,p6,p7,tL,tU,flowWindow;
   JPanel threshHold;  //hold thresholds tL and tU
   JPanel goalLimits; //hold goal thresholds
   JLabelTextScroll g0,g1,p5;
   JTextArea textModel;
    JScrollPane textScroll;
   JTextField polyId; //enter an id to check the data
   JLabel outLabel; //output goes here
   JCheckBox checkModel;  //decide whether to use internal model
   JCheckBox checkFlow; //decide if 0 Flow allowed
   JPanel pp; //look at data panel
   int[] subFlows=new int[UserInput.nComponents+1]; //hold index of subflow components
   boolean isSuperFlow=false; //tells if this is a super Flow component
    boolean isFirstFlowRead=false; //is this the first flow entered
   int[] regimeActions; //hold n of action yrs for each regime
 
   Vector doNothings=new Vector(5); //holds year 0 options for BlockForm notBlocks
   int theDoNothing=0;  //holds an option that is doNothing for all periods
   int[] byYrs;  //pointer to byGone yrs
   int nByYrs; //n of byYrs  
   boolean byHas1=false; //indicates if year 1 is byGone
   boolean bySkip=false;  //used to decide if a byYear should be skipped in deltaObj()  
    
   //variables related to the objective function
   FlowData[] data; //class to hold the data read from the file
   int xMin, xMax; // the years shown on the graph;
   float[] gama, gamaD,gamaz,gamazD, g;
   float[] model; //holds user input model values
   float wtFactor=10000000; //used to scale the weight so 1.0 is in ballpark
  
   //user input variables
  String flowFile;
  protected  float gama0=0; //time 0 starting value
  protected  float goal0=0;  //preferred deviation from gama0
  protected  float goalF;  //preferred yr to yr deviation
  protected  float rate;  // interest rate for flow trend
  protected  double weightF=1; //control variance of flow model
  protected  double weight0=1; //control variance of time 0 model
  protected  float threshLow; //lower threshhold
  protected  float threshHi; //upper threshhold
  protected  float goalWidth;   // flow goal + or - percent
   
public static void main (String args[]) {
   FrameMenu frame=new FrameMenu ("FlowForm Parent Frame");
    frame.setBounds(200,200,400,200);
   //frame.show();
   Frame form= new FlowForm(frame, "FlowForm Dialog");
   form.show();
}/*end main*/

/**
*indicates whether  component has a Graph associated with it
*/
public boolean isGraph(){ return(true);}

public FlowForm(FrameMenu parent, String s){
  super(s, 10, 1);
  this.parent=parent;
  menuBar = new JMenuBar();
  graph = new Figure(s,1,1,UserInput.hbpDir);
  aTextListener=new ATextListener();
  p1=new JLabelTextField("file/subFlows",20,"str");
  p1.text.addCaretListener(aTextListener);
  p1.setBackground(Color.getColor("habplan.flow.background"));
  byPanel=new JLabelTextField("ByGone Yrs",10,"str");
  byPanel.text.addCaretListener(aTextListener);
  byPanel.setBackground(Color.getColor("habplan.flow.background"));
  p2=new JLabelTextField("Time0:start flow",8,"float");
  p2.text.addCaretListener(aTextListener);
  p2.text.setText("10000");
  p2.setBackground(Color.getColor("habplan.flow.background"));
  tL=new JLabelTextField("Target Threshold: -",6,"float");
  tL.text.addCaretListener(aTextListener);
  tL.text.setText("0.0");
  tL.setBackground(Color.getColor("habplan.flow.background"));
  tU=new JLabelTextField(": +",6,"float");
  tU.text.addCaretListener(aTextListener);
  tU.text.setText("0.0");
  tU.setBackground(Color.getColor("habplan.flow.background"));
  threshHold=new JPanel();
  threshHold.add(tL);
  threshHold.add(tU);
  threshHold.setBackground(Color.getColor("habplan.flow.background"));
  g0=new JLabelTextScroll("Goal",4,0,100,100);
  g0.text.addCaretListener(aTextListener);
  g0.text.setText("0.1");
  g0.setBackground(Color.getColor("habplan.flow.background"));
  g1=new JLabelTextScroll("Flow Goal",4,0,100,100);
  g1.text.addCaretListener(aTextListener);
  g1.text.setText("0.5");
  g1.setBackground(Color.getColor("habplan.flow.background"));
  flowWindow=new JLabelTextField("Goal +/- ",4,"float");
  flowWindow.text.addCaretListener(aTextListener);
  flowWindow.text.setText(".05"); //make sure window is set to reasonable default
   flowWindow.setBackground(Color.getColor("habplan.flow.background"));

  p5=new JLabelTextScroll("Flow Model Slope Rate",4,-100,100,100);
  p5.text.addCaretListener(aTextListener);
  p5.text.setText("0.0");
  p5.setBackground(Color.getColor("habplan.flow.background"));
  p6=new JLabelTextField("Weight: Flow",8,"float");
  p6.text.addCaretListener(new WtTextListener());
  p6.text.setText("1");
  p6.setBackground(Color.getColor("habplan.flow.background"));
  p7=new JLabelTextField("Time0",8,"float");
  p7.text.addCaretListener(new WtTextListener());
  p7.text.setText("1");
  p7.setBackground(Color.getColor("habplan.flow.background"));
  checkModel = new JCheckBox("No Model",false);
  checkModel.addItemListener(new CheckListener());
  checkModel.setBackground(Color.getColor("habplan.flow.background"));
  checkFlow = new JCheckBox("Allow 0 Flow",false);
  textModel=new JTextArea("",3,25);
  textModel.addCaretListener(aTextListener);
  textModel.setBackground(Color.white);
  textScroll=new JScrollPane(textModel);
  
  //output label
  outLabel=new JLabel("Actual Goal: ");
   outLabel.setBackground(Color.black);
   outLabel.setForeground(Color.white);
  
  loadButton = new JFileButton("File Selector",parent,p1.text,null,null);  
  loadButton.setBackground(Color.getColor("habplan.flow.background"));
  
  //create a data verify field
  pp = new JPanel();
  JLabel verify = new JLabel("Verify data for Polygon #");
  pp.add(verify);
  pp.setBackground(Color.getColor("habplan.flow.background"));
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
     *remove the Slave Listeners added by addSlaveUpdateListeners
     *They should have the same textfield vector
     *@param f is the FlowForm to free from slavery
     */
 protected void removeLinkUpdateListeners(FlowForm f){
      CaretListener[] cL;
      LinkTextListener sL;
      //the textFields to remove listeners from
      JTextField[] tF={g0.text,g1.text,flowWindow.text,p5.text};

      for(int j=0;j<tF.length;j++){
	cL=tF[j].getCaretListeners();
	for(int i=0;i<cL.length;i++){
	    if(!(cL[i] instanceof LinkTextListener))continue;
	    sL=(LinkTextListener)cL[i];
	    if(sL.getIdNum()==f.idNum)tF[j].removeCaretListener(cL[i]);
	}
      }

      //enable checkBoxMenuItem on form f, now there is no danger
	//from creating a circular link, change color
	String fName=getTitle().substring(0,2);
	for(int i=1;i<flowLinkMenu.nFlow;i++){
	    if(f.flowLinkMenu.box[i].getText().equals(fName)){
	       f.flowLinkMenu.box[i].setEnabled(true);
	       f.flowLinkMenu.box[i].setBackground(Color.lightGray.brighter());
	       break;
	    }
	}
    }


    /**
     *Add the caret Listeners to all textfields in slave FlowForm f
     *The fields will update whenever the corresponding field in this
     *master form updates.  The tF array should be identical to the
     *tF array in removeSlaveUpdateListeners().  Also fTF points to
     *the corresponding enslaved flow fields.
     **@param f is the FlowForm to enslave
     **/
    protected void addLinkUpdateListeners(FlowForm f){
	//TextField in this flowform to add listeners to
	JTextField[] tF={g0.text,g1.text,flowWindow.text,p5.text};
	//Corresponding textfield in enslaved form
	JTextField[] fTF={f.g0.text,f.g1.text,f.flowWindow.text,
			  f.p5.text};
	for(int j=0;j<tF.length;j++){
	   tF[j].addCaretListener(new LinkTextListener(f.idNum,tF[j],fTF[j]));
	}

	//disable checkBoxMenuItem on form f to prevent user
	//from creating a circular link, change color
	String fName=getTitle().substring(0,2);
	for(int i=1;i<flowLinkMenu.nFlow;i++){
	    if(f.flowLinkMenu.box[i].getText().equals(fName)){
	       f.flowLinkMenu.box[i].setEnabled(false);
	       f.flowLinkMenu.box[i].setBackground(new Color(250,120,0));
	       break;
	    }
	}
    }

  

/**
*Shrink or unshrink the menu so it can use
*less screen realEstate
*/
public void shrink(boolean shrunk){
    if(UserInput.nFlow>1 && flowLinkMenu==null){//only do this once
     flowLinkMenu=new FlowLinkMenu(parent,this);
     menuBar.add(flowLinkMenu);
     setJMenuBar(menuBar);
    }
    getContentPane().setBackground(Color.black); //lets the label be black.
  JPanel flowPanel=new JPanel(); //holds 0 flow checkbox and flow goal
  //flowPanel.add(checkFlow); don't add this for the time being
  flowPanel.setBackground(Color.getColor("habplan.flow.background"));
  flowPanel.add(flowWindow);  flowPanel.add(g1);
  JPanel zeroPanel = new JPanel();
  zeroPanel.setBackground(Color.getColor("habplan.flow.background"));
  zeroPanel.add(p2); zeroPanel.add(g0);
if (checkModel.isSelected()==false){
 if (!shrunk){  getContentPane().removeAll();
  if(!byHas1){
      getContentPane().setLayout(new GridLayout(5,1));
      getContentPane().add(threshHold);
      getContentPane().add(zeroPanel);
  }else {setLayout(new GridLayout(4,1));getContentPane().add(threshHold);}
  getContentPane().add(flowPanel);
  getContentPane().add(p5);
  getContentPane().add(outLabel);
  pack();
 }/*end if*/
 else{
  getContentPane().removeAll(); 
  if(byHas1)getContentPane().setLayout(new GridLayout(9,1));
  else getContentPane().setLayout(new GridLayout(10,1));
  JPanel bPanel=new JPanel();
  bPanel.setBackground(Color.getColor("habplan.flow.background"));
  bPanel.add(checkModel);
  bPanel.add(loadButton);
  getContentPane().add(bPanel);
  getContentPane().add(p1);
  getContentPane().add(byPanel);
  if(!byHas1){ getContentPane().add(zeroPanel);}
   getContentPane().add(threshHold);
   getContentPane().add(flowPanel);
   getContentPane().add(p5);
   getContentPane().add(p6);
  if(!byHas1){
   remove(p6);
   JPanel wtPanel=new JPanel();
   wtPanel.setBackground(Color.getColor("habplan.flow.background"));
   wtPanel.add(p6);
   wtPanel.add(p7);
   getContentPane().add(wtPanel);
   }/*end if !byHas1*/
  getContentPane().add(outLabel);
  getContentPane().add(pp);
  pack();
 }/*end else*/
  }/*end if checkModel=false*/
 if (checkModel.isSelected()==true){
 if (!shrunk){
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(4,1));
  getContentPane(). add(threshHold);
  getContentPane().add(flowPanel);
  getContentPane().add(textScroll);
  getContentPane().add(outLabel);
  pack();
 }/*end if*/
 else{
    getContentPane().removeAll();
    getContentPane().setLayout(new GridLayout(3,1));
    
    JPanel checkSlope=new JPanel();
    checkSlope.setBackground(Color.getColor("habplan.flow.background"));
    checkSlope.add(checkModel); checkSlope.add(p5);
    
    JPanel dum1=new JPanel();
    dum1.setBackground(Color.getColor("habplan.flow.background"));
    dum1.setLayout(new GridLayout(3,1));
    dum1.add(checkSlope);
    dum1.add(threshHold);
    dum1.add(flowPanel);
    getContentPane().add(dum1);
    
    JLabel modLabel=new JLabel("Add year,flow pairs to textarea separated by ;");
    JPanel modPanel=new JPanel();
    modPanel.setBackground(Color.getColor("habplan.flow.background").brighter());
    modPanel.add(modLabel);
    getContentPane().add(textScroll);
    JPanel dum5=new JPanel();
    dum5.setBackground(Color.black);
    dum5.setLayout(new GridLayout(3,1));
    dum5.add(modPanel);
    dum5.add(p6);
    dum5.add(outLabel);
    getContentPane().add(dum5);
    pack();
 }/*end else*/
 
  }/*end if checkModel==true*/  
} /*end shrink()*/

/**
*User clicks a checkbox
*/
public class CheckListener implements ItemListener{
 public void itemStateChanged(ItemEvent e){
   shrink(!parent.shrunk); 
   readTheForm();//force new settings to be read  
 }/*end itemStateChanged*/
} /*end inner CheckListener class*/



public class PolyActionListener implements ActionListener{
 public void actionPerformed(ActionEvent event){
   String s;
   int Id=1,regime=1;
   int j2; //gives the row where the option is found in flow
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
  
   s="Flow data for Polygon " + s + "\n \n";
   s=s+"Option Years  Flows  \n"; 
  for(int j=1; j<=regimeLast; j++){
    j2=data[Id].flowPtr(j);
    if (j2>0){
      s=s+regimeArray.elementAt(j) + " ";
   for (int i=1 ; i<data[Id].flow[j2].length;i++)
      s=s+ Integer.toString(data[Id].flow[j2][i]) + " ";
      s=s+"\n";
	    }/*end if j2>0*/ 
  }/*end for j*/
  
  parent.console.println(s);
   
 }/*end actionperformed*/
} /*end inner PolyActionListener class*/

 /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){
        vec[0]=new Boolean(checkModel.isSelected()); //noModel setting
	vec[1]=p1.text.getText(); //filename
	vec[2]=byPanel.text.getText(); //byGone years
	vec[3]=p2.text.getText(); //time 0 start value
	vec[4]=g0.text.getText(); //time 0 goal
	vec[5]=tL.text.getText(); //lower threshold
	vec[6]=tU.text.getText();  //upper threshold
	vec[7]=flowWindow.text.getText(); //flow goal +/-
	vec[8]=g1.text.getText();//flow goal
	vec[9]=p5.text.getText();//flow slope
	vec[10]=p6.text.getText();//flow weight
	vec[11]=p7.text.getText();//time 0 weight
	vec[12]=textModel.getText().trim();//specified model values
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
       checkModel.setSelected( ((Boolean)vec[0]).booleanValue() ); //noModel setting
       shrink(true);
       p1.text.setText((String)vec[1]); //filename
       byPanel.text.setText((String)vec[2]); //byGone years
       p2.text.setText((String)vec[3]); //time 0 start value
       g0.text.setText((String)vec[4]); //time 0 goal
       tL.text.setText((String)vec[5]); //lower threshold
       tU.text.setText((String)vec[6]);  //upper threshold
       flowWindow.text.setText((String)vec[7]); //flow goal +/-
       g1.text.setText((String)vec[8]);//flow goal
       p5.text.setText((String)vec[9]);//flow slope
       p6.text.setText((String)vec[10]);//flow weight
       p7.text.setText((String)vec[11]);//time 0 weight
       textModel.setText((String)vec[12]);//specified model values
     }
/**
*clear all settings.  Done when user wants to load a new problem without quitting
*The hope is that this will allow users to load another job without quitting
*see FrameMenu.reInitialize() also
*/    
 protected void clearSettings(){
      xMin=10000;
      xMax=-1;
      nByYrs=0; //byGone years
      isSuperFlow=false;
       doNothings.clear();//clear doNothings
       byHas1=false; //indicates if year 1 is byGone
       bySkip=false;  //used to decide if a byYear should be skipped in deltaObj() 
       nextRegime=1;  //Static var, used to index the management regimes
       nextPolygon=1; //static var, index of polygons
       regimeLast=0;
     }/*end clearSettings()*/

/**
*Create the subFlow vector from user input
*Compare F's from user to each component for a
*match.  Case doesn't matter.  Textfield stays
*red if no match.  
*/
public void makeSubFlows(){
 
     String text = p1.text.getText().trim();
     text=text.replace(';',' '); text=text.replace(',',' ');
     isSuperFlow=true;
    for (int i=1; i<=UserInput.nComponents; i++)this.subFlows[i]=0; //initialize to 0
	String sub; //use to pull out Flows from the string
	int finish=0;
	  while (text.length()>0){
	   p1.text.setBackground(Color.red);
	  finish = text.indexOf(" "); //index of next  separator
	  finish=finish>0?finish:text.length();
	  sub=text.substring(0,finish);
	  text=text.substring(finish,text.length()).trim(); //throw out used part;
	  
	  for (int i=1; i<=UserInput.nComponents; i++){
	    if (parent.form[i].getTitle().regionMatches(true,0,sub,0,2)){
	    p1.text.setBackground(Color.white);
	      FlowForm f=(FlowForm)parent.form[i];
	     if (f.isSuperFlow){ //get the lower level flows
	      for (int j=1;j<=UserInput.nComponents;j++)
		if(f.subFlows[j]==1)this.subFlows[j]=1; //copy the 1's 
	     }else this.subFlows[i]=1; //this is first level of superflow
	    }/*end if regionMatches*/
	  }/*end for i*/
	  
       }/*end while*/
      
      //for (int i=1;i<=UserInput.nComponents;i++){
	//FlowForm fSub=null;
	//if (subFlows[i]==1){fSub=(FlowForm)parent.form[i];
	//parent.console.println("i element "+i+" "+fSub.getTitle());}}

}/*end makeSubFlows()*/


/**
*The reads the user's input on the component form
*It is called when text changes and when the components
*data are read.  This ensures that the input data is read
*even if the form is in shrunk mode
*/
public void readTheForm(){
 try{
 flowFile=p1.text.getText().trim();
 if (flowFile.indexOf(";")>0 || flowFile.indexOf(",")>0)makeSubFlows();
 goalF=Float.valueOf(g1.text.getText().trim()).floatValue();
 goalWidth=Float.valueOf(flowWindow.text.getText().trim()).floatValue();
 rate=Float.valueOf(p5.text.getText().trim()).floatValue();
 if (!byHas1){
 gama0=Float.valueOf(p2.text.getText().trim()).floatValue();
 goal0=Float.valueOf(g0.text.getText().trim()).floatValue();
  }/*end if !byHas1*/
 threshLow=Float.valueOf(tL.text.getText().trim()).floatValue(); 
 threshHi=Float.valueOf(tU.text.getText().trim()).floatValue();
 isColorsLoaded=false; //this will force reloading the graph colors file just in case
 
  //make a model vector filled with -999's;
  model=new float[xMax-xMin+2];
  for (int i=xMin;i<=xMax;i++)model[i-xMin+1]=-999;

 if (checkModel.isSelected()==true){ 
  
  //check for commas and change to blanks
  String text = textModel.getText().trim();
   text=text.replace(',',' ');
   String sub,suby,beta; //use to pull out numbers from the string
	int y,finish=0;
	
	  while (text.length()>0){
	   textModel.setBackground(Color.red);
	  finish = text.indexOf(";")+1; //index of next  separator
	  finish=finish>0?finish:text.length();
	  sub=text.substring(0,finish);
	  text=text.substring(finish,text.length()).trim(); //throw out used part;
	  suby=sub.substring(0,sub.indexOf(" ")).trim(); //get yr index
	  sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
	  beta=sub.substring(0,sub.indexOf(";")).trim(); //get beta value;
	 y=Integer.parseInt(suby);
	 if (y<xMin||y>xMax & xMax!=0)
	  parent.console.println("Year out of bounds in user input for FlowModel");
	 model[y-xMin+1]=Float.valueOf(beta).floatValue();
       }/*end while*/
	  modelFillIn(); //fill in gaps between user specified end points
	textModel.setBackground(Color.white);
 } /* end if (checkModel.isSelected()==true){*/

  
   }
 catch(Exception e){
   }
 // Get the byGone years
 
  boolean byChange=false; //indicates if a FlowForm shrink is needed
  if (byHas1)byChange=true; //prevent unnecessary calls to shrink 
  byYrs=new int[1]; //initialize in case user doesn't enter any byYears
  String text;
 try{
     
 text = byPanel.text.getText().trim();
  if (!text.equals("")){
	byYrs=new int[100];
	//check for different separators and change to blanks
	if (text.indexOf(";")>0 ||text.indexOf(",")>0){
	 text=text.replace(';',' '); text=text.replace(',',' ');
	 }
	String sub,sub2; //use to pull out numbers from the string
	int i=0, finish=0;
	 // parent.console.println("separator nNotBlocks text.length() " + " " + separator + UserInput.nNotBlocks + "  " + text.length());
	  while (text.length()>0){
	   byPanel.text.setBackground(Color.red);
	  finish = text.indexOf(" "); //index of next  separator
	  finish=finish>0?finish:text.length();
	  sub=text.substring(0,finish);
	  text=text.substring(finish,text.length()).trim(); //throw out used part;
	  sub2=sub.indexOf("-")>0?sub.substring(sub.indexOf("-")+1,sub.length()):sub;
	  sub=sub.indexOf("-")>0?sub.substring(0,sub.indexOf("-")):sub;
      
	  for (int ii=Integer.parseInt(sub);ii<=Integer.parseInt(sub2);ii++){
	   i++;
	   if (i>99)parent.console.println("Habplan needs to be reconfigured for this many byGone years.");
	    byYrs[i]=ii;
	   if (ii<=0){parent.console.println("ByGone Year out of range!");i+=100;}  //print warning and stay red;
	  } /*end for ii*/
       
       }/*end while*/
	  byHas1=false;
	  nByYrs=i;
	  for (int j=1;j<=nByYrs;j++){if (byYrs[j]==1)byHas1=true;
	     //parent.console.println("byYrs["+j+"]="+byYrs[j] +"  nByYrs="+nByYrs);
	  }
	  if (!byChange && byHas1){this.shrink(!parent.shrunk); byChange=true;} //only  use shrink if year 1 is byGone
	  if (byChange && !byHas1){this.shrink(!parent.shrunk); byChange=false;} //just in case user takes year 1 out
	   byPanel.text.setBackground(Color.white); 	  
  } /*endif text.equals("")*/
       
	    
  
   }/*end try*/
 catch(Exception e){ } 



}/*end readTheForm()*/

/**
*fill in the gaps in user specified target values 
*/
protected void modelFillIn(){
     int x1=xMin,x2=xMax; //hold years defining the gap
     boolean isgap=false; //indicates if first part of gap is defined
     float y1=0,y2=0; //hold gap endpoint values
    for(int t=xMin; t<=xMax;t++){
	if(!isgap & model[t-xMin+1]!=-999)//beginning of first gap
	   {y1=model[t-xMin+1];x1=t; isgap=true; continue;}
	if(isgap & model[t-xMin+1]!=-999)//end of gap
	 {y2=model[t-xMin+1];x2=t;
	   //now fill in the gap with linear interpolation
	 for(int x=x1+1;x<x2;x++) model[x-xMin+1]=y1+(x-x1)*(y2-y1)/(x2-x1);
	   //set up to look for next gap endpoint
	   y1=y2;x1=x2; 
	}/*end if isgap*/
    }/*end for t*/
}

    /**
     *Listener to change corresponding textFields on slave flowforms
     *When Master textfield changes, so must slave textfield
     */
    public class LinkTextListener implements CaretListener{
	JTextField textM, textS; //master and slave textFields
	int idNum; //idNum of enslaved FlowForm
	public LinkTextListener(int idNum,JTextField textM, JTextField textS){
	    this.textM=textM;
	    this.textS=textS;
	    this.idNum=idNum;
	}
	public void caretUpdate(CaretEvent event){
	    textS.setText(textM.getText().trim());
	}/*end textChangedValue*/

	public int getIdNum(){return(idNum);}
    }/*end inner Slave class*/

/**
*This transfers user input to private variables
*the input comes from this form, the fOut form settings
*are adjusted to be the same as this form
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
        weightF=wt[0]; //put dominant weight in position 0
	weight0=wt[1];
	//parent.console.println("Flow.setWeight: weightF= wt[0]="+weightF+" "+wt[0]);
        p6.text.setText(String.valueOf(weight0));
	p7.text.setText(String.valueOf(weightF));
    }/*end method*/

    /**
     *get the weight variable value(s)
     */
    public double[] getWeight(){
	double[] wt = {weightF, weight0} ;//dominant weight in position 0
	return(wt);
     }


    /**
     *scale the weight(s) by dividing by the supplied value
     *then call setWeight()
     */
    public void scaleWeight(double factor){
        weightF=weightF/factor;
	weight0=weight0/factor;
    }/*end method*/

/**
*This transfers user input to  the weights
*Weights get a separate listener because they are
*updated each iteration
*/
public class WtTextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 try{
 weightF=Float.valueOf(p6.text.getText().trim()).floatValue();
 weight0=Float.valueOf(p7.text.getText().trim()).floatValue();
   }
 catch(Exception e){ 
   }
 }/*end textChangedValue*/
} /*end inner WtTextListener class*/


/**
*Save users settings to a file for possible future use
*/
public void writeSettings(PrintWriter out){
    out.println("");
    String aTitle=getTitle();
  out.println("<flow title=\""+aTitle+"\">");
 out.println("  <file value=\""+p1.text.getText().trim()+"\" />");
 out.println("  <bygone value=\""+byPanel.text.getText().trim()+"\" />");
 out.println("  <time0 value=\""+p2.text.getText().trim()+"\" />");
 out.println("  <goal0 value=\""+g0.text.getText().trim()+"\" />");
 out.println("  <threshLo value=\""+tL.text.getText().trim()+"\" />");
 out.println("  <threshHi value=\""+tU.text.getText().trim()+"\" />");
 out.println("  <goalPlus value=\""+flowWindow.text.getText().trim()+"\" />");
 out.println("  <goalF value=\""+g1.text.getText().trim()+"\" />");
 out.println("  <slope value=\""+p5.text.getText().trim()+"\" />");
 out.println("  <weightF value=\""+p6.text.getText().trim()+"\" />");
 out.println("  <weight0 value=\""+p7.text.getText().trim()+"\" />");
 out.println("  <isModel value=\""+checkModel.isSelected()+"\" />");
 out.println("  <model value=\""+textModel.getText().trim()+"\" />");
 out.println("  <title value=\""+graph.getGTitle()+"\" />");
 Rectangle bds=graph.getBounds();
 out.println("  <bounds height=\""+bds.height+"\" width=\""+bds.width+"\" x=\""+bds.x+"\" y=\""+bds.y+"\" />");
  out.println("</flow>");
}

   /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("file"))p1.text.setText(atts.getValue(0));
	if(qName.equals("bygone"))byPanel.text.setText(atts.getValue(0));
	if(qName.equals("time0"))p2.text.setText(atts.getValue(0));
	if(qName.equals("goal0"))g0.text.setText(atts.getValue(0));
	if(qName.equals("threshLo"))tL.text.setText(atts.getValue(0));
	if(qName.equals("threshHi"))tU.text.setText(atts.getValue(0));
	if(qName.equals("goalPlus"))flowWindow.text.setText(atts.getValue(0));
	if(qName.equals("goalF"))g1.text.setText(atts.getValue(0));
	if(qName.equals("slope"))p5.text.setText(atts.getValue(0));
	if(qName.equals("weightF"))p6.text.setText(atts.getValue(0));
	if(qName.equals("weight0"))p7.text.setText(atts.getValue(0));
	if(qName.equals("model"))textModel.setText(atts.getValue(0));
	if(qName.equals("isModel"))checkModel.setSelected(Boolean.valueOf(atts.getValue(0)));
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
*make sure this form is filled out
*/
public boolean verify(){
  
 if(p1.text.getText().trim().length()<3){
   parent.console.println("No Flow datafile given??");
   return(false);
   }/*end if*/
  
try{
 Float.valueOf(g1.text.getText().trim()).floatValue();
Float.valueOf(flowWindow.text.getText().trim()).floatValue();
 Float.valueOf(tL.text.getText().trim()).floatValue();
 Float.valueOf(tU.text.getText().trim()).floatValue();
 Float.valueOf(p5.text.getText().trim()).floatValue();
 Float.valueOf(p6.text.getText().trim()).floatValue();
 if(!byHas1){ //don't need these if 1 is a byGone yr
  Float.valueOf(p2.text.getText().trim()).floatValue();
  Float.valueOf(g0.text.getText().trim()).floatValue();
  Float.valueOf(p7.text.getText().trim()).floatValue();
 }
 return(true);
   }
 catch(Exception e){
  parent.console.println("Fill out the Flow form!");
  return(false);
   } 

}/*end verify()*/

/**
*Sets up the data when this is a superFlow component, i.e.
*it gets its data from sub components.
*/

public boolean readSuperFlowData(){
    boolean wasRead=false;
    FlowForm f=null;
  for (int i=1;i<=UserInput.nComponents;i++){
    if (subFlows[i]==0)continue;
      f=(FlowForm)parent.form[i];
      f.isFirstFlowRead=true; //avoid error messages about first seen polys
    if(!parent.isAdded[i]){ //read the subFlow data if necessary
     wasRead=f.readData();
      if (!wasRead){
	 parent.console.println("ERROR: Couldn't read subFlow data from "+f.getTitle());
	 return(false);
      }/*end if !wasRead*/	 
    }/*end if !parent.isAdded()*/ 
    parent.isAdded[i]=true;//add the data and check the box
    parent.compBox[i].setSelected(true);
   } /*end for i*/
   
   //now point to the first subFlows data and settings
   for (int i=1;i<=UserInput.nComponents;i++){
    if (subFlows[i]==0)continue;
      f=(FlowForm)parent.form[i];
      this.data=f.data;
      this.polyFirst=f.polyFirst;
      this.polyLast=f.polyLast;
      this.regimeLast=f.regimeLast;
      this.xMin=f.xMin;
      this.xMax=f.xMax;
     break;    
   } /*end for i*/
return(true);
}/*end readSuperFlowData()*/

/**
*read data from a file
*/

public boolean readData(){ 
   
  readTheForm(); //force reading of form settings just in case.
  if (dataRead)return(dataRead); //don't read the data unnecessarily
  dataRead=false; //return this at end
  int badCount=0; //count bad data rows, abort after 100
  if (isSuperFlow)return(readSuperFlowData());
  int regime; //temp var to hold regime index
  String regimeName=""; //hold a regime name
  String element=""; //converted to an integer for the hashTables
  
  int[][] tempFlow; //holds data temporarily
  tempFlow=new int[2][2]; tempFlow[1][1]=0; //defined here to fool compiler
  nextPolygonSave=nextPolygon; //use to reset polygon numbers if read fails
  xMin=100000; xMax=-100000; //starting values for x min and max;
  polyFirst=-999; polyLast=-999;
  regimeLast=-999;
  boolean isDoNothingRegime=false;  //set this to true if there are doNothing regimes
  
  fileName=findFile(p1.text.getText().trim()); //class function to filter filenames
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
      regimeActions=new int[nextRegime+1]; //hold n of actions per regime
      for(int i=0;i<regimeActions.length;i++)regimeActions[i]=1; //init to 1
   if (polyTable.isEmpty()){
       isFirstFlowRead=true;
    polyFirst=1; polyLast=nPolysRead;
    polyTable=new Hashtable(nPolysRead+1,(float)0.9);
    polyArray=new Vector(nPolysRead+1);
    nextPolygon=1; //start new polygon count, this is a class variable
   }/*end if polyTable.isEmpty*/
   
    tempFlow = new int[nextRegime+1][cntMax]; 
    data=new FlowData[nPolysRead+1];
    for (int i=1;i<=nPolysRead; i++)data[i]=new FlowData();
    nPolysRead=0;
  }/*end if pass==2*/
  
   
   nLines=0;
  try{	 
      in = new BufferedReader(new FileReader(fileName),1024);
      
      
    while ((line=in.readLine())!=null){ 
	
      token=new StringTokenizer(line,delimiters); //all legit delimeters
      int n=token.countTokens(); //n of tokens on this line
      if(n%2!=0){parent.console.println("ERROR: Wrong number of items after line "+nLines+"\n"+line);
       continue;}/*fi*/
      if(n==0)continue; //must be a blank line
      
       polyNow=token.nextToken(); //current polygon id

       
       if(!polyNow.equals(polyBefore)){

	   
	   if(pass==2  & !isFirstFlowRead & !polyTable.containsKey(polyNow)){
	     parent.console.println("ERROR: Polygon "+polyNow+" : Does Not Exist in Other Flows.\n You MUST fix this!\n");
	   }
	   nPolysRead++;
	   nRegimes=1;
         if(pass==2 & isFirstFlowRead & polyTable.containsKey(polyNow))
	 parent.console.println("ERROR: Polygon "+polyNow+" Appears multiple times.\n You MUST fix this!\n");
         } //reading a new polygon
	  else nRegimes++; //reading another regime for same polygon
	polyBefore=polyNow;  
       
      if (pass==1){
	  cntMax=(cntMax>n)?cntMax:n; //get max n of columns
	  regimeMax=(regimeMax>nRegimes)?regimeMax:nRegimes; //get max n of regimes per polygon
      }/*fi pass=1*/
      
      if(pass==2 & nRegimes==1 & nLines!=0){//must have just finished a polygon
	 int pIndex=tempFlow[0][0]; //the internal polygon id
	 int nregs=tempFlow[0][1]; //n of regimes for this polygon
	 int yrsOut=2; //n of years and outputs for each regime
	  data[pIndex].flow=new int[nregs+1][];
	 for(int r=1;r<data[pIndex].flow.length;r++){
	   yrsOut=tempFlow[r][0];
	   data[pIndex].flow[r]=new int[yrsOut+1];
	   data[pIndex].flow[r][0]=tempFlow[r][1];//regime index
	    if(yrsOut/2>regimeActions[tempFlow[r][1]]){
	    regimeActions[tempFlow[r][1]]=yrsOut/2;//n of actions by regime ind
	   }
	 for(int c=0;c<yrsOut;c++){data[pIndex].flow[r][c+1]=tempFlow[r][c+2];}
        }/*end for r*/
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
	
	 
	 if (!regimeTable.containsKey(regimeName)){
	   nextRegime++;
	   element=Integer.toString(nextRegime-1);
	   regimeTable.put(regimeName,element);
	   regimeArray.addElement(regimeName);
	 } /*end if pass==2 &&*/ 
	 
	 if (pass==2){ //now capture the data into tempFlow
	  int polyIndex=Integer.parseInt((String)polyTable.get(polyNow));
	   polyIndex=polyIndex-polyFirst+1;  //local index not global
	  if(nRegimes==1)tempFlow=new int[regimeMax+1][cntMax+1];//first line for this polygon
	  tempFlow[0][0]=polyIndex; 
	  tempFlow[0][1]=nRegimes;//will retain the nRegimes for this polygon
	  tempFlow[nRegimes][0]=n-2; //n of years and outputs
	  tempFlow[nRegimes][1]= Integer.parseInt((String)regimeTable.get(regimeName));
	  try{for(int c=0;c<n-2;c++)tempFlow[nRegimes][c+2]=new Double(token.nextToken()).intValue();
	  }catch(Exception et){parent.console.println("Bad data at this Polygon and Regime: "+polyNow+" "+regimeName+": "+et);
	      badCount++;
	      if(badCount>100){
		  parent.console.println("Aborting: fix bad data");
		  return(false);
	      }
	  }
	   for(int c=0;c<(n-2)/2;c++){//get min and max years
	       int year=tempFlow[nRegimes][c+2];
	    if(year<=0){ //record that doNothings exist in data and
		//dont let negative years impact xMin,xMax
		if(year==0)isDoNothingRegime=true;  
	    }
		else{
		 xMin=(year<xMin)?year:xMin;
		 xMax=(year>xMax)?year:xMax;
		} /* />
  <objective value="1end if else isDoNothingRgime*/          
	    }/*end for c<(n-2)/2*/ 	
	 }/*fi pass==2*/
     
     nLines++;
    }/*end of while loop*/
    
    
    if(pass==2){//capture data from last polygon in file
	 int pIndex=tempFlow[0][0]; //the internal local polygon id
	 int nregs=tempFlow[0][1]; //n of regimes for this polygon
	 int yrsOut=2; //n of years and outputs for each regime
	  data[pIndex].flow=new int[nregs+1][];
	 for(int r=1;r<data[pIndex].flow.length;r++){
	   yrsOut=tempFlow[r][0];
	   data[pIndex].flow[r]=new int[yrsOut+1];
	   data[pIndex].flow[r][0]=tempFlow[r][1];//regime index
           if(yrsOut/2>regimeActions[tempFlow[r][1]]){
	    regimeActions[tempFlow[r][1]]=yrsOut/2;//max n of actions by regime
	   }
	 for(int c=0;c<yrsOut;c++){data[pIndex].flow[r][c+1]=tempFlow[r][c+2];}
        }/*end for r*/
      }/*fi pass==2*/
   
    
     if (pass==2){ 
       if (polyLast==-999)polyLast=polyFirst+nPolysRead-1;
      regimeLast=nextRegime-1; 
      if (isDoNothingRegime)getDoNothings();
      else parent.console.println("There are no do-nothing regimes? ... you should check the data!");
     dataRead=true; //if you get here the data was read OK
     applyRegimeEdits(parent,fileName,this); //read regime edit file if exists

     if(parent.runRunning)init(); //make sure an init() is done when a component is added on the fly
     if(xMax<0){//must have been all doNothing regimes
	 xMin=0;
	 xMax=0;
     }
     parent.console.println("Successfully read Polygons assigned internal Id's "+polyFirst+" to "+polyLast+". \nDetected years "+xMin+" to "+xMax);
     parent.console.println("The current objective function evaluates "+regimeLast+" regimes.");
     //make sure n of polygons is correct
     if (polyFirst==1)
       for (int j=1;j<=parent.nComponents;j++)
	if(parent.isAdded[j] && parent.form[j].polyFirst==1 && (parent.form[j].polyLast != this.polyLast))
	parent.console.println("====This is bad!: \nN of Polygons mismatch between "+ this.getTitle() +" and "+ parent.form[j].getTitle()); 
   } /*end if pass==2*/ 
   
    
   if (checkModel.isSelected())shrink(true); //this forces a read of the users model values
    
} /*end try*/
   catch (Exception e) {
    parent.console.println("Error: reading " + fileName + "\n" + e);
    nextPolygon=nextPolygonSave; //restore to old value
    dataRead=false; }
finally {  try { if (in != null) {in.close();} } catch (IOException e) {} }
  
  }/*end pass loop*/
  
  return dataRead;
 }/*end readData()*/
 

/** />
  <objective value="1
*Fill the doNothing vector with the options-period pairs that
*occur in year 0.   The BlockForm accesses this vector to 
*automatically include these as notBlock options
*theDoNothing value is needed by the Block Component (and maybe others)
*to force a stand to be set to theDoNothing when killSmallBlocks is used
*/
void getDoNothings(){
 int row,fsize,t,val;
 String s;
for(int i=1; i<=regimeLast; i++){
  int ind=0; // indicates if all periods are doNothing
 for (int j=1;j<=polyLast-polyFirst+1; j++){
  row=data[j].flowPtr(i);  /*the correct row in the FLOW matrix*/
  if (row==0)continue; //polygon j doesn't have this option
   fsize=(data[j].flow[row].length-1)/2; //n of yrs of output
      
  for (int j2=1;j2<=fsize;j2++){
   t=data[j].flow[row][j2];
   val=data[j].flow[row][j2+fsize];
   if(t==0 && val==0){//only pick up automatic donothing when val==0
   s=Integer.toString(i)+"@"+Integer.toString(j2);
   doNothings.addElement(s);
   } else ind=1; //this isn't theDoNothing option  
  }/*end for j2*/ 
  
   break;
 }/*end for j*/
  if (ind==0)theDoNothing=i; //this regime is doNothing for all periods
}/*end for i*/

}/*end getDoNothings()*/
 
/**
*Come here when this is a superFlow component and the current
*global polygon index is out of range.  This will find an
*owned dataset that is the logical next  dataset relative to
* the start value. Used mainly by init().
*if start=0 the subFlow with the lowest polyFirst value is selected
*return(false) means this component doesn't own the required data
*@param start will usually be the current polyFirst value
*/ 

boolean findNextData(int start){
  int hold=1000000000,ind=0;
  FlowForm f=null,fNext=null;
for (int i=1;i<=UserInput.nComponents;i++){
    if (subFlows[i]==0)continue;
      f=(FlowForm)parent.form[i];
    if (f.polyFirst>start && f.polyFirst<hold){
      hold=f.polyFirst;
      fNext=f;
      ind=1; 
    }/*end if polyFirst>*/   
 } /*end for i*/
 if(ind==0)return(false);   
      this.data=fNext.data;
      this.polyFirst=fNext.polyFirst;
      this.polyLast=fNext.polyLast;
      this.regimeLast=fNext.regimeLast;
      this.xMin=f.xMin;
      this.xMax=f.xMax;
 return(true);   
}/*end findNextData()*/

/**
*Come here when this is a superFlow component and the current
*global polygon index is out of range.  This will find an
*owned dataset that contains ig or will return false
*/ 

boolean findData(int ig){
  FlowForm f=null;
for (int i=1;i<=UserInput.nComponents;i++){
    if (subFlows[i]==0)continue;
      f=(FlowForm)parent.form[i];
    if (ig>=f.polyFirst && ig<=f.polyLast){ 
      this.data=f.data;
      this.polyFirst=f.polyFirst;
      this.polyLast=f.polyLast;
      this.regimeLast=f.regimeLast;
      this.xMin=f.xMin;
      this.xMax=f.xMax;
      return(true);   
    }/*end if ig>*/
 }/*end for i*/ 
  return(false);    
}/*end findNextData()*/
 
/**
*Used by metropolis algorithm to determine
*the valid schedules for polygon i.  If nScheds==-1
*it returns all schedules, o/w it returns schedules
*that match whats already in validScheds vector
*@param i indicates the polygon,
*@param nScheds is the number of schedules in vector nValidScheds
*/
public int getValidScheds(int ig, int nScheds){
    // parent.console.println("component ig polyFirst polyLast "+getTitle()+" "+ig+" "+polyFirst+" "+polyLast);  
    
    if (ig<polyFirst || ig>polyLast){
     boolean test=false;
     if(isSuperFlow)test=findData(ig);
     if (!test)return(nScheds); //not a local polygon, don't change nValidScheds
    }/*end if ig<*/    
    int i=ig-polyFirst+1; //transform global to local polygon index
    int[] temp; //holds the schedules temporarily
     if (nScheds==-1) temp=new int[data[i].flow.length+1];
     else temp=new int[nScheds+1]; //temp storage for validScheds
     
    int count=0; //count valid scheds  
     
     //get current schedule in position 1
     if (polySched[ig]!=0){count=1; temp[1]=polySched[ig];} 
    
        int z; //holds a schedule
    if (nScheds==-1){ //nScheds=-1 means this is first component checked for schedule validity
	for (int k=1; k<data[i].flow.length; k++){
	  z=data[i].getSched(k);
	  if(z!=polySched[ig]) temp[++count]=z;  //only use current sched in position 1
	}/*end for k*/
      } /*end if nScheds==-1*/
     else {
      for (int k=1;k<=nScheds;k++){
       z=validScheds[k];
       int rowz=data[i].flowPtr(z);/*the correct row for z in the FLOW matrix*/
       if (rowz==0)continue;  /*not a valid option*/
       if(z!=polySched[ig]) temp[++count]=z;  //only use current sched in position 1  
     }/*end for k*/ 
    }/*end if else nScheds==-1*/

    if(count<validScheds.length){
      System.arraycopy(temp,1,validScheds,1,count);
    }else{
	JOptionPane.showMessageDialog(parent,"Data error for polygon "+polyArray.get(ig-1)+" in "+getTitle(),"Exit Habplan to Fix data error!",JOptionPane.OK_OPTION);
        System.exit(1);  
    }
	
  return(count);
 
}/*end getValidScheds()*/
 
 /**
*Initialize the  total flow
*Called at the initiation of the Metropolis Algorithm 
*/
public void init(){
  int yrs=xMax-xMin+1; //total n of years covered by this component
 gamaz=new float[yrs+1];//used in deltaObj to evaluate proposal option
  gamazD=new float[yrs+1]; //used in deltaObj to hold z options thresholded totals
 gama=new float[yrs+1]; //holds actual flow
 g=new float[yrs+1];  //holds the target flow
 int row; //row in flow for the current schedule;
 int fsize; //number of years of output for this schedule;
 int t,t2; //The year for gama subscript
 int i; //local polygon ind
 boolean test=true;

//first compute the actual flow vector, gama[]

if (isSuperFlow) test=findNextData(0); //get the first dataset   
   
 while (test){
   //parent.console.println("isSuperFlow component polyFirst polyLast:  "+isSuperFlow+" "+getTitle()+" "+polyFirst+" "+polyLast);
 for(int ig=polyFirst;ig<=polyLast;ig++){
   i=ig-polyFirst+1; //transform global to local polygon index 
   row=data[i].flowPtr(polySched[ig]);  /*the correct row in the FLOW matrix*/
   if (row==0){parent.console.println("FlowForm: Invalid management option, error in assigning schedule to polygon " + i);}
   fsize=(data[i].flow[row].length-1)/2;
 for (int j=fsize;j>=1;j--){  /*handles multiple output years*/
   t=data[i].flow[row][j]-xMin+1; /*scaled year under current option*/
  if (t>=1 && t<=yrs)  /*its not just a place holder in FLOW and its <= yrs*/
     gama[t]=gama[t]+data[i].flow[row][fsize+j];
 } /*end for j*/
} /*end for ig = global polygon index*/

  test=findNextData(polyFirst);
}/*end while()*/
if (isSuperFlow)findNextData(0); //reset to first dataset 
  model(gama,g); /*gama0 is user supplied*/ 
  
// now compute the derived flow, gamaD[] with threshHolding
 gamaD=new float[yrs+1]; 
 if (threshLow==0 && threshHi==0){
  for(t=1;t<=yrs;t++) gamaD[t]=gama[t];
  return;
 }/*end if no thresholds*/
 
 for(t=1;t<=yrs;t++){
   float f=gama[t];
    if (checkFlow.isSelected() && g[t]-threshLow-f>f)f=g[t]-f;
    else if (f<=g[t]-threshLow)f+=threshLow;
    else if (f>g[t]+threshHi)f-=threshHi;
    else f=g[t];
   gamaD[t]=f;       
 }/*end for t*/ 
  
 // for (t=1;t<=yrs;t++) parent.console.println("t gamaD g "+t+" "+gamaD[t]+" "+g[t]);
    
} /*end init()*/ 

 
/**
*Compute Objective Function change when one unit changes
*its management regime.  This one is combining flow and a link
*to time 0, so it's almost like 2 components in one.
*@param members contains indices of member polys, z is the new regime
*/
public double deltaObj(int[] members, int z){

     double Fdelta=0.0;  //holds the change related to flow
     double Idelta=0.0;   //holds change related to time 0 linkage
      int yrs=xMax-xMin+1;
   for(int t=1;t<=yrs;t++){
     gamaz[t]=gama[t]; /*initialize gamaz*/ 
     gamazD[t]=gamaD[t];
  }/*end for t*/   

  for(int mem=0;mem<members.length;mem++){ //loop through the members
	int ig=members[mem]; //process one member of this unit at a time
	if(ig==0)continue; //no data for this member

  if (ig<polyFirst || ig>polyLast){
     boolean test=false;
     if(isSuperFlow)test=findData(ig);
     if (!test)continue; //not a local polygon so move on
  }/*end if ig<*/
  int i=ig-polyFirst+1; //transform global to local polygon index 
 boolean bySkip=false;     

 int start=xMin+1;
 if (checkModel.isSelected()==true)start=xMin; //use year 1 from user instead of gama0

 int row=0,rowz=0,fsizeX=0,fsizeZ=0,fsize=0,i1=0,t=0,t2=0; 

   row=data[i].flowPtr(polySched[ig]);
   rowz=data[i].flowPtr(z);
   fsizeX=(data[i].flow[row].length-1)/2;
   fsizeZ=(data[i].flow[rowz].length-1)/2;
   fsize=Math.max(fsizeX,fsizeZ);
        
    for (int j=1;j<=fsize;j++){ /*make needed changes to gamaz*/
     if (j<=fsizeX){
      i1=data[i].flow[row][j]-xMin+1;  /*the scaled current management year*/
      i1=(i1>0)?i1:0;  //make sure doNothings stay at 0, not negative indices 
       gamaz[i1]=gamaz[i1]-data[i].flow[row][fsizeX+j];	
       float f=gamaz[i1];  //do the thresholding for whats taken out
			  //remember i1=0 means a do-nothing option 
	if (checkFlow.isSelected() && g[i1]-threshLow-f>f)f=g[i1]-f;
	else if (f<=g[i1]-threshLow)f+=threshLow;
        else if (f>g[i1]+threshHi)f-=threshHi;
	else f=g[i1];
	gamazD[i1]=f;
     } /*end if j<fsizeX*/ 	 
     if (j<=fsizeZ){
      i1=data[i].flow[rowz][j]-xMin+1;  /*the z management year*/
      i1=(i1>0)?i1:0;  //make sure doNothings stay at 0, not negative indices
      gamaz[i1]=gamaz[i1]+data[i].flow[rowz][fsizeZ+j];	
	float f=gamaz[i1];  //do the thresholding for whats added
	if (checkFlow.isSelected() && g[i1]-threshLow-f>f)f=g[i1]-f;
	else if (f<=g[i1]-threshLow)f+=threshLow;
        else if (f>g[i1]+threshHi)f-=threshHi;
	else f=g[i1];
	gamazD[i1]=f; 
     } /*end if j<fsizeZ*/	
    } /*end for j=1*/
      
  
         /*COMPUTE F(X)-F(Z)*/
       model(gama,g); /*gama0 is user supplied*/ 
       //Fdelta=0.0;
    
    for (t=start;t<=xMax;t++){
     bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
      t2=t-xMin+1;
      if(!bySkip) Fdelta+=Math.pow(gamaD[t2]-g[t2],2); /*F(X)*/
     }/*end for t*/
      model(gamaz,g);   
    for (t=start;t<=xMax;t++){
     //parent.console.println("t gama gamaD "+t+" "+gama[t]+" "+gamaD[t]);
     bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
     t2=t-xMin+1;
     if(!bySkip) Fdelta-=Math.pow(gamazD[t2]-g[t2],2); /*F(X)-F(Z)*/
    } /*end for t*/
    
       
     
         /*COMPUTE I(X)-I(Z)*/
	 //Idelta=0.; 
    if (checkModel.isSelected()==false){	//not with user supplied targets
      bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==xMin){bySkip=true; break;}
      if(!bySkip) Idelta+=(Math.pow(gamaD[1]-gama0,2) -
			  Math.pow(gamazD[1]-gama0,2));

    }/*if checkModel==false*/
    
    }/*end for mem loop*/  
   Fdelta=Fdelta*weightF;
    Idelta=Idelta*weight0;
           
   //divide by yrs and gama0 to make similar weights work for different measurement scales
   //this is similar to a coefficient of variation times a weight
 return ((Fdelta+Idelta)/(wtFactor+0.01)/yrs);

}/*end deltaObj()*/

 /**
*adjustments to be made during a metropolis iteration
*if a change is made
*/
public void duringIter(){
 for(int t=1;t<=xMax-xMin+1;t++){
  gama[t]=gamaz[t];   
  gamaD[t]=gamazD[t]; 
 }/*end for*/ 
}/*end duringIter()*/

 /**
*adjustments to be made after a metropolis iteration
*/
public  void postIter(){
    int count=0,yrs=xMax-xMin+1;
    float x=0, x2=0;
  for(int t=1;t<=yrs;t++){
    if (gama[t]>0){ x+=gama[t];x2+=Math.pow(gama[t],2); count++;}
  }/*end for i*/
  //take average of xBar and x2, where x2 is sum(x squared)
  wtFactor=(float)(x+x2)/2/count/100; //seems to work, what can I say?
  
  // now refresh the derived flow, gamaD[] with threshHolding
 for(int t=1;t<=yrs;t++){
   float f=gama[t];
    if (checkFlow.isSelected() && g[t]-threshLow-f>f)f=g[t]-f;
    else if (f<=g[t]-threshLow)f+=threshLow;
    else if (f>g[t]+threshHi)f-=threshHi;
    else f=g[t];
   gamaD[t]=f;       
 }/*end for t*/ 
   
  if (graph.isVisible()){  
    model(gama,g);
    float[] xx=new float[xMax-xMin+2];
    for(int t=xMin; t<=xMax; t++)xx[t-xMin+1]=t;
    graph.ag[1].plot(xx,gama,"Actual Flow",.05,.8);
    graph.ag[1].plotHold(xx,g,"Target Flow",.55,.8);
     if (threshLow!=0 || threshHi!=0){
      float[] tl=new float[xMax-xMin+2];
      float[] tu=new float[xMax-xMin+2];
      for(int t=xMin; t<=xMax; t++){
       tl[t-xMin+1]=g[t-xMin+1]-threshLow;
       tu[t-xMin+1]=g[t-xMin+1]+threshHi; 
     }/*end for t*/ 
     if(threshLow!=0)graph.ag[1].plotHold(xx,tl,"ThreshLow",.05,.1);
     if (threshHi!=0)graph.ag[1].plotHold(xx,tu,"ThreshHi",.55,.1);
    }/*end if threshLow...*/
      if (!isColorsLoaded){
	  isColorsLoaded=true; graph.loadColors(); //make sure it looks like user wants
	  if (xMax-xMin<20){graph.ag[1].xWrite.setTics(5);graph.ag[1].xTics.setTics(5);}
      }/*end if isColorsLoaded*/
     graph.repaint();
   }/*end if*/
   
   weightF=adjustWeightF((float)weightF);
   p6.text.setText(String.valueOf(weightF));
   bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==xMin){bySkip=true; break;}
    
  if (checkModel.isSelected()==false && !bySkip){ //skip if 1=byYear or user input the targets
    //parent.console.println("adjusting weight0");
   weight0=adjustWeight0(weight0);
   p7.text.setText(String.valueOf(weight0));
  } /*end if checkModel==false*/
}/*end postIter()*/

/**
*compute the predicted FLOW under a user specified model
*change this function to use different models.  The vector g
*is returned with the modeled values
*/
void model(float gama[], float g[])  
{

 int t,j,t2;
 int yrs=xMax-xMin+1;
 if(model==null)readTheForm();
 
try{ //sometimes readTheForm is slow so model vector not ready
  for( t=1;t<=yrs;t++) g[t]=0; /*initialize g to 0's*/ 
   bySkip=false; for (j=1;j<=nByYrs;j++) if(byYrs[j]==xMin){bySkip=true; break;}
   if (bySkip)g[1]=gama[1];
   else if (checkModel.isSelected()==true && model[1]!=-999)g[1]=model[1];
   else g[1]=gama0; /*user specified value*/ 
     
      
  for(t=xMin+1;t<=xMax;t++){ 
    bySkip=false; for (j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
    t2=t-xMin+1;
    if (bySkip){g[t2]=gama[t2];continue;} //set target to actual byGone value
    if (checkModel.isSelected()==true && model[t2]!=-999){g[t2]=model[t2];continue;}
    if (t>xMin+1) g[t2]=((t2-1)*g[t2-1]+gama[t2-1])*(1+rate)/t2;
    else g[t2]=gama[t2-1]*(1+rate);         
   }/*end for t*/
   
   }//end try
  catch (Exception e){
      readTheForm(); //try reading the form again
      parent.console.println("The temporary problem: "+e);
  }
   
  return; 
} /************************end model function************************/


/**
*adjust weightF to keep yearly relative flow change between
*lowerF and upperF
*/

protected final float adjustWeightF(float wt)
{
double delta=1.,wtNew,rdif=1., yDelta=1., yRdif=1.;
double upperF=1,lowerF=0;
double aFactor=.98; //how fast to adjust the weight
if(parent.nPolys>200)aFactor=.95;
if(parent.nPolys>500)aFactor=.9;

int t2, start=xMin+1;
if (checkModel.isSelected()==true)start=xMin;

 upperF=(goalF+goalWidth);
 upperF=(upperF<1.)?upperF:1.;
 lowerF=(goalF-goalWidth);
 lowerF=(lowerF>0.)?lowerF:0.;
 
 try{ //helps if model vector is being created in readTheForm while running
 
 for (int t=start;t<=xMax;t++){ //first yr is too dominated by user specified gama0
   bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==t){bySkip=true; break;}
   t2=t-xMin+1;
   if (bySkip || g[t2]==0)continue; //this is a byGone year so skip it
   rdif=1. - Math.abs((gamaD[t2]-g[t2])/(g[t2]+ minValue)); 
   if (rdif<delta && rdif>=0) delta=rdif;
      //compute yr-to-yr deviation if t>1 and the current flow not specified by user and no thresholds
   if(t>start && !(checkModel.isSelected() && model[t2]!=-999) && threshLow==0 && threshHi==0)yRdif=1.-Math.abs((gamaD[t2]-gamaD[t2-1])/(g[t2]+ minValue)); 
   if (yRdif<yDelta && yRdif>=0) yDelta=yRdif;
 } /*end for t*/
 
 delta=Math.round(delta*100)/100.; //round off to nearest hundreth
 yDelta=Math.round(yDelta*100)/100.;
  wtNew=wt;
   //too much variation about line or yr to yr, then increase weight
 wtNew=(yDelta>upperF&&delta>upperF)?wtNew*aFactor:wtNew;  
 wtNew=(yDelta<lowerF||delta<lowerF)?wtNew/aFactor:wtNew; //too little variation then decrease weight
 if (wtNew<minValue)wtNew=minValue; /*keep p in bounds, display will only show 3 decimals*/
 else if(wtNew>Float.MAX_VALUE){
     isWt2big=true;
   wtNew=Float.MAX_VALUE;
  if (parent.met.iter%20==0)
   parent.console.println("Warning: Weight on "+getTitle()+"\n exceeds machine limit: try reducing goal.");
 }/*end else MAX_VALUE*/
 
 if ((float)wtNew==wt)converged=1; //indicates convergence
 else if ((float)wtNew<wt)converged=2; //overConverged
 else converged=0;   //underConverged
 
 //write some output before returning
 bySkip=false; for (int j=1;j<=nByYrs;j++) if(byYrs[j]==xMin){bySkip=true; break;}
 String text0="Goal:";
 if (checkModel.isSelected()==false && !bySkip){  //don't do this with user input model
  rdif=1.-Math.abs((gama[1]-gama0)/gama0);
  rdif=Math.round(rdif*100)/100.;
  if (rdif<0)rdif=0.; //don't allow negative goal
  text0="Goals: Time0=" + rdif;
 } /*end if checkModel==false*/ 
  
 outLabel.setBackground(Color.black); //solaris seems to need this
 outLabel.setText(text0 + " 1-y-yHat=" + delta + " 1-yr to yr=" + yDelta);
 goalValue=(yDelta+delta)/2.; //used by fitness function

} //end try
catch (Exception e){
wtNew=wt;
parent.console.println("adjustWeightF(): Use Suspend before switching to noModel option\n to avoid this annoying error." );
parent.console.println("The temporary problem: "+e);
}
return((float)wtNew);
} /*******************end adjustWeightF*******************/ 

/**
*adjust weight0 to keep relative flow change for year 1
*about gama0 between rate and rate/2
*/
float adjustWeight0(double wt)
{
double delta,wtNew,lower0=0.,upper0=1.;
double aFactor=.99; //how much to adjust weight
if(parent.nPolys>200)aFactor=.95;
if(parent.nPolys>500)aFactor=.9;

upper0=(goal0+.05);
 upper0=(upper0<1.)?upper0:1.;
 lower0=(goal0-.05);
 lower0=(lower0>0.)?lower0:0.;
 
delta=1. - Math.abs((gamaD[1]-gama0))/(gama0+minValue);
if (delta<0) delta=0;  //zero indicates the worst fit possible
 wtNew=wt;
 wtNew=(delta>upper0?wtNew*aFactor:wtNew); //too little variation then decrease weight
 wtNew=(delta<lower0?wtNew/aFactor:wtNew);
 if (wtNew<minValue)wtNew=minValue;/*keep q in bounds*/
 else if(wtNew>Float.MAX_VALUE)wtNew=Float.MAX_VALUE;
 
return((float)wtNew);
} /*******************end adjustWeight0*******************/
 

} /*end FlowForm class*/
