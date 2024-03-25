/**
*Draw the yaxis tic marks
*/
package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class YGrid extends YTics{
 
 public YGrid(AGraph ag){
    super(ag); 
    setTics(3);
    setStroke(dotted);
 }/*end constructor*/
 
/**
*What to draw. Override to draw other shapes
*/
Shape drawThis(double x,double y){
    shape.setLine(x,y,x+ag.gwidth,y);
    return shape;
}/*end drawThis()*/


}/*end class*/
