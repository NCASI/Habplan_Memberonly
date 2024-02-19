import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.rmi.*;

import java.util.*;

public class RemoteControlTable extends JFrame
    implements ActionListener{
    final static int maxJobs=20; //this gives max n of Jobs
    SendJob[] sendJob=new SendJob[maxJobs]; 
    RetrieveJob[] retrieveJob=new RetrieveJob[maxJobs]; 
    
    String tempSaveFile="tempSaveFile.hbp";  //used for restore feature
    int nRows=2;
    int nCols=6;
    int selectedRow=1;//row in table selected
    int updateInterval=4;  //seconds between update intervals for send
    FrameMenu parent;
    HabplanServerImpl habplanServer;
    JTable table;
    CustomModel model; //the table model
    JLabel serverLabel;
    String bindName="server1";
    String server="localhost"; //the selected host
    boolean send, retrieve;   //send or retrieve state
    protected Object[][] cell= new Object[nRows][nCols];
    protected String[] columnNames={"ID", "Server", "Hide", "Send",
	     "Retrieve", "Status"};	
    protected Object[] rowData={ "1","localhost",
	new Boolean(false), new Boolean(false), new Boolean(false), "none"};	
     
   
    JButton closeButton, addRowButton, sendButton, retrieveButton, restoreButton,
       serversButton, timeButton, killButton; 
    JCheckBox serverCheckBox;
   
    
    public RemoteControlTable(FrameMenu parent){
	super("Habplan Remote Control");
	this.parent=parent;
	setBounds(400,200,375,250);
	
	//create default table entries
	for(int i=0;i<nRows;i++)
	  for (int j=0; j<nCols;j++)cell[i][j]=rowData[j];	
	
	 createTable();
	 
    
	createButtons();   
		
	//provide a command line approach to creating a server with -D option
	if (System.getProperty("habplan.server","false").equals("true"))makeAServer();
	
	 setDefaultCloseOperation(HIDE_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      dispose();
	  }
      });   
    }/*end constructor*/
 
/**
*make the table
*/
public void createTable(){	
	model=new CustomModel(cell,columnNames);
	table=new JTable(model);
	table.getModel().addTableModelListener(new TableListener());
	table.getTableHeader().setToolTipText("Double click on server address to select or edit");
	table.setSize(10,100);
	JScrollPane scroll=new JScrollPane(table);
	getContentPane().add(scroll,"Center");
	columnResize();//resize columns	
	
    }
 
 
  /**
 *capture settings into Object vector to be saved into HoldState Object
 */
protected void captureHoldStateSettings(Object[] vec){
  vec[0]=new Integer(nRows);
  vec[1]=new Integer(nCols);
  storeTable(); //put current table in cell array
  vec[2]=cell;    
    }

/**
*load the settings from HoldState that were save by captureHoldStateSettings()
*/    
 protected void loadHoldStateSettings(Object[] vec){
   nRows=((Integer)vec[0]).intValue();
   nCols=((Integer)vec[1]).intValue();
   cell=(Object[][])vec[2];
   restoreTable();
     }
 
 
/**
*used to restore saved table settings
*/    
public void restoreTable(){
    int r=model.getRowCount()-nRows;
    for(int i=1;i<=r;i++)model.addRow(rowData);
    
    for(int j=0;j<3;j++) //only restore first 2 columns with server names
	for(int i=0;i<nRows;i++)model.setValueAt(cell[i][j],i,j);
 }/*end method*/
 
 /**
 *store the table settings
 */
 public void storeTable(){
     cell=new Object[nRows][nCols];
	for(int i=0;i<nRows;i++)  
	  for (int j=0; j<nCols;j++)
	      cell[i][j]=table.getValueAt(i,j);
     }/*end method*/
        
