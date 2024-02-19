/**
*A class to allow the user to edit the regimes for individual polygons
*
*@author Paul Van Deusen June 2005
*/
import regime.*;
import gis.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;
import java.io.*;

public class RegimeEditor extends RegimeTable implements ItemListener{

    FrameMenu parent;
    JComboBox comboBox;
    JCheckBox appendBox; //for appending polygons to table
    boolean isAppend=false;
    ObjForm form; //the generic form the regimes come from
    FlowForm flowForm; //the Form the regimes come from
    Biol2Form biol2Form;
    BlockForm blockForm;
    int[] objInd=null; //keep track of orig index of objForms in comboBo
    int index=0; //index of currently selected comboBox item
    JLabel nameLabel=new JLabel("Polygon: ");
    RegimeTable regimeTable; //hold regimes for the selected polygon
    RegimeSelectionTool regimeSelectionTool;
    JFrame thisFrame; //used to reference this frame
    Action applyAction, applyThisAction, polySelectAction;
    BasicTable polyTable; //gis polygon Table
    boolean isFromColorTable=false; //indicates selections from polyColorTable

     public RegimeEditor(FrameMenu parent){
	 super("Regime Editor");
	 thisFrame=this;
	 this.parent=parent;
	 setVisible(false);
	 setLocationRelativeTo(parent.gisViewer);
         regimeSelectionTool=new RegimeSelectionTool(this,"Regime Selector");
	 createTable();
	 applyAction=new ApplyAction();
	 applyThisAction=new ApplyThisAction();
	 polySelectAction=new PolySelectAction();
	 addFromTable();
	 createComboBox();
        appendBox=new JCheckBox("Append");
	appendBox.addItemListener(this);
	appendBox.setToolTipText("Append Polygons to Table");
	 JLabel flowLab=new  JLabel("Editing");
	 JPanel flowPanel=new JPanel();
	 flowPanel.add(flowLab);
	 flowPanel.add(comboBox);
	 flowPanel.add(nameLabel);
	 flowPanel.add(appendBox);
	 getContentPane().add(flowPanel,BorderLayout.NORTH);
	 setLocationRelativeTo(parent);
	 setSize(450,210);
	  model.removeTableModelListener(tableListener);
		    saveAction.setEnabled(false);
	  setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      makeRelatedWindowsInvisible();
	      dispose();
	  }
	 }); 
    }/*end constructor*/

     /**
     *handle the appendBox
     **/
     public void itemStateChanged(ItemEvent e) {	
	 isAppend=appendBox.isSelected();
     }

    /**
     *Add some actions from the table 
     **/
    protected void addFromTable(){
	 toolsMenu.add(regimeSelectionTool.delRowsAction).setToolTipText("Delete Selected Rows");
	 toolBar.add(regimeSelectionTool.delRowsAction).setToolTipText("Delete Selected Rows");
	  popup.add(regimeSelectionTool.delRowsAction).setToolTipText("Delete Selected Rows");

	  toolsMenu.add(regimeSelectionTool.delNotRowsAction).setToolTipText("Delete NOT Selected Rows");
	 toolBar.add(regimeSelectionTool.delNotRowsAction).setToolTipText("Delete NOT Selected Rows");
	  popup.add(regimeSelectionTool.delNotRowsAction).setToolTipText("Delete NOT Selected Rows");
	  
	  toolBar.add(applyThisAction).setToolTipText("Apply Modified Regimes in Table");
	  popup.add(applyThisAction).setToolTipText("Apply Modified Regimes in Table");
	  toolBar.addSeparator();
	   toolsMenu.add(saveAction).setToolTipText("Append Modified Regimes to File");
	   JButton tempButton=toolBar.add(saveAction);
	   tempButton.setToolTipText("Append Modified Regimes to File"); 
	   tempButton.setBackground(Color.darkGray);
	   tempButton.setForeground(Color.white);
   
	  popup.add(saveAction).setToolTipText("Append Modified Regimes to File");
	  toolsMenu.add(deleteAction).setToolTipText("Delete Modified Regime File");
	  tempButton=toolBar.add(deleteAction);
	   tempButton.setToolTipText("Delete Modified Regime File");
	   tempButton.setBackground(Color.darkGray);
	   tempButton.setForeground(Color.white);
	  popup.add(deleteAction).setToolTipText("Delete Modified Regime File");
	  toolsMenu.add(applyAction).setToolTipText("Apply Modified Regime File");
	  tempButton=toolBar.add(applyAction);
	  tempButton.setToolTipText("Apply Modified Regime File");
	  tempButton.setBackground(Color.darkGray);
	  tempButton.setForeground(Color.white);
	  popup.add(applyAction).setToolTipText("Apply Modified Regime File");
  	  toolsMenu.add(applyThisAction).setToolTipText("Apply Modified Regimes in Table");
	  toolBar.addSeparator();
	 
	  tempButton=toolBar.add(polySelectAction);
	 tempButton.setToolTipText("Get Selections From Polygon Color Table");
	 tempButton.setBackground(Color.blue);
	  tempButton.setForeground(Color.white);
	  
    }

     /**
     *Close all related RegimeEditor windows when the main viewer isn't
     *visible
     */
    protected void  makeRelatedWindowsInvisible(){
	regimeSelectionTool.setVisible(false);
	setVisible(false);
    }
    
    protected void createComboBox(){
	comboBox=new JComboBox();
	objInd=new int[parent.nComponents+1];
        boolean isInit=false;
        int j=0;
	for (int i=1; i<=parent.nComponents; i++){
	    if(parent.componentName[i].startsWith("F") || parent.componentName[i].startsWith("Bio2") || parent.componentName[i].startsWith("BK") ){
		comboBox.addItem(parent.componentName[i]);
		objInd[j++]=i;

		if(!isInit && parent.componentName[i].startsWith("F")){
		    isInit=true; //F1 is initialized 
		    form=parent.form[i];
		    flowForm=(FlowForm)parent.form[i]; //init to F1
		    setObjType("FLOW");
		}
	    }	   
	} /*end for i*/
	comboBox.setToolTipText("Select a Component for regime editing");
	comboBox.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){ 
		    int jOld=index;
		     if(isTableChanged()){
		       int result=JOptionPane.showConfirmDialog(thisFrame,
		       "Do you want to save edits for "+form.getTitle(),
                       "Regime Edits are Lost When Switching Components?",
		        JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
			if(result==JOptionPane.YES_OPTION){
			    comboBox.setSelectedIndex(jOld);
			    form=parent.form[objInd[jOld]];
			    return;
			}
		    }
		    String formName=(String)comboBox.getSelectedItem();
		    model.removeTableModelListener(tableListener);
		    setIsEditable(true); //make table editable
		    saveAction.setEnabled(false);
		    index=comboBox.getSelectedIndex();
		    form=parent.form[objInd[index]];
		   
		    if(form.dataRead){
			appendBox.setSelected(false);
			setTableChanged(false);
			regimeSelectionTool.reConstruct();
			if(form instanceof FlowForm){
			    flowForm=null;
			    flowForm=(FlowForm)parent.form[objInd[index]];
			    setObjType("FLOW");
			    setFlowData();
			    setSaveFileName(ObjForm.findFile(flowForm.p1.text.getText().trim()));
			}
			else if(form instanceof Biol2Form){
			    biol2Form=null;
			    biol2Form=(Biol2Form)parent.form[objInd[index]];
			    setObjType("BIO2");
			    setBio2Data();
			    setSaveFileName(ObjForm.findFile(biol2Form.p1.text.getText().trim()));
			}
			else if(form instanceof BlockForm){
			    setIsEditable(false); //make table un-editable
			    blockForm=null;
			    blockForm=(BlockForm)parent.form[objInd[index]];
			    setObjType("BLOCK");
			    setBlockData();
			     setSaveFileName(ObjForm.findFile(blockForm.p1.text.getText().trim()));
			}
 
		    }else{//no dataRead 
			comboBox.setSelectedIndex(jOld);
			form=parent.form[objInd[jOld]];
			index=jOld;
			JOptionPane.showMessageDialog(parent.regimeEditor,"Read "+formName+" Data First","NO "+formName+" DATA READ!",JOptionPane.OK_OPTION);
		    }
		   model.addTableModelListener(tableListener);
		}/*end actionPerformed()*/   
	    }); 
	
    }

    /**
     *Called  to set the data. The caller doesn't need to
     *know what type of data to set, so can call from external class
     *e.g. GisViewer.class
     **/
    public void setPolyData(){
	model.removeTableModelListener(tableListener);//not an edit change
	    if(form instanceof FlowForm){
		 if(getSaveFileName()==null)setSaveFileName(ObjForm.findFile(flowForm.p1.text.getText().trim()));
		setFlowData();
	    }
	    else if(form instanceof Biol2Form){
		if(getSaveFileName()==null)setSaveFileName(ObjForm.findFile(biol2Form.p1.text.getText().trim()));
		setBio2Data();
	    }
	     else if(form instanceof BlockForm){
		setBlockData();
	    }
	    //let saveAction be enabled after user editing
		    model.addTableModelListener(tableListener);
    }

    /**
     *Put flow data in the regime edit table
     *
     **/
    protected void setFlowData(){
       
	int rows=0,cols=0, rowSum=0;
	for(int j=1; j<=flowForm.regimeLast; j++){
    int j2=flowForm.data[polyIndex].flowPtr(j);
    if (j2>0){
	int temp=flowForm.data[polyIndex].flow[j2].length;
	cols=(cols>temp)?cols:temp;
	rowSum++;//with dup regimes this could differ from rows
	if(j2>rows)rows=j2;
    }
  }/*end for j*/
	if(rowSum!=rows){ //duplicate regimes
	    JOptionPane.showMessageDialog(parent.regimeEditor,"Blank rows indicate polygon has duplicated regimes.","Duplicate Regimes in "+flowForm.getTitle(),JOptionPane.OK_OPTION);
		    }


	cols++;
	String[][] newData=new String[rows][cols];
	int yrs=(cols-2)/2;
	for(int j=1; j<=flowForm.regimeLast; j++){
    int j2=flowForm.data[polyIndex].flowPtr(j);
    if (j2>0){
        newData[j2-1][0]=polyName;
	newData[j2-1][1]=(String)flowForm.regimeArray.get(j);
	int yrs2=(flowForm.data[polyIndex].flow[j2].length)/2;
        for (int i=1 ; i<=yrs2;i++)
           newData[j2-1][i+1]= Integer.toString(flowForm.data[polyIndex].flow[j2][i]);
	for (int i=1+yrs ; i<=yrs+yrs2;i++)
           newData[j2-1][i+1]= Integer.toString(flowForm.data[polyIndex].flow[j2][i+yrs2-yrs]);

	    }/*end if j2>0*/ 
  }/*end for j*/
	changeData(newData);
    }

    /**
     *Put Bio2 data in the regime edit table
     *
     **/
    protected void setBio2Data(){
	int rows=0;
	int nRegimes=ObjForm.regimeArray.size();
	for(int j=1; j<=nRegimes; j++){
	    if(!Double.isNaN( biol2Form.data[polyIndex].BofX(j)))rows++;
	}
	int cols=3;
	
	String[][] newData=new String[rows][cols];
	
	int j2=0;
	for(int j=1; j<nRegimes; j++){
	    double d=biol2Form.data[polyIndex].BofX(j);
	    if(Double.isNaN(d))continue;
	    newData[j2][0]=polyName;
	    newData[j2][1]=(String)ObjForm.regimeArray.get(j);
	    newData[j2][2]=Double.toString(d);   
	    j2++;
	}/*end for j*/
	changeData(newData);
    }

    
     /**
     *Put Block data in the regime edit table
     *
     **/
    protected void setBlockData(){
	int rows=1;
	int cols=1+blockForm.data[polyIndex].nabes.length;
	
	String[][] newData=new String[rows][cols];


	for(int c=1; c<blockForm.data[polyIndex].nabes.length; c++){
	    newData[0][0]=polyName;
	    newData[0][1]=Float.toString(blockForm.data[polyIndex].size);
	    newData[0][1+c]=(String)ObjForm.polyArray.elementAt(blockForm.data[polyIndex].nabes[c]-1);   
	}/*end for j*/

	changeData(newData);
    }

    /**
     *check to see if polygon is already in table
     *@return true or false
     **/
    public boolean isPolyInTable(String polyName){
	for(int r=0;r<model.getRowCount();r++)
	    if(polyName.equals((String)model.getValueAt(r,0)))return(true);
	return(false);

    }

    /**
     *put newData into the regime table
     **/
    public void changeData(String[][] newData){
	if(isAppend){
	   
	    if(!isFromColorTable && isPolyInTable(polyName)){
		JOptionPane.showMessageDialog(this,"Don't Add same polygon again!","Polygon Already Added",JOptionPane.OK_OPTION);
		return;
	    }
	    int row1=data.appendData(newData);
	    scrollToVisible(row1);
	}else{
	    data.changeData(newData);
	}
	model.fireTableStructureChanged();
    }

    /**
     *Set the current polygon index
     *note that polygon indices generally start at 1 in Habplan
     *Except in the polyArray where they start at 0.
     **/
    public void setPolyIndex(int index){
	polyIndex=index;
	try{
	polyName=(String)ObjForm.polyArray.get(index-1);
	nameLabel.setText("Polygon: "+polyName);
	}catch(Exception e){
	    setVisible(false);
	    JOptionPane.showMessageDialog(this,"Read Some Data First","NO DATA READ!",JOptionPane.OK_OPTION);
	}
    }

    /**
     *Get the regimes for all selected polygons in the polygon color
     *table
     **/
    public void getFromPolyTable(){
	BasicTable polyTable=parent.gisViewer.polyLayer.colorTable;
	if(!polyTable.isVisible())polyTable.clearSelections();
	polyTable.setVisible(true);
        isFromColorTable=true; //prevents question during append about dups

	int[] rows=polyTable.getSelectedTableRows();
	if(rows==null){
	   JOptionPane.showMessageDialog(this,"Select some rows, then push the button again","No Rows Selected In Polygon Color Table",JOptionPane.OK_OPTION);
	   return;
	}
	else{
	  int result=JOptionPane.showConfirmDialog(this,
	   "Add the selected rows to the RegimeEditor? ",
	   "Really Add Selected Rows?",JOptionPane.YES_NO_OPTION,
	   JOptionPane.WARNING_MESSAGE); 
	  if(result==JOptionPane.NO_OPTION)return; 
	}

	boolean isAppendAtStart=isAppend; //is it already in append mode
	for(int i=0;i<rows.length;i++){
	    if(i>0)isAppend=true; //after first row, append the rest
	    int r=rows[i];
	    setPolyIndex(r+1);
	    setPolyData();
	}
	isAppend=isAppendAtStart;//reset isAppend to where it was
       isFromColorTable=false; //prevents question during append about dups
    }
