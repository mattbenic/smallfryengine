//#condition MIDP20
//# /*
//#  * MIDP2Image.java
//#  *
//#  * Created on 12 February 2005, 06:43
//#  */
//# 
//# package smallfry.util;
//# 
//# import javax.microedition.midlet.*;
//# import javax.microedition.lcdui.*;
//# 
//# /**
//#  *
//#  * @author  Chris
//#  * @version
//#  */
//# public class MIDP2Image
//# {
//#     public int width;
//#     public int height;
//#     public int[] argb;
//#     boolean processAlpha;
//# 
//#     /**
//#      * create an immutable image
//#      */
//#     public MIDP2Image(int width, int height, int colour, boolean processAlpha)
//#     {
//#         int size = width*height;
//#         argb = new int[size];
//#         for(int i = 0; i < size; i++)
//#         {
//#             argb[i] = colour;
//#         }
//# 
//#         this.processAlpha = processAlpha;
//#         this.width = width;
//#         this.height = height;
//#     }
//# 
//#     /**
//#      * create an immutable image
//#      */
//#     public MIDP2Image(Image image)
//#     {
//#         width = image.getWidth();
//#         height = image.getHeight();
//# 
//#         int size = width*height;
//#         argb = new int[size];
//# 
//#         image.getRGB(argb, 0, width, 0, 0, width, height);
//# 
//#         this.processAlpha = processAlpha;
//#     }
//# 
//#     public void paint(Graphics graphics, int x, int y, int anchor)
//#     {
//#         // on the SE K700 for example the translated origin of the graphics
//#         // does not seem to used. Instead the real origin is used:
//#if BUG_DRAWRGBORIGIN
//#             x += graphics.getTranslateX();
//#             y += graphics.getTranslateY();
//#endif
//# 
//#         if((anchor & Graphics.RIGHT) == Graphics.RIGHT)
//#         {
//#             x -= width;
//#         }
//#         else if((anchor & Graphics.HCENTER) == Graphics.HCENTER)
//#         {
//#             x -= width / 2;
//#         }
//# 
//#         if((anchor & Graphics.BOTTOM) == Graphics.BOTTOM)
//#         {
//#             y -= height;
//#         }
//#         else if((anchor & Graphics.VCENTER) == Graphics.VCENTER)
//#         {
//#             y -= height / 2;
//#         }
//# 
//#         graphics.drawRGB(argb, 0, width, x, y, width, height, processAlpha);
//#     }
//# 
//#     /*
//#      * getWidth
//#      */
//#     public int getWidth()
//#     {
//#         return width;
//#     }
//# 
//#     /*
//#      * getWidth
//#      */
//#     public int getHeight()
//#     {
//#         return height;
//#     }
//# }
//# 