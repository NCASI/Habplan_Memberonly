/**
*GISViewer for viewing ArcView Shapefiles with Habplan
*@author Paul Van Deusen 3/2001, major additions: 10/2004, 12/2005
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.awt.geom.*;
import gis.*;
import org.xml.sax.Attributes;


public class GISViewer extends JFrame{
    FrameMenu parent;
    JPanel controlPanel; //controls and a clear copy for glass pane zooming
    JRadioButton regimeRadio, polygonRadio; //what colorTable to use
    String colorMethod="polygon"; //the method for coloring
    Vector colorsProject=new Vector(); //holds colors from an xml project file
    JButton showButton, zoomButton; //show a colorTable, zoom
    JButton bgButton, lineButton; //buttons for color control
    JLabel clickLabel; //used by canvas to write info after user clicks on shape. see setClickLabel()
    ACanvas canvas;  //drawing area
    JTextField fileText; //holds fileName from user
    String shapeFileName; //fileName from JFileChooser
    GISLayer polyLayer=null; //GISLayer class keeps track of n of layers
    boolean isShapeFileRead=false; //indicates if shapeFile was read yet
    boolean isMissMatchWarning=false;//indicates if already warned of mismatch
    boolean useHoldStateColors=false; //effects how color Tables made
    boolean wasReDrawn;  //indicates if last call to autoReDraw() did a reDraw
    BasicTable regimeColorTable; //for manipulating color by regime
    Action initAction;
    int drawIter=0;//indicates on what iteration to do a reDraw of map
    ZoomGlassPane zoomGlassPane;
    boolean isZoomed=false; //keep track of zoom status
    boolean isBuffered=true; //keep track of double buffering status
    JPanel gp; //glasspane used for zooming via a ZoomGlassPane instance
    JSlider slider; //used to slow speed for dynamic GIS display
    HoldStateGIS holdStateGIS; //hold the settings so user can reload later
    RepaintManager rm = null;

     public GISViewer(FrameMenu parent){
	super("Habplan GIS Viewer");
        getContentPane().setLayout(new BorderLayout());
	this.parent=parent;
	setBounds(350,50,600,400);
	canvas=new ACanvas();
        rm = RepaintManager.currentManager(this);
	getContentPane().add(canvas,BorderLayout.CENTER);
	clickLabel=new JLabel("Info:");//added to controlPanel later
	clickLabel.setToolTipText("Click on a shape for info");
	canvas.setClickLabel(clickLabel);
	createFilePanel();
	createColorPanel();
	createRefreshPanel();
	createGlassWindow();
	polyLayer=new GISLayer(this); //make a default layer
	regimeColorTable=new BasicTable(this,"Regime Color Table",UserInput.ExampleDir);
	holdStateGIS=new HoldStateGIS(); 
	 setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addComponentListener(new ResizeListener());
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      makeRelatedWindowsInvisible();
	      dispose();
	  }
	 });   
    }/*end constructor*/


    /**
     *Close all related GIS windows when the main viewer isn't
     *visible
     */
    protected void  makeRelatedWindowsInvisible(){
	if(!isShapeFileRead)return;  //no other windows yet
	regimeColorTable.setVisible(false);
	polyLayer.colorTable.setVisible(false);
       regimeColorTable.makeRelatedWindowsInvisible();
	polyLayer.colorTable.makeRelatedWindowsInvisible();
	parent.regimeEditor.makeRelatedWindowsInvisible();
    }
    
   /**  
    *make NORTH Panel that holds file stuff, reDraw, and Zoom
    */
   protected void createFilePanel(){
	controlPanel=new JPanel(new BorderLayout());
	JPanel filePanel=new JPanel();
	//do saveButton part
	final JButton saveButton=new JButton("Save");
	saveButton.setToolTipText("Save GIS settings");
	saveButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent e){
		   String fName=UserInput.saveFile;
	if (fName.indexOf(System.getProperty("file.separator"))<0){
	    fName=UserInput.hbpDir+UserInput.saveFile;
	}
		   captureSettings();
		   saveState(fName);
		   JOptionPane.showMessageDialog(controlPanel,"Saved to: "+fName+"2","GIS Settings Saved",JOptionPane.WARNING_MESSAGE); 
	       }
	   });
	// filePanel.add(saveButton);
	//do file choosing part
	fileText=new JTextField(12);
	fileText.setToolTipText("Enter the shapefile name.");
       JButton fileButton=new JButton("File");
       fileButton.setToolTipText("Open a FileChooser");
       fileButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent e){
		   selectFile();
		   fileText.setText(shapeFileName);
	       }
	   });
       filePanel.add(fileButton);
       filePanel.add(fileText);
        slider=new JSlider(0,100);
       slider.setMajorTickSpacing(25);
       slider.setPaintTicks(true);
       slider.setPaintLabels(true);
       slider.setValue(0);
       slider.setToolTipText("Value=0");
       slider.setToolTipText("Increase to improve dynamic display"); 
       filePanel.add(slider);

       controlPanel.add(filePanel,BorderLayout.NORTH); 
       controlPanel.add(clickLabel,BorderLayout.SOUTH);
          //add an update panel
       JPanel upDatePanel=new JPanel();
       JButton reDrawButton=new JButton("Draw");
       reDrawButton.setToolTipText("reDraw the GIS Display.");
       reDrawButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent event){
		   reDrawDisplay();
	       }
	   });
	upDatePanel.add(reDrawButton);
        zoomButton=new JButton("Zoom");
	zoomButton.setToolTipText("Left then Right Mouse to zoom");
	zoomButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent event){
		    
		   if(isZoomed){//unzoom if isZoomed=true
		       isZoomed=false;
		       canvas.setBounds(polyLayer.getBounds());
		       canvas.repaint();
		       return;
		   }/*end if isZoomed*/
		       zoomGlassPane.setLineColor(canvas.getLineColor());
		       getContentPane().removeAll();
		       getContentPane().add(canvas,BorderLayout.CENTER);
		       setVisible(false);
		       rm.setDoubleBufferingEnabled(true);
		       reDrawDisplay();
		       setVisible(true);
		        gp.setVisible(true);
	       }
	   });
       upDatePanel.add(zoomButton);	   
       controlPanel.add(upDatePanel,BorderLayout.CENTER);
	getContentPane().add(controlPanel,BorderLayout.SOUTH);
       
    }/*end method*/
    
   /**
    *Zoom the display.  Left mouse to pick a rectangle.  Right to 
    *implement the zoom.  Called from ZoomGlassPane class
    */
   public void zoomDisplay(Rectangle2D rect){
       if(rect.getX()!=-1 & rect.getY()!=-1){
	   canvas.setClipRect(rect);
	   isZoomed=true;
       }
       gp.setVisible(false);
       setVisible(false);
       Container c=getContentPane();
       c.removeAll();
       c.add(controlPanel,BorderLayout.SOUTH);
       c.add(canvas,BorderLayout.CENTER);
       reDrawDisplay();
       if(parent.runSuspend || parent.runStop)
	   rm.setDoubleBufferingEnabled(false);
       setVisible(true);
   }/*end method*/ 
    
    protected void createGlassWindow(){
	gp=new JPanel(new BorderLayout());
	gp.setOpaque(false);
	setGlassPane(gp); 
	zoomGlassPane=new ZoomGlassPane(this);
	gp.add(zoomGlassPane,BorderLayout.CENTER);
	}/*end method*/
    
    /**
     *Create the WEST panel for color control tables
     */
   public void createColorPanel(){
       JPanel colorPanel=new JPanel();
       Listener listener=new Listener();
	ButtonGroup group=new ButtonGroup(); //so only 1 button can be true
	regimeRadio=new JRadioButton("Regime");
        regimeRadio.addItemListener(listener);
	regimeRadio.setToolTipText("Coloring control by regime");
	polygonRadio=new JRadioButton("Polygon",true);
        polygonRadio.addItemListener(listener);
	polygonRadio.setToolTipText("Coloring control by polygon");
	group.add(regimeRadio);
	group.add(polygonRadio);
	colorPanel.add(regimeRadio);
	colorPanel.add(polygonRadio);
        showButton=new JButton("Table");
	showButton.setToolTipText("Open a color table");
        showButton.setEnabled(false);
	 showButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent event){
		  if(colorMethod.equals("polygon")){
		   polyLayer.colorTable.setVisible(!polyLayer.colorTable.isVisible());   
		 regimeColorTable.setVisible(false);
		 regimeColorTable.makeRelatedWindowsInvisible();
		 polyLayer.colorTable.makeRelatedWindowsInvisible();
		   }
		   else if (colorMethod.equals("regime")){
		   regimeColorTable.setVisible(!regimeColorTable.isVisible());
		    polyLayer.colorTable.setVisible(false);
		    polyLayer.colorTable.makeRelatedWindowsInvisible();
		    regimeColorTable.makeRelatedWindowsInvisible();
		   }  
	       }/*end if else if block*/
	   });
	colorPanel.add(showButton);
	controlPanel.add(colorPanel,BorderLayout.WEST);
    }/*end method*/

     class Listener implements ItemListener{
	public void itemStateChanged(ItemEvent e){
	    if(regimeRadio.isSelected()){
	       if(ObjForm.regimeArray.size()==1){
		    showButton.setEnabled(false);
		  JOptionPane.showMessageDialog(controlPanel,"Habplan Has Read No Regimes Yet","Regime Color Table Error",JOptionPane.WARNING_MESSAGE);  
		  return;
	       }
		colorMethod="regime";
		    if(isShapeFileRead){
		     polyLayer.colorTable.setVisible(false);
		     regimeColorTable.makeRelatedWindowsInvisible();
		     polyLayer.colorTable.makeRelatedWindowsInvisible();
		    }
	     }else if(polygonRadio.isSelected()){
		 colorMethod="polygon";
		 if(isShapeFileRead)showButton.setEnabled(true);
		 regimeColorTable.setVisible(false);
		 regimeColorTable.makeRelatedWindowsInvisible();
	         polyLayer.colorTable.makeRelatedWindowsInvisible();
	    }
	}/*end method*/
    }/*end inner Listener class*/

    /**
     *Create SOUTH panel that controls how the display
     *gets refreshed
     */

    protected void createRefreshPanel(){
	JPanel refreshPanel=new JPanel();
	final JComboBox comboBox=new JComboBox();
	comboBox.setToolTipText("Auto redraw after n iterations");
	for(int i=0;i<6;i++) comboBox.addItem(String.valueOf(i));
	for(int i=10;i<21;i+=5) comboBox.addItem(String.valueOf(i));
	for(int i=30;i<51;i+=10) comboBox.addItem(String.valueOf(i));
	for(int i=100;i<501;i+=100) comboBox.addItem(String.valueOf(i));
	comboBox.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		String item=(String)comboBox.getSelectedItem();
		drawIter=Integer.parseInt(item);
		}
	    });
	refreshPanel.add(comboBox);
	 bgButton=new JButton();
	bgButton.setBackground(canvas.bgColor);
	bgButton.setToolTipText("Change Background color");
	bgButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent event){
		   Color c=colorChooser(canvas.getBackground());
		   canvas.setBackground(c);
		   bgButton.setBackground(c);
	       }
	   });
	 lineButton=new JButton();
	lineButton.setBackground(canvas.getLineColor());
	lineButton.setToolTipText("Change Line color");
	lineButton.addActionListener(new ActionListener(){
	       public void actionPerformed(ActionEvent event){
		   Color c=colorChooser(canvas.getLineColor());
		   canvas.setLineColor(c);
		   lineButton.setBackground(c);
	       }
	   });
	refreshPanel.add(bgButton);
	refreshPanel.add(lineButton);
	controlPanel.add(refreshPanel,BorderLayout.EAST);
    }/*end method*/

    /**
     *Get a colorChooser.  Needed insided anonymous inner classes
     *@param initColor the initial color of chooser
     */
    public Color colorChooser(Color initColor){
        Color c=JColorChooser.showDialog(this,"Pick a color",initColor);
	return c;
    }

    /**
     *Called at the end of each Habplan Iteration to see if
     *a redraw is required
     *@param iter the Habplan iteration that was just completer
     *@return true if redrawn, false otherwise.
     */
    public  synchronized void  autoReDraw(int iter){
	
        wasReDrawn=false;
	if(drawIter==0|!isVisible()|!regimeRadio.isSelected())return;
	if(iter%drawIter!=0)return;
	reDrawDisplay();
        wasReDrawn=true;
    }

    /**
     *get value of wasReDrawn
     */
    public boolean getWasReDrawn(){return wasReDrawn;}
   
    /**
     *get slider value in milliseconds
     */
    public int getSliderValue(){return slider.getValue()*100;}

