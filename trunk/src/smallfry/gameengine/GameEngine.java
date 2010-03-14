package smallfry.gameengine;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
//#if NOKIAUI || SONYE
//# import com.nokia.mid.ui.*;
//#endif

import smallfry.util.*;

/*
 * An extension of the nokia full screen canvas
 * that provides the central logic for the engine, updating and painting all elements.
 *
 * Revision History:
 *
 * 2005-02-27: Changed paintImage to be a MultiImage for animated images
 *             Made all updates dependant on time.
 * 2005-03-01: Added textpos and textanchor
 *             Made paintMenu use textpos and textanchor
 *             Moved gameInProgress into GameEngine
 */
public abstract class GameEngine
//#if NOKIAUI || SONYE
//# extends FullCanvas        
//# //  //#elif MIDP10_SIEMENS      //== siemens canvas respondes slower to keypressed ??
//#         //this works, but i don't think it does anything major,
//#         // i thought it would enable full screen on siemens phones,
//#         // but there is no mention of that in the docs.
//#         // i'm gonna leave it here, just because.
//# //        extends com.siemens.mp.color_game.GameCanvas
//#else 
        extends Canvas

//#endif
        implements Runnable
{
    //Random number
    public static final Random rand = new Random();
    
    //This is the minimum framerate the engine will run at, if it takes long than this rate
    //to do the update and rendering the engine will start running in slow motion
    public static int MIN_FRAMERATE = 10;
    public static int MAX_FRAMERATE = 100;
    
    //Event
    public static final int EVENT_ENDOFCOLLISIONARRAY = 0x01;
    
    //Flags
    public static final short STATE_NONE =        0;
    public static final short STATE_CLEAR =       (1 << 0);
    public static final short STATE_LOAD =        (1 << 1);
    public static final short STATE_ENGINE =      (1 << 2);
    public static final short STATE_INTERFACE_BELOW =   (1 << 3);
    public static final short STATE_INTERFACE_ONTOP =   (1 << 4);
    public static final short STATE_MENU =        (1 << 5);
    public static final short STATE_TEXT =        (1 << 6);
    public static final short STATE_IMAGE =       (1 << 7);
    public static final short STATE_QUIT =        (1 << 8);
    public static final short STATE_OTHER =       (1 << 9);
    
    //Collision
    public static final int COLL_NONE = 0;
    public static final int COLL_COLL = 1;
    public static final int COLL_ABVE = 2;
    public static final int COLL_BELW = 3;
    public static final int COLL_LEFT = 4;
    public static final int COLL_RGHT = 5;
    
    //Special object addition
    public static final int ADD_BOTTOM = 0;
    public static final int ADD_TOP = 1;
    
    //Font indices
    public static int fontMenu;
    public static int fontMessages;
    
    //Misc
    public static int frameCount = 0;
    public static boolean gameInProgress;
    public volatile static boolean paused = true;
    public static final LinkList vpList = new LinkList();  //list of viewports
    public static final ViewPort vpDefault = new ViewPort();      //the default viewport
    public static int keysDown;
    public static int keysPressed;
    public static boolean menuKeyPressed;
    public static boolean anyKeyPressed;
    
    public static final Vec2d playArea = new Vec2d();   //the total playing area of the current level
    public static int playWidth;        //the width of the play area
    public static int playHeight;       //the height of the play area
    public static int width;
    public static int height;
    
    private volatile static boolean kill;
    public static final Hashtable imageCache = new Hashtable();
    
    //Global state
    public static Class entityClass;
    public static MIDlet midlet;
    public static GameEngine instance;
    public static Thread thread;
    public static GameEntity player;
    
    public static short updateState /* = STATE_NONE*/;
    public static short prevUpdateState /* = STATE_NONE*/;
    protected static short paintState;
    protected static short prevPaintState;
    protected static int clearCol;
    protected static MultiImage paintImage;
    protected static final Vec2d paintPos = new Vec2d();
    protected static final Vec2d textPosFP = new Vec2d();
    protected static int paintAnchor;
    protected static int textAnchor;
    protected static char [][] paintStrings;
    protected static int paintSelection;
    
    //Double buffering
    protected static Image bufferImage;
    
    //Lists
    public static final LinkList entityPool = new LinkList();
    public static final LinkList activeEntities = new LinkList();
    public static final LinkList inactiveEntities = new LinkList();
    public static final LinkList[] teams;
    
    //Static initialization
    static
    {
        //#debug info
//#         System.out.println("Loading GameEngine class");

/*        
        //force the classes to load
        TilePaintableArray.loadClass();
        TileCollisionArray.loadClass();
        ScrollingLayer.loadClass();
*/        
        teams = new LinkList[GameEntity.OWNER_MAX];
        for(int i = 0; i < GameEntity.OWNER_MAX; i++)
        {
            teams[i] = new LinkList();
        }
    };
    
//#mdebug debug
//#     //Debug timing
//#     public static final int NUM_TIMERS = 3;
//#     public static final DebugTimer[] debugTimers = new DebugTimer[NUM_TIMERS];
//#     public static int curTimer = NUM_TIMERS;
//#enddebug
//#if FPS
//#     public static long fps = 0;
//#     public static int numFrames = 0;
//#     public static long fpsStartTime;
//#     public static boolean showMem = false;
//#endif
    
    
    /** INITIALIZATION
     * @param midlet
     * @param instance
     * @param entityClass
     * @param numPreallocEntities
     * @throws Exception  */
    //Static init
    public static final void Init(MIDlet midlet, GameEngine instance, Class entityClass, int numPreallocEntities)
    throws Exception
    {
        //Set simple static params
        GameEngine.midlet = midlet;
        GameEngine.entityClass = entityClass;
        GameEngine.instance = instance;
        thread = new Thread(instance);
        
        //Init basic values
        width = instance.getWidth();
        height = instance.getHeight();
        playArea.x = width;
        playArea.y = height;
        playWidth = width;
        playHeight = height;
        
        //Initialize object pooling
        for(int i = 0; i < numPreallocEntities; i++)
        {
            GameEntity obj = (GameEntity) entityClass.newInstance();
            entityPool.insertBeforeHead(obj.poolNode);
        }
        
        //Insert the default viewport
        addViewPort(vpDefault, ADD_TOP);
        
        //#if FPS
//#         numFrames = 0;
//#         fpsStartTime = System.currentTimeMillis();
        //#endif
        //#mdebug debug
//#         debugTimers[0] = new DebugTimer("TOTAL");
//#         debugTimers[1] = new DebugTimer("UPDATE");
//#         debugTimers[2] = new DebugTimer("PAINT");
        //#enddebug
       
        thread.start();
    }
    
    //Constructor
    public GameEngine(boolean forceDoubleBuffer)
    {
//#if MIDP10_SIEMENS
//#         super(false);
//#endif        
        
        instance = this;
        
//#if MIDP20
//#         setFullScreenMode(true);
//#         try { Thread.sleep(1000); } catch(Exception e) {}
//#endif
        
        //Get buffer objects if necessary
        if(!isDoubleBuffered() || forceDoubleBuffer)
        {
            bufferImage = Image.createImage(getWidth(), getHeight());
        }
    }
    
    /**
     * Final canvas paint method
     * @param graphics
     *
     * Siemens phones can call this method multiple times because the servicerepaints
     * don't work correctly.  The synchonize prevents this method from being called
     * more than once at the same time.
     *
     */
    public final synchronized void paint(Graphics graphics)
    {
        //If we are manually double buffering, swap our buffer image to screen
        if(bufferImage != null)
        {
            graphics.drawImage(bufferImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            return;
        }
        
        //otherwise just paint the world
        paintWorld(graphics);
    }
     
    public final void paintWorld(Graphics graphics)
    {
        //Paint different elements
        {
            //Clear
            if(STATE_CLEAR == (paintState & STATE_CLEAR))
            {
                Utils.setROffset(0,0, false);
                Utils.setClip(graphics, 0, 0, getWidth(), getHeight());
                Utils.fillRect(graphics, 0, 0, getWidth(), getHeight(), clearCol);
            }
            
            //Interface paint
            if(STATE_INTERFACE_BELOW == (paintState & STATE_INTERFACE_BELOW))
            {
                paintInterface(graphics, STATE_INTERFACE_BELOW);
            }
            
            //Paint main engine elements
            if(STATE_ENGINE == (paintState & STATE_ENGINE))
            {
                paintEngine(graphics);
            }
            
            //Paint image
            if(STATE_IMAGE == (paintState & STATE_IMAGE))
            {
                if(paintImage != null)
                {
                    Utils.setROffset(0,0, false);
                    Utils.drawImage(graphics, paintImage, paintPos.x, paintPos.y, paintAnchor, 0);
                }
//#mdebug warn
//# else
//# {
//#     System.out.println("STATE_IMAGE defined, but paintImage == null");
//# }
//#enddebug
            }
            
            //Perform loading paint
            if(STATE_LOAD == (paintState & STATE_LOAD))
            {
                paintLoadScreen(graphics);
            }
            
            //Interface paint
            if(STATE_INTERFACE_ONTOP == (paintState & STATE_INTERFACE_ONTOP))
            {
                paintInterface(graphics, STATE_INTERFACE_ONTOP);
            }
            
            //Paint text
            if(STATE_TEXT == (paintState & STATE_TEXT))
            {
                paintText(graphics);
            }
            
            //Paint menu
            if(STATE_MENU == (paintState & STATE_MENU))
            {
                paintMenu(graphics);
            }
            
            //Paint Other
            if(STATE_OTHER == (paintState & STATE_OTHER))
            {
                paintOther(graphics);
            }
        }
        
        //Paint debug info
//#mdebug debug        
//#         {
//#             if(curTimer < NUM_TIMERS)
//#             {
//#                 debugTimers[curTimer].paint(graphics, 2, height - 2,
//#                 Graphics.LEFT | Graphics.BOTTOM);
//#             }
//#         }
//#enddebug        
        //Paint FPS
//#if FPS
//#         {
//#             Utils.setROffset(0,0,false);
//#             char [] counterChars;
//#             if(showMem)
//#             {
//#                 counterChars = Long.toString(Runtime.getRuntime().freeMemory()).toCharArray();
//#             }
//#             else
//#             {
//#                 counterChars = Long.toString(fps).toCharArray();
//#             }
//#             Utils.drawString(graphics, counterChars, 0, getWidth() - (Utils.fonts[fontMessages].size.x * (counterChars.length+1)),
//#                     2, Graphics.LEFT | Graphics.TOP);
//#         }
//#endif
    }
    
    /**
     * Update the text
     *
     */
    protected void updateText(int dtms)
    {
        //Scroll if there is text left to scroll
        int paintStringsHeight  = Utils.FPtoInt(textPosFP.y) +
                (paintStrings.length * (Utils.fonts[fontMessages].size.y *3)/2);
        {
            //Player scrolling down
            if((1 << DOWN) == (keysDown & (1 << DOWN)))
            {
                if(paintStringsHeight >= height - Utils.fonts[fontMessages].size.y * 3 / 2)
                {
                    textPosFP.y -= (dtms * 5);
                }
            }
            //Player scrolling up
            else if((1 << UP) == (keysDown & (1 << UP)))
            {
                if(Utils.fonts[fontMessages].size.y > Utils.FPtoInt(textPosFP.y))
                {
                    textPosFP.y += (dtms * 5);
                }
            }
            //Autoscroll
            /*else
            {
                textPosFP.y -= dtms;
            }*/
        }
    }
    
    /**
     * Method that may be overriden to draw the text buffer to the screen
     * @param graphics
     */
    protected void paintText(Graphics graphics)
    {
        //by default use the global offset
        Utils.setROffset(0, 0, true);

        int ySpace = (Utils.fonts[fontMessages].size.y * 3) / 2;
        int yPos = Utils.FPtoInt(textPosFP.y);
        int xPos = Utils.FPtoInt(textPosFP.x);
        
        //save the current clipping
        int [] clipBounds = Utils.getClipBounds(graphics);
        
        int paintStringsHeight  = Utils.FPtoInt(textPosFP.y) +
                (paintStrings.length * (Utils.fonts[fontMessages].size.y *3)/2);
        
        //will all the text fit onto one screen?
        if((paintStrings.length * (Utils.fonts[fontMessages].size.y *3)/2) > height)
        {
            //create a space for the up arrow.
            yPos += ySpace;
            //should the up arrow be drawn?
            if(Utils.fonts[fontMessages].size.y > Utils.FPtoInt(textPosFP.y))
            {
                Utils.drawString(graphics, "u".toCharArray(), 0, width / 2, 0, Graphics.TOP | Graphics.HCENTER);
            }
            
            //clip the screen leaving enough space for the arrows
            Utils.setClip(graphics, 0, ySpace, width, height - ySpace);
        }
        
        Vec2d.temp1.x = 0;
        Vec2d.temp1.y = -ySpace;
        Vec2d.temp2.x = width;
        Vec2d.temp2.y = height + ySpace;
        
        for(int i = 0; i < paintStrings.length; i++)
        {
            Vec2d.temp3.x = width / 2;
            Vec2d.temp3.y = yPos;
            //make sure the text is in the box
            if(Vec2d.pointAABTest(Vec2d.temp3, Vec2d.temp1, Vec2d.temp2))
            {
                Utils.drawString(graphics, paintStrings[i], 0,
                        xPos, yPos, textAnchor);
            }
            yPos += ySpace;
            
            if(yPos + ySpace > height)
            {
                break;
            }
        }
        
        //restore current clip
        Utils.setClip(graphics, clipBounds[0], clipBounds[1], clipBounds[2], clipBounds[3]);
        
        if(paintStringsHeight > height)
        {
            if(paintStringsHeight >= height - Utils.fonts[fontMessages].size.y * 3 / 2)
            {
                Utils.drawString(graphics, "d".toCharArray(), 0, width / 2, height, Graphics.BOTTOM | Graphics.HCENTER);
            }
        }
    }
    
    /**
     * Method that may be overriden by subclasses to paint the menu
     * @param graphics
     */
    protected void paintMenu(Graphics graphics)
    {
        Utils.drawString(graphics, paintStrings[paintSelection], fontMenu,
                Utils.FPtoInt(textPosFP.x), Utils.FPtoInt(textPosFP.y), textAnchor);
    }
    
    /**
     * Method that may be overriden by subclasses to paint the engine
     * @param graphics
     */
    protected void paintEngine(Graphics graphics)
    {
        ListNode node = vpList.head;
        while(null != node)
        {
            ((ViewPort)node.obj).paint(graphics);
            node = node.next;
        }
    }
    
    /**
     * Abstract method to be overriden by subclasses to paint the loading screen
     * @param graphics
     */
    abstract protected void paintLoadScreen(Graphics graphics);
    
    /**
     * Abstract method to be overriden by subclasses to paint the interface
     * @param graphics
     */
    abstract protected void paintInterface(Graphics graphics, int onTopState);
    
    /**
     * method to be overriden by subclasses to paint anything they want
     * @param graphics
     */
    abstract protected void paintOther(Graphics graphics);
    
    /**************************************************************************************************
     * UPDATE
     *
     **************************************************************************************************/
    //Main loop
    public void run()
    {
        try
        {Thread.sleep(100);}
        catch(InterruptedException e)
        {}
        
//#debug info
//#         try
        {
            final int maxDelay = 1000/MIN_FRAMERATE;
            final int minDelay = 1000/MAX_FRAMERATE;
            
            int frameTime = maxDelay; //init the current frame time to the min framerate
            long frameStart = System.currentTimeMillis();
            //Keep looping
            while(false == kill)
            {
                //Update our global frame count (0 is a safe value)
                frameCount = (frameCount == Integer.MAX_VALUE)?1:frameCount+1;
                
//#mdebug debug
//#                 debugTimers[0].startFrame();
//#                 debugTimers[1].startFrame();
//#enddebug                
                
                
                //Update based on state
                /*synchronized(instance)*/
                {
                    //Update text
                    if(STATE_TEXT == (paintState & STATE_TEXT))
                    {
                        updateText(frameTime);
                    }
                    
                    if(0 != ((STATE_INTERFACE_BELOW | STATE_INTERFACE_ONTOP) & updateState))
                    {
                        updateInterface(frameTime);
                    }
                    
                    if(STATE_ENGINE == (STATE_ENGINE & updateState))
                    {
                        //System.out.println("Update engine");
                        if(paused)
                        {
                            Thread.yield();
                        }
                        else
                        {
                            updateEngine(frameTime);
                        }
                    }
                    
                    //update stuff that always need to be updated.
                    updateAlways(frameTime);
                    
                    if(STATE_LOAD == (STATE_LOAD & updateState))
                    {
                        //System.out.println("Update load");
                        //Handle loading
                        if(!load())
                        {
                            kill = true;
                        }
                        Utils.gc();
                    }
                    
                    if(STATE_OTHER == (STATE_OTHER & updateState))
                    {
                        //System.out.println("Update other");
                        updateOther(frameTime);
                    }
                    
                    //Handle keypresses in the menu
                    if(STATE_MENU == (updateState & STATE_MENU))
                    {
                        updateMenu(frameTime);
                    }
                    
                    //Clear keyPresses
                    keysPressed = 0;
                    anyKeyPressed = false;
                    menuKeyPressed = false;
                    
//#mdebug debug
//#                     debugTimers[1].endFrame();
//#                     debugTimers[2].startFrame();
//#enddebug                    
                    repaint();
                }
                    
                //Block for paint to finish
                serviceRepaints();
                
                
//#mdebug debug
//#                 debugTimers[2].endFrame();
//#                 debugTimers[0].endFrame();
//#enddebug                

//#if FPS
//#                 numFrames++;
//#                 if(System.currentTimeMillis() - fpsStartTime > 1000)
//#                 {
//#                     fps = numFrames;
//#                     numFrames = 1;
//#                     fpsStartTime = System.currentTimeMillis();
//#                 }
//#endif
                
                //Get time elapsed and sleep if necessary
                long time;
                do
                {
                    time = System.currentTimeMillis();
                frameTime = (int)(time - frameStart);
                
                //clamp the frame rate, don't let the update ms be too big.
                //  the engine will start running in slow motion so that the
                //  the player movements don't end up moving too far.
                //Don't let the game run too fast, so sleep if the framerate
                //  is too high
                    if(frameTime > maxDelay)
                {
                    //clamp the frame rate
                    frameTime = maxDelay;
                }
                
                    //sleep 0 to allow other threads in this app to do their thing
                    //we must sleep even if the frame rate is too low, otherwise
                    //the jvm may not render or accept keypresses as these events
                    //may happen in other threads
                    Thread.yield();
                
                }while(frameTime < minDelay);

                //set the frame start time to the current time
                frameStart = time;
            }
        }
//#mdebug info
//#         catch(Exception e)
//#         {
//#             Utils.handleError(e, "Unhandled exception in GameEngine.run", 0);
//#         }
//#enddebug
    }
    
    //Interface update function (may be extended by subclass but will usally be called as well)
    protected abstract void updateOther(int dtms);
    
    //Interface update function (may be extended by subclass but will usally be called as well)
    protected abstract void updateInterface(int dtms);
    
    //Engine update function (may be extended by subclass but will usally be called as well)
    protected void updateEngine(int dtms)
    {
        //Keep backlight on
        Utils.forceBackLight();
        
        //Update the viewports
        {
            ListNode node = vpList.head;
            while(null != node)
            {
                ((ViewPort)node.obj).update(dtms);
                node = node.next;
            }
        }
        
        //Update objects
        updateEntities(dtms);
    }
    
    //Update game entities
    private static void updateEntities(int dtms)
    {
        /**
         * @todo Should optomize this with a sorted linklist or something..... (CRS)
         */
        
        //Test the player against everything in team powerup
        {
            ListNode nodeP = teams[GameEntity.OWNER_POWERUP].head;
            ListNode nodePnext;
            GameEntity entityP;
            while(null != nodeP)
            {
                nodePnext = nodeP.next;
                entityP = ((GameEntity)nodeP.obj);
                if(GameEngine.instance.collide(player, entityP))
                {
                    break;
                }
                nodeP = nodePnext;
            }
        }
        
        //Test everyone in team player against everyone in team enemy
        {
            ListNode nodeP = teams[GameEntity.OWNER_PLAYER].head;
            ListNode nodeE, nodePnext, nodeEnext;
            GameEntity entityP, entityE;
            while(null != nodeP)
            {
                nodePnext = nodeP.next;
                entityP = ((GameEntity)nodeP.obj);
                nodeE = teams[GameEntity.OWNER_ENEMY].head;
                while(null != nodeE)
                {
                    nodeEnext = nodeE.next;
                    entityE = ((GameEntity)nodeE.obj);
                    if(GameEngine.instance.collide(entityP, entityE))
                    {
                        break;
                    }
                    nodeE = nodeEnext;
                }
                nodeP = nodePnext;
            }
        }
        
        //Check for world collision (only if no object collisions happened)
        {
            int collResult;
            ListNode nodeCol = vpDefault.collidableInstances.head;
            while(null != nodeCol)
            {
                ListNode nodeP = teams[GameEntity.OWNER_PLAYER].head;
                ListNode nodePnext;
                GameEntity entityP;
                while(null != nodeP)
                {
                    nodePnext = nodeP.next;
                    entityP = ((GameEntity)nodeP.obj);
                    
                    //Againt the player team
                    collResult = ((TileCollisionArray)nodeCol.obj).hasCollided(entityP.pos);
                    
                    if(COLL_NONE !=  collResult)
                    {
                        int damage = entityP.collided(null);
                        if(damage > 0)
                        {
                            entityP.damage(damage);
                        }
                    }
                    nodeP = nodePnext;
                }
                nodeCol = nodeCol.next;
            }
        }
        
        {
            //Active elements
            ListNode node = activeEntities.head;
            ListNode node2;
            GameEntity entity;
//#debug debug
//#             int count = 0;
            while(null != node)
            {
//#mdebug debug
//#                 if(count++ > 100)
//#                 {
//#                     System.out.println("Entity list in an infinite loop!!!!  (GameEngine.updateEnitities())");
//#                 }
//#enddebug                
                entity = (GameEntity)node.obj;
                node2 = node.next;
                
                if(entity.isActive(vpDefault))
                {
                    entity.update(dtms);
                }
                else
                {
                    //Remove from the active list and possibly move into the inactive list
                    vpDefault.paintableList.remove(entity.paintNode);
                    activeEntities.remove(node);
                    if(entity.deactivate())
                    {
                        inactiveEntities.insertAfterTail(node);
                    }
                    else if(entityClass.isInstance(entity))
                    {
                        returnEntity(entity);
                    }
                }
                
                node = node2;
            }
        }
        
        //Inactive elements
        {
            ListNode node = inactiveEntities.head;
            ListNode node2;
            GameEntity entity;
            while(null != node)
            {
                entity = (GameEntity)node.obj;
                node2 = node.next;
                if(entity.isActive(vpDefault))
                {
                    //Remove from the inactive list and move into the active list
                    inactiveEntities.remove(node);
                    if(entity.activate())
                    {
                        if(entity.flags == entity.FLG_COLL_BOTTOM)
                        {
                            activeEntities.insertBeforeHead(node);
                        }
                        else
                        {
                            activeEntities.insertAfterTail(node);
                        }
                                
                        if(entity.flags == entity.FLG_PAINT_BOTTOM)
                        {
                            vpDefault.paintableList.insertBeforeHead(entity.paintNode);
                        }
                        else
                        {
                            vpDefault.paintableList.insertAfterTail(entity.paintNode);
                        }
                                
                        entity.update(dtms);
                    }
                    else
                    {
                        returnEntity(entity);
                    }
                }
                node = node2;
            }
        }
    }
    
    //Check for and handle collision,
    public boolean collide(GameEntity entityA, GameEntity entityB)
    
    {
        //Only test and handle if objects are not on the same owner
        if(Vec2d.AABBoxCentrePointTest(entityA.pos, entityA.dim, entityB.pos, entityB.dim))
        {
            //do the tests first
            int damageA = entityA.collided(entityB);
            int damageB = entityB.collided(entityA);
            
            //now destroy the entities
            if(damageA > 0)
            {
                entityA.damage(damageA);
            }
            if(damageB > 0)
            {
                entityB.damage(damageB);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Method to handle key pressed input. When the engine is running, keypresses are stored
     */
    protected void updateMenu(int dtms)
    {
        if((1 << UP) == (keysPressed & (1 << UP)))
        {
            if(--paintSelection < 0)
            {
                paintSelection = paintStrings.length - 1;
            }
        }
        else if((1 << DOWN) == (keysPressed & (1 << DOWN)))
        {
            if(++paintSelection >= paintStrings.length)
            {
                paintSelection = 0;
            }
        }
    }
    
    /**
     * Method to handle key pressed input. When the engine is running, keypresses are stored
     * @param keyCode
     */
    protected void keyPressed(int keyCode)
    {
        anyKeyPressed = true;
        
//#mdebug debug
//#         if(keyCode == KEY_POUND)
//#         {
//#             if((++curTimer) > NUM_TIMERS)
//#             {
//#                 curTimer = 0;
//#             }
//#             return;
//#         }
//#enddebug
//#if FPS
//#         if(keyCode == KEY_STAR)
//#         {
//#             showMem = !showMem;
//#             return;
//#         }
//#endif
//#if NOKIAUI
//#         if((keyCode == KEY_SOFTKEY1) ||
//#         (keyCode == KEY_SOFTKEY2) ||
//#         (keyCode == KEY_SOFTKEY1))
//#         {
//#             menuKeyPressed = true;
//#             super.keyPressed(keyCode);
//#             return;
//#         }
//#endif
        
        //Buffer all game keys pressed
        // must check for an exception, since some MIDP2 implementations
        // dont like non-gameAction keysCodes being passed to getGameAction
        try
        {
            int gameAction = getGameAction(keyCode);
            if(0 != gameAction)
            {
//System.out.println("GameAction(keyCode)" + gameAction + "( " + keyCode + ")");
                keysDown |= (1 << gameAction);
                keysPressed |= (1 << gameAction);
                super.keyPressed(keyCode);
                
                //let the fire button also be the menu button sometimes
                if((!gameInProgress || (updateState & STATE_MENU) == STATE_MENU)
                && (FIRE == gameAction))
                {
                    menuKeyPressed = true;
                }
                return;
            }
        }
        catch(Exception e)
        {}
        
         //Not a game action, treat as a menu press
         menuKeyPressed = true;

         super.keyPressed(keyCode);
    }
    
    /**
     * Method to handle key released input. When the engine is running, key releases
     * @param keyCode
     */
    protected void keyReleased(int keyCode)
    {
        try
        {
            int gameAction = getGameAction(keyCode);
            if(0 != gameAction)
            {
                keysDown &= ~(1 << gameAction);
            }
        }
        catch(Exception e)
        {}
        
        super.keyReleased(keyCode);
    }
    
    /*
     * Checks if a key corresponding to a certain game action was pressed this frame
     * @param gameAction The game action code to check for
     */
    public static boolean wasGameActionPressed(int gameAction)
    {
        return ((1 << gameAction) == (keysPressed & (1 << gameAction)));
    }
    
    /*
     * Checks if a key corresponding to a certain game action is currently depressed
     * @param gameAction The game action code to check for
     */
    public static boolean isGameActionDown(int gameAction)
    {
        return ((1 << gameAction) == (keysDown & (1 << gameAction)));
    }
    
    //Handle special events within the game
    /**
     * @param eventID
     * @param userData
     */
    public boolean handleGameEvent(int eventID, Object userData)
    {
        return false;
    }
    
    /**************************************************************************************************
     * INTERFACE
     *
     **************************************************************************************************/
    /**
     * Pause the game
     */
    public void pauseGame()
    {
        paused = true;
        Utils.stopAllSounds();
    }
    
    /**
     * Continue a paused game
     */
    public void continueGame()
    {
        paused = false;
    }
    
    /**
     * Add an image to the image cache
     * @param path
     * @param xSections
     * @param ySections
     */
    public static final MultiImage cacheImage(String path, int xSections, int ySections)
    {
        MultiImage image = getCachedImage(path);
        if(image == null)
        {
            //Cache miss, load the file
            try
            {
                image = new MultiImage(Utils.createImage(path), xSections, ySections, 0);
                imageCache.put(path, image);
            }
            catch(Exception e)
            {
//#debug debug
//#                 e.printStackTrace();
            }
        }
//#mdebug warn
//#         else
//#         {
//#             System.out.println("Attempting to recache and image that already exists: "+path);
//#         }
//#enddebug
        return image;
    }
    
    /**
     * Add an image to the image cache under a different filename
     */
    public static final void cacheImageAs(String path, MultiImage image)
    {
        MultiImage image2 = getCachedImage(path);
        if(image2 == null)
        {
            imageCache.put(path,image);
        }
    }
    /**
     * Add an image to the image cache under a different filename
     */
    public static final MultiImage cacheImageAs(String path, Image image)
    {
        MultiImage img = getCachedImage(path);
        if(img == null)
        {
            img = new MultiImage(image, 1, 1, 0);
            imageCache.put(path,img);
        }
        return img;
    }
    
    /**
     * Remove image from the image clip
     * @param path
     */
    public static final void unCacheImage(String path)
    {
        try
        {
            imageCache.remove(path);
        }
        catch(Exception e)
        {
//#debug debug
//#             e.printStackTrace();
        }
    }
    
    //Obtains an image from the cache (returns null if not in the cache)
    /**
     * @param path
     * @return  */
    public static final MultiImage getCachedImage(String path)
    {
        return (MultiImage) imageCache.get(path);
    }
    
    //Add a game entity to the engine
    /**
     * @param entity
     * @param topOrBottom  --- removed as it doesn't really matter if we add the
     entity to the top or bottom to the inactive list, you should rather set the 
     FLG_COLL_TOP if you want to add it to the end or beginning of the active
     entities list*/
    public static /*final*/ void addEntity(GameEntity entity)
    {
        inactiveEntities.insertAfterTail(entity.poolNode);
    }
    
    /**
     *  Release resources currently used by the engine
     */
    public void reset()
    {
        /*synchronized(instance)*/
        {
            if(null != thread)
            {
                //return all objects to the pools
                repoolObjsFromLinkList(activeEntities);
                repoolObjsFromLinkList(inactiveEntities);
                
                {
                    ListNode node;
                    node = entityPool.head;
                    while(null != node)
                    {
                        ((GameEntity)node.obj).resetEntity();
                        node = node.next;
                    }
                }
                
                //Clear entity lists
                activeEntities.clearAll();
                inactiveEntities.clearAll();
                
                //Reset viewports and clear the viewport list
                {
                    ListNode node = vpList.head;
                    while(null != node)
                    {
                        ((ViewPort) node.obj).reset(true);
                        node = node.next;
                    }
                    vpList.clearAll();
                }
                
                //Insert the default viewport
                addViewPort(vpDefault, ADD_TOP);
                
                //Clear all the images cached
                imageCache.clear();
            }
        }
        //Allow the gc to run
        Utils.gc();
    }
    
    /** addViewPort
     *
     * @param topOrBottom
     * @param vp ViewPort */
    public static void addViewPort(ViewPort vp, int topOrBottom)
    {
        vp.dim.x = playWidth;
        vp.dim.y = playHeight;
        if(topOrBottom == ADD_TOP)
        {
            vpList.insertAfterTail(vp.node);
        }
        else
        {
            vpList.insertBeforeHead(vp.node);
        }
    }
    
    /**
     * Set the current state
     * @param updateState
     * @param paintState
     */
    public static final void setState(short updateState, short paintState)
    {
        /*synchronized(instance)*/
        {
            //Check for quit
            if(STATE_QUIT == (STATE_QUIT & updateState))
            {
                instance.saveState();
                midlet.notifyDestroyed();
            }
            
            //Switch states
            prevUpdateState = GameEngine.updateState;
            GameEngine.updateState = updateState;
            prevPaintState = GameEngine.paintState;
            GameEngine.paintState = paintState;
            
            //Replace existing display pointers
            switch(updateState)
            {
                case STATE_LOAD:
                    instance.reset();
                    break;
                case STATE_ENGINE:
                    instance.continueGame();
                    break;
            }
            
            //There is a good chance something will have been freed here, invoke the collector
            Utils.gc();
        }
    }
    
    /**
     * Factory method to obtain a new GameEntity
     * @return A GameEntity instance
     */
    public final static GameEntity createEntity()
    {
        GameEntity entity = null;
        
        //Try the list
        try
        {
            if(!entityPool.isEmpty())
            {
                entity = (GameEntity) entityPool.tail.obj;
                entityPool.remove(entity.poolNode);
            }
//#mdebug warn
//#             else
//#             {
//#                 throw new IndexOutOfBoundsException("!!!!!!! Too few entities in list");
//#             }
//#enddebug
        }
        catch (Exception e)
        {
//#mdebug warn
//#             e.printStackTrace();
//#enddebug
        }
        
        return entity;
    }
    
    /**
     * Returns a game entity instance to the pool
     * @param entity The GameEntity instance to return
     */
    public final static void returnEntity(GameEntity entity)
    {
        entity.resetEntity();
        setEntityOwner(entity, GameEntity.OWNER_NONE);
        entityPool.insertAfterTail(entity.poolNode);
    }
    
    /**
     *  Sets the entity owner, moving it into the appropriate list
     *  @param entity The entity to update
     *  @param owner The entity's new owner
     */
    public final static void setEntityOwner(GameEntity entity, int owner)
    {
        if(entity.owner != GameEntity.OWNER_NONE)
        {
            teams[entity.owner].remove(entity.teamNode);
        }
        entity.owner = owner;
        if(owner != GameEntity.OWNER_NONE)
        {
            teams[entity.owner].insertAfterTail(entity.teamNode);
        }
    }
    
//#if MIDP20
//#     /*
//#      * called when the canvas size has changed
//#      */
//#     public void sizeChanged(int w, int h)
//#     {
//#             
//#     //recreate the buffer Image
//#             if(bufferImage != null)
//#             {
//#                 if(w != bufferImage.getWidth() || h != bufferImage.getHeight())
//#                 {
//#                     bufferImage = Image.createImage(getWidth(), getHeight());
//#                 }
//#             }
//#     
//#         //hack to recenter the image if the screeen size changes
//#         if(width > 0 && height > 0)
//#         {
//#             paintPos.x = paintPos.x * w / width;
//#             paintPos.y = paintPos.y * h / height;
//#         }
//# 
//#         width = w;
//#         height = h;
//#         playArea.x = w;
//#         playArea.y = h;
//#         playWidth = w;
//#         playHeight = h;
//#     }
//#endif
    /**
     * Remove all entities from a link list and return them to the object pool
     * @param list LinkList
     */
    public final static void repoolObjsFromLinkList(LinkList list)
    {
        ListNode node, nextNode;
        node = list.head;
        while(null != node)
        {
            nextNode = node.next;
            if(entityClass.isInstance(node.obj))
            {
                setEntityOwner((GameEntity)node.obj, GameEntity.OWNER_NONE);
                list.remove(node);
                entityPool.insertAfterTail(node);
            }
            node = nextNode;
        }
    }
    
    /*
     * Freezes all viewports
     */
    public static final boolean freezeViewports(boolean freeze, ViewPort excludeViewport)
    {
        boolean frozen = vpDefault.bFreeze;
        
        //Freeze all the viewports
        ListNode node = vpList.head;
        while(null != node)
        {
            if( (ViewPort)node.obj != excludeViewport)
            {
                ((ViewPort)node.obj).freeze(freeze);
            }
            node = node.next;
        }
        
        return frozen;
    }
    
    /*
     * Called to notify the canvas that it is about to be displayed
     */
    protected void showNotify()
    {
        continueGame();
    }
    
     /*
      * Called to notify the canvas it is about to be hidden
      */
    protected void hideNotify()
    {
        pauseGame();
    }
    
    /**
     * Abstract level loading method to be overridden by subclasses
     * @return
     */
    protected abstract boolean load();
    
    /*
     *  Saves the current game state and settings
     */
    public abstract void saveState();
    
    /*
     *  Loads the game state and settings
     */
    public abstract void loadState();

    /*
     * update the utils. override this if u don't want prefetching
     * this update is always called even if the game is paused.
     */
    protected void updateAlways(int frameTime)
    {
        //draw the double buffer image in the main loop!
        if(bufferImage != null)
        {
            paintWorld(bufferImage.getGraphics());
        }
        
        Utils.update(frameTime);
        Utils.updateSound(frameTime, false);
    }
}
