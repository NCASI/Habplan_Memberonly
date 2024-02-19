/**
*Base Class for the algorithms, i.e. extend to create
* Metropolis and Genetic algorithm
*/
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import java.net.*;


public class Algorithm extends Thread{
   static FrameMenu parent;
    
    protected static boolean isWaiting=true;//algorithm is suspended if true
  protected static int iter, //the  iteration number
       zProp, //the  proposal regime;
       zInit; //keep track of changes to zProp by components (killSmallBlocks can change this in BlockSize component)
  
  protected double fitness;  //the fitness value of a schedule
    
public Algorithm(FrameMenu parent){
    this.parent=parent;   
}/*end constructor*/  

/** 
*allow zProp to be changed by individual obj components (like BlockForm)
*influences the proposal schedule in Metropolis Algorithm
*/
final protected void setZProp(int zProp){this.zProp=zProp;}

/**
*used to get a random, valid proposal schedule 
*@param i denotes polygon i
*/

final public static  int randomSchedule(int i){
 int z=0;
 int ind=1; //used to keep from rechoosing the current schedule

 if (ObjForm.polySched[i]==0)ind=0; //either the 1st iteration or no valid schedules
 if (ObjForm.nOptions>ObjForm.validScheds.length-1)ObjForm.validScheds=new int[ObjForm.nOptions+1]; //a new component may have added more regimes
  ObjForm.nScheds=-1; //-1 tells the component that its the first one
  for (int j=1;j<=parent.nComponents;j++){ 
      if(parent.isAdded[j]){
        int newNscheds=parent.form[j].getValidScheds(i,ObjForm.nScheds);
        if(newNscheds>ObjForm.nScheds)ObjForm.nScheds=newNscheds;
      }
  }/*end for*/
 if (ObjForm.nScheds==0)parent.console.println("No Valid Schedules for polygon "+ObjForm.polyArray.elementAt(i-1));   
 
 //parent.console.print("poly scheds "+i);
 // for (int j=1;j<=ObjForm.nScheds;j++)
 //  parent.console.print(" "+ObjForm.validScheds[j]);
 //    parent.console.println(" ");


 if (ObjForm.nScheds==1)return(ObjForm.validScheds[1]);  //only 1 valid schedule 
 z= (int)Math.floor(Math.random()*(ObjForm.nScheds-ind)) +1+ind; //can't choose current schedule   
  return(ObjForm.validScheds[z]);
}/*end randomSchedule()*/

    /*
     *Check if a schedule is valid for all members of this group
     *@param i is the internal polygon id
     *@param sched is the schedule being checked
     *@return true or false
     **/
    final public static int groupRandomSchedule(int[] member){
	int [] groupScheds=null;
	int z,i=0;
	for(int m=0;m<member.length;m++){
	    i=member[m];
	    if(i==0)continue; //member has no data

	    if (ObjForm.nOptions>ObjForm.validScheds.length-1)ObjForm.validScheds=new int[ObjForm.nOptions+1]; //a new component may have added more regimes
	    ObjForm.nScheds=-1; //-1 tells the component that its the first one
	    for (int j=1;j<=parent.nComponents;j++){ 
		if(parent.isAdded[j])ObjForm.nScheds=parent.form[j].getValidScheds(i,ObjForm.nScheds);
	    }/*end for*/
	    if(m==0){
		groupScheds=new int[ObjForm.nScheds+1];
		for(int ii=1;ii<=ObjForm.nScheds;ii++){
		    groupScheds[ii]=ObjForm.validScheds[ii];
		}
	    }
	    else{//only keep valid schedules
		boolean valid;
		for(int ig=1;ig<groupScheds.length;ig++){
		    valid=false;
		    for(int iv=1;iv<=ObjForm.nScheds;iv++){
			if(groupScheds[ig]==ObjForm.validScheds[iv]){
			    valid=true;
			    break;
			}
		    }
		    if(!valid)groupScheds[ig]=-1;
		}
	    }
	}/*end for m*/
	//now get the shared schedules into validScheds
	ObjForm.nScheds=0;
	for (int j=1;j<groupScheds.length;j++){
	    if(groupScheds[j]!=-1){
	       //System.out.println("lastMem sched "+" "+i+" "+groupScheds[j]);
		ObjForm.nScheds++;
		ObjForm.validScheds[ObjForm.nScheds]=groupScheds[j];
	    }
	}
	if (ObjForm.nScheds==0)return(-1);  //no valid schedule
	if (ObjForm.nScheds==1)return(ObjForm.validScheds[1]);  //1 valid schedule 
  
	z= (int)Math.floor(Math.random()*(ObjForm.nScheds-1)) +2; //can't choose current schedule   
	return(ObjForm.validScheds[z]);
    }/*end groupValidSchedule()*/


    /**
*The algorithm
*/
public void run(){

}/*end  run()*/
 
    /**
     *Do Whatever needs to be done before starting another iteration.
     *This applies to the Metropolis Algorithm and hopefully to
     *any other algorithms added later, e.g. the Genetic Algorithm.
     */
    public  void  postIter(){
  
 try{
     SwingUtilities.invokeAndWait (new Runnable() {
	     public void run(){
	        try{
		 for (int j=1;j<=parent.nComponents;j++)
		  if(parent.isAdded[j]){
		   parent.form[j].postIter();
		   if (parent.form[j].isConverged()==1)parent.compBox[j].setForeground(Color.red);
		   else if (parent.form[j].isConverged()==2)parent.compBox[j].setForeground(Color.yellow);
		   else parent.compBox[j].setForeground(Color.black);     
		  }/*end for/if isAdded*/
	  parent.bestSchedule.saveBest(); //check if this is one of the best to save  
	  parent.gisViewer.autoReDraw(iter);//reDraw the GIS map if user wants
		 }catch(Exception e){}
	     }});
 }catch(Exception e){System.out.println("Error Algorithm.postIter(): "+e);}     
     
 if(parent.gisViewer.getWasReDrawn()){ //use delay if user is redrawing gis
	 int delay=parent.gisViewer.getSliderValue();
      try{sleep(delay);}catch(Exception e){};
   }   
 parent.outputDialog.saveAll(iter); //save any user requested items to files
 parent.isImport=false; //after iteration 1 allow changes to imported schedule


    }/*end postIter method*/

   
}/*end Algorithm class*/
