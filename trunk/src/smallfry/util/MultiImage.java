package smallfry.util;

//turn this off to allow midp20 to use normal images, that is if u r not using image flipping and rotation.
//#ifdef MIDP20
//#define MIDP20_USESPRITES
//#endif

import javax.microedition.lcdui.*;

//#if MIDP20_USESPRITES
//# import javax.microedition.lcdui.game.*;
//#endif

//#if NOKIAUI
//# import com.nokia.mid.ui.*;
//#endif 
//#ifdef MIDP20_USESPRITES
//# import javax.microedition.lcdui.game.*;
//#endif

/**
 *
 * <p>Title: MultiImage</p>
 * <p>Description: Manages the loading and splitting of images into an array of multiple smaller images</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: smallfry</p>
 * @author Chris Tsimogiannis
 *
 * @version 1.0
 *
 * Revision History:
 *
 * 16 September 2004 - CT - added replace colours functions
 *
 */
public final class MultiImage
{
    /**
     * Cut out array of mini images
     */
//#if MIDP20_USESPRITES
//#     public Sprite images[];
//#     public int [] sequence;
//#else
    public Image images[];
    public int [] sequence;
    
//#endif
    
//#if MIDP10
    //flip and rotation indices
    byte[] flipHorzIdx;
    byte[] flipVertIdx;
    byte[] flipHorzVertIdx;
//#endif
    
    /*
     * NOT SUPPORTED YET.  it uses way too much memory anyway
    char[] rot90Idx;
     
    char[] flipHorzVertRot90Idx;
    char[] flipHorzRot90Idx;
    char[] fliVertRot90Idx;
     */
    
    /**
     * average size of all the mini images
     */
    public Vec2d size = new Vec2d();
    
    /**
     * first image offset
     */
    final int offset;
    
    //#ifdef DEBUG
    static
    {
        System.out.println("Loading MultiImage class");
    }
    //#endif 
    
    /**
     * create a empty multiimage
     *
     * offset - the number of blank images to reserve
     */
    public MultiImage(int offset)
    {
        this.offset = offset;
        
//#if MIDP20_USESPRITES
//#         images = new Sprite[offset];
//#else
        images = new Image[offset];
//#endif
    }
    
    /**
     * create a single image multiimage
     */
    public MultiImage(Image parent)
    {
        this.offset = 0;
        
//#ifdef MIDP20_USESPRITES
//#         images = new Sprite[offset];
//#else
        images = new Image[offset];
//#endif 
        //append the images
        append(parent);
    }
    
    
    /**
     * create a multiimage from a parent image
     *
     * @param parent Image
     * @param xSections int
     * @param ySections int
     * @param offset int - the number indices to offset the images.  i.e. an offset of 1 will result
     * in images[0] to be null and therefore the images will be loaded starting at index 1
     */
    public MultiImage(Image parent, int xSections, int ySections, int offset)
    {
        this.offset = offset;
        
//#if MIDP20_USESPRITES
//#         images = new Sprite[offset];
//#else 
            images = new Image[offset];
//#endif 
        
        //append the images
        append(parent, xSections, ySections);
    }
    
    /**
     * Copy constructor
     *
     * Make a another multimage that points to the same image data, so that we don't waste memory
     * but has it's own sequence and other data.
     */
    public MultiImage(MultiImage img)
    {
//#if MIDP10
        this.flipHorzIdx = img.flipHorzIdx;
        this.flipVertIdx = img.flipVertIdx;
        this.flipVertIdx = img.flipHorzVertIdx;
//#endif        
        this.offset = img.offset;
        this.sequence = img.sequence;
        this.size = img.size;
        this.images = img.images;
    }
    
    
    /**
     * Breaks up the image and creates multiple smaller images.  The original parent
     * can be destroyed.  The size of the image is given in
     * sections so that the parent image can be resized without any consequences to the mini images.
     *
     * @param parent Image
     * @param xSections int
     * @param ySections int
     */
    public final void append(Image parent, int xSections, int ySections)
    {
        int xSize = parent.getWidth() / xSections;
        int ySize = parent.getHeight() / ySections;
        
        //check for single image case
        if(xSections * ySections == 1)
        {
            append(parent);
        }
        else
        {
            /* split the image up into sections */
            for(int y = 0; y < ySections; y++)
            {
                for(int x = 0; x < xSections; x++)
                {
                    /* Create the image fully transparent as default, for nokia only */
                    Image image = Utils.createImage(xSize, ySize, 0x00000000);
                    Utils.drawImage(image.getGraphics(), parent, -x * xSize, -y * ySize, Graphics.TOP | Graphics.LEFT, 0);
                    append(image);
                }
            }
        }
        parent = null;
    }
    
