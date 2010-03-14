package smallfry.gameengine;

import java.io.*;

import smallfry.util.*;

/**
 *
 * <p>Title: TileArray</p>
 * <p>Description: Manages the loading and storage of tile indices</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: smallfry</p>
 * @author: Chris Tsimogiannis
 * @version 1.0
 *
 * REVISION HISTORY:
 *
 * 17 September 2004 - CT - added wrapX and wrapY flags.  Also added offset variable
 */
public class TileArray
{
    /**
     * Link List node
     */
    public final ListNode node = new ListNode(this);
    /**
     * size of the tile array in logical units
     */
    public final Vec2d size;
    /**
     * stride is the number of bytes per tile
     */
    public final int stride;
    /**
     * the byte array of indices into the multi image
     * Chars are used to avoid the unsigned byte issue
     */
    public byte tiles[];
    
    /**
     * flags
     */
    public int flags;
    
    /**
     * flag to wrap the tile array on the x axis
     */
    public final static int wrapXFlag = 0x10;
    /**
     * flag to wrap the tile array on the y axis
     */
    public final static int wrapYFlag = 0x20;
    /**
     * horizontal scrolling tile array
     */
    public final static int horzScrollFlag = 0x40;
    /**
     * vertical scrolling tile array
     */
    public final static int vertScrollFlag = 0x80;
    
    /**
     * world collision
     */
    public final static int worldCollision = 0x100;
    
//#mdebug info
    static
    {
        System.out.println("Loading TileArray class");
    }
//#enddebug
    
    /**
     * Initialize the byte array to the size given
     *
     * @param size Vec2d
     * @param stride int - number of bytes per tile index
     */
    public TileArray(Vec2d size, int stride)
    {
        this.stride = stride;
        this.size  = size;
        this.tiles = new byte[size.x*size.y*stride];
    }
    
    /**
     *
     * @param tArray TileArray
     */
    public TileArray(TileArray tileArray)
    {
        this.stride = tileArray.stride;
        this.size  = tileArray.size;
        this.tiles = tileArray.tiles;
    }
    
    /**
     * loadTaf
     *
     * @param inputStream
     * @return boolean
     */
    public TileArray(DataInputStream inputStream)
    throws IOException
    {
        int xSize, ySize;
        
        //Load Version (byte)
        byte version = (byte)inputStream.readByte();
        
        //1st load x and y size (ints)
        xSize = inputStream.readInt();
        ySize = inputStream.readInt();
        stride = inputStream.readInt();
        
        size = new Vec2d(xSize, ySize);
        //initialize any arrays
        init(xSize, ySize);
        
        for(int y = 0; y < ySize; y++)
        {
            for(int x = 0; x < xSize; x++)
            {
                readData(inputStream, x, y);
            }
        }
    }
    
    /**
     * initialize any arrays needed
     */
    protected void init(int xSize, int ySize)
    {
        tiles = new byte[size.x*size.y*stride];
    }
    
    /**
     *
     * @param inputStream DataInputStream
     * @param userData Object
     * @throws IOException
     * @return char
     */
    public void readData(DataInputStream inputStream, int x, int y)
    throws IOException
    {
        for(int i = 0; i < stride; i++)
        {
            tiles[(x+y*size.x)*stride+i] = (byte)inputStream.readByte();
        }
    }
}