class InitAction extends AbstractAction{
    public InitAction(){
      super("Init");
    }
    public void actionPerformed(ActionEvent e){
      if(colorMethod.equals("polygon")){
	  polyLayer.colorTable.setVisible(false);
	  makePolygonColorTable();
	  polyLayer.colorTable.setVisible(true);
      }else if(colorMethod.equals("regime")){
	  regimeColorTable.setVisible(false);
	  makeRegimeColorTable();
	  regimeColorTable.setVisible(true);
      }/*end if else*/
    }/*end actionPerformed()*/
  }/*end Action*/

      /**
     *Draw the GIS Display using current settings
     */
    public  void reDrawDisplay(){
	if(!isShapeFileRead){//initialize the data and display
	    init();
	    regimeColorToShapeColor();
	}
	if(colorMethod.equals("regime"))regimeColorToShapeColor();
	 canvas.setShapeColor(polyLayer.getShapeColor());
	 canvas.repaint();
	
    }/*end method*/

    /**
     *Handle the mapping of colors from the regimeColorTable
     *to the polyLayer color vector.  This maintains the concept
     *of colors being controlled by the polyLayer
     */
    public void regimeColorToShapeColor(){
	int n=polyLayer.shapeList.size();
	if(!isMissMatchWarning && ObjForm.polySched.length-1!=n){
	    isMissMatchWarning=true;
	    JOptionPane.showMessageDialog(controlPanel,"Wrong N of Polys in ShapeFile, n="+n+".\n No Further Warning Will be Given","GIS/Polygon Data Mismatch",JOptionPane.WARNING_MESSAGE); 
	    return;
	}/*fi*/
	for(int r=0;r<n;r++){
	    int r1=ObjForm.polySched[r+1]-1;
	    polyLayer.colorTable.data.color[r]=regimeColorTable.data.color[r1];
	}/*end for r*/
    }/*end method*/

  

    /**
     *Read the data and initialize the display area
     */
    public void init(){
	 readPolygons();
      if(!isShapeFileRead)return;
      initAction=new InitAction();
      polyLayer.colorTable.setTitle("Polygon Color Table");
      polyLayer.colorTable.toolBar.addSeparator();
      polyLayer.colorTable.toolBar.add(initAction).setToolTipText("ReInitialize Table");
      regimeColorTable.toolBar.addSeparator();
      regimeColorTable.toolBar.add(initAction).setToolTipText("ReInitialize Table");

      //remake colorTable is not done in readPolygons() already      
      if(polyLayer.colorTable.data.rows!=ObjForm.nPolys)
            makePolygonColorTable();
       makeRegimeColorTable();
      showButton.setEnabled(true);  //can show table now
      canvas.setBounds(polyLayer.getBounds());
      canvas.setShapeList(polyLayer.shapeList,polyLayer.shapeType);
      canvas.addMouseListener(new CanvasListener());
       //set a string to show when user clicks on the canvas
	 setDrawStringArrayByType("polygon");
	 polyLayer.colorTable.table.getModel().addTableModelListener(new TableListener());
       
    }/*end method
 
     /**
      *Set up the polygonColor Table
      *
      */
    public void makePolygonColorTable(){
	try{polyLayer.colorTable.saveUndoData();
	}catch(Exception e){System.out.println("makePolygonColorTable() undo error: "+e);}

	boolean usePolyArray=false; //use polyArray Vector if ready
        int n=polyLayer.shapeList.size();
	if(ObjForm.polyArray!=null){
	    n=ObjForm.polyArray.size();
	    usePolyArray=true;
	}/*end if*/
	 if(!useHoldStateColors){
	    Color[] color=new Color[n];
	    String[][] data=new String[n][1];
	    for(int r=0;r<n;r++){
		color[r]=Color.green;
		if(usePolyArray)
		    data[r][0]=(String)ObjForm.polyArray.elementAt(r);
		else data[r][0]=String.valueOf(r+1); 
	    }/*end for r*/
	    polyLayer.colorTable.data.changeData(color,data);
	  }/*end dont use holdStateColors section*/
	 else{//useHoldStateColors
	  polyLayer.colorTable.data.changeData(holdStateGIS.polygonColor,holdStateGIS.polygonColorData);
	  polyLayer.colorTable.data.setColumnNames(holdStateGIS.polygonColumnNames);
	 }/*end else useHoldStateColors*/
	  polyLayer.colorTable.fireStructureChangeEvent();
    }/*end method*/

     /**
      *Set up the regimeColor Table
      *
      */
    public void makeRegimeColorTable(){
	     if(ObjForm.regimeArray.size()==1){
		    showButton.setEnabled(false);
		  JOptionPane.showMessageDialog(controlPanel,"Habplan Has Read No Regimes Yet","Regime Error",JOptionPane.WARNING_MESSAGE); 
		  return;
		}
        int n=ObjForm.regimeArray.size()-1;
	
	try{regimeColorTable.saveUndoData();
	}catch(Exception e){System.out.println("makeRegimeColorTable() error: "+e);}
	    Color[] color=new Color[n];
	    String[][] data=new String[n][1];
	    for(int r=0;r<n;r++){
              try{
		  String[] cn=(String[])colorsProject.get(r);
	  //int regId=Integer.parseInt((String)ObjForm.regimeTable.get(cn[0]));
		  int r1,g1,b1,a1;
		  r1=Integer.parseInt(cn[1]);
		  g1=Integer.parseInt(cn[2]);
		  b1=Integer.parseInt(cn[3]);
		  a1=Integer.parseInt(cn[4]);
		  color[r]=new Color(r1,g1,b1,a1);
	      }catch(Exception e){
	       color[r]=Color.green;
	      }
	       data[r][0]=(String)ObjForm.regimeArray.elementAt(r+1);
	    }/**/
  
	    if(useHoldStateColors&holdStateGIS.regimeColor.length==n){
		for(int r=0;r<n;r++) color[r]=holdStateGIS.regimeColor[r];
		useHoldStateColors=false;
	    }
	   
	    //dont change data if it came from holdStateGIS
	     regimeColorTable.data.changeData(color,data);
	    String[] names=regimeColorTable.data.getColumnNames();
	    names[0]="Color"; //enforce correct names in cols 0 and 1
	    names[1]="Regime";
	    regimeColorTable.data.setColumnNames(names);
	    regimeColorTable.fireStructureChangeEvent();
    }/*end method*/

  /**
   *Select a shapefile for reading
   *
   */
  public void selectFile(){
    JFileChooser chooser = new JFileChooser(new File(UserInput.ExampleDir));
    HBPFilter filter = new HBPFilter("shp","Shapefiles"); //show .shp files
    chooser.setFileFilter(filter);
    int state=chooser.showOpenDialog(null);
    File file=chooser.getSelectedFile();
    if (file != null && state == JFileChooser.APPROVE_OPTION){
      shapeFileName=file.getAbsolutePath();
      int p2=shapeFileName.lastIndexOf(UserInput.ExampleDir);
      if(p2!=-1){//only retain whats after ExampleDir
	  p2=p2+UserInput.ExampleDir.length();
	  shapeFileName=shapeFileName.substring(p2);
      }
      isShapeFileRead=false; //need to read shapeFile
     }
    else if (state==JFileChooser.CANCEL_OPTION){
	shapeFileName=fileText.getText(); //keep current filename
    };

  }/*end method*/  

    /**
     *Read the polygons from a shapeFile
     *Do this before makeBlockData
     *@param fileName the name of the ESRI Shapefile
     */
    public void readPolygons(){
	if(polyLayer==null)polyLayer=new GISLayer(this);
	if(fileText.getText().equals("")){//no file specified
	JOptionPane.showMessageDialog(this,"Specify a Shapefile to read","ShapeFile Specification Error",JOptionPane.WARNING_MESSAGE);
	return; 
	}/*fi*/
	if(polyLayer.colorTable.dataFile!=""){
            String fileName=ObjForm.findFile(polyLayer.colorTable.dataFile);
	    polyLayer.colorTable.readTheData(fileName);
	}
	//filter the fileName to see if its in exampleDir
	String fileName=ObjForm.findFile(fileText.getText().trim());
	int n=polyLayer.readData(fileName);
	if(n<=0)JOptionPane.showMessageDialog(this,"Nothing was Read","Read Error",JOptionPane.WARNING_MESSAGE);
	else JOptionPane.showMessageDialog(parent,n+" Polygons read","Wait for Drawing",JOptionPane.INFORMATION_MESSAGE);  
	if(n==0)isShapeFileRead=false;
	else isShapeFileRead=true; 
    }/*end method*/

    /**
     *Set canvas.drawStringArray according to user's wish
     *This causes a message to pop up at mouse click locations
     *on the canvas.  The types are "polygon" to give polygon id,
     *"next type here"
     *@param messageType the type of message wanted
     */
   public void  setDrawStringArrayByType(String messageType){
	 int n=polyLayer.shapeList.size();
	 String[] drawStringArray=new String[n];
	if(messageType.equals("polygon")){
	    String[][] rowData=polyLayer.colorTable.data.getRowData();
	    //return the value in the Polygon column if it exists
	     int col=polyLayer.colorTable.getColumnByName("Polygon");
	     if(col<0 || rowData[0].length<2)col=1; //no polygon column
	    for(int r=0;r<n;r++)
		drawStringArray[r]="Polygon: "+rowData[r][col-1];
	}/*fi*/
	canvas.setDrawStringArray(drawStringArray);
    }/*end method*/
    
    /**
     *Set double buffering. Called from FrameMenu class
     **/
   public void  setBuffering(boolean isBuffered){
       rm.setDoubleBufferingEnabled(isBuffered);
   }

    /**
     *Save the current state of the GIS viewer
     *@param filePrefix the prefix of the file name
     */
    public void saveState(String filePrefix){
	captureSettings();
	  try{    
     FileOutputStream fileOut=new FileOutputStream(filePrefix+"2");
     ObjectOutputStream out=new ObjectOutputStream(fileOut);
     out.writeObject(holdStateGIS); //save current holdState object
     } catch(IOException e){System.out.println("GISViewer.saveState(): "+e);}   
    }/*end method*/

    /**
     *Load a previously saved state of the GIS viewer
     *@param filePrefix the prefix of the file name
     */
    public void loadState(String filePrefix){
	try{   
    FileInputStream fileIn=new FileInputStream(filePrefix+"2");  
    ObjectInputStream in=new ObjectInputStream(fileIn);
    holdStateGIS=(HoldStateGIS)in.readObject();
      restoreSettings();
    }catch(Exception e){ }
    }/*end method*/

     /**
     *Save settings to xml file for easy initialization and portability
     *
     **/
    public void writeSettings(PrintWriter out){ 
	String shpFile=fileText.getText();   
	out.println("  <shapefile value=\""+shpFile+"\" />");
      out.println("  <polyfile value=\""+polyLayer.colorTable.dataFile+"\" />");
      int r,g,b,a;
      r=canvas.getLineColor().getRed();
      g=canvas.getLineColor().getGreen();
      b=canvas.getLineColor().getBlue();
      a=canvas.getLineColor().getAlpha();
      if(a==255) out.println("  <linecolor r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" />");
      else out.println("  <linecolor r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" a=\""+a+"\" />");
      r=canvas.getBackground().getRed();
      g=canvas.getBackground().getGreen();
      b=canvas.getBackground().getBlue();
      a=canvas.getBackground().getAlpha();
      if(a==255)out.println("  <bgcolor r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" />");
      else  out.println("  <bgcolor r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" a=\""+a+"\" />");

	Rectangle bds=getBounds();
	out.println("  <bounds height=\""+bds.height+"\" width=\""+bds.width+"\" x=\""+bds.x+"\" y=\""+bds.y+"\" />");
      
      	int n=regimeColorTable.data.color.length;
        if(n>2){ //check if gisViewer was used
        for(int i=0;i<n;i++){
	    r=(regimeColorTable.data.color[i]).getRed();
	    g=(regimeColorTable.data.color[i]).getGreen();
	    b=(regimeColorTable.data.color[i]).getBlue();
	    a=(regimeColorTable.data.color[i]).getAlpha();
	    if(a==255)out.println("  <color regime=\""+(String)ObjForm.regimeArray.elementAt(i+1)+"\" r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" />");
	    else out.println("  <color regime=\""+(String)ObjForm.regimeArray.elementAt(i+1)+"\" r=\""+r+"\" g=\""+g+"\" b=\""+b+"\" a=\""+a+"\" />");
	}
	}else{ //write out what was in the file originally if no gisViewer use
	    if(colorsProject.size()==0)return;
	      String[] names=new String[5];
	    for(int i=0;i<colorsProject.size();i++){
		names=(String[])colorsProject.get(i);
		if(names[4].equals("255"))
		out.println("  <color regime=\""+names[0]+"\" r=\""+names[1]+"\" g=\""+names[2]+"\" b=\""+names[3]+"\" />");	
		   else out.println("  <color regime=\""+names[0]+"\" r=\""+names[1]+"\" g=\""+names[2]+"\" b=\""+names[3]+"\" a=\""+names[4]+"\" />");
	    }
	}
    }

    
     /**
     *Read settings from an xml project file.  This is called from 
     *ProjectDialog.readProjectFile
     **/
    public void readSettings(String qName,Attributes atts){
	try{
	if(qName.equals("shapefile"))fileText.setText(atts.getValue(0));
	if(qName.equals("polyfile")){
	    polyLayer.colorTable.dataFile=atts.getValue(0);
	    String shpFile=fileText.getText();
	    if(polyLayer.colorTable.dataFile.equals("") && shpFile.endsWith(".shp")){
		int len=shpFile.length()-3;
		polyLayer.colorTable.dataFile=shpFile.substring(0,len)+"dbf";
		System.out.println("polyfile "+polyLayer.colorTable.dataFile);
	    }
	}
	try{
	    int r,g,b,a=255;
	    if(qName.equals("linecolor") & atts.getLength()>=3){
		r=Integer.parseInt(atts.getValue(0));
		g=Integer.parseInt(atts.getValue(1));
		b=Integer.parseInt(atts.getValue(2));
		if(atts.getLength()>3)a=Integer.parseInt(atts.getValue(3));
		Color col=new Color(r,g,b,a);
		canvas.setLineColor(col);
		lineButton.setBackground(col);
	    }
	   if(qName.equals("bgcolor") & atts.getLength()>=3){
		r=Integer.parseInt(atts.getValue(0));
		g=Integer.parseInt(atts.getValue(1));
		b=Integer.parseInt(atts.getValue(2));
		if(atts.getLength()>3)a=Integer.parseInt(atts.getValue(3));
		Color col=new Color(r,g,b,a);
		canvas.setBackground(col);
		bgButton.setBackground(col);
	    }
	    }catch(Exception e){
	    System.out.println("colors in GISViewer.readSettings():"+e);
	}

	try{
	    if(qName.equals("bounds") && atts.getLength()>=4){
		int height=Integer.parseInt(atts.getValue(0));
		int width=Integer.parseInt(atts.getValue(1));
		int x=Integer.parseInt(atts.getValue(2));
		int y=Integer.parseInt(atts.getValue(3));
		setBounds(x,y,width,height);
	    }
	
	    }catch(Exception e){
	    System.out.println("setBounds in GISViewer.readSettings():"+e);
	}


	if(qName.equals("color") && atts.getLength()>=4 ){
	    String[] names=new String[5];
	     names[4]="255";//default for alpha value in case none provided
	    names[0]=atts.getValue(0);//regimeName
	    names[1]=atts.getValue(1);//r 
	    names[2]=atts.getValue(2);//g
	    names[3]=atts.getValue(3);//b
	    if(atts.getLength()>4)names[4]=atts.getValue(4);//alpha
	    colorsProject.add(names);
	}
	}catch(Exception e){parent.console.println(getTitle()+".readSettings() "+e);}
    }

     /**
     *capture the current GISViewer settings
     */
    public void captureSettings(){
	holdStateGIS.shapeFileName=fileText.getText().trim();
	holdStateGIS.lineColor=canvas.getLineColor();
	holdStateGIS.bgColor=canvas.getBackground();
	holdStateGIS.bounds=getBounds();
	holdStateGIS.sliderValue=slider.getValue();
	//save the colors.  The rest can be reconstructed
	int n=regimeColorTable.data.color.length;
	holdStateGIS.regimeColor=new Color[n];
        for(int i=0;i<n;i++)
	    holdStateGIS.regimeColor[i]=regimeColorTable.data.color[i];
	//save the polygon Table stuff    
	 n=polyLayer.colorTable.data.color.length;
	holdStateGIS.polygonColor=new Color[n];
        for(int i=0;i<n;i++)
	    holdStateGIS.polygonColor[i]=polyLayer.colorTable.data.color[i];
	holdStateGIS.polygonColorData=polyLayer.colorTable.data.dumpData();   
	holdStateGIS.polygonColumnNames=polyLayer.colorTable.data.getColumnNames(); 
    }/*end method*/

     /**
     *Restore previously captured settings
     */
    public void restoreSettings(){
	useHoldStateColors=true;
	fileText.setText(holdStateGIS.shapeFileName);
	canvas.setLineColor(holdStateGIS.lineColor);
	canvas.setBackground(holdStateGIS.bgColor);
	bgButton.setBackground(holdStateGIS.bgColor);
	lineButton.setBackground(holdStateGIS.lineColor);
	setBounds(holdStateGIS.bounds);
	slider.setValue(holdStateGIS.sliderValue);
    }/*end method*/

    /**
     *Listener to know when new data read into colorTable
     *so can update the drawStringArray
     */  
    class TableListener implements  TableModelListener{  
	public void tableChanged(TableModelEvent e){
	    setDrawStringArrayByType("polygon");
	}  /*end method*/  
    }/*end listener class*/ 
  
    /**
     *Listener to know when user clicks on map.  This will Show the
     *row in the regimeColorTable that goes with the clicked polygon.
     *The downclick starts the search in the Canvas class and colors
     *the polygon.  The release sets the line in the colorTable
     */
    class CanvasListener extends MouseAdapter{


	public void mouseReleased(MouseEvent e){
	    int row=canvas.getSelectedShapeIndex();
	    polyLayer.colorTable.scrollToVisible(row);
	     if(e.getButton()!=1 && ObjForm.regimeArray.size()>1){
		parent.regimeEditor.setVisible(true);
		parent.regimeEditor.setPolyIndex(row+1);
		parent.regimeEditor.setPolyData();
	    }
	     if(parent.runStop | parent.runSuspend)
		 rm.setDoubleBufferingEnabled(false);
	}
    }/*end listener class*/

    /**
     *redraws the GIS display on resize.  A small resize occurs
     *whenever the user changes one of the colortables to make a redraw
     */
 
    class ResizeListener extends ComponentAdapter{
	public void componentResized(ComponentEvent e){
	    if(isShapeFileRead)reDrawDisplay();
	}
	  public void componentShown(ComponentEvent e){
	    if(isShapeFileRead)reDrawDisplay();
	}
    }

    

    
}/*end class*/