/**
*Make the button panel
*/    
private void createButtons(){
    
    Font font = new Font("SansSerif", Font.BOLD, 10);
     //button section
    closeButton = new JButton("Close");
    closeButton.addActionListener(new ButtonListener());
    closeButton.setFont(font);
    closeButton.setToolTipText("Make this window go away");
    sendButton = new JButton("Send");
    sendButton.addActionListener(new ButtonListener());
    sendButton.setFont(font);
    sendButton.setToolTipText("Send the current job to selected server.");
    addRowButton = new JButton("addRow");
    addRowButton.addActionListener(new ButtonListener());
    addRowButton.setFont(font);
    addRowButton.setToolTipText("Add row to address another server");
    retrieveButton = new JButton("Retrieve");
    retrieveButton.addActionListener(new ButtonListener());
    retrieveButton.setFont(font);
    retrieveButton.setToolTipText("Retrieve job from selected server");
    restoreButton = new JButton("Restore");
    restoreButton.addActionListener(new ButtonListener());
    restoreButton.setFont(font);
    restoreButton.setToolTipText("Restore status before last retrieve");
    serversButton = new JButton("Servers");
    serversButton.addActionListener(new ButtonListener());
    serversButton.setFont(font);
    serversButton.setToolTipText("List the registered servers on this machine");
    
    serverCheckBox=new JCheckBox("Server");
    serverCheckBox.setFont(font);
    serverCheckBox.addActionListener(this);
    serverCheckBox.setToolTipText("Check to make this a server: 1st start rmiregistry");
    
    timeButton = new JButton("Timer");
    timeButton.addActionListener(new ButtonListener());
    timeButton.setFont(font);
    timeButton.setToolTipText("Change the time between status updates");
     
    killButton = new JButton("Kill");
    killButton.addActionListener(new ButtonListener());
    killButton.setFont(font);
    killButton.setToolTipText("Kill the current Send or Retrieve job");
    
    JPanel buttonPanel= new JPanel();
    buttonPanel.setLayout(new GridLayout(2,2));
     
    buttonPanel.add(sendButton);
    buttonPanel.add(retrieveButton);
    buttonPanel.add(restoreButton);
    buttonPanel.add(killButton);
    
    getContentPane().add(buttonPanel,BorderLayout.SOUTH);
    
    serverLabel=new JLabel(server);
    serverLabel.setForeground(Color.yellow);
    JPanel labelPanel=new JPanel();
    labelPanel.setLayout(new BorderLayout());
    labelPanel.add(serverLabel,BorderLayout.CENTER);
    
    JPanel northPanel=new JPanel();
    northPanel.add(closeButton);
    northPanel.add(addRowButton);
    labelPanel.add(northPanel,BorderLayout.NORTH);
    
    JPanel southPanel=new JPanel();
    southPanel.add(serverCheckBox);
    southPanel.add(serversButton);
    southPanel.add(timeButton);
    labelPanel.add(southPanel,BorderLayout.SOUTH);
    getContentPane().add(labelPanel,BorderLayout.NORTH);
    
    }/*end method*/

/**
 *handle menu checkbox
 */ 
  public void actionPerformed(ActionEvent e){
      
      if(e.getActionCommand().equals("Server")){
	  if (serverCheckBox.isSelected())makeAServer();
	  }/*end if*/
           
  }/*end actionperformed()*/ 
  
  
/**
* Handle button clicks
**/
   
public class ButtonListener implements ActionListener{
    
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    
     if (cmd.equals("Close")) {
      setVisible(false); dispose();  
     return;  
    }   
    
    if (cmd.equals("Send")) {	
       if (!parent.runHistory){parent.console.println("Import or run something before sending"); return;}
      table.setValueAt(new Boolean(true),selectedRow,3);//check send 
     table.setValueAt(new Boolean(false),selectedRow,4);//uncheck retrieve
	 parent.setState.setSnapShot(); //capture current state in holdState
       String url="rmi://"+server+"/";
        sendJob[selectedRow]=new SendJob(url,bindName,parent.holdState);
	sendJob[selectedRow].setPriority(Thread.NORM_PRIORITY);
	sendJob[selectedRow].start();		
     return;  
    }  
    
