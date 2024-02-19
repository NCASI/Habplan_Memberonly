/** EsriPolygon.java
 *This class reads Esri polygon data from a shapefile and 
 *contains a method to convert it to a java.awt.shape
 *and then adds it to a vector of shapes in ShapeFileRead class
 *@author Paul Van Deusen
 *@version 1
 */

package gis;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class EsriPolygon{

  //Main File Record variables
    int recordNumber=0;
    int recordLength;
    int shapeType;
    int totalShapes; //keep track of how many shapes were read
    ShapeFileRead shr;  //pointer to the calling instance of this class
    double[] box=new double[4]; //hold Xmin,Ymin,Xmax,Ymax
    int numParts;
    int numPoints;
    int[] parts;
    Point[] point;

    public EsriPolygon(ShapeFileRead shr){
     this.shr=shr;
	try{
	    readData();
	}catch(Exception e){System.out.println("Error constructing EsriPolygon: "+e);}
    }/*end constructor*/

    /**
     *Read the data for this polygon.
     *Assumption is that your positioned in the file in the correct location
     */
protected void readData(){
             int dSize=1000; //size of d vector, to reduce reInstantiation of d
	     double[] d=new double[dSize]; //read x,y pairs into this
	try{
	    while(recordNumber>=0){//keep reading until end of file
		try{recordNumber=shr.in.readInt();
		}catch(EOFException e){recordNumber=-1;continue;}
		totalShapes=recordNumber; //will hold total read at end
	    recordLength=shr.in.readInt();
	    shapeType=shr.in.readLEInt();
	    if(shapeType!=shr.shapeType)
		throw (new IOException("Not the right shape"));
	    for(int i=0;i<box.length;i++)
		box[i]=shr.in.readLEDouble();
	    numParts=shr.in.readLEInt();
	    numPoints=shr.in.readLEInt();
	    int[] part = new int[numParts+1]; //index of point 1 in each part
	    for(int i = 0; i < numParts; i++)
		part[i] = shr.in.readLEInt();
	    part[numParts]=numPoints;  //make it easy to read the points

	    //now read the points and create a GeneralPath shape
             GeneralPath gp=new GeneralPath(GeneralPath.WIND_NON_ZERO);
	     // System.out.println("RecordNumber "+recordNumber);

            float ptx, pty;
	    for (int p=1;p<=numParts;p++){
		ptx=(float)shr.in.readLEDouble();
                pty=(float)shr.in.readLEDouble();
		gp.moveTo(ptx,pty);
            int nRead=2*(part[p]-part[p-1]-1);
            if (nRead>dSize){// make d bigger if needed
		dSize=nRead;
                d=new double[dSize];
	    }/*fi*/
            
	    shr.in.readLEDoubles(d,nRead);
	   
	    for(int i=0;i<nRead;i+=2){
		ptx=(float)d[i];
                pty=(float)d[i+1];
		 gp.lineTo(ptx,pty);
		}/*end for i*/
	    }/*end for p*/
	   
	    //add this shape to shapeList;
	     shr.addShape(gp);
	   
	    }/*end while loop*/
		
	}catch(Exception e){System.out.println("EsriPolygon: "+e);}
    }/*end method*/
}/*end class*/
