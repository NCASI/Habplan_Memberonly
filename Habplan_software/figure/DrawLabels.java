/**
*Draw labels on the graph
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

class DrawLabels extends GraphComponent{
 AGraph ag;
 float [] bounds; /*used in reScale()*/
 String label; //a vector of labels
 float xUser,yUser,x,y;  //coordinates as specified by the user, and scaled versions
 float xMin,xMax,yMin,yMax;
 
 
 public DrawLabels(AGraph ag){
    super();        
    this.ag=ag;
    bounds=new float[4];   
 }/*end constructor*/
 
 /**
 *Draw the labels
 */
 public void draw(Graphics2D g2){ 
  reScale();
  g2.setPaint(color);
  g2.drawString(label,x,y);
 }/*end draw()*/
 
/**
*make the label.
*/
public void label(String label, Color color, float x, float y){
    this.label=label;
    this.color=color;
    xUser=x;
    yUser=y;
}/*end label*/


/**
*Used to get x and y mins and maxes from graph
*and decide where the tics go with current scale
*/
public void reScale(){
  bounds=ag.getLineBounds(); 
   xMin=bounds[0];
   xMax=bounds[1];
  yMin=bounds[2];
  yMax=bounds[3]; 
  y=(float)(ag.gy+ag.gheight-(yUser-yMin)/(yMax-yMin)*ag.gheight);
  x=(float)(ag.gx+(xUser-xMin)/(xMax-xMin)*ag.gwidth);
   
}/*end of reScale*/

}/*end of class*/
