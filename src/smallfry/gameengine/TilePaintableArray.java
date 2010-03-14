package smallfry.gameengine;

import javax.microedition.lcdui.*;

import smallfry.util.*;
import java.io.*;

/**
 *
 * <p>Title: TileArray</p>
 * <p>Description: Manages the rendering of tiles to the viewport</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: smallfry</p>
 * @author: Chris Tsimogiannis
 * @version 1.0
 *
 * REVISION HISTORY:
 *
 * 17 September 2004 - CT - added wrapX and wrapY flags.  Also added offset variable
 */
public class TilePaintableArray
        extends TileArray
        implements Paintable
{
    /**
     * the tile anchor
     */
    public final static int anchor = Graphics.TOP | Graphics.LEFT;
    
    /**
     * the tiles.  Must not be greater than 255 tiles in the multiimage
     */
    public final MultiImage images;
    
    /**
     * offset
     */
    public final Vec2d offset = new Vec2d();
    
    /**
     * rotations
     */
    public byte[] rotations;
    
//#mdebug info
    static
    {
        System.out.println("Loading TilePaintableArray class");
    }
//#enddebug
    
    /**
     *
     * @param inputStream DataInputStream
     * @param multiImage MultiImage
     */
    public TilePaintableArray(DataInputStream inputStream, MultiImage multiImage)
    throws IOException
    {
        super(inputStream);
        this.images = multiImage;
    }
    
    /**
     * int xSize - x size of the map data
     * int ySize - y size of the map data
     * initialize any arrays needed
     */
    protected final void init(int xSize, int ySize)
    {
        super.init(xSize, ySize);
        this.rotations = new byte[xSize*ySize];
    }
    
    /**
     * Paint the tile array, based on the viewport position and the vieport dimensions
     *
     * @param graphics Graphics
     * @param ViewPort vp
     */
    public final boolean paint(Graphics graphics, ViewPort vp)
    {
        /* Lets figure out which tile we should render */
        int xStart = (vp.pos.x - offset.x) / images.size.x - 1;
        int yStart = (vp.pos.y - offset.y) / images.size.y - 1;
        int xEnd = ( (vp.pos.x - offset.x + vp.dim.x) / images.size.x) + 1;
        int yEnd = ( (vp.pos.y - offset.y + vp.dim.y) / images.size.y) + 1;
        
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
        int xImageSize = images.size.x;
        int yImageSize = images.size.y;
        int xPos, yPos;
        int iRot;
        int iIndex;
        int xx, yy;
        
        /* was something drawn */
        boolean bDrew = false;
        
        /* Draw the tiles */
        xPos = xStart * xImageSize;
        for(int x = xStart; x < xEnd; x++)
        {
            xx = x;
            if((flags & wrapXFlag) == wrapXFlag)
            {
                while(xx < 0) xx += size.x;
                while(xx >= size.x) xx -= size.x;
            }
            
            yPos = yStart * yImageSize;
            for(int y = yStart; y < yEnd; y++)
            {
                yy = y;
                if((flags & wrapYFlag) == wrapYFlag)
                {
                    while(yy < 0) yy += size.y;
                    while(yy >= size.y) yy -= size.y;
                }
                
                for(int i = 0; i < stride; i++)
                {
                    /**
                     * Get the tile index, zero Indices are considered to be empty tiles
                     */
                    iIndex = (int)tiles[(xx+yy*size.x)*stride+i];
                    if(iIndex > 0)
                    {
                        iRot = (int)((rotations[xx+yy*size.x] >> i*2) & (Utils.flipHorzFlag | Utils.flipVertFlag));
//#mdebug warn
                        if(images.images[iIndex] == null)
                        {
                            System.out.println("Attempting to draw Null Image -- Index: "+iIndex);
                        }
//#enddebug
                        images.paint(graphics,
                                xPos - vp.pos.x + offset.x,
                                yPos - vp.pos.y + offset.y,
                                iIndex,
                                anchor,
                                iRot);
                        bDrew = true;
                    }
                }
                yPos += yImageSize;
            }
            /* Pre calculate */
            xPos += xImageSize;
        }
        
        return bDrew;
    }
    
    /**
     * clear all objects associated with this object
     */
    public void clearAll()
    {
        if(images != null)
        {
            images.clearAll();
        }
    }
    
    /**
     *
     * @param inputStream DataInputStream
     * @param int x - the x pos of the map data
     * @param int y - the y pos of the map data
     * @throws IOException
     */
    public void readData(DataInputStream inputStream, int x, int y)
    throws IOException
    {
        super.readData(inputStream, x, y);
        rotations[x+y*size.x] = (byte)inputStream.readByte();
    }
    
    /**
     * dummy funciton to force this class to load
     */
    public static void loadClass()
    {
    }
}
