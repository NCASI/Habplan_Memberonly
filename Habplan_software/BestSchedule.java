import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import org.xml.sax.Attributes;

public class BestSchedule extends JFrame {
   
   FrameMenu parent;
   String tempSaveFile="tempSaveFile.hbp";  //used for restore feature
    JTextBox[] textBox;
    JComboBox saveCombo, loadCombo;
    JButton loadButton, restoreButton, closeButton, resetButton;
    JCheckBox dynamicQBox; //turn dynamic Q-type weighting algorithm on or off
    static JLabel outLabel; 
    protected static boolean isLoad=false; //indicates that user loaded this

    private int nc; //n of components
    protected static int lastChange=0; //trys since best schedule was changed
    protected static double bestFitness=0; //bestFitness so far
    protected int nSave=1,//n of best schedules to save
		  nLoad=1; //which of the n best to load
    protected static int nLimit=10; // the max amount to allow saved, changed by programmer
  
    int[][] bestSched; //hold the best schedules
    protected static double currentFitness=0;
    protected static double[] fitness;
    	  
    public BestSchedule(FrameMenu parental){
	super("Best Schedule Control");
	this.parent=parental;
	setBounds(400,460,375,250);
	nc=parent.nComponents;
	
	 //set up vectors to hold best schedules
	bestSched=new int[nLimit+1][]; 
	fitness=new double[nLimit+1];
	
	//make choices for how many schedules to save
	saveCombo=new JComboBox();
	for(int i=1;i<=nLimit;i++)saveCombo.addItem(Integer.toString(i));
	 saveCombo.addItemListener(new ItemListener(){
	public void itemStateChanged(ItemEvent e){
	   nSave=Integer.parseInt((String)saveCombo.getSelectedItem());
	   adjustLoadCombo(); //only allow loading 1 of nSave possibilities
	   //System.out.println("nSave= "+nSave);
	 }/*end actionPerformed()*/ 
        }); 
	
	resetButton=new JButton("Reset");
	resetButton.addActionListener(new ButtonListener());
	resetButton.setToolTipText("Reset BestSchedules to start fresh");
         
	dynamicQBox=new JCheckBox("Dynamic");
        dynamicQBox.setToolTipText("Activate Dynamic Weighting Algorithm");
        dynamicQBox.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent event){
		 if(dynamicQBox.isSelected())parent.met.setDynamic(true);
		 else parent.met.setDynamic(false);
		}
	    });

	JLabel lab1=new JLabel("Save N Best");
	lab1.setToolTipText("N of best schedules to retain");
	JPanel savePanel=new JPanel();
	savePanel.add(lab1);
	savePanel.add(saveCombo);
	savePanel.add(resetButton);
        savePanel.add(dynamicQBox);
	getContentPane().add(savePanel,BorderLayout.NORTH);
	
	//which schedule to load
	loadCombo=new JComboBox();
	loadCombo.addItemListener(new ItemListener(){
	public void itemStateChanged(ItemEvent e){ 
	  try{nLoad=Integer.parseInt((String)loadCombo.getSelectedItem());
	  }catch(Exception e2){nLoad=1;}
	   //System.out.println("nLoad= "+nLoad);
	 }/*end actionPerformed()*/ 
        }); 
	adjustLoadCombo();//add the right number of selections
	
	Font font = new Font("SansSerif", Font.BOLD, 10);
	//button section
	closeButton = new JButton("Close");
	closeButton.addActionListener(new ButtonListener());
        closeButton.setFont(font);
	closeButton.setToolTipText("Make this window go away");
	loadButton=new JButton("Load");
	loadButton.addActionListener(new ButtonListener());
	loadButton.setFont(font);
	loadButton.setToolTipText("Load the selected best schedule");
	restoreButton=new JButton("Restore");
	restoreButton.addActionListener(new ButtonListener());
	restoreButton.setFont(font);
	restoreButton.setToolTipText("Restore to before last load");
	JPanel loadPanel=new JPanel();
	loadPanel.add(loadButton);
	loadPanel.add(loadCombo);
	loadPanel.add(restoreButton);
	loadPanel.add(closeButton);
	getContentPane().add(loadPanel,BorderLayout.CENTER);
	
	//Build Fitness function section
	JPanel fitPanel=new JPanel();
	fitPanel.setBackground(Color.black);
	fitPanel.setForeground(Color.white);
	fitPanel.setLayout(new BorderLayout());
	int cols=4;
	try{cols=Integer.parseInt(System.getProperty("habplan.config.nrow").trim());}
	 catch(Exception e){ /*leave cols=4*/};
	int rows=(int)(Math.ceil((float)nc/cols)); 
	JPanel scorePanel=new JPanel();
        scorePanel.setLayout(new GridLayout(rows,cols,1,1));
	textBox=new JTextBox[nc+1];
	for (int i=1;i<=nc;i++){
	  textBox[i]=new JTextBox(parent.componentName[i]);
	  scorePanel.add(textBox[i]);  
	    }/*end for i*/
	  fitPanel.add(scorePanel,"South"); 
	  //output label for printing output
	   outLabel=new JLabel("Fitness Function Definition",JLabel.CENTER);
	   outLabel.setForeground(Color.white);
	   outLabel.setBackground(Color.black);
	  fitPanel.add(outLabel,"North"); 
	 getContentPane().add(fitPanel,"South");   
	
	pack();
	 setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      dispose();
	  }
	 });   
    }/*end constructor*/
 
