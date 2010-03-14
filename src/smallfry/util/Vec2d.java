package smallfry.util;

/*
 2d vector class
 
 */
public final class Vec2d
{
    /**
     * nasty
     */
    public static final Vec2d temp1 = new Vec2d();
    public static final Vec2d temp2 = new Vec2d();
    public static final Vec2d temp3 = new Vec2d();
    
    public int x;
    public int y;
    
    //#mdebug info
    static
    {
        System.out.println("Loading Vec2d class");
    }
    //#enddebug
    
    public Vec2d()
    {
    }
    
    public Vec2d(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Vec2d(Vec2d vec)
    {
        x = vec.x;
        y = vec.y;
    }
    
    //Set the values of this vector
    public final void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    //Set the values of this vector
    public final void set(Vec2d vec2)
    {
        x = vec2.x;
        y = vec2.y;
    }
    
    //Add another vector into this vector
    public final void add(Vec2d vec)
    {
        x += vec.x;
        y += vec.y;
    }
    
    //Subtract another vector from this vector
    public final void subtract(Vec2d vec)
    {
        x -= vec.x;
        y -= vec.y;
    }
    
    //Multiply this vector by another vector
    public final void multiply(Vec2d vec)
    {
        x *= vec.x;
        y *= vec.y;
    }
    
    //Divide this vector by another vector
    public final void divide(Vec2d vec)
    {
        x /= vec.x;
        y /= vec.y;
    }
    
    //Scale this vector by a scalar
    public final void scale(int scalar)
    {
        x *= scalar;
        y *= scalar;
    }
    
    //Divide this vector by a scalar
    public final void invScale(int scalar)
    {
        x /= scalar;
        y /= scalar;
    }
    
    //String representation of the object
    //Adds vec1 and vec2 and puts the result into res
    public final static Vec2d add(Vec2d res, Vec2d vec1, Vec2d vec2)
    {
        res.x = vec1.x + vec2.x;
        res.y = vec1.y + vec2.y;
        return res;
    }
    
    //Return a new vector that is the sum of vec1 & vec2
    public final static Vec2d add(Vec2d vec1, Vec2d vec2)
    {
        return new Vec2d(vec1.x + vec2.x, vec1.y + vec2.y);
    }
    
    //Return a new vector that is the difference of vec1 & vec2
    public final static Vec2d subtract(Vec2d vec1, Vec2d vec2)
    {
        return new Vec2d(vec1.x - vec2.x, vec1.y - vec2.y);
    }
    
    //Return a new vector that is the product of vec1 & vec2
    public final static Vec2d multiply(Vec2d vec1, Vec2d vec2)
    {
        return new Vec2d(vec1.x * vec2.x, vec1.y * vec2.y);
    }
    
    //Return a new vector that is the vec1 / vec2
    public final static Vec2d divide(Vec2d vec1, Vec2d vec2)
    {
        return new Vec2d(vec1.x / vec2.x, vec1.y / vec2.y);
    }
    
    //Return a new vector that is vec scaled by scalar
    public final static Vec2d scale(Vec2d vec, int scalar)
    {
        return new Vec2d( (int) (vec.x * scalar), (int) (vec.y * scalar));
    }
    
    //Return a new vector that vec divided by scalar
    public final static Vec2d invScale(Vec2d vec, int scalar)
    {
        return new Vec2d( (int) (vec.x / scalar), (int) (vec.y / scalar));
    }
    
    /**
     *
     * @param v1 Vec2d
     * @param v2 Vec2d
     * @return int
     */
    public final static int dotProduct(Vec2d v1, Vec2d v2)
    {
        return v1.x * v2.x + v1.y * v2.y;
    }
    
    /**
     *
     * @param v1 Vec2d
     * @param v2 Vec2d
     * @return int
     */
    public final static int dotProductFP(Vec2d v1FP, Vec2d v2FP)
    {
        return Utils.FPtoInt(v1FP.x * v2FP.x + v1FP.y * v2FP.y);
    }
    
    /**
     *
     *
     */
    public final void normalizeFP()
    {
        final int ax = x >= 0 ? x : -x;
        final int ay = y >= 0 ? y : -y;
        final int max = ax > ay ? ax : ay;
        final int min = ax < ay ? ax : ay;
        final int vFPmag = Utils.FPaSqrt(Utils.FPmultiply(x, x) + Utils.FPmultiply(y, y), max + (min/2));
        if(vFPmag != 0)
        {
            x = Utils.FPdivide(x, vFPmag);
            y = Utils.FPdivide(y, vFPmag);
        }
        else
        {
            x = 0;
            y = Utils.FP_1_0;
        }
    }
    
    /**
     * Collision test functions
     */
    
    /**
     *
     * @param sphCntFP Vec2d
     * @param sphRdsFP int
     * @param lineOrgFP Vec2d
     * @param lineDirFP Vec2d
     * @param lineLengthFP int
     * @return boolean
     */
/*
    public final static boolean circleLineTest(Vec2d sphCntFP,
                                              int sphRdsFP,
                                              Vec2d lineOrgFP,
                                              Vec2d lineDirFP,
                                              int lineLengthFP)
    {
      //calculate the parametric t at which point the ray is closest to the sphere
      int tFP = Vec2d.dotProductFP(sphCntFP, lineDirFP);
      tFP -= Vec2d.dotProductFP(lineOrgFP, lineDirFP);
 
      // now clamp T to the line's valid range
      if(tFP < 0)
      {
        tFP = 0;
      }
      else if(tFP > lineLengthFP)
      {
        tFP = lineLengthFP;
      }
 
      // calculate the vector to the closest point to the sphere
      int ptxFP = lineOrgFP.x + Utils.multiplyFP(lineDirFP.x, tFP) - sphCntFP.x;
      int ptyFP = lineOrgFP.y + Utils.multiplyFP(lineDirFP.y, tFP) - sphCntFP.y;
      ptxFP = Utils.multiplyFP(ptxFP, ptxFP);
      ptyFP = Utils.multiplyFP(ptyFP, ptyFP);
 
      // check the distance
      if( (ptxFP + ptyFP) < Utils.multiplyFP(sphRdsFP, sphRdsFP))
      {
        return true;
      }
 
      return false;
    }
 */
    
    /**
     *
     * @param pos1 Vec2d
     * @param dim1 Vec2d
     * @param pos2 Vec2d
     * @param dim2 Vec2d
     * @return boolean
     */
    public final static boolean AABBoxTest(Vec2d pos1, Vec2d dim1, Vec2d pos2, Vec2d dim2)
    {
        /* Find collision by elimination */
        if(pos1.x > pos2.x + dim2.x)
        {
            return false;
        }
        if(pos2.x > pos1.x + dim1.x)
        {
            return false;
        }
        if(pos1.y > pos2.y + dim2.y)
        {
            return false;
        }
        if(pos2.y > pos1.y + dim1.y)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     *
     * @param pos1 Vec2d
     * @param dim1 Vec2d
     * @param pos2 Vec2d
     * @param dim2 Vec2d
     * @return boolean
     */
    public final static boolean AABBoxCentrePointTest(Vec2d pos1, Vec2d dim1, Vec2d pos2, Vec2d dim2)
    {
        int d11 = dim1.x/2;
        int d12 = dim1.y/2;
        int d21 = dim2.x/2;
        int d22 = dim2.y/2;
        
        // Find collision by elimination
        if(pos1.x - d11 > pos2.x + d21)
        {
            return false;
        }
        if(pos2.x - d21 > pos1.x + d11)
        {
            return false;
        }
        if(pos1.y - d12 > pos2.y + d22)
        {
            return false;
        }
        if(pos2.y - d22 > pos1.y + d12)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     *
     * @param pt
     * @param pos
     * @param dim
     */
    public final static boolean pointAABTest(Vec2d pt, Vec2d pos, Vec2d dim)
    {
        // Find collision by elimination
        if(pt.x < pos.x)
        {
            return false;
        }
        if(pt.y < pos.y)
        {
            return false;
        }
        if(pt.y > pos.y + dim.y)
        {
            return false;
        }
        if(pt.x > pos.x + dim.x)
        {
            return false;
        }
        return true;
    }
    
    /**
     *
     * @param pos Vec2d
     * @param dim Vec2d
     * @param yLine int
     * @param x1 int
     * @param x2 int
     * @return boolean
     */
    public final static boolean AABHorzLineTest(Vec2d pos, Vec2d dim, int yLine, int x1, int x2)
    {
        if(pos.y > yLine)
        {
            return false;
        }
        if(pos.x > x2)
        {
            return false;
        }
        if(pos.x + dim.x < x1)
        {
            return false;
        }
        if(pos.y + dim.y < yLine)
        {
            return false;
        }
        
        return true;
    }
    /**
     *
     * @param pos Vec2d
     * @param dim Vec2d
     * @param xLine int
     * @param y1 int
     * @param y2 int
     * @return boolean
     */
    public final static boolean AABVertLineTest(Vec2d pos, Vec2d dim, int xLine, int y1, int y2)
    {
        if(pos.x > xLine)
        {
            return false;
        }
        if(pos.y > y2)
        {
            return false;
        }
        if(pos.y + dim.y < y1)
        {
            return false;
        }
        if(pos.x + dim.x < xLine)
        {
            return false;
        }
        
        return true;
    }
    /**
     * @param c1
     * @param r1
     * @param c2
     * @param r2
     */
    public final static boolean sphereSphereTest(Vec2d c1, int r1, Vec2d c2, int r2)
    {
        int xdiff = (c2.x - c1.x);
        int ydiff = (c2.y - c1.y);
        return ((xdiff*xdiff) + (ydiff*ydiff)) < (r1*r1 + r2*r2);
    }
    
    /**
     * @param c1
     * @param rSQ1
     * @param c2
     * @param rSQ2
     */
    public final static boolean sphereSphereTestSQ(Vec2d c1, int rSQ1, Vec2d c2, int rSQ2)
    {
        int xdiff = (c2.x - c1.x);
        int ydiff = (c2.y - c1.y);
        return ((xdiff*xdiff) + (ydiff*ydiff)) < (rSQ1 + rSQ2);
    }
    
    /**
     *
     */
    public final static boolean pointSphereTest(Vec2d p1, Vec2d c2, int r2)
    {
        int xdiff = (c2.x - p1.x);
        int ydiff = (c2.y - p1.y);
        return ((xdiff*xdiff) + (ydiff*ydiff)) < (r2 * r2);
    }
    
    /**
     *
     */
    public final static boolean pointSphereTestSQ(Vec2d p1, Vec2d c2, int rSQ2)
    {
        int xdiff = (c2.x - p1.x);
        int ydiff = (c2.y - p1.y);
        return ((xdiff*xdiff) + (ydiff*ydiff)) < (rSQ2);
    }
}