    /**
     * append another multiimage
     */
    public final void append(MultiImage mi)
    {
        for(int i = 0; i < mi.images.length; i++)
        {
            append(mi.images[i]);
        }
    }
    
    /**
     * append a single image
     */
    public final int append(Image img)
    {
        //calculate the average size of this multi-image
        size.x = (size.x * (images.length - offset) + img.getWidth()) / (images.length - offset + 1);
        size.y = (size.y * (images.length - offset) + img.getHeight()) / (images.length - offset + 1);
        
//#ifdef MIDP20_USESPRITES
//#       return append(new Sprite(img));
//#else 
        Image[] newImages = new Image[images.length+1];
        System.arraycopy(images, 0, newImages, 0, images.length);
        images = newImages;
        images[images.length-1] = img;
        
//#ifdef MIDP10
        if(flipHorzIdx != null)
        {
            byte[] newIdx;
            //flipHorzIdx
            newIdx = new byte[flipHorzIdx.length+1];
            System.arraycopy(flipHorzIdx, 0, newIdx, 0, flipHorzIdx.length);
            flipHorzIdx = newIdx;
            flipHorzIdx[flipHorzIdx.length-1] = (byte)(flipHorzIdx.length-1);
            
            //flipVertIdx
            newIdx = new byte[flipVertIdx.length+1];
            System.arraycopy(flipVertIdx, 0, newIdx, 0, flipVertIdx.length);
            flipVertIdx = newIdx;
            flipVertIdx[flipVertIdx.length-1] = (byte)(flipVertIdx.length-1);
            
            //flipHorzVertIdx
            newIdx = new byte[flipHorzVertIdx.length+1];
            System.arraycopy(flipHorzVertIdx, 0, newIdx, 0, flipHorzVertIdx.length);
            flipHorzVertIdx = newIdx;
            flipHorzVertIdx[flipHorzVertIdx.length-1] = (byte)(flipHorzVertIdx.length-1);
        }
//#endif
        return images.length-1;
        
        
//#endif
    }
    
    /*
     * append a sprite
     */
//#ifdef MIDP20_USESPRITES
//#     public final int append(Sprite img)
//#     {
//#         //calculate the average size of this multi-image
//#         size.x = (size.x * (images.length - offset) + img.getWidth()) / (images.length - offset + 1);
//#         size.y = (size.y * (images.length - offset) + img.getHeight()) / (images.length - offset + 1);
//# 
//#         Sprite[] newImages = new Sprite[images.length+1];
//#         System.arraycopy(images, 0, newImages, 0, images.length);
//#         images = newImages;
//#         images[images.length-1] = img;
//#         return images.length-1;
//#     }
//#endif 
    
//#ifdef MIDP10
    /*
     * setManipulation
     */
    public final void setManipulation(int idxNormal, int idxManipulated, int manipulation)
    {
        //initialize the arrays if necessary
        if(flipHorzIdx == null)
        {
            flipHorzIdx = new byte[images.length];
            flipVertIdx = new byte[images.length];
            flipHorzVertIdx = new byte[images.length];
            for(byte i = 0; i < images.length; i++)
            {
                flipHorzIdx[i] = i;
                flipVertIdx[i] = i;
                flipHorzVertIdx[i] = i;
            }
        }
        
        if(manipulation > 0)
        {
            if(manipulation == Utils.flipHorzFlag)
            {
                flipHorzIdx[idxNormal] = (byte)idxManipulated;
            }
            else if(manipulation == Utils.flipVertFlag)
            {
                flipVertIdx[idxNormal] = (byte)idxManipulated;
            }
            else if(manipulation  == (Utils.flipVertFlag | Utils.flipHorzFlag))
            {
                flipHorzVertIdx[idxNormal] = (byte)idxManipulated;
            }
/*
 * NOT SUPPORT YET
            else if(manipulation == Utils.rot90Flag)
            {
            }
            else if(manipulation  == (Utils.flipVertFlag | Utils.rot90Flag))
            {
            }
            else if(manipulation  == (Utils.flipHorzFlag | Utils.rot90Flag))
            {
            }
            else if(manipulation  == (Utils.flipHorzFlag | Utils.flipVertFlag | Utils.rot90Flag))
            {
            }
 */
        }
    }
//#endif 
    
    /*
     * setFrameSequence
     */
    public final void setFrameSequence(int[] sequence)
    {
        this.sequence = sequence;
    }
    
    /**
     * Retrieves the image at offset i
     *
     * @param i int
     * @return Image
     */
    //#if MIDP20_USESPRITES
//#     public final Sprite getImage(int i)
    //#else 
    public final Image getImage(int i)
    //#endif 
    {
        return images[i + offset];
    }
    
