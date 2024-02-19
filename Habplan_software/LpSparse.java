
import java.util.*;
/**
 *A class for storing sparse matrices used by LpBuild
 *Its based on the idea that for LP matrices over 99% of the cells are empty.
 *This creates a lot of columns that expand only enough to hold the
 *non-zero row entries. It turns out to be very efficient for
 *producing MPS output.
 *It is assumed here that empty cells contain 0.0
 */

public class LpSparse{
    LpSparse.Column[] col;
    int rows,cols;
    int initCapacity;

    public LpSparse(int rows, int cols,int initCapacity){
        this.rows=rows;
	this.cols=cols;
	this.initCapacity=initCapacity;
	this.col=new Column[cols+1];
	for(int i=0;i<=cols;i++)
	    col[i]=new Column(initCapacity);
    }
    /*
     *put a value at position r,c
     *this prints out a warning if the position already has a value
     */
    protected void put(int r, int c, double value){
        Row row;

	if(r>rows)System.out.println("Warning: you are adding too many rows to LpSparse matrix. r="+r);
	row=new Row(r,value);
        if(col[c].size>=col[c].length)col[c]=expandCol(c);
	 col[c].items[col[c].size]=row;
         col[c].size++;       
    }

    /*
     *get the value at position r,c
     *return 0.0 if nothing if found;
     */
    public double get(int r,int c){
	for(int i=0;i<col[c].size;i++){
	    if(col[c].items[i].r==r){
		return col[c].items[i].value;
	    }
	}
	return 0.0;
    }
    /**
     *Expand the capacity of a column to hold more nonzero elements
     *
     */
    private Column  expandCol(int c){
	Row[] temp=new Row[col[c].size];
        Column newcol;
        int size=col[c].size;
        System.arraycopy(col[c].items,0,temp,0,size);//hold items in temp
        newcol=new Column(size+initCapacity);
        System.arraycopy(temp,0,newcol.items,0,size);
	    newcol.size=size;
	    return newcol;
	  
    }

    /*
     *Print out some basic statistics on the LpSparse matrix
     *aveSize assumes nothing in row 0
     */
    public void stats(){
	System.out.println("Summary of this LpSparse matrix:");
	System.out.println("LpSparse rows, cols = "+rows+", "+cols);
        double aveSize=0.;
	for(int i=0;i<=cols;i++){
	    aveSize+=col[i].size;
	}
	double sparcity=aveSize/((rows-1)*(cols-1));
	aveSize/=cols-1;
	System.out.println("Sparsity ratio = "+sparcity);
	System.out.println("Ave non-zero items per column = "+aveSize);
	/*uncomment to print out some columns
        for(int c=1;c<=2;c++){
	    for(int r=0;r<col[c].size;r++)
		System.out.println("c,r,value "+c+","+col[c].items[r].r+","+col[c].items[r].value);
	}
	*/
    }
 
    protected class Column{
	Row[] items;
        int size=0;//the position for the next nonZero element;
        int length;
	public Column(int capacity){
	    items=new Row[capacity];
            length=capacity-1;
	    }
    }

    protected class Row{
	int r; //the row position
	double value;  
	public Row(int r, double value){
	    this.r=r;
	    this.value=value;
	}
    }

}
