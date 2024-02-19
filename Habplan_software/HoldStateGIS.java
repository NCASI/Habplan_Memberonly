import java.awt.*;

/**
*Hold the current settings and state of a Habplan GISViewer.  
*See GISViewer.captureSettings()
*/
class HoldStateGIS implements java.io.Serializable{
    
    String shapeFileName=null; //the shapefile
    Rectangle bounds; //the viewer bounds
    Color lineColor=Color.black; //canvas line color
    Color bgColor=Color.white; //canvas bgColor;
    Color[] regimeColor={Color.red,Color.green};
    int sliderValue=0; //the delay time for dynamic display
     String[] polygonColumnNames={"Color","Polygon"};
     Color[] polygonColor={Color.red,Color.green}; //hold polygon colors
     String[][] polygonColorData=new String[1][1];//hold polygonColorTable data
    //vars that might be used later follow
    //these don't appear in GISViewer.captureSettings now
    //they can be used later without ruining saved holdStateGIS objects
     int drawIter=0; //how often to draw, not saved or restored
     String[][] regimeColorData=new String[1][1];//hold regimeColorTable data
     String colorMethod="polygon";//to reset the coloring method

    public HoldStateGIS(){ //make some default values
	bounds=new Rectangle(350,50,600,400);
    }/*end constructor*/

  
}/*end class*/
