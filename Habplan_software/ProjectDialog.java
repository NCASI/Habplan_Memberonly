
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.beans.*;

/**
*A dialog for Habplan users to read and write
*Habplan project files in text form
*
*@author Paul Van Deusen (NCASI)  12/05
*/
public class ProjectDialog extends JDialog {
   
  FrameMenu parent;
  static String[] polyName; //hold the imported schedule names
  private static String fileSep=System.getProperty("file.separator");
  JFileButton fileButton;
  JTextField p1,p2;
  JButton writeButton,loadButton, closeButton;
   XMLReader xr;
  
public static void main (String args[]) {
 
}/*end main*/


public ProjectDialog(FrameMenu parent, String s){
  super(parent, s);
  setLocation(150,300);
  this.parent=parent;
  getContentPane().setLayout(new GridLayout(3,1));
   
       //import section 
  String file=UserInput.HabplanDir+"project"+fileSep + "project.xml";     
  p1=new JTextField(file,20);    
  JLabel lab1=new JLabel("Project file");
  JPanel panelI=new JPanel();
  panelI.add(lab1);
  panelI.add(p1);
  fileButton= new JFileButton("Project file",parent,p1,"xml","Habplan Project Files");

  //button section
   Font font = new Font("Times", Font.BOLD, 14);
    writeButton = new JButton("Write");
    writeButton.addActionListener(new ButtonListener());
    writeButton.setToolTipText("Write out a project file");
    writeButton.setFont(font);
    loadButton = new JButton("Read");
    loadButton.addActionListener(new ButtonListener());
    loadButton.setToolTipText("Read a saved project file");
    loadButton.setFont(font);
    closeButton = new JButton("Close");
    closeButton.addActionListener(new ButtonListener());
    closeButton.setFont(font);
   
   JPanel buttonPanel=new JPanel();
    buttonPanel.add(writeButton);
    buttonPanel.add(loadButton);
    buttonPanel.add(closeButton);   
  
   getContentPane().add(fileButton);
   getContentPane().add(panelI);
   getContentPane().add(buttonPanel);
 
   Color color=Color.getColor("habplan.ccflow.background").brighter();
   getContentPane().setBackground(color);
   buttonPanel.setBackground(color);
   panelI.setBackground(color);
   fileButton.setBackground(color);
  pack();

      addWindowListener(new WindowAdapter(){
       public void windowClosing (WindowEvent event)
    {
     dispose();
      
    }
      });

}/*end constructor*/

/**
* Handle button clicks
**/
   
public class ButtonListener implements ActionListener{

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    
     if (cmd.equals("Close")) {
      setVisible(false);   
     return;  
    }   
    
    if (cmd.equals("Write")) {
	writeProjectFile(p1.getText().trim());
     return;  
    }   
     
