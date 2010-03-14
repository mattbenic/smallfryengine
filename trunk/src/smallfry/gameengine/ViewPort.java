package smallfry.gameengine;

import smallfry.util.*;
import javax.microedition.lcdui.*;

/**
 * Revision History
 *
 * CT - 21/10/04 - Added freeze function
 *               - Added enabled flag
 *
 * 24 October 2004 - MB - prevPos set to 0,0 in reset to fix player bug
 */

public class ViewPort
{
    public final ListNode node = new ListNode(this);
    
    public final Vec2d pos = new Vec2d();
    public final Vec2d dim = new Vec2d();
    
    public final Vec2d prevPosFP = new Vec2d();
    public final Vec2d posFP = new Vec2d();
    public final Vec2d speedFP = new Vec2d();
    
    //Freezing will make the viewport not move, but still be drawn
    public boolean bFreeze;
    //An enabled viewport will be drawn
    public boolean bEnable;
    
    //List of Paintables
    public final LinkList paintableList = new LinkList();
    
    //List of collideables
    public final LinkList collidableInstances = new LinkList();
    
//#mdebug info
    static
    {
        System.out.println("Loading ViewPort class");
    }
//#enddebug
    
    /**
     *
     */
    public ViewPort()
    {
        bEnable = true;
    }
    
    /**
     * Reset the viewport into the same state as when it was created.
     */
    public final void reset(boolean bClear)
    {
        pos.set(0,0);
        this.posFP.x = 0;
        this.posFP.y = 0;
        prevPosFP.set(0,0);
        
        bFreeze = false;
        bEnable = true;
        
        if(bClear)
        {
            paintableList.clearAll();
            collidableInstances.clearAll();
            
            ListNode node;
            //Loop through all the paintable objects
            node = paintableList.head;
            while(null != node)
            {
                ((Paintable)node.obj).clearAll();
                node = node.next;
            }
        }
    }
    
    /**
     *
     */
    public final void update(int dtms)
    {
        prevPosFP.set(posFP);
        
        if(!bFreeze)
        {
            //Move the viewport along
            posFP.x += (speedFP.x * dtms) / 1000;
            posFP.y += (speedFP.y * dtms) / 1000;
            
            //Convert it to int
            pos.x = Utils.FPtoInt(posFP.x);
            pos.y = Utils.FPtoInt(posFP.y);
            
            //Update MapCodes
            ListNode node = collidableInstances.head;
            while(null != node)
            {
                ( (/* Collidable*/ TileCollisionArray)node.obj).update(dtms, this);
                node = node.next;
            }
        }
    }
    
    /**
     * Moves a pos the same distance the viewport moved the previous frame
     */
    public final void updatePos(Vec2d FPpos, Vec2d pos)
    {
        FPpos.x += posFP.x - prevPosFP.x;
        FPpos.y += posFP.y - prevPosFP.y;
        pos.x = Utils.FPtoInt(FPpos.x);
        pos.y = Utils.FPtoInt(FPpos.y);
    }
    
    /**
     *
     * @return int
     */
    public final int right()
    {
        return pos.x + dim.x;
    }
    
    /**
     *
     * @return int
     */
    public final int bottom()
    {
        return pos.y + dim.y;
    }
    
    /**
     *
     */
    public void paint(Graphics graphics)
    {
        if(bEnable)
        {
            ListNode node;
            //Loop through all the paintable objects
            node = paintableList.head;
            while(null != node)
            {
                ((Paintable)node.obj).paint(graphics, this);
                node = node.next;
            }
        }
    }
    
    /**
     *
     */
    public void freeze(boolean bFreeze)
    {
        this.bFreeze = bFreeze;
        prevPosFP.set(posFP);
    }
    
    /**
     *
     */
    public void enable(boolean bEnable)
    {
        this.bEnable = bEnable;
        
        ListNode node = collidableInstances.head;
        while(null != node)
        {
            ( (/* Collidable */ TileCollisionArray)node.obj).reset();
            node = node.next;
        }
    }
}
