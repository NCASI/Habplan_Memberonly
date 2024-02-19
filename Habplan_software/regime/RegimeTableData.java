/**
*class to hold Table data
*column names must be handled in the RegimeTable extension
*/
package regime;
import java.awt.*;

public class RegimeTableData implements java.io.Serializable, Cloneable{
    int rows=2;
    int cols=4; //rowData cols plus color col
    String[] columnNames={"Polygon","Regime","yr1","out1"};
    String[][] rowData={{"P1","R1","0","0"},{"P2","R1","0","0"}};
    String[][] oldData; //maintain a pointer to old data capability
    RegimeTableData clone; //override in extensions
    String objType="FLOW";  //FLOW, BIO2 or BLOCK for handling data types
   
  /**
 *clone the Table data object.  Used to restore old
 *Table data if user wants to abort changes
 */ 
 public Object clone() throws CloneNotSupportedException{
   RegimeTableData clone=new RegimeTableData();
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
     *The RegimeTableData extension calls this to set column names
     */
    public void setColumnNames(){
	columnNames=new String[cols];
	columnNames[0]="Polygon";
	columnNames[1]="Regime";
	columnNames[2]="Data"; //handles objType=="BIO2"

	if(objType.equals("FLOW")){
	    int yrs=(cols-2)/2;
	    for(int i=1;i<=yrs;i++){
		String yName="Yr"+i;
		String oName="Out"+i;
		columnNames[1+i]=yName;
		columnNames[1+(cols-2)/2+i]=oName;
	    }
	}if(objType.equals("BLOCK")){
	    columnNames[1]="Size";
	    for(int c=2;c<cols;c++){
		int j=c-1;
		columnNames[c]="Nabe"+j;
	    }
	}
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
      setColumnNames();
  }/*end method*/

     /**
     *Append to current rowData and update the table
     *
     **/
    public int appendData(String[][] data){
	if(data==null)return(0);
	if(rowData==null){changeData(data); return(0);}
	String[][] rowData2=appendByType(data);
	int row1=data.length+1; //set table to this row
	changeData(rowData2);
	return(row1);
    }
  
  
    /**
     *append a String matrix to rowData and return the result
     *the procedure depends on the objType
     *@param tempData - the data to be appended to rowData
     *@param objType - FLOW BIO2 or BLOCK
     *@return the appended data 
     */
    public String[][] appendByType(String[][] tempData){
		int row1=tempData.length;
		rows=row1+rowData.length;
		cols=Math.max(tempData[0].length,rowData[0].length);
		String[][] tempData2=new String[rows][cols];
		//add in the old data with current polygons removed
		//then add the new data
		if(objType.equals("BIO2")){
		for(int i=0;i<row1;i++)tempData2[i]=tempData[i];
		for(int i=row1;i<rows;i++)tempData2[i]=rowData[i-row1];
		}else if(objType.equals("FLOW")){
		    int yrs=(tempData[0].length-2)/2;
		    int yrs2=(cols-2)/2;
		    for(int i=0;i<row1;i++){//add old data w/o current polys
			for(int j=0;j<2+yrs;j++)
			    tempData2[i][j]=tempData[i][j];
			for(int j=2+yrs;j<2+2*yrs;j++)
			    tempData2[i][j+yrs2-yrs]=tempData[i][j];
		    }
		       yrs=(rowData[0].length-2)/2;
		    	for(int i=row1;i<rows;i++){//add new data
			for(int j=0;j<2+yrs;j++)
			    tempData2[i][j]=rowData[i-row1][j];
			for(int j=2+yrs;j<2+2*yrs;j++)
			    tempData2[i][j+yrs2-yrs]=rowData[i-row1][j];
		    }
		}else if(objType.equals("BLOCK")){
		for(int i=0;i<row1;i++)
		    for(int j=0;j<tempData[0].length;j++)
		    tempData2[i][j]=tempData[i][j];
		for(int i=row1;i<rows;i++)
		    for(int j=0;j<rowData[0].length;j++)
			tempData2[i][j]=rowData[i-row1][j];
		}else return(tempData); //doNothing
		return(tempData2);
    }

  /**
  *dump the data
  */
  public String[][] dumpData(){
      String[][] data=new String[rows][cols];
      for(int r=0;r<rows;r++)
       for(int c=0;c<cols;c++)
        data[r][c]=rowData[r][c];
     return data;
    }/*end method*/

 public void setObjType(String type){objType=type;}
    
}/*end RegimeTableData class*/
