package smallfry.gameengine;

import javax.microedition.lcdui.*;
import smallfry.util.*;

public interface Paintable
{
    /**
     *
     * @param graphics DirectGraphics
     * @param viewportPos Vec2d
     * @param viewportDim Vec2d
     *
     * @return bool - true if something was drawn
     */
    public boolean paint(Graphics graphics, ViewPort vp);
    
    public void clearAll();
}
