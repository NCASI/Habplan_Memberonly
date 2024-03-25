package figure;
import java.awt.*;

class GColors implements java.io.Serializable{
    Color[] lineColor={Color.yellow,Color.red,Color.green,Color.orange,Color.cyan,Color.magenta};
    Color outerBackground=Color.darkGray;
    Color innerBackground=Color.black;
    Color textColor=Color.white;
    Color axisColor=Color.white;
    Color gridColor=Color.white;
    Font textFont=new Font("times",Font.PLAIN,12);
    boolean yGridState=false;
    boolean xGridState=true;
    boolean lineLabelState=true;
    boolean bufferState=true;
    String aTitle;
}
