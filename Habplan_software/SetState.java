import java.util.*;
import java.io.*;


/**
*Class to gather all settings, the current schedule and weights --
*and to save or load them to or from the Serializable class
*HoldState.  HoldState is kept separate and simple because it is
*sent over the network to allow parallel processing
*/

 class SetState {
    //create vector of Objects to hold everything for each component type;
    Object[] vec;
    final static int maxForm=13;//make this big enough to hold the largest forms items
   
    FrameMenu f;
    
    public  SetState(FrameMenu f) {
	this.f=f;
	vec=new Object[maxForm];  //set to hold the biggest form
    }/*end constructor*/
    
/**
*put the current settings into Objects in holdState
*by calling appropriate Objectizing methods
*/	
    public void setSnapShot(){
	 f.captureHoldStateSettings(vec);
	f.holdState.setSchedule(vec);
	 f.bestSchedule.captureHoldStateSettings(vec);
	f.holdState.setFitness(vec);
	 f.remoteControlTable.captureHoldStateSettings(vec);
	f.holdState.setRemote(vec);
    
	for (int i=1;i<=f.nComponents;i++){
	    if(f.form[i] instanceof FlowForm){
		FlowForm form=(FlowForm)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setFlow(vec);}
	    if(f.form[i] instanceof BlockForm){
		BlockForm form=(BlockForm)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setBlock(vec);}	
	   if(f.form[i] instanceof CCFlowForm){
		CCFlowForm form=(CCFlowForm)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setCCFlow(vec);}
	     if(f.form[i] instanceof BiolForm){
		BiolForm form=(BiolForm)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setBioI(vec);}		
	     if(f.form[i] instanceof Biol2Form){
		Biol2Form form=(Biol2Form)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setBioII(vec);}		
	    if(f.form[i] instanceof SpaceForm){
		SpaceForm form=(SpaceForm)f.form[i];
		form.captureHoldStateSettings(vec);
		f.holdState.setSMod(vec);}						
	}/*end for i*/
    }/*end method*/
    
/**
*restore saved settings by calling unVec methods   
*/
     public void getSnapShot(){
	 vec=f.holdState.getSchedule();
	f.loadHoldStateSettings(vec);
	 vec=f.holdState.getFitness();
	f.bestSchedule.loadHoldStateSettings(vec);
	 vec=f.holdState.getRemote();
	f.remoteControlTable.loadHoldStateSettings(vec);
	for (int i=1;i<=f.nComponents;i++){
	    if(f.form[i] instanceof FlowForm){
		FlowForm form=(FlowForm)f.form[i];
		vec=f.holdState.getFlow();
		form.loadHoldStateSettings(vec);
		}
	   if(f.form[i] instanceof BlockForm){
		BlockForm form=(BlockForm)f.form[i];
		vec=f.holdState.getBlock();
		form.loadHoldStateSettings(vec);}	
	  if(f.form[i] instanceof CCFlowForm){
		CCFlowForm form=(CCFlowForm)f.form[i];
		vec=f.holdState.getCCFlow();
		form.loadHoldStateSettings(vec);}	
	   if(f.form[i] instanceof BiolForm){
		BiolForm form=(BiolForm)f.form[i];
		vec=f.holdState.getBioI();
		form.loadHoldStateSettings(vec);}	
	    if(f.form[i] instanceof Biol2Form){
		Biol2Form form=(Biol2Form)f.form[i];
		vec=f.holdState.getBioII();
		form.loadHoldStateSettings(vec);}		
	    if(f.form[i] instanceof SpaceForm){
		SpaceForm form=(SpaceForm)f.form[i];
		vec=f.holdState.getSMod();
		form.loadHoldStateSettings(vec);}					
	}/*end for i*/  
	   
     }/*end method*/
        
    /**
    *save the current state of Habplan
    *@param fname the fully specified path and file to save to
    */
    public void saveState(String fname){
    try{    
     setSnapShot();
     FileOutputStream fileOut=new FileOutputStream(fname); 
     ObjectOutputStream out=new ObjectOutputStream(fileOut);
     out.writeObject(f.holdState); //save current holdState object
     } catch(IOException e){System.out.println("Unable to save Habplan settings. "+e);}    
    }/*end saveState()*/
    
    /**
    *load saved settings into holdState object
    *@param fname file where serialized object resides
    */
    public void loadState(String fname){
	try{   
    FileInputStream fileIn=new FileInputStream(fname);  
    ObjectInputStream in=new ObjectInputStream(fileIn);
    f.holdState=(HoldState)in.readObject();
    //for(int i=0;i<f.nComponents;i++)System.out.println("order state "+f.holdState.entryOrder[i][0]+" "+f.holdState.entryOrder[i][1]);
    getSnapShot();
    }catch(Exception e){}
    }/*end loadState()*/

       /**
     *clear the entryOrder array
     *Called when loading saved problem settings from main menu
     */
    public void unsetEntryOrder(){
	for (int i=0;i<f.nComponents;i++){
	    f.holdState.entryOrder[i][0]=new Integer(0); //not been entered
	    f.holdState.entryOrder[i][1]=new Boolean(false); //not in objective function
	}
    }  
    
    /**
    *automatically read data, enter objective function components
    *then start the run
    */
    public void setCheckBoxes(){
	f.stopTheRun();
	 //for(int i=0;i<f.nComponents;i++)System.out.println("order state "+parent.holdState.entryOrder[i][0]+" "+f.holdState.entryOrder[i][1]);
       for (int j=1;j<=f.nComponents;j++){//enter the components in the proper order
	 for (int i=1; i<=f.nComponents; i++){
	   if( f.holdState.getEntryOrder(i)==j){f.compBox[i].setSelected(true); //read the data
	    f.manualItemStateChanged(); //do what needs to be done when a component is added
	    f.compBox[i].setSelected(f.holdState.getObjCheck(i));} //unCheck if it isn't currently in
	    f.manualItemStateChanged();
	 }/*end for i*/
        }/*end for j*/
       f.unitBox.setEnabled(true);
	f.startTheRun();
    }/*end method*/
    
}/*end class*/
