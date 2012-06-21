package com.javafx.experiments.scenicview.remote;

import java.rmi.RemoteException;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.AppEvent.SVEventType;


public class RemoteScenicViewServer {

    RemoteScenicView scenicView;
    RemoteApplication application;
    
    public RemoteScenicViewServer() throws RemoteException {
        
        scenicView = new RemoteScenicViewImpl();
        final Thread remoteBrowserFinder = new Thread()
        {
            @Override
            public void run()
            {

                    while(scenicView==null)
                    {
                        try 
                        {   
                            scenicView = new RemoteScenicViewFinder("127.0.0.1", 7654).getRemoteBrowser();
                        
                            sleep(10000);
                        } 
                        catch (final Exception e) 
                        {
                            
                        }                           
                    }
                    try {
                        scenicView.dispatchEvent(new AppEvent(SVEventType.EVENT_LOG));
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                
            }
        };
        remoteBrowserFinder.start();
    }
    
}
