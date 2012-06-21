package com.javafx.experiments.scenicview.remote;

import java.rmi.registry.*;


public class RemoteApplicationFinder
{
	
	String serverAdress;
	int serverPort;
	RemoteApplication rmiClient;
	
	public RemoteApplicationFinder (final String serverAdress, final int serverPort) throws Exception
	{
		this.serverAdress = serverAdress;
		this.serverPort = serverPort;
		final Registry registry=LocateRegistry.getRegistry(
				serverAdress,
	               (new Integer(serverPort)).intValue()
	           );
	    // look up the remote object
		rmiClient=(RemoteApplication)(registry.lookup("AgentServer"));		
	}

	public RemoteApplication getRemoteApplication()
	{
		return rmiClient;
	}
	
}