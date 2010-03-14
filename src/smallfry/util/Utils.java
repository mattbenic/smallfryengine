/*
 * Utils.java
 *
 * Created on 13 October 2004, 10:13
 */

//#mdebug info
//#define EASY_FP 1
//#enddebug


//#mdebug info
//#define DEBUGINFO
//#enddebug


package smallfry.util;

import java.io.*;
import java.util.*;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

//#if NOKIAUI
//# import com.nokia.mid.ui.*;
//#if MIDP10
//# import com.nokia.mid.sound.*;
//#endif
//#endif

//#if MIDP20
//# import javax.microedition.lcdui.game.*;
//# import javax.microedition.media.*;
//# import javax.microedition.media.control.*;
//#endif

//#ifdef BLUETOOTH
//--import javax.bluetooth.*;
//#endif

/**
 *
 * @author  Matt
 *
 * This class provides a number of platform-independant utility functions
 *
 * Revision History:
 *
 * 23 October 2004 - MB - Made high score load/save more general
 *                      - Added generic wrappers for Nokia-specific audio and lights
 *
 */
public class Utils
{
    // Error handling
    public static final String SF_ERR  = "SF_ERR";
    
    // Pointer to the one midlet
    public static MIDlet midlet;
    
    //Singleton instance Class for file accesses
    //MIDP spec does not dictate when classes are loaded-
    // using a class not in our jar might cause crashes on some platforms
    private static Class instanceClass = null;
    
    /*
     * Miscellaneous constants
     */
    public static final int MAX_HTTPCON_CHARS = 1000;
    
    /**
     * Render offset
     */
    public static int xROffset = 0;
    public static int yROffset = 0;
    
    /**
     * Clipping Offset
     * The Canvas offset.  It is used to offset smaller canvas on big screens.
     */
    public static int xCOffset = 0;
    public static int yCOffset = 0;
    
    //Rotations
    public final static int flipHorzFlag = 0x1;
    public final static int flipVertFlag = 0x2;
    public final static int rot90Flag    = 0x4;
    
    //FONT
    public static MultiImage[] fonts;
    
    //Languages
    public static String[] resources;
    
    public static boolean prioritySound;
//#if MIDP20
//#     public static Player[] soundEffects;
//#     public static Player nextSound;
//#     public static Player currentSound;
//#elif NOKIAUI
//#     public static byte[][] soundEffects;
//#     //Sound
//#     public static final int numConcurrentTones = Sound.getConcurrentSoundCount(Sound.FORMAT_TONE);
//#     //    public static int numTonesPlaying;
//#     public static Sound[] sounds;
//#     public static boolean[] prioritySoundPlaying;
//#     public static byte[] nextSound;
//#     public static int currentSound;
//#endif
    
    
    public static int volume = 1;
    
    static
    {
    //#if DEBUGINFO
//#         System.out.println("Loading Utils class");
    //#endif
    }
    
    
    /**
     * approximate sqrt
     * input - a FP value
     * output - the FP sqrt of the value
     *
     * WARNING: This function has been optimized for an initialGuess
     *          that is pretty close to the final sqrt value
     *          Increase the number of iterations if u want
     *          a more accurate sqrt value
     */
//#if DEBUGINFO
//#     public static int debugMaxSqrtIter = 0;
//#endif
    public static int FPaSqrt(int sqrFP, int initialGuessFP)
    {
        int x = initialGuessFP;
//#if DEBUGINFO
//#define DB
//#         for(int i = 0; ; i++)
//#         {
//#             if(i > 100)
//#             {
//#                     System.out.println("------------SQRT FAILED on " + i + " iteration when trying to get sqrt of " + sqrFP + " ("+Utils.FPtoInt(sqrFP)+")");
//#                     return x;
//#             }
//#             
//#             int nx = (x + FPdivide(sqrFP,x))/2;
//#             if(Math.abs(Math.abs(nx) - Math.abs(x)) < FP_0_0625)
//#             {
//#                 if(i > debugMaxSqrtIter)
//#                 {
//#                     System.out.println("------------SQRT Iter: " + i);
//#                     debugMaxSqrtIter = i;
//#                 }
//#                 return x;
//#             }
//#             x = nx;
//#         }
//#endif
        
//#ifdef DB
//#undefine DB
//#else
        //  x = 0.5*(x + sqr/x);
        //unroll loop
        x = (x + FPdivide(sqrFP,x))/2;  //1
        x = (x + FPdivide(sqrFP,x))/2;  //2
        x = (x + FPdivide(sqrFP,x))/2;  //3

        return x;
//#endif
    }
    
    /*
     *
     */
    public static void gc()
    {
        System.gc();
        Thread.yield();
    }
    
    /**
     * @param fonts Array of font images
     * @param iNumChars int
     */
    public static void initFonts(MultiImage fonts[])
    {
        Utils.fonts = fonts;
    }
    
    /**
     * @param fonts Array of font images
     * @param iNumChars int
     */
    public static void initResources(String textResourceFilename, int languageID) throws Exception
    {
        Utils.resources = getTextResources(textResourceFilename, languageID);
    }    
    
    /*
     * Device independant createImage function
     */
    public static final Image createImage(String imgName) throws Exception
    {
        try
        {
            return Image.createImage(imgName);
        }
        catch( Exception e )
        {
            gc();
            return Image.createImage(imgName);
        }
    }
    
/*
 * Device independant createImage function
 */
    public static final Image createImage(int xSize, int ySize, int clearColour)
    {
        try
        {
            //#if NOKIAUI && MIDP10
//#                      return DirectUtils.createImage(xSize, ySize, clearColour);
            //#else
            Image img = Image.createImage(xSize, ySize);
            img.getGraphics().setColor(clearColour);
            img.getGraphics().fillRect(0, 0, img.getWidth(), img.getHeight());
            return img;
            //#endif
        }
        catch( Exception e)
        {
//#if DEBUGINFO
//#             System.out.println("createImage failed once!!!!");
//#endif
            gc();
            
            //#if NOKIAUI && MIDP10
//#                      return DirectUtils.createImage(xSize, ySize, clearColour);
            //#else
            Image img = Image.createImage(xSize, ySize);
            img.getGraphics().setColor(clearColour);
            img.getGraphics().fillRect(0, 0, img.getWidth(), img.getHeight());
            return img;
            //#endif
        }
    }
    
//#if MIDP20
//#     /**
//#      * midp20 sprite draw
//#      */
//#     public static final void drawImage(Graphics g, Sprite img, int x, int y, int anchor, int manipulation)
//#     {
//#         //Convert from our rotation flags to midp20's
//#         int iMidp20Rot = 0;
//#         if(manipulation > 0)
//#         {
//#             if(manipulation == flipVertFlag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT180;
//#             }
//#             else if(manipulation == flipHorzFlag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR;
//#             }
//#             else if(manipulation == rot90Flag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT90;
//#             }
//#             else if(manipulation  == (flipVertFlag | flipHorzFlag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT180;
//#             }
//#             else if(manipulation  == (flipVertFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT270;
//#             }
//#             else if(manipulation  == (flipHorzFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT90;
//#             }
//#             else if(manipulation  == (flipHorzFlag | flipVertFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT270;
//#             }
//#             img.setTransform(iMidp20Rot);
//#         }
//#         else
//#         {
//#             img.setTransform(Sprite.TRANS_NONE);
//#         }
//# 
//#         if((anchor & Graphics.RIGHT) == Graphics.RIGHT)
//#         {
//#             x -= img.getWidth();
//#         }
//#         else if((anchor & Graphics.HCENTER) == Graphics.HCENTER)
//#         {
//#             x -= img.getWidth() / 2;
//#         }
//# 
//#         if((anchor & Graphics.BOTTOM) == Graphics.BOTTOM)
//#         {
//#             y -= img.getHeight();
//#         }
//#         else if((anchor & Graphics.VCENTER) == Graphics.VCENTER)
//#         {
//#             y -= img.getHeight() / 2;
//#         }
//# 
//#         img.setPosition(xROffset + x, yROffset + y);
//#         img.paint(g);
//# 
//#     }
//# 
//#endif
    
    /*
     *  MultiImage drawImage
     */
    public static final void drawImage(Graphics g, MultiImage img, int x, int y, int anchor, int manipulation)
    {
        img.paint(g, xROffset + x, yROffset + y, 0, anchor, manipulation);
    }
    
/*
 * Device independant drawImage function..
 */
    public static final void drawImage(Graphics g, Image img, int x, int y, int anchor, int manipulation)
    {
        //#if NOKIAUI && MIDP10
//#         //Convert from our rotation flags to nokia's
//#         if(manipulation > 0)
//#         {
//#             int iNokiaRot = 0;
//#             if((manipulation & flipVertFlag) == flipVertFlag)
//#             {
//#                 iNokiaRot |= DirectGraphics.FLIP_VERTICAL;
//#             }
//#             if((manipulation & flipHorzFlag) == flipHorzFlag)
//#             {
//#                 iNokiaRot |= DirectGraphics.FLIP_HORIZONTAL;
//#             }
//#             if((manipulation & rot90Flag) == rot90Flag)
//#             {
//#                 iNokiaRot |= DirectGraphics.ROTATE_270;
//#             }
//#             DirectUtils.getDirectGraphics(g).drawImage(img, xROffset + x, yROffset + y, anchor, iNokiaRot);
//#         }
//#         else
//#         {
//#             g.drawImage(img, xROffset + x, yROffset + y, anchor);
//#         }
//# 
        //#elif MIDP20 && !NOKIAUI
//#         //Convert from our rotation flags to midp20's
//#         int iMidp20Rot = 0;
//#         if(manipulation > 0)
//#         {
//#             if(manipulation == flipVertFlag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT180;
//#             }
//#             else if(manipulation == flipHorzFlag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR;
//#             }
//#             else if(manipulation == rot90Flag)
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT90;
//#             }
//#             else if(manipulation  == (flipVertFlag | flipHorzFlag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT180;
//#             }
//#             else if(manipulation  == (flipVertFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT270;
//#             }
//#             else if(manipulation  == (flipHorzFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_MIRROR_ROT90;
//#             }
//#             else if(manipulation  == (flipHorzFlag | flipVertFlag | rot90Flag))
//#             {
//#                 iMidp20Rot = Sprite.TRANS_ROT270;
//#             }
//#             g.drawRegion(img, 0, 0, img.getWidth(), img.getHeight(), iMidp20Rot, xROffset + x, yROffset + y, anchor);
//#         }
//#         else
//#         {
//#             g.drawImage(img, xROffset + x, yROffset + y, anchor);
//#         }
//# 
        //#else
        g.drawImage(img, xROffset + x, yROffset + y, anchor);
        //#endif
    }
    
