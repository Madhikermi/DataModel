/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datamodellogger.report;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abuda
 */
public class GuiLogger {
  String workingDir=null;
  String fname=null;
  String tskname=null;
    public boolean startGUILogger(String taskname) {

        try {
            workingDir = System.getProperty("user.dir");
            fname=taskname+"_log.zip";
            tskname=taskname;
            String path = workingDir + "\\" + taskname;
            String command = "psr.exe /start /output " + path + "_log.zip   /sc 1 /slides 1 /gui 0 /arcxml 1";

            Runtime.getRuntime().exec(command);
            System.out.println("Process Step Recorder Started > Log file: " + path + "_log.zip");
            return true;

        } catch (IOException ex) {
            System.out.println("Exception in GuiLogger.startGUILogger");
            Logger.getLogger(GuiLogger.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean stopGUILogger() {
        try {
            String command = "psr.exe /stop";
            Runtime.getRuntime().exec(command);
            System.out.println("Process Step Recorder Stopped");
            Thread.sleep(200);
            return true;
        } catch (IOException ex) {
            System.out.println("Exception in GuiLogger.stopGUILogger");
            Logger.getLogger(GuiLogger.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
          Logger.getLogger(GuiLogger.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Exception in GuiLogger.stopGUILogger");
           return false;
      }
    }
     public String getfilePath() {
         return workingDir;
     }
     public String getfileName() {
         return fname;
     }
     
     public String taskName() {
         return tskname;
     }

}
