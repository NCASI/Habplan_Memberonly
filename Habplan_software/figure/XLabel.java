/**
*Draw X axis tic marks along the axis
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class XLabel extends DrawAlongAxis{
    
 
 public XLabel(AGraph ag){
    super(ag); 
    setTics(10);
    setIsScaleFree(false);       
    setIsXAxis(true);
 }/*end constructor*/
 
/**
*What to draw
*/
Shape drawThis(double x,double y){
    
    return null;
}/*end drawThis()*/

/**
*x coordinates
*/
double xCoordinate(int i){
    return ag.gx+(double)(i/Max)*ag.gwidth;
}/*end xCoordinate()*/

/**
*y coordinates
*/
double yCoordinate(int i){
    return ag.gy+ag.gheight; 
}/*end yCoordinate()*/


}/*end  class*/