/**
*adjust to load values to conform to the nSave selection.
*called when user changes the number of best schedules to save
*from the saveCombo box.
*/
private void adjustLoadCombo(){
	try{loadCombo.removeAllItems();} catch(Exception e){}
	for(int i=1;i<=nSave;i++)loadCombo.addItem(Integer.toString(i));          
}/*end method()*/ 

 /**
 *save the index and score values for holdState use
 *ensures that holdState is serializable and simple?
 *a reference is passed to the save Object vector
 */
    protected void captureHoldStateSettings(Object[] vec){
	int[] index=new int[nc+1];
	double[] value=new double[nc+1];
     for (int j=1;j<=nc;j++)index[j]=textBox[j].index;
     for (int j=1;j<=nc;j++)value[j]=textBox[j].value;
      vec[0]=index;
      vec[1]=value;
    }/*end method*/

/**
*load textBox settings from holdState. Usually
*this loads from the vector made by getFitnessSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
     int[] index=(int[])vec[0];
     double[] value=(double[])vec[1];
     for (int j=1;j<=nc;j++){
	textBox[j].index=index[j];
	 if (textBox[j].index==1)textBox[j].checkBox.setSelected(true);
	   else textBox[j].checkBox.setSelected(false);
     }/*end for*/   
     
     for (int j=1;j<=nc;j++){
	textBox[j].value=value[j];
	 if((int)textBox[j].value==textBox[j].value)//don't show unnecessary decimals
	  textBox[j].textField.setText(Integer.toString((int)textBox[j].value));
	 else
	   textBox[j].textField.setText(Double.toString(textBox[j].value));   
	}/*end for*/
     }/*end method*/

/**
 *save the index and score values to the xml project file
 *ensures that holdState is serializable and simple?
 *a reference is passed to the save Object vector
 */
    protected void writeSettings(PrintWriter out){
	String weights="", states="";
	for (int j=1;j<=nc;j++){
	    states+=textBox[j].index;
	    if(j!=nc)states+=",";
	}
	for (int j=1;j<=nc;j++){
	    weights+=textBox[j].value;
	    if(j!=nc)weights+=",";
	}
	out.println("  <weight value=\""+weights+"\" />");
	out.println("  <state value=\""+states+"\" />");
    }/*end method*/

	
     /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){

	try{   
	if(qName.equals("weight")){
	    String[] value=(atts.getValue(0)).split(",");
	    for (int j=1;j<=nc;j++){
		try{
		textBox[j].value=Double.parseDouble(value[j-1]);
		}catch(Exception e){
		    System.out.println("BestSchedule.readSettings value:" +e);
		}
		if((int)textBox[j].value==textBox[j].value)//don't show unnecessary decimals
		    textBox[j].textField.setText(Integer.toString((int)textBox[j].value));
		else
		    textBox[j].textField.setText(Double.toString(textBox[j].value));   
	    }/*end for*/
	}

	if(qName.equals("state")){
	    String[] index=(atts.getValue(0)).split(",");
	    for (int j=1;j<=nc;j++){
		try{
		    textBox[j].index=Integer.parseInt(index[j-1]);
		}catch(Exception e){
		    System.out.println("BestSchedule.readSettings index:" +e);
		}
		if (textBox[j].index==1)textBox[j].checkBox.setSelected(true);
		else textBox[j].checkBox.setSelected(false);
	    }/*end for*/ 

	}
	}catch(Exception e){parent.console.println(getTitle()+".readSettings() "+e);}
    }
  
