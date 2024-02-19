/*
 * Basic Table for manipulating GIS Colors
 * @author  Paul Van Deusen
 * @version 1.0
 */
package gis;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

public class BasicTable extends JFrame{
    BasicTable thisTable; //provides a reference for the progressMeter
    //override tableIcon and columnNames  in extensions.
    protected ImageIcon tableIcon; 
    Color selectedColor; //color selected by user
    public String dataFile=""; //name of last file read or written
    protected String columnNamePrefix="Var"; //prefix of column name when user adds a column
    protected String[] columnNames={"Color","Polygon","var1","var2","var3","var4","var5","var6","var7","var8"};
    public BasicTableData data, oldData; //holds the current and original
    SelectionTool colorTableSelectionTool;//for selecting rows from this table
    ColumnNamesTool columnNamesTool; //for changing some of the columnNames
    int nUndo=2;//number of possible undos
    int currentUndo=0; // keeps track of currently saved undos
    int availableUndo=0; //actual available saves for Undos
    BasicTableData[] undoData=new BasicTableData[nUndo]; //used to undo the last edit
    public JTable table;
    ReadData readData; //class for reading data
    ReadDbf readDbf;   //read dbf file class
    ListSelectionModel selectionModel; //the table selection model
    Action readDataAction,readDbfAction,writeDataAction,addRowAction,delRowAction,
      addColAction,delColAction,selectAllRowsAction,
      clearSelectedRowsAction, undoAction, colorAction;
    JMenuBar menuBar;
    JMenu editMenu; 
    JMenu toolsMenu; 
    JPopupMenu popup;
    JFrame parent; //the GISViewer

    public JToolBar toolBar;
     CustomTableModel model; //the Table model
    boolean isTableChanged=false; //remember if table data are changed for saving
    static String ExampleDir="example";
 
    public BasicTable(JFrame parent, String title, String ExampleDir) {
        this(parent,title);
	this.ExampleDir=ExampleDir; //so we can open to the example dir
    }

    public BasicTable(JFrame parent, String title) {
     super(title); 
     thisTable=this; 
     this.parent=parent;  
    data=new BasicTableData();
    readData=new ReadData(this);//used for reading table data from files
    readDbf=new ReadDbf(this);//read dbf file
    createMenuBar();
    createToolBar();
    createPopup();
    createTable();
     colorTableSelectionTool=new SelectionTool(this,"Select: "+title);
     columnNamesTool=new ColumnNamesTool(this,"Change Names: "+title);
    setBounds(50,75,250,250);
     setVisible(false);
     setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      makeRelatedWindowsInvisible();
	      dispose();
	  }
	 });   
  }/*end constructor*/

 /**
     *Close all related Tool windows when the main viewer isn't
     *visible
     */
    public void  makeRelatedWindowsInvisible(){
	colorTableSelectionTool.setVisible(false);
	columnNamesTool.setVisible(false);
    }
  
  private void createMenuBar(){
      //make the actions here too
      colorAction=new ColorAction();
    addRowAction=new AddRowAction();
    delRowAction=new DelRowAction();
    addColAction=new AddColAction();
    delColAction=new DelColAction();
    undoAction=new UndoAction();
    selectAllRowsAction=new SelectAllRowsAction();
    clearSelectedRowsAction=new ClearSelectedRowsAction();
    readDataAction=new ReadDataAction();
    readDbfAction=new ReadDbfAction();
    writeDataAction=new WriteDataAction();
     //make the menus
    menuBar=new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.add(readDataAction).setToolTipText("Read table data from text file");
    fileMenu.add(readDbfAction).setToolTipText("Read table data from dbf file");
    fileMenu.add(writeDataAction).setToolTipText("Save table data to text file");
    menuBar.add(fileMenu);
    editMenu = new JMenu("Edit");
    editMenu.add(undoAction).setToolTipText("Undo previous changes");
    editMenu.add(selectAllRowsAction).setToolTipText("Select all rows");
    editMenu.add(clearSelectedRowsAction).setToolTipText("UnSelect all rows");
    editMenu.add(addRowAction).setToolTipText("Add a row after 1st selection");
    editMenu.add(delRowAction).setToolTipText("Delete selected row");
    editMenu.add(addColAction).setToolTipText("Append a column");
    editMenu.add(delColAction).setToolTipText("Delete last column");
    menuBar.add(editMenu);
    toolsMenu = new JMenu("Tools");
    toolsMenu.add(colorAction).setToolTipText("Color Selected Rows");
    menuBar.add(toolsMenu);
    setJMenuBar(menuBar);
  }/*end createMenuBar()*/
  
