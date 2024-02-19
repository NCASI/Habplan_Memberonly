import java.util.*;

/**
*Hold the current settings and state of Habplan.  This can be saved
*and passed across the network to facilitate compute serving
*/
class HoldState implements java.io.Serializable{

  
//n of items on each type of form
final static int  nFForm=13, nCForm=5, nBForm=8, nBIForm=5, nBIIForm=4,nSForm=4;
final static int nSchedule=5; //n of things saved related to schedule and main menu
    final static int nFitness=2;
    final static int nRemote=3; //n of things saved for remote control table
    Object[] tempVec; //for some intermediate operations 
    Object[][] flow;   //storage for the menu items
    Object[][] block;
    Object[][] ccFlow;
    Object[][] bioI;
    Object[][] bioII;
    Object[][] sMod;
    Object[] schedule; //hold nPolys and the current schedule and main menu
    Object[] fitness; //hold fitness information
    Object[][] entryOrder; //records the order that components were entered in column 0
    // and the checked components at save time in col 1
    Object[] remote; //the table entries for remote servers		       

//number of each kind of component
int flowCnt, bioICnt, bioIICnt, sModCnt, blockCnt, ccFlowCnt;  //keeps track of which one we're on
int nFlow, nBioI, nBioII, nSMod, nCCFlow, nBlock; //total number of each kind
int nComponents; //total number of components
/**
*create Objects to hold form settings
*/
public  HoldState(int nFlow,int[] nBKofF,int[] nCofF,
     int nBioI,int nBioII,int nSMod){
 this.nFlow=nFlow;
 this.nBioI=nBioI;
 this.nBioII=nBioII;
 this.nSMod=nSMod;
 for (int i=0;i<nBKofF.length;i++)nBlock+=nBKofF[i];
 for (int i=0;i<nCofF.length;i++)nCCFlow+=nCofF[i];
   //be sure to edit this if new components are added
 nComponents=nFlow+nBioI+nBioII+nSMod+nBlock+nCCFlow;
 entryOrder=new Object[nComponents][2];
 //initialize the entryOrder vector
 for (int i=0;i<nComponents;i++){
     entryOrder[i][0]=new Integer(0); //not been entered
     entryOrder[i][1]=new Boolean(false); //not in objective function
 }/*end for i*/

  tempVec=new Object[SetState.maxForm]; //used for temp storage
 flow=new Object[nFlow][nFForm]; //allocate space for each components form items
 ccFlow=new Object[nCCFlow][nCForm]; 
 block=new Object[nBlock][nBForm];
 bioI=new Object[nBioI][nBIForm];
 bioII=new Object[nBioII][nBIIForm];
 sMod=new Object[nSMod][nSForm];
 schedule=new Object[nSchedule];
 fitness=new Object[nFitness];
 remote=new Object[nRemote];
}/*end constructor*/ 

  

/**
*record the entry order of components, 
*usually called from FrameMenu as a component is entered
*If component is taken out and re-entered its entry order doesn't change
*this is because internal regime id's depend on the entry order
* and this is used to reconstruct those id's when importing a saved job
*@param component is the component id, 1 to nComponents
*/ 
public void setEntryOrder(int component){
    component--; //since frameMenu calls the 1st component number 1

    if( ((Integer)entryOrder[component][0]).intValue()>0 )return; //it's being re-entered
    int next=1;//gives the next component order of entry number
    
    for (int i=0;i<nComponents;i++)
     if( ((Integer)entryOrder[i][0]).intValue()>0 )next++;
    entryOrder[component][0]=new Integer(next); 
    }

/**
*return the entry order for a component
*@param component is the component id, 1 to nComponents
*/    
public int getEntryOrder(int component){
    component--; //since frameMenu calls the 1st component number 1
    return ((Integer)entryOrder[component][0]).intValue();
    }
    
/**
*keep track of the components that are entered into the objective function
*@param component is the component id, 1 to nComponents
*@param isIn is true if in or false if out
*/ 
public void setObjCheck(int component, boolean isIn){
    component--; //since frameMenu calls the 1st component number 1
    entryOrder[component][1]=new Boolean(isIn); 
    //for(int i=0;i<nComponents;i++)System.out.println("order state "+entryOrder[i][0]+" "+entryOrder[i][1]);
    }   

/**
*Useful when importing a saved run to get the "in" components at time of save
*@return true if the component was entered into the objective function
*@param component is the component id, 1 to nComponents
*/ 
public boolean getObjCheck(int component){
    component--; //since frameMenu calls the 1st component number 1
    return ((Boolean)entryOrder[component][1]).booleanValue(); 
    }   

/**
*copy the current schedule
*/    
public void setSchedule(Object[] in){
    System.arraycopy(in,0,schedule,0,nSchedule);
}
    
