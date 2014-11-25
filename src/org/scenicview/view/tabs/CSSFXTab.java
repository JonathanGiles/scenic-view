package org.scenicview.view.tabs;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import org.fxconnector.StageID;
import org.fxconnector.event.EvCSSFXEvent;
import org.fxconnector.event.FXConnectorEvent;
import org.fxconnector.event.FXConnectorEvent.SVEventType;
import org.scenicview.view.ScenicViewGui;
import org.scenicview.view.cssfx.CSSFXTabContentController;
import org.scenicview.view.cssfx.MonitoredCSS;

public class CSSFXTab extends Tab {
    private static final String CSSFX_TAB_NAME = "CSSFX";
    private ScenicViewGui gui;
    private CSSFXTabContentController cssfxTabContentController;
    private Map<StageID, ObservableList<MonitoredCSS>> cssByStage = new HashMap<StageID, ObservableList<MonitoredCSS>>();

    public CSSFXTab(ScenicViewGui gui) {
        super(CSSFX_TAB_NAME);
        this.gui = gui;

        // Set the tab icon, uses a SVG CSS3 icon
        setGraphic(createTabGraphic());
        setContent(createTabContent());
        
        setClosable(false);
    }

    private Node createTabContent() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(CSSFXTabContentController.class.getResource("cssfxtabcontent.fxml"));
            Node root = fxmlLoader.load();
            cssfxTabContentController = (CSSFXTabContentController)fxmlLoader.getController();
            cssfxTabContentController.setScenicViewGui(gui);
            return root;
        } catch (Exception ex) {
            ex.printStackTrace();
            Label errorLabel = new Label("Failure loading CSSFX tab");
            errorLabel.setAlignment(Pos.CENTER);
            StackPane sp = new StackPane(errorLabel);
            return sp;
        }
    }

    private Node createTabGraphic() {
        SVGPath p = new SVGPath();
        p.setContent("m 180.75256,334.77228 -0.53721,2.68603 10.93285,0 -0.3412,1.73502 -10.9401,0 -0.52996,2.68603 10.93286,0 -0.6098,3.06352 -4.40654,1.45917 -3.81851,-1.45917 0.26134,-1.32849 -2.68602,0 -0.63884,3.22323 6.31579,2.41742 7.2813,-2.41742 0.96552,-4.84937 0.19601,-0.97277 1.24138,-6.2432 z");
        StackPane sp = new StackPane(p);
        sp.setMaxWidth(24.0);
        sp.setMaxHeight(24.0);
        sp.setPrefWidth(24.0);
        sp.setPrefHeight(24.0);
        return sp;
    }

    public void handleEvent(FXConnectorEvent appEvent) {
        if (appEvent instanceof EvCSSFXEvent) {
            EvCSSFXEvent cssEvent = (EvCSSFXEvent) appEvent;
            SVEventType type = cssEvent.getType();
            
            switch (type) {
            case CSS_ADDED:
                addCSS(cssEvent.getStageID(), cssEvent.getUri(), cssEvent.getSource());
                break;
            case CSS_REMOVED:
                removeCSS(cssEvent.getStageID(), cssEvent.getUri());
                break;
            case CSS_REPLACED:
                replaceCSS(cssEvent.getStageID(), cssEvent.getUri(), cssEvent.getSource());
                break;
            default:
                break;
            }
        }
    }

    public void registerStage(StageID stageID) {
        ObservableList<MonitoredCSS> stageCSS = cssByStage.computeIfAbsent(stageID, sid -> FXCollections.observableArrayList());
        if (cssfxTabContentController.getMonitoredCSS() != null) {
            cssfxTabContentController.setMonitoredCSS(stageCSS);
        }
    }

    public void stageSelected(StageID stageID) {
        if (stageID == null) {
            cssfxTabContentController.setMonitoredCSS(FXCollections.emptyObservableList());
        } else {
            ObservableList<MonitoredCSS> stageCSS = cssByStage.computeIfAbsent(stageID, sid -> FXCollections.observableArrayList());
            cssfxTabContentController.setMonitoredCSS(stageCSS);
        }
    }
    
    public void addCSS(StageID stageID, String uri, String source) {
        replaceCSS(stageID, uri, source);
    }

    public void removeCSS(StageID stageID, String uri) {
        ObservableList<MonitoredCSS> monitoredCSS = cssByStage.get(stageID);
        if (monitoredCSS != null) {
            monitoredCSS.removeIf(css -> css.getCSS().equals(uri));
        }
    }

    public void replaceCSS(StageID stageID, String uri, String source) {
        ObservableList<MonitoredCSS> monitoredCSS = cssByStage.get(stageID);
        if (monitoredCSS != null) {
            FilteredList<MonitoredCSS> existingCSS = monitoredCSS.filtered(css -> css.getCSS().equals(uri));
            
            if (existingCSS.size() > 0) {
                existingCSS.forEach(css -> css.mappedBy().set(source));
            } else  {
                MonitoredCSS css = new MonitoredCSS(uri);
                css.mappedBy().set(source);
                monitoredCSS.add(css);
            }
        }
    }
}
