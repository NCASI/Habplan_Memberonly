import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

/**
 *Set up required vectors and matrix for an LP run directly from
 *an existing Habplan data set
 *Using the following LP framework
 *
 *max cvec*X
 *s.t. Amat<bvec
 *the methods in here fill out cvec, bvec and Amat
 *these can be passed to LpMps to make an mps file
 *It would be easy to make other output formats as well.
 *@author Paul Van Deusen  NCASI 12/2003
 */


public class LpBuild extends JFrame {
   
   FrameMenu parent;
   RunThread thread;
    LpRun lpRun;
    JProgressMeter progress; //used when writing out an MPS file
    JTextBox[] textBox; //an inner class
    JButton mpsButton, closeButton, lpButton;
    JFileButton fileButton;
    JTextField textField, nameField;
    static JLabel outLabel; 

    private int nc; //n of components
    Color bgColor;
    int nFlows=0, nCCFlows=0, bio2Index=0;
    int[] flowIndex, ccFlowIndex;

    //****variables that hold components sent to MpsWrite
    int nPolyRegs=0; //holds the total number of polygons crossed with valid regimes
    int nAccounting=0; //holds number of accounting variables
    int nFlowAccounting=0; //holds number of flow accounting vars

    int nUserFlows=0; //holds n of rows added for user requested flow constraints
    int nUserCCFlows=0; //holds n of rows added for user requested ccflow constraints
    Vector bvec, cvec, bnames, cnames, rowType;
    int[][] scheds; //contains the valid schedules for each polygon
    LpSparse Amat;
    //*************************end MPS vars**************************
    	  
