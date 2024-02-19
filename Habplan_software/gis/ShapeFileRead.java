/** ShapeFileRead.java
 * Read ESRI Shapefiles according to Version 1000 description
 *
 * @author Paul Van Deusen
 * @version 1  only reads polygons
 */

package gis;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class ShapeFileRead{

 public final static int SHAPEFILE_CODE = 9994; // shapefile magic number

 // probably will never handle more than these shape types
    public final static int NULL = 0;
    public final static int POINT = 1;
    public final static int POLYLINE = 3;
    public final static int POLYGON = 5;
    public final static int MULTIPOINT = 8;

   
     BeLeDataInputStream in;
     //Main File Header variables
    int fileLength;
    int shapeType;
     double xMin,yMin,xMax,yMax; //hold bounds
    GISLayer parent; //the layer thats calling this

    public ShapeFileRead(GISLayer parent){
	this.parent=parent;
    }/*end constructor*/

    /**
     *read the Main File Header.
     *Check the Version and get the Bounding Box.
     */
    public  void readData(String fileName){
	try{
	     URL location;
             if(fileName.startsWith("http"))
	      in=new BeLeDataInputStream(new URL(fileName));
	     else in=new BeLeDataInputStream(fileName);
	    int fileCode = in.readInt(); //read bytes 0-3
	    if (fileCode != SHAPEFILE_CODE) {
		throw (new IOException("Not a shapefile")); }
	    in.skipBytes(20);           //skip up thru byte 23
            fileLength=2*in.readInt();    //read bytes 24-27
            int version=in.readLEInt();    //read bytes 28-33 as little endian
	    if(version!=1000){
		throw (new IOException("Not a version 1000 shapefile")); }
	    shapeType=in.readLEInt();  //the rest are little endian
            //get 2d bounding box
		xMin=in.readLEDouble();
		yMin=in.readLEDouble();
		xMax=in.readLEDouble();
		yMax=in.readLEDouble();   
	    parent.setBounds(xMin,yMin,xMax,yMax);
	    parent.shapeType=shapeType; //record the shapeType in GISLayer
	    in.skipBytes(32);  //go to start of first record header

	    switch(shapeType){
	case NULL:		       
	    ;  //just fall thru until we've made a class to handle this
        case POLYGON:
	    new EsriPolygon(this); break;
	default:
	    throw new IOException("We don't do shapefile shape type " + shapeType);
	    }/*end switch*/


	}catch(Exception e){System.out.println("Error Reading Main Header: "+e);}
	
    }/*end method*/

    /**
     *Add shapes to the shapeList vector.
     *This is called as shapes are read, e.g. by an EsriPolygon instance.
     */
    public void addShape(Shape shape){
	parent.shapeList.addElement(shape);
    }/*end method*/


} /*end class*/
