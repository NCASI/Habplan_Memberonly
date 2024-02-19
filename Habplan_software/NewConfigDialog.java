
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;


/**
*A pop-up dialog for Habplan users to allow  Habplan
*to restart and reconfigure for a new problem
*@author Paul Van Deusen (NCASI)  10/98
*/
public class NewConfigDialog extends JDialog {
   
  FrameMenu parent;
  
  JButton yButton, nButton;

  
  
public static void main (String args[]) {
 
}/*end main*/


public NewConfigDialog(FrameMenu parent, String s){
  super(parent, s, true);
  setLocation(400,300);
  this.parent=parent;
  
  
  // component dialog form
 
    Font font = new Font("Times", Font.BOLD, 16);
    
    
    yButton = new JButton("ReConfigure and Quit");
    yButton.addActionListener(new ButtonListener());
    yButton.setActionCommand("ReConfigure");
    yButton.setFont(font);
    
    nButton = new JButton("Continue as is");
    nButton.addActionListener(new ButtonListener());
    nButton.setActionCommand("Continue");
    nButton.setFont(font);
    
    JPanel buttonPanel=new JPanel();
    buttonPanel.add(yButton);
    buttonPanel.add(nButton);
    buttonPanel.setBackground(Color.blue);
    setLayout(new GridLayout(3,1,1,1));
    JLabel message1=new JLabel("You are loading saved data that requires a different configuration!");
    JLabel message2=new JLabel("Habplan can auto-reconfigure and quit, then you can restart Habplan.");
    message1.setForeground(Color.white);
    message1.setFont(font);
     message2.setForeground(Color.white);
     message2.setFont(font);
    
    getContentPane().setBackground(Color.blue);
    getContentPane().add(message1);
    getContentPane().add(message2);
    getContentPane().add(buttonPanel);
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
    
    if (cmd.equals("ReConfigure")) {
     String output="habplan.config.nrow="+parent.nrowInFile +"\n"+
		   "habplan.config="+parent.configInFile;
     parent.configDialog.changeConfig(output);
     dispose();
     System.exit(1);
    }  
    
     if (cmd.equals("Continue")) {
      dispose(); 
    }  
    
  
   
        
    
  }/*end actionperformed()*/
} /*end inner ButtonListener class*/

} /*end NewConfigDialog class*/


 
