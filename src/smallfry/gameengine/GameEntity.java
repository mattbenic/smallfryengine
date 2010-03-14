package smallfry.gameengine;

import javax.microedition.lcdui.*;
import smallfry.util.*;


/** The top level entity class
 *
 * REVISION HISTORY:
 *
 * 28 September 2004 - MB - Added player pos code.
 * 16 October 2004 - MB - Fixed ArrayIndexOutOfBoundsException bug
 * 28 October 2004 - MB - moved damage method here
 * 31 October 2004 - CT - updated collision
 */
public class GameEntity
        implements Paintable
{
    /* base gameentity specific flags */
    public static final int FLG_PAINT_BOTTOM = 0x01;     //added to the paintable list on top, or bottom
    public static final int FLG_COLL_BOTTOM = 0x02;     //added to the entity collision list on top or bottom
    public static final int FLG_RESERVED_3 = 0x04;
    public static final int FLG_RESERVED_4 = 0x08;
    public static final int FLG_RESERVED_5 = 0x10;
    public static final int FLG_RESERVED_6 = 0x20;
    public static final int FLG_RESERVED_7 = 0x40;
    public static final int FLG_RESERVED_8 = 0x80;

    /* game gameentity specific flags start here*/
    public static final int FLG_USER_START = 0x1000;
    
    public int flags;
    
    public static final int OWNER_NONE      = -1;
    public static final int OWNER_PLAYER    =  0;
    public static final int OWNER_POWERUP   =  1;
    public static final int OWNER_ENEMY     =  2;
    public static final int OWNER_MAX       =  3;
    
    public final Vec2d dim = new Vec2d();
    public final Vec2d posFP = new Vec2d();
    public final Vec2d pos = new Vec2d();
    public final Vec2d velFP = new Vec2d();
    
    /* Entity Collision */
    public final Vec2d center = new Vec2d();
    public int radius;
    
    public boolean alive;
    public int hitPoints;
    public int owner = OWNER_NONE;
    public int manipulation; //rendering manipulations
    
    //animation
    //the current frame
    public int frame;
    //the current animimation millisecond
    public int animMS;
    //the time between frames
    public int animInterval;
    
    public MultiImage image;
    
    public final ListNode poolNode = new ListNode(this);
    public final ListNode paintNode = new ListNode(this);
    public final ListNode teamNode = new ListNode(this);
    
//#mdebug info
//#     static
//#     {
//#         System.out.println("Loading GameEntity class");
//#     }
//#enddebug
    
    //Init function, may be overriden for subclasses
    public boolean init(String imagePath, int animInterval)
    {
//#mdebug warn
//#         try
//#         {
//#enddebug
            //Try to get the cachedimage
            return init(GameEngine.getCachedImage(imagePath), animInterval);
//#mdebug warn
//#         }
//#         catch(Exception e)
//#         {
//#             System.out.println("Missing entity image: "+imagePath);
//#             e.printStackTrace();
//#             return false;
//#         }
//#enddebug
    }
    
    //Init function, may be overriden for subclasses
    public boolean init(MultiImage image, int animInterval)
    {
        this.image = image;

//#mdebug error
//#         if(null == image) throw new ArrayIndexOutOfBoundsException("Missing entity image");
//#enddebug

        if(null != image)
        {
            dim.set(image.size);
            velFP.set(0, 0);
            alive = true;
            frame = 0;
            this.animInterval = animInterval;
            animMS = animInterval;
            hitPoints = 1;
            manipulation = 0;
            return true;
        }

        return false;
    }
    
    //update method to be overriden by subclasses
    public void update(int dtms)
    {
        posFP.x += (velFP.x * dtms) / 1000;
        posFP.y += (velFP.y * dtms) / 1000;
        
        pos.x = Utils.FPtoInt(posFP.x);
        pos.y = Utils.FPtoInt(posFP.y);
        
        //update the animation
        if(animInterval > 0)
        {
            animMS -= dtms;
            if(animMS < 0)
            {
                frame++;
                int numframes = image.sequence == null ? image.images.length : image.sequence.length;
                if(frame >= numframes)
                {
                    frame = 0;
                }
                animMS = animInterval;
            }
        }
    }
    
    //set the current position
    public final void setPos(int x, int y)
    {
        posFP.x = Utils.InttoFP(x);
        posFP.y = Utils.InttoFP(y);
        pos.x = x;
        pos.y = y;
    }
    
    //Render function to be overriden by subclasses
    public boolean paint(Graphics graphics, ViewPort vp)
    {
        image.paint(graphics, pos.x - vp.pos.x, pos.y - vp.pos.y, frame, Graphics.HCENTER | Graphics.VCENTER, manipulation);
        return true;
    }
    
    //Called when being moved out of the inactive list,
    //  if true is returned the object will be added to the active list, otherwise it will be destroyed
    public boolean activate()
    {
        return true;
    }
    
    //Called when being moved out of the active list,
    //  if true is returned the object will be added to the inactive list, otherwise it will be destroyed
    public boolean deactivate()
    {
        return false;
    }
    
    //Returns true if the entity is active, false otherwise
    public boolean isActive(ViewPort vp)
    {
        if(alive)
        {
            return Vec2d.AABBoxTest(pos, dim, vp.pos, vp.dim);
        }
        else
        {
            return false;
        }
    }
    
    /*
     * By default, when collisions occur, the entity will will be killed
     *
     * @return the amount of damage to cause this entity
     */
    public int collided(GameEntity collidedWith)
    {
        if( (null == collidedWith) || ( (OWNER_NONE != owner) && (owner != collidedWith.owner)))
        {
            return 1;
        }
        
        return 0;
    }
    
    /**
     * Handle damage
     */
    public void damage(int dmgAmount)
    {
        hitPoints -= dmgAmount;
        if(hitPoints <= 0)
        {
            alive = false;
        }
    }
    
    /**
     * reset this enitity
     */
    public void resetEntity()
    {
        image = null;
        flags = 0;
    }
    
    /*
     * Implement paintable method
     */
    public void clearAll()
    {
    }
    
    /*
     * Utility method to 'hover' the game object within an area
     */
    public void hover(Vec2d topLeftFP, Vec2d botRightFP)
    {
        // Limit x
        if(posFP.x < topLeftFP.x)
        {
            posFP.x = topLeftFP.x;
            velFP.x = -velFP.x;
        }
        else if(posFP.x > botRightFP.x)
        {
            posFP.x = botRightFP.x;
            velFP.x = -velFP.x;
        }
        
        // Limit x
        if(posFP.y < topLeftFP.y)
        {
            posFP.y = topLeftFP.y;
            velFP.y = -velFP.y;
        }
        else if(posFP.y > botRightFP.y)
        {
            posFP.y = botRightFP.y;
            velFP.y = -velFP.y;
        }
    }
}
