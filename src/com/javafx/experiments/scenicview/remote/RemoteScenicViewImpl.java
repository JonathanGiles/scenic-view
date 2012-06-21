package com.javafx.experiments.scenicview.remote;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.javafx.experiments.scenicview.connector.AppEvent;

public class RemoteScenicViewImpl extends RMIBrowserObject implements RemoteScenicView{

    RemoteApplication application;
    
    public RemoteScenicViewImpl() throws RemoteException {
        super();
        try
        {
            // get the address of this host.
            thisAddress= (InetAddress.getLocalHost()).toString();
        }
        catch(final Exception e)
        {
            throw new RemoteException("can't get inet address.");
        }

        thisPort= 7557;  // this port(registry’s port)


        // create the registry and bind the name and object.

        registry = LocateRegistry.createRegistry( thisPort );

        registry.rebind("ScenicView", this);
    }

    @Override public void dispatchEvent(final AppEvent event) {
        System.out.println(event.getType());
    }

    @Override public void onAgentStarted(final int port) {
        System.out.println("Agent started on port:"+port);
        final Thread remoteBrowserFinder = new Thread()
        {
            @Override
            public void run()
            {

                    while(application==null)
                    {
                        try 
                        {   
                            application = new RemoteApplicationFinder("127.0.0.1", port).getRemoteApplication();
                        
                            sleep(10000);
                        } 
                        catch (final Exception e) 
                        {
                            
                        }                           
                    }
                    try {
                        application.sendInfo("Info to aggent");
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                
            }
        };
        remoteBrowserFinder.start();
    }
    
    public static void main (final String [] args) throws RemoteException, InterruptedException {
        final RemoteScenicViewImpl impl = new RemoteScenicViewImpl();
//        while(impl.application==null) {
//            Thread.sleep(1000);
//        }
        
    }

}
