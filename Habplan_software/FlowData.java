
/**
*This  class holds data associated with the Flow components
*and the subcomponents that need to know the year when
*flow output occurs.  Such subcomponents include Block
*and ccForm
*/

 public class FlowData {
// basic stuff for polygons, e.g. stands         
public int [][] flow;  //flow[i][1] contains yr1 for option i, flow[i][2] is yr2 or flow 1
		      //the remaining columns have corresponding flows
/**
*@return The row in the flow matrix containing data
*for the specified regime.
*the regime id is stored in column 0 of each row
*/		
public int flowPtr(int regime){
 int row;
  if (regime>ObjForm.nextRegime/2f){ //start at end if big regime
   for (row=this.flow.length-1; row>=1; row--)
   if (this.flow[row][0]==regime)return(row);}
  else {
   for (row=1; row<this.flow.length; row++)
    if (this.flow[row][0]==regime)return(row);
  }   		 
return(0); //return 0 if regime not found
}/**/

/**
*@return the regime in the row specified or 0 if out of bounds
*/
public int getSched(int row){
if (row>0 && row<flow.length) return(flow[row][0]);
return(0);
}/*end getSched()*/

} /*end FlowData class*/ 
