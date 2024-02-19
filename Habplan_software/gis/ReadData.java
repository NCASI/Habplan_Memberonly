package gis;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
/**
*ReadData class will read ascii files. 11/2000
*@author Paul Van Deusen, NCASI
*/
public class ReadData{
  protected static String[][] data; //hold the input data
  protected static int nLines=0;     // n of lines in file;
  protected static int nCols=0; // max n of elements per line
  String outSeparator="\t"; //separator for variables when writing to file
  BasicTable parentTable;
    
 public ReadData(BasicTable parentTable) {
     this.parentTable=parentTable;
     }

/**
*the variable separator used when writing to a file
*/     
protected void setOutSeparator(String sep){outSeparator=sep;}
 
/**
*write data to a file
*assuming the data matrix is square,
* i.e. all rows must have same n of cols
*/ 
public void write(String fileName, String[][] data){
 BufferedWriter out=null;
 int rows=data.length;
 int cols=data[0].length;
 try{
    out = new BufferedWriter(new FileWriter(fileName));
    for(int i=0;i<rows;i++){
	for (int j=0;j<cols;j++){
	  if(data[i][j]==null)data[i][j]=" ";  
	   out.write(data[i][j]);
	   if(j<cols-1)out.write(outSeparator);
	}/*end for j*/    
	out.newLine();
    }/*end for i*/
     
 } catch (Exception e) {
	   JOptionPane.showMessageDialog(parentTable,"Error writing to "+fileName,
	      "Write Data Error",JOptionPane.WARNING_MESSAGE, parentTable.tableIcon);
     }
finally {  try { if (out != null) {out.close();} } catch (IOException e) {} }  
}/*end method*/
    
/**
*read data from a file
*/

public String[][] read(String fileName){ 

String delimiters="\t\n\r,;";   
  
  StringTokenizer token;
  BufferedReader in=null;
  String line=" ";
  nCols=0;

  for (int pass=1;pass<=2;pass++){ //make 2 passes over the data
  
   if (pass==2)data=new String[nLines][nCols];   
   nLines=0;
  try{	 
      in = new BufferedReader(new FileReader(fileName),1024);
      
    while ((line=in.readLine())!=null){ 
    
     if (pass==1 & nLines==0 & line.indexOf(" ")!=-1){
       int result=JOptionPane.showConfirmDialog(parentTable.menuBar,
        "Are Columns space delimited?","Variable Delimiter Question",JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);
        if(result==JOptionPane.CANCEL_OPTION)return null;
        if(result==JOptionPane.YES_OPTION)delimiters=" \t\n\r,;"; //add a space    
      }/*fi pass==1*/
    
      token=new StringTokenizer(line,delimiters); //all legit delimeters
      int n=token.countTokens(); //n of tokens on this line
      if (pass==1)nCols=(nCols>n)?nCols:n; //get max n of columns
     if (pass==2){ 
      for(int i=0;i<n;i++)data[nLines][i]=token.nextToken();
     }/*end if pass=2*/
     nLines++;
    }/*end of while loop*/
} /*end try*/
   catch (Exception e) {
     JOptionPane.showMessageDialog(parentTable,"Error reading "+e,
	      "Reading Data Error",JOptionPane.WARNING_MESSAGE, parentTable.tableIcon);
     }
finally {  try { if (in != null) {in.close();} } catch (IOException e) {} }
  
  }/*end pass loop*/

  return data;
 }/*end readData()*/
 
 /**
 *Print out lines n1 through n2 of the data matrix
 */
public void printData(int n1, int n2){
    if(n1<0)System.out.println("can't print a line less than 0");
    if(n2>nLines)System.out.println("There are only "+nLines+" of data starting at line 0!");
    
  for (int i=n1;i<n2;i++){
      for (int j=0;j<data[i].length;j++){
	  System.out.print(data[i][j]+" ");
	  }
	 System.out.println(" "); //goto newline 
  }  
    
}/*end printData()*/


    
}/*end class*/
