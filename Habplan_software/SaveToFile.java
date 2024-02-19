
import java.io.*;


/**
*This is for saving something to a file
*Mainly I use it for saving the same stuff thats being graphed
*so I can later make a fancy publication quality graph
*@author Paul Van Deusen (NCASI)  2/98
*/
public  class SaveToFile {
   
String file; 
  
public static void main (String args[]) {
  
}/*end main*/


/**
*Append to the user specified file
*/
 public void append(String file, float y[]){
  
  RandomAccessFile fout=null;
   try {
    fout =  new RandomAccessFile(file,"rw");
    fout.seek(fout.length());
     for (int i=1;i<=y.length;i++){
      fout.writeBytes(String.valueOf(y[i]));
      fout.writeBytes(" ");
      }
      fout.writeBytes("\n");
     //parent.console.println("just did a saveSched");
 }/*end try*/
  catch(Exception e){
   System.out.println("Error saving to file " + file);
   System.out.println(e.toString());
  } /*end catch*/
   finally { try { if (fout != null) fout.close(); } catch (IOException e) {} }      
 } /*end append(y)*/  

/** 
*delete this file
*/
public void delete(String file){

      File file2=new File(file);
      if (file2.exists()){
       file2.delete();
       //parent.console.println("\nDeleted " + text.getText());
      }/*end if*/
}/*end delete()*/


} /*end SaveToFile class*/


 