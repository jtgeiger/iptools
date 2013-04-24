package com.sibilantsolutions.iptools.util;

import static com.sibilantsolutions.iptools.util.HexDumpDeferred.prettyDump;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sibilantsolutions.iptools.event.ReceiveEvt;
import com.sibilantsolutions.iptools.event.SocketListenerI;

public class Socker
{
    final static private Logger log = LoggerFactory.getLogger( Socker.class );

    static public void readLoop( Socket socket, SocketListenerI listener )
    {
        log.info( "Running read loop for socket={}.", socket );

        InputStream ins;
        try
        {
            ins = socket.getInputStream();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            throw new UnsupportedOperationException( "OGTE TODO!", e );
        }

        byte[] b = new byte[1024];
        boolean isRunning = true;
        while ( isRunning )
        {
            int numRead = -1304231933;
            isRunning = false;

            try
            {
                numRead = ins.read( b );
                isRunning = ( numRead >= 0 );
                
                if ( ! isRunning )
                {
                    log.info( "Socket closed intentionally by remote host (read returned={})={}.", numRead, socket );
                }
            }
            catch ( SocketException e )
            {
                if ( socket.isClosed() )
                {
                    log.info( "Socket read unblocked after being closed={}.", socket );
                }
                else
                {
                    // TODO Auto-generated catch block
                    throw new UnsupportedOperationException( "OGTE TODO!", e );
                }
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                throw new UnsupportedOperationException( "OGTE TODO!", e );
            }
            
            if ( isRunning )
            {
                log.info( "Read=0x{}/{}: \n{}", HexDump.numToHex( numRead ), numRead, prettyDump( b, 0, numRead ) );
                try
                {
                    listener.onReceive( new ReceiveEvt( b, numRead, socket ) );
                }
                catch ( Exception e )
                {
                    log.error( "Trouble processing data:", new Exception( e ) );
                    //TODO: Send a 503.
                }
            }
        }

        try
        {
            socket.close();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            throw new UnsupportedOperationException( "OGTE TODO!", e );
        }

        log.info( "Finished read loop for socket={}.", socket );
    }

    static public void readLoopThread( final Socket socket, final SocketListenerI listener )
    {
        Runnable r = new Runnable() {

            @Override
            public void run()
            {
                log.info( "Started receiver thread for socket={}.", socket );

                readLoop( socket, listener );

                log.info( "Finished receiver thread for socket={}.", socket );
            }

        };

        new Thread( r ).start();
    }

    static public void send( byte[] buf, Socket socket )
    {
        send( buf, 0, buf.length, socket );
    }

    static public void send( byte[] buf, int length, Socket socket )
    {
        send( buf, 0, length, socket );
    }

    static public void send( byte[] buf, int offset, int length, Socket socket )
    {
        log.info( "Send=0x{}/{}: \n{}", HexDump.numToHex( length ), length, prettyDump( buf, offset, length ) );

        try
        {
            OutputStream os = socket.getOutputStream();
            os.write( buf, offset, length );
            os.flush();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            throw new UnsupportedOperationException( "OGTE TODO!", e );
        }

    }

    static public void send( String s, Socket socket )
    {
        byte[] bytes = s.getBytes( HexDump.cs );
        send( bytes, socket );
    }

}
