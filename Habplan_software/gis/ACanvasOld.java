package gis;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.font.*;

/**
*ACanvas is  panel class for making a drawing surface
*Just put it in a JFrame or where ever

*/

public class ACanvas extends JPanel{ 
    AffineTransform affineTransform=new AffineTransform();;  //map transform
    double xMin; //map xMin in map coordinates, not screen coordinates
    double yMin; //map yMin, computed in setBounds method
    double xMax; //map xMax
    double yMax; //map yMax
    double mapWidth;//map width in map units
    double mapHeight;
    double width; //width in screen coordinates
    double height;//height in screen coordinates
    double mouseX;//most recent location where user clicked
    double mouseY;
    Vector shapeList=null; //the shapes to be painted. set with setShapeList()
    int shapeType; //one of the ESRI shapeTypes. set with setShapeList() 
    Color[] shapeColor; //color array for shapes. set with setShapeColor()
    Color lineColor=Color.black; //the lineColor
    public Color bgColor=Color.white;
    JLabel clickLabel; //put info here when user clicks on a shape. see setClickLabel()
    String[] drawStringArray=null; //used for popping up info about a mouse location
    RepaintManager rm = null;
     boolean isBuffered=true;
    Rectangle2D clipRect; //used for zooming or clipping
    boolean polygonSelect=false; //set to true so user can pick polygons

     // probably will never handle more than these shape types
    public final static int NULL = 0;
    public final static int POINT = 1;
    public final static int POLYLINE = 3;
    public final static int POLYGON = 5;
    public final static int MULTIPOINT = 8;

    Font font;
  
    public ACanvas(){
	super();
	 rm = RepaintManager.currentManager(this);
	font=new Font("times",Font.PLAIN,12);
	setBackground(bgColor);
	addMouseListener(new MouseAdapter(){
		public void mousePressed(MouseEvent e){
		    Point loc=e.getPoint();
                    mouseX=xMin+loc.x*mapWidth/width;
                    mouseY=yMin+(height-loc.y)*mapHeight/height;
		    polygonSelect=true;
		     repaint();
		}
	    });
    } /*end of constructor*/
   

    /**
     *Set Double Bufferring status.  Takes effect after next repaint()
     */
    public void setDoubleBuffered(boolean isBuffered){
	this.isBuffered=isBuffered;}

   
    /**
     *set a clipping rectangle.  This transforms screen coordinates
     *to map coordinates. So it should be passed a rectangle that the
     *user draws on the screen
     *@param rect a Rectangel2D in screen coordinates
     */
     public void setClipRect(Rectangle2D rect){	
	 double cw=rect.getWidth()*mapWidth/width;
	 double ch=rect.getHeight()*mapHeight/height;
	 double cx=xMin+rect.getX()*mapWidth/width;
	 double cy=yMin+(height-rect.getY())*mapHeight/height;
	 cy=cy-ch;
	 clipRect=new Rectangle2D.Double(cx,cy,cw,ch);
	 setBounds(clipRect);
    }/*end method*/
	 
    /**
     *Paint method
     */
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if(shapeList==null)return;
	makeTransform(g2);
	int colorIndex=0;
        
	for(int i=0;i<shapeList.size();i++){
	    Shape s=(Shape)shapeList.elementAt(i);
	      g2.setPaint(shapeColor[colorIndex]);
	      if(polygonSelect & s.contains(mouseX,mouseY)){
	         g2.setPaint(lineColor.brighter().brighter());
	         polygonSelect=false;
	       if(drawStringArray.length==shapeList.size())
	            clickLabel.setText(drawStringArray[i]);
	      }/*fi*/
	     if(colorIndex<shapeColor.length-1)colorIndex++;
	      g2.fill(s);
	      g2.setPaint(lineColor);
	      g2.draw(s);
	}/*end for i*/  
        rm.setDoubleBufferingEnabled(isBuffered);   
    } /*end paint*/

    /**
    *set a pointer to a label where info can go after a user clicks on
    *a shape.  Called from a controller class, e.g. GISViewer
    *@param clickLabel a JLabel to display text on
    */
     public void setClickLabel(JLabel clickLabel){this.clickLabel=clickLabel;}

    /**
     *set the shapeList vector.  This is what gets painted.
     *@param shapeList the Vector containing the shapes
     *@param shapeType the type of ESRI shape
     */
    public void setShapeList(Vector shapeList, int shapeType){
	this.shapeList=shapeList;
	this.shapeType=shapeType;
    }

    /**
     *set the overall bounds in map units
     *@param rect the bounding box as a rectangle
     */
    public void setBounds(Rectangle2D rect){
	xMin=rect.getX();
	yMin=rect.getY();
	mapWidth=rect.getWidth();
	mapHeight=rect.getHeight();
	xMax=xMin+mapWidth;
	yMax=yMin+mapHeight;
       }/*end method*/


     /**
     *set the color vector for drawing or filling shapes
     *These colors are used sequentially until they are gone.
     *The remaining shapes are painted in the last color.
     *So if there is 1 color then all shapes are the same color.
     *@param shapeColor a Color array
     */
    public void setShapeColor(Color[] shapeColor){
	this.shapeColor=shapeColor;
       }/*end method*/

    /**
     *Make the correct transform before plotting
     *@param g2 a Graphics2D
     */
    public void makeTransform(Graphics2D g2){
	
	Insets insets=this.getInsets();	
	Dimension d=this.getSize();
	 width=d.width-insets.left-insets.right;
	 height=d.height-insets.top-insets.bottom;
	 //affineTransform=new AffineTransform();	
	affineTransform.setToScale(width/mapWidth,-height/mapHeight);
	 affineTransform.translate(-xMin,-yMax);
	g2.setTransform(affineTransform);
        g2.setStroke(new BasicStroke((float)(mapHeight/1000.)));
        
    }/*end method*/

    /**
     *set the string that should be drawn when user clicks on
     *a location
     *@param a String array that corresponds to the shapeList
     */
    public void setDrawStringArray(String[] drawStringArray){
	this.drawStringArray=drawStringArray;
    }
   
    public void setLineColor(Color color){lineColor=color;}
     public Color getLineColor(){return lineColor;}
    public void setPolygonSelect(boolean select){polygonSelect=select;}
    public boolean isPolygonSelect(){return polygonSelect;}
   
}/*end  class*/