    public static final void setColour(Graphics g, int colour)
    {
        //#if NOKIAUI && MIDP10
//#                  DirectUtils.getDirectGraphics(g).setARGBColor(colour);
        //#else
        g.setColor(colour);
        //#endif
    }
    
    public static final void fillRect(Graphics g, int x, int y, int dx, int dy, int colour)
    {
        Utils.setColour(g, colour);
        g.fillRect(xROffset +  x,yROffset + y, dx, dy);
    }
    
    /**
     * set the rendering offset
     */
    public static final void setROffset(int xOffset, int yOffset, boolean useCOffset)
    {
        if(useCOffset)
        {
            xROffset = xOffset + xCOffset;
            yROffset = yOffset + yCOffset;
        }
        else
        {
            xROffset = xOffset;
            yROffset = yOffset;
        }
    }
    
    /**
     *
     * @param dg DirectGraphics
     * @param text char[]
     * @param x int
     * @param y int
     * @param anchor int
     */
    public static final void drawString(Graphics graphics, char[] text, int fontIdx,
            int x, int y, int anchor)
    {
        if(text == null)
        {
            return;
        }

        MultiImage letters = fonts[fontIdx];
        int width = letters.size.x;
        int len = text.length;
        char c;
        int iIndex;
        int vAnchor;
        
        //Extract vertical anchor
        if(Graphics.VCENTER == (anchor & Graphics.VCENTER))
        {
            vAnchor = Graphics.VCENTER;
        }
        else if(Graphics.BOTTOM == (anchor & Graphics.BOTTOM))
        {
            vAnchor = Graphics.BOTTOM;
        }
        else
        {
            vAnchor = Graphics.TOP;
        }
        
        //Set x according to horizontal anchor
        if(Graphics.HCENTER == (anchor & Graphics.HCENTER))
        {
            x -= (len * width)/2;
        }
        else if(Graphics.RIGHT == (anchor & Graphics.RIGHT))
        {
            x -= len * width;
        }

        for(int i = 0; i < len; i++)
        {
            Utils.setColour(graphics, 0xffff1010);
            
            c = text[i];
            
            switch(c)
            {
                case 'e':   //enter char
                    iIndex = 46;
                    break;
                case 'u':   //up arrow
                    iIndex = 47;
                    break;
                case 'd':   //down arrow
                    iIndex = 48;
                    break;
                case 's':   //small space
                    x += width / 2;
                    continue;
            }            
            
            graphics.drawChar(c, x, y, 0);
            x += graphics.getFont().charWidth(text[i]);
        }
        
        if(true) return;
        
        
        //#if !FINAL
        try
        {
            //#endif
            for(int i = 0; i < len; i++)
            {
                c = text[i];
                
                iIndex = 256;
                
                if(c >= 'A' && c <= 'Z')
                {
                    iIndex = c - 'A';
                }
                else if(c >= '0' && c <= '9')
                {
                    iIndex = c - '0' + 26;
                }
                else if(c != ' ')
                {
                    switch(c)
                    {
                        case ',':
                            iIndex = 36;
                            break;
                        case '.':
                            iIndex = 37;
                            break;
                        case '-':
                            iIndex = 38;
                            break;
                        case '\'':
                            iIndex = 39;
                            break;
                        case '!':
                            iIndex = 40;
                            break;
                        case '?':
                            iIndex = 41;
                            break;
                        case '+':
                            iIndex = 42;
                            break;
                        case '=':
                            iIndex = 43;
                            break;
                        case '*':
                            iIndex = 44;
                            break;
                        case '%':
                            iIndex = 45;
                            break;
                        case 'e':   //enter char
                            iIndex = 46;
                            break;
                        case 'u':   //up arrow
                            iIndex = 47;
                            break;
                        case 'd':   //down arrow
                            iIndex = 48;
                            break;
                        case 's':   //small space
                            x += width / 2;
                            continue;
                    }
                }
                else //Space
                {
                    x += width;
                    continue;
                }
                
                if(iIndex < letters.images.length)
                {
                    letters.paint(graphics, x, y, iIndex, vAnchor | Graphics.LEFT, 0);
                }
                x += width;
            }
            //#if !FINAL
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        //#endif
    }
    
    /*
     * Writes an array of String:int pairs to the RMS
     */
    public static void saveStringIntArray(String recordStoreName, String[] settingNames, int[] settingValues)
    {
        //Delete existing record store
        try
        {
            RecordStore.deleteRecordStore(recordStoreName);
        }
        catch(Exception e)
        {
            //#if !FINAL
            e.printStackTrace();
            //#endif
        }
        
        //Recreate and populate new recordstore
        RecordStore recordStore = null;
        try
        {
            //#if !FINAL
            System.out.println("Saving settings to recordstore \""+recordStoreName+"\"");
            if(settingNames == null && settingValues == null)
            {
                System.out.println("ERROR in saveStringIntArray: settingNames and settingValues is to null!!!");
            }
            else if(settingNames != null && settingValues != null)
            {
                if(settingNames.length != settingValues.length)
                {
                    System.out.println("ERROR in saveStringIntArray: settingNames.length != settingValues.length");
                }
            }
            //#endif
            recordStore = RecordStore.openRecordStore(recordStoreName, true);
            
            
            int arrLength = 0;
            if(settingValues != null)
            {
                arrLength = settingValues.length;
            }
            if(settingNames != null)
            {
                arrLength = settingNames.length;
            }
            
            for(int i = 0; i < arrLength; i++)
            {
                //#if !FINAL
                if(settingNames != null)
                {
                    System.out.println("\t"+settingNames[i] + ":");
                }
                if(settingValues != null)
                {
                    System.out.println("\t"+settingValues[i]);
                }
                System.out.println();
                //#endif
                
                //Build a byte array to write to the RMS
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(byteStream);
                if(settingNames != null)
                {
                    dataStream.writeUTF(settingNames[i]);
                }
                if(settingValues != null)
                {
                    dataStream.writeInt(settingValues[i]);
                }
                byte [] byteArray = byteStream.toByteArray();
                dataStream.close();
                byteStream.close();
                
                //Add the data to the RMS
                recordStore.addRecord(byteArray, 0, byteArray.length);
            }
            //#if !FINAL
            System.out.println("Done.");
            //#endif
        }
        catch(Exception e)
        {
            //#if !FINAL
            e.printStackTrace();
            //#endif
        }
        
        if(recordStore != null)
        {
            try
            {
                recordStore.closeRecordStore();
            }
            catch(Exception ee)
            {
            }
        }
        
    }
    
    /*
     * Loads an array of String:int pairs from the rms
     */
    public static void loadStringIntArray(String recordStoreName, String[] settingNames, int[] settingValues)
    {
        RecordStore recordStore = null;
        try
        {
            //#if DEBUGINFO
//#             System.out.println("Loading settings from recordstore \""+recordStoreName+"\"");
//#             
//#             if(settingNames == null && settingValues == null)
//#             {
//#                 System.out.println("ERROR in loadStringIntArray: settingNames and settingValues is to null!!!");
//#             }
//#             else if(settingNames != null && settingValues != null)
//#             {
//#                 if(settingNames.length != settingValues.length)
//#                 {
//#                     System.out.println("ERROR in loadStringIntArray: settingNames.length != settingValues.length");
//#                 }
//#             }
            //#endif
            recordStore = RecordStore.openRecordStore(recordStoreName, false);
            
            int arrLength = 0;
            if(settingValues != null)
            {
                arrLength = settingValues.length;
            }
            if(settingNames != null)
            {
                arrLength = settingNames.length;
            }
            for(int i = 0; i < arrLength; i++)
            {
                //Build a byte array to write to the RMS
                ByteArrayInputStream byteStream = new ByteArrayInputStream(recordStore.getRecord(i+1));
                DataInputStream dataStream = new DataInputStream(byteStream);
                if(settingNames != null)
                {
                    settingNames[i] = dataStream.readUTF();
                }
                if(settingValues != null)
                {
                    settingValues[i] = dataStream.readInt();
                }
                dataStream.close();
                byteStream.close();
                //#if DEBUGINFO
//#                 if(settingNames != null)
//#                 {
//#                     System.out.println("\t"+settingNames[i] + ":");
//#                 }
//#                 if(settingValues != null)
//#                 {
//#                     System.out.println("\t"+settingValues[i]);
//#                 }
//#                 System.out.println();
                //#endif
            }
            
            if(recordStore != null)
            {
                try
                {
                    recordStore.closeRecordStore();
                }
                catch(Exception ee)
                {
                }
            }
            
            //#debug info
//#             System.out.println("Done.");
        }
        catch(Exception e)
        {
            if(recordStore != null)
            {
                try
                {
                    recordStore.closeRecordStore();
                }
                catch(Exception ee)
                {
                }
            }
            
            //#if DEBUGINFO
//#             e.printStackTrace();
            //#endif
            //In case of failure, create new record store, with the appropriate number of items
            saveStringIntArray(recordStoreName, settingNames, settingValues);
        }
    }
    
    public static void forceBackLight()
    {
        //#if NOKIAUI
//#                  DeviceControl.setLights(0, 100);
        //#endif
    }
    
    public static void initSound(int numSounds)
    {
    //#if MIDP20
//#         soundEffects = new Player[numSounds];
    //#elif NOKIAUI
//#         soundEffects = new byte[numSounds][];
//#         sounds = new Sound[numConcurrentTones];
//#         prioritySoundPlaying = new boolean[numConcurrentTones];
//#         for(int i = 0; i < numConcurrentTones; i++) {
//#             prioritySoundPlaying[i] = false;
//#             // Initialise a sound object we'll use later. We create the object here
//#             // with meaningless values becuase we'll init it later with proper
//#             // numbers. A sound listener is then set.   The first time the sound
//#             // is played, it seems to take for ever, so lets just play this sound now..
//#             // Although it shouldn't really make a noise because of the 0 freq.
//#             sounds[i] = new Sound(0, 1L);
//#         }
    //#endif
    }
    
    /**
     * Sets the global volume
     * @param volume:  0 = off, 255 max
     */
    public static void setVolume(int volume)
    {
        Utils.volume = volume;
        //#if MIDP20
//#             if(soundEffects != null)
//#                 {
//#             VolumeControl control;
//#             for(int i = 0; i < soundEffects.length; i++)
//#             {
//#                 try
//#                 {
//#                     soundEffects[i].realize();
//#                     control = (VolumeControl) soundEffects[i].getControl("VolumeControl");
//#                     control.setLevel(volume * 50);
//#                     control.setMute(volume == 0);
//#                 }
//#                 catch (Exception e)
//#                 {
//#                 }
//#             }
//#             }
        //#elif NOKIAUI
//#         for(int i = 0; i < numConcurrentTones; i++) {
//#             sounds[i].setGain(volume * 100);
//#         }
        //#endif
    }
    
    /**
     * Plays a sound sequence from a byte array
     * NOKIA implementation plays OTA sounds
     * MIDP2 implementation plays sound format specified by format
     */
    public static void playSoundFromByteArray(byte [] data, String format, boolean force)
    {
        if(data.length == 0) return;
        try
        {
        //#if MIDP20
//#             Player player = Manager.createPlayer(new ByteArrayInputStream(data), format/*"audio/midi"*/);
//#             player.realize();
//#             playSound(player, force);
        //#elif NOKIAUI
//#             playSound(data, force);
        //#endif
        }
        catch (Exception e)
        {
//#if DEBUGINFO
//#             e.printStackTrace();
//#endif
            }
    }
    
    /**
     * Plays a sound sequence from a byte array
     * NOKIA implementation plays OTA sounds
     * MIDP2 implementation plays MIDI sounds
     */
    public static void playSound(Object obj, boolean force)
    {
        //don't play a sound if the volume is 0 or there is nothing to play
        if(obj == null || volume == 0)
        {
            return;
        }
        
        try
        {
            if(!prioritySound)
            {
        //#if MIDP20
//#                 nextSound = (Player) obj;
        //#elif NOKIAUI
//#                 if(((byte [])obj).length == 0) return;
//#                 nextSound = (byte []) obj;
        //#endif
                prioritySound = force;
            }
        }
        catch(Exception e)
        {
//#if DEBUGINFO
//#             e.printStackTrace();
//#endif
        }
    }
    
    /**
     * stops all the sounds
     */
    public static void stopAllSounds()
    {
//#if MIDP20
//#         if(soundEffects != null)
//#         {
//#         //Pause the music
//#         for(int i = 0; i <= soundEffects.length; i++)
//#         {
//#             try
//#             {
//#                 if(null != soundEffects[i])
//#                 {
//#                     soundEffects[i].stop();
//#                 }
//#             }
//#             catch(Exception e)
//#             {
//#             }
//#         }
//#         }
//#endif
    }

    /**
     * Utils update
     */
    public static void update(int dtms)
    {        
    }
    
    /**
     * Utils update
     */
    public static void updateSound(int dtms, boolean noSoundPrefetch)
    {
        //do we have a new sound to play?
        //#if MIDP20
//#         if(nextSound != null)
//#         {
//#             //Can we play a sound
//#             {
//# 
//#                 if(currentSound == null || ((currentSound != null) && (currentSound.getState() != Player.STARTED))
//#                 || (prioritySound && (currentSound != nextSound)))
//#                 {
//#                     try
//#                     {
//#                         //Force the current player to stop
//#                         if(currentSound != null)
//#                         {
//#                             currentSound.stop();
//# 
//#                             //if sound prefetching has been disabled
//#                             if(noSoundPrefetch)
//#                             {
//#                                 //We may not have more than one sound
//#                                 //in the prefetch state at once!
//#                                 currentSound.deallocate();
//#                                 if(Player.REALIZED != currentSound.getState())
//#                                 {
//#                                     return;
//#                                 }
//#                             }
//#                         }
//# 
//#                         //Play the next sound
//#                         currentSound = nextSound;
//#                         //currentSound.prefetch();
//#                         VolumeControl control = (VolumeControl) currentSound.getControl("VolumeControl");
//#                         control.setLevel(volume * 50);
//#                         control.setMute(volume == 0);
//#                         currentSound.start();
//#                     }
//#                     catch (Exception e)
//#                     {
        //#debug error
//#                         e.printStackTrace();
//# 
//#                     }
//#                     nextSound = null;
//#                     prioritySound = false;
//#                 }
//#             }
//#         }
        //#elif NOKIAUI
//# 
//#             if(nextSound != null)
//#             {
//#                 //go to the next slot in the sound bank
//#                 currentSound++;
//#                 if(currentSound == numConcurrentTones)
//#                 {
//#                     currentSound = 0;
//#                 }
//# 
//#                 //is that sound playing
//#                 //!!! this just doesn't seem to work!!!  the sound is considered to still be playing long
//#                 //    after it has already stopped
//#                 //            boolean soundPlaying = Sound.SOUND_PLAYING == sounds[currentSound].getState();
//#                 boolean soundPlaying = false;
//# 
//#                 //if a sound is not playing then play this sound
//#                 // or if this sound we want to play is priority then play the sound
//#                 // unless a priority sound is already playing
//#                 if((!soundPlaying) || (prioritySound && !prioritySoundPlaying[currentSound]))
//#                 {
//#                     sounds[currentSound].init(nextSound, Sound.FORMAT_TONE);    //load the sound data
//#                     sounds[currentSound].play(1);                          //play it once
//#                     if(prioritySound)
//#                     {
//#                         prioritySoundPlaying[currentSound] = true;
//#                     }
//#                     else
//#                     {
//#                         prioritySoundPlaying[currentSound] = false;
//#                     }
//#                     nextSound = null;
//#                     prioritySound = false;
//#                 }
//#             }
        //#endif
    }
    
    
    /**
     *  filenum = 0: load all the files
     **/
        public static byte[][] loadFilesFromPak(DataInputStream inputStream, int filenum) throws Exception
        {
            //check header
            if(inputStream.readByte() != 'S' ||
                    inputStream.readByte() != 'F' ||
                    inputStream.readByte() != 0x12)
            {
//#if DEBUGINFO
//#             if(true)
//#                 throw new Exception("The pak file system has been upgraded to version 1.2.");
//#endif                
                return null;
            }
            
            byte numFiles = inputStream.readByte();
            
            if(inputStream.readByte() != 0)
            {
                return null;
            }

            byte[][] fileArray;
            
            //load all the files
            if(filenum == 0)
            {
                fileArray = new byte[numFiles][];
            }
            else
            {
                fileArray = new byte[1][];
            }
            
            int count = 0;
            int outputcount = 0;

            do
            {
                int size = inputStream.readInt();
                
                if(count == filenum || filenum == 0)
                {
                    byte[] bytes = readBytesFromFile(inputStream, size);
                    
                    fileArray[outputcount] = bytes;
                    
                    if(filenum != 0)
                    {
                        return fileArray;
                    }
                    
                    outputcount++;
                }
                else
                {
                    inputStream.skipBytes(size);
                }
                count++;
            }
            while(count < numFiles);
            
            return fileArray;
        }
    
    /**
     * loads a single file from within the pak file
     */
    public static byte[] loadFromPak(String filename, int filenum) throws Exception
    {
//#if DEBUGINFO
//#         System.out.println("Utils.loadFromPak("+filename+", "+filenum+")");
//#endif   
        
        DataInputStream inputStream = new DataInputStream(getResourceAsStream(filename));
        byte[] bytes = loadFromPak(inputStream, filenum);
        inputStream.close();
        inputStream = null;
        return bytes;
    }

    /**
     * loads a single file from within the pak file
     */
    public static byte[] loadFromPak(DataInputStream inputStream, int filenum) throws Exception
    {
        byte [][] fileArray = loadFilesFromPak(inputStream, filenum);
        if(fileArray != null)
        {
            return fileArray[0];
        }
        return null;
    }    
    
    /**
     * loads a single file from within the pak file
     */
    public static byte[][] loadFilesFromPak(String filename, int filenum) throws Exception
    {
//#if DEBUGINFO
//#         System.out.println("Utils.loadFromPak("+filename+", "+filenum+")");
//#endif   
        
        DataInputStream inputStream = new DataInputStream(getResourceAsStream(filename));
        
        return loadFilesFromPak(inputStream, filenum);
    }
    
    /**
     * loads a file from within the pak file
     */
    public static byte[] readBytesFromFile(DataInputStream inputStream, int size) throws Exception
    {
        byte[] bytes = new byte[size];
        int readbytes = 0;
        int offset = 0;
        while (true)
        {
            readbytes = inputStream.read(bytes, offset, size);
            offset += readbytes;
            size -= readbytes;
            if (readbytes == -1 || size <= 0)
            {
                // may need to handle error condition here!
                break;
            }
        }
        
        inputStream.read(bytes, 0, size);
        return bytes;
    }
    
    /**
     * retrieves a single text resource from the text pak file.
     **/
    public static String getTextResource(String filename, int languageID, int resID) throws Exception
    {
        DataInputStream inputStream = new DataInputStream(getResourceAsStream(filename));
        byte[] languageFile = Utils.loadFromPak(inputStream, languageID);
        inputStream = null;
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(languageFile);
        inputStream = new DataInputStream(byteStream);
        byteStream = null;  
        languageFile = null;
        
        byte[] resourceFile = Utils.loadFromPak(inputStream, resID);
        inputStream = null;

        return new String(resourceFile);
    }

   /**
     * retrieves a single text resource from the text pak file.
     **/
    public static String getTextResource(int resID)
    {
        return Utils.resources[resID];
    }
    
    /**
     * retrieves a single text resource from the text pak file.
     **/
    public static String[] getTextResources(String filename, int languageID) throws Exception
    {
        byte[][] allLaguages = loadFilesFromPak(filename, 0);
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(allLaguages[languageID]);
        DataInputStream inputStream = new DataInputStream(byteStream);
        byteStream = null;  
        allLaguages = null;
        
        byte[][] allResource = Utils.loadFilesFromPak(inputStream, 0);
        inputStream = null;

        String[] resources = new String[allResource.length];
        for(int i = 0; i < allResource.length; i++)
        {
            resources[i] = new String(allResource[i]);
        }
            
        return resources;
    }
    
    /**
     *  loads an image from the pakfile
     */
    public static Image loadImageFromPak(String filename, int filenum) throws Exception
    {
        byte[] imagebytes = Utils.loadFromPak(filename, filenum);

        return Image.createImage(imagebytes,0,imagebytes.length);
    }
    
    /**
     * load multi-image special format from byte[]
     */
    public static MultiImage createMultiImage(byte[] pak, int miOffset) throws Exception
    {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(pak);
        DataInputStream inputStream = new DataInputStream(byteStream);
        byteStream = null;
        
        MultiImage img = new MultiImage(miOffset);
        
            //check header
            if(inputStream.readByte() != 'S' ||
                    inputStream.readByte() != 'F' ||
                    inputStream.readByte() != 0x12)
            {
//#if DEBUGINFO
//#             if(true)
//#                 throw new Exception("The pak file system has been upgraded to version 1.2.");
//#endif                
                return null;
            }
            
            byte numFiles = inputStream.readByte();
            
            if(inputStream.readByte() != 0)
            {
                return null;
            }

        
        //load the multiimage config
        int size = inputStream.readInt();
        int dx = inputStream.readInt();
        int dy = inputStream.readInt();
        byte opaque = inputStream.readByte();
        //first load the pallete
        size = inputStream.readInt();
        byte[] pal = readBytesFromFile(inputStream, size);
        byte[] bytes;
        
        //load the opaque part of the image
        if(opaque == 1)
        {
            //load the opaque image
            size = inputStream.readInt();
            bytes = readBytesFromFile(inputStream, size);
            img.append(Utils.makePNG(pal, bytes), dx, dy);
        }
        
        try
        {
            while(true)
            {
                size = inputStream.readInt();
                bytes = readBytesFromFile(inputStream, size);
                img.append(Utils.makePNG(pal, bytes));
            }
        }
        catch(EOFException e)
        {
        }
        
        inputStream.close();
        inputStream = null;
        
        return img;
    }
    
    /**
     * create a png from the special format
     */
    public static Image makePNG(byte[] pal, byte[] bytes) throws Exception
    {
        final int png_header_size = 25;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // write PNG header
        bos.write(new byte[]
        {(byte)137,80,78,71,13,10,26,10});
        //write IHDR tag
        bos.write(bytes, 0, png_header_size);
        //write pallete
        bos.write(pal);
        //write rest of image data
        bos.write(bytes, png_header_size, bytes.length-png_header_size);
        bos.close();
        //create the image
        return Image.createImage(bos.toByteArray(), 0, bos.size());
    }
    
    /*
     * flips or rotates an original image
     * the source image must be completely opaque
     * this function should only be used for MIDP1 and SIEMENS phones as
     * MIDP2 and NOKIA phones have a draw Rotated function
     *
     * This function is really slow and doesn't work properly on the midp2
     * emulator for some reason!!!
     *
     * image - the original opaque image
     * manipulation - the manipulation flags
     */
    public static final Image createManipulatedImage(Image image, int manipulation)
    {
        int xSize, ySize;
        if((manipulation & rot90Flag) == rot90Flag)
        {
            ySize = image.getWidth();
            xSize = image.getHeight();
        }
        else
        {
            xSize = image.getWidth();
            ySize = image.getHeight();
        }
        
        //Create the new image
        Image newImage = createImage(xSize, ySize, 0x00000000);
        Graphics g = newImage.getGraphics();
        
//#if MIDP20
//#         Utils.drawImage(g, new Sprite(image), 0, 0, Graphics.TOP | Graphics.LEFT, manipulation);
//#elif NOKIAUI
//#         Utils.drawImage(g, image, 0, 0, Graphics.TOP | Graphics.LEFT, manipulation);
//# // //#elif SIEMENS
//# //TODO:  I know that the siemens api has a function to do this, must have a look
//#else
        for(int x = 0; x < xSize; x++)
        {
            for(int y = 0; y < ySize; y++)
            {
                int xPos = 0;
                int yPos = 0;
                
                g.setClip(x, y, 1, 1);
                g.clipRect(x, y, 1, 1);
                if((manipulation & flipHorzFlag) == flipHorzFlag)
                {
                    xPos = -image.getWidth() + 2*x + 1;
                }
                if((manipulation & flipVertFlag) == flipVertFlag)
                {
                    yPos = -image.getHeight() + 2*y + 1;
                }
                if((manipulation & rot90Flag) == rot90Flag)
                {
//TODO::: figure what to do with the rotation
                }
                
                g.drawImage(image, xPos, yPos, Graphics.TOP | Graphics.LEFT);
            }
        }
//#endif
        
        return newImage;
    }
    
    /**
     * replaces all colours in the from array, with the colours in the to array,
     * for a single image
     *
     * @param image - the image to replace the colours in
     * @param fromColours - the array of colours to replace
     * @param toColours - the array of colours to replace with
     *
     */
    public static final Image replaceColoursInImage(Image image, short[] fromColours, short[] toColours)
    {
//#if NOKIAUI
//#         if(image != null)
//#         {
//#             //save the width and height of the image
//#             final int xSize = image.getWidth();
//#             final int ySize = image.getHeight();
//#             DirectGraphics dg;
//#             final short pixels[] = new short[xSize * ySize];
//#             //Get thre pixel format
//#             final int pixelFormat = DirectGraphics.TYPE_USHORT_4444_ARGB; //dg.getNativePixelFormat();
//#             //were any colours changed
//#             boolean changed = false;
//# 
//#             //Get the direct graphics
//#             dg = DirectUtils.getDirectGraphics(image.getGraphics());
//#             //Get the pixels
//#             dg.getPixels(pixels, 0, xSize, 0, 0, xSize, ySize, pixelFormat);
//# 
//#             //Go through all the pixels
//#             for(int i = 0; i < pixels.length; i++)
//#             {
//#                 for(int j = 0; j < fromColours.length; j++)
//#                 {
//#                     //check if this pixel is the colour we are looking for
//#                     if(pixels[i] == fromColours[j])
//#                     {
//#                         changed = true;
//#                         pixels[i] = toColours[j];
//#                     }
//#                 }
//#             }
//# 
//#             //Only replace the image if any pixels were changed
//#             if(changed == true)
//#             {
//#                 //Create a new image
//#                 image = Utils.createImage(xSize, ySize, 0x00000000);
//#                 dg = DirectUtils.getDirectGraphics(image.getGraphics());
//#                 //draw the modified pixel to the image
//#                 dg.drawPixels(pixels, true, 0, xSize, 0, 0, xSize, ySize, 0, pixelFormat);
//#             }
//#         }
        //#endif
        
        return image;
    }
    
    /**
     * Common FP values
     */
    public static final int FP_2_0  = Utils.InttoFP(2);
    public static final int FP_1_0  = Utils.InttoFP(1);
    public static final int FP_0_5  = Utils.FPdivide(FP_1_0, FP_2_0);
    public static final int FP_0_25 = Utils.FPdivide(FP_0_5, FP_2_0);
    public static final int FP_0_125 = Utils.FPdivide(FP_0_25, FP_2_0);
    public static final int FP_0_0625 = Utils.FPdivide(FP_0_125, FP_2_0);
    
    /**
     * Fixed point precision
     */
    public static final int FPPRECISION = 7;
    
    /**
     *
     * @param fp int
     * @return int
     */
    public static final int FPtoInt(int fp)
    {
//#if EASY_FP        
//#         return fp / 100;
//#else
        return fp >> FPPRECISION;
//#endif
    }
    
// TODO: need to make these fixed point functions inlined
    
    /**
     *
     * @param i int
     * @return int
     */
    public static final int InttoFP(int i)
    {
//#if EASY_FP        
//#         return i * 100;
//#else
        return i << FPPRECISION;
//#endif    
    }
    
    /**
     *
     * @param FP1 int
     * @param FP2 int
     * @return int
     */
    public static final int FPdivide(int FP1, int FP2)
    {
//#if EASY_FP        
//#         return( (FP1 * 100) / FP2);
//#else
        return( (FP1 << FPPRECISION) / FP2);
//#endif    
    }
    
    /**
     *
     * @param FP1 int
     * @param FP2 int
     * @return int
     */
    public static final int FPmultiply(int FP1, int FP2)
    {
//#if EASY_FP        
//#         return (int)((((long)FP1) * ((long)FP2)) / 100);
//#else
        return (int)((((long)FP1) * ((long)FP2)) >> FPPRECISION);
//#endif    
    }
    
    /*
     * Convert a string into a 2-dimansional char array based on display size
     * @param str The source string
     * @param newLines Should the string be broken up according to newline characters
     * @param with The width to fit the text into. Width <= 0 indicates no width fitting.
     *
     * Note: This is an expensive method, run it once and store the output!
     */
    public static char [][] getDisplayCharArray(String src, boolean newLines, int width)
    {
        Vector lineStrings = new Vector();
        int lastIndex, nextIndex, strLen;
        
        //First split by newline characters
        if(newLines)
        {
            strLen = src.length();
            lastIndex = -1;
            nextIndex = src.indexOf('\n');
            nextIndex = (nextIndex >= 0)?nextIndex:strLen;
            
            //As long as there are still characters in the string
            while(lastIndex != nextIndex)
            {
                //Add the substring to our list
                lineStrings.addElement(src.substring(lastIndex+1, nextIndex));
                
                //Move on
                lastIndex = nextIndex;
                nextIndex = src.indexOf('\n', lastIndex+1);
                nextIndex = (nextIndex >= 0)?nextIndex:strLen;
            }
        }
        else
        {
            lineStrings.addElement(src);
        }
        
        
        //Now fit the strings into the specified width
        if(width > 0)
        {
            String str;
            Vector temp = lineStrings;
            lineStrings = new Vector();
            int limit;
            
            //Run through each string so far
            for(int i = 0; i < temp.size(); i++)
            {
                str = ((String) temp.elementAt(i));
                strLen = str.length();
                if(strLen <= width)
                {
                    lineStrings.addElement(str);
                    continue;
                }
                limit = Math.min(width, strLen);
                nextIndex = str.lastIndexOf(' ', limit);
                if(nextIndex == -1)
                {
                    nextIndex = str.lastIndexOf('s', limit);
                }
                if(nextIndex == -1)
                {
                    nextIndex = str.lastIndexOf('.', limit);
                    if(nextIndex < limit)
                    {
                        nextIndex++;
                    }
                }
                nextIndex = (nextIndex >= 0)?nextIndex:limit;
                
                //Break check is handled in the loop
                while(true)
                {
                    //Add the substring to our list
                    lineStrings.addElement(str.substring(0, nextIndex));
                    
                    //Have we reached the end of the string?
                    if(nextIndex == strLen)
                    {
                        break;
                    }
                    else
                    {
                        //Did we perhaps split in the middle of a word?
                        if(' ' == str.charAt(nextIndex) || 's' == str.charAt(nextIndex))
                        {
                            str = str.substring(nextIndex+1, strLen);
                        }
                        else
                        {
                            str = str.substring(nextIndex, strLen);
                        }
                        
                        //Move on
                        strLen = str.length();
                        if(strLen <= width)
                        {
                            lineStrings.addElement(str);
                            break;
                        }
                        limit = Math.min(width, strLen);
                        nextIndex = str.lastIndexOf(' ', limit);
                        nextIndex = (nextIndex >= 0)?nextIndex:limit;
                    }
                }
            }
        }
        
        //Finally convert the vector into a char[][]
        int vecLen = lineStrings.size();
        char [][] retArray = new char[vecLen][];
        for(int i = 0; i < vecLen; i++)
        {
            retArray[i] = ((String) lineStrings.elementAt(i)).toCharArray();
        }
        
        return retArray;
    }
    
    /**
     * Perform a stock animation effect
     * @param graphics The target graphics object
     * @param timeSoFar The time taken so far by the animation
     * @param x The top left x position
     * @param y The top left y position
     * @param dx The x size of the wipe.
     * @param dy The y size of the wipe
     * @param clour The wipe colour
     * @param animIn - if true - the wipe animation will be going to it's 
     *
     * returns true if it renderred to the screen, else false.
     */
    public static boolean paintAnimationWipe(Graphics graphics, 
            int timeSoFar,
            int timeToWipeIn,
            int timeToShow,
            int timeToWipeOut,
            int x,
            int y,
            int dx,
            int dy,
            int colour)
    {
        if(timeSoFar <= timeToWipeIn)
        {
            Utils.fillRect(graphics, x, y, (dx * (timeToWipeIn - timeSoFar)) / timeToWipeIn, dy, colour);
            return true;
        }
        else if(timeSoFar >= (timeToWipeIn + timeToShow))
        {
            Utils.fillRect(graphics, x, y, (dx * (timeSoFar - (timeToWipeIn + timeToShow))) / timeToWipeOut, dy, colour);
            return true;
        }
        
        return false;
    }
    
    /**
     * Safely opens a resource as a stream
     * This method is a workaround for the issue where
     *  MIDP does not specify exactly when classes not in
     *  the JAR are addressable
     *
     * @param name Path in the jar to the resource
     */
    public static InputStream getResourceAsStream(String name)
    {
        //Create our instanceClass if it does not yet exist
        if(null == instanceClass)
        {
            instanceClass = (new Utils()).getClass();
        }
        
        return instanceClass.getResourceAsStream(name);
    }
    
    
    public static int calcCRC(int crcInit, int crcVal, String str)
    {
        if(str == null)
        {
            return 0;
        }
        
        int crc1 = crcInit;
        int crc2 = crcVal;
        for(int j = 0; j < str.length(); j++)
        {
            crc1 = (crc1 + str.charAt(j)) & 0x7fff;
            crc2 = (crc2 + (crc1 * crcVal) + str.charAt(j) & 0xffff);
        }
        return (crc1 << 16) | (crc2);
    }
    
    /**
     * pseudo random function
     *
     */
    public static int pseudoRnd(int seed)
    {
        seed = (int)((long)seed * (long)214013 + (long)2531011);
        return (((seed) >> 16) & 0x7fff);
    }
    
    //version 1.0 of the encryption
    public static final String ENCRYPTION_VERSION = "10|";
    
    /**
     * encrypts a string
     * the password should be saved on the client and server side while the random number will be
     * embedded into the encrypted string
     * string will probably come out double the size...
     */
    public static String encryptString(String clearText, String password, int pseudoRndInit)
    {
        StringBuffer encryptedString = new StringBuffer(clearText.length()*2);
        
        //add an encryption header
        encryptedString.append(ENCRYPTION_VERSION);
        //add the random number used for this encryption
        encryptedString.append(pseudoRndInit);
        //add the separator before the encrypted stuff
        encryptedString.append("|");
        
        //enctypt it
        int rnd = pseudoRndInit;
        int crc = pseudoRndInit;
        int j = 0;
        for( int i = 0; i < clearText.length(); i++)
        {
            //update random number
            rnd = pseudoRnd(rnd);
            
            int passChar = (int)password.charAt((j++) % password.length());
            int clearChar = (int)clearText.charAt(i);
            //simply xor them all together
            int val = (clearChar ^ passChar) ^ (rnd&0xff);
            
            //prepend leading zero if necessary
            if(val < 16)
            {
                encryptedString.append("0");
            }
            //hex encode
            encryptedString.append( Integer.toHexString(val) );
            //update crc
            crc += clearChar + passChar;
        }
        //save the crc
        crc = crc & 0xff;
        if(crc < 16)
        {
            encryptedString.append("0");
        }
        encryptedString.append( Integer.toHexString(crc) );
        
        return encryptedString.toString();
    }
    
    /**
     * decrypts a string
     * the password should be saved on the client and server side while the random number will be
     * embedded into the encrypted string
     */
    public static String decryptString(String encryptedText, String password) throws Exception
    {
        int idx;
        
        if(encryptedText == null || password == null)
        {
            throw new Exception("ENCRYPTION ERROR 1.");
        }
        
        StringBuffer decryptedString = new StringBuffer(encryptedText.length()/2);
        
        //check version
        if(!encryptedText.startsWith(ENCRYPTION_VERSION))
        {
            throw new Exception("ENCRYPTION ERROR 2.");
        }
        
        //find the rnd number seed used
        idx = encryptedText.indexOf("|", ENCRYPTION_VERSION.length());
        if(idx == -1)
        {
            throw new Exception("ENCRYPTION ERROR 3.");
        }
        int rnd = Integer.parseInt(encryptedText.substring(ENCRYPTION_VERSION.length(), idx));
        
        //decrypt
        int crc = rnd;
        int j = 0;
        for( int i = idx+1; i < encryptedText.length()-2; i+=2)
        {
            //update random number
            rnd = pseudoRnd(rnd);
            //first we need to unhexecode it
            int encChar = Integer.parseInt(encryptedText.substring(i, i+2), 16);
            
            int passChar = (int)password.charAt((j++) % password.length());
            //simple xor
            int val = (encChar ^ passChar) ^ (rnd&0xff);
            
            //append
            decryptedString.append( (char)val );
            //calculate the crc
            crc += passChar + val;
        }
        
        //check the crc
        int check = Integer.parseInt(encryptedText.substring(encryptedText.length()-2), 16);
        if(check != (crc&0xff))
        {
            throw new Exception("ENCRYPTION ERROR 4.");
        }
        
        return decryptedString.toString();
    }
    
    
//#if BLUETOOTH
//--     public static final int BTACT_none =                   0;          //no action
//--     public static final int BTACT_ExcErr =                 1;          //error with an exception
//--     public static final int BTACT_ExcCloseNotifier =       2;          //error with an exception
//--     public static final int BTACT_ErrExc =                 3;          //error with an exception
//--     public static final int BTACT_WarnNodevicesFound =     4;          //no devices found warning
//--     public static final int BTACT_WarnNoServiceFound =     5;          //no service found warning
//--     public static final int BTACT_FoundService =           6;          //a service was found, obj = name
//--     public static final int BTACT_Connecting =             7;          //connecting to device
//--     public static final int BTACT_ExcLocalDeviceInq =      8;          //local device inquiry error with an exception
//--     public static final int BTACT_SearchService =          9;          //search service started
//--     public static final int BTACT_SearchCancelled =       10;          //the search has been cancelled
//--     public static final int BTACT_ExcCancel =             11;          //cancel error with a exception
//--     public static final int BTACT_ProgressStart =         12;          //start the progress bar, obj = num steps
//--     public static final int BTACT_ProgressUpdate =        13;          //update the progress bar, obj = current step
//--     public static final int BTACT_ProgressComplete =      14;          //any progress bars should be set to 100% and removed
//--     public static final int BTACT_WaitingForConnection =  15;          //waiting for a conneciton has started
//--     public static final int BTACT_StartInquiry =          16;          //device inquiry started
//--     public static final int BTACT_Connected =             17;          //connected successfully object = array of connections
//--     public static final int BTACT_Disconnected =          18;          //disconnected
//--     public static final int BTACT_DataReceived =          19;          //Data received
//--
//--     public static BluetoothCallback btCB;
//--     public static BluetoothDiscovery BTdisc;
//--     private static void BTinitDiscovery( String UUID, BluetoothCallback btCB )
//--     {
//--      System.out.println("new BluetoothDiscovery( btCB );");
//--        // Create Discovery Object
//--        BTdisc = new BluetoothDiscovery( btCB );
//--
//--      System.out.println("BTdisc.setServiceUUID( UUID );");
//--        // Set UUID
//--        BTdisc.setServiceUUID( UUID );
//--
//--        String name;
//--
//--        // Check if Bluetooth is turned on
//--        try
//--        {
//--      System.out.println("LocalDevice.getLocalDevice().getFriendlyName()");
//--            name = LocalDevice.getLocalDevice().getFriendlyName();
//--        }
//--//        catch( BluetoothStateException e )
//--        catch( Exception e )
//--        {
//--            // display user notification
//--//            showAlertAndExit( "", "Please switch Bluetooth on!", AlertType.ERROR );
//--            btCB.bluetoothAction(BTACT_ExcErr, e);
//--            return;
//--        }
//--
//--        // Sets the name how this device is shown to the remote user
//--        BTdisc.setName( name );
//--     }
//--
//--     public static BTServerThread st;
//--     public static void BTServerStart( String UUID, BluetoothCallback btCB )
//--     {
//--         System.out.println("BTServerStart");
//--         Utils.btCB = btCB;
//--         //initialize the bluetooth discovery
//--         BTinitDiscovery(UUID, btCB);
//--
//--      System.out.println("new BTServerThread();");
//--
//--        // Start Server
//--        st = new BTServerThread();
//--        st.start();
//--     }
//--     public static void BTServerStop()
//--     {
//--         System.out.println("BTServerStop");
//--         try
//--         {
//--             if(BTdisc != null)
//--             {
//--                BTdisc.kill();
//--             }
//--             if(st != null)
//--             {
//--                st.join();
//--             }
//--         }
//--         catch(InterruptedException e)
//--         {
//--         }
//--        st = null;
//--        BTdisc = null;
//--     }
//--
//--     public static BTClientThread ct;
//--     public static void BTClientStart( String UUID, BluetoothCallback btCB )
//--     {
//--         Utils.btCB = btCB;
//--         //initialize the bluetooth discovery
//--         BTinitDiscovery(UUID, btCB);
//--        // Start Client with SEARCH_CONNECT_FIRST_FOUND
//--        ct = new BTClientThread( BluetoothDiscovery.SEARCH_CONNECT_FIRST_FOUND );
//--        ct.start();
//--     }
//--
//--     public static void BTStartReceiveThreads(BluetoothConnection[] conn)
//--     {
//--         System.out.println("BTStartReceiveThreads");
//--        for( int i=0; i<conn.length; i++ )
//--        {
//--            // loop through all connections
//--            ReceiveThread rt = new ReceiveThread( conn[i], i );
//--            rt.start();
//--        }
//--     }
//--
//--    /**
//--     * Checks if all connections are closed.
//--     * If so, jumps to main menu.
//--     * @return true if all connection are closed, otherwise false.
//--     */
//--    private static boolean checkIfAllClosed()
//--    {
//--/*
//--        // Check if all connections are closed
//--        boolean allclosed = true;
//--        for( int l=0; l<btConnections.length; l++ )
//--        {
//--            if( btConnections[l].isClosed() != true  )
//--            {   // still open
//--                allclosed = false;
//--            }
//--        }
//--        // If all connections closed then restart
//--        if( allclosed )
//--        {   // And restart
//--            startUI();
//--        }
//--        // return
//--        return allclosed;
//--  */
//--        return true;
//--    }
//--
//--
//--     public static boolean BThandleCallback( int actionID, Object obj)
//--     {
//--         System.out.println(actionID + " " + obj);
//--         return true;
//--     }
//--
//--    // Innerclass
//--    /** The ServerThread is used to wait until someone connects. <br
//--     * A thread is needed otherwise it would not be possible to display
//--     * anything to the user.
//--     */
//--    private static class BTServerThread
//--    extends Thread
//--    {
//--        /**
//--         * This method runs the server.
//--         */
//--        public void run()
//--        {
//--            try
//--            {
//--                // Wait on client
//--                BluetoothConnection[] con = BTdisc.waitOnConnection();
//--                if( con[0] == null )
//--                {
//--                    System.out.println("Connection cancelled");
//--                    // Connection cancelled
//--                    return;
//--                }
//--                btCB.bluetoothAction(BTACT_Connected, con);
//--            }
//--            catch( Exception e )
//--            {
//--                // display error message
//--                e.printStackTrace();
//--                return;
//--            }
//--
//--        }
//--    }
//--
//--    // Innerclass
//--    /** The ClientThread is used to search for devices/Services and connect to them. <br>
//--     * A thread is needed otherwise it would not be possible to display
//--     * anything to the user.
//--     */
//--    private static class BTClientThread
//--    extends Thread
//--    {
//--        // Search type
//--        private int searchType;
//--
//--        /** Constructor
//--         * @param st The search type. Possible values:
//--         * {@link BluetoothDiscovery.SEARCH_CONNECT_FIRST_FOUND SEARCH_CONNECT_FIRST_FOUND},
//--         * {@link BluetoothDiscovery.SEARCH_CONNECT_ALL_FOUND SEARCH_CONNECT_ALL_FOUND},
//--         * {@link BluetoothDiscovery.SEARCH_ALL_DEVICES_SELECT_ONE SEARCH_ALL_DEVICES_SELECT_ONE},
//--         * {@link BluetoothDiscovery.SEARCH_ALL_DEVICES_SELECT_SEVERAL SEARCH_ALL_DEVICES_SELECT_SEVERAL}.
//--         */
//--        protected BTClientThread( int st )
//--        {
//--            // store search type
//--            searchType = st;
//--        }
//--
//--
//--        /**
//--         * This method runs the client.
//--         */
//--        public void run()
//--        {
//--            try
//--            {
//--                BluetoothConnection conn[] = BTdisc.searchService( searchType );
//--                if( conn.length != 0 )
//--                {
//--                    btCB.bluetoothAction(BTACT_Connected, conn);
//--                    for( int i=0; i<conn.length; i++ )
//--                    {
//--                        // loop through all connections
//--                        System.out.println(conn[i].remoteName);
//--                    }
//--                }
//--                else
//--                {
//--                    // nothing found
//--                }
//--            }
//--            catch( Exception e )
//--            {    // display error message
//--                return;
//--            }
//--        }
//--    }
//--
//--        // Inner class
//--        /**
//--         * The ReceiveThread is used to receive the remote keypresses. <br>
//--         * For each remote device there exists an own RecieveThread.
//--         */
//--        private static class ReceiveThread
//--        extends Thread
//--        {
//--            BluetoothConnection conn;
//--            int index;
//--
//--            /**
//--             * Constructor.
//--             * @param i Index, that corresponds to the number of the BluetoothConnection.
//--             */
//--            public ReceiveThread( BluetoothConnection c, int i )
//--            {
//--                // Store
//--                conn = c;
//--                index = i;
//--            }
//--
//--            /**
//--             * Reads from stream until end of stream reached (disconnect).<br>
//--             * The read character (which is the key the remote user pressed) is
//--             * displayed to the local user.
//--             */
//--            public void run()
//--            {
//--                while( true )
//--                {
//--                    while( true )
//--                    {
//--                        byte[] bytes;
//--
//--                        // Read (blocking)
//--                        try
//--                        {
//--                            bytes = conn.readBytes();
//--                            btCB.bluetoothAction(BTACT_DataReceived, bytes);
//--                        }
//--                        catch( IOException e )
//--                        {
//--                            // If error, then disconnect
//--                            conn.close();
//--                            // Check if all connections are closed
//--                            checkIfAllClosed();
//--
//--                            btCB.bluetoothAction(BTACT_Disconnected, e);
//--                            return;
//--                        }
//--
//--                        if( bytes == null )
//--                        {
//--                            // Close
//--                            conn.close();
//--                            // Check if all connections are closed
//--                            checkIfAllClosed();
//--                            // show that device is disconnected
//--                            btCB.bluetoothAction(BTACT_Disconnected, null);
//--                            return;
//--                        }
//--                    }
//--                }
//--            }
//--        }
//--
//#
//#endif
    
//#if ONLINE_SUBMIT
//#     public static String ON_HEADER = "SFGD";
//# 
//#     private static void setRequestPropertySafe(HttpConnection con, String header, String value) throws IOException
//#     {
//#         if(null != value)
//#         {
//#             con.setRequestProperty(header, value);
//#         }
//# 
//# //        System.out.println("header = value: " + header + " = " + value);
//#     }
//# 
//#     private static void setHttpConnectionRequestSystemParams(HttpConnection con, Canvas canvas, MIDlet midlet) throws IOException
//#     {
//#         String str;
//# 
//#         //Jad/Jar properties
//#         setRequestPropertySafe(con, "SF-MidletVersion", midlet.getAppProperty("MIDlet-version"));
//# 
//#         //Runtime properties
//#         con.setRequestProperty("SF-TotMem", Long.toString(Runtime.getRuntime().totalMemory()));
//#         con.setRequestProperty("SF-FreeMem", Long.toString(Runtime.getRuntime().freeMemory()));
//# 
//#         //System properties
//#         setRequestPropertySafe(con, "SF-Platform", System.getProperty("microedition.platform"));
//#         setRequestPropertySafe(con, "SF-Configuration", System.getProperty("microedition.configuration"));
//#         setRequestPropertySafe(con, "SF-Profile", System.getProperty("microedition.profiles"));
//#         setRequestPropertySafe(con, "SF-Locale", System.getProperty("microedition.locale"));
//#         setRequestPropertySafe(con, "SF-HostName", System.getProperty("microedition.hostname"));
//# 
//#         //Canvas properties
//#         con.setRequestProperty("SF-CanvasSize", ""+canvas.getWidth()+"x"+canvas.getHeight());
//#     }
//# 
//#     /**
//#      * Post a request with some headers and content to the server and
//#      * process the headers and content.
//#      * <p>
//#      * Connector.open is used to open url and a HttpConnection is returned.
//#      * The request method is set to POST and request headers set.
//#      * A simple command is written and flushed.
//#      * The HTTP headers are read and processed.
//#      * If the length is available, it is used to read the data in bulk.
//#      * From the StreamConnection the InputStream is opened.
//#      * It is used to read every character until end of file (-1).
//#      * If an exception is thrown the connection and stream is closed.
//#      * @param url the URL to process.
//#      */
//#     public static Vector postViaHttpConnection(String url, String [] reqPropHdrs, String [] reqPropVals,
//#             Canvas canvas, MIDlet midlet,
//#             String postData) throws IOException
//#     {
//#         int status = 0;
//#         HttpConnection c = null;
//#         InputStream is = null;
//#         OutputStream os = null;
//#         Vector headers = null;
//# 
//#         gc();
//# 
//#         try
//#         {
//# 
//#             c = (HttpConnection)Connector.open(url);
//# 
//#             // Set the request method and headers
//#             c.setRequestMethod(HttpConnection.GET);
//#             c.setRequestProperty("Connection", "Close");
//#             //c.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.0");
//#             //c.setRequestProperty("Content-Language", "en-US");
//#             //c.setRequestProperty("Content-Length", Integer.toString(-1));
//#             //c.setRequestProperty("Accept", "text/html,text/");
//#             //c.setRequestProperty("Accept-Encoding", "identity");
//#             //c.setRequestProperty("Content-Type", "text/plain");
//# 
//#             setHttpConnectionRequestSystemParams(c, canvas, midlet);
//# 
//#             //Add user specified headers
//#             for(int i = Math.min(reqPropHdrs.length, reqPropVals.length)-1; i >= 0; --i)
//#             {
//#                 Utils.setRequestPropertySafe(c, reqPropHdrs[i], reqPropVals[i]);
//#             }
//# 
//#             // Getting the output stream may flush the headers
//#             //don't write  anothing
//#             if(postData.length() > 0)
//#             {
//#                 os = c.openOutputStream();
//#                 os.write(postData.getBytes());
//#                 os.close();		//Flush causes problems in some cases- use close
//#             }
//# 
//#             // Get the status code, causing the connection to be made
//#             status = c.getResponseCode();
//# 
//#             // Any 500 status number (500, 501) means there was a server error
//#             if ((status == HttpConnection.HTTP_NOT_IMPLEMENTED) ||
//#                     (status == HttpConnection.HTTP_VERSION) ||
//#                     (status == HttpConnection.HTTP_INTERNAL_ERROR) ||
//#                     (status == HttpConnection.HTTP_GATEWAY_TIMEOUT) ||
//#                     (status == HttpConnection.HTTP_BAD_GATEWAY))
//#             {
//#                 System.err.print("WARNING: Server error status ["+status+"] ");
//#                 System.err.println("returned for url ["+url+"]");
//# 
//#                 if (is != null)
//#                 {
//#                     is.close();
//#                 }
//#                 if (os != null)
//#                 {
//#                     os.close();
//#                 }
//#                 if (c != null)
//#                 {
//#                     c.close();
//#                 }
//#                 throw new IOException("WARNING: Server error status ["+status+"] ");
//#             }
//# 
//# 
//#             // Only HTTP_OK (200) means the content is returned.
//#             if (status != HttpConnection.HTTP_OK)
//#             {
//#                 throw new IOException("Response status not OK ["+status+"]");
//#             }
//# 
//#             // Open the InputStream and get the ContentType
//#             is = c.openInputStream();
//#             String type = c.getType();
//# 
//#             // Get the length and process the data
//#             int ch, count = 0;
//#             int len = (int)c.getLength();
//#             StringBuffer retStr = new StringBuffer();
//#             if (len != -1)
//#             {
//#                 // Read exactly Content-Length bytes
//#                 for (int i = 0; i < len; i++)
//#                 {
//#                     if ((ch = is.read()) != -1)
//#                     {
//#                         if (ch <= ' ')
//#                         {
//#                             ch = ' ';
//#                         }
//#                         retStr.append((char) ch);
//#                         if (++count > MAX_HTTPCON_CHARS)
//#                         {
//#                             break;
//#                         }
//#                     }
//#                 }
//#             }
//#             else
//#             {
//#                 byte data[] = new byte[MAX_HTTPCON_CHARS];
//#                 int n = is.read(data, 0, data.length);
//#                 for (int i = 0; i < n; i++)
//#                 {
//#                     ch = data[i] & 0x000000ff;
//#                     retStr.append((char)ch);
//#                 }
//#             }
//# 
//#             //parse the body.
//#             String body = retStr.toString();
//# 
//# 
//# //            body = body.replace('\n', '+');
//# //            body = body.replace(' ', '+');
//# 
//#             headers = new Vector();
//# 
//#             int lstIdx = 0;
//#             int nxtIdx = 0;
//# 
//#             nxtIdx = body.indexOf('+', lstIdx);
//#             if(nxtIdx == -1)
//#                 return null;
//#             //check SFGD header
//#             if(body.substring(lstIdx, nxtIdx).equals(ON_HEADER))
//#             {
//#                 lstIdx = nxtIdx+1;
//#                 nxtIdx = body.indexOf('+', lstIdx);
//#                 if(nxtIdx == -1)
//#                     return null;
//#                 while (nxtIdx != lstIdx)
//#                 {
//#                     //these are headers
//#                     headers.addElement(body.substring(lstIdx, nxtIdx));
//#                     lstIdx = nxtIdx+1;
//#                     nxtIdx = body.indexOf('+', lstIdx);
//#                     if(nxtIdx == -1)
//#                         return null;
//#                 }
//#                 headers.addElement(body.substring(nxtIdx+1));
//#             }
//# 
//#         }
//#         finally
//#         {
//#             if (is != null)
//#                 is.close();
//#             if (os != null)
//#                 os.close();
//#             if (c != null)
//#                 c.close();
//#         }
//# 
//#         return headers;
//#     }
//#endif
    
    /*
     * Generate a random positive integer value between min (incl) and max (excl),
     *  using rand.
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @param rand Random object to use to generate the value
     * @return a random positive integer in the range [min, max)
     */
    public static int getRandomPositiveInt(int min, int max, Random rand)
    {
        int retval = Math.abs(rand.nextInt());
        retval %= (max - min);
        return retval + min;
    }
    
    /*
     * Get the current clipbounds from the graphics context and store them in clipBounds
     * @param g The graphics context to obtain the clipbounds from
     * @return an array into which the bounds will be copied
     * @note The results are stored in clipbounds as follows:
     * clipBounds[0] = clipX, clipBounds[1] = clipY,
     * clipBounds[2] = clipWidth, clipBounds[3] = clipHeight
     */
    public static int [] getClipBounds(Graphics g)
    {
        return new int [] {g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight()};
    }
    
    public static void setClip(Graphics g, int x, int y, int dx, int dy)
    {
        g.setClip(xROffset + x, yROffset + y, dx, dy);
        g.clipRect(xROffset + x, yROffset + y, dx, dy);
    }


    /**
     * pre pads a string with a specific character to a certain length.
     * e.g.   123 -> 000123
     */
    public static final String prePadString(String str, char pad, int len)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < len - str.length(); i++)
        {
            buf.append(pad);
        }
        return buf.append(str).toString();
    }
    
    /**
     * pre pads a string with a specific character to a certain length.
     * e.g.   123 -> 000123
     */
    public static final String prePadInt(int val, char pad, int len)
    {
        return prePadString(Integer.toString(val), pad, len);
    }
    
    
    
    /**
     * retrieves the Last error message
     *
     */
    public static String getLastError()
    {
        String [] errArray = new String[1];
        loadStringIntArray(SF_ERR, errArray, null);
        return errArray[0];
    }
    
    /*
     * Error handling message that will process the error
     *  depending on build properties.
     * @param e Exception object
     * @param str Error string
     * @param id Error ID
     */
    public static void handleError(Exception e, String str, int id)
    {
        StringBuffer strBfr = new StringBuffer();
        
        //Add error code
        strBfr.append("[ERR #");
        strBfr.append(id);
        strBfr.append("]\n");
//#if DEBUGINFO
//#         System.out.println(strBfr.toString());
//#endif
        
        //Add custom error message
        if(null != str)
        {
            strBfr.append(str);
            strBfr.append("]\n");
//#if DEBUGINFO
//#             System.out.println(str);
//#endif
        }
        
        //Add exception
        if(null != e)
        {
            strBfr.append(e);
            strBfr.append("]\n");
//#if DEBUGINFO
//#             e.printStackTrace();
//#endif
        }
        
//#if DEBUGINFO
//#         //Add extra debug info
//#         strBfr.append("Total mem: ");
//#         strBfr.append(Runtime.getRuntime().totalMemory());
//#         strBfr.append("\nFree mem: ");
//#         gc();
//#         strBfr.append(Runtime.getRuntime().freeMemory());
//#         strBfr.append("]\n");
//#endif

        //Save the error to the RMS
        String [] errArray = new String[1];
        errArray[0] = strBfr.toString();
        saveStringIntArray(SF_ERR, errArray, null);
        
//#if DEBUGINFO
//#         //Display the error (current displayable will automatically be reverted to)
//#         if(midlet != null && (e != null || str != null) )
//#         {
//#             Alert alert = new Alert("Error", strBfr.toString(), null, AlertType.ERROR);
//#             Display.getDisplay(midlet).setCurrent(alert);
//#         }
//#endif
        
        //While we're doing something slow, may as well garbage collect
        Utils.gc();
    }

    /*
     * RumbleX constants
     */
    static final int RX_COMPANYID = 10;
    static final String RX_COMPANYPASSWORD = "flint";
    
