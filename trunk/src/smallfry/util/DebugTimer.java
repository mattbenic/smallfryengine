//#mdebug debug
package smallfry.util;

import javax.microedition.lcdui.*;

/*
  Debug timing utility.
 */
public class DebugTimer
{
  public int numFrames = 0;
  public long frameTime = 0;
  public long totalTime = 0;
  public long aveTime = 0;
  public String label;

  public DebugTimer(String label)
  {
    this.label = label;
  }

  public DebugTimer()
  {
    this.label = "";
  }

  public void startFrame()
  {
    frameTime = System.currentTimeMillis();
  }

  public String toString() {
      return label + " " + Long.toString(aveTime);
  }

  public void endFrame()
  {
    numFrames++;
    frameTime = System.currentTimeMillis() - frameTime;
      totalTime += frameTime;
    if(totalTime >= 1000)
    {
      aveTime = totalTime / numFrames;
      numFrames = 0;
      totalTime = 0;
    }
  }

  public void paint(Graphics graphics, int x, int y, int anchor)
  {
    Utils.drawString(graphics, toString().toCharArray(), 0,
                        x, y - Utils.fonts[0].size.y, anchor);
  }
}
//#enddebug