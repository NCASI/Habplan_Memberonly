/**
*Draw X grid marks along the axis
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class XGrid extends XTics{  
 // Line2D shape;
  
 public XGrid(AGraph ag){
    super(ag); 
    setTics(5);
    setStroke(dotted);
 }/*end constructor*/
 
/**
*What to draw
*/
Shape drawThis(double x,double y){
    shape.setLine(x,y,x,y-ag.gheight);
    return shape;
}/*end drawThis()*/


}/*end  class*/