/**
*evaluate the fitness of current schedule
*/  
public double fitness(){
    double fitness=0.0;
    for (int j=1;j<=nc;j++)
    if(parent.isAdded[j]){
     if (parent.form[j].isConverged()>=1)
       fitness+=textBox[j].value + textBox[j].index*parent.form[j].goalValue;      
    }/*end for/if isAdded*/
    return fitness;
    }/*end method*/
    
 /**
 *save the current schedule if the fitness function is better than 
 *the previously saved schedules in bestSched vector
 */ 
 public void saveBest(){
     
     
  if(!isLoad){//don't test user loaded old best schedules
    currentFitness=fitness(); //get the fitness value to compare to saved schedules
    lastChange++; //count times since last change;
    
  for (int i=1;i<=nSave;i++){
          
      if(currentFitness<=fitness[i])continue;  //only go thru if fit>fitness

       //parent.console.println("i fit fitness "+i+" "+currentFitness+" "+fitness[i]);
	    //shift file names ahead and put last name first
	   
	     System.arraycopy(bestSched,i,bestSched,i+1,nSave-i);
	     System.arraycopy(fitness,i,fitness,i+1,nSave-i);
	      
	fitness[i]=currentFitness;
	synchronized(ObjForm.polySched){ //lock polySched while making a copy
	 int length=ObjForm.polySched.length;
	 bestSched[i]=new int[length];
	 System.arraycopy(ObjForm.polySched,0,bestSched[i],0,length);
	}/*end sync*/
	
	lastChange=0; //reset lastChange
	bestFitness=((int)(currentFitness*100))/100.;
	break;
    }/*end for i*/
  }/*end if isLoad*/
     if (Algorithm.iter>3)isLoad=false;//set to true when user hits load button
     double fit=((int)(currentFitness*100))/100.; 
     outLabel.setText("Best= "+bestFitness+", Current= "+fit+", ItersHeld= "+lastChange);
     return;  
  }/*end method*/  
  
  /**
  *Reset bestFitness to 0
  */
  public static void resetBestFitness(){
      //System.out.println("BestSched.resetBestFitness called");
      bestFitness=0.0; isLoad=false; lastChange=0;
      for(int i=1;i<=nLimit;i++)fitness[i]=0.0;
      outLabel.setText("Fitness reset");
      }/*end method*/
    
  
  /**
* Handle button clicks
**/   
public class ButtonListener implements ActionListener{
    
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    
     if (cmd.equals("Close")) {
      setVisible(false); dispose();  
     return;  
    } 
      
     if (cmd.equals("Reset")) {
      resetBestFitness();
     return;  
    } 
     
   if (cmd.equals("Load")) {
       if(!(parent.runStop && Algorithm.isWaiting)){
	     JOptionPane.showMessageDialog(restoreButton,"Stop run before loading","Can't Load While Still Running",JOptionPane.WARNING_MESSAGE);
	     return;
	 }/*fi !isLoad*/
       	 ImportThread importThread=new ImportThread();
	 importThread.start();
	 JProgressMeter progress=new JProgressMeter(parent,importThread,getTitle());
	 progress.work();
      
     return;  
    }   
    
     if (cmd.equals("Restore")) {
	 if(!isLoad){
	     JOptionPane.showMessageDialog(restoreButton,"Can't restore before Loading","Load Precedes Restore",JOptionPane.WARNING_MESSAGE);
	     return;
	 }/*fi !isLoad*/
	 isLoad=false;
       parent.setState.loadState(UserInput.TempDir+tempSaveFile);
       parent.setIsImport(true);
       parent.isImportRun=true; //do 1 iteration to restore this schedule 
       parent.setState.setCheckBoxes(); //read data and check obj components 
     return;  
    }   
   
  }/*end actionperformed()*/

} /*end inner ButtonListener class*/

public void manualLoad(){
      isLoad=true; 
      parent.setIsImport(true);
      parent.isImportRun=true;
      parent.setState.saveState(UserInput.TempDir+tempSaveFile);  //save current status for future restore
      System.arraycopy(bestSched[nLoad],0,ObjForm.polySched,0,bestSched[nLoad].length);
      currentFitness=fitness[nLoad];
      parent.setState.setCheckBoxes();
      //parent.console.println("Fitness= "+currentFitness);
    }
  
/**
*inner class for specialized JtextField and CheckBox Component
*/
class JTextBox extends JPanel{
    JTextField textField;
    JCheckBox checkBox;
    double value=1;//the value in the textField
    int index=0;//1 if checked, 0 otherwise
    
    public JTextBox(String name){	
     checkBox=new JCheckBox(name);
     checkBox.setToolTipText("Check to add the goal value to fitness function");
     textField=new JTextField(Integer.toString((int)value),2);
     textField.setToolTipText("Score for fitness function when goal is attained");
     setLayout(new FlowLayout());
     add(checkBox);
     add(textField);
     
     textField.addCaretListener(new CaretListener(){
	public void caretUpdate(CaretEvent e){
	    textField.setBackground(Color.white);
	  try{value=Double.valueOf(textField.getText()).doubleValue();   
	  }catch(Exception e2){textField.setBackground(Color.red);}
	 // parent.console.println("value= "+value);
	 }/*end actionPerformed()*/ 
    }); 
    
       checkBox.addItemListener(new ItemListener(){
	public void itemStateChanged(ItemEvent e){ 
	  BestSchedule.resetBestFitness();
	  index=0;
	  if(checkBox.isSelected())index=1;
	 // parent.console.println("index= "+index);
	 }/*end actionPerformed()*/ 
    }); 
     
    }/*end constructor*/
    
}/*end inner TextBox class*/

    class ImportThread extends Thread{
	public void run(){
	    manualLoad();  //Load requested stored schedule
	}
    }/*end inner Thread class*/



}/*end class*/