//#ifdef RUMBLEX    
//#     /**
//#      * Submit a single score to RumbleX
//#      * @return Returns The RumbleX result name if successful, null otherwise.
//#      * @param gameID The RumbleX Game ID
//#      * @param subGameID The RumbleX Subgame ID (-1 for none)
//#      * @param playerID Player name, ID or email address
//#      * @param scoreValue Integer representation of the score
//#      * @param scoreString String representation of the score
//#      * @param additionalDevData String additional data that should be uploaded with the score, such as an error
//#      */
//#     public static String rxSubmitSingleScore(int gameID, int subGameID, String playerID, int scoreValue, String scoreString, String additionalDevData)
//#     {
//#         // Set up the RX object        
//#         com.rumblex.RumbleX rx = new com.rumblex.RumbleX();
//#         rx.setCompanyID(RX_COMPANYID);
//#         rx.setCompanyPass(RX_COMPANYPASSWORD);
//#         rx.setGameID(gameID);
//#         rx.setDump(additionalDevData);
//#         if(subGameID > 0)
//#         {
//#             rx.setSubGameID(subGameID);
//#         }
//#         rx.addScore(playerID, scoreValue, scoreString);
//#         rx.send();
//#         do{ 
//#             try{ 
//#                 Thread.sleep(0); 
//#             }catch(InterruptedException e){} 
//#         } while(!rx.uploadDone);
//#         if(rx.errorCode > rx.rxError_NoError)
//#         {
//#             Utils.handleError(null, "RumbleX error", rx.errorCode);
//#         }
//#         else if(rx.errorCode == -1)
//#         {
//#             //I think this means that scores were submitted to the server, but
//#             //the response is crap.  It has a mysql error in it or something.
//#             return playerID;    //so just return what was entered.
//#         }       
//#         else
//#         {
//#             return rx.resultName.toUpperCase();
//#         }
//#         return null;
//#     }
//# 
//#     /**
//#      * Submit a single score to RumbleX
//#      * @return Returns The RumbleX result name if successful, null otherwise.
//#      * @param gameID The RumbleX Game ID
//#      * @param subGameID The RumbleX Subgame ID (<= for none)
//#      * @param scoreView The view used to fetch scores
//#      * @param tryOlderViews Whether to check older score views if the result is empty
//#      * @param playerIDs Array in which retrieved player IDs will be stored
//#      * @param scores Array in which retrieved scores will be stored
//#      * @param startIdx Array index to begin at (inclusive)
//#      * @param endIdx Array index to end at (exclusive)
//#      * @param timeOut Maximum amount of time to wait for RumbleX
//#      */
//#     public static boolean rxRetrieveScores(int gameID, int subGameID, int scoreView, boolean tryOlderViews, 
//#                                                     String [] playerIDs, int [] scores, 
//#                                                     int startIdx, int endIdx, long timeOut)
//#     {
//#         // Set up the RX object        
//#         com.rumblex.RumbleX rx = new com.rumblex.RumbleX();
//#         rx.setCompanyID(RX_COMPANYID);
//#         rx.setCompanyPass(RX_COMPANYPASSWORD);
//#         rx.setGameID(gameID);
//#         
//#         if(subGameID > 0)
//#         {
//#             rx.setSubGameID(subGameID);
//#         }
//#         // TEMPORARY - check for all time
//#         //rx.setScoreView(scoreView);
//#         rx.setScoreView(rx.rxScore_ALLTIME);
//#         
//#         // Request scores and wait
//#         rx.getScore(0);
//#         rx.send();
//#         long startTime = System.currentTimeMillis();
//#         do{ 
//#             try{ 
//#                 Thread.sleep(0); 
//#             }catch(InterruptedException e){} 
//#         } while(!rx.uploadDone && (System.currentTimeMillis() - startTime) < timeOut);
//#         
//#         if(!rx.uploadDone)
//#         {
//#             // Timeout
//#             Utils.handleError(null, "RumbleX timeout", 0);
//#         }
//#         else if(rx.errorCode == 201)
//#         {
//#             // TEMPORARY - check for all time
//#             /*// No results found, try older views
//#             if(tryOlderViews && scoreView != rx.rxScore_ALLTIME)
//#             {  
//#                 long remainingTime = timeOut - (System.currentTimeMillis() - startTime);
//#                 int nextView = (scoreView==rx.rxScore_TODAY)?rx.rxScore_WEEK:
//#                                 (scoreView==rx.rxScore_WEEK)?rx.rxScore_MONTH:rx.rxScore_ALLTIME;
//#                 return rxRetrieveScores(gameID, subGameID, nextView, tryOlderViews, 
//#                                         playerIDs, scores, startIdx, endIdx, remainingTime);
//#             }
//#             else*/
//#             {
//#                 Utils.handleError(null, "No scores found", 0);
//#             }
//#         }
//#         else if(rx.errorCode != rx.rxError_NoError)
//#         {
//#             // Something else went wrong
//#             Utils.handleError(null, "RumbleX error", rx.errorCode);
//#         }
//#         else
//#         {
//#             // Success
//#             int rxIdx = 0;
//#             for(int i=startIdx; i<endIdx && rxIdx < 10; i++)
//#             {
//#                 try 
//#                 {
//#                     scores[i] = Integer.parseInt(rx.getRankScore(rxIdx));
//#                 } catch (NumberFormatException e) 
//#                 {
//#                     // Not enough scores, so we ignore the empty strings
//#                     return true;
//#                 }
//#                 playerIDs[i] = rx.getRankName(rxIdx).toUpperCase();
//#                 rxIdx++;
//#             }
//#             return true;
//#         }
//#         
//#         return false;
//#     }
//#endif    
}