protected void createToolBar(){
    toolBar=new JToolBar();
    toolBar.add(colorAction).setToolTipText("Color Selected Rows");
   
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

  
 class ColorAction extends AbstractAction{
    public ColorAction(){
      super("Color");
    }
    public void actionPerformed(ActionEvent e){
       selectedColor=JColorChooser.showDialog(thisTable,"Pick a color",Color.white);
       if (selectedColor != null){
      	Thread thread=new RunThread(); 
	thread.start();// start colorSelectedRows()
       JProgressMeter progress=new JProgressMeter(thisTable,thread,getTitle());
	progress.work();
       }
    }/*end actionPerformed()*/
  }/*end Action*/

 class ReadDbfAction extends AbstractAction{
    public ReadDbfAction(){
      super("ReadDbf");
    }
    public void actionPerformed(ActionEvent e){
      readDbf();
    }/*end actionPerformed()*/
  }/*end Action*/

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

    /**
     *Can call from other classes, like RegimeEditor
     **/
    public void clearSelections(){
	selectionModel.clearSelection();
    }

class UndoAction extends AbstractAction{
    public UndoAction(){
	super("Undo");
	}/*end method*/
    public void actionPerformed(ActionEvent e){
	 if(availableUndo>0)data=undoData[currentUndo++];
	 model.fireTableStructureChanged();   
	 TableColumn colorColumn=table.getColumn("Color");
	 colorColumn.setCellRenderer(new ColorRenderer());
	 if(currentUndo>=availableUndo)currentUndo--; //only this many available data objects
	}/*end method*/	
}/*end action*/


/**
*save old table data for an undo
*/
public void saveUndoData()
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
	TableColumn colorColumn=table.getColumn("Color");
	colorColumn.setCellRenderer(new ColorRenderer());
	table.getModel().addTableModelListener(new TableListener());
	table.getTableHeader().setToolTipText("Edits don't take hold until you click on another cell.");
	selectionModel=table.getSelectionModel();
	//table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	JScrollPane scroll=new JScrollPane(table);
	getContentPane().add(scroll,BorderLayout.CENTER);
		//add horizontal scrolling for big tables
	 if(model.getColumnCount()>10)
	     table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//determine which rows are selected
	 selectionModel.addListSelectionListener(
      new ListSelectionListener(){
	public void valueChanged(ListSelectionEvent e){
	 int firstSelectedRow=selectionModel.getMinSelectionIndex();
	 int lastSelectedRow=selectionModel.getMaxSelectionIndex();
	//does nothing now, might need it later
	}/*end valueChanged method*/
    });
	
    }
 
/**
*Listener to know when a change occured 
*/    
public class TableListener implements TableModelListener{
     
    public void tableChanged(TableModelEvent e){
	isTableChanged=true;
		//add horizontal scrolling for big tables
	 if(model.getColumnCount()>8 )
	     table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}  /*end method*/  
    }
