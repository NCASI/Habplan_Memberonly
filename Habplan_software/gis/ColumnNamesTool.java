/**
*class to allow User to Change Names in Tables
*@author Paul Van Deusen 12/2000
*/
package gis;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ColumnNamesTool extends JFrame{
    BasicTable parentTable;
    
    boolean isListening=false; //is this a table listener
    JPanel tool; //this contains the textfields etc
    VariableRow[] variableRow; //defined as inner class
    String[] columnNames;

    Action showAction, setNamesAction, row1NamesAction;
    JToolBar toolBar;
    protected int rows=0,cols=0; //n of rows and cols in parent table
    
    public ColumnNamesTool(BasicTable parentTable, String title){
     super(title);
     getContentPane().setLayout(new BorderLayout());
     this.parentTable=parentTable;
     setVisible(false);
     addToMenus();//add actions to parentTable menus
     createToolBar();
   }/*end constructor*/
  
   
 protected void createToolBar(){
    toolBar=new JToolBar();
    setNamesAction=new SetNamesAction();
    toolBar.add(setNamesAction).setToolTipText("Reset the column names");
    toolBar.addSeparator();
    row1NamesAction=new Row1NamesAction();
    toolBar.add(row1NamesAction).setToolTipText("Set column names to Row 1 Values");
    getContentPane().add(toolBar,BorderLayout.SOUTH);
    toolBar.addSeparator();
   
    }/*end method*/    

/**
*add actions to parent Table popup menu
*/   
   protected void addToMenus(){
    showAction=new ShowAction();   
    parentTable.popup.add(showAction).setToolTipText("Toggle ColumnNames Tool");
    parentTable.toolsMenu.add(showAction).setToolTipText("Toggle ColumnNamesTool");
  }/*end method*/
       
  class ShowAction extends AbstractAction{
    public ShowAction(){
      super("ColumnNamesTool");
    }
    public void actionPerformed(ActionEvent e){
      if(!isListening){//add listener only on first opening
	reConstruct();//construct the tool when listener is added
        parentTable.table.getModel().addTableModelListener(new TableListener());
	isListening=true;
      }/*end if listen*/
      setVisible(!isVisible());
    }/*end actionPerformed()*/
  }/*end Action*/
  
   class SetNamesAction extends AbstractAction{
    public SetNamesAction(){
      super("SetNames");
    }
    public void actionPerformed(ActionEvent e){
      cols=parentTable.model.getColumnCount(); 
      if(cols<=2)return; //don't change names of Color or Polygon column
      columnNames=new String[cols];
      columnNames[0]="Color";
      columnNames[1]="Polygon";
      for(int c=2;c<cols;c++)
      columnNames[c]=variableRow[c].name.getText().trim();
      parentTable.setColumnNames(columnNames);
      parentTable.data.setColumnNames(columnNames);
      parentTable.model.fireTableStructureChanged();
       TableColumn colorColumn=parentTable.table.getColumn("Color");
	colorColumn.setCellRenderer(new ColorRenderer());
    }/*end actionPerformed()*/
  }/*end Action*/
  
   class Row1NamesAction extends AbstractAction{
    public Row1NamesAction(){
      super("Row1Names");
    }
    public void actionPerformed(ActionEvent e){
      cols=parentTable.model.getColumnCount();
      if(cols<=2)return; //don't change names of Color or Polygon column
      columnNames=new String[cols];
      columnNames[0]="Color";
      columnNames[1]="Polygon";
      for(int c=2;c<cols;c++)
      columnNames[c]=(String)parentTable.model.getValueAt(0,c);
      parentTable.setColumnNames(columnNames);
      parentTable.data.setColumnNames(columnNames);
      parentTable.model.fireTableStructureChanged();
      TableColumn colorColumn=parentTable.table.getColumn("Color");
	colorColumn.setCellRenderer(new ColorRenderer());
    }/*end actionPerformed()*/
  }/*end Action*/
  
/**
  *reConstruct the tool to conform to the current table configuration
  */
  protected void reConstruct(){
    if (cols==parentTable.model.getColumnCount())return;  //no need to redo
    else cols=parentTable.model.getColumnCount();
    Container con=getContentPane();
    if(isListening)con.remove(tool);//the listener indicates that its not the first construction
    tool=new JPanel(new GridLayout(cols+1,1));
    JPanel header=new JPanel(new GridLayout(1,2));
    variableRow=new VariableRow[cols];
    JLabel column, name; //headers
    column=new JLabel("Column",JLabel.CENTER);
    name=new JLabel("Variable Name",JLabel.CENTER);
    
    header.add(column);
    header.add(name);
    
     tool.add(header);
    for (int i=0;i<cols;i++){
	variableRow[i]=new VariableRow(i);
	tool.add(variableRow[i]);
    }/*end for i*/
    con.add(tool,BorderLayout.CENTER);
    pack();
    
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
  *class to make a single row in the selection panel
  *constructor needs a column name
  */
  protected class VariableRow extends JPanel{
      JTextField name;
      JLabel columnID;
      
      protected String columnName;   
      
      public VariableRow(int col){
       columnID=new JLabel(Integer.toString(col), JLabel.CENTER);
       columnName=parentTable.model.getColumnName(col); 
       name=new JTextField(columnName);
       name.setToolTipText("Enter the name you want");  
       name.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
       name.setBackground(Color.white); 
      
       setLayout(new GridLayout(1,2));
       add(columnID);
       add(name);    
      }/*end constructor*/
      
  }/*end inner class*/
  
  
  
 



}/*end class*/
