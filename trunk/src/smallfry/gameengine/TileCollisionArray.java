package smallfry.gameengine;

import smallfry.util.*;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * <p>Title: TileCollisionArray</p>
 * <p>Description: Manages the collision of tiles</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: smallfry</p>
 * @author: Chris Tsimogiannis
 * @version 1.0
 *
 * REVISION HISTORY:
 *
 * 17 September 2004 - CT - added wrapX and wrapY flags.  Also added offset variable
 * 23 October 2004 - CT - added reset function
 */
public class TileCollisionArray
        extends TileArray
        /* implements Collidable */
{
    /**
     * the size of one collision block
     */
    public final Vec2d tileSize;
    
    /**
     * the collision array's offset
     */
    public final Vec2d offset;
    
    //Flags
    public static final int FLAG_TOP = (0x01);
    public static final int FLAG_LEFT = (0x02);
    public static final int FLAG_RIGHT = (0x04);
    public static final int FLAG_BOTTOM = (0x08);
    //Following are not flags
    public static final int FLAG_DIAGDR = (0x80);
    public static final int FLAG_DIAGDL = (0x81);

    //#mdebug info
    static
    {
        System.out.println("Loading CollisionArray class");
    }
    //#enddebug
    
    /**
     *  Default Constuctor
     */
    public TileCollisionArray(TileArray tileArray, Vec2d tileSize, Vec2d offset)
    {
        super(tileArray);
        this.offset = offset;
        this.tileSize = tileSize;
    }
    
    /**
     *
     * @param intpuStream DataInputStream
     * @param tileSize Vec2d
     * @param offset Vec2d
     * @throws IOException
     */
    public TileCollisionArray(DataInputStream inputStream, Vec2d tileSize, Vec2d offset)
    throws IOException
    {
        super(inputStream);
        this.offset = offset;
        this.tileSize = tileSize;
    }
    
    /**
     *
     */
    public void reset()
    {
    }
    
    /**
     * Point tile collision - We are currently not supporting point movement,
     * so therefore we cannot do point line collision.  This function only checks
     * for point and complete collidable tiles collision.
     *
     * @param pt Vec2d - pos of point
     * @return int collision result
     */
    public int hasCollided(Vec2d pt)
    {
        if((flags & worldCollision) != worldCollision)
        {
            return GameEngine.COLL_NONE;
        }
        
        /* Lets figure out which tile we should collide with */
        int xTile = (pt.x - offset.x) / tileSize.x;
        int yTile = (pt.y - offset.y) / tileSize.y;
        
        if((flags & wrapXFlag) != wrapXFlag)
        {
            if(xTile < 0 || xTile >= size.x)
            {
                return GameEngine.COLL_NONE;
            }
        }
        else
        {
            while(xTile < 0)	   xTile += size.x;
            while(xTile >= size.x) xTile -= size.x;
        }
        
        if((flags & wrapYFlag) != wrapYFlag)
        {
            if(yTile < 0 || yTile >= size.y)
            {
                return GameEngine.COLL_NONE;
            }
        }
        else
        {
            while(yTile < 0)	   yTile += size.y;
            while(yTile >= size.y) yTile -= size.y;
        }
        
        // get the index at that position
        int iIndex = tiles[xTile+yTile*size.x] & 0xFF;
        
        
        /**
         * Zero Indices are considered to be empty tiles
         */
        if(iIndex > 0)
        {
            //Diagonals are considered to be fully collidable
            if((iIndex & FLAG_DIAGDL) == FLAG_DIAGDL)
            {
                return GameEngine.COLL_BELW;
            }
            if((iIndex & FLAG_DIAGDR) == FLAG_DIAGDR)
            {
                return GameEngine.COLL_ABVE;
            }
            
            //Points cannot collide with lines!!!  unless we store the previous
            //position
            /** We are not supporting this type of collision currently
             *
             * if( (iIndex & FLAG_TOP) == FLAG_TOP)
             * {
             * if(Vec2d.AABHorzLineTest(topLeft, dim, yCur, xCur, xCur + xTileSize))
             * {
             * return GameEngine.COLL_ABVE;
             * }
             * }
             * if( (iIndex & FLAG_LEFT) == FLAG_LEFT)
             * {
             * if(Vec2d.AABVertLineTest(topLeft, dim, xCur, yCur, yCur + yTileSize))
             * {
             * return GameEngine.COLL_LEFT;
             * }
             * }
             * if( (iIndex & FLAG_RIGHT) == FLAG_RIGHT)
             * {
             * if(Vec2d.AABVertLineTest(topLeft, dim, xCur + xTileSize, yCur, yCur + yTileSize))
             * {
             * return GameEngine.COLL_RGHT;
             * }
             * }
             * if( (iIndex & FLAG_BOTTOM) == FLAG_BOTTOM)
             * {
             * if(Vec2d.AABHorzLineTest(topLeft, dim, yCur + yTileSize, xCur, xCur + xTileSize))
             * {
             * return GameEngine.COLL_BELW;
             * }
             * }
             */
        }
        
        return GameEngine.COLL_NONE;
    }
    
    
    /**
     *
     *
     *
     * @param c Vec2d - center of circle
     * @param r int - radius of circle
     * @return int collision result
     */
    public int hasCollided(Vec2d c, int r)
    {
        /* Lets figure out which tile we should collide with */
        int xStart = (c.x - offset.x - r/2) / tileSize.x;
        int yStart = (c.y - offset.y - r/2) / tileSize.y;
        int xEnd   = ((c.x - offset.x + r/2) / tileSize.x) + 1;
        int yEnd   = ((c.y - offset.y + r/2) / tileSize.y) + 1;
        
        /* Clamp */
        if((flags & wrapXFlag) != wrapXFlag)
        {
            if(xStart < 0)
            {
                xStart = 0;
            }
            if(xEnd >= size.x)
            {
                xEnd = size.x;
            }
        }
        if((flags & wrapYFlag) != wrapYFlag)
        {
            if(yStart < 0)
            {
                yStart = 0;
            }
            if(yEnd >= size.y)
            {
                yEnd = size.y;
            }
        }
        
        /* Cache values */
        int xTileSize = tileSize.x;
        int yTileSize = tileSize.y;
        int xCur, yCur;
        int iIndex;
        int xx, yy;
        
        /* Check for collision with the tiles */
        xCur = xStart * xTileSize + offset.x;
        for(int x = xStart; x < xEnd; x++)
        {
            xx = x;
            if((flags & wrapXFlag) == wrapXFlag)
            {
                while(xx < 0) xx += size.x;
                while(xx >= size.x) xx -= size.x;
            }
            
            yCur = yStart * yTileSize + offset.y;
            for(int y = yStart; y < yEnd; y++)
            {
                yy = y;
                if((flags & wrapYFlag) == wrapYFlag)
                {
                    while(yy < 0) yy += size.y;
                    while(yy >= size.y) yy -= size.y;
                }
                
                // get the index at that position
                iIndex = tiles[xx+yy*size.x] & 0xFF;
                /**
                 * Zero Indices are considered to be empty tiles
                 */
                if(iIndex > 0)
                {
                    //Diagonals are considered to be fully collidable
                    if((iIndex & FLAG_DIAGDL) == FLAG_DIAGDL)
                    {
                        return GameEngine.COLL_BELW;
                    }
                    if((iIndex & FLAG_DIAGDR) == FLAG_DIAGDR)
                    {
                        return GameEngine.COLL_ABVE;
                    }
                    
                    /** We are not supporting this type of collision currently
                     *
                     * if( (iIndex & FLAG_TOP) == FLAG_TOP)
                     * {
                     * if(Vec2d.circleHorzLineTest(c, r, yCur, xCur, xCur + xTileSize))
                     * {
                     * return GameEngine.COLL_ABVE;
                     * }
                     * }
                     * if( (iIndex & FLAG_LEFT) == FLAG_LEFT)
                     * {
                     * if(Vec2d.circleVertLineTest(c, r, xCur, yCur, yCur + yTileSize))
                     * {
                     * return GameEngine.COLL_LEFT;
                     * }
                     * }
                     * if( (iIndex & FLAG_RIGHT) == FLAG_RIGHT)
                     * {
                     * if(Vec2d.circleVertLineTest(c, r, xCur + xTileSize, yCur, yCur + yTileSize))
                     * {
                     * return GameEngine.COLL_RGHT;
                     * }
                     * }
                     * if( (iIndex & FLAG_BOTTOM) == FLAG_BOTTOM)
                     * {
                     * if(Vec2d.circleHorzLineTest(c, r, yCur + yTileSize, xCur, xCur + xTileSize))
                     * {
                     * return GameEngine.COLL_BELW;
                     * }
                     * }
                     */
                    return GameEngine.COLL_NONE;
                }
                yCur += yTileSize;
            }
            /* Pre calculate */
            xCur += xTileSize;
        }
        
        return GameEngine.COLL_NONE;
    }
    
    /**
     *
     * @param topLeft Vec2d - top left pos of AAB
     * @param dim dim - size of AAB
     * @return int collision result
     */
    public int hasCollided(Vec2d topLeft, Vec2d dim)
    {
        /**
         * TODO: include stride
         */
        
        /* Lets figure out which tile we should collide with */
        int xStart = (topLeft.x - offset.x) / tileSize.x;
        int yStart = (topLeft.y - offset.y) / tileSize.y;
        int xEnd = ( (topLeft.x - offset.x + dim.x) / tileSize.x) + 1;
        int yEnd = ( (topLeft.y - offset.y + dim.y) / tileSize.y) + 1;
        
        /* Clamp */
        if((flags & wrapXFlag) != wrapXFlag)
        {
            if(xStart < 0)
            {
                xStart = 0;
            }
            if(xEnd >= size.x)
            {
                xEnd = size.x;
            }
        }
        if((flags & wrapYFlag) != wrapYFlag)
        {
            if(yStart < 0)
            {
                yStart = 0;
            }
            if(yEnd >= size.y)
            {
                yEnd = size.y;
            }
        }
        
        /* Cache values */
        int xTileSize = tileSize.x;
        int yTileSize = tileSize.y;
        int xCur, yCur;
        int iIndex;
        int xx, yy;
        
        /* Check for collision with the tiles */
        xCur = xStart * xTileSize + offset.x;
        for(int x = xStart; x < xEnd; x++)
        {
            xx = x;
            if((flags & wrapXFlag) == wrapXFlag)
            {
                while(xx < 0) xx += size.x;
                while(xx >= size.x) xx -= size.x;
            }
            
            yCur = yStart * yTileSize + offset.y;
            for(int y = yStart; y < yEnd; y++)
            {
                yy = y;
                if((flags & wrapYFlag) == wrapYFlag)
                {
                    while(yy < 0) yy += size.y;
                    while(yy >= size.y) yy -= size.y;
                }
                
                // get the index at that position
                iIndex = tiles[xx+yy*size.x*stride];
                /**
                 * Zero Indices are considered to be empty tiles
                 */
                if(iIndex > 0)
                {
                    //Diagonals are considered to be fully collidable
                    if((iIndex & FLAG_DIAGDL) == FLAG_DIAGDL)
                    {
                        return GameEngine.COLL_BELW;
                    }
                    if((iIndex & FLAG_DIAGDR) == FLAG_DIAGDR)
                    {
                        return GameEngine.COLL_ABVE;
                    }
                    
                    /** We are not supporting this type of collision currently
                     *
                     * if( (iIndex & FLAG_TOP) == FLAG_TOP)
                     * {
                     * if(Vec2d.AABHorzLineTest(topLeft, dim, yCur, xCur, xCur + xTileSize))
                     * {
                     * return GameEngine.COLL_ABVE;
                     * }
                     * }
                     * if( (iIndex & FLAG_LEFT) == FLAG_LEFT)
                     * {
                     * if(Vec2d.AABVertLineTest(topLeft, dim, xCur, yCur, yCur + yTileSize))
                     * {
                     * return GameEngine.COLL_LEFT;
                     * }
                     * }
                     * if( (iIndex & FLAG_RIGHT) == FLAG_RIGHT)
                     * {
                     * if(Vec2d.AABVertLineTest(topLeft, dim, xCur + xTileSize, yCur, yCur + yTileSize))
                     * {
                     * return GameEngine.COLL_RGHT;
                     * }
                     * }
                     * if( (iIndex & FLAG_BOTTOM) == FLAG_BOTTOM)
                     * {
                     * if(Vec2d.AABHorzLineTest(topLeft, dim, yCur + yTileSize, xCur, xCur + xTileSize))
                     * {
                     * return GameEngine.COLL_BELW;
                     * }
                     * }
                     */
                    return GameEngine.COLL_NONE;
                }
                yCur += yTileSize;
            }
            /* Pre calculate */
            xCur += xTileSize;
        }
        
        return GameEngine.COLL_NONE;
    }
    
    /**
     *
     * @param vp ViewPort
     */
    public void update(int dtms, ViewPort vp)
    {
        int xStart, xEnd, yStart, yEnd;
        if(vertScrollFlag == (vertScrollFlag & flags))
        {
            xStart  = 0;
            xEnd    = size.x;
            yStart  = (vp.pos.y - offset.y) / tileSize.y - 1;
            yEnd    = ((vp.pos.y - offset.y + vp.dim.y) / tileSize.y) + 1 /* + SPAWN_BUFFER */;
            
            //check if the collision array has left the screen
            if(yStart == 0)
            {
                GameEngine.instance.handleGameEvent(GameEngine.EVENT_ENDOFCOLLISIONARRAY, vp);
            }
        }
        else // if(horzScrollFlag == (horzScrollFlag & flags))
        {
            xStart  = (vp.pos.x - offset.x) / tileSize.x - 1;                                  // in tiles (not pixels)
            xEnd    = ((vp.pos.x - offset.x + vp.dim.x) / tileSize.x) + 1 /* + SPAWN_BUFFER */;  // in tiles (not pixels)
            yStart  = 0;
            yEnd    = size.y;
            
            //check if the collision array has left the screen
            if(xStart == size.x)
            {
                GameEngine.instance.handleGameEvent(GameEngine.EVENT_ENDOFCOLLISIONARRAY, vp);
            }
        }
        
        // Clamp
        if((flags & wrapXFlag) != wrapXFlag)
        {
            if(xStart < 0)
            {
                xStart = 0;
            }
            if(xEnd >= size.x)
            {
                xEnd = size.x;
            }
        }
        if((flags & wrapYFlag) != wrapYFlag)
        {
            if(yStart < 0)
            {
                yStart = 0;
            }
            if(yEnd >= size.y)
            {
                yEnd = size.y;
            }
        }
        
        int iIndex;
        int xx, yy;
        
        //loop through all the tiles on the screen
        for(int x = xStart; x < xEnd; x++)
        {
            xx = x;
            //check for horizontal wrapping
            if((flags & wrapXFlag) == wrapXFlag)
            {
                while(xx < 0) xx += size.x;
                while(xx >= size.x) xx -= size.x;
            }
            
            for(int y = yStart; y < yEnd; y++)
            {
                yy = y;
                //check for vertical wrapping
                if((flags & wrapYFlag) == wrapYFlag)
                {
                    while(yy < 0) yy += size.y;
                    while(yy >= size.y) yy -= size.y;
                }
                
                //loop through all the mapcodes for this tile
                for(int i = 0; i < stride; i++)
                {
                    iIndex = (int)tiles[(xx+yy*size.x)*stride+i];
                    if(iIndex > 0)
                    {
                        //calculate the position of the mapcode
                        Vec2d.temp1.set(x * tileSize.x + tileSize.x / 2 + offset.x, y * tileSize.y + tileSize.y / 2 + offset.y);
                        
                        //let the engine handle this mapcode
                        if(GameEngine.instance.handleGameEvent(iIndex, Vec2d.temp1))
                        {
                            //change the tile index so that it doesn't trigger again.
                            tiles[(xx+yy*size.x)*stride+i] = (byte)(-iIndex);
                        }
                    }
                }
            }
        }
    }
    
    public static void loadClass()
    {
    }
}
