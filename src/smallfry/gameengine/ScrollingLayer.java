package smallfry.gameengine;

import javax.microedition.lcdui.*;

import smallfry.util.*;

/**
 * \
 * <p>Title: ScrollingLayer</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 *
 * REVISION HISTORY:
 *
 * 17 September 2004 - CT - Removed Graphics variable.
 */
public class ScrollingLayer
        implements Paintable
{
    /**
     * Link List node
     */
    public final ListNode nodePaint = new ListNode(this);
    /**
     * Link List node
     */
    public final ListNode nodeColl = new ListNode(this);
    /**
     * the clear colour
     */
    public final int[][] fillColourGrid;
    /**
     * Array of cached images
     */
    final Image[][] images;
    /**
     * Array of column x positions
     */
    public final int[] xPos;
    /**
     * Array of row y positions
     */
    public final int[] yPos;
    /**
     * flag for horizontal motion
     */
    public static final int horzFlag = 0x1;
    /**
     * flag for vertical motion
     */
    public static final int vertFlag = 0x2;
    /**
     * flag for clearing the background to transparent before rendering
     */
    public static final int clearFlag = 0x4;
    /**
     * Layer will never change image will continuously 'rotate' around the screen
     */
    public static final int permFlag = 0x8;
    /**
     *
     */
    public static final int wrapFlag = 0x10;
    /**
     * Paintable instances render to absolute positions
     * e.g. Say the yOffset has been set to 100, the paintable instance will
     *      be given a viewport that is at least 100.  If this flag is not set,
     *      the viewport will start at 0.
     */
    public static final int absolutePosFlag = 0x40;
    /**
     * this flag lets the scrolling layer know that transparency is not required
     */
    public static final int opaqueFlag = 0x80;
    /**
     *  garbage collect after freeing up resources - added this to try fix
     *  the siemens overdraw bug
     */
    public static final int garbageCollectFlag = 0x100;
    /**
     * size of one block
     */
    public final Vec2d blockSize = new Vec2d();
    /**
     * Number of x,y sections
     */
    final Vec2d sections;
    /**
     *  Size of buffered area
     */
    final Vec2d size;
    /**
     * offset
     */
    public final Vec2d offset;
    /**
     * flags
     */
    final int flags;
    
    /**
     * temp variable which is reused within the class, so that we don't have to alloc it every frame!
     */
    public final Vec2d pos = new Vec2d();
    public final ViewPort vpTemp = new ViewPort();
    
    public final LinkList paintableInstances = new LinkList();
    
//#mdebug info
    static
    {
        System.out.println("Loading ScrollingLayer class");
    }
//#enddebug
    
    /**
     *
     * @param size Vec2d - size of the canvas to cache
     * @param sections Vec2d - number of x,y sections
     * @param offset Vec2d - the offset of the scrolling layer
     * @param fillColourGrid intp[p[ - the colour to clear the images with initially at each grid location
     * @param flag - if this layer should cache horz or vert moving layers
     */
    public ScrollingLayer(Vec2d size, Vec2d sections, Vec2d offset, int [][] fillColourGrid, int flag)
    {
        /**
         * Size of buffered area
         */
        this.size = size;
        
        /**
         * Size of one block
         */
        int xSectionSize = size.x / sections.x;
        int ySectionSize = size.y / sections.y;
        blockSize.set(xSectionSize, ySectionSize);
        
        /**
         *     We need an extra section to work with, if we are gonna be scrolling in that direction
         */
        this.flags = flag;
        if( (flag & horzFlag) == horzFlag)
        {
            sections.x++;
        }
        if( (flag & vertFlag) == vertFlag)
        {
            sections.y++;
        }

        /**
         * we need to grow the fill colour grid if necessary
         */
        this.fillColourGrid = new int[sections.x][sections.y];
        for(int x = 0; x < this.fillColourGrid.length; x++)
        {
            for(int y = 0; y < this.fillColourGrid[x].length; y++)
            {
                this.fillColourGrid[x][y] = fillColourGrid[x % fillColourGrid.length][y % fillColourGrid[x % fillColourGrid.length].length];
            }
        }
        
        this.sections = sections;
        
        /**
         * Create image and graphics
         */
        images = new Image[sections.x][sections.y];
        /**
         * Array to specificy positions of the rows/columns
         */
        xPos = new int[sections.x+1];
        yPos = new int[sections.y+1];
        
        this.offset = offset;
        
        for(int x = 0; x < xPos.length; x++)
        {
            xPos[x] = xSectionSize * x + offset.x;
        }
        for(int y = 0; y < yPos.length; y++)
        {
            yPos[y] = ySectionSize * y + offset.y;
        }
        
        if((flag & wrapFlag) == wrapFlag)
        {
            for(int x = 0; x < sections.x; x++)
            {
                for(int y = 0; y < sections.y; y++)
                {
                    if(x == 0 && y == 0)
                    {
                        images[x][y] = Utils.createImage(xSectionSize, ySectionSize, this.fillColourGrid[x][y]);
                    }
                    else
                    {
                        images[x][y] = images[0][0];
                    }
                }
            }
        }
        else
        {
            for(int x = 0; x < sections.x; x++)
            {
                for(int y = 0; y < sections.y; y++)
                {
                    images[x][y] = Utils.createImage(xSectionSize, ySectionSize, this.fillColourGrid[x][y]);
                }
            }
        }
        
        //clean up
        if( (flags & garbageCollectFlag) == garbageCollectFlag)
        {
            Utils.gc();
        }
    }
    
    /**
     *
     */
    public final void renderAll()
    {
        for(int x = 0; x < sections.x; x++)
        {
            renderColumn(x);
        }
        
        // Once we have rendered all the images we do not have to keep the list
        if((flags & permFlag) == permFlag)
        {
            paintableInstances.clearAll();
            
            //clean up
            if( (flags & garbageCollectFlag) == garbageCollectFlag)
            {
                Utils.gc();
            }
        }
    }
    
    /**
     *
     * @param x int
     *
     * @return bool - true if something was drawn
     */
    public final boolean renderColumn(int x)
    {
        /* was anything drawn */
        boolean bDrew = false;
        
        pos.x = xPos[x];
        if((flags & absolutePosFlag) != absolutePosFlag)
        {
            pos.x -= offset.x;
        }
        
        for(int y = 0; y < sections.y; y++)
        {
            if(images[x][y] == null)
            {
                continue;
            }
            
            //check if this image needs to be cleared
            if( (flags & clearFlag) == clearFlag)
            {
                int colour = fillColourGrid[x][y];
                if( (flags & opaqueFlag) == opaqueFlag)
                {
                    //no tranpancy required
                    //do not fillrect if the fill colour is fully transparent
                    if((colour & 0xff000000) != 0x00000000)
                    {
                        Graphics g = images[x][y].getGraphics();
                        g.setColor(colour);
                        g.fillRect(0, 0, images[x][y].getWidth(),images[x][y].getHeight());
                    }
                }
                else
                {
                    images[x][y] = null;
                    //clean up
                    if( (flags & garbageCollectFlag) == garbageCollectFlag)
                    {
                        Utils.gc();
                    }
                    images[x][y] = Utils.createImage(blockSize.x, blockSize.y, colour);
                }
            }
            
            pos.y = yPos[y];
            if((flags & absolutePosFlag) != absolutePosFlag)
            {
                pos.y -= offset.y;
            }
            
            //Loop through all the Paintable objects for this layer
            ListNode node = paintableInstances.head;
            while(null != node)
            {
                vpTemp.pos.set(pos);
                vpTemp.dim.set(blockSize);
                vpTemp.posFP.set(Utils.InttoFP(pos.x), Utils.InttoFP(pos.y));
//                vpTemp.prevPos.set(pos);
                
                if(images[x][y] != null)
                {
                    bDrew |= ((Paintable)node.obj).paint(images[x][y].getGraphics(), vpTemp);
                }
                node = node.next;
            }
        }
        
        return bDrew;
    }
    
    /**
     *
     * @param y int
     *
     * @return boolean - true if something was drawn
     */
    public final boolean renderRow(int y)
    {
        boolean bDrew = false;
        
        pos.y = yPos[y];
        if((flags & absolutePosFlag) != absolutePosFlag)
        {
            pos.y -= offset.y;
        }
        
        for(int x = 0; x < sections.x; x++)
        {
            if(images[x][y] == null)
            {
                continue;
            }
            
            //check if this image needs to be cleared
            if( (flags & clearFlag) == clearFlag)
            {
                int colour = fillColourGrid[x][y];
                
                if( (flags & opaqueFlag) == opaqueFlag)
                {
                    //no tranpancy required
                    //do not fillrect if the fill colour is fully transparent
                    if((colour & 0xff000000) != 0x00000000)
                    {
                        Graphics g = images[x][y].getGraphics();
                        g.setColor(colour);
                        g.fillRect(0, 0, images[x][y].getWidth(),images[x][y].getHeight());
                    }
                }
                else
                {
                    images[x][y] = null;
                    if( (flags & garbageCollectFlag) == garbageCollectFlag)
                    {
                        Utils.gc();
                    }
                    images[x][y] = Utils.createImage(blockSize.x, blockSize.y, colour);
                }
            }
            
            pos.x = xPos[x];
            if((flags & absolutePosFlag) != absolutePosFlag)
            {
                pos.x -= offset.x;
            }
            
            //Loop through all the Paintable objects for this layer
            ListNode node = paintableInstances.head;
            while(null != node)
            {
                vpTemp.pos.set(pos);
                vpTemp.dim.set(blockSize);
                vpTemp.posFP.set(Utils.InttoFP(pos.x), Utils.InttoFP(pos.y));
//                vpTemp.prevPos.set(pos);
                
                if(images[x][y] != null)
                {
                    bDrew |= ((Paintable)node.obj).paint(images[x][y].getGraphics(), vpTemp);
                }
                node = node.next;
            }
        }
        
        return bDrew;
    }
    
    /**
     * /**
     * paint
     *
     * @param graphics Graphics
     * @param ViewPort vp
     */
    public final boolean paint(Graphics graphics, ViewPort vp)
    {
        /* did we draw anything */
        boolean bDrew = false;
        
        //TODO: Fix this ?? not sure why it isn't working anymore?
        /* Only redraw if viewport has changed */
//        if(vp.prevPos.x != vp.pos.x ||
//           vp.prevPos.y != vp.pos.y)
        {
            /**
             * Check if we need to update any columns
             */
            if( (flags & horzFlag) == horzFlag)
            {
                for(int x = 0; x < sections.x; x++)
                {
                    if( (xPos[x] + blockSize.x) < vp.pos.x)
                    {
                        xPos[x] += (blockSize.x * sections.x);
                        /* The image is only drawn once */
                        if((flags & permFlag) != permFlag)
                        {
                            bDrew |= renderColumn(x);
                        }
                    }
                    else if(xPos[x] > (vp.right()))
                    {
                        xPos[x] -= blockSize.x * sections.x;
                        /* The image is only drawn once */
                        if((flags & permFlag) != permFlag)
                        {
                            bDrew |= renderColumn(x);
                        }
                    }
                }
            }
            
            /**
             * Check if we need to update any rows
             */
            if((flags & vertFlag) == vertFlag)
            {
                for(int y = 0; y < sections.y; y++)
                {
                    if( yPos[y] + blockSize.y < vp.pos.y)
                    {
                        yPos[y] += blockSize.y * sections.y;
                        /* The image is only drawn once */
                        if((flags & permFlag) != permFlag)
                        {
                            bDrew |= renderRow(y);
                        }
                    }
                    else if(yPos[y] > vp.bottom())
                    {
                        yPos[y] -= blockSize.y * sections.y;
                        /* The image is only drawn once */
                        if((flags & permFlag) != permFlag)
                        {
                            bDrew |= renderRow(y);
                        }
                    }
                }
            }
        }
        bDrew = false;
        //Loop through all the blocks and see if they fall within the viewport
        for(int x = 0; x < sections.x; x++)
        {
            pos.x = xPos[x];
            for(int y = 0; y < sections.y; y++)
            {
                pos.y = yPos[y];
                if(images[x][y] != null)
                {
                    Utils.drawImage(graphics, images[x][y],
                            pos.x - vp.pos.x,
                            pos.y - vp.pos.y,
                            Graphics.TOP | Graphics.LEFT,
                            0);
                    bDrew = true;
                }
            }
        }
        return bDrew;
    }
    
    /**
     * clear all objects associated with this object
     */
    public void clearAll()
    {
        for(int x = 0; x < sections.x; x++)
        {
            for(int y = 0; y < sections.y; y++)
            {
                images[x][y] = null;
            }
        }
        //clean up
        if( (flags & garbageCollectFlag) == garbageCollectFlag)
        {
            Utils.gc();
        }
    }
    
    /**
     * dummy funciton to force this class to load
     */
    public static void loadClass()
    {
    }
}
