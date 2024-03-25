package figure;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class FontPicker extends JFrame {
	private FontPanel fontPanel = new FontPanel(this);
	private JLabel     label     = new JLabel(" ", JLabel.CENTER);
	Font font; //this is the current font, set at updateLabel
	JButton closeButton = new JButton("Close"),
  		applyFontButton = new JButton("Apply");
	ButtonListener listener;
	Figure parent;
		
	
	public FontPicker(Figure parent) {
	    super("Habplan FontPicker");
	    this.parent=parent;
		Container content=getContentPane();
		content.add(fontPanel, BorderLayout.NORTH);
		content.add(label, BorderLayout.CENTER);
		updateLabel(fontPanel.getSelectedFont());
		listener=new ButtonListener();
		closeButton.addActionListener(listener);
		applyFontButton.addActionListener(listener);
	    JPanel buttons=new JPanel();
		buttons.add(closeButton);
		buttons.add(applyFontButton);
		content.add(buttons, BorderLayout.SOUTH);
	}

	public void updateLabel(Font font) {
		label.setText(fullNameOfFont(font));
		label.setFont(font);
		this.font=font;
	}
	
	public Font getFont(){return font;}
	
	private String fullNameOfFont(Font font) {
		String family = font.getFamily();
		String style  = new String();

		switch(font.getStyle()) {
			case Font.PLAIN:  style = " Plain ";  break;
			case Font.BOLD:   style = " Bold ";   break;
			case Font.ITALIC: style = " Italic "; break;
			case Font.BOLD + Font.ITALIC:
				style = " Bold Italic "; 
				break;
		}
		return family + style + Integer.toString(font.getSize());
	}
    class ButtonListener implements ActionListener{
	public void actionPerformed(ActionEvent e){
	    if (e.getActionCommand().equals("Close"))setVisible(false);
	    if (e.getActionCommand().equals("Apply"))parent.setFont(getFont());
	}
    }/*end ButtonListener class*/
    
}
class FontPanel extends JPanel
		    implements ListSelectionListener {
	private FontPicker fontPicker;
	String[] family={"Dialog","SansSerif","Serif","Monospaced","Helvetica",
		"TimesRoman","Courier","DialogInput","ZapfDingbats"};
	String[] style={"Plain","Bold","Italic","BoldItalic"};	
	String size[] = {"6","8","10","12", "14", "16", "18", "24", "36","40","44"};	
	
	
	private JList familyList = new JList(family),
	             styleList  = new JList(style), 
	             sizeList   = new JList(size);
	 
	private JScrollPane spFamily = new JScrollPane(familyList),		     
			    spStyle = new JScrollPane(styleList),
			    spSize = new JScrollPane(sizeList);
	   

	public FontPanel(FontPicker fontPicker) {

		this.fontPicker = fontPicker;
				
		familyList.setVisibleRowCount(4);
		styleList.setVisibleRowCount(4);
		sizeList.setVisibleRowCount(4);
		familyList.setSelectedIndex(1);
		styleList.setSelectedIndex(1);
		sizeList.setSelectedIndex(4);

		add(spFamily); 
		add(spStyle); 
		add(spSize);

		familyList.addListSelectionListener(this);
		styleList.addListSelectionListener(this);
		sizeList.addListSelectionListener(this);
	}
	
	public void valueChanged(ListSelectionEvent event) {
			fontPicker.updateLabel(getSelectedFont());
		}
	
	public Font getSelectedFont() {
	    int style;
	    try{style=convertStyle((String)styleList.getSelectedValue());}
	    catch(Exception e){style=Font.PLAIN;}
		return new Font((String)familyList.getSelectedValue(),style,
			Integer.parseInt((String)sizeList.getSelectedValue()));					
	}
	
	
public int convertStyle(String style) {
    if (style.equals("Plain"))return Font.PLAIN;
    else if (style.equals("Bold"))return Font.BOLD;
    else if (style.equals("Italic"))return Font.ITALIC;
    else if (style.equals("BoldItalic"))return Font.BOLD+Font.ITALIC;
    else return Font.PLAIN;
	}/*convertStyle()*/
}
