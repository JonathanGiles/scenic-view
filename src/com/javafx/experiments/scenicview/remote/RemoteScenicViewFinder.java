package com.javafx.experiments.scenicview.remote;

import java.rmi.registry.*;


public class RemoteScenicViewFinder
{
	
	String serverAdress;
	int serverPort;
	RemoteScenicView rmiClient;
	
	public RemoteScenicViewFinder (final String serverAdress, final int serverPort) throws Exception
	{
		this.serverAdress = serverAdress;
		this.serverPort = serverPort;
		final Registry registry=LocateRegistry.getRegistry(
				serverAdress,
	               (new Integer(serverPort)).intValue()
	           );
	    // look up the remote object
		rmiClient=(RemoteScenicView)(registry.lookup("ScenicView"));		
	}

	public RemoteScenicView getRemoteBrowser()
	{
		return rmiClient;
	}
	
}