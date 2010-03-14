package smallfry.util;

/**
 *
 * <p>Title: Matt Benic</p>
 * <p>Description: A simple list node to be used in conjunction with the LinkList class</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public final class ListNode
{
    /**
     * Pointer to the next node in the list
     */
    public ListNode next;
    /**
     * Pointer to the previous item in the list
     */
    public ListNode prev;
    /**
     * Object containging this node
     */
    public final Object obj;
    
    //#mdebug info
    static
    {
        System.out.println("Loading ListNode class");
    }
    //#enddebug
    
    /**
     * Specialised constructor to set the object pointer
     * @param obj Object to be managed by this node
     */
    public ListNode(Object obj)
    {
        this.obj = obj;
    }
}
