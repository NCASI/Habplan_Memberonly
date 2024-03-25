/**
*Draw the yaxis tic marks
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class YTics extends DrawAlongAxis{
  Line2D shape;
 
 public YTics(AGraph ag){
    super(ag); 
    this.shape=new Line2D.Double();
    setTics(3);
    setIsScaleFree(true);       
    setIsXAxis(false);
 }/*end constructor*/
 
/**
*What to draw. Override to draw other shapes
*/
Shape drawThis(double x,double y){
    shape.setLine(x-1,y,x+3,y);
    return shape;
}/*end drawThis()*/

/**
*x coordinates.
@param i only used when x coordinates vary
*/
double xCoordinate(float i){
    return ag.gx;
}/*end xCoordinate()*/


/**
*y coordinates
*@param i only used when y coordinates vary
*/
double yCoordinate(float i){
    return ag.gy+ag.gheight-(double)((i-Min)/(Max-Min))*ag.gheight; 
}/*end yCoordinate()*/


}/*end YTics class*/
