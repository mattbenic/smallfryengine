//#condition BLUETOOTH
//--// Copyright 2004 Nokia Corporation.
//--//
//--// THIS SOURCE CODE IS PROVIDED 'AS IS', WITH NO WARRANTIES WHATSOEVER,
//--// EXPRESS OR IMPLIED, INCLUDING ANY WARRANTY OF MERCHANTABILITY, FITNESS
//--// FOR ANY PARTICULAR PURPOSE, OR ARISING FROM A COURSE OF DEALING, USAGE
//--// OR TRADE PRACTICE, RELATING TO THE SOURCE CODE OR ANY WARRANTY OTHERWISE
//--// ARISING OUT OF ANY PROPOSAL, SPECIFICATION, OR SAMPLE AND WITH NO
//--// OBLIGATION OF NOKIA TO PROVIDE THE LICENSEE WITH ANY MAINTENANCE OR
//--// SUPPORT. FURTHERMORE, NOKIA MAKES NO WARRANTY THAT EXERCISE OF THE
//--// RIGHTS GRANTED HEREUNDER DOES NOT INFRINGE OR MAY NOT CAUSE INFRINGEMENT
//--// OF ANY PATENT OR OTHER INTELLECTUAL PROPERTY RIGHTS OWNED OR CONTROLLED
//--// BY THIRD PARTIES
//--//
//--// Furthermore, information provided in this source code is preliminary,
//--// and may be changed substantially prior to final release. Nokia Corporation
//--// retains the right to make changes to this source code at
//--// any time, without notice. This source code is provided for informational
//--// purposes only.
//--//
//--// Nokia and Nokia Connecting People are registered trademarks of Nokia
//--// Corporation.
//--// Java and all Java-based marks are trademarks or registered trademarks of
//--// Sun Microsystems, Inc.
//--// Other product and company names mentioned herein may be trademarks or
//--// trade names of their respective owners.
//--//
//--// A non-exclusive, non-transferable, worldwide, limited license is hereby
//--// granted to the Licensee to download, print, reproduce and modify the
//--// source code. The licensee has the right to market, sell, distribute and
//--// make available the source code in original or modified form only when
//--// incorporated into the programs developed by the Licensee. No other
//--// license, express or implied, by estoppel or otherwise, to any other
//--// intellectual property rights is granted herein.
//--
//--
//--
//--//==============================================================================
//--// Package Statements
//--package smallfry.util;
//--
//--
//--
//--//==============================================================================
//--// Import Statements
//--
//--import javax.microedition.lcdui.*;
//--import javax.microedition.io.*;
//--import java.io.*;
//--import java.util.*;
//--import javax.bluetooth.*;
//--
//--
//--//==============================================================================
//--// CLASS (OR INTERFACE) DECLARATIONS
//--
//--/** The <code>BluetoothConnection</code> encapsulates the Stream Input and
//-- * Output connections.
//-- * A BluetoothConnection is returned by
//-- * {@link BluetoothDiscovery#searchService BluetoothDiscovery.searchService} or
//-- * {@link BluetoothDiscovery#waitOnConnection BluetoothDiscovery.waitOnConnection}.
//-- */
//--
//--public class BluetoothConnection
//--{
//--    //==============================================================================
//--    // Final variables (Class constants)
//--
//--    //==============================================================================
//--    // Class (static) variables
//--
//--    //==============================================================================
//--    // Instance variables
//--    private StreamConnection streamConnection;
//--    /** This is the InputStream associated with the Bluetooth Connection.
//--     */
//--    public InputStream inputStream;
//--    /** This is the OutputStream associated with the Bluetooth Connection.
//--     */
//--    public OutputStream outputStream;
//--    /** This is the name of the local device associated with the Bluetooth Connection.
//--     */
//--    public String localName;     // Name of local device for this connection
//--    /** This is the name of the remote device associated with the Bluetooth Connection.
//--     */
//--    public String remoteName;    // Name of remote device for this connection
//--    public String url; // urlStrings used for connecting, for server this is empty.
//--
//--    //==============================================================================
//--    // Constructors and miscellaneous (finalize method, initialization block,...)
//--
//--    /** Creates an <code>BluetoothConnection</code> object.
//--     * An Input- and an OutputStream is opened.
//--     * This constructor is used for mobile terminated connections (local device = server).
//--     * The connection object already exists and is passed as parameter.
//--     * @param con The StreamConnection object.
//--     * @param ln Name of local device for the connection. This is typically a friendly
//--     * name for the device.
//--     * @param rn Name of remote device for the connection. This is typically the service name or eg. the
//--     * friendly name of the remote device.
//--     * @param notif The notifier that has been used to create the StreamConenction object con. Is only used in Close(): the notifier is closed as well.
//--     */
//--    public BluetoothConnection(StreamConnection con, String ln, String rn)
//--        throws IOException
//--    {
//--        // Store name
//--        localName = ln;
//--        remoteName = rn;
//--
//--        // Init url to zero
//--        url = "";
//--
//--        // Store stream connection
//--        streamConnection = con;
//--
//--        // Open Input and Output Streams
//--        openStreams();
//--    }
//--
//--    /** Creates an <code>BluetoothConnection</code> object.
//--     * An Input- and an OutputStream is opened.
//--     * This constructor is used for mobile originated connections (local device = client).
//--     * A url is given as parameter and the constructor creates a connection.
//--     * @param urlStrings If local device = client then this is the url it connected to.
//--     * If local device = server then it is empty.
//--     * @param ln Name of local device for the connection. This is typically a friendly
//--     * name for the device.
//--     * @param rn Name of remote device for the connection. This is typically the service name or eg. the
//--     * friendly name of the remote device.
//--     * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
//--     */
//--    public BluetoothConnection(String urlStrings, String ln, String rn)
//--        throws IOException
//--    {
//--        // Store name
//--        localName = ln;
//--        remoteName = rn;
//--
//--        // Store the url
//--        url = urlStrings;
//--
//--        // Connect to url
//--        connect();
//--    }   /*  End of the constructor method   */
//--
//--    /**
//--     * Connects to url. Can only be used if url String is available, ie.
//--     * 'BluetoothConnection' was created with constructor
//--     * 'BluetoothConnection( String urlStrings, String ln, String rn )'.
//--     * This method is public. So, in case the link was disconnected (or link
//--     * was lost) this method can be used also to reconnect. Normally this method
//--     * is already called from the constructor.
//--     * @throws IOException Signals that an I/O exception of some sort has
//--     * occurred. This class is the general class of exceptions produced by
//--     * failed or interrupted I/O operations.
//--     */
//--    private void connect()
//--        throws IOException
//--    {
//--        // Create stream connection
//--        streamConnection = (StreamConnection) Connector.open( url );
//--
//--        // Open Input and Output Streams
//--        openStreams();
//--    }
//--
//--    /**
//--     * Opens the InputStream and the OutputStream for the
//--     * StreamConnection object (streamConnection)
//--     */
//--    private void openStreams()
//--        throws IOException
//--    {
//--        inputStream = streamConnection.openInputStream();
//--        outputStream = streamConnection.openOutputStream();
//--    }
//--
//--    /**
//--     * Closes all connections.
//--     */
//--    synchronized public void close()
//--    {
//--        try
//--        {
//--            outputStream.close();
//--        }
//--        catch(IOException e)
//--        {
//--            // There is not much we can do; we tried to close it.
//--        }
//--
//--        try
//--        {
//--            inputStream.close();
//--        }
//--        catch(IOException e)
//--        {
//--            // There is not much we can do; we tried to close it.
//--        }
//--
//--        try
//--        {
//--            if(streamConnection != null)
//--            {
//--                streamConnection.close();
//--                streamConnection = null;
//--            }
//--        }
//--        catch( IOException e )
//--        {
//--            // There is not much we can do; we tried to close it.
//--        }
//--    }
//--
//--
//--
//--
//--
//--
//--
//--
//--
//--    /** Sends a string to remote device.
//--     * It first sends the length (int), followed by the characters.
//--     * String is not null-terminated.
//--     * String is sent directly to remote device (flushed).
//--     * Strings have to be limited to 255 chars max.
//--     * Is used by {@link BluetoothDiscovery BluetoothDiscovery}.
//--     * @param s String to be send to remote device.
//--     */
//--    public void writeString(String s)
//--        throws IOException
//--    {
//--        // Convert to byte array
//--        byte[] bytes = s.getBytes();
//--        // Length
//--        outputStream.write(bytes.length); // Writes only the low-order byte of length
//--        // String
//--        outputStream.write(bytes);
//--        // Flush
//--        outputStream.flush();
//--    }
//--
//--    /** Reads a string from remote device.
//--     * It first reads the length (int), followed by the characters.
//--     * String is not null-terminated.
//--     * Strings have to be limited to 255 chars max.
//--     * Is used by {@link BluetoothDiscovery BluetoothDiscovery}.
//--     * @return String that is read from the remote device.
//--     */
//--    public String readString()
//--        throws IOException
//--    {
//--        // Length
//--        int length = inputStream.read();
//--        byte[] bytes = new byte[length];
//--        read(bytes, length);
//--
//--        return new String(bytes);
//--    }
//--
//--    /**
//--     * Writes an integer
//--     */
//--    public void writeInt( int v )
//--    throws IOException
//--    {
//--        outputStream.write( (v & 0xFF) );
//--        outputStream.write( ((v>>8) & 0xFF) );
//--        outputStream.write( ((v>>16) & 0xFF) );
//--        outputStream.write( ((v>>24) & 0xFF) );
//--    }
//--
//--    /**
//--     * Reads an integer
//--     */
//--    public int readInt()
//--    throws IOException
//--    {
//--        int res;
//--
//--        res = inputStream.read();
//--        res += inputStream.read()<<8;
//--        res += inputStream.read()<<16;
//--        res += inputStream.read()<<24;
//--        return res;
//--    }
//--
//--    /** Returns true if connection has been closed.
//--     * @return true if connection is closed, false if connection is open.
//--     */
//--    public boolean isClosed()
//--    {
//--        if( streamConnection == null )
//--        {
//--            // is closed
//--            return true;
//--        }
//--        else
//--        {
//--            // is still open
//--            return false;
//--        }
//--    }
//--
//--    /** Reads bytes into the array. Blocks until 'len' bytes are read.
//--     * @param arr Array that should get the data.
//--     * @param len Number of bytes to read. Method doesn't return until all
//--     * bytes are read.
//--     */
//--    public void read( byte[] arr, int len )
//--    throws IOException
//--    {
//--        int offs, count;
//--
//--        offs = 0;
//--        while( len > 0 )
//--        {
//--            // If still not all bytes read
//--            count = inputStream.read( arr, offs, len );
//--            len -= count;
//--            offs += count;
//--        }
//--    }
//--
//--    /**
//--     * writes a byte array to the outputstream
//--     */
//--    public void writeBytes(byte[] bytes)
//--    throws IOException
//--    {
//--        //length
//--        writeInt(bytes.length);
//--        // bytes
//--        outputStream.write(bytes);
//--        // Flush
//--        outputStream.flush();
//--
//--    }
//--
//--    /**
//--     * read in a byte array from the inputstream
//--     */
//--    public byte[] readBytes()
//--    throws IOException
//--    {
//--        int offs, count;
//--
//--        int len = readInt();
//--        if(len == 0)
//--        {
//--            return null;
//--        }
//--
//--        byte[] bytes = new byte[len];
//--
//--        offs = 0;
//--        while( len > 0 )
//--        {
//--            // If still not all bytes read
//--            count = inputStream.read( bytes, offs, len );
//--            len -= count;
//--            offs += count;
//--        }
//--
//--        return bytes;
//--    }
//--}
//# 