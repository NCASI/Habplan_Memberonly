/**
*class to hold Habgen Table data
*column names must be handled in the BasicTable extension
*/
package table;

public class BasicTableData implements java.io.Serializable, Cloneable{
    int rows=2;
    int cols=3; //n of time periods, is 1 less than columns in table
    String[] columnNames={"Polygon","Regime","Rank"};
    String[][] rowData={{"1","0","0","0"},{"2","0","0","0"}};
    String[][] oldData; //maintain a pointer to old data capability
    BasicTableData clone; //override in extensions
   
 /**
 *clone the Table data object.  Used to restore old
 *Table data if user wants to abort changes
 */ 
 public Object clone() throws CloneNotSupportedException{
   BasicTableData clone=new BasicTableData();
   clone.rows=this.rows;
   clone.cols=this.cols;
   clone.rowData=new String[rows][cols];
   clone.columnNames=new String[cols];
   for(int r=0;r<rows;r++){
       for (int c=0; c<cols; c++){
	clone.rowData[r][c]=this.rowData[r][c];
	clone.columnNames[c]=this.columnNames[c];
   }}/*end for*/
  return clone; 
 }/*end clone()*/
    
  
  /**
  *add a row of data
  *@param the first selected row
  */  
  public void addRow(int selectedRow){
      rows++;
      oldData=rowData;
      rowData=new String[rows][cols];
      int after=0; //change to 1 at the insert point
      for(int r=0;r<rows;r++){
       for (int c=0; c<cols; c++){
	 if(r!=selectedRow+1)  
	  rowData[r][c]=oldData[r-after][c];
	else{after=1; rowData[r][c]="";}/*end if else*/
       }}/*end for*/	
       oldData=null;
  } /*end method*/  
  /**
  *delete a row of data
  *@return the current selected row, since deletion can change it
  */
  public int delRow(int selectedRow){
      rows--;
      if (rows<1){rows=1;return 0;}
      if(selectedRow>rows)selectedRow=rows-1; 
      oldData=rowData;
      rowData=new String[rows][cols];
      int after=0; //change to 1 at the insert point
       for(int r=0;r<rows+1;r++){
       for (int c=0; c<cols; c++){
	 if(r!=selectedRow)rowData[r-after][c]=oldData[r][c];
	 else after=1;
       }}/*end for*/
       oldData=null;
      return selectedRow;
  } /*end method*/  
  
  /**
  *add a Column of data
  */  
  public void addCol(){
      cols++;
      oldData=rowData;
      rowData=new String[rows][cols]; 
      for(int r=0;r<rows;r++){
       for (int c=0; c<cols; c++){
	if (c<cols-1) rowData[r][c]=oldData[r][c];
	else rowData[r][c]="";
       }}/*end for*/
       oldData=null;
  } /*end method*/  
  
  /**
  *delete a Column of data
  */
  public void delCol(){
      cols--;
      if (cols<1)cols=1;
  }/*end method*/
  
  /**
  *The BasicTableData extension calls this to set column names
  */
  public void setColumnNames(String[] names){
       columnNames=new String[cols];
      for(int i=0;i<cols;i++)
       if(i<names.length)columnNames[i]=names[i];
       else columnNames[i]=Integer.toString(i);
    }/*end method*/

/**
*set column count
*/    
public void setCols(int cnum){cols=cnum;}    
  
  /**
  *substitute new data into the table
  */
  public void changeData(String[][] data){
      if(data==null)return;
      rows=data.length;
      cols=data[0].length;
      rowData=new String[rows][cols];
      for(int r=0;r<rows;r++)
       for(int c=0;c<cols;c++)
	rowData[r][c]=data[r][c];
  }/*end method*/
  
  /**
  *dump the data
  */
  protected String[][] dumpData(){
      String[][] data=new String[rows][cols];
      for(int r=0;r<rows;r++)
       for(int c=0;c<cols;c++)
        data[r][c]=rowData[r][c];
     return data;
    }/*end method*/
    
}/*end RegimeTableData class*/
