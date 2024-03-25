/**
*Line will create a line component for the graph
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class Line extends GraphComponent{
 AGraph ag;
 float xMin,xMax,yMin,yMax; //min and max values of line
 float[] xx,yy,xs,ys;  //hold the coordinates for this line, and scaled coordinates
 int npts=0;  //length of line
 Line2D shape;
 
 public Line(AGraph ag){
    super();        
    this.ag=ag;
    shape=new Line2D.Float();
 }/*end constructor*/
 
 
 /**
 *Draw this line.  
 */
 public void draw(Graphics2D g2){
  reScale();   
  g2.setStroke(stroke);
  g2.setPaint(color);
  for (int i=2;i<=npts;i++){
    shape.setLine(xs[i-1],ys[i-1],xs[i],ys[i]);
    g2.draw(shape);
  }/*end for i*/ 
 }/*end draw()*/

/**
*Store the x,y vectors and compute min and max for x and y
*x and y will be rescaled at plot time
*/
public void plot(float[] x, float[] y,boolean isPlot){
 xMin=Float.MAX_VALUE;xMax=-xMin;
 yMin=Float.MAX_VALUE;yMax=-yMin;  
 if (npts<x.length-1){ //only make new pt objects when needed
   xx=new float[x.length]; yy=new float[x.length];
   xs=new float[x.length]; ys=new float[x.length];
  }/*end if*/
 npts=x.length-1;
 if (y.length!=x.length)System.out.println("ERROR: Length of x and y vectors should be equal for plotting!");
 for (int i=1;i<x.length;i++){
    xx[i]=x[i]; yy[i]=y[i];
    yMin=(y[i]<yMin?y[i]:yMin); 
    xMin=(x[i]<xMin?x[i]:xMin);
    yMax=(y[i]>yMax?y[i]:yMax);
    xMax=(x[i]>xMax?x[i]:xMax);
 }/*end for i*/

 if (yMin==yMax)yMax=1.0001f*yMin+.001f; //rescale() doesn't work if yMin=Ymax
 ag.setLineBounds(xMin,xMax,yMin,yMax,isPlot); //keep AGraph current on mins and maxes
}/*end plot()*/

public void reScale(){

   float [] bounds=ag.getLineBounds();   
   //get adjusted ht and YIntercept of this equation
  float ht=(float)ag.gheight*(yMax-yMin)/(bounds[3]-bounds[2]),
       YIntercept=(float)(ag.gy+ag.gheight*(1-(yMin-bounds[2])/(bounds[3]-bounds[2])));

   for (int i=1; i<=npts; i++){
    xs[i]=(float)ag.gx+(xx[i]-xMin)/(xMax-xMin)*(float)ag.gwidth;
    ys[i]=(float)(YIntercept-(yy[i]-yMin)/(yMax-yMin)*ht);
   }/*end for i*/
}/*end reScale*/
 					       						       
}/*end Axis class*/
