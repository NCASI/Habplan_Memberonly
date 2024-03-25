package figure;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class Figure extends JFrame
    implements ActionListener{
    private static int nextId=1; //keep track of number of Figures, mainly to id the savefile for colors	
    private int id; //the number of the instance of Figure
    public AGraph[] ag;
    JLabel userTitle;// for user to add a title
    JPanel titlePanel; //put user title here
    int rows,cols;
    GColors gColors,bwColors;
    AxisDialog axisDialog;
    Action[]  actions={
	new PrintAction(),
	new AddTitleAction(),
	new SaveColorsAction(),
	new LoadColorsAction(),
	new FontAction(),
	new BlackWhiteAction(),
	new HBPColorsAction(),
	new OuterColorAction(),
	new InnerColorAction(),
	new TextColorAction(),
	new AxisColorAction(),
	new GridColorAction(),
	new LineColorAction()
    };
   
    FontPicker fontPicker; 
    RepaintManager repaintManager; //to control double buffering
    JCheckBoxMenuItem bufferCheckBox, yGridCheck, xGridCheck, lineLabelCheck;
     
    JMenuBar menuBar;
    JMenu fileMenu, optionMenu, graphMenu, looksMenu, offOnMenu;
    JMenu[] subGraph;
    Figure parentFig;
    String saveDir; //where user color changes are saved
   
 public Figure(String title, int rows, int cols){
     this(title, rows, cols, null); //user doesn't specify a saveDir
 }   
    
 public Figure(String title, int rows, int cols, String saveDir){
     super(title);
     id=nextId; //get unique figure id
     nextId++;
     this.parentFig=this;
     this.saveDir=saveDir;
     setDefaultCloseOperation(HIDE_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	  }
      });   
      this.rows=rows;
      this.cols=cols;
      JPanel centerPanel=new JPanel();
      centerPanel.setLayout(new GridLayout(rows,cols));
      repaintManager=RepaintManager.currentManager(this);
      repaintManager.setDoubleBufferingEnabled(true);
      ag=new AGraph[rows*cols+1];
      for (int i=1;i<=rows*cols;i++){
	ag[i]=new AGraph(this,4);  
	centerPanel.add(ag[i]);
      } /*end for i*/
      getContentPane().add(centerPanel,BorderLayout.CENTER);
      titlePanel=new JPanel();
      userTitle=new JLabel();
      titlePanel.add(userTitle);
      getContentPane().add(titlePanel,BorderLayout.NORTH);
      
      createMenu();
      fontPicker=new FontPicker(this);
      fontPicker.setSize(400,200);
      fontPicker.setLocation(300,300);
      fontPicker.setVisible(false);
      
      axisDialog = new AxisDialog("Set Axis Limits");
      gColors=new GColors();
      setSize(150+(cols)*150,100+rows*100);
      int yLoc=100+10*(id-1);
      int xLoc=200+10*(id-1);
      if(id>25){xLoc=250; yLoc=150;} //this would be a lot of graphs to fit on the screen!
     setLocation(xLoc,yLoc);
     setVisible(false);
  }/*end constructor*/   
 
 /**
 *handle buffering checkbox
 */ 
  public void actionPerformed(ActionEvent e){
      if(bufferCheckBox.getState()){
	  repaintManager.setDoubleBufferingEnabled(true);
      }
      else{
	  repaintManager.setDoubleBufferingEnabled(false);  
      }
      if(e.getActionCommand().equals("yGrid"))gColors.yGridState=yGridCheck.getState();
      if(e.getActionCommand().equals("xGrid"))gColors.xGridState=xGridCheck.getState();
      if(e.getActionCommand().equals("LineLabels"))gColors.lineLabelState=lineLabelCheck.getState();
      if(e.getActionCommand().equals("DoubleBuffer"))gColors.bufferState=bufferCheckBox.getState();	   
      setColors();         
  }/*end actionperformed()*/   