      if (cmd.equals("Kill")) { //using stop to ensure that threads can always be killed
       if (sendJob[selectedRow].isAlive()) sendJob[selectedRow].interrupt(); 
     return;  
    }   
    
     
     if (cmd.equals("addRow")) {
       if (nRows==maxJobs){
	   parent.console.println("Only this many rows allowed: "+maxJobs);
	   return;}	 
       nRows++;
      model.addRow(rowData);
     return;  
    }   
    
     
     if (cmd.equals("Retrieve")) {
	 table.setValueAt(new Boolean(false),selectedRow,3);//uncheck send 
     table.setValueAt(new Boolean(true),selectedRow,4);//check retrieve
       if (sendJob[selectedRow].isAlive()) sendJob[selectedRow].interrupt();//kill sendjob thread first
	  String url="rmi://"+server+"/";
	parent.setState.saveState(tempSaveFile);  //save current status for future restore
	retrieveJob[selectedRow]=new RetrieveJob(url,bindName);
	retrieveJob[selectedRow].setPriority(Thread.NORM_PRIORITY);
	retrieveJob[selectedRow].start();
     return;  
    }   
    
     
     if (cmd.equals("Restore")) {
       int row=selectedRow;	 
       parent.setState.loadState(tempSaveFile); 
       parent.isImportRun=true; //do 1 iteration to restore this schedule 
       parent.setState.setCheckBoxes(); //read data and check obj components 
         //next line just makes sure that the server selection is reset properly by TableListener
       table.setValueAt((String)table.getValueAt(row,1),row,1);     
     return;  
    }   
    
     
     if (cmd.equals("Servers")) {
          parent.console.println("Servers registered on this machine:");
     try{
	  String[] bindings=Naming.list("");
	  for (int i=0;i<bindings.length;i++)
	    parent.console.println("binding Name: "+bindings[i]);
      } catch (Exception e2){parent.console.println("Error: "+e2);}

     return;  
    }    
    
    if (cmd.equals("Timer")) {
	String title="Seconds between updates";
	String message="Pick an update interval";
	String[] values={"4","6","8","10","15","30","60","90","120"};
          String s=(String)JOptionPane.showInputDialog(
	    parent.remoteControlTable,message,title,JOptionPane.QUESTION_MESSAGE,
	    null,values,Integer.toString(updateInterval)); 
	 if (s==null)return;
	 else updateInterval=Integer.parseInt(s);    
     return;  
    }   
   
   
  }/*end actionperformed()*/

} /*end inner ButtonListener class*/

    
   /**
   *call to resize the columns
   */ 
    public void columnResize(){
	TableColumn col;
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	col=table.getColumn(columnNames[0]);
	 col.setMinWidth(getPreferredWidthForColumn(col));
	 col.setMaxWidth(getPreferredWidthForColumn(col));
	 col=table.getColumn(columnNames[1]);
	 col.setMinWidth(getPreferredWidthForColumn(col));
	 table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	}/*end method*/
    
    public int getPreferredWidthForColumn(TableColumn col){
	int hw=columnHeaderWidth(col);
	int cw = widestCellInColumn(col);
	return hw>cw?hw:cw;
	}
	
    private int columnHeaderWidth(TableColumn col){
	TableCellRenderer renderer = col.getHeaderRenderer();
	//Component comp=renderer.getTableCellRendererComponent(
	 //   table,col.getHeaderValue(),false,false,0,0);
	//return comp.getPreferredSize().width;
	return 10;
	}/*end method*/    

     private int widestCellInColumn(TableColumn col){
	 int c=col.getModelIndex(), width=0, maxw=0;
	 for (int r=0; r<table.getRowCount();++r){
	     TableCellRenderer renderer=table.getCellRenderer(r,c);
	 Component comp=renderer.getTableCellRendererComponent(
	   table,table.getValueAt(r,c),false,false,r,c);
	   
	   width=comp.getPreferredSize().width;
	   maxw=width>maxw?width:maxw;
	     }/*end for*/
	return maxw;	     
	 }/*end method*/	
	
    class CustomModel extends DefaultTableModel{
	public CustomModel(Object[][] cell, String[] columnNames){
	    super(cell,columnNames);    
	    }
    public Class getColumnClass(int col){
	 //dataVector is a protected member of DefaultTableModel class
	Vector v=(Vector)dataVector.elementAt(0);
	return v.elementAt(col).getClass();
	}	    
	    
    public boolean isCellEditable(int row, int col){
	if (col>=0 && col<3)return true;
	else return false;
	}    
    }/*end innner class*/

/**
*Listener to know which row is selected
*/    
public class TableListener implements TableModelListener{
     
    public void tableChanged(TableModelEvent e){
	if(e.getColumn()>2)return; //ignore changes in status columns, don't want to reset selectedRow by accident
	selectedRow=e.getFirstRow();
	bindName="server"+(String)table.getValueAt(selectedRow,0);
	server=(String)table.getValueAt(selectedRow,1);
	 serverLabel.setText(server+": "+bindName); 	     
	columnResize();//server address may have been edited
	}  /*end method*/  
    }

  
class SendJob extends Thread{
    HoldState holdState;
    HabplanServer theServer;
    int iterations=0; //total number of itertions requested
    boolean jobStatus;
    int row=1;
    
