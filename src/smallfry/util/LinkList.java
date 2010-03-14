package smallfry.util;

/**
 * A simple linklist implementation
 * To use the list, create ListNode objects parameterised with the
 * objects to be managed by the list.
 *
 *@edit 2006/07/11 Reformatted for NetBeans 5.0/antenna preprocessor directives
 */
public final class LinkList
{
    /**
     * Pointer to the head of the list
     */
    public ListNode head;
    /**
     * Pointer to the tail of the list
     */
    public ListNode tail;
    /**
     * Current length of the list
     */
    public int length;
    
    //#mdebug info
//#     static
//#     {
//#         System.out.println("Loading LinkList class");
//#     }
    //#enddebug
    
    /**
     * Default constructor
     */
    public LinkList()
    {
    }
    
    /**
     * clear the list and force node pointers to null
     */
    public final void clearAll()
    {
        ListNode node, nextNode;
        
        node = head;
        while(null != node)
        {
            nextNode = node.next;
            node.next = null;
            node.prev = null;
            
            node = nextNode;
        }
        
        head = null;
        tail = null;
        
        length = 0;
    }
    
    /**
     * clear
     */
    public final void clear()
    {
        head = null;
        tail = null;
        
        length = 0;
    }
    
    /**
     * isEmpty
     *
     * @return boolean
     */
    public final boolean isEmpty()
    {
        return(head == null) ? true : false;
    }
    
    /**
     * insertBeforeHead
     *
     * @param node ListNode
     */
    public final void insertBeforeHead(ListNode node)
    {
//#mdebug error
//#         if(isNodeInList(node))
//#         {
//#             try
//#             {
//#                 throw new Exception("Inserting node that is already in this list!");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
        
        
        node.next = head;
        node.prev = null;
        if(null != head)
        {
            head.prev = node;
        }
        else
        {
            tail = node;
        }
        head = node;
        length++;
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#             
//#         }
//#enddebug
    }
    
    /**
     * insertAfterTail
     *
     * @param node ListNode
     */
    public final void insertAfterTail(ListNode node)
    {
//#mdebug error
//#         if(isNodeInList(node))
//#         {
//#             try
//#             {
//#                 throw new Exception("Inserting node that is already in this list!");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
        
        node.prev = tail;
        node.next = null;
        if(null != tail)
        {
            tail.next = node;
        }
        else
        {
            head = node;
        }
        tail = node;
        length++;
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#             
//#         }
//#enddebug
    }
    
    /**
     * insertBeforeNode
     *
     * @param insertNode ListNode
     * @param beforeNode ListNode
     */
    public final void insertBeforeNode(ListNode insertNode, ListNode beforeNode)
    {
//#mdebug error
//#         if(isNodeInList(insertNode))
//#         {
//#             try
//#             {
//#                 throw new Exception("Inserting node that is already in this list!");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
        
        insertNode.next = beforeNode;
        insertNode.prev = beforeNode.prev;
        beforeNode.prev = insertNode;
        if(head == beforeNode)
        {
            head = insertNode;
        }
        length++;
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#             
//#         }
//#enddebug
        
    }
    
    /**
     * insertAfterNode
     *
     * @param insertNode ListNode
     * @param afterNode ListNode
     */
    public final void insertAfterNode(ListNode insertNode, ListNode afterNode)
    {
//#mdebug error
//#         if(isNodeInList(insertNode))
//#         {
//#             try
//#             {
//#                 throw new Exception("Inserting node that is already in this list!");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
        
        insertNode.prev = afterNode;
        insertNode.next = afterNode.next;
        afterNode.next = insertNode;
        if(tail == afterNode)
        {
            tail = insertNode;
        }
        length++;
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#             
//#         }
//#enddebug
    }
    
    /**
     * remove
     *
     * @param node ListNode
     */
    public final void remove(ListNode node)
    {
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#         
//#         if(!isNodeInList(node))
//#         {
//#             try
//#             {
//#                 throw new Exception("Trying to remove node that is not in the list.");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
        
        ListNode next = node.next;
        ListNode prev = node.prev;
        if(null == next)   // If node is the tail
        {
            tail = prev;
        }
        else
        {
            next.prev = prev;
        }
        
        if(null == prev)   // If the node is the head
        {
            head = next;
        }
        else
        {
            prev.next = next;
        }
        
        length--;
        
//#mdebug error
//#         if(tail == null && head != null ||
//#                 head == null && tail != null)
//#         {
//#             try
//#             {
//#                 throw new Exception("Inconsistent link list state");
//#             }
//#             catch(Exception e)
//#             {
//#                 e.printStackTrace();
//#             }
//#         }
//#enddebug
    }
    
    /**
     * Checks to see if the node is already in this list
     * @param checkNode The Node to check for
     * @return true if the node exists in the list, false otherwise
     */
    public final boolean isNodeInList(ListNode checkNode)
    {
        ListNode node;
        
        node = head;
        while(null != node)
        {
            if(checkNode == node)
            {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    /**
     * Merge the mergeList into our own
     *
     */
    public void merge(LinkList mergeList)
    {
        //if we are empty then just become the mergeList
        if(isEmpty())
        {
            head = mergeList.head;
        }
        else
        {
            tail.next = mergeList.head;
        }
        
        tail = mergeList.tail;
        length += mergeList.length;
        mergeList.clear();
    }
        
    
}