    if (cmd.equals("Read")) {

	readProjectFile(p1.getText().trim());
    }  
    
   
  }/*end actionperformed()*/

} /*end inner ButtonListener class*/

    
    /**
     *Write current settings into the specified file
     **/
    public void writeProjectFile(String fName){
	PrintWriter out=null;
        

	try{
	    out = new PrintWriter(new BufferedWriter(new FileWriter(fName,false)));
	  out.println("<?xml version=\"1.0\" ?>");  
        out.println("<project>");

	out.println("");
	out.println("<general>"); 
	parent.writeSettings(out); 
	out.println("</general>");
	out.println("");

	out.println("<components>");
	 for(int i=1;i<=parent.nComponents;i++){
	    parent.form[i].writeSettings(out);
	}
	 out.println("");
	 out.println("</components>");
	 out.println("");
	out.println("<bestschedule>"); 
	parent.bestSchedule.writeSettings(out); 
	out.println("</bestschedule>");
	out.println("");
	out.println("<output>"); 
	parent.outputDialog.writeSettings(out); 
	out.println("</output>");
	out.println("");
	
        out.println("<gis>"); 
	 parent.gisViewer.writeSettings(out); 
	out.println("</gis>");
	out.println("");
	out.println("<schedule>");
	parent.writeScheduleSettings(out);
	out.println("</schedule>");
	out.println("");
	 out.println("</project>");
      
	}catch(Exception e){
         JOptionPane.showMessageDialog(this,
	     "Error:"+e,
	    "Error Writing Project XML File",
           JOptionPane.ERROR_MESSAGE);
	}finally{	
	 out.flush();
	 out.close();
	 parent.console.println("Saved file: " +fName);
	}

    }



	 /**
     *read a project file
     **/
    public void readProjectFile(String fName){
	try{ 
        XMLReader xr = XMLReaderFactory.createXMLReader();
	ProjectHandler handler = new ProjectHandler(parent);
	xr.setContentHandler(handler);
	xr.setErrorHandler(handler);
        FileReader reader = new FileReader(fName);
        InputSource inputSource = new InputSource(reader);
	xr.parse(inputSource);
	}catch(Exception e){
         JOptionPane.showMessageDialog(this,e,
	    "Error Reading Project Text File",
           JOptionPane.ERROR_MESSAGE);
	}finally{
	  
	}
 
    }

    class ProjectHandler  extends DefaultHandler {
	FrameMenu parent;
	 boolean isImportError=true; //true if import of schedule failed
	public ProjectHandler(FrameMenu parent){
	    super();
	    this.parent=parent;
	}

	//these indicate the section being read;
	boolean isGeneral, isGis, isComponent, isFlow, isCCFlow, isBlock, isBiol2, isSpatial, isBiol, isSchedule,isBestSchedule,isOutput;
	FlowForm f=null;
	CCFlowForm cf=null;
	BlockForm bf=null;
	Biol2Form biol2=null;
	BiolForm biol=null;
	SpaceForm sf=null;

	public void startElement (String uri, String name,
				  String qName, Attributes atts)
	{
	    String title="";
	    

	    if(qName.equals("general"))isGeneral=true;
	    if(qName.equals("components"))isComponent=true;
	    if(qName.equals("bestschedule"))isBestSchedule=true;
	    if(qName.equals("schedule"))isSchedule=true;
	    if(qName.equals("output"))isOutput=true;
	    if(qName.equals("gis"))isGis=true;
	    if(qName.equals("flow") || qName.equals("ccflow") || qName.equals("block") || qName.equals("biol2") || qName.equals("biol") || qName.equals("spatial") ){
		String[] titleF=atts.getValue(0).split(" ");
		title=titleF[0].trim();
	
		for(int i=1;i<=parent.nComponents;i++){

                    titleF=parent.form[i].getTitle().split(" ");
		   
                    if(titleF[0].trim().equals(title))title="found";
		    else continue;

		    if(parent.form[i] instanceof FlowForm){
			f=(FlowForm)parent.form[i];
			isFlow=true; break;
		    }
		    if(parent.form[i] instanceof CCFlowForm){
			cf=(CCFlowForm)parent.form[i];
         		isCCFlow=true; break;
		    }
		    if(parent.form[i] instanceof BlockForm){
			bf=(BlockForm)parent.form[i];
			isBlock=true; break;
		    }
		    if(parent.form[i] instanceof Biol2Form){
			biol2=(Biol2Form)parent.form[i];
		        isBiol2=true; break;
		    }
		    if(parent.form[i] instanceof BiolForm){
			biol=(BiolForm)parent.form[i];
			isBiol=true; break;
		    }
		    if(parent.form[i] instanceof SpaceForm){
			sf=(SpaceForm)parent.form[i];
		        isSpatial=true; break;
		    }


		}
		if(!title.equals("found")){
		    parent.console.println("Project File Error: No Component with title="+title);
		   
		}
	    } //end block to handle start of a new obj component

	    if(isGeneral)parent.readSettings(qName,atts);
	     if(isSchedule)parent.readSettings(qName,atts);
       if(isBestSchedule)parent.bestSchedule.readSettings(qName,atts);
       if(isGis)parent.gisViewer.readSettings(qName,atts);	
	    if(isComponent){//reading obj component material
		if(isFlow && f!=null) f.readSettings(qName,atts);
		if(isCCFlow && cf!=null) cf.readSettings(qName,atts);
		if(isBlock && bf!=null) bf.readSettings(qName,atts);
		if(isBiol2 && biol2!=null) biol2.readSettings(qName,atts);
		if(isBiol && biol!=null) biol.readSettings(qName,atts);
		if(isSpatial && sf!=null) sf.readSettings(qName,atts);
	    }
	  if(isOutput)parent.outputDialog.readSettings(qName,atts);
	} /*end of startElement*/

	 public void endElement (String uri, String name, String qName)
    {
	if(qName.equals("general"))isGeneral=false;
	if(qName.equals("components"))isComponent=false;
	if(qName.equals("bestschedule"))isBestSchedule=false;
	if(qName.equals("flow")){isFlow=false; f=null;};
	if(qName.equals("ccflow")){isCCFlow=false; cf=null;}
	if(qName.equals("block")){isBlock=false; bf=null;}
	if(qName.equals("biol2")){isBiol2=false; biol2=null;}
	if(qName.equals("biol")){isBiol=false; biol=null;}
	if(qName.equals("spatial")){isSpatial=false; sf=null;}
	if(qName.equals("schedule"))isSchedule=false;
	if(qName.equals("output"))isOutput=false;
	if(qName.equals("gis"))isGis=false;
    }

	 public void characters (char ch[], int start, int length)
    {
	String data = (new String(ch,start,length)).trim();

	if(isGeneral && !data.equals("")){
	    parent.descriptiveData = data;
	    parent.console.println(data); //show in console
	}
    }/*characters()*/

	public void startDocument ()
	{
         parent.reInitialize(); //reset everything and read data again
	}

	public void endDocument ()
	{
	    if(parent.isSaveImport){
		ImportThread importThread=new ImportThread();
		importThread.start();
		JProgressMeter progress=new JProgressMeter(parent,importThread,getTitle());
		progress.work();
	    }
	}


	

    }/*end inner ProjectReader class*/

   

    protected void  readProjectFile(){

	for(int i=1;i<=parent.nComponents;i++){
	    if(!parent.compBox[i].isSelected())continue;
	    if(parent.form[i] instanceof FlowForm){
		FlowForm f=(FlowForm)parent.form[i];
		f.graph.setVisible(true);
	    }
	    else if(parent.form[i] instanceof CCFlowForm){
		CCFlowForm f=(CCFlowForm)parent.form[i];
		f.graph.setVisible(true);
	    }
	    else if(parent.form[i] instanceof BlockForm){
		BlockForm f=(BlockForm)parent.form[i];
		f.graph.setVisible(true);
	    }	    
	}
	return; 	
    }

     /**
     *import a schedule from the uml project file
     *called by DefaultHandler extension at end of document
     **/
    protected void importSchedule(){
		boolean isStart=parent.start.isEnabled();
	parent.stopTheRun();
	parent.start.setEnabled(false);
	parent.console.println("Initializing with imported schedule.");
	parent.setIsImport(true);//load and stop before iteration 2
	 for (int i=1; i<=parent.nComponents; i++){
	    parent.compBox[i].setSelected(ObjForm.isInComponent[i-1]); 
	     parent.manualItemStateChanged();
	 }/*end for i*/
         parent.unitBox.setEnabled(true);
	 //import the old schedule
	 for (int i=1; i<=ObjForm.nPolys; i++){
	     String[] names= (String[])(ObjForm.oldSched.get(i-1));
	     try{
		 int regId=Integer.parseInt((String)ObjForm.regimeTable.get(names[1].trim()));
	 int polyId= Integer.parseInt((String)ObjForm.polyTable.get(names[0].trim()));
	      ObjForm.polySched[polyId]=regId;
	      //parent.console.println("poly reg "+polyId+" "+regId);
	     }catch(Exception e){
	        parent.console.println("cant find: poly, reg "+names[0]+" "+names[1]);
	     }
	 }

	 ObjForm.oldSched=null;
	 parent.autoTitles();
	 parent.startTheRun();
	 parent.start.setEnabled(isStart);


    }

  
 class ImportThread extends Thread{

	public void run(){
	  importSchedule();	    
	}
    }


} /*end ImportDialog class*/


 
