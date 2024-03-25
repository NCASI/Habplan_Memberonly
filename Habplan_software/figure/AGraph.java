package figure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.font.*;

/**
*AGraph is my crude panel class for making a 2d graph.
*Just put it in a JFrame or whereever
*This is an improvement over the original AGraph written in 11/97
*AGraph holds a Vector of component objects that go on the graph.
*Associated classes  represent the graphical components, like Axis
*@author Paul Van Deusen (NCASI) 11/99
*/

public class AGraph extends JPanel{ 
  Color bgColor=Color.darkGray;
  FontRenderContext frc;  //for figuring out the size of the text
  Rectangle2D textSize;  //used for holding size of text
  float left=0,right=0,top=0,bottom=0; //initial offsets
  double gx=2,gy=2,gwidth=100,gheight=100; //initial dimensions of drawing rectagle;
  double textHeight=0,textWidth=0; //useful for drawing below xaxis
  Vector grafItem; //holds the graphical components
  public Axis ax; 
  public YTics yTics;
  public XTics xTics;
  public XGrid xGrid;
  public YGrid yGrid;
  public XWrite xWrite;
  public YWrite yWrite;
  private int maxLines=1; //maximum number of lines allowed for this graph
  Line[] line;
  LineLabel[] lineLabel;
  protected int nLines=0; //number of lines currently on the graph
    protected float xMin=Float.MAX_VALUE, xMax=-xMin,
  yMax=xMax,yMin=xMin; //overall mins and maxs for all lines on graph
  protected float yMinUser=Float.NaN, //user specified yAxis limits
		yMaxUser=Float.NaN;
  final float maxYAxis=1000000; //after this size then use exponential notation for y axis labeling
  final static double eTo10=1/Math.log(10.); //used to convert natural log to log10 
  Font font;
    Figure parent;
  
