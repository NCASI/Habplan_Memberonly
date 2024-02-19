import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

/**
 * This class creates and displays a window containing a TextArea, 
 * in which the contents of a text file are displayed.
 *  Modified from _Java Examples in a Nutshell_. (http://www.oreilly.com)
 **/
public class JFileViewer extends JFrame implements ActionListener {
  String directory;  // The default directory to display in the FileDialog
  JTextArea textarea; // The area to display the file contents into 
    JScrollPane textScroll;//contains textarea
    int fontSize=12;

  /** Convenience constructor: file viewer starts out blank */
  public JFileViewer() { this(null, null); }
  /** Convenience constructor: display file from current directory */
  public JFileViewer(String filename) { this(null, filename); }

  /**
   * The real constructor.  Create a FileViewer object to display the
   * specified file from the specified directory 
   **/
  public JFileViewer(String directory, String filename) {
    super();  // Create the (closeable) frame
    setLocation(15,15);
    // Create a TextArea to display the contents of the file in
    textarea = new JTextArea("", 20, 40);
    textarea.setFont(new Font("Times", Font.PLAIN, fontSize));
    textarea.setEditable(false);
    textarea.setLineWrap(true);
    textarea.setWrapStyleWord(true);
    textarea.setBackground(Color.white);
    textScroll=new JScrollPane(textarea);
    getContentPane().add(textScroll,BorderLayout.CENTER);
        
    // Create a bottom panel to hold a couple of buttons in
    JPanel p = new JPanel();
    p.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    getContentPane().add(p,BorderLayout.SOUTH);

    // Create the buttons and arrange to handle button clicks
    Font font = new Font("Times", Font.BOLD, 14);
    JButton openfile = new JButton("Open File");
    JButton close = new JButton("Close");
    openfile.addActionListener(this);
    openfile.setActionCommand("open");
    openfile.setFont(font);
    close.addActionListener(this);
    close.setActionCommand("close");
    close.setFont(font);
     JButton plus = new JButton("+");
    plus.addActionListener(this);
    plus.setActionCommand("+");
    plus.setFont(font);
    plus.setToolTipText("Increase Font Size");
    JButton minus = new JButton("-");
    minus.addActionListener(this);
    minus.setActionCommand("-");
    minus.setFont(font);
    minus.setToolTipText("Decrease Font Size");
    p.add(plus);
    p.add(minus);
    p.add(openfile);
    p.add(close);

    this.pack();

    // Figure out the directory, from filename or current dir, if necessary
    if (directory == null) {
      File f;
      if ((filename != null) && (f = new File(filename)).isAbsolute()) {
        directory = f.getParent();
        filename = f.getName();
      }
      else directory = System.getProperty("user.dir");
    }

    this.directory = directory;    // Remember the directory, for FileDialog
    setFile(directory, filename);  // Now load and display the file
  }
  
  /**
   * Load and display the specified file (if any) from the specified directory
   **/
  public void setFile(String directory, String filename) {
    if ((filename == null) || (filename.length() == 0)) return;
    File f;
    FileReader in = null;
    // Read and display the file contents.  Since we're reading text, we
    // use a FileReader instead of a FileInputStream.
    try {
      f = new File(directory, filename); // Create a file object
      in = new FileReader(f);            // Create a char stream to read  it
      int size = (int) f.length();       // Check file size
      char[] data = new char[size];      // Allocate an array big enough for it
      int chars_read = 0;                // How many chars read so far?
      while(chars_read < size)           // Loop until we've read it all
        chars_read += in.read(data, chars_read, size-chars_read);
      textarea.setText(new String(data));       // Display chars in TextArea
      this.setTitle("Habplan FileViewer: " + filename); // Set the window title
      textarea.setCaretPosition(1); //position at beginning of file
      
    }
    // Display messages if something goes wrong
    catch (IOException e) { 
      textarea.setText(e.getClass().getName() + ": " + e.getMessage());
      this.setTitle("Habplan FileViewer: " + filename + ": I/O Exception");
    }
    // Always be sure to close the input stream!
    finally { try { if (in != null) in.close(); } catch (IOException e) {} }
  }



  /**
   * Handle button clicks
   **/
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

     if (cmd.equals("+")){
      fontSize("+");
      }  /*end if*/  
    if (cmd.equals("-")){
      fontSize("-");
      }  /*end if*/
   
    if (cmd.equals("open")) {          // If user clicked "Open" button
      // Create a file dialog box to prompt for a new file to display
	 JFileChooser fc = new JFileChooser();
         try{
	     fc.setCurrentDirectory(new File(directory));
	     int returnVal = fc.showOpenDialog(this); 
	     String fname = (fc.getSelectedFile()).getName();
	     directory=(fc.getSelectedFile()).getParent();
	     setFile(directory,fname); // Load and display selection
	 }catch(Exception et){System.out.println("JFileViewer: "+et);}
    }
    else if (cmd.equals("close"))      // If user clicked "Close" button
      this.dispose();                  //    then close the window
  }
  
      /**
     *Change the fontSize of the textarea
     *@param + is increase, - is decrease
     **/
    public void fontSize(String direction){
        int inc=2;
	if(direction.equals("+")) fontSize+=inc;
	else if(direction.equals("-")){
	    fontSize=(fontSize>4)?fontSize-=inc:4;
	}else fontSize=12;

	String content=textarea.getText();
	int position=textarea.getCaretPosition();
	textarea.setFont(new Font("Times", Font.BOLD, fontSize));
	textarea.setText(content);
	textarea.setCaretPosition(position);
    }

    
  /**
   * The FileViewer can be used by other classes, or it can be
   * used standalone with this main() method.
   **/
  static public void main(String[] args) throws IOException {
    // Create a FileViewer object
    JFrame f = new JFileViewer((args.length == 1)?args[0]:null);
    // Arrange to exit when the FileViewer window closes
    f.addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) { System.exit(0); }
    });
    // And pop the window up
    f.show();
  }
}