/**
*Make the menubar and menus
*/
private void createMenu(){
    actions[0]=new PrintAction(); //gif won't show unless its instantiated again
    menuBar=new JMenuBar();
    fileMenu=new JMenu("File");
    optionMenu=new JMenu("Option");
    optionMenu.setToolTipText("Save Changes Made Here Under File Menu");
    graphMenu=new JMenu("Graphs");
    int cut=4; //cut off between option and file actions
    for (int i=0;i<cut;i++)
      fileMenu.add(actions[i]);
     fileMenu.add(graphMenu); 
     subGraph=new JMenu[rows*cols+1];    
    for(int i=1;i<=rows*cols;i++){
	subGraph[i]=new JMenu("Graph"+Integer.toString(i));
	graphMenu.add(subGraph[i]);
	subGraph[i].add(new SetYAxisAction(Integer.toString(i)));
    }/*end for i*/ 
    
    bufferCheckBox=new JCheckBoxMenuItem("DoubleBuffer");
    bufferCheckBox.addActionListener(this);
    bufferCheckBox.setToolTipText("Double buffering eliminates flickering but slows execution");
    optionMenu.add(bufferCheckBox);
    
    looksMenu=new JMenu("Appearance");
    looksMenu.setToolTipText("Change font and colors of items on the graph");
    optionMenu.add(looksMenu);
    for (int i=cut;i<actions.length;i++)
      looksMenu.add(actions[i]);
      
    yGridCheck=new JCheckBoxMenuItem("yGrid");
    yGridCheck.addActionListener(this);
    yGridCheck.setState(true);
    yGridCheck.setToolTipText("turn yGrid lines on or off");
    xGridCheck=new JCheckBoxMenuItem("xGrid");
    xGridCheck.addActionListener(this);
    xGridCheck.setState(true);
    xGridCheck.setToolTipText("turn xGrid lines on or off"); 
    lineLabelCheck=new JCheckBoxMenuItem("LineLabels");
    lineLabelCheck.addActionListener(this);
    lineLabelCheck.setState(true);
    lineLabelCheck.setToolTipText("turn line labels on or off");
    
    offOnMenu=new JMenu("TurnOff");
    offOnMenu.setToolTipText("Turn menu items off and on");
    optionMenu.add(offOnMenu);
    
      offOnMenu.add(yGridCheck);
      offOnMenu.add(xGridCheck);
      offOnMenu.add(lineLabelCheck);
    menuBar.add(fileMenu);
    menuBar.add(optionMenu);
    getRootPane().setJMenuBar(menuBar);
     makeBlackWhiteObject(); //set up colors for Black and white look  
    
}/*end createMenu()*/
  
  public void makeBlackWhiteObject(){
      bwColors=new GColors();
      for (int i=0;i<bwColors.lineColor.length;i++)
      bwColors.lineColor[i]=Color.black;  
      bwColors.outerBackground=Color.white;
      bwColors.innerBackground=Color.white;
      bwColors.textColor=Color.black;
      bwColors.axisColor=Color.black;
      bwColors.gridColor=Color.black;
  }
  
  class FontAction extends AbstractAction {
   public FontAction(){
       super("Font");
   }    
   public void actionPerformed(ActionEvent event){
       fontPicker.setVisible(true);
   }    
  }/*end action class*/

 class SetYAxisAction extends AbstractAction {
     AGraph ag;
   public SetYAxisAction(String num){
       super("SetYAxis"+num);
       ag=parentFig.ag[Integer.parseInt(num)];//keep track of the graph
   }    
   public void actionPerformed(ActionEvent event){    
	     axisDialog.display(ag);
   }    
  }/*end action class*/


 class PrintAction extends AbstractAction {
   public PrintAction(){
     // super("Print",new ImageIcon(saveDir+"printer.gif"));
    super("Print");
   }    
   public void actionPerformed(ActionEvent event){
      FigurePrintable fp=new FigurePrintable(parentFig);
      fp.startPrintJob();
   }    
  }/*end action class*/
  
   class AddTitleAction extends AbstractAction {
   public AddTitleAction(){
       super("Add Title");
   }    
   public void actionPerformed(ActionEvent event){
     String title=JOptionPane.showInputDialog("Enter a Title");
     setGTitle(title);
   }  
  }/*end action class*/


    /**
     *Adds a title to graph.  Useful for external calling
     */
    public void setGTitle(String title){
	gColors.aTitle=title;
	userTitle.setText(title);
    }

    /**
     *Get the graph title.  Useful for external calling
     */
    public String getGTitle(){
	return(userTitle.getText());
    }

class SaveColorsAction extends AbstractAction {
   public SaveColorsAction(){
       super("SaveColors");
   }    
   public void actionPerformed(ActionEvent event){
     try{  
     FileOutputStream fileOut=new FileOutputStream(saveDir+"saveColors"+id+".hbp"); 
     ObjectOutputStream out=new ObjectOutputStream(fileOut);
     out.writeObject(gColors); //save the current color configuration
     } catch(IOException e){System.out.println("Unable to save your colors. "+e);}
   }    
  }/*end action class*/

