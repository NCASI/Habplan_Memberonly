/*
 * Table for manipulating Regime
 * @author  Paul Van Deusen
 * @version 1.0
 */
package regime;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

public class RegimeTable extends JFrame{
    //override tableIcon and columnNames  in extensions.
    protected ImageIcon tableIcon; 
    String[] columnNames={"Polygon","Regime","yr1","out1","v1","v2"};
    public RegimeTableData data, oldData; //holds the current and original
    int nUndo=3;//number of possible undos
    int currentUndo=0; // keeps track of currently saved undos
    int availableUndo=0; //actual available saves for Undos
    boolean isEditable=true;//returned by isCellEditable()
    RegimeTableData[] undoData=new RegimeTableData[nUndo]; //used to undo the last edit
    JTable table;
    
    String saveFileName=null;
    String objType="FLOW";//selected Obj Component,FLOW, BIO2 or BLOCK
    public int polyIndex=1; //the current polygon of interest
    public String polyName="";
    HBPFilter filter=new HBPFilter("edits","Regime Edit Files");
    public ReadData readData; //class for reading data
    ListSelectionModel selectionModel; //the table selection model
     Action readDataAction,writeDataAction,addRowAction,delRowAction,
      addColAction,delColAction,selectAllRowsAction,
      clearSelectedRowsAction, undoAction;
    public Action saveAction, deleteAction;
    JMenuBar menuBar;
    JMenu editMenu; 
    public JMenu toolsMenu; 
    public JPopupMenu popup;
    public JToolBar toolBar;
    public CustomTableModel model; //the Table model
    public TableListener tableListener;
    boolean isTableChanged=false; //remember if table data are changed for saving
 
  
  public RegimeTable(String title) {
     super(title);
    data=new RegimeTableData();
    readData=new ReadData(this);//used for reading table data from files
    createMenuBar();
    createToolBar();
    createPopup();
    
  }/*end constructor*/
  
  private void createMenuBar(){
      //make the actions here too
    addRowAction=new AddRowAction();
    delRowAction=new DelRowAction();
    addColAction=new AddColAction();
    delColAction=new DelColAction();
    undoAction=new UndoAction();
    selectAllRowsAction=new SelectAllRowsAction();
    clearSelectedRowsAction=new ClearSelectedRowsAction();
    saveAction=new SaveAction();
    deleteAction=new DeleteAction();
    readDataAction=new ReadDataAction();
    writeDataAction=new WriteDataAction();
     //make the menus
    menuBar=new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.add(readDataAction).setToolTipText("Read table data from file");
    fileMenu.add(writeDataAction).setToolTipText("Save table data to file");
    menuBar.add(fileMenu);
    editMenu = new JMenu("Edit");
    editMenu.add(undoAction).setToolTipText("Undo previous changes");
    editMenu.add(selectAllRowsAction).setToolTipText("Select all rows");
    editMenu.add(clearSelectedRowsAction).setToolTipText("UnSelect all rows");
    menuBar.add(editMenu);
    toolsMenu = new JMenu("Tools");
    //toolsMenu.add(delRowAction).setToolTipText("Delete a row after 1st selection");
    menuBar.add(toolsMenu);
    setJMenuBar(menuBar);
  }/*end createMenuBar()*/
  
protected void createToolBar(){
    toolBar=new JToolBar();
    //toolBar.add(delRowAction).setToolTipText("Delete selected row");
   
    getContentPane().add(toolBar,BorderLayout.SOUTH);
    }/*end method*/  
/**
*create popup menu
*/  
protected void createPopup(){
    popup=new JPopupMenu();
    popup.add(selectAllRowsAction).setToolTipText("Select all rows");
    popup.add(clearSelectedRowsAction).setToolTipText("UnSelect all rows");
    this.addMouseListener(new MouseAdapter(){
	public void mousePressed(MouseEvent e){
	    if(e.isPopupTrigger())popup.show(menuBar,e.getX(),e.getY());
	}
    });
}/*end method*/

 
   /**
  *Set Table icon
  */
  public void setTableIcon(ImageIcon icon){
       tableIcon=icon;
  }/*end method*/

  
 class ReadDataAction extends AbstractAction{
    public ReadDataAction(){
      super("ReadData");
    }
    public void actionPerformed(ActionEvent e){
	try{saveUndoData();} catch(Exception e2){}
      readData();
    }/*end actionPerformed()*/
  }/*end Action*/
  
