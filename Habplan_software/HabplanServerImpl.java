import java.util.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
/**
*implements the server interface
*/

 class HabplanServerImpl extends UnicastRemoteObject
      implements HabplanServer{
	FrameMenu parent;
	  
	  public HabplanServerImpl(FrameMenu parent)
	      throws RemoteException{
		 this.parent=parent;
		  }/*end constructor*/
   
    
   /**
    *Return to the client the current HoldState object.
    */
    public HoldState returnSchedule() throws RemoteException{
	 parent.bestSchedule.manualLoad();
	 parent.setState.setSnapShot(); //put everything into holdState object
	 return parent.holdState;
	}/*end method*/
     
    /**
    *now run the server for the specified iterations and return the result in 
    *a HoldState object.
    */
    public boolean runSchedule(HoldState holdState)
     throws RemoteException{
	 try{
	 parent.holdState=holdState; //make this current state
	 parent.setState.getSnapShot(); //fill out forms
	 parent.setState.setCheckBoxes(); //read data and run
	 }catch(Exception e){
	   parent.console.println("Error at HabplanServerImpl.runSchedule "+e);
	   return false;}
	 return true;
     }
    
    /**
    *Stop this server from running
    */
    public void stopTheRun() throws RemoteException{
	parent.stopTheRun();
    } 
  
     
  /**
    *Tell what iteration the server is on
    */
    public int whatIteration() throws RemoteException{
	return Algorithm.iter;
	}
	
   /**
    *Set Hidden mode for server console
    */
    public void setHidden(boolean hide) throws RemoteException{
	parent.console.setHidden(hide);
	}	

     
     /**
     *Report that the server is alive
     */
     public boolean ping(){return true;}

}/*end class*/
