import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

/**
 *management unit form for entering information related to
 *management units, i.e. cutting units or cutting blocks
 *@author Paul Van Deusen (NCASI)  12/04
 **/
public class UnitForm extends JFrame{

    FrameMenu parent;
    JFileButton unitButton;
    JTextField unitField; 
    boolean isUnitDataRead=false; //true after successful read 
    boolean isPrepUnits;//indicates if prepUnits was run yet to shut off warns
    Hashtable unitTable=null;//key=unit name, value=String of polygon names
    Vector unitArray=null;//item i is name of the unit i

    public UnitForm(FrameMenu parent){
	super("Management Unit File");
	this.parent=parent;
	setLocationRelativeTo(parent.editMenu);
	//set up management unit panel
	unitField=new JTextField("",17);
	unitField.addCaretListener(new ATextListener());
	unitField.setToolTipText("Name of Management Unit File");
	unitButton = new JFileButton("Find MU File",this,unitField,null,null);  
	unitField.setToolTipText("Name of Management Unit File");
	JPanel topPanel=new JPanel(new BorderLayout());
	topPanel.add(unitButton,BorderLayout.WEST);
	topPanel.add(unitField,BorderLayout.EAST);
	getContentPane().add(topPanel,BorderLayout.NORTH);
	addWindowListener(new WindowAdapter(){
		public void windowClosing (WindowEvent event){
		    setVisible(false);
		} });   
	pack();
    }/*end constructor*/

    public class ATextListener implements CaretListener{  
	public void caretUpdate(CaretEvent event){
	    JTextField field=(JTextField)event.getSource();
	    try{
		if(field==unitField){
		    UserInput.unitFile=unitField.getText().trim();
		    isUnitDataRead=false;
		}
	    }/*end try*/
	    catch(Exception e){ } 
	}/*end textChangedValue*/
    } /*end inner ATextListener class*/