  class WriteDataAction extends AbstractAction{
    public WriteDataAction(){
      super("WriteData");
    }
    public void actionPerformed(ActionEvent e){
     writeData();
    }/*end actionPerformed()*/
  }/*end Action*/

class AddRowAction extends AbstractAction{
    public AddRowAction(){
	super("Row+");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	try{saveUndoData();} catch(Exception e2){}
	model.addRow();
	}/*end method*/	
}/*end action*/

class DelRowAction extends AbstractAction{
    public DelRowAction(){
	super("Row-");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	try{saveUndoData();} catch(Exception e2){}
	model.delRow();
	}/*end method*/	
}/*end action*/


class AddColAction extends AbstractAction{
    public AddColAction(){
	super("Col+");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	try{saveUndoData();} catch(Exception e2){}
	model.addCol();
	}/*end method*/	
}/*end action*/

class DelColAction extends AbstractAction{
    public DelColAction(){
	super("Col-");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	try{saveUndoData();} catch(Exception e2){}
	model.delCol();
	}/*end method*/	
}/*end action*/

class SelectAllRowsAction extends AbstractAction{
    public SelectAllRowsAction(){
	super("SelectAll");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	selectionModel.setSelectionInterval(0,data.rows-1);
	}/*end method*/	
}/*end action*/

   class ClearSelectedRowsAction extends AbstractAction{
    public ClearSelectedRowsAction(){
      super("UnSelect");
    }
    public void actionPerformed(ActionEvent e){
      selectionModel.clearSelection();
    }/*end actionPerformed()*/
  }/*end Action*/

class UndoAction extends AbstractAction{
    public UndoAction(){
	super("Undo");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	 if(availableUndo>0)data=undoData[currentUndo++];
	  model.removeTableModelListener(tableListener);
	 model.fireTableStructureChanged();   
	  model.addTableModelListener(tableListener);
	 if(currentUndo>=availableUndo)currentUndo--; //only this many available data objects
         if(availableUndo==0){
	     isTableChanged=false;
	     saveAction.setEnabled(false);
	 }
	}/*end method*/	
}/*end action*/

class SaveAction extends AbstractAction{
    public SaveAction(){
	super("Save");
	setEnabled(false);
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	saveModifiedRegimes();
	}/*end method*/	
}/*end action*/

class DeleteAction extends AbstractAction{
    public DeleteAction(){
	super("Delete");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	deleteRegimeEditFile();
	}/*end method*/	
}/*end action*/


/**
*save old table data for an undo
*/
protected void saveUndoData()
  throws CloneNotSupportedException{
     currentUndo=0; //reset save pointer
     if(++availableUndo>nUndo)availableUndo--;//avoid nullpointer when using undo after too few saves
    for(int i=nUndo-1;i>0;i--)undoData[i]=undoData[i-1];//shift undoData ahead
    undoData[0]=(RegimeTableData)data.clone();
    //System.out.println("clone rows cols "+undoData.rows+" "+undoData.cols);
}/*end method*/
  
/**
*make the table, called by MainWindow.onOpening()
*/
public void createTable(){	
	model=new CustomTableModel();
	table=new JTable(model);
	tableListener=new TableListener();
	table.getModel().addTableModelListener(tableListener);
	table.getTableHeader().setToolTipText("Edits don't take hold until you click on another cell.");
	//table.setBounds(50,50,100,100);
	selectionModel=table.getSelectionModel();
	//table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	JScrollPane scroll=new JScrollPane(table);
	getContentPane().add(scroll,BorderLayout.CENTER);
	//add horizontal scrolling for big tables
	     table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//determine which rows are selected
	 selectionModel.addListSelectionListener(
      new ListSelectionListener(){
	public void valueChanged(ListSelectionEvent e){
	    // int  firstSelectedRow=selectionModel.getMinSelectionIndex();
	    //int  lastSelectedRow=selectionModel.getMaxSelectionIndex();
	//does nothing now, might need it later
	}/*end valueChanged method*/
    });
	
    }
 /**
 *save the current Table values
 *actually they are saved to disk until the user exits program
 */   
protected void saveTable(){
      int result=JOptionPane.showConfirmDialog(this,
      "Save Regime changes?",
       "Are You Sure?",JOptionPane.OK_CANCEL_OPTION,
       JOptionPane.WARNING_MESSAGE,tableIcon);
      switch(result){
	case JOptionPane.OK_OPTION:
	 try{
	  oldData=(RegimeTableData)data.clone();//overwrite old data
	  }catch(java.lang.CloneNotSupportedException e){System.out.println("Error on save: "+e);}
	  isTableChanged=false; //so onClosing doesn't ask any more questions
	  saveAction.setEnabled(false);
	 break;
	case JOptionPane.CANCEL_OPTION:   
	 return; //abort the save
	  }/*end switch*/ 	
    }/*end method*/