    public SendJob(String serverURL, String bindName, HoldState holdState){
	this.holdState=holdState;
	try{this.iterations=Integer.parseInt(parent.p2.text.getText());}
	catch(Exception e){parent.console.println("Invalid n of iterations");}
	row=selectedRow;//selectedRow might change, so this stores it
	try{theServer=(HabplanServer)Naming.lookup(serverURL+bindName);}
	catch(Exception e){parent.console.println("SendJob error: "+e);
	   parent.console.println("Naming URL "+serverURL+bindName);
	   }/*end try catch*/
	}/*end constructor*/
 public void run(){
     table.setValueAt("wait",row,5);
     boolean isHidden=((Boolean)table.getValueAt(selectedRow,2)).booleanValue();
     try{  
	  theServer.stopTheRun(); //make sure its not in middle of another run
	  theServer.setHidden(isHidden);
	  jobStatus=theServer.runSchedule(holdState);}
     catch (java.rmi.RemoteException e){parent.console.println("SendJob.run() error communicating with "+
		        server+"\n"+e); }   
	finally{if(!jobStatus){table.setValueAt("fail",row,5); iterations=0;}}	
	   int nIters=0,lastIters=-1;
	try{ while(nIters<iterations){
	      try{ sleep(updateInterval*1000);}//user can pick updateInterval
	      catch(InterruptedException e2){table.setValueAt("kill",row,5);
					      theServer.stopTheRun();return;}
	      nIters=theServer.whatIteration();
		//parent.console.println("nIters iterations "+nIters+" "+iterations);
	      if (nIters>=iterations) {table.setValueAt("done",row,5);return;}
	      if(lastIters==nIters){table.setValueAt("died",row,5); continue;}
	       lastIters=nIters;
	        table.setValueAt(Integer.toString(nIters),row,5);
	     }/*end while*/	 
	}catch(Exception e){table.setValueAt("fail",row,5);}
     }/*end method*/
 	
}/*end inner class SendJob*/

 class RetrieveJob extends Thread{
    HabplanServer theServer;
    HoldState holdState;
    int row=1;
    
    public RetrieveJob(String serverURL, String bindName){
	
	row=selectedRow;//selectedRow might change, so this stores it
	try{theServer=(HabplanServer)Naming.lookup(serverURL+bindName);}
	catch(Exception e){parent.console.println("RetrieveJob error: "+e);
	   parent.console.println("Naming URL "+serverURL+bindName);
	   }/*end try catch*/
	}/*end constructor*/
 public void run(){
     boolean isHidden=((Boolean)table.getValueAt(selectedRow,2)).booleanValue();
     try{ 
       theServer.stopTheRun();
       theServer.setHidden(isHidden);
       if(isInterrupted())return;  //probably will never be of any use
	parent.holdState=theServer.returnSchedule();
       parent.setState.getSnapShot(); //set retrieved state
       parent.isImportRun=true;
       parent.setState.setCheckBoxes();
	  //next line just makes sure that the server selection is reset
	  //properly by TableListener
       table.setValueAt((String)table.getValueAt(row,1),row,1);
       table.setValueAt("done",row,5);
      }catch (java.rmi.RemoteException e){parent.console.println("RetrieveJob.run() error communicating with "+
		        server+"\n"+e);table.setValueAt("fail",row,5);return; }		   
      			
         }/*end method*/
 	
}/*end inner class RetrieveJob*/


/**
*bind this to a name for rmiregistry if this is to be a server
*/
private void makeAServer(){
    
     String title="N 0f Servers.";
     String message="Pick a server id number:";
     String[] selectionValues={"1","2","3","4","5","6",};
     String[] bindings=null;
     String s="1";
     
    try{bindings=Naming.list("");} catch (Exception e2){parent.console.println("makeAServer Error: "+e2);}
      
      if (bindings.length>0){
	   s=(String)JOptionPane.showInputDialog(this,message,title,
	    JOptionPane.QUESTION_MESSAGE,null,selectionValues,selectionValues[0]);
	   if(s==null)return; //pushed cancel  
      }/*end if bindings length*/
      bindName="server"+s;
    
    try{ habplanServer=new HabplanServerImpl(parent);
		Naming.rebind(bindName,habplanServer);
	    }catch (Exception e){
		parent.console.println("Error at RemoteControlTable:"+e+
		"\nMaybe you didn't start rimregistry");}  
	serverCheckBox.setSelected(true);//gets a check when done from command line	 
    }/*end method*/

}/*end RemoteControlTable class*/


