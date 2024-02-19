
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;


/**
*A JButton that opens a file dialog. Its linked to a JTextField
*that displays the current file name
*
*@author Paul Van Deusen (NCASI)  12/97, modified to swing 12/03
*/

public class JFileButton extends JPanel {
  JButton button; 
    HBPFilter filter;
  JFileChooser chooser;
  JFrame myFrame; //the parent Frame
  JTextField textfield; //textfield that holds filename
    String ext; //file extension for filtering
    String desc;// file description

    public JFileButton(String s, JFrame myFrame, JTextField textfield, String ext, String desc){
	this.myFrame=myFrame;
	this.textfield=textfield;
        chooser = new JFileChooser();
        if(ext!=null & desc!=null){
	 filter = new HBPFilter(ext,desc);
	 chooser.setFileFilter(filter);
	}
	button = new JButton(s);
  	button.addActionListener(new ButtonListener());
	button.setToolTipText("Open a file chooser");
	add(button);
  
    }/*end constructor*/

 /**
     * Handle button clicks
     **/   
    public class ButtonListener implements ActionListener{
    
	public void actionPerformed(ActionEvent e) {
	    File file=new File(textfield.getText());
	   
	    if(!file.isFile()){//maybe its in the exampleDir
	       file=new File(UserInput.ExampleDir+textfield.getText());
	    }
            
	    if(file.isFile() || file.getName().equals("")){
		chooser.setSelectedFile(file);
	    }else chooser.setCurrentDirectory(new File(UserInput.ExampleDir));
	    

    int returnVal = chooser.showOpenDialog(myFrame);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
	String sep=System.getProperty("file.separator");
	String fname= chooser.getCurrentDirectory().toString()+sep+chooser.getSelectedFile().getName();
    textfield.setText(fname);
    }
	}/*end actionperformed()*/

    } /*end inner ButtonListener class*/

    public void setFileName(String name){textfield.setText(name);}
    public String getFileName(){return textfield.getText();}


} /*end  class*/
