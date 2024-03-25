package figure;
import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;

/**
*handle printing out a figure
*/
public class FigurePrintable implements Printable{
    protected Figure fig;
    PrinterJob pj;
    public FigurePrintable(Figure fig){
	this.fig=fig;
    }/*end constructor*/
/**
*Start the printing process
*/    
 public void startPrintJob(){  
    boolean isBuffered=fig.repaintManager.isDoubleBufferingEnabled(); 
     pj = PrinterJob.getPrinterJob();
     pj.setPrintable(this);
     if(pj.printDialog()){
	 try{fig.repaintManager.setDoubleBufferingEnabled(false);
	     pj.print();}  //get higher resolution with no Buffering
	 catch(PrinterException e){System.out.println("Print failed: "+e);}
	 finally{fig.repaintManager.setDoubleBufferingEnabled(isBuffered);}
     }/*end if*/
 }/*end startPrintJob*/
 
 public int print(Graphics g, PageFormat pf, int pageIndex){
     if (pageIndex!=0)return NO_SUCH_PAGE;
     Graphics2D g2=(Graphics2D)g;
     Container fig2=fig.getContentPane();
     double scaleWidth=pf.getImageableWidth()/fig2.getSize().width; 
     double scaleHeight=pf.getImageableHeight()/fig2.getSize().height;
     double scale=Math.min(scaleWidth,scaleHeight);
    
     double xMargin=(pf.getImageableWidth() - scale*fig2.getSize().width)/2.;
     double yMargin=(pf.getImageableHeight() - scale*fig2.getSize().height)/2.;
     
    g2.translate(pf.getImageableX()+xMargin,pf.getImageableY()+yMargin);
    g2.scale(scale,scale);
    fig2.paint(g2);
    
     return PAGE_EXISTS;
 }/*end print*/
    
}/*end class*/
