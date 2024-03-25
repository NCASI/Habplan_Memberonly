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

public class XWrite extends WriteAlongAxis{
 
 public XWrite(AGraph ag){
    super(ag);    
    setIsXAxis(true);
    setIsScaleFree(false);
    setTics(10);    
    
 }/*end constructor*/
 
 
/**
*x coordinates
*/
float xCoordinate(float i){    
    return (float)(ag.gx+(double)((i-Min)/(Max-Min))*ag.gwidth-ag.getTextWidth()/2);
}/*end xCoordinate()*/

/**
*y coordinates
*the 1.3f interacts with bottom variable in AGraph.
*even though thats a poor design
*/
float yCoordinate(float i){
    return (float)(ag.gy+ag.gheight+1.3f*ag.getTextHeight()); 
}/*end yCoordinate()*/


}/*end class*/