    /** Paints the specified frame
     *
     * @param graphics Graphics object to paint to
     * @param x X position to paint to
     * @param y Y position to paint to
     * @param frame Frame to paint
     * @param anchor Alignment anchor
     * @param manipulation
     */
    public final void paint(Graphics graphics, int x, int y, int frame, int anchor, int manipulation)
    {
        //recalculate the frame based on the sequence
        if(sequence != null)
        {
            frame = sequence[frame % sequence.length];
        }
        
//midp10 doesn't support manipulation, so we must use another image if we can
//#if MIDP10
        if(flipHorzIdx != null)
        {
            if(manipulation > 0)
            {
                if(manipulation == Utils.flipHorzFlag)
                {
                    frame = (int)flipHorzIdx[frame];
                }
                else if(manipulation == Utils.flipVertFlag)
                {
                    frame = (int)flipVertIdx[frame];
                }
                else if(manipulation  == (Utils.flipVertFlag | Utils.flipHorzFlag))
                {
                    frame = (int)flipHorzVertIdx[frame];
                }
/*
 * NOT SUPPORT YET
                else if(manipulation == Utils.rot90Flag)
                {
                }
                else if(manipulation  == (Utils.flipVertFlag | Utils.rot90Flag))
                {
                }
                else if(manipulation  == (Utils.flipHorzFlag | Utils.rot90Flag))
                {
                }
                else if(manipulation  == (Utils.flipHorzFlag | Utils.flipVertFlag | Utils.rot90Flag))
                {
                }
 */
            }
        }
        //#endif
        
//#mdebug warn
//#         if(frame >= images.length)
//#         {
//#             System.out.println("ARRAY INDEX OUT OF BOUNDS: frame=" + frame + " images.length:" + images.length);
//#         }
//#enddebug
        
        if(null != images[frame])
        {
            Utils.drawImage(graphics, images[frame], x, y, anchor, manipulation);
        }
    }
    
    /**
     * replaces all colours in the from array, with the colours in the to array,
     * in the multi-image
     *
     * @param fromColours - the array of colours to replace
     * @param toColours - the array of colours to replace with
     *
     */
    public final void replaceColour(short[] fromColours, short[] toColours)
    {
//#if NOKIAUI && MIDP10
//#         Image image;
//#         int curIndex;
//#         for(int i = 0; i < images.length; i++) 
//#         {
//#             //get the index into the image array
//#             images[i] = Utils.replaceColoursInImage(images[i], fromColours, toColours);
//#         }
//#endif
    }
    
    
    /**
     * marks images as used so that they aren't deleted by the
     * deleteunused() function
     *
     * @param used - the array of indices of the used images
     * @returns boolean[] - an array of
     */
    public final boolean[] markUsed(byte used[], boolean[] usedImages)
    {
        if(usedImages == null)
            usedImages = new boolean[images.length];
        
        for(int i = 0; i < used.length; i++)
        {
//#mdebug warn
//#             if((used[i] & 0x00ff) >= usedImages.length)
//#             {
//#                 System.out.println("markUsed: ARRAY INDEX OUT OF BOUNDS: (used[i] & 0x00ff)=" + (used[i] & 0x00ff) + " usedImages.length:" + usedImages.length + " - images.length: " + images.length);
//#             }
//#enddebug
            usedImages[used[i] & 0x00ff] = true;
        }
        return usedImages;
    }
    
    /**
     * deletes all unused images
     */
    public final void deleteUnused(boolean[] usedImages)
    {
//#mdebug debug
//#         int removed = 0;
//#enddebug
        for(int i = 0; i < images.length; i++)
        {
            if(usedImages[i] == false)
            {
//#mdebug debug
//#                 removed++;
//#enddebug
                images[i] = null;
            }
        }
//#mdebug debug
//#         System.out.println("Num images removed: " + removed);
//#enddebug
        usedImages = null;
    }
    
    /**
     * deletes all unused images
     */
    public final void deleteUsed(byte used[][])
    {
//#mdebug debug
//#         int removed = 0;
//#enddebug
        for(int i = 0; i < used.length; i++)
        {
            for(int j = 0; j < used[i].length; j++)
            {
//#mdebug debug
//#                 removed++;
//#enddebug
                images[used[i][j] & 0x00ff] = null;
            }
        }
        
//#mdebug debug
//#         System.out.println("Num images removed: " + removed);
//#enddebug
    }
    
    /**
     * clear all data
     */
    public void clearAll()
    {
        if(images != null)
        {
            for(int i = 0; i < images.length; i++)
            {
                images[i] = null;
            }
        }
        images = null;
    }
    
    /*
     * Obtain the image offset
     */
    public int getOffset()
    {
        return offset;
    }
}