/**
* inner class for custom table model
*/    
 class CustomTableModel extends AbstractTableModel{
    public int getRowCount(){return data.rows;}
    public int getColumnCount(){return data.cols;}
    
    public Object getValueAt(int row,int col){
	if(col==0)return data.color[row];
	else return data.rowData[row][col-1];}
    public String getColumnName(int col){try{return data.columnNames[col];
	   }catch(Exception e){JOptionPane.showMessageDialog(menuBar,"Resetting Column Names.","Column error",JOptionPane.OK_OPTION,tableIcon);
	   data.setColumnNames(columnNames);
	   return data.columnNames[col];}/*end try catch*/
	}/*end method*/	
    public boolean isCellEditable(int row,int col){return false;}
    public void setValueAt(Object value,int row,int col){
	//try{saveUndoData();} catch(Exception e2){}
	if(col==0)data.color[row]=(Color)value;
	else data.rowData[row][col-1]=(String)value;
	//fireTableCellUpdated(row,col);
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
	 model.fireTableStructureChanged(); 
        TableColumn colorColumn=table.getColumn("Color");
        colorColumn.setCellRenderer(new ColorRenderer());
	}/*end method()*/
    public void delCol(){
	data.delCol(); fireTableStructureChanged();
	 TableColumn colorColumn=table.getColumn("Color");
        colorColumn.setCellRenderer(new ColorRenderer());
	}/*end method()*/
	    
}/*end CustomTableModel*/

 /**
*@return the index of the column called name in table (-1 means not there)
*/
public int getColumnByName(String name){
    int col=-1;
    for(int c=0;c<model.getColumnCount();c++)
     if(model.getColumnName(c).toLowerCase().equals(name.toLowerCase())){
      col=c; break;  
     }/*fi*/
  return col; 
}/*end method*/

    /**
     *fire structure changed event.  can be called from outside this package
     */
     public void fireStructureChangeEvent(){
	model.fireTableStructureChanged();
	TableColumn colorColumn=table.getColumn("Color");
        colorColumn.setCellRenderer(new ColorRenderer());
     }/*end  method*/

  
    /**
     *read polygon data and set up default color Table
     **/
    public void readTheData(String dataFile){
	String[][] tempData;
	if(dataFile.endsWith("dbf")){
	     tempData=readDbf.read(dataFile);//has names in row 0
	   }else{
	       tempData=readData.read(dataFile);
	   }

	    if(tempData==null)return;
	    Color[] color=new Color[tempData.length];
	    for(int i=0;i<color.length;i++)color[i]=Color.green;
            columnNames=new String[tempData[0].length+1];
	    for(int i=1;i<columnNames.length;i++)columnNames[i]=tempData[0][i-1];
            columnNames[0]="Color";
	    data.changeData(color,tempData);
	    data.delRow(0); //get rid of row 0 with the names in it
	    data.setColumnNames(columnNames);
	    model.fireTableStructureChanged();
	    TableColumn colorColumn=table.getColumn("Color");
	    colorColumn.setCellRenderer(new ColorRenderer());
	     //add horizontal scrolling for big tables
	     if(model.getColumnCount()>10)
	      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    isTableChanged=true;
    }
   
     /**
     *Read a dbf file into a table
     */
    public void readDbf(){
	JFileChooser chooser = new JFileChooser(new File(ExampleDir));
        HBPFilter filter = new HBPFilter("dbf","DbfFiles"); //show .shp files
        chooser.setFileFilter(filter);
	int state=chooser.showOpenDialog(null);
	File file=chooser.getSelectedFile();
	if (file != null && state == JFileChooser.APPROVE_OPTION){
	    dataFile=file.getPath();
	    readTheData(dataFile);
	}
	else if (state==JFileChooser.CANCEL_OPTION);
    }/*end method*/

   
  /**
   *Read a text Data file into a table
   */
  public void readData(){
    JFileChooser chooser = new JFileChooser(new File(".//example"));
    int state=chooser.showOpenDialog(null);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
	 dataFile=file.getPath();
	 readTheData(dataFile);
	}
    else if (state==JFileChooser.CANCEL_OPTION);
  
  }/*end method*/

 /**
     *  Scrolls the row (rowIndex) so that it is visible within the
     * viewport.
     **/
    public void scrollToVisible(int rowIndex) {
       
        JViewport viewport = (JViewport)table.getParent();
	int vColIndex=0; //base view on first cell in row
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
    
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
    
        // Translate the cell location so that it is relative
        // to the view, assume northwest corner is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);
    
        // Scroll the area into view and select row to highlight it
        viewport.scrollRectToVisible(rect);
	table.setRowSelectionInterval(rowIndex,rowIndex);
    }

  
    /**
   *Save a Table into a Data file
   */
  public void writeData(){
    JFileChooser chooser = new JFileChooser(new File("..//example"));
    int state=chooser.showSaveDialog(null);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
	dataFile=file.getPath();
	int rows=model.getRowCount();
	int cols=model.getColumnCount()-1; //dont save colors
	String[][] rowData;
     int result=JOptionPane.showConfirmDialog(menuBar,
       "Put Column Names in Row 1","Column Names Question",JOptionPane.YES_NO_CANCEL_OPTION,
       JOptionPane.QUESTION_MESSAGE);
       if(result==JOptionPane.CANCEL_OPTION)return;
       if(result==JOptionPane.YES_OPTION){ 
	    rowData=new String[rows+1][cols];
	   for(int c=0;c<cols;c++) rowData[0][c]=(String)model.getColumnName(c);
	   for(int r=1;r<rows+1;r++)rowData[r]=data.rowData[r-1];
       }
       else{ rowData=new String[rows][cols]; 
       	      for(int r=0;r<rows;r++)rowData[r]=data.rowData[r]; 
	  }/*end if YES_OPTION*/         
	 readData.write(dataFile,rowData);    
	}/*end if APPROVE_OPTION*/
    else if (state==JFileChooser.CANCEL_OPTION);
  }/*end method*/

    public void colorSelectedRows(){
	try{saveUndoData();} catch(Exception e2){}
       int[] rows=getSelectedTableRows();
       if(rows==null)return;
       for(int i=0;i<rows.length;i++){
	   int r=rows[i];
	   model.setValueAt(selectedColor,r,0);
       }/*end for i*/
       model.fireTableDataChanged();
       	Dimension dim=parent.getSize();
	if(Math.random()<0.5)dim.width+=1; else dim.width-=1;
	parent.setSize(dim);//the resize makes it redraw
    }/*end method*/

/**
*return a vector of selected row indices
*/  
 public int[] getSelectedTableRows(){
    int first=selectionModel.getMinSelectionIndex();
    int last=selectionModel.getMaxSelectionIndex();
    if(first<0)return null;
    int count=0; //count the n of selections in first pass
    for(int i=first;i<=last;i++)
     if(selectionModel.isSelectedIndex(i))count++;
    
     int[] selections=new int[count];
     int j=0; //index into selections vector
    for(int i=first;i<=last;i++)//get selections in second pass
     if(selectionModel.isSelectedIndex(i))selections[j++]=i;
    return selections;
}/*end method*/

 /**
     *inner class to do the time consuming coloring
     * operation on a separate thread. 
     */  
    class RunThread extends Thread{

	public RunThread(){
	   
	}/*end constructor*/
	public void run(){
	    colorSelectedRows();
		return;
	}/*end method*/  
    }/*end inner Thread class*/

   
  
}/*end class*/
