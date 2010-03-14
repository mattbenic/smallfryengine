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
//--import java.util.Vector;
//--import javax.bluetooth.*;
//--import java.util.Timer;
//--import java.util.TimerTask;
//--
//--
//--//==============================================================================
//--// CLASS (OR INTERFACE) DECLARATIONS
//--
//--/** The <code>BluetoothDiscovery</code> provides an easy API to search devices
//-- * and services.<br>
//-- * Main methods to use are searchService and waitOnConnection.<br>
//-- * searchService is used to search the vicinity for other BluetoothDevices and
//-- * connect to them if the offered service matches.<br>
//-- * waitOnConnection is used to offer an service and wait on other devices to
//-- * connect to the local device.<br>
//-- * Before these methods are called you usually should setup the name and the service.
//-- * This is done with setName and setServiceUUID.<br>
//-- * Please note that for discoverability mode and for inquiry the LIAC is used, so
//-- * that you will find only devices which are in limited inquiry access mode. This
//-- * is because this Java code is optimized for MIDlet to MIDlet communication, so that
//-- * the same MIDlet is running on local and on remote device. With using LIAC the
//-- * search process is much faster in environments with many Bluetooth devices around.<br>
//-- */
//--
//--public class BluetoothDiscovery
//--{
//--    //==============================================================================
//--    // Final variables (Class constants)
//--    /** The CONCEPT_SDK_BUILD constant determines if MIDlet should run in Concept SDK emulator.
//--     * Unfortunately there are a few flaws in the emulator, so that certain functionality
//--     * cannot be used. If you want to debug in the Concept SDK emulator on PC please set the constant
//--     * to true, for the final target (phone) release you should set this to false.
//--     * Also, if CONCEPT_SDK_BUILD is true, a short Alert is shown (in setServiceUUID, which is one of
//--     * the first methods invoked) to make you aware that this is not the final build. <br>
//--     * <img src="doc-files/crop_concept_sdk_build.jpg"><br>
//--     * The Alert is not shown if CONCEPT_SDK_BUILD is false.
//--     */
//--    static public final boolean CONCEPT_SDK_BUILD = false;   // It's public so that it's description is shown in javadoc
//--    // Attribute ID for service name for base language
//--    static private final int SERVICE_NAME_BASE_LANGUAGE = 0x0100;
//--    // The major device class (in CoD) used for phones:
//--    static private final int MAJOR_DEVICE_CLASS_PHONE = 0x0200;
//--    // Search types
//--    /** Searches each device for the service. If a device with the service is found
//--     * the search is cancelled. A connection is setup to that device/service.
//--     * No device is shown to the user. */
//--    static public final int SEARCH_CONNECT_FIRST_FOUND = 1;
//--    /**  Searches each device for the service. All devices with the searched service
//--     *   will be connected. No devices are shown to the user. */
//--    static public final int SEARCH_CONNECT_ALL_FOUND = 2;
//--    /**   Searches for the service on every found device. A list of devices that
//--     *   offer the service is displayed to the user. User can stop the service
//--     *   search if at least one device has been found. After stop or after all
//--     *   devices are shown to the user the user can select one device. This
//--     *   device is connected. */
//--    static public final int SEARCH_ALL_DEVICES_SELECT_ONE = 3;
//--    /**   Searches for the service on every found device. A list of devices that
//--     *   offer the service is displayed to the user. User can stop the service
//--     *   search if at least one device has been found. After stop or after all
//--     *   devices are shown to the user the user can select one or several devices. These
//--     *   devices are connected.
//--     *   Note that this functionality is only available on devices which support
//--     *   point-to-multipoint connectivity (bluetooth.connected.devices.max > 1). If the
//--     *   device supports only point-to-point connectivity (bluetooth.connected.devices.max = 1) the behaviour is the same as
//--     *   if  SEARCH_ALL_DEVICES_SELECT_ONE would have been used.
//--     */
//--    static public final int SEARCH_ALL_DEVICES_SELECT_SEVERAL = 4;
//--    /** Maximum number of connectable, active devices  according to Bluetooth specification) */
//--    static private final int BLUETOOTH_MAX_DEVICES = 7;
//--
//--    //==============================================================================
//--    // Class (static) variables
//--
//--    //==============================================================================
//--    // Instance variables
//--    private BluetoothCallback btCB;
//--
//--    // Bluetooth/JSR82 variables
//--    private LocalDevice localDevice = null; // Reference to LocalDevice
//--    private int previousDiscoverabilityMode = -1; // Used to restore discoverabilty mode afterwards
//--    // UUID:
//--    // x = random, b = BD address of one of my Bluetooth devices (because BD addresses are also unique)
//--    // xxxx xxxx xxxx 1xxx 8xxx bbbbbbbbbbbb
//--    // Eg. for my device = 006057028C19, ie. "00000000000010008000006057028C19"
//--    private String serviceUUID = null;
//--    // The name of the device/service
//--    private String localName = null;
//--
//--    // Used for Client
//--    private DiscoveryAgent discoveryAgent; // Reference to DiscoveryAgent (used for Inquiry)
//--    private Listener listener; // Discovery Listener (used for inquiry)
//--    private int searchType;    // Holds the search type the user wants to use
//--    private int serviceSearchTransId; // trans action id for current service search, if there is no serv search this is -1
//--    private Vector urlStrings; // This holds the url strings of the returned services
//--    private Vector foundServiceRecords; // holds all the found service records
//--    private int maxDevices; // Number of max. connectable devices (retrieved by getproperty)
//--    private int warning; // Warnign presented to the user in case no device or no service was found
//--
//--    // Used for Server
//--    private StreamConnectionNotifier notifier;  // For the server
//--    // Used for both
//--    private BluetoothConnection[] btConnections;  // All Bluetooth connections
//--
//--    // Will get value of this, so this can be easily accessed from inner classes
//--    private BluetoothDiscovery  root;
//--
//--    // Used for waiting/notify
//--    private Object block_c;	// for Client
//--    private Object block_s;	// for Server
//--    private Object block_ss;	// for termination of service search
//--    private Object block_notifier;   // For Notifier.close
//--
//--    //==============================================================================
//--    // Constructors and miscellaneous (finalize method, initialization block,...)
//--
//--    /** Creates an <code>BluetoothDiscovery</code> object.
//--     * @param disp The Bluetooth callback variable
//--     *
//--     */
//--    public BluetoothDiscovery( BluetoothCallback btCB )
//--    {
//--        // store 'this'
//--        root = this;
//--        // store the callback
//--        this.btCB = btCB;
//--        // Initialize
//--        // Create object used to synchronize/lock
//--        block_c = new Object();
//--        block_s = new Object();
//--        block_ss = new Object();
//--        block_notifier = new Object();
//--
//--        try
//--        {
//--      System.out.println("LocalDevice.getLocalDevice()");
//--            // Obtain local device object
//--            localDevice = LocalDevice.getLocalDevice();
//--      System.out.println("maxDevices = Integer.parseInt( localDevice.getProperty( bluetooth.connected.devices.max ) );");
//--            // Retrieve number of connectable devices
//--            maxDevices = Integer.parseInt( localDevice.getProperty( "bluetooth.connected.devices.max" ) );
//--            // Because of definition with parked devices
//--            if( maxDevices > BLUETOOTH_MAX_DEVICES )
//--            {
//--                // limit to 7
//--                maxDevices = BLUETOOTH_MAX_DEVICES;
//--            }
//--        }
//--        catch(Exception e)
//--        {
//--            // not much that can be done
//--            localDevice = null;
//--            btCB.bluetoothAction(Utils.BTACT_ExcLocalDeviceInq, e);
//--        }
//--
//--    }   /*  End of the constructor method   */
//--
//--    /** Closes the Notifier.
//--     * If Notifier is not null, Notifier.close is called . Afterwards
//--     * Notifier is set to null.
//--     */
//--    private void closeNotifier()
//--    {
//--        synchronized( block_notifier )
//--        {
//--            if( notifier != null )
//--            {
//--                try
//--                {
//--                    // Does not work correctly in Concept SDK,
//--                    // but should be done in real phone to deregister service
//--                    notifier.close();
//--                }
//--                catch(Exception e)
//--                {
//--                    btCB.bluetoothAction(Utils.BTACT_ExcCloseNotifier, e);
//--                }
//--                notifier = null;
//--            }
//--        }
//--    }
//--
//--    /**
//--     * cancel the discovery
//--     */
//--    public void cancelDiscovery()
//--    {
//--        // User chose to cancel this class
//--        try
//--        {
//--            // Cancel discovery process
//--            if( listener != null )
//--            {
//--                // cancel client
//--                // Info note to user
//--                btCB.bluetoothAction(Utils.BTACT_SearchCancelled, null);
//--
//--                // cancel Inquiry
//--                discoveryAgent.cancelInquiry( listener );
//--                // Cancel service search
//--                // Because this has to wait on termination a new thread is required.
//--                // Otherwise the alert above would not be displayed.
//--                waitOnServSearchTermination w = new waitOnServSearchTermination();
//--                w.start();
//--            }
//--            // Remove listener
//--            listener = null;
//--            // This is used to cancel a server session
//--            closeNotifier();
//--
//--            btCB.bluetoothAction(Utils.BTACT_ProgressComplete, null);
//--        }
//--        catch(Exception e)
//--        {
//--            btCB.bluetoothAction(Utils.BTACT_ExcCancel, e);
//--        }
//--    }
//--
//--    /**
//--     * SaveDiscoverability
//--     * Saves the current discoverability mode.
//--     */
//--    private void saveDiscoverability()
//--    {
//--        try
//--        {
//--            // Store discoverability mode
//--            previousDiscoverabilityMode =
//--                LocalDevice.getLocalDevice().getDiscoverable();
//--        }
//--        catch(Exception e)
//--        {
//--            // We will just ignore, and not try to save it.
//--        }
//--    }
//--
//--    /**
//--     * RestoreDiscoverability
//--     * Restores the discoverability mode.
//--     */
//--    private void restoreDiscoverability()
//--    {
//--        try
//--        {   // Restore discoverability mode
//--            if( previousDiscoverabilityMode != -1 )
//--            {
//--                localDevice.setDiscoverable( previousDiscoverabilityMode );
//--            }
//--        }
//--        catch( Exception e )
//--        {
//--            // We will just ignore; there is not much we can do.
//--        }
//--    }
//--
//--
//--    /** Sets the local name that is used by the server/device (not the friendly name).
//--     * This can freely be chosen, it could also be the friendly name of the device.
//--     * @param ln Name to be used.
//--     */
//--    public void setName( String ln )
//--    {
//--        // Store service name
//--        localName = ln;
//--    }
//--
//--    /** Sets the UUID for the service.
//--     * A UUID is made of:
//--     * x = random, b = BD address of one of my Bluetooth devices (because BD addresses are also unique)
//--     * xxxx xxxx xxxx 1xxx 8xxx bbbbbbbbbbbb
//--     * Eg. for my 3650 the BD address is 006057028C19. So I could invent a
//--     * UUID equal to "00000000000010008000006057028C19".
//--     * @param UUID UUID that shall be used.
//--     */
//--    public void setServiceUUID( String UUID )
//--    {
//--        // store UUID
//--        serviceUUID = UUID;
//--    }
//--
//--
//--    /** Does an Inquiry followed by a service search.
//--     * The user has (depending on parameter) the possibility to select one or more devices.
//--     * The devices are connected and a list of connections is returned.
//--     * @param st Defines the search type. Depending on the search type the user might have to
//--     * select one or more found devices. Allowed values are: <br>
//--     * {@link #SEARCH_CONNECT_FIRST_FOUND SEARCH_CONNECT_FIRST_FOUND},<br>
//--     * {@link #SEARCH_CONNECT_ALL_FOUND SEARCH_CONNECT_ALL_FOUND},<br>
//--     * {@link #SEARCH_ALL_DEVICES_SELECT_ONE SEARCH_ALL_DEVICES_SELECT_ONE}, and<br>
//--     * {@link #SEARCH_ALL_DEVICES_SELECT_SEVERAL SEARCH_ALL_DEVICES_SELECT_SEVERAL}.
//--     * @return Returns a list of connected devices.
//--     * @throws BluetoothStateException Is thrown when a request is made to the Bluetooth system that the system cannot support in its present state.
//--     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it using the interrupt method in class Thread.
//--     * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
//--     */
//--    public BluetoothConnection[] searchService( int st )
//--        throws BluetoothStateException, InterruptedException, IOException
//--    {
//--        StreamConnection con;
//--        DataElement de;
//--        String rname;
//--
//--        // Reset search transaction id
//--        serviceSearchTransId = -1;
//--
//--        // store search type
//--        searchType = st;
//--
//--        if( searchType == SEARCH_ALL_DEVICES_SELECT_SEVERAL )
//--        {   // if user should select several devices
//--            if( maxDevices == 1 )
//--            { // but only point-to-point possible
//--                // So switch back to "SELECT_ONE"
//--                searchType = SEARCH_ALL_DEVICES_SELECT_ONE;
//--            }
//--        }
//--        // Initialize
//--        foundServiceRecords = new Vector();
//--        urlStrings = new Vector();
//--        warning = Utils.BTACT_none;
//--
//--        // obtain discovery object which will be used for inquiry
//--        discoveryAgent = localDevice.getDiscoveryAgent();
//--
//--        // Create Discovery Listener (Inquiry Listener) Object
//--        listener = new Listener();
//--
//--        // Show progress bar for Inquiry
//--        btCB.bluetoothAction(Utils.BTACT_StartInquiry, null);
//--        btCB.bluetoothAction(Utils.BTACT_ProgressStart, new Integer(105));
//--        // startInquiry is asynchronous. Here we have to wait until it "notify"s us.
//--        synchronized( block_c )
//--        {   // start the inquiry on LIAC only
//--            discoveryAgent.startInquiry( DiscoveryAgent.LIAC, listener );
//--            // wait
//--            block_c.wait();
//--        }
//--
//--        btCB.bluetoothAction(Utils.BTACT_ProgressComplete, null);
//--
//--        // Check if service or devices not found and alert to user
//--        if( warning != Utils.BTACT_none )
//--        {
//--            btCB.bluetoothAction(warning, null);
//--        }
//--
//--        // Create list
//--        btConnections = new BluetoothConnection[urlStrings.size()];
//--        // Check if devices have been found
//--        if( urlStrings.size() > 0 )
//--        {
//--            // connect only if devices have been found
//--
//--            // Start connection progress bar
//--            btCB.bluetoothAction(Utils.BTACT_Connecting, null);
//--            btCB.bluetoothAction(Utils.BTACT_ProgressStart,  new Integer(urlStrings.size()));
//--
//--            // Connect all devices
//--            for( int i=0; i<urlStrings.size(); i++ )
//--            {
//--                // Retrieve remote name
//--                de = ((ServiceRecord)foundServiceRecords.elementAt(i)).getAttributeValue( SERVICE_NAME_BASE_LANGUAGE );
//--                rname = (String) de.getValue();
//--
//--                // Update progress bar
//--                btCB.bluetoothAction(Utils.BTACT_ProgressUpdate, new Integer(i));
//--
//--                btConnections[i] = new BluetoothConnection( (String) urlStrings.elementAt(i), localName, rname );
//--                // Send name to remote device
//--                btConnections[i].writeString( localName );
//--            }
//--
//--            // Stop (connecting) progress bar
//--            btCB.bluetoothAction(Utils.BTACT_ProgressComplete, null);
//--        }
//--
//--        // reset listener
//--        listener = null;
//--
//--        return btConnections;
//--    }
//--
//--
//--    /** Starts the server and register the service.
//--     * Waits until someone connects to this service.
//--     * @return A list containing 1 element which is the connection that has been
//--     * created from a remote device to this device. In case there has been a connection error
//--     * a list containing 1 element which is null is returned.
//--     * @throws BluetoothStateException Is thrown when a request is made to the Bluetooth system that the system cannot support in its present state.
//--     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it using the interrupt method in class Thread.
//--     * @throws IOException Signals that an I/O exception of some sort has occurred. This class is the general class of exceptions produced by failed or interrupted I/O operations.
//--     */
//--    public BluetoothConnection[] waitOnConnection()
//--    throws BluetoothStateException, IOException, InterruptedException
//--    {
//--        acceptAndOpenThread t;
//--        String ServiceName;
//--
//--        // Save Discoverability Mode
//--        saveDiscoverability();
//--
//--        // Go in Limited Inquiry scan mode
//--        localDevice.setDiscoverable( DiscoveryAgent.LIAC );
//--
//--        // Call connector.open to create Notifier object
//--        notifier = (StreamConnectionNotifier) Connector.open( "btspp://localhost:" + serviceUUID + ";name=" + localName + ";authorize=false;authenticate=false;encrypt=false" );
//--
//--        // Show text box with possibility to cancel the server session.
//--        btCB.bluetoothAction(Utils.BTACT_WaitingForConnection, null);
//--
//--//        setTimeout( FOREVER );
//--        // Spawn new thread which does acceptandopen
//--        t = new acceptAndOpenThread();
//--
//--        // wait on thread (until someone connects)
//--        synchronized( block_s )
//--        {
//--            // Start acceptAndOpen
//--            t.start();
//--            // wait
//--            block_s.wait();
//--        }
//--
//--        // Clear Notifier (is already closed)
//--        notifier = null;
//--        // restore discoverability mode
//--        restoreDiscoverability();
//--        // return the connection
//--        return btConnections;
//--    }
//--
//--        /**
//--         * Closes all connections
//--         */
//--        public synchronized void close()
//--        {
//--            if(btConnections != null)
//--            {
//--                // Disconnect
//--                for( int i=0; i<btConnections.length; i++ )
//--                {
//--                    if(btConnections[i] != null)
//--                    {
//--                        // loop through all connections
//--                        btConnections[i].close();
//--                    }
//--                }
//--            }
//--        }
//--
//--    // Inner class: Listener
//--    // Listens on events like deviceDiscovered or servicesDiscovered.
//--    private class Listener
//--    implements DiscoveryListener
//--    {
//--        private Vector cached_devices;
//--        ServiceRecord  currServRec;
//--
//--        /** Constructor
//--         */
//--        public Listener()
//--        {
//--            // Initialize
//--            cached_devices = new Vector();
//--        }
//--
//--        /**
//--         * Called when a device is found during an inquiry.  An inquiry
//--         * searches for devices that are discoverable.  The same device may
//--         * be returned multiple times.
//--         *
//--         * @see DiscoveryAgent#startInquiry
//--         *
//--         * @param btDevice the device that was found during the inquiry
//--         *
//--         * @param cod the service classes, major device class, and minor
//--         * device class of the remote device
//--         *
//--         */
//--        public void deviceDiscovered( RemoteDevice btDevice, DeviceClass cod )
//--        {
//--            // Filter CoD: Ie. only store devices in case the device is
//--            // a phone. That also prevents from the link level security problem:
//--            // Phone's do not use link level security.
//--            // (Because LIAC has been used for inquiry it is anyway very unlikely
//--            // that we run into the link level security problem.)
//--//            if( ! CONCEPT_SDK_BUILD )
//--            {   // Concept SDK returns wrong values for CoD
//--                if( cod.getMajorDeviceClass() != MAJOR_DEVICE_CLASS_PHONE )
//--                {   // return in case it's not a phone
//--                    return;
//--                }
//--            }
//--
//--            // It's another phone, so store it in the list
//--            if( ! cached_devices.contains( btDevice ) )
//--            {   // But only if it is not already in the list (same device might be reported more than once)
//--                cached_devices.addElement( btDevice );
//--            }
//--        }
//--
//--
//--        /**
//--         * Called when an inquiry is completed. The <code>discType</code> will be
//--         * <code>INQUIRY_COMPLETED</code> if the inquiry ended normally or
//--         * <code>INQUIRY_TERMINATED</code> if the inquiry was canceled by a call to
//--         * <code>DiscoveryAgent.cancelInquiry()</code>.  The <code>discType</code>
//--         * will be <code>INQUIRY_ERROR</code> if an error occurred while
//--         * processing the inquiry causing the inquiry to end abnormally.
//--         *
//--         * @see #INQUIRY_COMPLETED
//--         * @see #INQUIRY_TERMINATED
//--         * @see #INQUIRY_ERROR
//--         *
//--         * @param discType the type of request that was completed; either
//--         * <code>INQUIRY_COMPLETED</code>, <code>INQUIRY_TERMINATED</code>, or
//--         * <code>INQUIRY_ERROR</code>
//--         */
//--        public void inquiryCompleted( int discType )
//--        {
//--            if( discType == INQUIRY_COMPLETED )
//--            {   // Check if devices have been found
//--                if( cached_devices.size() == 0 )
//--                {   // No device found
//--                    warning = Utils.BTACT_WarnNodevicesFound;
//--                }
//--                else
//--                {
//--                    btCB.bluetoothAction(Utils.BTACT_ProgressComplete, null);
//--
//--                    // Start service search progress bar
//--                    btCB.bluetoothAction(Utils.BTACT_SearchService, null);
//--                    btCB.bluetoothAction(Utils.BTACT_ProgressStart, new Integer(cached_devices.size()));
//--
//--                    // start service search
//--                    nextDeviceServiceSearch();
//--                    return;
//--                }
//--            }
//--
//--            // In case inquiry was terminated or no device has been found
//--            // then return to main function
//--            synchronized( block_c )
//--            {
//--                block_c.notifyAll();
//--            }
//--            // Note: progressBar is anyway stopped by searchService method
//--        }
//--
//--        /**
//--         * NextDeviceServiceSearch.
//--         * Retrieves the next device from the stored list and searches
//--         * for the Service on this device.
//--         */
//--        private void nextDeviceServiceSearch()
//--        {
//--            UUID[] u = new UUID[1];
//--            u[0] = new UUID( serviceUUID, false );
//--            int attrbs[] =
//--            { SERVICE_NAME_BASE_LANGUAGE }; // Retrieved service record should include service name
//--            RemoteDevice dev;
//--
//--            // Update progress bar
//--            btCB.bluetoothAction(Utils.BTACT_ProgressUpdate, null);
//--
//--            // Retrieve next device
//--            try
//--            {
//--                dev = (RemoteDevice) cached_devices.firstElement();
//--                cached_devices.removeElementAt( 0 );
//--            }
//--            catch( Exception e )
//--            {   // All devices searched
//--                if( foundServiceRecords.size() == 0 )
//--                {   // If no device found then alert to user
//--                    warning = Utils.BTACT_WarnNoServiceFound;
//--                }
//--                // If service not found on any device return to main,
//--                // also if SEARCH_CONNECT_ALL_FOUND was selected.
//--                if( (foundServiceRecords.size() == 0)
//--                | (searchType == SEARCH_CONNECT_ALL_FOUND) )
//--                {  // return to main function
//--                    synchronized( block_c )
//--                    {
//--                        block_c.notifyAll();
//--                    }
//--                }
//--                // If deviceList is used, then it's ready now (will change "Stop"
//--                // button into "Cancel".
//--//                if( deviceList != null )
//--                {
//--//                    deviceList.ready();
//--                }
//--                return;
//--            }
//--
//--            // search for the service
//--            try
//--            {
//--                currServRec = null;
//--                serviceSearchTransId = discoveryAgent.searchServices( attrbs, u, dev, listener );
//--            }
//--            catch( BluetoothStateException e )
//--            {
//--                // an error occured
//--                // Try next device
//--                nextDeviceServiceSearch();
//--            }
//--
//--        }
//--
//--        /** Is called when the service we searched for is found.
//--         *
//--         * @param transID the transaction ID of the service search that is posting the result.
//--         * @param servRecord The service we searched for.
//--         */
//--        public void servicesDiscovered( int transID, ServiceRecord[] servRecord )
//--        {
//--            // A Service was found on the device.
//--            currServRec = servRecord[0];
//--        }
//--
//--        /** Depending on the search type different actions are taken.
//--         * Possible search types are:<br>
//--         * {@link #SEARCH_CONNECT_FIRST_FOUND SEARCH_CONNECT_FIRST_FOUND},<br>
//--         * {@link #SEARCH_CONNECT_ALL_FOUND SEARCH_CONNECT_ALL_FOUND},<br>
//--         * {@link #SEARCH_ALL_DEVICES_SELECT_ONE SEARCH_ALL_DEVICES_SELECT_ONE}, and<br>
//--         * {@link #SEARCH_ALL_DEVICES_SELECT_SEVERAL SEARCH_ALL_DEVICES_SELECT_SEVERAL}.
//--         * See also {@link #searchService searchService}.
//--         * @param transID the transaction ID of the service search that is posting the result.
//--         * @param respCode a code showing how the service search was completed
//--         */
//--        public void serviceSearchCompleted( int transID, int respCode )
//--        {
//--            synchronized( block_ss )
//--            {
//--                // Reset trans action id
//--                serviceSearchTransId = -1;
//--
//--                // Collect all devices with right service
//--                if( currServRec != null )
//--                {    // A device with this service is found, so add it
//--                    foundServiceRecords.addElement( currServRec );
//--                    urlStrings.addElement( currServRec.getConnectionURL( ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false ) );
//--                }
//--
//--                switch( searchType )
//--                {
//--                    case SEARCH_CONNECT_FIRST_FOUND:
//--                        // Stop searching after first device with the service is found
//--                        if( currServRec != null )
//--                        {    // A device with this service is found, so stop
//--                            synchronized( block_c )
//--                            {
//--                                block_c.notifyAll();
//--                            }
//--                            return;
//--                        }
//--                        break;
//--                    case SEARCH_CONNECT_ALL_FOUND:
//--                        break;
//--                    case SEARCH_ALL_DEVICES_SELECT_ONE:
//--                    case SEARCH_ALL_DEVICES_SELECT_SEVERAL:
//--                        if( currServRec != null )
//--                        {
//--                            // Update displayed list
//--                            displayFoundServices();
//--                        }
//--                        break;
//--                }
//--
//--                // Check if service search was terminated
//--                if( respCode == SERVICE_SEARCH_TERMINATED )
//--                {   // Notify the terminator (cancelServSearch)
//--                    synchronized( block_ss )
//--                    {
//--                        block_ss.notifyAll();
//--                    }
//--                }
//--                else
//--                {   // Search next device
//--                    nextDeviceServiceSearch();
//--                }
//--            }
//--        }
//--
//--        /**
//--         * displayFoundServices
//--         * Updates the displayed found services.
//--         */
//--        private void displayFoundServices()
//--        {
//--            // Append new service/device
//--            // Retrieve remote name
//--            DataElement de = ((ServiceRecord)foundServiceRecords.lastElement()).getAttributeValue( SERVICE_NAME_BASE_LANGUAGE );
//--            String rname = (String) de.getValue();
//--            // Add device to list
//--            btCB.bluetoothAction(Utils.BTACT_FoundService, rname);
//--        }
//--    }
//--
//--
//--    // Inner class
//--    /**
//--     * waitOnServSearchTermination
//--     * Only required in case a service search was cancelled, before at least
//--     * one service was found.
//--     * Waits until serviceSearchCompleted is called and then
//--     * notifies the searchServices method.
//--     */
//--    private class waitOnServSearchTermination
//--    extends Thread
//--    {
//--        /**
//--         * Terminates service search.
//--         * Waits until serviceSearchCompleted is called and then
//--         * notifies the searchServices method.
//--         */
//--        public void run()
//--        {
//--            synchronized( block_ss )
//--            {
//--                if( serviceSearchTransId != -1 )
//--                {   // only if there is a service search active
//--                    discoveryAgent.cancelServiceSearch( serviceSearchTransId );
//--                    // wait until service search has terminated
//--                    try
//--                    {
//--                        block_ss.wait();
//--                    }
//--                    catch(InterruptedException e)
//--                    {
//--                        // We can ignore this
//--                    }
//--                    // Now notify searchService method
//--                    synchronized( block_c )
//--                    {
//--                        block_c.notifyAll();
//--                    }
//--                }
//--            }
//--        }
//--    }
//--
//--    /**
//--     * DeviceList is a List to display the found devices.
//--     */
//--
//--    /**
//--     * Cancels the current service search.
//--     */
//--    public void cancelServSearch()
//--    {
//--        synchronized( block_ss )
//--        {
//--            if( serviceSearchTransId != -1 )
//--            {
//--                // only if there is a service search active
//--                discoveryAgent.cancelServiceSearch( serviceSearchTransId );
//--                // wait until service search has terminated
//--                try
//--                {
//--                    block_ss.wait();
//--                }
//--                catch( InterruptedException e )
//--                {
//--                    // we can ignore this
//--                }
//--            }
//--        }
//--    }
//--
//--    /**
//--     * Should be called when no more elements will be added to the
//--     * list.
//--     * The "Stop" button will change into "Cancel".
//--     */
//--    public void ready()
//--    {
//--    }
//--
//--    /**
//--     * Respond to commands.
//--     * @param c The command.
//--     * @param s The displayable object. */
//--/*
//--    public void commandAction( Command c, Displayable s )
//--    {
//--        int com = c.getCommandType();
//--
//--        if( (c == SELECT_COMMAND) && (searchType == SEARCH_ALL_DEVICES_SELECT_ONE) )
//--        {                // Behave the same as if OK was pressed.
//--            com = Command.OK;
//--        }
//--
//--        switch( com )
//--        {
//--            case Command.OK:
//--                // User selected OK
//--                // Cancel the current service search.
//--                cancelServSearch();
//--
//--                // Remove all elements that are not selected
//--                for( int i=size()-1; i>=0; i-- )
//--                {
//--                    if( ! isSelected(i) )
//--                    {   // not selected then remove
//--                        urlStrings.removeElementAt(i);
//--                    }
//--                }
//--
//--                // return to searchService function
//--                synchronized( block_c )
//--                {
//--                    block_c.notifyAll();
//--                }
//--                break;
//--
//--            case Command.STOP:
//--                // User stopped
//--                // Cancel the current service search.
//--                cancelServSearch();
//--                // Exchange stop button with cancel button
//--                ready();
//--                break;
//--
//--            case Command.CANCEL:
//--                // User cancelled
//--                // Remove all elements
//--                urlStrings.removeAllElements();
//--                // return to main function
//--                synchronized( block_c )
//--                {
//--                    block_c.notifyAll();
//--                }
//--                break;
//--        }
//--    }
//--*/
//--    /** acceptAndOpenThread.
//--     * This is needed as thread to allow cancellation from user interface.
//--     * Thread just does an acceptAndOpen and waits until Exception is thrown
//--     * eg. UI called Cancel or client connects.
//--     * If a client connects a thread is started for that connection.
//--     */
//--    private class acceptAndOpenThread
//--    extends Thread
//--    {
//--        /**
//--         * run method
//--         * Start acceeptAndOpen and wait on Exception or connection.
//--         */
//--        public void run()
//--        {
//--            StreamConnection con;
//--
//--            // Prepare data
//--            btConnections = new BluetoothConnection[1];
//--            // Register service
//--            try
//--            {
//--                // Wait on client
//--                System.out.println("acceptAndOpen");
//--                con = (StreamConnection) notifier.acceptAndOpen();
//--                btConnections[0] = new BluetoothConnection(con, localName, "Host");
//--                // Read host name
//--                btConnections[0].remoteName = btConnections[0].readString();
//--            }
//--            catch(Exception e)
//--            {
//--                // Accept and open terminated abnormally (maybe cancel)
//--                btConnections[0] = null;
//--            }
//--            // Remove notifier
//--            closeNotifier();
//--            // wakeup
//--            synchronized( block_s )
//--            {
//--                block_s.notifyAll();
//--            }
//--        }
//--    }
//--
//--    //kill everything
//--    public void kill()
//--    {
//--        // Remove notifier
//--        closeNotifier();
//--    }
//--}
//# 