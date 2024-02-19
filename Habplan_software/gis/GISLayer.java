/**
 *GISLayer class holds the ESRI components for 1 layer of
 *a particular GIS project
 *@author Paul Van Deusen 3/2001
 */

package gis;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class GISLayer{
    protected static int nLayers=0;//count n of layers
    public Vector shapeList; //the items read
    public int shapeType;  //the ESRI shapeType
    protected Rectangle2D.Double bounds; //bounding box
    public BasicTable colorTable;
    public JFrame parent;
    public GISLayer(int nShapes){
	nLayers++;
	shapeList=new Vector(nShapes);
    }/*end constructor*/

    public GISLayer(JFrame parent){
     this(200);
     this.parent=parent;
     colorTable=new BasicTable(parent,"Color Table: Layer("+nLayers+")");
    }//make a shapeList of size 200

    /**
     *Read the shapeFile data to fill the shapeList Vector
     *@param fileName the ESRI ShapeFile name
     *@return the number of items put into shapeList (-1) if failed
     */
    public int readData(String fileName){
	try{
	ShapeFileRead shr=new ShapeFileRead(this);
        shr.readData(fileName);
	return shapeList.size();
	}catch(Exception e){return -1;}
    }/*end method*/

    /**
     *@return A Rectangle2D.Double for the bounding box
     */
    public Rectangle2D getBounds(){return bounds;}
      
    /**
     *Make a Rectangle2D.Double for the bounding box
     *set during data reading
     */
    public void setBounds(double xMin,double yMin,double xMax,double yMax){
	 bounds = new Rectangle2D.Double(xMin,yMin,xMax-xMin,yMax-yMin);
    }/*end method*/
    public int getNLayers(){return nLayers;}
    /**
     *Get the colors from the color table
     */
    public Color[] getShapeColor(){
	return colorTable.data.color;
    }
   
   
}/*end class*/
