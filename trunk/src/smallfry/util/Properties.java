/*
 * Properties.java
 *
 * Created on 29 August 2006, 09:20
 *
 * Partial implementation of J2SE java.util.Properties.
 */

package smallfry.util;

import java.util.*;
import java.io.*;

/**
 * This class is a partial implementation of the Properties class included in J2SE.
 * For simplicity sake the recursive default system is not implemented.
 *
 * @author Matt
 */
public class Properties extends Hashtable
{
    /** Creates a new instance of Properties */
    public Properties()
    {
    }
    
    /*
     *  Searches for the property with the specified key in this property list.
     */
    public String getProperty(String key)
    {
        Object obj = get(key);
        return (null == obj)?null:obj.toString();
    }
    
    public String getProperty(String key, String defaultValue)
    {
        Object obj = get(key);
        if(null == obj)
        {
            put(key, defaultValue);
            return defaultValue;
        }
        return obj.toString();
    }
    
    public void load(InputStream inStream) throws IOException
    {
        InputStreamReader in = null;
        try
        {
            in = new InputStreamReader(inStream);
            StringBuffer keyBuf = new StringBuffer();
            StringBuffer valBuf = new StringBuffer();
            StringBuffer curBuf;
            boolean readingKey = true;
            boolean lineStarted = false;
            boolean specialChar = false;
            char ch = (char) in.read();
            
            //Keep going while we have data
            while(-1 != ch)
            {
                //Comments
                if(!lineStarted && ch == '#')
                {
                    do
                    {
                        ch = (char) in.read();
                    } while(-1 != ch && 10 != ch && 13 != ch);
                    continue;
                }
                curBuf = (readingKey)?keyBuf:valBuf;
                
                //Special characters
                if(specialChar)
                {
                    switch(ch)
                    {
                        case 10:
                        case 13:
                            do
                            {
                                ch = (char) in.read();
                            } while(-1 != ch && 10 != ch && 13 != ch);
                            continue;
                        case 't':
                            curBuf.append('\t');
                            break;
                        case 'n':
                            curBuf.append('\n');
                            break;
                        case 'r':
                            curBuf.append('\r');
                            break;
                        case '\\':
                        case '\"':
                        case '\'':
                            curBuf.append(ch);
                            break;
                        case ':':
                        case '=':
                            if(readingKey)
                            {
                                curBuf.append(ch);
                                break;
                            }
                        default:
                            curBuf.append('\\');
                            curBuf.append(ch);
                    }
                    specialChar = false;
                    ch = (char) in.read();
                    continue;
                }
                else if('\\' == ch)
                {
                    ch = (char) in.read();
                    specialChar = true;
                    continue;
                }
                
                switch(ch)
                {
                    //Newline indicates an empty value
                    case 10:
                    case 13:
                    case '\uffff':  //HACK: '\uffff' not picked up correctly
                        if(0 != keyBuf.length()) put(keyBuf.toString(), valBuf.toString());
                        keyBuf.delete(0, keyBuf.length());
                        valBuf.delete(0, valBuf.length());
                        readingKey = true;
                        lineStarted = false;
                        //HACK: '\uffff' not picked up correctly
                        if(ch == '\uffff') return;
                        break;
                        //Ignore leading whitespace, in key use any trailing whitespace as assignment
                    case ' ':
                    case '\t':
                    case ':':
                    case '=':
                        if(0 != curBuf.length())
                        {
                            if(!readingKey)
                            {
                                curBuf.append(ch);
                            }
                            else
                            {
                                readingKey = false;
                            }
                        }
                        break;
                    default:
                        curBuf.append(ch);
                        lineStarted = true;
                }
                
                ch = (char) in.read();
            }
        }
        finally
        {
            if(null != in) in.close();
        }
    }
    
    public Object setProperty(String key, String value)
    {
        return put(key, value);
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        Enumeration keys = keys();
        String key, value;
        while(keys.hasMoreElements())
        {
            key = (String) keys.nextElement();
            value = (String) get(key);
            buff.append(key);          
            buff.append(": ");          
            buff.append(value);          
            buff.append('\n');          
        }
        
        return buff.toString();
    }
}
