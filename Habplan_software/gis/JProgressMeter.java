package gis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 *A class to display a progress bar in a dialog while something
 *time consuming is happening.
 */
class JProgressMeter extends JDialog{
    JProgressBar progressBar;
    JButton  killButton;
    JPanel panel;
    Thread worker; //the thread that is being monitored
    JFrame parent;
    public JProgressMeter(JFrame parent,Thread worker,String title){
	super(parent);
	setLocationRelativeTo(parent);
	  this.parent=parent;
	setTitle(title);
	createProgressBar();
        this.worker=worker;
	killButton=new JButton("Close");
	killButton.setToolTipText("Close this window, doesn't kill process");
	killButton.addActionListener(new ButtonListener());
	panel =new JPanel();
	panel.add(progressBar);
        panel.add(killButton);
	getContentPane().add(panel);
	setVisible(false); 
	setLocationRelativeTo(parent); 
	pack();
    }/*end constructor*/
    
    protected void createProgressBar(){
	progressBar = new JProgressBar();
	progressBar.setBackground(Color.green);
	progressBar.setToolTipText("This is a progress bar");
	progressBar.setStringPainted(true);
	progressBar.setIndeterminate(true);
	progressBar.setStringPainted(true); 
	progressBar.setString("Working");
    }/*end method*/

  
    /*
     *reset  min and max 
     */
    protected void init(int min, int max){
  
	setVisible(true);
	progressBar.setMinimum(min);
	progressBar.setMaximum(max);
	progressBar.setValue(0);
    }
    protected void init(){init(0,100);}

    /*
     *Call this  to start the progress meter
     */
    protected void work(){
	RunThread thread=new RunThread(this);
        thread.start();
    }
    /**
     * Handle button clicks
     **/   
    public class ButtonListener implements ActionListener{
    
	public void actionPerformed(ActionEvent e) {
	    setVisible(false);
            dispose();
	}/*end actionperformed()*/

    } /*end inner ButtonListener class*/


    /**
     *inner class to allow the progress meter to work
     *simultaneously with the time consuming task
     */  
    class RunThread extends Thread{
	JProgressMeter meter;
	public RunThread(JProgressMeter meter){
	    this.meter=meter;
	}/*end constructor*/
	public void run(){
	    meter.setVisible(true);
	    meter.toFront();
	    try{
		while(worker.isAlive()){
		 sleep(20);	}
	    }catch(Exception e){;}
	    progressBar.setString("Finished");
	    progressBar.setIndeterminate(false);
	    meter.setVisible(false);
	    meter.dispose();
	}/*end method*/  
    }/*end inner Thread class*/
  
   
}/*end class*/