class LoadColorsAction extends AbstractAction {
   public LoadColorsAction(){
       super("LoadColors");
   }    
   public void actionPerformed(ActionEvent event){
    loadColors();
   }    
  }/*end action class*/
  
 /**
 *Load previously saved colors
 */ 
  public void loadColors(){
    try{   
    FileInputStream fileIn=new FileInputStream(saveDir+"saveColors"+id+".hbp");  
    ObjectInputStream in=new ObjectInputStream(fileIn);
    gColors=(GColors)in.readObject();
    }catch(Exception e){}
    setColors();   
  }/*end loadColors()*/

 class OuterColorAction extends AbstractAction {
   public OuterColorAction(){
       super("OuterBackGround");
   }    
   public void actionPerformed(ActionEvent event){
       gColors.outerBackground=JColorChooser.showDialog(
	parentFig, "Pick Outer Background Color",
	ag[1].getBackground());
    setColors();
   }    
  }/*end action class*/

 class BlackWhiteAction extends AbstractAction {
   public BlackWhiteAction(){
       super("Black&White");
   }    
   public void actionPerformed(ActionEvent event){
    gColors=bwColors;   
    setColors();
   }    
  }/*end action class*/

 class HBPColorsAction extends AbstractAction {
   public HBPColorsAction(){
       super("HabplanColors");
   }    
   public void actionPerformed(ActionEvent event){
    gColors=new GColors();   
    setColors();
   }    
  }/*end action class*/
  
 class InnerColorAction extends AbstractAction {
   public InnerColorAction(){
       super("InnerBackGround");
   }    
   public void actionPerformed(ActionEvent event){
       gColors.innerBackground=JColorChooser.showDialog(
	parentFig, "Pick Inner Background Color",
	ag[1].ax.bgColor);
    setColors();
   }    
  }/*end action class*/
  
   class TextColorAction extends AbstractAction {
   public TextColorAction(){
       super("TextColor");
   }    
   public void actionPerformed(ActionEvent event){
       gColors.textColor=JColorChooser.showDialog(
	parentFig, "Pick Text Color",
	ag[1].xWrite.color);
    setColors();
   }    
  }/*end action class*/
  
    class AxisColorAction extends AbstractAction {
   public AxisColorAction(){
       super("AxisColor"); 
   }    
   public void actionPerformed(ActionEvent event){
       gColors.axisColor=JColorChooser.showDialog(
	parentFig, "Pick Axis Color",
	ag[1].ax.color);
    setColors();
   }    
  }/*end action class*/
  
   class GridColorAction extends AbstractAction {
   public GridColorAction(){
       super("GridColor");
   }       
   public void actionPerformed(ActionEvent event){
       gColors.gridColor=JColorChooser.showDialog(
	parentFig, "Pick Grid Color",
	ag[1].xGrid.color);
    setColors();
   }    
  }/*end action class*/
  
   class LineColorAction extends AbstractAction {
   public LineColorAction(){
       super("LineColor");
   }    
   public void actionPerformed(ActionEvent event){
       int line=0;
       int skipLine=0;
       
      DONE: for (int i=1;i<=rows*cols;i++){ 
	 skipLine=0; //reset for each graph in the figure
      for (int j=0;j<ag[i].grafItem.size();j++){
	  GraphComponent gc=(GraphComponent)ag[i].grafItem.elementAt(j);  
       if (gc instanceof Line){
	   skipLine++;
	 if (skipLine<=line)continue; //already got a color for this line;
	  gColors.lineColor[line]=JColorChooser.showDialog(
	   parentFig, "Pick Color for Line"+Integer.toString(line+1),
	    gc.color);
	setColors();    
	 line++;   
	if (line>=gColors.lineColor.length)break DONE; //done all colors available 
       }//end if
      }//end for j
      }//end for i    
   }    
  }/*end action class*/
  
  /**
  *Inner class to create a dialog to enter axis scale values
  */
  class AxisDialog extends JDialog
	implements ActionListener {
	 AGraph agDialog;  //the graph that this dialog should point to
	 private JPanel buttonPanel, textPanel;
	 private JTextField yMinText, yMaxText;
	 private JLabel yMinLabel, yMaxLabel;
	 private JButton closeButton, setButton, unSetButton;      
      public AxisDialog(String title){
	  super();
	  setTitle(title);
	  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	  setVisible(false);
	  
	  yMinText=new JTextField("0",10);
	  yMaxText=new JTextField("0",10);	
	  yMinLabel=new JLabel("Enter yMin"); 
	  yMaxLabel=new JLabel("Enter yMax"); 
	  textPanel=new JPanel();
	  textPanel.setLayout(new GridLayout(2,2,2,1));
	  textPanel.add(yMinLabel);
	  textPanel.add(yMinText);
	  textPanel.add(yMaxLabel);
	  textPanel.add(yMaxText);
	  
	 closeButton=new JButton("Close");
	  setButton= new JButton("Set");
	  unSetButton= new JButton("UnSet");
	  closeButton.addActionListener(this);
	  setButton.addActionListener(this);
	  unSetButton.addActionListener(this);
	  buttonPanel=new JPanel();
	  buttonPanel.add(closeButton);
	  buttonPanel.add(setButton);
	  buttonPanel.add(unSetButton);
	  
	  Container c=getContentPane();
	  c.setLayout(new GridLayout(2,1,2,1));
	  c.add(textPanel);
	  c.add(buttonPanel);
	  pack();
      }
      /**
      *handle button clicks
      */
      public void actionPerformed(ActionEvent e){
	 if(e.getActionCommand().equals("Close"))setVisible(false); 
	 if(e.getActionCommand().equals("Set"))set();
	 if(e.getActionCommand().equals("UnSet"))unSet(); 
      }
      
      /**
      *set the yAxis on associated AGraph
      */
      public void set(){
	  float yMin=Float.valueOf(yMinText.getText().trim()).floatValue();
	  float yMax=Float.valueOf(yMaxText.getText().trim()).floatValue();
	  agDialog.setYAxis(yMin,yMax);
      }
      
      /**
      *UnSet the yAxis on associated AGraph
      */
      public void unSet(){
	 agDialog.unSetYAxis(Float.NaN,Float.NaN);
      }
      
      
      /**
      *make dialog visible and get pointer to a particular graph
      */
      public void display(AGraph ag){
	  setVisible(false);
	  pack();//make sure it looks ok
	  setLocationRelativeTo(ag);
	  setVisible(true);
	  agDialog=ag;
      }/*end display()*/
  }/*end AxisDialog class*/
  
  /**
  *set the colors according to the current values in GColor instance
  *note: this is erroneously called whenever you click on a textfield in
  *the FlowForm and resets everything. Thats why title is commented out
  */
