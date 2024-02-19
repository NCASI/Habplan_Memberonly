import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import table.*;

/**
 *Convert an MPS file into Habplan format
 *@author Paul Van Deusen  NCASI 7/2004
 */


public class MpsRead extends JFrame {
    Vector xVar, cVec; //xVariable names and obj coefficients
    JProgressMeter progress; //used when writing out an MPS file
    JButton readButton, polyButton, closeButton;
    JFileButton fileButton;
    JTextField textField, columnField, splitField;
    int splitCol=9; //user specified column to split polygon/regime names
    JCheckBox splitBox;
    String splitChar; //what separates polygon/regime pairs
    boolean isSplitChar=true; //are we splitting on a character?
    static JLabel outLabel; 
    Color bgColor=new Color(204,204,255);
    BasicTable polyTable;
    SelectionTool polySelect;
    //change this later
    String fName="/home/pvandeus/myjava/Habplan2/LP/habplan2LP.mps";
    
    //this can be run in stand-alone mode
     public static void main (String args[]) {
	System.out.println("MpsRead is running");
	MpsRead mpsRead=new MpsRead();
	mpsRead.show();
    }

    public MpsRead(){
	super("Habplan MPS Reader");
	setBounds(400,460,375,250);
	polyTable=new BasicTable(this,"Polygon Table");
        polySelect=new SelectionTool(polyTable,"Select Rows in PolyTable");
	polyTable.createTable();
	polyTable.pack();
        //GUI file section
	textField=new JTextField(fName,30);
	textField.setToolTipText("MPS input file");
	fileButton=new JFileButton("File",this,textField,null,null);
        fileButton.button.setToolTipText("Select a file for MPS output");
	 JPanel filePanel=new JPanel();
	filePanel.setBackground(bgColor);
	filePanel.add(fileButton);
	filePanel.add(textField);
	getContentPane().add(filePanel,BorderLayout.NORTH);
	//input panel section
	columnField=new JTextField(null,2);
	columnField.setToolTipText("Enter last column of variable name");
	splitField=new JTextField(null,2);
	splitField.setToolTipText("The character that splits the variable and regime names");
	columnField.setEnabled(false); 
	final JLabel columnLabel=new JLabel("Polygon/Regime Split Column");
	final JLabel splitLabel=new JLabel("Split Character");
	columnLabel.setEnabled(false);
	splitBox=new JCheckBox("Use Split Character",true);
	splitBox.setBackground(bgColor);
	splitBox.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e){ 
		    columnField.setEnabled(false); 
		    splitField.setEnabled(false);
		    columnLabel.setEnabled(false); 
		    splitLabel.setEnabled(false);
		    isSplitChar=false;
		    if( e.getStateChange()==ItemEvent.SELECTED){
			splitField.setEnabled(true); 
			splitLabel.setEnabled(true);
			splitChar=splitField.getText();
			isSplitChar=true;
		    }else{
			columnField.setEnabled(true);
			columnLabel.setEnabled(true);}
		}/*end itemStateChanged()*/ 
	    }); 
        
	JPanel inputPanel=new JPanel();
	inputPanel.setBackground(bgColor);
	inputPanel.add(columnLabel);
	inputPanel.add(columnField);
	inputPanel.add(splitBox);
	inputPanel.add(splitLabel);  
	inputPanel.add(splitField); 
	getContentPane().add(inputPanel,BorderLayout.CENTER);
	//button section
	Font font = new Font("SansSerif", Font.BOLD, 10);
	closeButton = new JButton("Close");
	closeButton.addActionListener(new ButtonListener());
        closeButton.setFont(font);
	closeButton.setToolTipText("Make this window go away");
	readButton = new JButton("Read");
	readButton.addActionListener(new ButtonListener());
        readButton.setFont(font);
	readButton.setToolTipText("Read the MPS file");
	polyButton = new JButton("Polygon");
	polyButton.addActionListener(new ButtonListener());
        polyButton.setFont(font);
	polyButton.setToolTipText("Open the Polygon Table");
	JPanel buttonPanel=new JPanel();
	buttonPanel.setBackground(bgColor);
	buttonPanel.add(readButton);
	buttonPanel.add(polyButton);
	buttonPanel.add(closeButton);      
	getContentPane().add(buttonPanel,BorderLayout.SOUTH);

        pack();	
    	// setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
              onClosing();
	      dispose();
	  }
	 });   
    }/*end constructor*/

    /**
     *Close all associated windows
     */
    public void onClosing(){
	polySelect.dispose();
	polyTable.dispose();
    }

    /**
     *Read an MPS file
     *@param fName the complete path to the MPS file
     */
    public void readMps(String fName){
        xVar=new Vector(1000,1000);
        cVec=new Vector(1000,1000);
	String line; // a line from mps file
        String[] token; //a line from mps file broken into tokens 
	String section="BEGIN"; //the section of the mps file being read
        String name=null;  //the name of the mps file problem on line one
        String obj=null;  // the name of the objective function row
	fName=textField.getText();
	BufferedReader in=null;

	try{	 
      in = new BufferedReader(new FileReader(fName),1024);
  
    while ((line=in.readLine())!=null){ 
        line=line.trim(); //get rid of leading and trailing white space
	//System.out.println(line);
	token=line.split("\\s+"); //split on white space
        if(token[0].equals("NAME"))name=token[1];
        else if(token[0].equals("ROWS"))section="ROWS";
	else if(token[0].equals("COLUMNS"))section="COLUMNS";
	else if(token[0].equals("RHS"))section="RHS";
	//get name of objective function row
	if(section.equals("ROWS") && token[0].equals("N"))obj=token[1];
	//get the obj function coefficients for BIO2 data
        if(section.equals("COLUMNS") && token.length>1 ){
	    if(token[1].equals(obj)){
		xVar.add(token[0]);
		cVec.add(token[2]);
	    }
	    //handle 2 row per line format
	    if(token.length==5){
		if(token[3].equals(obj)){
		    xVar.add(token[0]);
		    cVec.add(token[4]);
		}
	    }
	}

    }
	}catch(Exception e){System.out.println("MpsRead.readMPS() error: "+e);}
        finally{try {
	    if (in != null) in.close();
        }catch (IOException ex ){System.out.println("Cant close mpsfile:"+ ex);
      }
	}

	getBio2();

    }/*end method*/

    /**
     *Extract the BIO2 data from xVar and cVec
     */
    public void getBio2(){
	Vector data=new Vector(1000,1000);
	String[] value={"1","2","3"};
	String[][] bData;
	int loc=-1; //location of splitChar
	int skip=0; //skips the splitChar if isSplitChar is true
	 String xLine,polygon,regime;
	if(isSplitChar){
	    splitChar=splitField.getText().trim();
	    skip=1;
	}
	else loc=Integer.parseInt(columnField.getText().trim());
	    for(int i=0;i<xVar.size();i++){
                xLine=((String)xVar.get(i)).trim();
                if(isSplitChar)loc=xLine.indexOf(splitChar);
		if(loc<0)continue;//no splitChar
		polygon=xLine.substring(0,loc);
		regime=xLine.substring(loc+skip);
		value=new String[3];
		value[0]=polygon;
		value[1]=regime;
		value[2]=((String)cVec.get(i)).trim();
		// System.out.println(value[0]+" "+value[1]+" "+value[2]);
		data.add(value);
	    }
            bData=new String[data.size()][3];
	    for(int i=0;i<data.size();i++){
		bData[i]=(String[])(data.get(i));
		//System.out.println(bData[i][0]+" "+bData[i][1]+" "+bData[i][2]);
	    }
	    if(data.size()==0){
		System.out.println("Couldn't find any decision vars, check the field or split character");
		return;
	    }

	    polyTable.data.changeData(bData);
	    polyTable.model.fireTableStructureChanged();
	    polyTable.setVisible(true);
	
    }
   
     /**
     * Handle button clicks
     **/   
    public class ButtonListener implements ActionListener{

	public ButtonListener(){
	    super();
	}
        
	public void actionPerformed(ActionEvent e) {
	    String cmd = e.getActionCommand();
    
	    if (cmd.equals("Close")) {
		onClosing();
		setVisible(false); dispose();  
		return;  
	    }   
	    
	    if (cmd.equals("Read")) {
		readMps(fName);  
		return;  
	    }  
	     if (cmd.equals("Polygon")) {
		polyTable.setVisible(!polyTable.isVisible());  
		return;  
	    }  
	}/*end actionperformed()*/

    } /*end inner ButtonListener class*/

}

