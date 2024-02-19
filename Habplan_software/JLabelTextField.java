import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;


/**
*A JLabel and JTextField Combo within a Panel.
*With a CaretListener to detect changes.
*Primarily for entries
*related to Objective Function Components for Habplan
*@author Paul Van Deusen (NCASI)  12/97, Updated 12/03 for Swing
*/

public class JLabelTextField extends JPanel {
  ATextListener listener;
  JTextField text; 
  JLabel label; 
  String varType;//type of variable, i.e int float etc.
 

public JLabelTextField(String s, int columns,  String varType){  
  label =new JLabel(s);
  this.varType=varType;  
  text = new JTextField(columns);  
  text.setBackground(Color.red.brighter());
  text.addCaretListener(new ATextListener());  
  add(label);  
  add(text);
    
}/*end constructor*/  
public class ATextListener implements CaretListener{  
 public void caretUpdate(CaretEvent event){
 
 try{  
 if (varType.equals("int"))
  Integer.parseInt(text.getText().trim());
   else if (varType.equals("0-1")){ 
 float temp=Float.valueOf(text.getText().trim()).floatValue();
 if (temp<0 || temp>1) text.setText("0-1");
 } 
  else if (varType.equals("1-1")){ 
 float temp=Float.valueOf(text.getText().trim()).floatValue();
 if (temp<-1 || temp>1) text.setText("-1-1");
 } 
   else if (varType.equals("float")) 
 Float.valueOf(text.getText().trim()).floatValue();
   else if (varType.equals("double")) 
  Double.valueOf(text.getText().trim()).doubleValue(); 
  text.setBackground(Color.white);
   }
 catch(Exception e){
   if (!text.getText().equals(null))
   text.setBackground(Color.red.brighter());
   }  
 }/*end method()*/  
} /*end inner ATextListener class*/  
  
    /**
     *disable the Label and the TextField
     */
    public void setEnabled(boolean isEnabled){
	super.setEnabled(isEnabled);
	text.setEnabled(isEnabled);
	label.setEnabled(isEnabled);
    }

    /**
     *modified getText function
     *to give a warning if the textField is blank
     */
    public String getText(){
	String number=text.getText();
	if(number.equals(""))text.setBackground(Color.yellow.brighter());
	return number;
    }

  
} /*end LabelTextField class*/
