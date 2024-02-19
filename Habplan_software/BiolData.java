import java.util.*;

/**
*Handles fields and methods for biological Type 1 data
*/
public class BiolData {
 double [] BofX; //biological data 
 static int [] regime; //regime that each element of BofX corresponds to
 
 /**
 *Returns the value of BofX for regime z or -1 if z is not valid
 *@return the value of BofX (the biological data) or -1 
 *@param  the regime of interest 
 */
 public double BofX(int z){
   int line=0;
  for (int row=1; row<regime.length; row++)
   if (regime[row]==z){line=row;break;}
  if (line!=0)return (BofX[line]); 
 return (-1.0);
 }/*end BofX*/ 
 
 /**
* Gives the regime thats held in the specified row of BofX[]
*@return the regime in the row specified or 0 if out of bounds
*/
public  int getSched(int row){
if (row>0 && row<regime.length) return(regime[row]);
return(0);
}/*end getSched()*/



/**
*Used to find the BofX value corresponding to specified rank for the
*currently valid regimes for the BIO component
*if rank=1 this gives the highest rank BofX of all valid schedules
*WARNING this relies on validScheds being set correctly for the polygon
*in FrameMenu.  Usually this is done by FrameMenu.randomSchedule.
*@param rank is the rank you're interested in, 1 means the biggest BofX
*@param validScheds are current valid schedules .
*@param nScheds is current number of valid schedules for this regime
*@return the BofX value at the requested rank is returned
*/
public double BofXByRank(int rank, int[] validScheds, int nScheds){
 if (nScheds<=0)return(0.);
 if (nScheds==1)return(BofX(validScheds[1]));
 
 Vector order=new Vector(nScheds+1);
 
 double bx=this.BofX(validScheds[1]);
  order.addElement(new Double(bx)); //get 1st" BofX in position 0
  //System.out.println("1 bx sched "+bx+" "+validScheds[1]);
 for (int i=2;i<=nScheds;i++){
  bx=this.BofX(validScheds[i]);
  //System.out.println("i bx sched"+i+" "+bx+" "+validScheds[i]);
  for (int j=1;j<=i;j++){
    if (j-1==order.size()){order.addElement(new Double(bx));break;}
    if (((Double)(order.elementAt(j-1))).doubleValue()<=bx){
       order.insertElementAt(new Double(bx),j-1);
      break;
    }/*end if*/
    
  }/*end for j*/  
 }/*end for i*/
  if (rank-1>=order.size())rank=order.size();
 return(((Double)order.elementAt(rank-1)).doubleValue());
}/*end BofXByRank()*/

   

} /*end  BiolData class*/