    /**
     *  Scrolls the row (rowIndex) so that it is visible within the
     * viewport.
     *@param rowIndex - the row you want to see
     **/
    public void scrollToVisible(int rowIndex) {
       
        JViewport viewport = (JViewport)table.getParent();
	int vColIndex=0; //base view on first cell in row
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
    
        // Translate the cell location so that it is relative
        rect.setLocation(rect.x-pt.x, rect.y-pt.y-2*rect.height);
        // Scroll the area into view 
        viewport.scrollRectToVisible(rect);
    }
  
    /**
     *set the value returned by isCellEditable()
     **/
    public void setIsEditable(boolean isEdit){isEditable=isEdit;}

/**
*Listener to know when a change occured 
*/    
public class TableListener implements TableModelListener{
     
    public void tableChanged(TableModelEvent e){
	isTableChanged=true;
	saveAction.setEnabled(true);
	}  /*end method*/  
    }
/**
* inner class for custom table model
*/    
public class CustomTableModel extends AbstractTableModel{
    public int getRowCount(){return data.rows;}
    public int getColumnCount(){return data.cols;}
    
    public Object getValueAt(int row,int col){return data.rowData[row][col];}
    public String getColumnName(int col){try{return data.columnNames[col];
	   }catch(Exception e){JOptionPane.showMessageDialog(menuBar,"Resetting Column Names.","Column error",JOptionPane.OK_OPTION,tableIcon);
	   data.setColumnNames();
	   return data.columnNames[col];}/*end try catch*/
	}/*end method*/	
    public boolean isCellEditable(int row,int col){return isEditable;}
    public void setValueAt(Object value,int row,int col){
	//System.out.println("setValueat row col "+row+" "+col);
	try{saveUndoData();} catch(Exception e2){}
	data.rowData[row][col]=(String)value;
	fireTableCellUpdated(row,col);
	}/*end setValueAt*/
    public void addRow(){
	int leadIndex=selectionModel.getLeadSelectionIndex();
	if(leadIndex<0)JOptionPane.showMessageDialog(toolBar,"Select an Insertion Point!","Row Selection Error",JOptionPane.YES_NO_OPTION,tableIcon);
	 else{
	int selectedRow=leadIndex>table.getRowCount()-1?
			    table.getRowCount()-1:leadIndex;
	data.addRow(selectedRow);
        fireTableRowsInserted(selectedRow+1,selectedRow+2);
	  }/*end else*/
	}/*end method()*/
    public void delRow(){
	int leadIndex=selectionModel.getLeadSelectionIndex();
	if(leadIndex<0)JOptionPane.showMessageDialog(toolBar,"Select a row to delete!","Row Selection Error",JOptionPane.YES_NO_OPTION,tableIcon);
	 else{
	int selectedRow=leadIndex>table.getRowCount()-1?
			    table.getRowCount()-1:leadIndex;
	selectedRow=data.delRow(selectedRow);
	fireTableRowsDeleted(selectedRow,selectedRow+1);
	 }/*end else*/
	}/*end method()*/	
   public void addCol(){
	data.addCol();
	}/*end method()*/
    public void delCol(){
	data.delCol(); fireTableStructureChanged();
	}/*end method()*/
	    
}/*end CustomTableModel*/

