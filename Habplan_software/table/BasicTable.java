/*
 * Basic Table for Habgen 9/2000
 * @author  Paul Van Deusen
 * @version 1.0
 */
package table;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

public class BasicTable extends JFrame{
    //override tableIcon and columnNames  in extensions.
    protected ImageIcon tableIcon; 
    protected String columnNamePrefix="Var"; //prefix of column name when user adds a column
    protected String[] columnNames={"1","2","3","4","5","6","7","8","9","10"};
    public BasicTableData data, oldData; //holds the current and original
    int nUndo=1;//number of possible undos
    int currentUndo=0; // keeps track of currently saved undos
    int availableUndo=0; //actual available saves for Undos
    public BasicTableData[] undoData=new BasicTableData[nUndo]; //used to undo the last edit
    JFrame parent;
    JTable table;
    ReadData readData; //class for reading data
    ListSelectionModel selectionModel; //the table selection model
    Action readDataAction,writeDataAction,addRowAction,delRowAction,
      addColAction,delColAction,saveAction,selectAllRowsAction,
      clearSelectedRowsAction, undoAction;
    JMenuBar menuBar;
    JMenu editMenu; 
    JMenu toolsMenu; 
    JPopupMenu popup;
    JToolBar toolBar;
    public CustomTableModel model; //the Table model
    boolean isTableChanged=false; //remember if table data are changed for saving
 

  
  public BasicTable(JFrame parent, String title) {
     super(title);
    this.parent=parent;
    data=new BasicTableData();
    readData=new ReadData(this);//used for reading table data from files
    createMenuBar();
    createToolBar();
    createPopup();
    Rectangle d=parent.getBounds();
    setBounds(d.x+d.width/4,d.y+d.height/4,400,500);
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
    menuBar.add(toolsMenu);
    setJMenuBar(menuBar);
  }/*end createMenuBar()*/
  
protected void createToolBar(){
    toolBar=new JToolBar();
    toolBar.add(addRowAction).setToolTipText("Add a row after 1st selection");
    toolBar.add(delRowAction).setToolTipText("Delete selected row");
    toolBar.add(addColAction).setToolTipText("Append a column");
    toolBar.add(delColAction).setToolTipText("Delete last column");
    toolBar.addSeparator();
    toolBar.add(saveAction).setToolTipText("Save the changes in this table only");
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
  *Set BasicTable column names
  */
  public void setColumnNames(String[] names){
       int nCols=names.length;
       columnNames=new String[nCols];
      for(int i=0;i<nCols;i++)columnNames[i]=names[i];
      data.setColumnNames(columnNames);
  }/*end method*/
  
  /**
  *Called after a column is added and need default name
  */
  public void addColumnNames(){
     int cols=model.getColumnCount();
      columnNames=new String[cols];
      for(int c=0;c<cols-1;c++)
       columnNames[c]=model.getColumnName(c);
     
       columnNames[cols-1]=columnNamePrefix+Integer.toString(cols-1);
      data.setColumnNames(columnNames);
      model.fireTableStructureChanged();   
  }
  
  /**
  *Set prefix for newly added column names
  */
  public void setColumnNamePrefix(String prefix){
      columnNamePrefix=prefix;
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
	 model.fireTableStructureChanged();   
	 if(currentUndo>=availableUndo)currentUndo--; //only this many available data objects
	}/*end method*/	
}/*end action*/

class SaveAction extends AbstractAction{
    public SaveAction(){
	super("Save");
	setEnabled(false);
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	saveTable();
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
    undoData[0]=(BasicTableData)data.clone();
    //System.out.println("clone rows cols "+undoData.rows+" "+undoData.cols);
}/*end method*/
  
/**
*make the table, called by MainWindow.onOpening()
*/
public void createTable(){	
	model=new CustomTableModel();
	table=new JTable(model);
	table.getModel().addTableModelListener(new TableListener());
	table.getTableHeader().setToolTipText("Edits don't take hold until you click on another cell.");
	table.setBounds(50,50,600,400);
	selectionModel=table.getSelectionModel();
	//table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	JScrollPane scroll=new JScrollPane(table);
	getContentPane().add(scroll,BorderLayout.CENTER);
	
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
      "Save changes in Table?",
       "Are You Sure?",JOptionPane.OK_CANCEL_OPTION,
       JOptionPane.WARNING_MESSAGE,tableIcon);
      switch(result){
	case JOptionPane.OK_OPTION:
	 try{
	  oldData=(BasicTableData)data.clone();//overwrite old data
	  }catch(java.lang.CloneNotSupportedException e){System.out.println("Error on save: "+e);}
	  isTableChanged=false; //so onClosing doesn't ask any more questions
	  //MainWindow.saveHabgenSettings(); //save but don't exit
	  System.out.println("BasicTable.saveTable() not working");
	  saveAction.setEnabled(false);
	 break;
	case JOptionPane.CANCEL_OPTION:   
	 return; //abort the save
	  }/*end switch*/ 	
    }/*end method*/
  
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
	   data.setColumnNames(columnNames);
	   return data.columnNames[col];}/*end try catch*/
	}/*end method*/	
    public boolean isCellEditable(int row,int col){return true;}
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
	addColumnNames();
	}/*end method()*/
    public void delCol(){
	data.delCol(); fireTableStructureChanged();
	}/*end method()*/
	    
}/*end CustomTableModel*/

  /**
   *Read a Data file into a table
   */
  public void readData(){
      JFileChooser chooser = new JFileChooser(new File("."));
    int state=chooser.showOpenDialog(null);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
	String dataFile=file.getPath();
	String[][] tempData=readData.read(dataFile);
	 if(tempData==null)return;
	 data.changeData(tempData);
	// data.setColumnNames(columnNames);
	 model.fireTableStructureChanged();
	 isTableChanged=true;
	}
    else if (state==JFileChooser.CANCEL_OPTION);
  
  }/*end method*/
  
    /**
   *Save a Table into a Data file
   */
  public void writeData(){
    JFileChooser chooser = new JFileChooser(new File("."));
    int state=chooser.showSaveDialog(null);
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
  
}/*end class*/
