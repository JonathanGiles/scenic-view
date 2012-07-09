package com.javafx.experiments.scenicview.update;

import java.util.List;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.remote.RemoteScenicViewImpl;

public class RemoteVMsUpdateStrategy extends CommonUpdateStrategy {

    private boolean first = true;

    public RemoteVMsUpdateStrategy() {
        super(RemoteVMsUpdateStrategy.class.getName());
    }

    @Override List<AppController> getActiveApps() {
        if (first) {
            /**
             * Wait for the server to startup
             */
            first = false;
            while (RemoteScenicViewImpl.server == null) {
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return RemoteScenicViewImpl.server.connect();
    }

    @Override public void finish() {
        // TODO Auto-generated method stub
        super.finish();
        RemoteScenicViewImpl.server.close();
        System.exit(0);
    }

    @Override protected void closeUnused(final List<StageController> unused) {
        // for (int i = 0; i < unused.size(); i++) {
        // unused.get(i).close();
        // }
    }

}
