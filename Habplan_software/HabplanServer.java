import java.rmi.*;
interface HabplanServer extends Remote {
    /**
    *Take and run the client's configuration/status
    */
    public boolean runSchedule(HoldState holdState) throws RemoteException;
    
    /**
    *Return to the client the current HoldState object.
    */
    public HoldState returnSchedule() throws RemoteException;
    
    /**
    *Tell what iteration the server is on
    */
    public int whatIteration() throws RemoteException;
    
    /**
    *Set Hidden mode for server console
    */
    public void setHidden(boolean hide) throws RemoteException;
    
     /**
    *Stop this server from running
    */
    public void stopTheRun() throws RemoteException;
    
    /**
    *check if the server is online
    */
    public boolean ping() throws RemoteException;
}/*end interface*/
