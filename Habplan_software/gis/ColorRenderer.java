/**
 *Renders the color column of a BasicTable color table
 *returns a Panel with the desired background color
 */

package gis;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

class ColorRenderer extends JPanel 
    implements TableCellRenderer{

    public Component getTableCellRendererComponent(
						  
						   JTable table, Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row, int col){
        String rgb=null;

	try{
	    Color color=(Color)value;   
	    setBackground(color);
	    rgb=color.getRed()+","+color.getGreen()+","+color.getBlue();
	    setToolTipText(rgb);
	}catch(Exception e){System.out.println("ColorRenderer Error: rgb "+rgb+" "+e);}
	return this;
    }/*end method*/



}/*end class*/
