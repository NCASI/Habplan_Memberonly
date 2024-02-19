import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
/**
*Gives the version information
*/

public class License extends JDialog{

JPanel panel, panel2;
JTextArea text1;
JButton close;
JCheckBox check1;
static String fileName=UserInput.hbpDir+".AcceptLicense";

RandomAccessFile fout; //in case the license file needs to be created.
File file1; //holds password;
JPasswordField password;
protected String secret;
boolean status=true;

public License(FrameMenu parent){
 super(parent,"Habplan License Information");
 setLocation(200,50); 
 
 
 File file1=new File(fileName); //decide if license is visible
 if (!file1.exists()){setVisible(true); status=false;}
 
  Font font = new Font("Times", Font.BOLD, 16);
   close = new JButton("Close");
    close.addActionListener(new ButtonListener());
    close.setActionCommand("close");
    close.setFont(font);
    
 check1=new JCheckBox("I agree to this",status);        
 check1.addItemListener(new CheckListener());

String text="The HABPLAN source code is copyrighted by NCASI.  Anyone may download  the binary version of ";
text+="HABPLAN from the NCASI website.  Use of HABPLAN for non-commercial purposes is ";
text+="encouraged.  A password is required to enable HABPLAN to run problem sizes of more than 500 ";
text+="polygons (stands).  Non-commercial users of Habplan should CONTACT NCASI to request a ";
text+="password.  In general, passwords for non-commercial uses will be granted only to universities and ";
text+="other non-profit / educational institutions for specific projects of limited duration. ";
text+="\n\n";
text+="All commercial uses of HABPLAN by individuals and organizations other than NCASI member ";
text+="companies are strictly prohibited unless authorized in advance in writing by NCASI.  Commercial ";
text+="uses include but are not limited to (a) any use of HABPLAN that has any substantial effect on ";
text+="private forest management; (b) any use of HABPLAN in consulting or other commercial service ";
text+="activities conducted for public-sector or private-sector clients, and (c) any sale of software or ";
text+="software-related services based directly or indirectly on HABPLAN. ";
text+="\n\n";
text+="Internal use of HABPLAN by NCASI member companies is unlimited.  Member companies that wish ";
text+="to use HABPLAN for external commercial purposes must pay a supplemental annual fee (in addition ";
text+="to dues).  External commercial purposes include but are not limited to: (a) any use of HABPLAN in ";
text+="consulting or other commercial service activities conducted for public-sector or external ";
text+="private-sector clients, and (b) any sale of software or software-related services based directly or ";
text+="indirectly on HABPLAN.  The supplemental fee for external use of HABPLAN for a year is ";
text+="$5,000.  This fee may be adjusted at the start of each NCASI fiscal year (April 1). If a NCASI ";
text+="member company decides to terminate its membership in NCASI, any and all rights to commercial ";
text+="use of HABPLAN will terminate simultaneously with termination of membership. ";
 
 text1=new JTextArea(text,17,40);
 text1.setEditable(false);
 text1.setLineWrap(true);
 text1.setWrapStyleWord(true);
 text1.setFont(font);
 text1.setBackground(Color.white);
 JScrollPane textScroll=new JScrollPane(text1,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 password = new JPasswordField(15);
 password.addCaretListener(new ATextListener());
 password.setEnabled(status);
 
 JLabel pwdLabel=new JLabel("Problem Size Password");
 
 panel=new JPanel();   
 panel.add(textScroll);
 
 panel2=new JPanel();
 panel2.setLayout(new GridLayout(4,1));
 panel2.add(close); 
 panel2.add(check1); 
 panel2.add(password);
 panel2.add(pwdLabel);
 panel.add(panel2);
 getContentPane().add(panel); 
 
 pack();

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

/**
*This reads the password
*/
public class ATextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
    secret=password.getText();
    if (secret.length()<2)return; 
    file1=new File(fileName); //this file determines initial license visibility
 if (file1.exists()){ file1.delete();
  try{   
 fout =  new RandomAccessFile(fileName,"rw"); 
 fout.writeBytes(secret); 
  } catch (Exception exc){System.out.println("License file problem: "+exc);};
 }/*end if*/ 
 }/*end textChangedValue*/
} /*end inner ATextListener class*/

/**
* Handle button clicks
**/
   
public class ButtonListener implements ActionListener{

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
     
    if (cmd.equals("close")) {
      dispose();
    }  
   
  }/*end actionperformed()*/
} /*end inner Listener class*/

 /**
*User clicks a checkbox (inner) class
*/
public class CheckListener implements ItemListener{
 public void itemStateChanged(ItemEvent e){
 
 file1=new File(fileName); //this file determines initial license visibility
 if (!check1.isSelected() & file1.exists()){ file1.delete(); status=false; password.setEnabled(false);}
 else {  
  try{
 fout =  new RandomAccessFile(fileName,"rw"); 
 fout.writeBytes("rx1400"); 
  } catch (Exception exc){System.out.println("License Class:License file problem: "+exc);};
 password.setEnabled(true); status=true;   
    }/*end else*/
 }/*end itemStateChanged*/
} /*end inner CheckListener class*/


} /*end License class*/
    
    
    
