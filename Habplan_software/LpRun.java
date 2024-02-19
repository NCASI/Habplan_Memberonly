import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

/**
 *Run lp_solve after setting up an mps file
 *
 *
 *@author Paul Van Deusen  NCASI 2/2004
 */


public class LpRun extends JFrame {
   
   FrameMenu parent;
    LpBuild lpBuild;
    LpRun thisLpRun;
   RunThread thread;
    PrintWriter outLP; //holds LP output
    Process p=null;
    Vector lpOut; //holds the LP output
    float lpObj; //holds LP OBJ value;
    int[] lpSched;
    Color bgColor;
    JButton closeButton, lpButton, getButton;
    JCheckBox checkBox;
    String fileIn; //location of the mpsfile to run
    String fileOut; //where to put the LP output;
    JTextField textFieldIn, textFieldOut;
    JTextArea textArea;
    static JLabel outLabel, inLabel; //output, input field labels
    JProgressMeter progress; //used when writing out an MPS file

    public LpRun(LpBuild lpBuild){
   	super("RUN LP SOLVER");
	this.lpBuild=lpBuild;
        this.parent=lpBuild.parent;
        thisLpRun=this; //need a pointer for inside button listeners
	setLocationRelativeTo(lpBuild);
	bgColor=(Color.getColor("habplan.lpbuild.background")).brighter();
       	Font font = new Font("SansSerif", Font.BOLD, 10);
	//button section
	closeButton = new JButton("Close");
	closeButton.addActionListener(new ButtonListener());
        closeButton.setFont(font);
	closeButton.setToolTipText("Kill LP run and close window");
	lpButton=new JButton("LP");
	lpButton.addActionListener(new ButtonListener());
	lpButton.setFont(font);
	lpButton.setToolTipText("Run the lp-solve program");
	getButton=new JButton("Get");
	getButton.addActionListener(new ButtonListener());
	getButton.setFont(font);
	getButton.setToolTipText("Get the LP solution into Habplan");
        getButton.setEnabled(false);
	checkBox=new JCheckBox("View Output");
        checkBox.setToolTipText("Check this to view output from lp solver");
	checkBox.setSelected(true);
	checkBox.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e){ 
		    if( e.getStateChange()==ItemEvent.SELECTED){
			textArea.setText(" ");
		    }
		}/*end actionPerformed()*/ 
	    }); 

	JPanel buttonPanel=new JPanel();
	buttonPanel.setBackground(bgColor);
	buttonPanel.add(closeButton);      
        buttonPanel.add(lpButton);
	buttonPanel.add(getButton);
	buttonPanel.add(checkBox);
	getContentPane().add(buttonPanel,BorderLayout.SOUTH);

	//file input output stuff
        fileIn=lpBuild.textField.getText();
	String prefix=lpBuild.textField.getText();//will be the outfile prefix
	int dotLoc=prefix.lastIndexOf('.');//locate period in the filename
        if(dotLoc!=-1) prefix=prefix.substring(0,dotLoc);
        fileOut=prefix.concat(".out");
        textFieldIn=new JTextField(fileIn,20);
	textFieldOut=new JTextField(fileOut,20);
        inLabel=new JLabel("Input File Name",JLabel.CENTER);
	inLabel.setToolTipText("The mps file name");
	outLabel=new JLabel("Output File Name",JLabel.CENTER);
	outLabel.setToolTipText("LP output goes here");
        //edit this to add outLabel and textFieldOut
	JPanel filePanel=new JPanel(new GridLayout(4,1));
	filePanel.setBackground(bgColor);
        filePanel.add(inLabel);
	filePanel.add(textFieldIn);
	filePanel.add(outLabel);
	filePanel.add(textFieldOut);
	getContentPane().add(filePanel,BorderLayout.NORTH);
	//LP output viewer panel
        textArea=new JTextArea(10,20);
        JScrollPane sp=new JScrollPane(textArea);
	getContentPane().add(sp,BorderLayout.CENTER);
	pack();
        //setDefaultCloseOperation(HIDE_ON_CLOSE);
	 addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
              p.destroy();
	      dispose();
	  }
	 });   
    }

    /**
     *Run the lp solver and save the results in lpObj and
     *the lpOut vector
     **/
    protected void lpSolve(){
	String s,s2,LpCmd=" ";
	String osName=System.getProperty("os.name","Unknown");
	lpOut=new Vector(100);
	//Don't care if its Windows XP or Windows XXX
	if(osName.indexOf("Windows")!=-1)osName="Windows";
	if(osName.equals("Linux"))
          LpCmd=UserInput.LPDir+"lp_solve -S2 -max -mps <"+fileIn;
	else if(osName.equals("Windows"))
	    LpCmd=UserInput.LPDir+"lp_solve.exe -S2 -max -mps <"+"\""+fileIn+"\"";
        else parent.console.println("lp_solve works with Linux or Windows!");
	//The command is split into an array at whitespaces.
	System.out.println("cmd = "+LpCmd);
	//System.out.println("os.name =  "+osName);
       	try{
          outLP = new PrintWriter(new BufferedWriter(new FileWriter(fileOut)));
	}catch(IOException e){
	    parent.console.println("LpRun.lpSolve():\n"+e);
	}

	try{
	    p = Runtime.getRuntime().exec(LpCmd);
	    BufferedReader stdInput = new BufferedReader(new 
							 InputStreamReader(p.getInputStream()));
	    if(checkBox.isSelected()){
	        textArea.setText("Output From LP SOLVER:\n");	
	    }
	    while ((s = stdInput.readLine()) != null) {

                if(s.lastIndexOf("objective")!=-1){//get obj value
		    int colon=s.lastIndexOf(':');
                    s2=s.substring(colon+1);
		    try{
			lpObj=Float.valueOf(s2).floatValue();
		    }catch(Exception ef){ lpObj=-1;
		    parent.console.println("Couldn't find obj value");
                    }
		}
		//write if does not end in 0 or ( has a . or no $)
                if(s.lastIndexOf("0")!=(s.length()-1)|
		   (s.lastIndexOf(".")!=-1|s.lastIndexOf("$")==-1)){
		if(checkBox.isSelected()){
		    textArea.append(s + "\n"); //write to users textArea
		    outLP.println(s); //also save in fileOut;
		}
		//get non-zero regime assignments
		if(s.lastIndexOf("$")!=-1)  lpOut.addElement(s);
		}
	    }
	}catch(java.io.IOException er){
	    parent.console.println("Runtime error: "+er);
	}finally{p.destroy(); outLP.close();}

	textArea.append("Obj Value="+lpObj+" Regime Assignments="+lpOut.size()+"\n");	

    }

    /**
     *kill the Runtime process, i.e. the LP job
     *Called from the progress meter
     */
    protected void destroy(){p.destroy();}

    /**
     *Pass the lp results to Habplan for display
     *
     **/
    protected void lp2Habplan(){
        lpSched=new int[ObjForm.nPolys+1];
        for(int i=0;i<lpSched.length;i++)lpSched[i]=-1; //init lpSched;
	String s;
	String[] poly,regime,value;//polyId, RegimeId, proportional assigment
        int maxSplits=100;
        poly=new String[maxSplits];
	regime=new String[maxSplits];
	value=new String[maxSplits];
        int j=0;
        int i=0;
	while( i<lpOut.size()){
	   s=(String)lpOut.elementAt(i);
	   int dollar=s.lastIndexOf('$');
           poly[j]=s.substring(0,dollar);//internal polygon id
	   regime[j]=s.substring(dollar+1,dollar+5).trim();//internal reg id
           value[j]=s.substring(dollar+5).trim();//proportion of stand assigned
	   // parent.console.println("j poly regime value "+j+" "+poly[j]+" "+regime[j]+" "+value[j]);
           if(j==0 & Double.valueOf(value[0]).doubleValue()==1.){//entire poly is assigned
	       lpSched[Integer.parseInt(poly[0])]=
                             Integer.parseInt(regime[0]);
	       j=-1;
	   }else if(j>0 && !poly[j].equals(poly[j-1])){
	       double maxReg=-1.;
	       int kMax=0; //index of regime with max proportion
               //now assign the regime with biggest value;
	       for(int k=0;k<j;k++){ 
		   double temp=  Double.valueOf(value[k]).doubleValue();
                   if(temp>maxReg){
		       kMax=k;//index of max regime
		       maxReg=temp;
		   }
	       }
	       lpSched[Integer.parseInt(poly[0])]=
                             Integer.parseInt(regime[kMax]);
	       j=-1;
	       i--; //need to reread last regime assignment
	   }
	   i++;
	   j++;
	}/*end while*/
	   
	//for(int i2=1;i2<ObjForm.nPolys+1;i2++) parent.console.println("i lpSched "+i2+" "+lpSched[i2]);
	parent.startTheRun();//this will initialize everything
	parent.stopTheRun();
	//wait for the algorithm thread to finish
        try{ parent.met.join(5000);  	}catch(InterruptedException ei){}
	//now substitute the LP schedule
	for(int i2=1;i2<ObjForm.nPolys+1;i2++){
            if(lpSched[i2]==-1)lpSched[i2]=getValidSched(i2);
	    ObjForm.polySched[i2]=lpSched[i2];
	} 
	parent.setIsImport(true);     
	parent.startTheRun();
               
	
    }

    /**
     *Access the scheds matrix created by getValidScheds() in
     *LpBuild to fill in a valid schedule for any polygon that the
     *LP solver might have skipped because it had a 0 obj coefficient.
     *This might happen for ponds, for example...
     */
    private int getValidSched(int i){
	if(lpBuild.scheds==null){
	    System.out.println("Error: Polygon with no valid schedule.");
	    return 1;
	}
	return lpBuild.scheds[i][1];
    }

    /**
     * Handle button clicks
     **/   
    public class ButtonListener implements ActionListener{
        
	public void actionPerformed(ActionEvent e) {
	    String cmd = e.getActionCommand();
    
	    if (cmd.equals("Close")) {
                destroy();
		setVisible(false); dispose();  
		return;  
	    }   
	    if (cmd.equals("LP")) {
		thread=new RunThread(); 
		thread.start();// run lpSolve()
		progress=new JProgressMeter(thisLpRun,thread,getTitle());
		progress.work();
		getButton.setEnabled(true);
		return;  
	    } 
	    if (cmd.equals("Get")) {
		lp2Habplan();  
		return;  
	    }   
	}/*end actionperformed()*/

    } /*end inner ButtonListener class*/

   
    /**
     *inner class to do the time consuming apply
     * operation on a separate thread. 
     */  
    class RunThread extends Thread{

	public RunThread(){
	   
	}/*end constructor*/
	public void run(){
	       lpSolve();
		return;
	}/*end method*/  
    }/*end inner Thread class*/
  

}/*end class*/