 /**
*return the schedule object
*/    
public Object[] getSchedule(){
    return schedule;
}
   
/**
*copy fitness function settings
*/   
public void setFitness(Object[] in){
    System.arraycopy(in,0,fitness,0,nFitness);
    }

/**
*return the fitness object
*/    
public Object[] getFitness(){
    return fitness;
}    
    
    
/**
*copy the current remote controls
*/    
public void setRemote(Object[] in){
    System.arraycopy(in,0,remote,0,nRemote);
}
    
 /**
*return the remote controls
*/    
public Object[] getRemote(){
    return remote;
}   
 
    
 /**
 *Copy the flow input Object into the local storage Object
 */   
 public void setFlow(Object[] in){
     System.arraycopy(in,0,flow[flowCnt],0,nFForm);
     flowCnt++;
     if(flowCnt==nFlow)flowCnt=0; //reset to prep for a getFlow
 } 

 
 /**
 *return the local flow Object
 */  
 public Object[] getFlow(){
     System.arraycopy(flow[flowCnt],0,tempVec,0,nFForm); //copying to tempVec simplifies incrementing flowCnt
     flowCnt++;
     if(flowCnt==nFlow)flowCnt=0; //reset to 0 to prep for another setFlow
     return tempVec;
 }
    
    /**
 *Copy the block input Object into the local storage Object
 *@param in is the input Object
 */   
 public void setCCFlow(Object[] in){
     System.arraycopy(in,0,ccFlow[ccFlowCnt],0,nCForm);
     ccFlowCnt++;
     if(ccFlowCnt==nCCFlow)ccFlowCnt=0;
 }   
 
 /**
 *return a block Object
 */   
 public Object[] getCCFlow(){
     System.arraycopy(ccFlow[ccFlowCnt],0,tempVec,0,nCForm); //copying to tempVec simplifies incrementing flowCnt
     ccFlowCnt++;
     if(ccFlowCnt==nCCFlow)ccFlowCnt=0;
     return tempVec;
 }   
    
/**
 *Copy the block input Object into the local storage Object
 *@param in is the input Object
 */   
 public void setBlock(Object[] in){
     System.arraycopy(in,0,block[blockCnt],0,nBForm);
     blockCnt++;
     if(blockCnt==nBlock)blockCnt=0;
 }   
 
 /**
 *return a block Object
 */   
 public Object[] getBlock(){
     System.arraycopy(block[blockCnt],0,tempVec,0,nBForm); //copying to tempVec simplifies incrementing flowCnt
     blockCnt++;
     if(blockCnt==nBlock)blockCnt=0;
     return tempVec;
 }   
    
  /**
 *Copy the bioI input Object into the local storage Object
 *@param in is the input Object
 */   
 public void setBioI(Object[] in){
     System.arraycopy(in,0,bioI[bioICnt],0,nBIForm);
     bioICnt++;
     if(bioICnt==nBioI)bioICnt=0;
 }   
 
 /**
 *return a bioI Object Vector
 */   
 public Object[] getBioI(){
     System.arraycopy(bioI[bioICnt],0,tempVec,0,nBIForm); //copying to tempVec simplifies incrementing flowCnt
     bioICnt++;
     if(bioICnt==nBioI)bioICnt=0;
     return tempVec;
 }   
 
  /**
 *Copy the bioII input Object into the local storage Object
 *@param in is the input Object
 */   
 public void setBioII(Object[] in){
     System.arraycopy(in,0,bioII[bioIICnt],0,nBIIForm);
     bioIICnt++;
     if(bioIICnt==nBioII)bioIICnt=0;
 }   
 
 /**
 *return a bioII Object Vector
 */   
 public Object[] getBioII(){
     System.arraycopy(bioII[bioIICnt],0,tempVec,0,nBIIForm); //copying to tempVec simplifies incrementing flowCnt
     bioIICnt++;
     if(bioIICnt==nBioII)bioIICnt=0;
     return tempVec;
 }   
    
      /**
 *Copy the spatial model input Object into the local storage Object
 *@param in is the input Object
 */   
 public void setSMod(Object[] in){
     System.arraycopy(in,0,sMod[sModCnt],0,nSForm);
     sModCnt++;
     if(sModCnt==nSMod)sModCnt=0;
 }   
 
 /**
 *return a spatial model Object Vector
 */   
 public Object[] getSMod(){
     System.arraycopy(sMod[sModCnt],0,tempVec,0,nSForm); //copying to tempVec simplifies incrementing flowCnt
     sModCnt++;
     if(sModCnt==nSMod)sModCnt=0;
     return tempVec;
 }   
}/*end class*/