    public LpBuild(FrameMenu parental){
	super("LP MPS Generator");
	this.parent=parental;
	setBounds(400,460,375,250);

	nc=parent.nComponents;
	bgColor=Color.getColor("habplan.lpbuild.background");
     
	String file=UserInput.LPDir + "habplan2LP.mps";
	//remove this after testing
        //file="/home/pvandeus/projects.d/habplanLP/habplan2LP.mps";
	textField=new JTextField(file,20);
	textField.setToolTipText("File name for MPS output");
	fileButton=new JFileButton("File",this,textField,null,null);
        fileButton.button.setToolTipText("Select a file for MPS output");
        JLabel nameLabel=new JLabel("Problem Name");
	nameLabel.setToolTipText("This name goes in the MPS output file");
	nameField = new JTextField("Habplan2MPS",20);
	nameField.setToolTipText("Put a problem name here");
	
        JPanel filePanel=new JPanel();
	filePanel.setBackground(bgColor);
	filePanel.add(fileButton);
	filePanel.add(textField);
        JPanel namePanel=new JPanel();
	namePanel.setBackground(bgColor);
	namePanel.add(nameLabel);
	namePanel.add(nameField);
	JPanel savePanel=new JPanel(new BorderLayout());
        savePanel.setBackground(bgColor);
	savePanel.add(filePanel,BorderLayout.NORTH);
	savePanel.add(namePanel,BorderLayout.SOUTH);
	getContentPane().add(savePanel,BorderLayout.NORTH);
	

	
	Font font = new Font("SansSerif", Font.BOLD, 10);
	//button section
	closeButton = new JButton("Close");
	closeButton.addActionListener(new ButtonListener(this));
        closeButton.setFont(font);
	closeButton.setToolTipText("Make this window go away");
	mpsButton=new JButton("MPS");
	mpsButton.addActionListener(new ButtonListener(this));
	mpsButton.setFont(font);
	mpsButton.setToolTipText("Create the MPS File");
	lpButton=new JButton("LpOpen");
	lpButton.addActionListener(new ButtonListener(this));
	lpButton.setFont(font);
	lpButton.setToolTipText("Open the LP Run Control Panel");
        lpButton.setEnabled(false);
	JPanel buttonPanel=new JPanel();
	buttonPanel.setBackground(bgColor);
	buttonPanel.add(closeButton);      
        buttonPanel.add(mpsButton);
	buttonPanel.add(lpButton);
	getContentPane().add(buttonPanel,BorderLayout.CENTER);

	//Build MPS Component Panel
	JPanel mpsPanel=new JPanel();
	mpsPanel.setLayout(new BorderLayout());
         mpsPanel.setBackground(new Color(100,100,170));
	int cols=4;
	try{cols=Integer.parseInt(System.getProperty("habplan.config.nrow").trim());}
	 catch(Exception e){ /*leave cols=4*/};
	int rows=(int)(Math.ceil((float)nc/cols)); 
	JPanel selectPanel=new JPanel();
	selectPanel.setBackground(bgColor);
        selectPanel.setLayout(new GridLayout(rows,cols,1,1));
	textBox=new JTextBox[nc+1];
	for (int i=1;i<=nc;i++){
	  textBox[i]=new JTextBox(parent.componentName[i],parent.form[i],i);
	  selectPanel.add(textBox[i]);  
        }/*end for i*/
	JScrollPane selectJPane = new JScrollPane(selectPanel);
	  mpsPanel.add(selectJPane,"South"); 
	  //output label for printing output
	   outLabel=new JLabel("Components to Contribute to MPS Output",JLabel.CENTER);
	   outLabel.setForeground(Color.white);
	   outLabel.setBackground(Color.BLUE);
	  mpsPanel.add(outLabel,"North"); 
	 getContentPane().add(mpsPanel,"South");   
	
	pack();
	// setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
              closeMpsWindows();
	      dispose();
	  }
	 });   
    }/*end constructor*/

 
    /**
     * Handle button clicks
     **/   
    public class ButtonListener implements ActionListener{
        LpBuild lpBuild;

	public ButtonListener(LpBuild lpBuild){
	    super();
	    this.lpBuild=lpBuild;
	}
        
	public void actionPerformed(ActionEvent e) {
	    String cmd = e.getActionCommand();
    
	    if (cmd.equals("Close")) {
		closeMpsWindows();
		setVisible(false); dispose();  
		return;  
	    }   
	     if (cmd.equals("MPS")) {
                 if(lpRun!=null){
		     lpRun.destroy();//kill current lp solver run
		     lpRun.setVisible(false);
		     lpRun.textArea.setText(" ");
		 }
		 buildMPS(); 
		 lpButton.setEnabled(true);
		return;  
	    } 
	       if (cmd.equals("LpOpen")) {
                   if(lpRun==null) lpRun=new LpRun(lpBuild);
		   lpRun.setVisible(!lpRun.isVisible());
		return;  
	    }   
	}/*end actionperformed()*/

    } /*end inner ButtonListener class*/


    /**
     *Method to close all widows related to Mps when main window
     *closes This keeps things from getting too messy for the user
     */
    protected void closeMpsWindows(){
	for (int i=1;i<=nc;i++){  
	 if(textBox[i].configMps!=null)
            textBox[i].configMps.setVisible(false); dispose();
	 if(textBox[i].configObj!=null)
	    textBox[i].configObj.setVisible(false); dispose();
	 if(lpRun!=null)lpRun.setVisible(false);
        }/*end for i*/
    }

    /**
     *Method to build the vectors and matrices required by
     *the WriteMPS class.   This calls the methods that do the
     *real work.  Each method called returns a boolean to indicate
     *success
     */
    protected void buildMPS(){
	thread=new RunThread(this); //create the mps file
        thread.start();
	progress=new JProgressMeter(this,thread,getTitle());
        progress.work();
    }/*end buildMPS()*/
    
    /**
     *Output tables to allow translation between internal Habplan regime
     *and polygon id numbers and the user supplied string name
     *The uses the same file prefix as the mps file, but adds different
     *suffixes
     */
    public boolean translationTables(){
        
        //first set up file output streams
	PrintWriter outreg, outpoly;
	String prefix=textField.getText();//this will be the file prefix
        String polyfile, regfile;
	int dotLoc=prefix.lastIndexOf('.');//locate period in the filename
        if(dotLoc!=-1) prefix=prefix.substring(0,dotLoc);
        regfile=prefix.concat(".regime");
	polyfile=prefix.concat(".poly");
	try{
	    outpoly = new PrintWriter(new BufferedWriter(new FileWriter(polyfile)));
	     outreg = new PrintWriter(new BufferedWriter(new FileWriter(regfile)));
	}catch(IOException e){
	    parent.console.println("LpBuild.translationTables():\n"+e);
	    return false;
	}

	//now output the regime and polygon translation tables
	outpoly.println("\"habplanID\",\"name\"");//csv file header
	for(int j=0;j<ObjForm.polyArray.size();j++){
	    outpoly.println((j+1)+", "+ObjForm.polyArray.elementAt(j));
	}
        outreg.println("\"habplanID\",\"name\"");//csv file header
	for(int j=1;j<ObjForm.regimeArray.size();j++){
	     outreg.println(j+", "+ObjForm.regimeArray.elementAt(j));
	}
    
	outpoly.close();
        outreg.close();
	return true;
    }

    /**
     *Make sure that the required data files are read and Habplan is
     *ready to go.  Get the indices of the selected components.
     *@return true if the data are read in without error.
     */
    private boolean readData(){
	nFlows=0;
	nCCFlows=0;
	bio2Index=0;
	for (int i=1;i<=nc;i++){
	    if(textBox[i].checkBox.isSelected()){
		parent.compBox[i].setSelected(true);//check the box on main window
		parent.manualItemStateChanged();
		if(!textBox[i].form.dataRead)return false;//there was a read error
		if(textBox[i].form instanceof FlowForm)nFlows++;
		if(textBox[i].form instanceof CCFlowForm)nCCFlows++;
		if(textBox[i].form instanceof Biol2Form){
		    if(bio2Index!=0)parent.console.println("Only using the last Bio2 Component that was checked.");
		    bio2Index=i;
		}
	    }else {
		parent.compBox[i].setSelected(false);
		parent.manualItemStateChanged();
	    }
        }/*end for i*/
	//now fill in flowIndex and ccFlowIndex
	flowIndex=new int[nFlows+1];
	ccFlowIndex=new int[nCCFlows+1];
        int j=0;
	for (int i=1;i<=nc;i++) if(textBox[i].checkBox.isSelected() && textBox[i].form instanceof FlowForm)flowIndex[++j]=i;
	j=0;
	for (int i=1;i<=nc;i++) if(textBox[i].checkBox.isSelected() && textBox[i].form instanceof CCFlowForm)ccFlowIndex[++j]=i;
	if(bio2Index==0){
	    parent.console.println("\n You need to select a Bio2 component!");
	    return false;
	}
	//	parent.console.println("bio2Index, nFlows, nCCFlows "+bio2Index+" "+nFlows+" "+nCCFlows);
	return true;
    }/*end readData()*/

     /**
     *Initialize the b vector (RHS) for the LP run.  This starts by
     *ensuring that acreage constraints are maintained
     */
    private boolean makeBvec(){
	bvec=new Vector(ObjForm.nPolys);//RHS coefficients
	bnames=new Vector(ObjForm.nPolys);
	rowType=new Vector(ObjForm.nPolys); //< is L, = is E etc
	
	//set up the constraint that regimes can be assigned to <=100% of the polygon
	for(int i=1;i<=ObjForm.nPolys;i++){
	    bvec.addElement(new Integer(1));
	    bnames.addElement("p"+i);
	    rowType.addElement("L");
	}

	return true;
    }/*end ()*/

    /*
     *Make cvec for an LP run.  This contains the objective
     *function coefficients which come from the Bio2 data. There is
     *generally one element for each polygon-regime combination. There
     *are additional elements for acounting variables.  The bvec (RHS
     *vector) is extended here for the accounting vars because it is
     *efficient to do so.
     */
    private boolean makeVecs(){

	cvec=new Vector(ObjForm.nPolys*10); //initial capacity allows for 10 regimes per polygon
	cnames=new Vector(ObjForm.nPolys*10);
	nAccounting=0;
	nFlowAccounting=0;
	nUserFlows=0; //keep count of user requested flow contraint rows
	nUserCCFlows=0;

	try{
	for(int i=1;i<=ObjForm.nPolys;i++){
	  
	    for(int k=1;k<scheds[i].length;k++){
		double b=((Biol2Form)(parent.form[bio2Index])).data[i].BofX(scheds[i][k]);
		cvec.addElement(new Double(b));
		//the full name is usually too long for an mps file format
		//	String aname=(String)ObjForm.polyArray.elementAt(i-1)+"$"+(String)ObjForm.regimeArray.elementAt(scheds[i][k]);
		String aname=i+"$"+scheds[i][k];
	    cnames.addElement(aname);
	    }
	    if (ObjForm.nScheds==0)parent.console.println("No Valid Schedules for polygon "+ObjForm.polyArray.elementAt(i-1));    
	}

	//now add accounting variables for each flow. One per year.
	FlowForm f;
	String aname;

	for(int i=1;i<=nFlows;i++){
	    f=((FlowForm)(parent.form[flowIndex[i]]));
	    aname = f.getTitle();
	    aname=aname.substring(0,aname.indexOf("Component")-1); //cut out component name, e.g. F1
	    int yrs=f.xMax-f.xMin+1; 
            nAccounting+=yrs; //keep track of total number of accounting vars
	    nFlowAccounting+=yrs; //keep track of n of flow accounting vars

	    for(int j=f.xMin;j<=f.xMax;j++){
		cvec.addElement(new Double(0.0));
	        cnames.addElement(aname+"Y"+j);
		bvec.addElement(new Integer(0)); //set up RHS for Flow accounting
		bnames.addElement(aname+"A"+j);
		rowType.addElement("E");
	    }
	}

		//add accounting variables for each CCFlow. One per year.
	CCFlowForm c;
	for(int i=1;i<=nCCFlows;i++){
	    c=((CCFlowForm)(parent.form[ccFlowIndex[i]]));
	    aname=c.getTitle();
	    aname=aname.substring(0,aname.indexOf("Component")-1); //cut out component name, e.g. C1(1)
	    int yrs=c.flowform.xMax-c.flowform.xMin+1; 
	    nAccounting+=yrs; //keep track of number of accounting vars

	    for(int j=c.flowform.xMin;j<=c.flowform.xMax;j++){
		cvec.addElement(new Double(0.0));
	        cnames.addElement(aname+"Y"+j);
		bvec.addElement(new Integer(0)); //set up RHS for CCFlow accounting
		bnames.addElement(aname+"A"+j);
		rowType.addElement("E");
	    }
	}

	//now add user requested flow constraints to bvec
	//i.e. place restrictions on accounting vars
	for(int i=1;i<=nFlows;i++){
	    f=((FlowForm)(parent.form[flowIndex[i]]));
	    int yrs=f.xMax-f.xMin+1; 
	    aname = f.getTitle();
	    aname=aname.substring(0,aname.indexOf("Component")-1); //cut out component name, e.g. F1
	    ConfigMps cm=textBox[flowIndex[i]].configMps;//pointer to flow config form
	    if(cm.selected==0)continue;//user selected nothing

	    for(int j=1;j<=yrs;j++){
		if(cm.selected==1 && j<yrs){//there are yrs-1 constraints
		    bvec.addElement(new Integer(0)); //RHS Flow accounting 1
		    bvec.addElement(new Integer(0));
		}else if(cm.selected==2){
		    if(j>=cm.textArea.Hi.length){
			parent.console.println("Error: LP Year ranges wrong for "+aname);
			continue;
		    }
		    bvec.addElement(new Double(cm.textArea.Hi[j])); //RHS Flow accounting 1
		    bvec.addElement(new Double(-cm.textArea.Lo[j]));//greater than const.
		}

		if(j==yrs && cm.selected==1)continue;
		bnames.addElement(aname+"H"+j);
		rowType.addElement("L");
		bnames.addElement(aname+"L"+j);
		rowType.addElement("L");
		nUserFlows+=2;//added 2 more rows to bvec
	    }
	}

	//now add user requested ccflow constraints to bvec
	//i.e. place restrictions on accounting vars
	for(int i=1;i<=nCCFlows;i++){
	    c=((CCFlowForm)(parent.form[ccFlowIndex[i]]));
	    aname=c.getTitle();
	    aname=aname.substring(0,aname.indexOf("Component")-1); //cut out component name, e.g. F1
	    int yrs=c.flowform.xMax-c.flowform.xMin+1; 
	
	    ConfigMps cm=textBox[ccFlowIndex[i]].configMps;//pointer to flow config form
	    if(cm.selected==0)continue;//user selected nothing

	    for(int j=1;j<=yrs;j++){ 
		if(cm.selected==1 && j<yrs){//there are yrs-1 constraints
		    bvec.addElement(new Integer(0)); // RHS  ccFlow accounting 1	    
		    bvec.addElement(new Integer(0)); 
		}else if(cm.selected==2){
		    if(j>=cm.textArea.Hi.length){
			parent.console.println("Error: LP Year ranges wrong for "+aname);
			continue;
		    }
		    bvec.addElement(new Double(cm.textArea.Hi[j])); //RHS ccFlow 2
		    bvec.addElement(new Double(-cm.textArea.Lo[j]));//greater than const.
		}

		if(j==yrs && cm.selected==1)continue;
		bnames.addElement(aname+"H"+j);
		rowType.addElement("L");
		bnames.addElement(aname+"L"+j);
		rowType.addElement("L");
		nUserCCFlows+=2; 
	    }
	}


	//for(int i=0;i<cvec.size();i++)  parent.console.println(cnames.elementAt(i)+" "+cvec.elementAt(i));	
	}catch(Exception e){
	    parent.console.println("\nLpBuild.makeCvec() Error\n"+e);
	    return false;
	}
	return true;

    }/*end ()*/


     /**
     *build the LP A matrix.  This should be called after cvec and
     *bvec are completed so we know the final values of rows and cols.
     */
    private boolean makeAmat(){
	int aRows=bvec.size();//rows and cols of Amat
	int aCols=cvec.size();
	Amat=new LpSparse(aRows,aCols,10);
	//	parent.console.println("aRows aCols nPolyRegs "+aRows+" "+aCols+" "+nPolyRegs);
	//first do the acreage constraints
	int  startCol=0, startRow=0;
        progress.progressBar.setString("Prep: 1 of 5");
	for(int poly=1;poly<=ObjForm.nPolys;poly++){//polygon
	    for(int reg=1;reg<scheds[poly].length;reg++){//regime
                Amat.put(poly,startCol+reg,1.0);
	    }
	    startCol+=scheds[poly].length-1;//increment the column position for next polygon
	}
        progress.progressBar.setString("Prep: 2 of 5");
	startRow=ObjForm.nPolys; //used first nPolys rows for acreage constraints
	
	//set up the Flow accounting
	FlowForm f;
	int row, fsize, t;

	for(int i=1;i<=nFlows;i++){
	    f=((FlowForm)(parent.form[flowIndex[i]]));
	    int yrs=f.xMax-f.xMin+1; 
	    for(int yr=1;yr<=yrs;yr++){//year
		 startCol=0;//shift over for the next polygon starting column
		 int nScheds; //keep track of n of schedules for a polygon
		for(int poly=1;poly<=ObjForm.nPolys;poly++){//polygon    
		    for(int reg=1;reg<scheds[poly].length;reg++){//regime
			row=f.data[poly].flowPtr(scheds[poly][reg]);  //the correct row in FLOW matrix
			fsize=(f.data[poly].flow[row].length-1)/2;
			for (int jj=fsize;jj>=1;jj--){  /*handles multiple output years*/
			    t=f.data[poly].flow[row][jj]-f.xMin+1; /*scaled year under current option*/
			    if(t==yr)Amat.put(startRow+yr,startCol+reg,f.data[poly].flow[row][fsize+jj]);
			}
		    }
		    startCol+=scheds[poly].length-1;//increment the column position for next polygon
		}
	    }
	    startRow+=yrs;
	}

	   //set up the A matrix for user requested flow constraints
		int userStart=ObjForm.nPolys+nAccounting; //1 less than the Amat row to start in
		int colStart=nPolyRegs;

	for(int i=1;i<=nFlows;i++){
	    ConfigMps cm=null;
	    f=((FlowForm)(parent.form[flowIndex[i]]));
	    int yrs=f.xMax-f.xMin+1; 
	    for(int yr=1;yr<=yrs;yr++){//year

	        cm=textBox[flowIndex[i]].configMps;//pointer to flow config form
		if(cm.selected==0)continue;//user selected nothing
	
		if(cm.selected==1){
		    double Hi=(new Double(cm.flowHi.getText())).doubleValue();
		    double Lo=(new Double(cm.flowLo.getText())).doubleValue();
                    if(yr<yrs){
                        userStart++;
                        colStart++;
			Amat.put(userStart,colStart,-Hi);
			Amat.put(userStart,colStart+1,1.0);
			Amat.put(userStart+1,colStart,Lo);
			Amat.put(userStart+1,colStart+1,-1.0);
			userStart++;
		    }
		} else if(cm.selected==2){
		    userStart++;
                    colStart++;
		    Amat.put(userStart,colStart,1.0);
		    Amat.put(userStart+1,colStart,-1.0);
		    userStart++;
		}
	    }
	    if(cm.selected==1)colStart++;
	}

	progress.progressBar.setString("Prep: 3 of 5");

	//set up the CCFlow accounting
	//usually restricted to single period regimes, but not required by this coding
	CCFlowForm c;

	for(int i=1;i<=nCCFlows;i++){
            boolean ccValid=false; //check if valid ccOpt
	    c=((CCFlowForm)(parent.form[ccFlowIndex[i]]));
	    f=c.flowform; //point to parent flowform
	    int yrs=f.xMax-f.xMin+1; 
	    
	    for(int yr=1;yr<=yrs;yr++){//year
		startCol=0;//shift over for the next polygon starting column
		int nScheds; //keep track of n of schedules for a polygon
		for(int poly=1;poly<=ObjForm.nPolys;poly++){//polygon    
		    for(int reg=1;reg<scheds[poly].length;reg++){//regime
			row=f.data[poly].flowPtr(scheds[poly][reg]);  //the correct row in FLOW matrix
			fsize=(f.data[poly].flow[row].length-1)/2;
			for (int jj=fsize;jj>=1;jj--){  /*handles multiple output years*/
			     ccValid=false;
			     for (int ii=1; ii<=c.nCCOpts;ii++)
				 if(scheds[poly][reg]==c.ccOpts[ii][1] && jj==c.ccOpts[ii][2]){ccValid=true;break;}
			     if (!ccValid)continue;  //dont waste anymore time on this one  
 
			    t=f.data[poly].flow[row][jj]-f.xMin+1; /*scaled year under current option*/
			    if(t==yr)Amat.put(startRow+yr,startCol+reg,c.data[poly].size);
			}
		    }
		    startCol+=scheds[poly].length-1;//increment the column position for next polygon
		}
	    }
	    startRow+=yrs;
	}



	//set up the A matrix for user requested ccflow constraints
	userStart=ObjForm.nPolys+nAccounting+nUserFlows; //Amat row to start in
	colStart=nPolyRegs+nFlowAccounting;

	for(int i=1;i<=nCCFlows;i++){
	    c=((CCFlowForm)(parent.form[ccFlowIndex[i]]));
	    ConfigMps cm=null;
	    f=c.flowform; //point to parent flowform
	    int yrs=f.xMax-f.xMin+1; 
	    
	    for(int yr=1;yr<=yrs;yr++){//year

		cm=textBox[ccFlowIndex[i]].configMps;//pointer to ccflow config form
		if(cm.selected==0)continue;//user selected nothing
	
		if(cm.selected==1){
		    double Hi=(new Double(cm.flowHi.getText())).doubleValue();
		    double Lo=(new Double(cm.flowLo.getText())).doubleValue();
		    
		     if(yr<yrs){
                        userStart++;
                        colStart++;
			Amat.put(userStart,colStart,-Hi);
			Amat.put(userStart,colStart+1,1.0);
			Amat.put(userStart+1,colStart,Lo);
			Amat.put(userStart+1,colStart+1,-1.0);
			userStart++;
		    }
		} else if(cm.selected==2){
		    userStart++;
                    colStart++;
		    Amat.put(userStart,colStart,1.0);
		    Amat.put(userStart+1,colStart,-1.0);
		    userStart++;
		}
	    }
	    if(cm.selected==1)colStart++;
	}
		        
	progress.progressBar.setString("Prep: 4 of 5");

	//put a -1 in the accounting variable columns to force the equality, i.e. bvec=0
	//start in row nPolys+1 and column nPolyRegs+1 and increment
	for(int rr=1;rr<=nAccounting;rr++){
	    Amat.put(ObjForm.nPolys+rr,nPolyRegs+rr,-1.0);
	}



	/**
	for(int i=419+1;i<(419+31);i++){
	    for(int j=1;j<Amat[1].length;j++){
		System.out.print(Amat[i][j]+" ");
	    }
	    System.out.println("");
	}
	**/
	progress.progressBar.setString("Prep: 5 of 5");
	return true;
     
    }/*end ()*/

	/**
	 *Function create a matrix of valid schedules for the polygons.
	 *It uses pre-existing functionality to fill ObjForm.validScheds
	 *for each polygon and stores results in scheds matrix
	 */
	private boolean getValidScheds(){
	    nPolyRegs=0; //total number of polygons crossed with valid regimes (most of the decision vars)
            scheds=new int[ObjForm.nPolys+1][];
	    try{
	     ObjForm.validScheds=new int[ObjForm.nOptions+1];
	    for(int i=1;i<=ObjForm.nPolys;i++){
		ObjForm.nScheds=-1; //-1 tells the component that its the first one
		for (int j=1;j<=parent.nComponents;j++){ 
		    if(parent.isAdded[j])ObjForm.nScheds=parent.form[j].getValidScheds(i,ObjForm.nScheds);
		}/*end for*/
	 
		nPolyRegs+=ObjForm.nScheds;
		scheds[i]=new int[ObjForm.nScheds+1];
		for (int j=1;j<=ObjForm.nScheds;j++)
		    scheds[i][j]=ObjForm.validScheds[j];
	    }
	    return true;
	    }catch(Exception e){
		parent.console.println("\nLpBuild.getValidScheds() Error\n"+e);
		return false;
	    }
	}/*end ()*/


   
  
    /**
     *inner class for specialized JtextField and CheckBox Component
     */
    class JTextBox extends JPanel{
	ConfigMps configMps;
	ConfigObj configObj;
	JCheckBox checkBox;
	JButton button;
	String type="NULLForm"; //selected components are given a meaningful name
	ObjForm form;
	final int num; //the component number that this is associated with

	public JTextBox(String name, ObjForm form, final int num){	
	
	    this.form=form;
	this.num=num;
        setBackground(bgColor);
	if(form instanceof Biol2Form)type="Biol2Form";
	else if(form instanceof CCFlowForm)type="CCFlowForm";
        else if(form instanceof FlowForm)type="FlowForm";

	if(type.equals("CCFlowForm") || type.equals("FlowForm"))	
	    configMps=new ConfigMps("MPS Config for "+form.getTitle());
	if(type.equals("Biol2Form"))	
	    configObj=new ConfigObj("OBJ Config for "+form.getTitle());

       button = new JButton("Config");
       button.setEnabled(false);
       button.addActionListener(new ActionListener(){

    public void actionPerformed(ActionEvent e) {
        if(type.equals("Biol2Form"))
          configObj.setVisible(!configObj.isVisible());
        else
	  configMps.setVisible(!configMps.isVisible());
    }});

       checkBox=new JCheckBox(name);
       if(type.equals("NULLForm"))checkBox.setEnabled(false);
     checkBox.setToolTipText("Check to add this component to the MPS file");
     setLayout(new FlowLayout());
       checkBox.addItemListener(new ItemListener(){
	public void itemStateChanged(ItemEvent e){ 
               button.setEnabled(false); 
	    if( e.getStateChange()==ItemEvent.SELECTED){
		button.setEnabled(true);
		parent.compBox[num].setSelected(true);//check the box on main window
		parent.manualItemStateChanged(); //readData immediately
	    }
	 }/*end actionPerformed()*/ 
    }); 

      add(checkBox);   
      add(button);
    }/*end constructor*/
    
}/*end inner TextBox class*/

 /**
  *inner class for configuring obj Flow components for MPS output
  */
    class ConfigMps extends JFrame implements ActionListener{
	JLabelTextField flowHi, flowLo;
	TextEntryArea textArea;
	JRadioButton button0,button1,button2;
	int selected=0; //indicates which option is selected
	public ConfigMps(String name){
	    super(name);
	    setLocationRelativeTo(this);
	    getContentPane().setLayout(new BorderLayout());
            JLabel evenLabel=new JLabel("Give upper and lower flow limits as proportions",JLabel.CENTER);
            getContentPane().setBackground(bgColor);

	    ButtonGroup group = new ButtonGroup();
	    button0=new JRadioButton("unSelect");
	    button0.setToolTipText("Select no constraints");
	    button0.setActionCommand("button0");
	    button0.addActionListener(this);
	    button0.setBackground(bgColor);
	    button0.setSelected(true);
	    group.add(button0);
            button1=new JRadioButton("Select");
	    button1.setToolTipText("Select this constraint");
	    button1.setActionCommand("button1");
	    button1.addActionListener(this);
	    button1.setBackground(bgColor);
	    group.add(button1);
            button2=new JRadioButton("Select");
	    button2.setActionCommand("button2");
	    button2.addActionListener(this);
	    button2.setBackground(bgColor);
	    group.add(button2);
	    JPanel evenPanel=new JPanel();
	    evenPanel.setBackground(bgColor);
	    evenPanel.setLayout(new GridLayout(2,1,1,1));
	    JPanel panel1=new JPanel();
	    panel1.setBackground(bgColor);
            flowHi=new JLabelTextField("UpperFlow",5,"float");
	    flowHi.setBackground(bgColor);
	    flowHi.setEnabled(false);
	    flowLo=new JLabelTextField("LowerFlow",5,"float");
	    flowLo.setBackground(bgColor);
	    flowLo.setEnabled(false);
	    panel1.add(button1);
            panel1.add(flowLo);
	    panel1.add(flowHi);
	    JPanel panel2=new JPanel(new BorderLayout());
	    panel2.setBackground(bgColor);
	    JLabel lab2=new JLabel("     Add year,lower,upper triplets separated by ;");
            textArea=new TextEntryArea(2,20);
	     JScrollPane scrollPane = 
		new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	    panel2.add(lab2,BorderLayout.NORTH);
	    panel2.add(button2,BorderLayout.WEST);
	    panel2.add(scrollPane,BorderLayout.CENTER);
	    evenPanel.add(panel1);
	    evenPanel.add(panel2); 
	    getContentPane().add(button0,BorderLayout.WEST);
            getContentPane().add(evenLabel,BorderLayout.NORTH);
	    getContentPane().add(evenPanel,BorderLayout.CENTER);
	pack();
	 setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      dispose();
	  }
	 });   
	 }/*end Constructor*/

	public void actionPerformed(ActionEvent e) {
             String cmd=e.getActionCommand();
             flowHi.setEnabled(false);
	     flowLo.setEnabled(false);
	     textArea.setEnabled(false);
	     if(cmd.equals("button0"))selected=0;//nothing selected
	     if(cmd.equals("button1")){//the first flow option
		 selected=1;
		 flowHi.setEnabled(true);
		 flowLo.setEnabled(true);
	     }
	     if(cmd.equals("button2")){//the second flow option
		 selected=2;
		 textArea.setEnabled(true);
	     }
	     
	     // parent.console.println("button is "+selected);
	    }

    }/*end ConfigMps Class*/

 /**
     *inner class for configuring objective function for MPS output
     */
    class ConfigObj extends JFrame implements ActionListener{
	boolean maximize=true;
	public ConfigObj(String name){
	    super(name);
	    setLocationRelativeTo(this);
	    getContentPane().setLayout(new GridLayout(2,2,1,1));
	    getContentPane().setBackground(bgColor);
            JLabel evenLabel=new JLabel("Maximize or Minimize the Objective Function?",JLabel.CENTER);
	    evenLabel.setBackground(bgColor);
	    JPanel evenPanel=new JPanel();
            evenPanel.setBackground(bgColor);
            JRadioButton maxButton=new JRadioButton("Max");
            maxButton.setActionCommand("Max");
            maxButton.setSelected(true);
	    JRadioButton minButton=new JRadioButton("Min"); 
	    minButton.setEnabled(false);  //min isn't an option yet
            minButton.setActionCommand("Min");
	    ButtonGroup group = new ButtonGroup();
	    group.add(maxButton);
	    group.add(minButton);
	    maxButton.addActionListener(this);
	    minButton.addActionListener(this);

            evenPanel.add(maxButton);
	    evenPanel.add(minButton);
	    getContentPane().add(evenLabel);
	    getContentPane().add(evenPanel);
	pack();
	 setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	      dispose();
	  }
	 });   
	 }/*end Constructor*/

	 public void actionPerformed(ActionEvent e) {
             String cmd=e.getActionCommand();
	     if(cmd.equals("Max"))
		   maximize=true;
	      else maximize=false;
	    }
    }/*end ConfigObj inner Class*/

    /**
     *inner class for entering detailed upper and lower flow bounds
     */
    class TextEntryArea extends JTextArea implements DocumentListener{
	JTextArea textArea;
	double[] Hi, Lo; //store flow upper and lower limits
	int[] yr; //store years to go with flow limits
	int minYr=10000000,maxYr=-1;
	Vector YY=new Vector(20),HH=new Vector(20),LL=new Vector(20);

	String text; 
	public TextEntryArea(int rows, int cols){
	    super(rows,cols);
	    setBackground(Color.red.brighter());
	    setEnabled(false);
	    getDocument().addDocumentListener(this);
	}

	public void insertUpdate(DocumentEvent e){

	    String text = getText().trim();
	    text=text.replace(',',' '); //get rid of commas
	    String sub,subY,subL,subH; //use to pull out numbers from the string
	    YY.clear();
	    LL.clear();
	    HH.clear();
	    minYr=1000000;
	    maxYr=-1;
	    int finish;
	    setBackground(Color.red);
	    try{
	    finish=0;
	    while (text.length()>0){
		finish = text.indexOf(";")+1; //index of next  separator
		finish=finish>0?finish:text.length();
		sub=text.substring(0,finish);
		text=text.substring(finish,text.length()).trim(); //throw out used part;
		subY=sub.substring(0,sub.indexOf(" ")).trim(); //get yr index
		sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
		subL=sub.substring(0,sub.indexOf(" ")).trim(); //get Lo flow
		sub=sub.substring(sub.indexOf(" ")+1,sub.length()); //trim used part
		subH=sub.substring(0,sub.indexOf(";")).trim(); //get Hi flow
		YY.addElement(subY);
		LL.addElement(subL);
		HH.addElement(subH);
	    }/*end while*/

	    /**for(int i=0;i<YY.size();i++)
		parent.console.println("subY subL subH "+YY.elementAt(i)+" "+LL.elementAt(i)+" "+HH.elementAt(i));
	    **/

	    Hi=new double[YY.size()+1];
	    Lo=new double[YY.size()+1];
	    yr=new int[YY.size()+1];

	    for(int i=0;i<YY.size();i++){
		yr[i+1]=Integer.parseInt((String)YY.elementAt(i));
		minYr=minYr<yr[i+1]?minYr:yr[i+1];
		maxYr=maxYr>yr[i+1]?maxYr:yr[i+1];
		Lo[i+1]=(new Double((String)LL.elementAt(i))).doubleValue();
		Hi[i+1]=(new Double((String)HH.elementAt(i))).doubleValue();
	    }
	    setBackground(Color.white);
	    }catch(Exception ec){return;}
	    if(maxYr-minYr+1>YY.size())modelFillIn();
	}

	public void removeUpdate(DocumentEvent e){insertUpdate(e);}
	public void changedUpdate(DocumentEvent e){/**forDocument interface**/}

	/**
	 *fill in the gaps in user specified flow limits 
	 */
	protected void modelFillIn(){
	    //need to resize and fillin these vectors
	    Hi=new double[maxYr-minYr+2];
	    Lo=new double[maxYr-minYr+2];
	    yr=new int[maxYr-minYr+2];
	    //fill with -999
	    for(int i=1;i<=maxYr-minYr+1;i++){
		Lo[i]=-999;
		Hi[i]=-999;
		yr[i]=-999;
	    }
	    //insert values in correct year space
	    for(int i=0;i<YY.size();i++){
		int yy=Integer.parseInt((String)YY.elementAt(i));
		yy=yy-minYr+1;//just in case yr1>1
		Lo[yy]=(new Double((String)LL.elementAt(i))).doubleValue();
		Hi[yy]=(new Double((String)HH.elementAt(i))).doubleValue();
		yr[yy]=Integer.parseInt((String)YY.elementAt(i));
	    }

	    int x1=minYr,x2=maxYr; //yrs defining the gap
	    boolean isgap=false; //indicates if first part of gap is defined
	    double y1=0,y2=0, //hold gap endpoint values for low
		y1H=0, y2H=0; //gap values for high
	    for(int t=minYr; t<=maxYr;t++){
		//interpolate the lower limit
		if(!isgap & Lo[t-minYr+1]!=-999)//beginning of first gap
		    {y1=Lo[t-minYr+1]; y1H=Hi[t-minYr+1]; x1=t;
		      isgap=true; continue;}
		if(isgap & Lo[t-minYr+1]!=-999)//end of gap
		    {y2=Lo[t-minYr+1]; x2=t; y2H=Hi[t-minYr+1];
		    //now fill in the gap with linear interpolation
		    for(int x=x1+1;x<x2;x++){
			Lo[x-minYr+1]=y1+(x-x1)*(y2-y1)/(x2-x1);
			Hi[x-minYr+1]=y1H+(x-x1)*(y2H-y1H)/(x2-x1);
		    }
		    //set up to look for next gap endpoint
		    y1=y2; y1H=y2H; x1=x2; 
		    }/*end if isgap*/
	
	    }/*end for t*/
	    for(int t=1; t<=maxYr-minYr+1;t++){
		Lo[t]=Math.floor(Lo[t]*100)/100;
		Hi[t]=Math.floor(Hi[t]*100)/100;         
		    }
	}
    }

    /**
     *inner class to do the time consuming apply
     * operation on a separate thread. 
     */  
    class RunThread extends Thread{
	MpsWrite mpsWrite; //handle writing the MPS file
	LpBuild threadParent;
	public RunThread(LpBuild threadParent){
	    this.threadParent=threadParent;
	}/*end constructor*/
	public void run(){
	    if(!readData())return;
	    if(!getValidScheds())return;
	    if(!makeBvec())return;
	    if(!makeVecs())return;
	    if(!makeAmat())return;
	    try{
		mpsWrite=new MpsWrite(threadParent);
	    }catch(Exception e){parent.console.println("LpBuild error:\n"+e);}
	    if(!mpsWrite.write())return;
	    //uncomment this to print the constraint names
	    //for(int i=0;i<bnames.size();i++)
	    // parent.console.println((String)bnames.elementAt(i));
	    translationTables();//make translation tables for Poly and regime ids
	    return;
	}/*end method*/  
    }/*end inner Thread class*/
  

}/*end class*/


