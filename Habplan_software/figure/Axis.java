/**
*Axis will draw the axis and the axis labels and determine
*How much room is left inside the axis for the graph
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class Axis extends GraphComponent{
 AGraph ag;
 Color bgColor=Color.black;
 Rectangle2D shape;
 
 public Axis(AGraph ag){
    super();        
    this.ag=ag;
    shape=new Rectangle2D.Double();
 }/*end constructor*/
 
 /**
 *Draw this axis
 */
 public void draw(Graphics2D g2){
  reScale();
  g2.setPaint(bgColor);
  g2.fill(shape);   
  g2.setPaint(color);
  g2.setStroke(stroke);
  g2.draw(shape);
 }/*end draw()*/

public void reScale(){ 
  shape.setRect(ag.gx, ag.gy, ag.gwidth, ag.gheight);
}/*end reScale*/

 /**
*Set the background color of this component
*/  
public void setBackground (Color bgColor){
    this.bgColor=bgColor;
}/*end setBackground()*/
 					       						       
}/*end Axis class*/