    /**
     *Read the management unit file.Fills in unitTable to have key=
     *unit name and value=polygon member names.  Also fills in unitArray that
     *has unit i name in position i.  These are used in ObjForm.prepUnits()
     */
    boolean readUnitData(){
	if(isUnitDataRead || !parent.unitBox.isSelected())return(true);
	String fName=unitField.getText().trim();
	if(fName.equals("")){//no file name
	    setVisible(true);
	    parent.showMessageDialog(parent,"Enter A Management Unit File Name","Units File Input Error",JOptionPane.INFORMATION_MESSAGE,null);
	    return(false);
	}
	String line=null;
	String[] token;
	String[] members,tempMembers;
	int maxMembers=-1;
	int unitID=0;
	unitTable=new Hashtable();//key is unit, value is String of polygons
	unitArray=new Vector();
	unitArray.add("0"); //want unit 1 name in position 1
	BufferedReader in=null;
	File file=new File(fName);
	if(!file.isFile()){//maybe its in the exampleDir
	    fName=UserInput.ExampleDir+fName;
	    file=new File(fName);
	}
	//if not in exampleDir, then give a message
	if(!file.isFile()){
	     setVisible(true);
	    parent.showMessageDialog(parent,"Can't Find The Units File","Units File Input Error",JOptionPane.INFORMATION_MESSAGE,null);
	    return(false);
	}
        file=null;

	try{
	    in = new BufferedReader(new FileReader(fName));
	    while ((line=in.readLine())!=null){
		line=line.trim(); //get rid of leading and trailing white space
		token=line.split("\\s+"); //split on white space
	 	//System.out.println("line="+line);
		if(line.equals(""))continue; //skip blank lines
		if(unitTable.containsKey(token[1])){
		    tempMembers=(String[])unitTable.get(token[1]);
		    int tm=tempMembers.length;
		    maxMembers=(tm>maxMembers)?tm:maxMembers;
		    members=new String[tm+1];
		    for(int i=0;i<tm;i++)
			members[i]=tempMembers[i];
		    members[tm]=token[0]; //add a new member to this unit
		}else{
		    unitID++;
		    members=new String[1];
		    members[0]=token[0]; //first member of this unit
		    unitArray.add(token[1]);
		}
		unitTable.put(token[1],members);
	    }

	    // for(int i=1;i<unitArray.size();i++)System.out.println("Unit i, name "+i+" "+unitArray.get(i));
	    parent.console.println("Read Management Unit File: "+fName);
	    parent.console.println("N of Management Units: "+unitTable.size());
	    parent.console.println("Maximum Polygons per unit: "+maxMembers);
	    isUnitDataRead=true; //indicates success;
	    //Prep units if some components are added to Obj function
	    for(int i=1; i<=parent.nComponents; i++){
		if(parent.isAdded[i]){
		    prepUnits(); 
		    break;
		}
	    }
	    return(true);
	}catch(Exception e){
	    isUnitDataRead=false; //indicates failure
	    parent.unitBox.setSelected(false);
	    parent.showMessageDialog(this,e.toString(),"Units File Input Error",JOptionPane.INFORMATION_MESSAGE,null);	
	    System.out.println("FrameMenu.readUnitFile() error: "+e);
	    return(false);}
        finally{try {
	    if (in != null) in.close();
        }catch (IOException ex ){System.out.println("Cant close unitFile:"+ ex);
	}
	}
    }/*readUnitData()*/

    
    /**
     *Prepare the management unit vector.  The result is static
     *int[][] ObjForm.unitIndex which has one row for each management unit,
     *with each row being an int[] containing the internal polygon id
     *of the unit members
     */
    protected  void prepUnits(){
	if(!parent.unitBox.isSelected()){
	    ObjForm.unitIndex=new int[ObjForm.nPolys+1][1];//dummy ObjForm.unitIndex matrix
	    for(int i=1;i<=ObjForm.nPolys;i++)ObjForm.unitIndex[i][0]=i; //1 poly per unit
	    return;
	}
	String[] member;
	String name;
	boolean[] isMember=new boolean[ObjForm.nPolys+1];//ensure every poly is in a unit
	int nMembers=0; //count the number of polys that are in a  unit;

	int[][] tempIndex=new int[unitArray.size()][];//holds ids of unit members

	for(int i=1;i<unitArray.size();i++){
  	    name=(String)unitArray.get(i);
	    member=(String[])unitTable.get(name);
	    //System.out.println("name, length="+name+"  "+member.length);
	    tempIndex[i]=new int[member.length];
	    for(int m=0;m<member.length;m++){
		String intID=(String)ObjForm.polyTable.get(member[m]);
		if(intID!=null){
		    int id=Integer.parseInt(intID);
		    int id2=id;
		    if(isMember[id]){//this polygon is already in a unit
			id2=0; // remove this poly from this unit
			nMembers--;
			// parent.console.println("**Warning: Can't allow multiple unit membership for polygon: "+member[m]+" in unit: "+name);
		    }
		    isMember[id]=true;//this poly is in a management unit
		    nMembers++; //how many are in a unit
		    tempIndex[i][m]=id2;
		   
		}
	    }
	}
	if(nMembers==ObjForm.nPolys){
	    ObjForm.unitIndex=tempIndex; //all polys are in a unit already
	}
	else{
	   
	    int nUnits=unitArray.size()+ObjForm.nPolys-nMembers;
	    ObjForm.unitIndex=new int[nUnits][];
	    
	    //first copy whats in tempIndex to the first rows of ObjForm.unitIndex
	     int j=unitArray.size();
	    for(int i=1;i<j;i++){
		ObjForm.unitIndex[i]=tempIndex[i];
	    }
	    //now add remaining polygons to 1 member units
	    for(int i=1;i<=ObjForm.nPolys;i++){
		if(!isMember[i]){
		    ObjForm.unitIndex[j]=new int[1];
		    ObjForm.unitIndex[j][0]=i;
		    j++;
		}
	    }
	}

	if(isPrepUnits)return;
	 isPrepUnits=true; //only show warnings once

	//print out names of polygons with no data that were in unit file
	int warningLimit=10,nWarnings=0;
	for(int i=1;i<ObjForm.unitIndex.length;i++){
	    //System.out.println("unit "+i);
	    if(nWarnings>=warningLimit){
		parent.console.println("****No data polygon warning limit exceeded. \nYou should check the management unit file.");
		break;
	    }

	    for(int j=0;j<ObjForm.unitIndex[i].length;j++){
		//System.out.println(" "+ObjForm.unitIndex[i][j]);
		if(ObjForm.unitIndex[i][j]==0){
		    nWarnings++;
		    name=(String)unitArray.get(i);
		    member=(String[])unitTable.get(name);
		    parent.console.println("**Warning: No data for polygon: "+member[j]+" in unit: "+name);
		}
	    }
	}
	
    }/*end prepUnits()*/

}/*end class*/
