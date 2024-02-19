
import sun.audio.*;
import java.awt.*;
import java.io.*;

/**
* sound class
*/
  class SoundPlayer {
       String fileName;
       File theFile = null;
       AudioData theData = null;
       InputStream nowPlaying = null;
       static boolean makeNoise=false;
       

       public SoundPlayer(String fileName) {
        this.fileName=fileName;
        open();   
       }

     
       public void open() {
                 try {
           theFile = new File(fileName);
           if (theFile != null) {
             FileInputStream fis = new FileInputStream(theFile);
             AudioStream as = new AudioStream(fis);
             theData = as.getData();
           }
           else System.out.println("Cant open file " + fileName);
         }
         catch (IOException e) {
           System.err.println(e);
         }
       }

       public static void setMakeNoise(boolean value){
          makeNoise=value;
       }

       public void play() {
         stop();  
	    if(!makeNoise)return;
            if (theData == null) open();  
            if (theData != null) {
           AudioDataStream ads = new AudioDataStream(theData);
           AudioPlayer.player.start(ads);
           nowPlaying = ads;
         }
       }

       public void stop() {
         if (nowPlaying != null) {
           AudioPlayer.player.stop(nowPlaying);
           nowPlaying = null;
         }
       }

       public void loop() {
         stop();
	 if(!makeNoise)return;
         if (theData != null) {
           ContinuousAudioDataStream cads = new ContinuousAudioDataStream(theData);
           AudioPlayer.player.start(cads);
           nowPlaying = cads;
         }
       }
  
     }/*end SoundPlayer class*/
