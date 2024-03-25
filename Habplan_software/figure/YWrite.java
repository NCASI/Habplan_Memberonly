/**
*Write something along the x axis
*override drawThis(), xCoordinates(), and yCoordinates
*reset isScaleFree, isXaxis, and change tics
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.font.*;

public class YWrite extends WriteAlongAxis{
 
 public YWrite(AGraph ag){
    super(ag);    
    setIsXAxis(false);
    setIsScaleFree(true);
    setTics(3);    
    
 }/*end constructor*/
 
 /**
 *Override draw to indicate level of exponentiation
 */
 public void draw(Graphics2D g2){ 
 super.draw(g2);
 int pow10=(int)Math.pow(10,ag.getYExponent());
 if (pow10>1) g2.drawString("x"+pow10,(int)ag.gx,(int)ag.gy-1);
 }
 
/**
*x coordinates
*/
float xCoordinate(float i){    
    return (float)(ag.left);
}/*end xCoordinate()*/

/**
*y coordinates
*/
float yCoordinate(float i){
    return (float)(ag.gy+ag.gheight-(double)((i-Min)/(Max-Min))*ag.gheight+ag.getTextHeight()/2); 
}/*end yCoordinate()*/


}/*end class*/
