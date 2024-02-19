import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
/**
*This is used for viewing text.
*you can initialize with a String, and
*append to or clear the textarea
*@author Paul Van Deusen (NCASI)  1/98
*/

public class JDataViewer extends JFrame implements FilenameFilter,ActionListener{
    
    JTextArea textarea;
    JScrollPane textScroll;
    int fontSize=12;
public JDataViewer(String title, String text){
 super(title);
 getContentPane().setLayout(new BorderLayout());
 
  // Create a TextArea to display the contents of the file in
    textarea = new JTextArea(text, 15, 40);
    textarea.setFont(new Font("Times", Font.BOLD, fontSize));
    textarea.setEditable(false);
    //textarea.setLineWrap(true);
    //textarea.setWrapStyleWord(true);
    textarea.setBackground(Color.white);
    textScroll=new JScrollPane(textarea);
    getContentPane().add(textScroll,BorderLayout.CENTER);
     // Create a bottom panel to hold a couple of buttons in
    JPanel p = new JPanel();
    p.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    getContentPane().add(p,BorderLayout.SOUTH);
  // add a close button  
    Font font = new Font("Times", Font.BOLD, 14);
    JButton close = new JButton("Close");
    close.addActionListener(this);
    close.setActionCommand("close");
    close.setFont(font);
    JButton clear = new JButton("Clear");
    clear.addActionListener(this);
    clear.setActionCommand("clear");
    clear.setFont(font);
    JButton plus = new JButton("+");
    plus.addActionListener(this);
    plus.setActionCommand("+");
    plus.setFont(font);
    plus.setToolTipText("Increase Font Size");
    JButton minus = new JButton("-");
    minus.addActionListener(this);
    minus.setActionCommand("-");
    minus.setFont(font);
    minus.setToolTipText("Decrease Font Size");
    p.add(plus);
    p.add(minus);
    p.add(clear);
    p.add(close);
    
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
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("+")){
      fontSize("+");
      }  /*end if*/  
    if (cmd.equals("-")){
      fontSize("-");
      }  /*end if*/
   
    if (cmd.equals("close")){      // If user clicked "Close" button
      this.dispose();
      }  /*end if*/       
     if (cmd.equals("clear")){      // If user clicked "Clear" button
      this.clear();       
     }  /*end if*/           
  } /*end actionPerformed()*/

    /**
     *Change the fontSize of the textarea
     *@param + is increase, - is decrease
     **/
    public void fontSize(String direction){
        int inc=2;
	if(direction.equals("+")) fontSize+=inc;
	else if(direction.equals("-")){
	    fontSize=(fontSize>4)?fontSize-=inc:4;
	}else fontSize=12;

	String content=textarea.getText();
	int position=textarea.getCaretPosition();
	clear();
	textarea.setFont(new Font("Times", Font.BOLD, fontSize));
	println(content);
	textarea.setCaretPosition(position);
    }

   

/**
*append text but don't setVisible()
*/
public void append(String text){
 textarea.append(text);
}/*end appendText()*/

/**
*like System.print command
*/
public void print(String text){ /*same as append*/
 textarea.append(text);
 textarea.setCaretPosition(textarea.getDocument().getLength());
 setVisible(true);
}/*end appendText()*/

/**
*like System.println command
*/
public void println(String text){ /*same as append + linefeed*/
 textarea.append(text + "\n");
 //make it scroll to bottom with setCaretPosition
 textarea.setCaretPosition(textarea.getDocument().getLength());
 setVisible(true);
}/*end appendText()*/

/**
*clear current text
*/
public void clear(){ 
 textarea.setText(" ");
}/*end clear()*/


/**
*set the visibility
*/
public void setHidden(boolean hide){setVisible(hide);}

/**
*allows display of official habplan .hlp files
*or for user help files ending in .usr
*/
  public boolean accept(File dir, String name) {

         name = name.toLowerCase();
         if (name.endsWith(".hlp")) return true;
         if (name.endsWith(".usr")) return true;
         return false;
 }/*end accept()*/



}/*end JDataViewer class*/
