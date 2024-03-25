/**
*Draw X axis tic marks along the axis
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class XTics extends DrawAlongAxis{  
  Line2D shape;
  
 public XTics(AGraph ag){
    super(ag); 
    this.shape=new Line2D.Double();
    setTics(10);
    setIsScaleFree(false);       
    setIsXAxis(true);
 }/*end constructor*/
 
/**
*What to draw
*/
Shape drawThis(double x,double y){
    shape.setLine(x,y-3,x,y+1);
    return shape;
}/*end drawThis()*/

/**
*x coordinates
*/
double xCoordinate(float i){
    return ag.gx+(double)((i-Min)/(Max-Min))*ag.gwidth;
}/*end xCoordinate()*/

/**
*y coordinates
*/
double yCoordinate(float i){
    return ag.gy+ag.gheight; 
}/*end yCoordinate()*/


}/*end  class*/
