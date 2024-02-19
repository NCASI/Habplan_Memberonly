
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;


/**
*A Label, TextField,SCroller Combo within a Panel.
*Originally for Habplan goal entries
*@author Paul Van Deusen (NCASI)  6/98, upgraded in 2004
*/

public class JLabelTextScroll extends JPanel {
  ATextListener textListener;
    ScrollAdjustmentListener scrollListener;
  JTextField text;
  JScrollBar scroll; 
  JLabel label; 
  int min,max,scale;
 
public static void main (String args[]) {
  System.out.println("No main() in LabelTextScroll class");
}/*end main*/
/**
*@param s is the label 
*@param columns is width of textfield
*@param min max gives range of scrollbar
*@param scale is what to divide max by to get the range you really want
*/
public JLabelTextScroll(String s, int columns, int min, int max, int scale){

   label =new JLabel(s);
  scroll=new JScrollBar(JScrollBar.HORIZONTAL);
  scrollListener=new ScrollAdjustmentListener();
  scroll.addAdjustmentListener(scrollListener);
  int mid=(Math.round((max-Math.abs(min)))/2);
  scroll.setValues(mid,0,min,max+1);
  this.max=max;
  this.min=min;
  this.scale=scale;
  text = new JTextField("0.1",columns);
  textListener=new ATextListener();
  text.addCaretListener(textListener);
  add(label);
  add(text);
  add(scroll); 
  
}/*end constructor*/

public class ScrollAdjustmentListener implements AdjustmentListener{
 public void adjustmentValueChanged(AdjustmentEvent event){
   try{
    float val=(float)(scroll.getValue());
    if (val>max)val=(float)max;
    text.setText(Float.toString(val/scale));
   }catch(Exception e){}
 }/*end adjustmentValueChanged()*/
} /*end inner class ScrollAdjustmentListener*/

public class ATextListener implements CaretListener{
 public void caretUpdate(CaretEvent event){
 
 try{
  text.setBackground(Color.white);
  String textNow=text.getText();
  
    float val=Math.round(Float.valueOf(textNow).floatValue()*scale);
    if (val>max){val=(float)max; text.setText(Float.toString(val/scale));}
    if (val<min){val=(float)min; text.setText(Float.toString(val/scale));}
    scroll.setValue((int)val);
    }
 catch(Exception e){
  text.setBackground(Color.red);
   }
 }/*end textChangedValue*/
} /*end inner ATextListener class*/


} /*end JLabelTextScroll class*/