    /**
     *delete the currently active regime edit file
     *Should be used with caution
     **/
    public void deleteRegimeEditFile(){
	File file=new File(saveFileName);
	if(!file.exists()){
	  JOptionPane.showMessageDialog(this,"There are no saved regime edits to delete","NOTHING TO DELETE",JOptionPane.OK_OPTION);
	  return;
	}

	int result=JOptionPane.showConfirmDialog(this,
	   "Delete Regime Edits in: "+saveFileName,
	   "Really Delete?",JOptionPane.YES_NO_OPTION,
	   JOptionPane.WARNING_MESSAGE);
	if(result==JOptionPane.NO_OPTION)return;
		
	file.delete();
    }

  
    /**
     *   save the modified regimes
     **/
    public void saveModifiedRegimes(){
	File file=new File(saveFileName);
	String[][] tempData=null, tempData2=null;
	int rows=0,cols=0;
        Vector keep=new Vector(); //row from file to keep
	if(file.exists()){//read existing edits and get rid of current polygons
	    tempData2=readData.read(saveFileName);
	    if(tempData2!=null){
		rows=tempData2.length;
		cols=tempData2[0].length;
		for(int i=0;i<rows;i++){
		    boolean isKeep=true;
		   for(int r=0;r<model.getRowCount();r++){
		       if(tempData2[i][0].equals((String)model.getValueAt(r,0))){
			   isKeep=false;
			   break;
		       }
		   }
		   if(isKeep)keep.add(new Integer(i));//keep this row
		}
		rows=keep.size();
		if(rows>0){
		    tempData=new String[rows][cols];
		    for(int j=0;j<rows;j++){
			int i=((Integer)(keep.get(j))).intValue();
			tempData[j]=tempData2[i];
			
		    }
		}
	    }    
	}

	    if(tempData==null){//no pre-existing edits
		tempData2=data.rowData;
	    }else{//append to pre-existing edits
		tempData2=data.appendByType(tempData);
	    }
	    	int result=JOptionPane.showConfirmDialog(this,
		   "Save modified regimes to "+saveFileName,
                   "Save Changes?",JOptionPane.YES_NO_OPTION,
		    JOptionPane.WARNING_MESSAGE);
		if(result==JOptionPane.YES_OPTION){
		    readData.write(saveFileName,tempData2);//write something    
		    isTableChanged=false;
		    saveAction.setEnabled(false);
		}
    }

  /**
   *Read a Data file into a table
   */
  public void readData(){
    JFileChooser chooser = new JFileChooser(saveFileName);
    chooser.setFileFilter(filter);
    int state=chooser.showOpenDialog(this);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
	String dataFile=file.getPath();
	String[][] tempData=readData.read(dataFile);
	 if(tempData==null)return;
	 data.changeData(tempData);
	 model.fireTableStructureChanged();
	}
    else if (state==JFileChooser.CANCEL_OPTION);
    saveAction.setEnabled(false);
    isTableChanged=false;
  }/*end method*/
  
    /**
   *Save a Table into a Data file
   */
  public void writeData(){
    JFileChooser chooser = new JFileChooser(saveFileName);
     chooser.setFileFilter(filter);
    int state=chooser.showSaveDialog(this);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
	String dataFile=file.getPath();
	int rows=model.getRowCount();
	int cols=model.getColumnCount();
	String[][] rowData;
     int result=JOptionPane.showConfirmDialog(menuBar,
       "Put Column Names in Row 1","Column Names Question",JOptionPane.YES_NO_CANCEL_OPTION,
       JOptionPane.QUESTION_MESSAGE);
       if(result==JOptionPane.CANCEL_OPTION)return;
       if(result==JOptionPane.YES_OPTION){ 
	    rowData=new String[rows+1][cols];
	   for(int c=0;c<cols;c++) rowData[0][c]=(String)model.getColumnName(c);
	   for(int r=1;r<rows+1;r++) rowData[r]=data.rowData[r-1];
       }
       else{ rowData=new String[rows][cols]; 
       	      for(int r=0;r<rows;r++)rowData[r]=data.rowData[r]; 
	  }/*end if YES_OPTION*/         
	 readData.write(dataFile,rowData);    
	}/*end if APPROVE_OPTION*/
    else if (state==JFileChooser.CANCEL_OPTION);
  }/*end method*/

    public void setSaveFileName(String fname){saveFileName=fname+".edits";}
    public String getSaveFileName(){return(saveFileName);}
    public void setObjType(String type){
	objType=type;
	data.setObjType(type);
}
    public String getObjType(){return(objType);}
    public boolean isTableChanged(){return(isTableChanged);}
    public void setTableChanged(boolean changed){isTableChanged=changed;}
}/*end class*/
  
