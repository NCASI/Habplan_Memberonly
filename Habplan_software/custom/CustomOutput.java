/**
*A user editable class to control output formats
*/
import java.io.*;

class CustomOutput
    implements OutputInterface {

/**
*Controls the use of your custom writeSchedule() method
*@return false to use the default method for saving schedules
*@return true to use your custom method
*/
    public boolean isWriteSchedule(){return true;}
  
/**
*This method is called for each polygon for each save
*@param fout points to the saveSched file that Habplan opened
*@param nSaves is the number of schedules that have been saved
*@param schedule is the name of the schedule
*@param polyName is the polygon ID
*/      
    public  void writeSchedule (PrintWriter fout, int nSaves, String polyName, String schedule){
    String s;
    s=Integer.toString(nSaves)+", "+polyName+", "+schedule;
    try{
      fout.println(s);
       } catch (Exception e){
      System.out.println("Error in writeSchedule: "+e);}
    }/*end write() method*/

}
