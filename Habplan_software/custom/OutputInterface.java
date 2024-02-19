import java.io.*;
public interface OutputInterface {
    
    boolean isWriteSchedule();
     void writeSchedule(PrintWriter fout, int nSaves, String polyName, String schedule);


}
