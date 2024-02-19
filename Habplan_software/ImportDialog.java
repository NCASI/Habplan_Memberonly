
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;


/**
*A dialog for Habplan users to set Habplan
*to the end results of a previous run.
*This is done by importing a previously saved
*schedule and form settings in holdState
*@author Paul Van Deusen (NCASI)  6/98 updated 1/2000
*/
public class ImportDialog extends JDialog {
   
  FrameMenu parent;
  static String[] polyName; //hold the imported schedule names
  
  JFileButton importButton;
  JTextField p1,p2;
  JButton saveButton,loadButton, closeButton;
  
  
public static void main (String args[]) {
 
}/*end main*/


public ImportDialog(FrameMenu parent, String s){
  super(parent, s);
  setLocation(150,300);
  this.parent=parent;
  getContentPane().setLayout(new GridLayout(3,1));
   
       //import section 
  String file=UserInput.hbpDir + "importSchedule.hbp";     
  p1=new JTextField(file,20);    
  JLabel lab1=new JLabel("Import file");
  JPanel panelI=new JPanel();
  panelI.add(lab1);
  panelI.add(p1);
  importButton= new JFileButton("Import file",parent,p1,"hbp","Habplan Save Files");

  //button section
   Font font = new Font("Times", Font.BOLD, 14);
    saveButton = new JButton("Save");
    saveButton.addActionListener(new ButtonListener());
    saveButton.setToolTipText("Save current schedule");
    saveButton.setFont(font);
    loadButton = new JButton("Load");
    loadButton.addActionListener(new ButtonListener());
    loadButton.setToolTipText("Load a saved schedule");
    loadButton.setFont(font);
    closeButton = new JButton("Close");
    closeButton.addActionListener(new ButtonListener());
    closeButton.setFont(font);
   
   JPanel buttonPanel=new JPanel();
    buttonPanel.add(saveButton);
    buttonPanel.add(loadButton);
    buttonPanel.add(closeButton);   
  
   getContentPane().add(importButton);
   getContentPane().add(panelI);
   getContentPane().add(buttonPanel);
 
   getContentPane().setBackground(Color.white);
  pack();

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

/**
* Handle button clicks
**/
   
public class ButtonListener implements ActionListener{

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    
     if (cmd.equals("Close")) {
      setVisible(false);   
     return;  
    }   
    
    if (cmd.equals("Save")) {
      parent.setState.saveState(p1.getText()); 
      parent.console.println("Saved to "+p1.getText());  
     return;  
    }   
     
    if (cmd.equals("Load")) {
	 if(!(parent.runStop && Algorithm.isWaiting)){
	     JOptionPane.showMessageDialog(loadButton,"Stop run before loading","Can't Load While Still Running",JOptionPane.WARNING_MESSAGE);
	     return;
	 }/*fi !isLoad*/
	 ImportThread importThread=new ImportThread();
	 importThread.start();
	 JProgressMeter progress=new JProgressMeter(parent,importThread,getTitle());
	 progress.work();
    }  
    
   
  }/*end actionperformed()*/

} /*end inner ButtonListener class*/

    protected void  manualLoad(){
	parent.reInitialize(); //reset everything and read data again 
      	boolean isStart=parent.start.isEnabled();
	parent.start.setEnabled(false);
	parent.setIsImport(true);//load and stop before iteration 2
	parent.setState.loadState(p1.getText()); 
	parent.setState.setCheckBoxes(); //read data and check obj components
	parent.console.println("Initializing with imported schedule.");
	parent.start.setEnabled(isStart);
	for(int i=1;i<=parent.nComponents;i++){
	    if(!parent.compBox[i].isSelected())continue;
	    if(parent.form[i] instanceof FlowForm){
		FlowForm f=(FlowForm)parent.form[i];
		f.graph.setVisible(true);
	    }
	    else if(parent.form[i] instanceof CCFlowForm){
		CCFlowForm f=(CCFlowForm)parent.form[i];
		f.graph.setVisible(true);
	    }
	    else if(parent.form[i] instanceof BlockForm){
		BlockForm f=(BlockForm)parent.form[i];
		f.graph.setVisible(true);
	    }	    
	}
	return; 	
    }

    class ImportThread extends Thread{

	public void run(){
	    manualLoad();
	     if(!parent.unitBox.isSelected()){
	     int result=JOptionPane.showConfirmDialog(parent,"Check the Unit Box","Enforce Management Units?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
	     if(result==JOptionPane.YES_OPTION)parent.unitBox.setSelected(true);
	 }
	}
    }

} /*end ImportDialog class*/


 
