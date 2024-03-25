/**
*Draw labels for lines on the graph
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

class LineLabel extends GraphComponent{
 AGraph ag; //reference to containing Agraph
 GraphComponent gcomp; //reference to associated GraphComponent
 float [] bounds; /*used in reScale()*/
 String label; //a vector of labels
 float xUser,yUser,x,y;  //coordinates as specified by the user, and scaled versions
 float xMin,xMax,yMin,yMax;
 Line2D line; 
 
 public LineLabel(AGraph ag){
    super();        
    this.ag=ag;
    this.gcomp=gcomp;
    bounds=new float[4];   
    line=new Line2D.Float();
 }/*end constructor*/
 
 /**
 *Draw the labels
 */
 public void draw(Graphics2D g2){ 
  if (!getDrawState())return; 
  reScale();
 //  g2.setPaint(color);
  // g2.setStroke(stroke);
  //the stroke and color don't need to be set as long as label is drawn immediately after its line
  g2.draw(line);
  g2.drawString(label,(int)x,(int)y);
 }/*end draw()*/
 
/**
*make the label.
*/
public void label(String label, double percentMaxX, double percentMaxY){
    this.label=label;
    xUser=(float)percentMaxX;
    yUser=(float)percentMaxY; //this is entered as a percent of maximum current y
}/*end label*/


/**
*Used to get x and y mins and maxes from graph
*and decide where the tics go with current scale
*/
public void reScale(){
  float[] bounds=ag.getLineBounds(); 
   xMin=bounds[0];
   xMax=bounds[1];
  yMin=bounds[2];
  yMax=bounds[3]; 
  double textHeight=ag.getTextHeight();
  double lineLength=.05f*ag.gwidth;
  y=(float)(ag.gy+ag.gheight-yUser*ag.gheight);
  x=(float)(ag.gx+xUser*ag.gwidth);
  line.setLine(x,y-textHeight/2,x+lineLength,y-textHeight/2);
  x+=1.5f*lineLength;  
}/*end of reScale*/

}/*end of class*/
