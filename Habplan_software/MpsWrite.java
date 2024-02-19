import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import figure.*;

/**
 *A class to write Linear Program MPS Files
 *This writes MPS files with 1 decision variable for each polygon
 *Plus 1 accounting variable for each year of each flow constraint
 *The polygon decision variables are between 0 and 1.
 *Therefore no acreage constraint is needed per se.
 *
 *There can be an annual acreage accounting variable, so you can keep
 *track of acres cut by year.  Therefore, acreage is another flow that
 *can be controlled.
 */

public class MpsWrite{
    
    LpBuild lpBuild;
    PrintWriter out; //handles writing mps text file
    Vector bvec, cvec, bnames, cnames, rowType;
    LpSparse Amat;

    public MpsWrite(LpBuild lpBuild){
	this.lpBuild=lpBuild;
	this.Amat=lpBuild.Amat;
	this.bvec=lpBuild.bvec;
	this.bnames=lpBuild.bnames;
        this.rowType=lpBuild.rowType;
	this.cvec=lpBuild.cvec;
	this.cnames=lpBuild.cnames;
	this.cnames=lpBuild.cnames;
    }/*end constructor*/

    /**
     *Do the writing
     */
    protected boolean write(){
	Amat.stats(); //print stats about Amat
	String fname=lpBuild.textField.getText();
	try{
	out = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
	}catch(IOException e){
	    lpBuild.parent.console.println("MpsWrite:\n"+e);
	    return false;
	}

	//now use out.print or println to write the mps file
	out.println("NAME          "+lpBuild.nameField.getText());
	out.println("ROWS");
	out.println(" N  obj");
	for(int i=0;i<bnames.size();i++){
	out.println(" "+rowType.elementAt(i)+"  "+bnames.elementAt(i));
	}

        String cnam,rnam, sp="               ";
	out.println("COLUMNS");
	for(int c=1;c<=cvec.size();c++){

	    if(c%1000==0)lpBuild.progress.progressBar.setString("Write: "+c/1000+" of "+ Math.round(cvec.size()/1000));

	    cnam=((String)cnames.elementAt(c-1))+sp;
	    cnam=cnam.substring(0,8);
	    double value=0.0;
	    rnam="obj                 ";
	    rnam=rnam.substring(0,8);
	    value=((Double)(cvec.elementAt(c-1))).doubleValue();
	    out.println("    "+cnam+"  "+rnam+"  "+value);
	    for(int r=0;r<Amat.col[c].size;r++){     
		int r2=Amat.col[c].items[r].r;//row number of this item
		value=Amat.col[c].items[r].value;
		rnam=((String)bnames.elementAt(r2-1))+sp; 
		rnam=rnam.substring(0,8);
		out.println("    "+cnam+"  "+rnam+"  "+value);
	    }
	}

	out.println("RHS");
	for(int i=0;i<bnames.size();i++){
	    if(i%1000==0)lpBuild.progress.progressBar.setString("RHS: "+i/1000+" of "+ Math.round(bnames.size()/1000));
	    rnam=((String)bnames.elementAt(i))+sp;
	    rnam=rnam.substring(0,8);
	    out.println("    RHS       "+rnam+"  "+bvec.elementAt(i));
	}
         out.println("ENDATA");
	//mps file complete, so close the stream
	boolean state=out.checkError();
	out.close();
	return true;
    }

}/*end class*/