   AGraph(Figure parent, int maxLines){
      super();
      //setBorder(BorderFactory.createLineBorder(Color.black,2));//purely for looks 
      this.parent=parent;
      font=new Font("times",Font.PLAIN,12);
      setBackground(bgColor);
      this.maxLines=maxLines;
      grafItem = new Vector();
      ax=new Axis(this); //assume every graph needs these components, so creat them now
      yTics=new YTics(this);//assume it needs tickmarks too
      yTics.setColor(Color.white);
      xTics=new XTics(this);
      xTics.setColor(Color.white);
      xGrid=new XGrid(this);
      xGrid.setStroke(GraphComponent.dotted);
      yGrid=new YGrid(this);
      yGrid.setStroke(GraphComponent.dotted);
      xWrite=new XWrite(this); //writing grafItems are added in plot() method
      yWrite=new YWrite(this);
      lineLabel=new LineLabel[maxLines+1];
      line=new Line[maxLines+1];
      for (int i=1;i<=maxLines;i++){
	  line[i]=new Line(this);
	  lineLabel[i]=new LineLabel(this);
      }/*end for i*/
  } /*end of constructor*/
	 
/**
*Paint method
*/
public void paintComponent(Graphics g) {
	super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	g2.setFont(font);
	frc=g2.getFontRenderContext();
	
	textSize = g2.getFont().getStringBounds(String.valueOf((int)xMax),frc);
	textWidth=textSize.getWidth(); //use elsewhere to position x axis labels
	textSize = g2.getFont().getStringBounds(String.valueOf((int)xMin),frc);
	double xMinWidth=textSize.getWidth();
	
	String test=String.valueOf((int)yMax);
	if (yMax>maxYAxis) test=String.valueOf((int)(maxYAxis-1));//go to exponential if true
	textSize = g2.getFont().getStringBounds(test,frc);
	textHeight=textSize.getHeight();
	
	Insets insets=getInsets();
	left=insets.left;
	right=insets.right;
	top=insets.top+1.1f*(float)textHeight+1;
	bottom=insets.bottom+1.4f*(float)textHeight+1; //the 1.4f interacts with yCoordinate() in XWrite
	Dimension d=getSize();
	gx=left+xMinWidth+(float)textSize.getWidth(); //where xaxis starts in userspace
	gy=top; //yaxis starts here
	gwidth=d.width-gx-right-1.2f*xMinWidth;
	gheight=d.height-top-bottom;
	
	for (int i=0; i<grafItem.size(); i++){
	 GraphComponent item=(GraphComponent) grafItem.elementAt(i); 
	  item.draw(g2);  	  
	}/*end for i*/
} /*end paint*/

/**
*Add the standard graph components, i.e. axis, tics, labels, etc
*/
public void addStandardComponents(){
      grafItem.add(ax);  //add grafItems that are always wanted
      grafItem.add(yGrid);
      grafItem.add(yTics);
      grafItem.add(xGrid);
      grafItem.add(xTics);
      grafItem.add(xWrite);
      grafItem.add(yWrite);   
}/*end drawNow()*/

/**
*Add another line to graph that already has at least 1 line
**/
public void plotHold(float[] x, float[] y, String label, double labx, double laby){ 
 nLines++;
 if (nLines>maxLines){System.out.println("\nExceeded allowed lines for this graph\n"); System.exit(0);}
 line[nLines].plot(x,y,false);
 lineLabel[nLines].label(label,labx,laby);
 grafItem.add(line[nLines]);
 grafItem.add(lineLabel[nLines]);
}/*end plotHold()*/

/**
*Add 1 line to graph.  Clear any thing there previously
**/
public void plot(float[] x, float[] y, String label, double labx, double laby){
 nLines=1;
 grafItem.removeAllElements();
 addStandardComponents();
  line[nLines].plot(x,y,true);
  lineLabel[nLines].label(label,labx,laby);
 grafItem.add(line[nLines]);
 grafItem.add(lineLabel[nLines]);
}/*end plot()*/

/**
*Called by YWrite to properly exponentiate the yAxis.
*This keeps numbers greater than maxYAxis from taking up to much space
*/
public int getYExponent(){
   int pow10=0;
  if (yMax>maxYAxis){
    pow10=(int)Math.ceil(Math.log(yMax/maxYAxis)*eTo10);
    pow10=(int)Math.max(pow10,1);
  }/*end if(ymax>=maxYAxis*/   
 return pow10;
}/*end getYExponent()*/


/**
*Called by line objects to keep track of xMin,xMax,yMin,yMax
@param isPlot=true means that its the first line for this graph and way off mins and maxes should be adjusted
*/
public void setLineBounds(float x1Min, float x1Max, float y1Min, float y1Max, boolean isPlot){
    final double yFactor=0.3; //the slop factor for yaxis rescaling
  if(isPlot){
    if(Math.abs((yMin-y1Min)/(y1Min+.001))>yFactor)yMin=y1Min; //change if old Min is far off
    if(Math.abs((yMax-y1Max)/(y1Max+.001))>yFactor){
	yMax=y1Max; //change if old Max is far off
    }
    unSetXAxis(); //ensure xAxis is OK if user reads in new data set with less x-range
  }/*end if isPlot*/ 
  yMin=(y1Min<yMin)?y1Min:yMin; 
  yMax=(y1Max>yMax)?y1Max:yMax;
  xMax=(x1Max>xMax)?x1Max:xMax;	    
  xMin=(x1Min<xMin)?x1Min:xMin;
  if (!Float.isNaN(yMinUser)){//user specifies yAxis limits
      yMax=(yMaxUser>yMax)?yMaxUser:yMax;
      yMin=(yMinUser<yMin)?yMinUser:yMin;
    } 
}/*end setLineBounds*/

/**
*Allow the user to set the scale of the yAxis.
*/
public void setYAxis(float min, float max){
    yMinUser=min;
    yMaxUser=max;
    setLineBounds(xMin,xMax,yMin,yMax,true);
    repaint();
}/*end setYAxis*/

/**
*Allow the user to UNset the scale of the yAxis.
*/
public void unSetYAxis(float min, float max){
    yMinUser=Float.NaN;
    yMaxUser=Float.NaN;
    yMin=Float.MAX_VALUE;
    yMax=-yMin;
    Line l;
    for (int i=0;i<grafItem.size();i++)
     if (grafItem.elementAt(i) instanceof Line){
	 l=(Line)grafItem.elementAt(i);
	 setLineBounds(l.xMin,l.xMax,l.yMin,l.yMax,true);
     }
    repaint();
}/*end setYAxis*/

/**
*reInitialize the x Axis
*This is called when the user reads in a new dataset
*to ensure that the xMin and xMax are properly rescaled
*/
public void unSetXAxis(){
    xMin=Float.MAX_VALUE;xMax=-xMin;
}/*end unSetXAxis*/

/**
*Called by TickMark objects to keep track of xMin,xMax,yMin,yMax
*@param: float[4] bounds is passed to hold the returned values. Position 0 is xMin,...
*/
public float[] getLineBounds(){
    float[] bounds={xMin,xMax,yMin,yMax};
    return bounds;
}/*end getLineBounds*/

/**
*get textHeight
*/
public double getTextHeight(){return textHeight;}

/**
*get some info on x-axis textWidth
*/
public double getTextWidth(){return textWidth;}

/**
*Append the raw y data to the user specified file
*this could slow things down since it opens and closes
*the file each time.  Purpose is to allow user to make fancy
*publication quality graphs with other software
*/
 public void append(String file){
  
  RandomAccessFile fout=null;
   try {
    fout =  new RandomAccessFile(file,"rw");
    fout.seek(fout.length());
    Line l;
    for (int i=0;i<grafItem.size();i++)
     if (grafItem.elementAt(i) instanceof Line){
	 l=(Line)grafItem.elementAt(i);
     for (int j=1;j<l.yy.length;j++){
      fout.writeBytes(String.valueOf(l.yy[j]));
      fout.writeBytes(" ");
      } /*end for j*/
      fout.writeBytes("\n");
     }/*end if instance of Line*/ 
     
 }/*end try*/
  catch(Exception e){
   System.out.println("Error saving graph data to file " + file);
   System.out.println(e.toString());
  } /*end catch*/
   finally { try { if (fout != null) fout.close(); } catch (IOException e) {} }      
 } /*end append(y)*/  



}/*end AGraph class*/
