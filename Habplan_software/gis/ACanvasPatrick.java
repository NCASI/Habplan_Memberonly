import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import com.svcon.jdbf.*;
import java.net.*;
//import java.awt.font.*;

/**
*ACanvas is  panel class for making a drawing surface
*it can be placed in any Container-type class
*/
public class ACanvas extends Canvas{ 
    /** 
     *Number of layers 
     */
    public int nLayers=1;
    /** 
     *Array of layers 
     */
    public ShapeFileReader[] layer;
    /** 
     *Bounds of the map
     */
    double[] bounds=new double[4];
    /** 
     *Map background color 
     */
    public Color bgColor=Color.black;
    /** 
     *Selection mode indicator color 
     */
    public Color selectColor=Color.gray;
    /** 
     *Color of user created polygons 
     */
    public Color userPolyColor=Color.red;
    AffineTransform affineTransform;  //set up the correct transform
    double xMin; //map xMin in map coordinates, not screen coordinates
    double yMin; //map yMin, computed in makeTransform() method
    double xMax; //map xMax
    double yMax; //map yMax
    double mapWidth;//map width in map units
    double mapHeight;
    double width; //width in screen coordinates
    double height;//height in screen coordinates
    double mouseX;//most recent location where user clicked
    double mouseY;
    Rectangle2D.Double clipRect;
    boolean polygonSelect=false; //set to true so user can pick polygons
    boolean polygonUserSelect=false;
    boolean polygonDraw=false;  //set to true to draw a polygon
    boolean circleDraw=false;
    boolean zoom=false;
    boolean layerPolygonSelect=false;
    /**
     *Temporarily hold user constructed polygon as they are built
     */
    public GeneralPath polyHold=new GeneralPath();
    /**
     *Hold user constructed polygons and circles
     **/
    public Vector userPolygon=new Vector();
    Vector userShapeTracker=new Vector(); //Keeps track of user shapes in a serializable format
    float[][] polyCoords = new float[2][100]; //holds up to 100 polygon coordinates
    int numPolyCoords = 0;  // track the number of coordinates in a user polygon
    int circleCode = 1; // indicator codes for shape type in userShapeTracker
    int polyCode = 0;
    double circleRad=0;
    JFrame circleData = new JFrame("");
    JFrame drawChoice = new JFrame("");
    JFrame instructionFrame = new JFrame("");
    boolean circleClick; //has the first point been clicked to draw a circle?
    double centerX, centerY;  //temp variables to hold circle points
    double currentBoundsXmin, currentBoundsXmax, currentBoundsYmin, currentBoundsYmax, 
	currentBoundsW, currentBoundsH; //keep track of current bounds
    boolean circleClickAllow=false; //let's the user click a circle
    int national=-1;
    int stateHolder=-1;
    Object [][] natData = new Object[100][100]; //holds national map dbf data
    String counties=""; // County shape file
    // Feature layer indicators
    boolean waterLoaded=false, shedLoaded=false, roadLoaded=false;
    // Feature Layer access
    ShapeFileReader waterLayer, shedLayer, roadLayer;
    int stateCode;
    String layerItem=null;
    JButton okBut = new JButton("Okay"), okBut2 = new JButton("Okay");

