package com.javafx.experiments.scenicview.remote;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.javafx.experiments.scenicview.StageModel;
import com.javafx.experiments.scenicview.connector.event.MousePosEvent;

public class RemoteApplicationImpl extends RMIBrowserObject implements RemoteApplication
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	RemoteApplication application;
	
	public RemoteApplicationImpl(final RemoteApplication browser, final int port) throws RemoteException 
	{
		try 
		{		
			this.application = browser;
			this.thisPort = port;
			thisAddress= (InetAddress.getLocalHost()).toString();

	        // create the registry and bind the name and object.

	        registry = LocateRegistry.createRegistry( thisPort );

	        registry.rebind("AgentServer", this);			
		} 
		catch (final Exception e) 
		{
			throw new RemoteException();
		}
		
	}

    @Override public void sendInfo(final String info) throws RemoteException {
        application.sendInfo(info);
    }
    
    public static RemoteScenicView scenicView;
    
    public static void main (final String [] args) throws RemoteException, InterruptedException {
        new RemoteApplicationImpl(new RemoteApplication() {
            
            @Override public void sendInfo(final String info) {
                System.out.println("INFO:"+info);
                
            }
        }, 7556);
        System.out.println("Remote application launched");
        
        final Thread remoteScenicViewFinder = new Thread()
        {
            @Override
            public void run()
            {

                    while(scenicView==null)
                    {
                        try 
                        {
                            System.out.println("Finding ScenicView...");
                            scenicView = new RemoteScenicViewFinder("127.0.0.1", 7557).getRemoteBrowser();
                        
                            sleep(10000);
                        } 
                        catch (final Exception e) 
                        {
                            
                        }                           
                    }
                    try {
                        scenicView.onAgentStarted(7556);
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        scenicView.dispatchEvent(new MousePosEvent(StageModel.STAGE_ID, "1024x345"));
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                
            }
        };
        remoteScenicViewFinder.start();
        
        while(scenicView==null) {
            Thread.sleep(1000);
        }
        System.out.println("ScenicView found:"+scenicView);

    }
	
}
