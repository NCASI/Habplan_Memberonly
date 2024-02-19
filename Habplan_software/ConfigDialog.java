import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;


/**
*A dialog for Habplan users to reconfigure Habplan
*to have different combinations of objective function
*components. upgraded to swing on 12/04
*@author Paul Van Deusen (NCASI)  6/98
*/
public class ConfigDialog extends JDialog {
   
  FrameMenu parent;
  
  private static int level=0;  //keep track of what form is showing
  JLabelTextField flowText,b1Text,b2Text,spaceText,nrowText;
  JLabelTextField[] blockText, ccFlowText;
  JPanel buttonPanel;
  JButton nextButton, finishButton, backButton;
  
  
  
public static void main (String args[]) {
 
}/*end main*/


public ConfigDialog(FrameMenu parent, String s){
  super(parent, s);
  setLocation(450,300);
  this.parent=parent;
  
  
  //main component entry form
  flowText=new  JLabelTextField("N of Flows",4,"int");
   flowText.text.addCaretListener(new ATextListener());
  b1Text=new  JLabelTextField("N of Biol Type 1",4,"int");
  b2Text=new  JLabelTextField("N of Biol Type 2",4,"int");
  spaceText=new  JLabelTextField("N of SpaceMods",4,"int");
  nrowText=new  JLabelTextField("N per Row on Form",4,"int");
  flowText.text.setText("1");
  b1Text.text.setText("0");
  b2Text.text.setText("1");
  spaceText.text.setText("1");
  nrowText.text.setText("4");
    Font font = new Font("SansSerif", Font.BOLD, 14);
    nextButton = new JButton("Next->");
    nextButton.addActionListener(new ButtonListener());
    nextButton.setActionCommand("Next");
    nextButton.setFont(font);
    nextButton.setToolTipText("Go to the next config form");

    finishButton = new JButton("Finish");
    finishButton.addActionListener(new ButtonListener());
    finishButton.setActionCommand("Finish");
    finishButton.setFont(font);
    finishButton.setToolTipText("Complete the configuration change");
    
    backButton = new JButton("Back->");
    backButton.addActionListener(new ButtonListener());
    backButton.setActionCommand("Back");
    backButton.setFont(font);
    backButton.setToolTipText("Return to the first config form");
    
    buttonPanel=new JPanel();
    buttonPanel.add(finishButton);
    buttonPanel.add(backButton);
    
   topForm();
 

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

/**
*Display the form to set top level components
*/
public void topForm(){
  level=0;
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(7,1));
  JLabel label=new JLabel("Top Level Components");
  getContentPane().add(label); 
  getContentPane().add(flowText);
  getContentPane().add(b1Text);
  getContentPane().add(b2Text);
  getContentPane().add(spaceText);
  getContentPane().add(nrowText);
  getContentPane().add(nextButton);
  getContentPane().setBackground(Color.white);
  pack(); 
  //make sure textListener is called
  flowText.text.setText(flowText.text.getText());
}/*end topForm()*/

/**
*Display the form to set subFlow components
*/
public void subFlowForm(int level){
  getContentPane().removeAll();
  getContentPane().setLayout(new GridLayout(4,1));
  JLabel label=new JLabel("Sub-Flow Components");
  getContentPane().add(label); 
  getContentPane().add(blockText[level]);
  getContentPane().add(ccFlowText[level]);
  int test = Integer.parseInt(flowText.text.getText().trim());
  if (level<test)getContentPane().add(nextButton);
  else getContentPane().add(buttonPanel);
  getContentPane().setBackground(Color.white);
  pack(); 
}/*end subFlowForm()*/

/**
* Handle button clicks
**/
   
public class ButtonListener implements ActionListener{

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    
    if (cmd.equals("Next")) {
     //make sure that textListener is called
	flowText.text.setText(flowText.text.getText().trim());
	level++;
     try{
	 int test = Integer.parseInt(flowText.text.getText().trim());
	 if (level>test){level=0; topForm();return;}
	 subFlowForm(level);
	 return;
     } catch (Exception e0) {return;}
    }  
    
     if (cmd.equals("Back")) {
      topForm();
      return;
    }  
    
    if (cmd.equals("Finish")) {
      prepareOutput();
      setVisible(false);
      return;
    }  
        
    
  }/*end actionperformed()*/

} /*end inner ButtonListener class*/

