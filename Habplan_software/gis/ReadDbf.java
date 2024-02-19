package gis;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.svcon.jdbf.*;
/**
*ReadDbf  will read dbf files using svcon free dbfReader class.
*@author Paul Van Deusen, NCASI 10/2004
*/
public class ReadDbf {
  protected static String[][] data; //hold the input data
  protected static int nRecords=0;     // n of lines in file;
  protected static int nFields=0; // max n of elements per line
  String outSeparator="\t"; //separator for variables when writing to file
  BasicTable parentTable;
    
 public ReadDbf (BasicTable parentTable) {
     this.parentTable=parentTable;
     }

/**
*read data from a dbf file
*/

public String[][] read(String fileName){ 
    DBFReader in=null;
    JDBField field = null;  //holds a field
    Vector record=new Vector(500,500); //holds all records
    Object[] aRecord=null;   //holds one record
 
    try{	 
	in = new DBFReader(fileName);
	nRecords=0; //n of records in the file
	 int count=0;
	 while(in.hasNextRecord()){
	     count++;
	     try{
		 record.add(in.nextRecord());
	     }catch(Exception e2){
		 System.out.println("Error reading dbf file: "+e2);
		 break;
	     }
	   
	 }
	

	nFields=in.getFieldCount();
	nRecords=record.size();
	data=new String[nRecords+1][nFields]; //put var names in first row
	//System.out.println("field count="+nFields+"  records="+nRecords);

	//put the field names into the first row of data matrix
	for(int j=0;j<nFields;j++){
	    field=in.getField(j);
	    data[0][j]=field.getName();
	}

	//now put the records into a String matrix in rows 1 to n
	for(int i=0;i<nRecords;i++){
	    aRecord=(Object[])record.get(i);
	    for(int j=0;j<nFields;j++){
		data[i+1][j]=aRecord[j].toString();
	    } 
	}
    } /*end try*/
   catch (JDBFException e) {
     JOptionPane.showMessageDialog(parentTable,"DBF Error reading "+e,
	      "Reading Data Error",JOptionPane.WARNING_MESSAGE, parentTable.tableIcon);
     }
finally {  try { if (in != null) {in.close();} } catch (JDBFException e) {} }
  
  return data;
 }/*end readData()*/
 
 /**
 *Print out lines n1 through n2 of the data matrix
 */
public void printData(int n1, int n2){
    if(n1<0)System.out.println("can't print a line less than 0");
    if(n2>nRecords)System.out.println("There are only "+nRecords+" of data starting at line 0!");
    
  for (int i=n1;i<n2;i++){
      for (int j=0;j<data[i].length;j++){
	  System.out.print(data[i][j]+" ");
	  }
	 System.out.println(" "); //goto newline 
  }  
    
}/*end printData()*/


    
}/*end class*/
