/*
 * DemoTimeout.java
 *
 * Created on 17 August 2006, 09:27
 *
 * This utility class is used to facilitate a timed demo.
 */

package smallfry.util;

import java.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/**
 *
 *  USE:

Add this to the startup function in your main midlet class:

//#if DEMO
//#             DemoTimeout demoTimeout = new DemoTimeout(this);
//#endif          

then add this in the appropriate paint method:
    
//#if DEMO
//#              DemoTimeout.drawDemoString(graphics, width/2, 0);
//#endif 

 *
 **/
        
/**
 * This utility class is used to facilitate a timed demo.
 * @author Matt
 */
public class DemoTimeout implements CommandListener
{
    static final int DEMO_MAX_PLAYS = 15;
    static final int DEMO_PLAYS_START_WARNING = 5;
    static final int DEMO_PLAYS_DRAW_TEXT = 10;
    public static int demoPlaysRemaining = DEMO_MAX_PLAYS;
    static char [] demoString;
    MIDlet midlet;
    Displayable displayable;
    
    /**
     * Creates a new instance of DemoTimeout
     */
    public DemoTimeout(MIDlet midlet)
    {
        this.midlet = midlet;
        this.displayable = Display.getDisplay(midlet).getCurrent();
        
        //Update the number of demo plays left
        RecordStore recordStore = null;
        try
        {
            recordStore = RecordStore.openRecordStore(midlet.getAppProperty("MIDlet-Name")+"DEMOPLAYS", true);
            
            //Check if we found a record
            if(recordStore.getNumRecords() == 0)
            {
                //New record store, save the maximum number of plays
                ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                DataOutputStream dataOutStream = new DataOutputStream(byteOutStream);
                dataOutStream.writeInt(demoPlaysRemaining);
                byte [] byteArray = byteOutStream.toByteArray();
                dataOutStream.close();
                byteOutStream.close();
                System.out.println("NEW RECORD: "+recordStore.addRecord(byteArray, 0, byteArray.length));
            }
            else
            {
                //Read in the count
                ByteArrayInputStream byteInStream = 
                        new ByteArrayInputStream(recordStore.getRecord(1));
                DataInputStream dataInStream = new DataInputStream(byteInStream);
                demoPlaysRemaining = dataInStream.readInt();
                dataInStream.close();
                byteInStream.close();

                //Update the record
                ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                DataOutputStream dataOutStream = new DataOutputStream(byteOutStream);
                dataOutStream.writeInt(demoPlaysRemaining-1);
                byte [] byteArray = byteOutStream.toByteArray();
                dataOutStream.close();
                byteOutStream.close();
                recordStore.setRecord(1, byteArray, 0, byteArray.length);
            }
        }
        catch (Exception e)
        {
            Utils.handleError(e, "DemoTimeout", 0);
            demoPlaysRemaining = 0;
        }
        finally
        {
            try
            {
                recordStore.closeRecordStore();
            }
            catch (Exception ee)
            {}
        }
        
        // Set up a string to be rendered to screen
        if(demoPlaysRemaining < DEMO_PLAYS_DRAW_TEXT)
        {
            String str = "DEMO -"+DemoTimeout.demoPlaysRemaining+"-";
            demoString = str.toCharArray();
        }
        
        // Expiry alerts
        String message = null;
        if(demoPlaysRemaining < 0)
        {
            message = "Thank you for trying the demo of "+
                    midlet.getAppProperty("MIDlet-Name")+
                    ". Please visit www.smallfrymobile.com to buy the full version.";
        }
        else if(demoPlaysRemaining < DEMO_PLAYS_START_WARNING)
        {
            message = "This demo of "+
                    midlet.getAppProperty("MIDlet-Name")+
                    " will expire after "+
                    demoPlaysRemaining +
                    " restart(s). Please visit www.smallfrymobile.com to buy the full version.";
        }
        if(message != null)
        {
            Image image = null;
            try
            {
                image = Image.createImage("/smallfry.png");
            } catch (Exception e) {}
            Alert alert = new Alert("Demo Expiry", message, image, AlertType.INFO);
            alert.addCommand(new Command("Ok", Command.OK, 1));
            alert.setCommandListener(this);
            alert.setTimeout(Alert.FOREVER);
            Display.getDisplay(midlet).setCurrent(alert);
        }
    }
    
    /**
     * Indicates that a command event has occurred on Displayable d.
     * @param c a Command object identifying the command. This is either one of the applications have been added to Displayable with addCommand(Command) or is the implicit SELECT_COMMAND of List.
     * @param d the Displayable on which this event has occurred
     */
    public void commandAction(Command c, Displayable d)
    {
        if(demoPlaysRemaining <= 0)
        {
            // Just exit
            midlet.notifyDestroyed();
        }
        else
        {
            Display.getDisplay(midlet).setCurrent(displayable);
        }
    }
    
    /**
     * Draws a string on the graphics context indicating the number of demo starts tries left.
     * @param graphics The graphics context to draw to
     * @param x The x position to draw the string at.
     * @param y the y position to draw the string at.
     */
    public static void drawDemoString(Graphics graphics, int x, int y)
    {
        if(null != demoString)
        {
            Utils.drawString(graphics, demoString, 0, x, y, Graphics.TOP | Graphics.HCENTER);
        }
    }
}
