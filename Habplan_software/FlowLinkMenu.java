import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *Creates the menu that allows a FlowForm to link to other
 *FlowForms.  FlowForms can be mutually linked or
 *indirectly linked via intermediate flow forms
 *@author Paul Van Deusen (NCASI) 1/05
 */

class FlowLinkMenu extends JMenu{

    FrameMenu parent;
    FlowForm master;
    int nFlow;
    FlowForm[] slave;
    JCheckBoxMenuItem[] box;

    public FlowLinkMenu(FrameMenu parent, FlowForm master){
	super("Link");
	this.master=master;
	this.parent=parent;
	setToolTipText("Link Other Flows to This Flow" );
	if(UserInput.nFlow>1)createMenu();
    }

    /**
     *make slave[j] point to jth flowForm and creates
     *a checkBox for each potential slave. No checkBox
     *for the master
     */
    protected void createMenu(){
	nFlow=UserInput.nFlow;
	
	slave=new FlowForm[nFlow];
	box=new JCheckBoxMenuItem[nFlow];
	//figure out how many other FlowForms there are
	int j=1;
	for(int i=1;i<=parent.nComponents;i++){
	    if(parent.form[i] instanceof FlowForm){
		if(i==master.idNum)continue;
		FlowForm f=(FlowForm)parent.form[i];
		slave[j]=f;
		box[j]=new JCheckBoxMenuItem(parent.componentName[i],false);
		box[j].setToolTipText("Link "+parent.componentName[i]);
		box[j].addItemListener(new CheckListener());
		box[j].setBackground(Color.lightGray.brighter());
		add(box[j]);
		j++;
		    }
	}/*end for i*/

    }/*method end*/

    /**
     *User clicks a checkbox
     */
    public class CheckListener implements ItemListener{
  
	public void itemStateChanged(ItemEvent e){
	    
	    JCheckBoxMenuItem thisBox=(JCheckBoxMenuItem) e.getSource();

	    for(int j=1;j<nFlow;j++){
		if(thisBox == box[j] && thisBox.isSelected()){
		    //System.out.println(master.getTitle()+" Enslaving "+slave[j].getTitle());
		    master.addLinkUpdateListeners(slave[j]);
		}
		if(thisBox == box[j] && !thisBox.isSelected()){
		    master.removeLinkUpdateListeners(slave[j]);
		    //System.out.println(master.getTitle()+" Freeing "+slave[j].getTitle());
  
		}

	    }
	}/*end itemStateChanged*/
    } /*end inner CheckListener class*/

}/*end class*/