    public ACanvas(int typeCode){
	super();
	national=typeCode;	
	setBackground(bgColor);
	currentBoundsXmin=xMin;
	currentBoundsYmin=yMin;
	currentBoundsXmax=xMax;
	currentBoundsYmax=yMax;
	currentBoundsW=mapWidth;
	currentBoundsH=mapHeight;

	// If it is a national map, get the dbf info for that map
	if(national>0){
	    try{
		DBFReader natDataReader = new DBFReader(new URL("http://ncasi.uml.edu/projects/FIA/states.dbf").openStream());
		int i=0;
		while(natDataReader.hasNextRecord()){
		    natData[i]=natDataReader.nextRecord();
		    i++;
		}
	    }catch(Exception jdbf){System.out.println(jdbf);}
	}
	// Setup the ShapeFileReader array
	layer=new ShapeFileReader[nLayers];//make room for the layers
	try{
	    layer[0]=new ShapeFileReader("counties");
	}catch(Exception e){System.out.println(" ColeMain Error: "+e);}
	// Account for various drawing possibilities
	addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e){
		    Point loc=e.getPoint();
		    if(national<0&&(currentBoundsH>currentBoundsW||yMin<35)){
			mouseX=xMin+loc.x*mapHeight/height;
			mouseY=yMin+(height-loc.y)*mapHeight/height;
		    }
		    else{
			mouseX=xMin+loc.x*mapWidth/width;
			mouseY=yMin+((mapHeight*(width/mapWidth))-loc.y)*mapWidth/width;
		    }
		    if(national>0){
			// Find the appropriate polygon
			Vector v = layer[0].getSL();
			for(int i=0; i<v.size(); i++){
			    if(((Shape)v.elementAt(i)).contains(mouseX,mouseY)){
				stateHolder=i;
			    }
			}
			// associate the polygon number with an actual state code
			if(natData[stateHolder]!=null){
			    stateCode = java.lang.Integer.parseInt((String)natData[stateHolder][2]);
			}
			else{
			    System.out.println("Could not retrieve state code!");
			}
		    }
		    else if(polygonSelect){
			repaint();
		    }
		    else if(polygonUserSelect){
			repaint();
		    }
		    else if(layerPolygonSelect){
			repaint();
		    }
		    else if(circleDraw){
			if(circleClickAllow){
			    if(circleClick){
				instructionFrame.setVisible(false);
				double hDist, vDist;
				hDist=mouseX-centerX;
				vDist=mouseY-centerY;
				circleRad=2*java.lang.Math.sqrt(((hDist*hDist)+(vDist*vDist)));
				makeCircle((float)centerX, (float)centerY, (float)circleRad);
				setCircleDraw(false);
				circleClick=false;
				setBackground(bgColor);
				circleClickAllow=false;
				mouseX=-1;
				mouseY=-1;			
			    }
			    else{
				centerX=mouseX;
				centerY=mouseY;
				mouseX=-1;
				mouseY=-1;
				circleClick=true;
			    }
			}
		    }
		    else if(polygonDraw & SwingUtilities.isLeftMouseButton(e)){
			makePolygon(true,(float)mouseX,(float)mouseY);
		    }
		    else if(polygonDraw & SwingUtilities.isRightMouseButton(e)){
			makePolygon(false,(float)mouseX,(float)mouseY);
			mouseX=-1;mouseY=-1;
		    }
		    else if(zoom & SwingUtilities.isRightMouseButton(e)){
			// Zoom out
			setSize(new Dimension((getWidth()*3)/4, (getHeight()*3)/4));
		    }
		    else if(zoom & SwingUtilities.isLeftMouseButton(e)){
			// Zoom in
			setSize(new Dimension((getWidth()*4)/3, (getHeight()*4)/3));
		    }
                    else{ 
			mouseY=-1;mouseX=-1;
		    }//don't select inadvertently
		    
		}
	    });
    } /*end of constructor*/
	 
    /**
     *Paint method
     */
    public void paint(Graphics g) {
	super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        makeTransform(g2);
	g2.setPaint(userPolyColor); //draw user polygons
	if(polygonDraw){
	    g2.draw(polyHold);
	}
	for(int i=0;i<userPolygon.size();i++){
	    g2.draw((Shape)userPolygon.elementAt(i));
	}
        if(polygonSelect){
	    layer[0].selectByPolygon(g2,mouseX,mouseY);
	}
	else if(polygonUserSelect){
	    layer[0].selectByUserPolygon(g2,mouseX,mouseY);
	}
	else if(layerPolygonSelect){
	    Vector counties = layer[0].getSL();
	    g2.setPaint(Color.yellow);
	    for(int i=0; i<counties.size(); i++){
		g2.draw((Shape)counties.elementAt(i));
	    }
	    // figure out which layer is being selected
	    int currentLayer=-1;
	    String matchingCode=null;
	    if(layerItem!=null){
		if(layerItem.equals("Watersheds")){
		    matchingCode="watersheds";
		}
		for(int i=1; i<layer.length; i++){
		    if(layer[i].getLayerType().equals(matchingCode)){
			currentLayer=i;
		    }
		}	 
	    }
	    if(currentLayer>=1){
		layer[currentLayer].selectByPolygon(g2, mouseX,mouseY);
	    }
	}
	else{
	    for(int i=(nLayers-1);i>=0;i--){
		layer[i].draw(g2);
	    }
	}/*end if else*/
    }



    /**
     *Make the correct transform before plotting
     *
     */
    public void makeTransform(Graphics2D g2){
	//Insets insets=getInsets();	
	Dimension d=getSize();
	width=d.width;//-insets.left-insets.right;
	height=d.height;//-insets.top-insets.bottom;
	
	affineTransform=new AffineTransform();	
	// Scale map depending on which is bigger: length or width
	if(national<0&&(currentBoundsH>currentBoundsW||yMin<35)){
	    affineTransform.scale(height/currentBoundsH,-height/currentBoundsH);
	}
	else{
	    affineTransform.scale(width/currentBoundsW,-width/currentBoundsW);
	}
	affineTransform.translate(-currentBoundsXmin,-currentBoundsYmax);
	g2.setTransform(affineTransform);
	g2.setStroke(new BasicStroke((float)(currentBoundsW/1000)));
    }/*end method*/

    /**
     *Make a polygon with the mouse
     *<br>Accomodates GeneralPath limitation on floats
     *<br>
     *<br>status: Whether polygon is complete or not
     *<br>x: Current point horizontal coordinate
     *<br>y: Current point vertical coordinate
     */
    public void makePolygon(boolean status, float x, float y){	
	try{
	    polyHold.lineTo(x,y); //works if not first point
	    polyCoords[0][numPolyCoords] = x;
	    polyCoords[1][numPolyCoords] = y;
	    numPolyCoords++;
	}catch(Exception e){
	    polyHold.moveTo(x,y);
	    polyCoords[0][numPolyCoords] = x;
	    polyCoords[1][numPolyCoords] = y;
	    numPolyCoords++;
	}
	if(status){repaint();return;} //still more points to add
        polyHold.closePath();
	Vector v = new Vector();
	v.addElement(new Integer(polyCode));
	v.addElement(polyCoords);
	v.addElement(new Integer(numPolyCoords));
	userShapeTracker.addElement(v);
        userPolygon.addElement(polyHold);
	layer[0].addUserShape((Shape)polyHold);
        polygonDraw=false;
        setBackground(bgColor);
	polyHold=new GeneralPath(); //ready to make another user poly
	numPolyCoords = 0;
	polyCoords=new float[2][100];
    }/*end method*/

    /**
     *Make a user circle based on given input
     *<br> Arguments are center point and radius
     */
    public void makeCircle(float x, float y, float radius){
	float[] circleCoords=new float[3];
	x = (x-(radius/(float)2));
	y = (y-(radius/(float)2));
	Ellipse2D newEllipse = new Ellipse2D.Float(x, y, radius, radius);
	Vector v = new Vector();
	v.addElement(new Integer(circleCode));
	circleCoords[0] = x;
	circleCoords[1] = y;
	circleCoords[2] = radius;
	v.addElement(circleCoords);
	userShapeTracker.addElement(v);
	userPolygon.addElement(newEllipse);
	layer[0].addUserShape((Shape)newEllipse);
    }
    
    public void setPolygonSelect(boolean select){
	polygonSelect=select; 
	layerPolygonSelect=false;
	polygonDraw=false;
	polygonUserSelect=false;
	circleDraw=false;
	zoom=false;
	circleData.setVisible(false);
	drawChoice.setVisible(false);
	instructionFrame.setVisible(false);
    }
    
    public void setUserPolygonSelect(boolean select){
	polygonUserSelect=select;
	layerPolygonSelect=false;
	polygonDraw=false;
	polygonSelect=false;
	circleDraw=false;
	zoom=false;
	circleData.setVisible(false);
	drawChoice.setVisible(false);
	instructionFrame.setVisible(false);
    }
    
    public void setLayerPolygonSelect(boolean select){
	layerPolygonSelect=select;
	polygonUserSelect=false;
	polygonDraw=false;
	polygonSelect=false;
	circleDraw=false;
	zoom=false;
	circleData.setVisible(false);
	drawChoice.setVisible(false);
	instructionFrame.setVisible(false);
    }   
    
    public boolean isUserPolygonSelect(){
	return polygonUserSelect;
    }
    
    public boolean isPolygonSelect(){
	return polygonSelect;
    }
    public boolean isLayerPolygonSelect(){
	return layerPolygonSelect;
    }
   
    public void setPolygonDraw(boolean select){
	polygonDraw=select; 
	layerPolygonSelect=false;
	polygonSelect=false;
	polygonUserSelect=false;
	circleDraw=false;
	zoom=false;
	circleData.setVisible(false);
	drawChoice.setVisible(false);
	instructionFrame.setVisible(false);
    }

    public boolean isPolygonDraw(){
	return polygonDraw;
    }
   
    public void setCircleDraw(boolean select){
	circleDraw=select; 
	layerPolygonSelect=false;
	polygonSelect=false;
	polygonUserSelect=false;
	polygonDraw=false;
	zoom=false;
    }

    public boolean isCircleDraw(){
	return circleDraw;
    }
    
    public void setZoom(boolean select){
	zoom=select;
	circleDraw=false; 
	layerPolygonSelect=false;
	polygonSelect=false;
	polygonUserSelect=false;
	polygonDraw=false;	
	circleData.setVisible(false);
	drawChoice.setVisible(false);
	instructionFrame.setVisible(false);
    }

    public boolean isZoom(){
	return zoom;
    }
    /**
     *Return a vector of the user created shapes
     **/
    public Vector getShapeTracker(){
	return userShapeTracker;
    }
    /**
     *Allow the user to choose how they want to input their
     *circle data: via a mouse click, or through
     *user specified coordinates
     */
    public void circleGo(){
	circleData = new JFrame("");
	drawChoice = new JFrame("");
	instructionFrame = new JFrame("");
	JPanel choicePanel = new JPanel();
	JPanel dataPanel = new JPanel();
	final JRadioButton clickPoint, typePoint;
	ButtonGroup bg;
	final JTextField lonT, latT, radT;
	JLabel cdLabel = new JLabel("Circle Drawing Options", javax.swing.SwingConstants.CENTER);
	JLabel howtoLabel = 
	    new JLabel("Choose how to draw your circle", javax.swing.SwingConstants.CENTER);
	JLabel instructions = 
	    new JLabel("Click the map twice: first select the center, then the outer boundry.", javax.swing.SwingConstants.CENTER);
	choicePanel.setLayout(new GridLayout(2, 1));
	dataPanel.setLayout(new GridLayout(6, 1));
	drawChoice.setSize(320, 200);
	circleData.setSize(200, 200);
	instructionFrame.setSize(700, 100);
	drawChoice.getContentPane().setLayout(new BorderLayout());
	circleData.getContentPane().setLayout(new BorderLayout());
	instructionFrame.getContentPane().setLayout(new GridLayout(1,1));
	bg = new ButtonGroup();
	clickPoint = new JRadioButton("I would like to click the map");
	typePoint = new JRadioButton("I would like to input the center manually");
	bg.add(clickPoint);
	bg.add(typePoint);
	okBut2 = new JButton("Okay");
	latT = new JTextField("");
	lonT = new JTextField("");
	radT = new JTextField("");
	choicePanel.add(clickPoint);
	choicePanel.add(typePoint);
	dataPanel.add(new JLabel("Center Latitude"));
	dataPanel.add(latT);
	dataPanel.add(new JLabel("Center Longitude"));
	dataPanel.add(lonT);
	dataPanel.add(new JLabel("Circle Radius"));
	dataPanel.add(radT);
	drawChoice.getContentPane().add(howtoLabel, BorderLayout.NORTH);
	drawChoice.getContentPane().add(choicePanel, BorderLayout.CENTER);
	drawChoice.getContentPane().add(okBut2, BorderLayout.SOUTH);
	circleData.getContentPane().add(cdLabel, BorderLayout.NORTH);
	circleData.getContentPane().add(dataPanel, BorderLayout.CENTER);
	circleData.getContentPane().add(okBut, BorderLayout.SOUTH);
	instructionFrame.getContentPane().add(instructions);
	okBut2.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if(clickPoint.isSelected()){
			drawChoice.setVisible(false);
			circleClickAllow=true;
			instructionFrame.setVisible(true);
		    }
		    else if(typePoint.isSelected()){
			    drawChoice.setVisible(false);
			    circleData.setVisible(true);
			    okBut.addActionListener(new ActionListener(){
				    public void actionPerformed(ActionEvent e){
					AlbersConverter ac = new AlbersConverter("");
					double[] acout = ac.geoToAlbers(java.lang.Double.valueOf(latT.getText()).doubleValue(), java.lang.Double.valueOf(lonT.getText()).doubleValue());
					
					makeCircle((float)acout[0],
						   (float)acout[1],
						   java.lang.Float.valueOf(radT.getText()).floatValue());
					circleData.setVisible(false);
					setCircleDraw(false);
					setBackground(bgColor);
				    }
				});
		    }
		}
	    });
	drawChoice.setVisible(true);
    }
    /**
     *Clear all zoom related variables
     **/
    public void clearZoomVars(){
	currentBoundsXmin=xMin;
	currentBoundsYmin=yMin;	
	currentBoundsXmax=xMax;
	currentBoundsYmax=yMax;
	currentBoundsW=mapWidth;
	currentBoundsH=mapHeight;
    }
        /**
     *Get the polygons selected by the user,
     *in the appropriate format
     **/
    public Vector getSelectedPolygons(){
	//  getSelectedPolygons() should return a Vector of the following
	//  format:
	//  Vector v{
	//          boolean[] selectedMapShapes
	//          String mapURL
	//          boolean selectedUserShapes
	//          Vector userShapes (in serializable format - see ACanvas.java)
	//          boolean[] otherLayer
	//          String otherLayerURL
	//          ...
	//  }
	Vector results = new Vector();
	boolean[] selected=layer[0].getSelected(), userSelected=layer[0].getUserSelected();
	results.add(selected);
	results.add(counties);
	results.add(userSelected);
	results.add(getShapeTracker());
	// Add any other layers which have selected shapes
	for(int i=1; i<layer.length; i++){
	    if(layer[i].hasSelectedShapes()){
		results.add(layer[i].getSelected());
		results.add(layer[i].getFileLocation());
	    }
	}
	return results;
    }
    
    /** Same as above ReadAction, but simply a method */
    public void readMap(){
	try{
	    // Clear out user Polygons
	    userPolygon.removeAllElements();

	    userShapeTracker.removeAllElements();
	    // Reset all map variables
	    layer[0].readData(counties);
	    bounds=layer[0].getBounds();
	    xMin=bounds[0];
	    yMin=bounds[1];
	    xMax=bounds[2];
	    yMax=bounds[3];
	    mapWidth=xMax-xMin;
	    mapHeight=yMax-yMin;		
	    clearZoomVars();
	}catch(Exception e2){System.out.println("Error at readData: "+e2);repaint();}
	//Dimension d = new Dimension();
	//d.setSize(mapWidth, mapHeight);
	//setSize(d);
	setSize(475,475);
	repaint();
    }
	
    /** 
     *Let the user select their polygons 
     */
    public void pickUserPolygons(){
	boolean select=isUserPolygonSelect();
	if(!select)setBackground(selectColor);
	else setBackground(bgColor);
	setUserPolygonSelect(!select);	    
	mouseX=-1; mouseY=-1;
    }
    
    /**
     *Let the user pick layer specific polygons
     */
    public void pickLayerPolygons(String layerSelected){
	if(layerSelected.equals("Counties")){
	    boolean select=isPolygonSelect();
	    if(!select){
		setBackground(selectColor);
	    }
	    else{
		setBackground(bgColor);
	    }
	    setPolygonSelect(!select);
	    mouseX=-1; mouseY=-1;
	}
	else{
	    boolean select=isLayerPolygonSelect();
	    if(!select){
		setBackground(selectColor);
	    }
	    else{
		setBackground(bgColor);
	    }
	    setLayerPolygonSelect(!select);
	    mouseX=-1; mouseY=-1;
	}
    }
    /**
     *Let the user draw  polygons
     */
    public void drawPolygon(){
	boolean select=isPolygonDraw();
	if(!select)setBackground(selectColor);
	else setBackground(bgColor);
	setPolygonDraw(!select);
    }
    
    /** 
     *Let the user draw circles 
     */
    public void drawCircle(){
	boolean select=isCircleDraw(); 
	if(!select)setBackground(selectColor);
	else setBackground(bgColor);
	setCircleDraw(!select);
	if(isCircleDraw()){
	    circleGo();
	}
    }

    /**
     *Let the user zoom
     */
    public void zoom(){
	boolean select = isZoom();
	if(!select)setBackground(selectColor);
	else setBackground(bgColor);
	setZoom(!select);
    }
    /**
     *Add map "features" by loading additional layers
     *<br>
     *<br>layerType: Layer description
     **/
    public void addLayer(String layerType){
	// In each case, the method:
	// 1.  Adds a new layer
	// 2.  Checks to see if the file has already been loaded
	// 3.  Loads or reads the file, depending on (2)
	// 4.  Repaints
	if(layerType.equals("waterways")){
	    ShapeFileReader[] temp = layer;
	    nLayers = nLayers+1;
	    layer=new ShapeFileReader[nLayers];
	    for(int i=0; i<temp.length; i++){
		layer[i]=temp[i];
	    }
	    if(waterLoaded){
		layer[(nLayers-1)]=waterLayer;
	    }
	    else{
		waterLayer=new ShapeFileReader(layerType);
		waterLayer.readData(counties.substring(0,counties.length()-8)+"water.shp");
		layer[(nLayers-1)]=waterLayer;
		waterLoaded=true;
	    }
	    repaint();
	}
	if(layerType.equals("watersheds")){
	    ShapeFileReader[] temp = layer;
	    nLayers=nLayers+1;
	    layer=new ShapeFileReader[nLayers];
	    for(int i=0; i<temp.length; i++){
		layer[i]=temp[i];
	    }	    
	    if(shedLoaded){
		layer[(nLayers-1)]=shedLayer;
	    }
	    else{
		shedLayer=new ShapeFileReader(layerType);
		shedLayer.readData(counties.substring(0,counties.length()-8)+"huc.shp");
		layer[(nLayers-1)]=shedLayer;
		shedLoaded=true;
	    }
	    repaint();
	}
	if(layerType.equals("roadways")){
	    ShapeFileReader[] temp = layer;
	    nLayers=nLayers+1;
	    layer=new ShapeFileReader[nLayers];
	    for(int i=0; i<temp.length; i++){
		layer[i]=temp[i];
	    }
	    if(roadLoaded){
		layer[(nLayers-1)]=roadLayer;
	    }
	    else{
		roadLayer=new ShapeFileReader(layerType);
		roadLayer.readData(counties.substring(0,counties.length()-8)+"fhards.shp");
		layer[(nLayers-1)]=roadLayer;
		roadLoaded=true;
	    }
	    repaint();
	}
    }

    /**
     *Remove a feature layer
     *<br>
     *<br>layerType: Layer description
     **/
    public void removeLayer(String layerType){
	ShapeFileReader[] temp=layer;
	int locationIndex=-1;
	for(int i=0;i<layer.length;i++){
	    if(layer[i].getLayerType().equals(layerType)){
		locationIndex=i;
		break;
	    }
	}
	if(locationIndex>=0){
	    layer=new ShapeFileReader[temp.length-1];
	    for(int i=0; i<locationIndex; i++){
		layer[i]=temp[i];
	    }
	    for(int i=locationIndex+1; i<temp.length; i++){
		layer[i-1]=temp[i];
	    }
	    nLayers=nLayers-1;
	}
	repaint();
    }
    
    public int getLayer(String whichLayer){
	int locationIndex=-1;
	for(int i=0;i<layer.length;i++){
	    if(layer[i].getLayerType().equals(whichLayer)){
		locationIndex=i;
		break;
	    }
	}
	return locationIndex;
    }
    
    public ShapeFileReader returnMap(){
	return layer[0];
    }
    
    public void setMap(ShapeFileReader it){
	layer[0]=it;
    }
    public int getStateCode(){
	return stateCode;
    }
    public void setLayerItem(String s){
	layerItem=s;
    }
}/*end  class*/
