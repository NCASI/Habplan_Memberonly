/**
*class to allow row selections in Color tables
*@author Paul Van Deusen 11/2000
*/
package gis;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class SelectionTool extends JFrame{
   
    BasicTable parentTable;
    ListSelectionModel selectionModel;
    JLabel selectedLabel; //first, last selected rows, see valueChanged()
    boolean isListening=false; //indicates if a table listener was added
    VariableRow[] variableRow; //defined as inner class
    boolean[] isNumeric;  //true is a column holds numeric data/ alphaNumeric otherwise
    JPanel tool;//the tool is constructed in this panel
    protected int rows=0,cols=0; //n of rows and cols in parent table
    
    Action showAction,selectRowsAction;
    JToolBar toolBar;

   
    public SelectionTool(BasicTable parentTable, String title){
	super(title);
	this.parentTable=parentTable;
  
     getContentPane().setLayout(new BorderLayout());
     
     Rectangle d=parentTable.getBounds();
     setBounds(d.x+d.width/3,d.y+d.height/3,d.width/2,d.height/2);
     setVisible(false);
     selectedLabel=new JLabel("First=0 Last=0");
     selectedLabel.setToolTipText("First and Last Selected Rows");
     getContentPane().add(selectedLabel,BorderLayout.NORTH);
     addToMenus();//add actions to parentTable menus
     createToolBar();
     
   }/*end constructor*/
  
   
 protected void createToolBar(){
    toolBar=new JToolBar();
    selectRowsAction=new SelectRowsAction();
    toolBar.add(selectRowsAction).setToolTipText("Do the row selection");
    JButton unselectButton=toolBar.add(parentTable.clearSelectedRowsAction);
    unselectButton.setToolTipText("UnSelect all rows");
    toolBar.addSeparator();
    getContentPane().add(toolBar,BorderLayout.SOUTH);
    
    }/*end method*/    

/**
*add actions to parent Table popup menu
*/   
   protected void addToMenus(){
    showAction=new ShowAction();   
    parentTable.popup.add(showAction).setToolTipText("Toggle SelectionTool");
    parentTable.toolsMenu.add(showAction).setToolTipText("Toggle SelectionTool");
  }/*end method*/
       
   class ShowAction extends AbstractAction{
    public ShowAction(){
      super("SelectionTool");
    }
    public void actionPerformed(ActionEvent e){
      if(!isListening){//add listener only on first opening
	reConstruct();//construct the tool when listener is added
        parentTable.table.getModel().addTableModelListener(new TableListener());
	selectionModel=parentTable.table.getSelectionModel();
      //determine which rows are selected
	 selectionModel.addListSelectionListener( new ListSelectionListener(){
	public void valueChanged(ListSelectionEvent e){
	      int first=selectionModel.getMinSelectionIndex()+1;
	      int last=selectionModel.getMaxSelectionIndex()+1;
              selectedLabel.setText("First="+first+" Last="+last);
	}/*end valueChanged method*/
	     });
	isListening=true;
      }/*end if listen*/
      setVisible(!isVisible());
    }/*end actionPerformed()*/
  }/*end Action*/
  
 class SelectRowsAction extends AbstractAction{
    public SelectRowsAction(){
      super("Select");
    }
    public void actionPerformed(ActionEvent e){
      (new SelectionThread("select")).start();
    }/*end actionPerformed()*/
  }/*end Action*/


  /**
  *select rows in the table according to limits specified
  */
  protected void selectIfRows(){
       //these index numbers are used to batch sequential selections
      int nextIndex=-99; // the next index in a continuous sequence
      int lastIndex=-99; //the last index selected
      cols=parentTable.model.getColumnCount();
      rows=parentTable.model.getRowCount();
      int nRowsSelected=0; //holds the n of rows selected on this pass
     
      
      String[] lower=new String[cols], //string value of user specified limits
	       upper=new String[cols];
      double[] dLower=new double[cols], //numeric value of limits
	       dUpper=new double[cols];
      boolean[] blankLower=new boolean[cols], //are the limits blank      
	        blankUpper=new boolean[cols];
      boolean isLowerOK=false, isUpperOK=false; //hold comparison of limits to cell value
       isNumeric=new boolean[cols];//is this a number var, based on checkBox setting
     
     //lower and upper will be used for alphanumerics
     //dLower and dUpper used for numeric variables contribution to row selection
      for(int c=1;c<cols;c++){ //init lower and upper
	isNumeric[c]=variableRow[c].checkBox.isSelected(); //true means its a number
	lower[c]=variableRow[c].GE.getText().trim();       //hold the user specified bounds
	upper[c]=variableRow[c].LE.getText().trim();
	blankLower[c]=lower[c].compareTo("")==0;
	blankUpper[c]=upper[c].compareTo("")==0;
	if(isNumeric[c]){ //convert to doubles if true and the space is not blank
	  try{	
	   dLower[c]=(!blankLower[c])?new Double(lower[c]).doubleValue():-99;
	   dUpper[c]=(!blankUpper[c])?new Double(upper[c]).doubleValue():-99; 
	  }catch(Exception e){
	      String message="Change "+parentTable.model.getColumnName(c)+" to Alphanumeric, see limits";
      	      alphaNumericMessage(message,"Alphanumeric Mis-Specifition",c);
	     }/*end catch*/
	}/*end if numeric*/
      }/*end for c*/
      
      String cell=""; //the table cell value
      
      boolean selectThisRow;
      for(int r=0;r<rows;r++){	     
	  selectThisRow=true; //the row is selected unless a comparison fails
	 

      for(int c=1;c<cols;c++){
	 if(!selectThisRow)break;
	
	if(blankLower[c] & blankUpper[c]){ continue;} //waste no more time on this variable/column
	cell=(String)parentTable.model.getValueAt(r,c);
    
     if(!isNumeric[c]){
	isUpperOK=cell.compareTo(upper[c])<=0;
	isLowerOK=cell.compareTo(lower[c])>=0; 
     }
     else{
	try{ 
	isUpperOK=new Double(cell).doubleValue()<=dUpper[c];
	isLowerOK=new Double(cell).doubleValue()>=dLower[c];
	 }catch(Exception e){
	      int row=r+1;//report the row with first row =1 not 0
	      String message="Change "+parentTable.model.getColumnName(c)+" to alphaNumeric, see row "+row;
	      alphaNumericMessage(message,"Alphanumeric Mis-Specifition",c);
	      if(!isNumeric[c])
	       showMessageDialog(this,"The change to alphanumeric may cause bad selections",
	      "Possible Row Selection Error",JOptionPane.YES_NO_OPTION, parentTable.tableIcon);
	     }/*end catch*/
     }/*end if isNumeric*/
	
	if(blankLower[c] & !isUpperOK){ selectThisRow=false; continue;}
	if(blankUpper[c] & !isLowerOK){ selectThisRow=false;continue;}
	if(!blankLower[c] & !blankUpper[c])
	 if(!isLowerOK | !isUpperOK){selectThisRow=false;continue;}
     	 
       }/*end for cols*/       
        if(selectThisRow && !parentTable.selectionModel.isSelectedIndex(r)){
	   if(lastIndex<0){lastIndex=r;nextIndex=r+1;}//initialize
	   if(nextIndex==r){
	       nextIndex++;
	   }
           else{
	       selectionModel.addSelectionInterval(lastIndex,nextIndex-1);
	       selectionModel.addSelectionInterval(r,r);//this takes long time
	       lastIndex=r;
	       nextIndex=r+1;
	   }
	   nRowsSelected++;	
	}/*end if selectRows*/
     }/*end for rows*/
       //make sure the last selected batch is actually selected
      if(nRowsSelected>0 && lastIndex<rows)selectionModel.addSelectionInterval(lastIndex,nextIndex-1);
      String message=nRowsSelected+" additional rows were selected.";
          showMessageDialog(toolBar,message,"Row Selection Report",JOptionPane.OK_OPTION, parentTable.tableIcon);
	
  }/*end method*/

 public void alphaNumericMessage(String message, String header, int c){
       int result=showConfirmDialog(this,
       message,header,JOptionPane.YES_NO_OPTION,
       JOptionPane.WARNING_MESSAGE,parentTable.tableIcon);
        switch(result){
	case JOptionPane.YES_OPTION:
	  variableRow[c].checkBox.setSelected(false);
	  isNumeric[c]=false;
	 break;
	case JOptionPane.NO_OPTION:   
	 return; //dont do it
	  }/*end switch*/ 	
 }/*end method*/ 
 

  /**
  *class to make a single row in the selection panel
  *constructor needs a column name
  */
  protected class VariableRow extends JPanel{
      JTextField GE, LE;
      JLabel name;
      JCheckBox checkBox;
      protected String columnName;   
      
      public VariableRow(String columnName){
	  this.columnName=columnName;
	  name=new JLabel(columnName,JLabel.RIGHT);
	  name.setToolTipText("Specify selection limits for this variable");
       GE=new JTextField(2);
       GE.setToolTipText("Rows are selected if GE this value");  
       GE.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
       GE.setBackground(Color.white);
       LE=new JTextField(2); 
       LE.setToolTipText("Rows are selected if LE this value");
       LE.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
       LE.setBackground(Color.white);
       checkBox=new JCheckBox();
       checkBox.setSelected(true); //normal is numeric 
       checkBox.setToolTipText("true=numeric, false=String");
       setLayout(new GridLayout(1,4));
       add(checkBox);
       add(name);
       add(GE);
       add(LE);      
      }/*end constructor*/
      
  }/*end inner class*/
  
  /**
  *reConstruct the tool to conform to the current table configuration
  */
  protected void reConstruct(){
    if (cols==parentTable.model.getColumnCount())return;  //no need to redo
    else cols=parentTable.model.getColumnCount();
    Container con=getContentPane();
   if(isListening){// listener indicates that its not the first construction
      setVisible(false);
     con.removeAll();
     getContentPane().add(selectedLabel,BorderLayout.NORTH);
     getContentPane().add(toolBar,BorderLayout.SOUTH);
    }
    tool=new JPanel(new GridLayout(cols+1,1));
    JPanel header=new JPanel(new GridLayout(1,4));
    variableRow=new VariableRow[cols];
    JLabel varType, name, lower, upper; //headers
    varType=new JLabel("Numeric",JLabel.LEFT);
    name=new JLabel("COLUMN",JLabel.RIGHT);
    lower=new JLabel("GE",JLabel.CENTER);
    upper=new JLabel("LE",JLabel.CENTER);
    header.add(varType);
    header.add(name);
    header.add(lower);
    header.add(upper);
    
     tool.add(header);
     for (int i=1;i<cols;i++){  //don't include col 0 = Color
	variableRow[i]=new VariableRow(parentTable.model.getColumnName(i));
	tool.add(variableRow[i]);
    }/*end for i*/
    JScrollPane scrollPane= new JScrollPane(tool,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    con.add(scrollPane,BorderLayout.CENTER);
    pack();
     Rectangle loc=parentTable.getBounds();
    loc.translate(50,50);
    loc.setSize(300,200);
    setBounds(loc);  
  }/*end method*/
  
 /**
*Listener to know when a change occured 
*/  
 class TableListener implements  TableModelListener{  
    public void tableChanged(TableModelEvent e){
      reConstruct(); //might need to rebuild the tool
    }  /*end method*/  
 }/*end listener class*/ 


    /**
     *Shows message dialogs without killing the thread that calls it
     *
     **/
    void showMessageDialog(final Component myParent,final String message, final String title,final int option,final Icon icon)  {
	try{
	    Runnable showModalDialog = new Runnable() {
		    public void run() {
			JOptionPane.showMessageDialog(myParent,
				        message,title,option,icon);
		    }
		};
	    SwingUtilities.invokeAndWait(showModalDialog);
	}catch(Exception e){
	    System.out.println("Error in showMessageDialog: "+e);
	}
    }     
    
     /**
     *Shows confirm dialogs without killing the thread that calls it
     *
     **/
    int showConfirmDialog(final Component myParent,final String message, final String title,final int option,final int type, final Icon icon)  {
	final int[] value=new int[1];//lets you assign value to a final int
	try{
	    Runnable showModalDialog = new Runnable() {
		    public void run() {
		    value[0]=JOptionPane.showConfirmDialog(myParent,
				        message,title,option,type,icon);
		    }
		};
	    SwingUtilities.invokeAndWait(showModalDialog);
	    return(value[0]);
	}catch(Exception e){
	    System.out.println("Error in showMessageDialog: "+e);
	    return(-1);
	}
    } 


/**
*inner class to do the time consuming apply
* operation on a separate thread. 
*/  
  class SelectionThread extends Thread{
      String method;
     public SelectionThread(String method){
	 this.method=method;
	 }/*end constructor*/
     public void run(){
	    showAction.setEnabled(false);
	    selectRowsAction.setEnabled(false);
	    parentTable.clearSelectedRowsAction.setEnabled(false);
	     if(method.equals("select"))selectIfRows(); //run this method
	    showAction.setEnabled(true);
	    selectRowsAction.setEnabled(true);
	    if(parentTable.getSelectedTableRows()!=null)
	    parentTable.clearSelectedRowsAction.setEnabled(true);
	   
     }/*end method*/  
  }/*end inner Thread class*/

}/*end class*/