class PolySelectAction extends AbstractAction{
    public PolySelectAction(){
	super("Polys");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	Thread thread=new RunThread(); 
	thread.start();// start colorSelectedRows()
       JProgressMeter progress=new JProgressMeter(parent.regimeEditor,thread,getTitle());
	progress.work();
	}/*end method*/	
}/*end action*/

class ApplyAction extends AbstractAction{
    public ApplyAction(){
	super("Apply");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	applyRegimeEditFile();
	}/*end method*/	
}/*end action*/

class ApplyThisAction extends AbstractAction{
    public ApplyThisAction(){
	super("ApplyThis");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	applyRegimesInTable();
	}/*end method*/	
}/*end action*/


      /**
     *apply the currently active regime edit file
     *Should be used with caution
     **/
    public void applyRegimeEditFile(){
	String saveFileName=getSaveFileName();
	File file=new File(saveFileName);
	if(!file.exists()){
	  JOptionPane.showMessageDialog(this,saveFileName+" does not exist.","NO EDITED REGIMES TO APPLY",JOptionPane.OK_OPTION);
	  return;
	}

	int result=JOptionPane.showConfirmDialog(this,
	   "Apply Regime Edits in: "+saveFileName,
	   "Really Apply Edits?",JOptionPane.YES_NO_OPTION,
	   JOptionPane.WARNING_MESSAGE);
	if(result==JOptionPane.NO_OPTION)return;
	applyRegimesInFile(saveFileName,objInd[index] );
    }

    /**
     *apply regime edits from file to components data in memory
     *@param fName - the file where the edits reside
     *@param objIndex - the index of the ObjForm form[] array
     **/
    public void applyRegimesInFile(String fName, int objIndex ){
	File file=new File(fName);
	if(!file.exists())return;
	String[][] edits; //the edit data goes here
	ObjForm form=parent.form[objIndex];
	if(form.dataRead){
	    edits=readData.read(fName);//get the edits
	    if(form instanceof FlowForm){
		applyFlowEdits(edits,(FlowForm)form);
	    }
	    else if(form instanceof Biol2Form){
		applyBio2Edits(edits,(Biol2Form)form);
	    }
	     else if(form instanceof BlockForm){
		 return;
	    }

	}/*fi dataRead*/
	
    }

     /**
     *apply regime edits in the current table
     *use applyRegimesInFile() to apply the filed edits
     **/
    public void applyRegimesInTable( ){
	String[][] edits; //the edit data goes here
	    int result=JOptionPane.showConfirmDialog(this,
	   "Apply Regime Edits in This Table? ",
	   "Really Apply Edits?",JOptionPane.YES_NO_OPTION,
	   JOptionPane.WARNING_MESSAGE);
	if(result==JOptionPane.NO_OPTION)return;
	if(form.dataRead){
	    edits=data.dumpData();
	    if(form instanceof FlowForm){
		applyFlowEdits(edits,(FlowForm)form);
	    }
	    else if(form instanceof Biol2Form){
		applyBio2Edits(edits,(Biol2Form)form);
	    }
	     else if(form instanceof BlockForm){
		 return;
	    }

	}/*fi dataRead*/
	
    }

 /**
     *apply some BIO2 component edits in a String matrix to data in memory
     *@param edits - string matrix of edits
     *@param form - the index of the component
     **/
    protected void applyBio2Edits(String[][] edits, Biol2Form form){
	int rows=edits.length;
	int cols=edits[0].length;
	String polyOld="adc@#$(12"; //previous polygon name
	String poly=null;
	int polyIndex=0,regimeIndex=0;
	int nregs=0;//count n of regimes per polygon
	int[] regime=null;
	double[] BofX=null;
	try{
	for(int i=0;i<rows;i++){
	    poly=edits[i][0];
	    if(!poly.equals(polyOld)){//go here when starting a new polygon
		if(polyIndex!=0){
		    form.data[polyIndex]=new Biol2Data();
		    form.data[polyIndex].regime=new int[nregs+1];
		    form.data[polyIndex].BofX=new double[nregs+1];
		    if(ObjForm.polySched.length>polyIndex){//might not be setup
		     ObjForm.polySched[polyIndex]=regimeIndex; //reset regime
		    }
		    for(int j=1;j<=nregs;j++){
			form.data[polyIndex].regime[j]=regime[j];
			form.data[polyIndex].BofX[j]=BofX[j];
		    }
		}
	       polyIndex=Integer.parseInt((String)ObjForm.polyTable.get(poly));
	       polyOld=poly;
	       regime=new int[rows+1]; //tmp regime data
	       BofX=new double[rows+1];
	       nregs=0;
	    }
	    regimeIndex=Integer.parseInt((String)ObjForm.regimeTable.get(edits[i][1]));
	    nregs++;
	  
		regime[nregs]=regimeIndex;
	        BofX[nregs]=Double.parseDouble(edits[i][2]);  
	    
	}

		if(rows>1){//get the last polygon before returning
		   form.data[polyIndex]=new Biol2Data();
		   form.data[polyIndex].regime=new int[nregs+1];
		   form.data[polyIndex].BofX=new double[nregs+1];
		   if(ObjForm.polySched.length>polyIndex){//might not be setup
		    ObjForm.polySched[polyIndex]=regimeIndex; //reset regime
		   }
		    for(int j=1;j<=nregs;j++){
			form.data[polyIndex].regime[j]=regime[j];
			form.data[polyIndex].BofX[j]=BofX[j];
		    }
		}

		 }catch(Exception e){
	    JOptionPane.showMessageDialog(parent.regimeEditor,"Edit data polygon: "+poly+": "+e,"Bad BIO2 Edit Data! ",JOptionPane.OK_OPTION);
		return;

	    }
    }


     /**
     *apply some flow component edits in a String matrix to data in memory
     *@param edits - string matrix of edits
     *@param form - the index of the component
     **/
    protected void applyFlowEdits(String[][] edits, FlowForm form){
	int rows=edits.length;
	int cols=edits[0].length;
	String polyOld="adc@#$(12"; //previous polygon name
	String poly=null;
	int polyIndex=0,regimeIndex=0;
	int nregs=0;//count n of regimes per polygon
	int[][] temp=null;
	try{
	for(int i=0;i<rows;i++){
	    poly=edits[i][0];
	    if(poly==null || poly.trim().equals(""))continue;
	    if(!poly.equals(polyOld)){//go here when starting a new polygon
		if(polyIndex!=0){
		    form.data[polyIndex]=new FlowData();
		    form.data[polyIndex].flow=new int[nregs+1][];
		    if(ObjForm.polySched.length>polyIndex){//might not be setup
		     ObjForm.polySched[polyIndex]=regimeIndex; //reset regime
		    }
		    for(int j=1;j<=nregs;j++)
			form.data[polyIndex].flow[j]=temp[j];
		}
	       polyIndex=Integer.parseInt((String)ObjForm.polyTable.get(poly));
	       polyOld=poly;
	       temp=new int[rows+1][]; //temp poly flow data
	       nregs=0;
	    }
	    regimeIndex=Integer.parseInt((String)ObjForm.regimeTable.get(edits[i][1]));
	    nregs++;
	    int fOuts=1;
	    for(int r=2;r<cols;r++){
		if(edits[i][r]!=null && !edits[i][r].equals(""))fOuts++;
	    }
	    temp[nregs]=new int[fOuts];
		int r1=0;
		temp[nregs][r1++]=regimeIndex;
		for(int r=2;r<cols;r++){
		if(edits[i][r]!=null && !edits[i][r].equals(""))temp[nregs][r1++]=Integer.parseInt(edits[i][r]);    
		}
	}

		if(polyIndex!=0){//get last polygon
		    form.data[polyIndex]=new FlowData();
		    form.data[polyIndex].flow=new int[nregs+1][];
		    if(ObjForm.polySched.length>polyIndex){
		     ObjForm.polySched[polyIndex]=regimeIndex;
		    }
		    for(int j=1;j<=nregs;j++){
			form.data[polyIndex].flow[j]=temp[j];
		    }
		}
	    }catch(Exception e){
	    JOptionPane.showMessageDialog(parent.regimeEditor,"Bad Edit Data polygon: "+poly+": "+e,"Bad Flow Edit Data! ",JOptionPane.OK_OPTION);
		parent.console.println("Flow edit data error: "+e);
		return;

	    }
    }

     /**
     *Shows message dialogs without killing the thread that calls it
     *
     **/
    void showMessageDialog(final Component myParent,final String message, final String title,final int option,final Icon icon)  {
	try{
	    Runnable showModalDialog = new Runnable() {
		    public void run() {
		JOptionPane.showInternalMessageDialog(myParent,
					    message,title,option,icon);
		    }
		};
	    SwingUtilities.invokeAndWait(showModalDialog);
	}catch(Exception e){
	    System.out.println("Error in showMessageDialog: "+e);
	}
    }    
 

    /**
     *inner class to do the time consuming coloring
     * operation on a separate thread. 
     */  
    class RunThread extends Thread{

	public RunThread(){
	   
	}/*end constructor*/
	public void run(){
	    getFromPolyTable();
		return;
	}/*end method*/  
    }/*end inner Thread class*/


}
