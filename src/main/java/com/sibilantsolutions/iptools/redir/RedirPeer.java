package com.sibilantsolutions.iptools.redir;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sibilantsolutions.iptools.event.LostConnectionEvt;
import com.sibilantsolutions.iptools.event.ReceiveEvt;
import com.sibilantsolutions.iptools.event.SocketListenerI;
import com.sibilantsolutions.iptools.net.SocketUtils;

public class RedirPeer implements Runnable
{
    final static private Logger log = LoggerFactory.getLogger( RedirPeer.class );

    private Socket socket;
    private RedirPeer peer;

    public void close()
    {
        if ( ! socket.isClosed() )
        {
            log.info( "Closing socket={}.", socket );
            try
            {
                socket.close();
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                throw new UnsupportedOperationException( "OGTE TODO!", e );
            }
        }
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket( Socket socket )
    {
        this.socket = socket;
    }

    public RedirPeer getPeer()
    {
        return peer;
    }

    public void setPeer( RedirPeer peer )
    {
        this.peer = peer;
    }

    @Override
    public void run()
    {
        log.info( "Running thread={} for peer={}.", Thread.currentThread(), socket );

        SocketListenerI listener = new SocketListenerI() {

            @Override
            public void onLostConnection( LostConnectionEvt evt )
            {
                //No-op; readLoop will return below and we will fall out.
            }

            @Override
            public void onReceive( ReceiveEvt evt )
            {
                peer.send( evt.getData(), evt.getOffset(), evt.getLength() );
            }

        };

        SocketUtils.readLoop( 4096, socket, listener );

        log.info( "Closing peer (if open)." );
        peer.close();

        log.info( "Finished thread={} for peer={}.", Thread.currentThread(), socket );
    }

    public void send( byte[] data, int offset, int length )
    {
        SocketUtils.sendNoLog( data, offset, length, socket );
    }

}