public void  setColors(){
    setFont(gColors.textFont);
    for (int i=1;i<=rows*cols;i++){
	ag[i].xGrid.setDrawState(gColors.xGridState);
	ag[i].yGrid.setDrawState(gColors.yGridState);
	ag[i].ax.bgColor=gColors.innerBackground;
	ag[i].setBackground(gColors.outerBackground);
	ag[i].xWrite.color=gColors.textColor;
	ag[i].yWrite.color=gColors.textColor;
	ag[i].xGrid.color=gColors.gridColor;
	ag[i].yGrid.color=gColors.gridColor;
	ag[i].xTics.color=gColors.gridColor;
	ag[i].yTics.color=gColors.gridColor;
	ag[i].ax.color=gColors.axisColor;
    }/*end for i*/
     titlePanel.setBackground(gColors.outerBackground);
     userTitle.setForeground(gColors.textColor);
    
     //userTitle.setText(gColors.aTitle);
     
    xGridCheck.setState(gColors.xGridState);
    yGridCheck.setState(gColors.yGridState);
    bufferCheckBox.setState(gColors.bufferState);
    lineLabelCheck.setState(gColors.lineLabelState);
    repaintManager.setDoubleBufferingEnabled(gColors.bufferState);
    
	//do line colors
	int line=0;
      for (int i=1;i<=rows*cols;i++){
	DONE:   
      for (int j=0;j<ag[i].grafItem.size();j++){
	  GraphComponent gc=(GraphComponent)ag[i].grafItem.elementAt(j);
       if (gc instanceof Line){
	 gc.setColor(gColors.lineColor[line]);
	 if (line<GraphComponent.lineStroke.length)gc.setStroke(GraphComponent.lineStroke[line]);
	 else gc.setStroke(GraphComponent.lineStroke[1]);
	 line++;   
	if (line>=gColors.lineColor.length)break DONE; //done all colors available 
       }//end if
      }//end for j
       line=0;
      }//end for i   
	
	//do line label on off
      for (int i=1;i<=rows*cols;i++){
	DONE2:   
      for (int j=0;j<ag[i].grafItem.size();j++){
	  GraphComponent gc=(GraphComponent)ag[i].grafItem.elementAt(j);
       if (gc instanceof LineLabel) gc.setDrawState(gColors.lineLabelState);
      }//end for j
      }//end for i   
	
   repaint();
}/*end setColors()*/
  
  /**
  *Set the fonts of all contained Agraph instances.
  */  
  public void setFont(Font font){
      gColors.textFont=font;//set permanently when called from fontpicker
     for(int i=1;i<=rows*cols;i++)ag[i].font=font;
     userTitle.setFont(font);
  }
  
  public static void main(String[] args){
      float[] x={0,5,10,20,30,40,50};
      float[] y={5,5, 120, 200, 250, 330, 450};
      float[] y1={5,120, 200, 250, 330, 500,350};
      float[] x2={0,15,20,25,30,35,40,45};
      float[] y2={500,5000000,7000000,9000000,8000000,10000000,11000000,12900000};
      Figure fig1=new Figure("Test Figure",3,1); 
      fig1.ag[1].plot(x,y1,"line1",.05,.8);
      fig1.ag[1].plotHold(x,y,"line2",.05,.6);
      fig1.ag[2].plot(x2,y2,"line1",.05,.8);
      fig1.ag[3].plot(x2,y2,"line1",.05,.8); 
      fig1.loadColors();  //add the users specified colors   
  }/*end main*/  
  
  
} /*end Figure class */
