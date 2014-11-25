package org.fxconnector.event;

import org.fxconnector.StageID;
import org.fxconnector.node.SVNode;

public class EvCSSFXEvent extends FXConnectorEvent {
    private static final long serialVersionUID = 7270158278849080779L;
    private final SVNode origin;
    private final String uri;
    private final String source;
    
    public EvCSSFXEvent(SVEventType type, StageID id, SVNode origin, String uri) {
        this(type, id, origin, uri, null);
    }
    
    public EvCSSFXEvent(SVEventType type, StageID id, SVNode origin, String uri, String source) {
        super(type, id);
        this.origin = origin;
        this.uri = uri;
        this.source = source;
    }

    public SVNode getOrigin() {
        return origin;
    }

    public String getUri() {
        return uri;
    }

    public String getSource() {
        return source;
    }
}
