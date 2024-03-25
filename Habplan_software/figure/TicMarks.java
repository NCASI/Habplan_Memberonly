/**
*Draw major and minor ticmarks on the graph
*This class is extended to get x or y tics or grids
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class TicMarks extends GraphComponent{
 AGraph ag;
 Line2D shape;
 float minorSize=4, majorSize=6; //minor and major ticmark lengths
 float ticSpace=10; //distance between  ticmarks 
 float nMinors=1;  //n of Minor tics between each major
 int nTics=4; //n of tic marks, a scale free alternative to giving ticSpace value directly
 boolean isScaleFree=true; //use scale free ticMark spacing if true;
 boolean isXAxis=true; //is this class for the X-axis, otherwise it must be Y-axis
 boolean isBothSides=true; //do axis on both sides, i.e. x or y on both sides
 Color majorColor=Color.magenta, minorColor=Color.cyan;
 
 float Min,Max;
 float [] bounds; /*used in reScale()*/
 
 public TicMarks(AGraph ag){
    super();        
    this.ag=ag;
    shape=new Line2D.Double();
    bounds=new float[4];
 }/*end constructor*/
 
 /**
 *Draw the ticmarks
 */
 public void draw(Graphics2D g2){ 
  double x,y1,y2; //xAxis, locations of yAxes (both ends)
  double axisStart,axisSize; //starting location, width or height of axis
  reScale(); 
  g2.setStroke(stroke);
  if (isScaleFree) ticSpace=(int)Math.round(Max/nTics);
  int skip=(int)Math.round( ticSpace/(nMinors+1) ); //space skipped between tics
  if (isXAxis){
       y1=ag.gy+ag.gheight; //the location of yAxis 1  in AGraph coordinates
       y2=ag.gy;           //yAxis 2
       axisStart=ag.gx;
       axisSize=ag.gwidth;
  } else {
      y1=ag.gx;
      y2=ag.gx+ag.gwidth; //reversed because xAxis is norma cartesian but y is opposite
      axisStart=ag.gy;
      axisSize=ag.gheight;
  }/*end if*/
  int count=0; //count to keep track of minors and majors
  for (int i=skip; i<Max; i+=skip){
    x=axisStart+(double)(i/Max)*axisSize;  
  count++;
   if (count<=nMinors){//its a minor tic
    g2.setColor(minorColor); 
     if (isXAxis){  
    if (isBothSides)shape.setLine(x,y2-(double)minorSize/2,x,y2+(double)minorSize/2);g2.draw(shape);
    shape.setLine(x,y1-(double)minorSize/2,x,y1+(double)minorSize/2);
     } else{ //not xAxis
     if (isBothSides)shape.setLine(y2-(double)minorSize/2,x,y2+(double)minorSize/2,x);g2.draw(shape);
    shape.setLine(y1-(double)minorSize/2,x,y1+(double)minorSize/2,x);
    }/*end if isXAxis*/
   }else{//its a major tic 
     count=0;  
     g2.setColor(majorColor);
       if(isXAxis){   
     if (isBothSides)shape.setLine(x,y2-(double)majorSize/2,x,y2+(double)majorSize/2);g2.draw(shape);
     shape.setLine(x,y1-(double)majorSize/2,x,y1+(double)majorSize/2);
       } else{// not XAxis
	 if (isBothSides)shape.setLine(y2-(double)majorSize/2,x,y2+(double)majorSize/2,x);g2.draw(shape);
        shape.setLine(y1-(double)majorSize/2,x,y1+(double)majorSize/2,x);
       } //end if else isXAxis block
   }/*end if majorTic*/
  g2.draw(shape);
  }/*end for i*/
 }/*end draw()*/

/**
*Used to get x and y mins and maxes from graph
*and decide where the tics go with current scale
*/
public void reScale(){
  bounds=ag.getLineBounds(); 
  if (isXAxis){
   Min=bounds[0];
   Max=bounds[1];
  }else{ 
  Min=bounds[2];
  Max=bounds[3];  
  }/*end if*/
}/*end of reScale*/

}/*end TicMarks class*/
