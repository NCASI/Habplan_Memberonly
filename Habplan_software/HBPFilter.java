import java.io.File;
import javax.swing.filechooser.*;
/********************************/
/* HBP file Filter  class */
/*******************************/
public  class HBPFilter extends FileFilter {

    String ext,desc; //the extension and description for this filter
    public HBPFilter(String ext,String desc){
	this.ext=ext;
	this.desc=desc;
    }

    //Accept all directories and all .hbp files.
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	String extension = getExtension(f);
	if (extension != null) {
	    if (extension.equals(ext)) {
		return true;
	    } else {
		return false;
	    }
	}
	return false;
    }
    //The description of this filter
    public String getDescription() {
	return desc;
    }
    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}/*end hbp filter class*/
