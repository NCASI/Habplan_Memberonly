/**
*class to hold Habgen Table data
*column names must be handled in the BasicTable extension
*/
package gis;
import java.awt.*;

public class BasicTableData implements java.io.Serializable, Cloneable{
    public int rows=2;
    int cols=2; //rowData cols plus color col
    String[] columnNames={"Color","Polygon"};
    public Color[] color={Color.red,Color.green};
    String[][] rowData={{"1"},{"2"}};
    String[][] oldData; //maintain a pointer to old data capability
    Color[] oldColor;
    BasicTableData clone; //override in extensions
   
 /**
 *clone the Table data object.  Used to restore old
 *Table data if user wants to abort changes
 */ 
 public Object clone() throws CloneNotSupportedException{
   BasicTableData clone=new BasicTableData();
   clone.rows=this.rows;
   clone.cols=this.cols; 
   clone.color=new Color[rows];
   clone.rowData=new String[rows][cols-1];
   clone.columnNames=new String[cols];
   clone.columnNames[0]="Color";  //one more col in columnNames than in rowData
   for(int r=0;r<rows;r++){
       clone.color[r]=this.color[r];
       for (int c=0; c<cols-1; c++){
	clone.rowData[r][c]=this.rowData[r][c];
	clone.columnNames[c+1]=this.columnNames[c+1];
   }}/*end for*/
  return clone; 
 }/*end clone()*/
    
  
  /**
  *add a row of data
  *@param the first selected row
  */  
  public void addRow(int selectedRow){
      rows++;
      cols--; //temp change for colors
      oldData=rowData;
      rowData=new String[rows][cols];
      oldColor=color;
      color=new Color[rows];
      int after=0; //change to 1 at the insert point
      for(int r=0;r<rows;r++){
       for (int c=0; c<cols; c++){
	   if(r!=selectedRow+1){  
	  rowData[r][c]=oldData[r-after][c];
	  color[r]=oldColor[r-after];
	   }
	else{
	    after=1;
	    rowData[r][c]="";
	    color[r]=Color.white;
	}/*end if else*/
       }}/*end for*/	
       oldData=null;
       cols++;//add back color column
  } /*end method*/  
  /**
  *delete a row of data
  *@return the current selected row, since deletion can change it
  */
  public int delRow(int selectedRow){
      rows--;
      cols--; //temp change to handle colors
      if (rows<1){rows=1;return 0;}
      if(selectedRow>rows)selectedRow=rows-1; 
      oldData=rowData;
      rowData=new String[rows][cols];
      oldColor=color;
      color=new Color[rows];
      int after=0; //change to 1 at the insert point
       for(int r=0;r<rows+1;r++){
       for (int c=0; c<cols; c++){
	 if(r!=selectedRow){
	     rowData[r-after][c]=oldData[r][c];
	     color[r-after]=oldColor[r];
	 }
	 else after=1;
       }}/*end for*/
       oldData=null;
       oldColor=null;
       cols++; //add 1 for color column
      return selectedRow;
  } /*end method*/  
  
  /**
  *add a Column of data.  Color column stays the same
  */  
  public void addCol(){
      oldData=rowData;
      rowData=new String[rows][cols]; 
      for(int r=0;r<rows;r++){
       for (int c=0; c<cols; c++){
	if (c<cols-1) rowData[r][c]=oldData[r][c];
	else rowData[r][c]="";
       }}/*end for*/
       oldData=null;
       cols++;
  } /*end method*/  
  
  /**
  *delete a Column of data
  */
  public void delCol(){
      cols--;
      if (cols<2)cols=2;
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
  *Retrieve the array of column names
  */
  public String[] getColumnNames(){
      String[] names=new String[cols];
      for(int i=0;i<cols;i++)
	names[i]=columnNames[i];
     return names;	
    }/*end method*/

/**
*set column count
*/    
public void setCols(int cnum){cols=cnum;}    
 
 
  /**
  *substitute new data into the table
  */
  public void changeData(Color[] newColor, String[][] data){
      if(data==null|color==null)return;
      rows=data.length;
      cols=data[0].length;
      rowData=new String[rows][cols];
      color=new Color[rows];
      for(int r=0;r<rows;r++){
	  color[r]= newColor[r];
       for(int c=0;c<cols;c++)
	rowData[r][c]=data[r][c];
      }/*end for r*/
      cols++; //add 1 for the color column
  }/*end method*/
  
  /**
  *dump the rowData. This is a copy, not just a pointer
  */
  public String[][] dumpData(){
      String[][] data=new String[rows][cols-1];
      for(int r=0;r<rows;r++)
       for(int c=0;c<cols-1;c++)
        data[r][c]=rowData[r][c];
     return data;
    }/*end method*/

    /**
     *get the color vector
     *@return the color vector
     */
    public Color[] getColor(){return color;}

    /**
     *get the rowData matrix
     *@return rowData
     */
    public String[][] getRowData(){return rowData;}
    
}/*end ColorTableData class*/