/**
*Create the subflow forms pieces on the fly
*/
public class ATextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
  int i; String s;
  String[] cLabel,bLabel;
  try{
    i = Integer.parseInt(flowText.text.getText().trim());
    cLabel=new String[i+1];
    bLabel=new String[i+1];
      //hold the old settings if user uses back button
    for (int k=1;k<=i;k++){
     bLabel[k]="0";
     cLabel[k]="0";
     try{ bLabel[k]=blockText[k].text.getText().trim();}
     catch(Exception e){}
     try{ cLabel[k]=ccFlowText[k].text.getText().trim();}
     catch(Exception e){}
     }/*end for k*/
    blockText=new JLabelTextField[i+1];
    ccFlowText=new JLabelTextField[i+1];
   for (int j=1;j<=i;j++){
     s=Integer.toString(j);
     
     blockText[j]=new JLabelTextField("N of Block("+s+")",4,"int");
     blockText[j].text.setText(bLabel[j]);
      
     ccFlowText[j]=new JLabelTextField("N of CCFlow("+s+")",4,"int");
     ccFlowText[j].text.setText(cLabel[j]);

   }/*end for j*/
  
   }/*end try*/
  catch(Exception e){;/*do nothing then*/}  
 
 }/*end textChangedValue*/
} /*end inner ATextListener class*/


/**
*Prepare output for changeConfig to put in the file
*First row is habplan.config.nrow
*Second row is habplan.config which contains
*nFlow, then nFlow numbers giving nCCFlows per Flow parent
*then nFlow numbers giving nBlocks per Flow parent
*then nBIO1's, then nBIO2's, then nSMods with all
*numbers separated by commas
*/

public void prepareOutput(){
 String output;
 int temp,subs;
  //do nrow first
 try{temp=Integer.parseInt(nrowText.text.getText().trim());}
 catch(Exception e){temp=4;}
 output="habplan.config.nrow="+Integer.toString(temp)+"\n";
 //flows
 try{temp=Integer.parseInt(flowText.text.getText().trim());}
 catch(Exception e){temp=1;}
 output=output+"habplan.config="+Integer.toString(temp)+",";
 //subFlows
 for (int i=1;i<=temp;i++){ //do CCFlows
   try{subs=Integer.parseInt(ccFlowText[i].text.getText().trim());}
  catch(Exception e){subs=1;}
  output=output+Integer.toString(subs)+",";
 }/*end for i*/ 
 for (int i=1;i<=temp;i++){ //do Blockx
   try{subs=Integer.parseInt(blockText[i].text.getText().trim());}
  catch(Exception e){subs=1;}
  output=output+Integer.toString(subs)+",";
 }/*end for i*/
  //Biol type 1
 try{temp=Integer.parseInt(b1Text.text.getText().trim());}
 catch(Exception e){temp=1;}
 output=output+Integer.toString(temp)+","; 
  //Biol type 2
 try{temp=Integer.parseInt(b2Text.text.getText().trim());}
 catch(Exception e){temp=1;}
 output=output+Integer.toString(temp)+",";
  //SpaceMod
 try{temp=Integer.parseInt(spaceText.text.getText().trim());}
 catch(Exception e){temp=1;}
 output=output+Integer.toString(temp);
 
 //parent.console.println(output);

 changeConfig(output);
 return;
}/*end prepareOutput()*/

/**
*Change habplan.config in properties.hbp file
*These will affect the next Habplan run
*/
public void changeConfig(String output){
String configFile="configuration.hbp";
String fileName=UserInput.hbpDir+configFile;
 File file2=new File(fileName);
     if (file2.exists()){
       file2.delete();
       parent.console.println("Changing the configuration file." + fileName);
     }/*end if*/

 FileOutputStream fout=null;
   try {
    fout =  new FileOutputStream(fileName);
    PrintWriter saveConfig = new PrintWriter(fout,true);
    saveConfig.println(output);
    parent.console.println("Changes take effect when you restart Habplan.");     
     }/*end try*/
  catch(Exception e){
   parent.console.println("Error writing the new configuration." );
  } /*end catch*/
   finally { try { if (fout != null) fout.close(); } catch (IOException e) {}     }
 
     return;          
}/*end changeConfig()*/






} /*end ConfigDialog class*/


 
