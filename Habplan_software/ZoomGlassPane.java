/**
*a custom glass pane to allow for zooming.  The lets the user draw a rectangle
*over the mapped image
*@author Paul Van Deusen March 2001
*/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;

public class ZoomGlassPane extends JPanel{
      Color lineColor;
     GISViewer parent;
     Point start=new Point(); //start point of zoom rectangle
     Point finish=new Point(); //finish point of rect
     Rectangle2D rect=new Rectangle2D.Double(-1,-1,0,0);;

    public ZoomGlassPane(GISViewer parent){
	this.parent=parent;
	JLabel label=new JLabel("ZOOM MODE");
	add(label);
	setOpaque(false);
	DragRectangle dragRectangle=new DragRectangle();
	addMouseListener(dragRectangle);
	addMouseMotionListener(dragRectangle);
    }/*end constructor*/
    
    class DragRectangle extends MouseAdapter
	    implements MouseMotionListener{
	boolean dragging=false; //indicates mouse down dragging
	boolean wasDragged=false;//indicates dragging has occurred
	
	public void mousePressed(MouseEvent e){
            if(!wasDragged)rect.setRect(-1,-1,0,0);//indicates not to zoom
	    if(SwingUtilities.isRightMouseButton(e))parent.zoomDisplay(rect);
	    start.x=e.getX();
	    start.y=e.getY();
	    dragging=true;
	    wasDragged=false;
	    finish.x=start.x; //erase any old rectangle
	    finish.y=start.y;	   
	      repaint();
	}
	    
	public void mouseReleased(MouseEvent e){
	    dragging=false;
	}    
	
	public void mouseClicked(MouseEvent e){
	    dragging=false;
	    //wasDragged=false;
	}    
	public void mouseDragged(MouseEvent e){
	    if(dragging){
		wasDragged=true;
		finish.x=e.getX();
		finish.y=e.getY();                     
                 repaint();		
	    }/*fi*/
	}   
	    
	public void mouseMoved(MouseEvent e){
	    //don't care
	}        
	
    }/*end inner class*/
    
     public void paintComponent(Graphics g) {
	 // super.paintComponent(g);  //not Opaque, so don't need to call
        Graphics2D g2 = (Graphics2D) g;
	double xMin=start.x;
	double yMin=start.y;
	double w=finish.x-start.x;
	double h=finish.y-start.y;
        rect.setRect(xMin,yMin,w,h);
	g2.setXORMode(Color.cyan);
       g2.setPaint(lineColor);
       g2.draw(rect);
    } /*end paint*/
    
    public void setLineColor(Color color){lineColor=color;}
}/*end class*/
