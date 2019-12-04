//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View;

import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;
import com.phoenixcontact.rsc.demo.Helper.MessageEvent;
import com.phoenixcontact.rsc.demo.Helper.Action;
import com.phoenixcontact.rsc.demo.Helper.SecureConnectionInfo;
import com.phoenixcontact.rsc.demo.RscDemoGui;
import com.phoenixcontact.rsc.demo.View.Tabs.FileServiceTab;
import com.phoenixcontact.rsc.demo.View.Tabs.GdsTab;
import com.phoenixcontact.rsc.demo.View.Tabs.MainTab;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * 
 * @author andreniggemann
 * 
 * Controller for the Main View displayed after login.
 * Loads further tabs and supplies communication infrastructure
 *
 */
public class MainViewController {

    @FXML Label messageLabel;
    @FXML AnchorPane messagePane;

    @FXML Tab mainTab;
    @FXML Tab gdsTab;
    @FXML Tab fileserviceTab;
    @FXML TabPane tabPane;

    @FXML ListView<String> logListView;


    private ObservableList<String> logs;
    private ServiceManager sm;
    private SecureConnectionInfo sci;
    private CoreCommunication communication;

    private TabController[] tabControllers;
    private EventHandler<WindowEvent> closeStageHandler;

    private Stage stage;

    /**
     * initialize after loading the corresponding fxml file
     */
    @FXML
    private void initialize() {

        logs = FXCollections.observableArrayList();//new ArrayObservableList<>();
        logListView.setItems(logs);
    }



    /**
     * initialized by the login screen
     * 
     * @param stage
     * The stage displaying the MainView. Necessary to make it possible to go back to the login screen.
     * 
     * @param sci
     * SecureConnectionInfo is used to establish a connection with the
     * controller via the service manager
     * 
     * @throws Exception
     * Is expected to throw to signal to the Login that the Main View could not be loaded.
     * Most likely because the connection to the plc could not be established
     */
    public void init(Stage stage, SecureConnectionInfo sci) throws Exception{
        //initialize local variables
        this.sm = new ServiceManager();
        sm.connect(sci.getConnectionInfo(), sci.getSecurityInfo());

        communication = new CoreCommunication(
                this::MessageHandler, this::ErrorHandler, this::LogHandler, this::backToLogin);

        this.sci = sci;
        this.stage = stage;

        //load all tabs. Can be easily extended by more tabs. Observe that those are pushed from the front, i.e. you have to insert them here in the reverse order

        var tabs = tabPane.getTabs();
        tabControllers = new TabController[] {
                this.<GdsTab>loadTab(tabs, "GDS", "/GdsTab.fxml"),
                this.<FileServiceTab>loadTab(tabs, "File Service", "/FileServiceTab.fxml"),
                this.<MainTab>loadTab(tabs, "Main Control" ,"/MainTab.fxml")
        };
        
        //Show the first tab
        tabPane.getSelectionModel().select(0);


        //hook into the OnCloseRequest to do clean up
        closeStageHandler = stage.getOnCloseRequest();
        stage.setOnCloseRequest(event -> {
            clean();
            if(closeStageHandler != null)
                closeStageHandler.handle(event);
        });
    }

    /**
     * Loads tabs into the TabPane and return the corresponding controller as a handle.
     * The new Tab will be pushed to the front of the TabPane. This means that this method should be called in
     * reverse order for the tabs you want to display
     * @param tabs
     * The list of tabs in the TabPane
     * @param displayName
     * The name that should be displayed at the top of the tabpane
     * @param resource
     * the name of the fxml file relative to the resource folder, starting with a slash.
     * @return
     */
    private  <T extends TabController> T loadTab(ObservableList<Tab> tabs, String displayName, String resource) {
        try{
        	var loader = new FXMLLoader(RscDemoGui.class.getResource(resource));
            var page = (Parent) loader.load();
            var viewController = loader.<T>getController();
            viewController.init(sm, communication);
            var tab = new Tab();
            tab.setText(displayName);
            tab.setContent(page);
            tabs.add(0, tab);
            return viewController;
        }
        catch(Exception ex){
            showError("Failed to load " + resource);
            return null;
        }
    }

    /**
     * Method to clean up the main screen and go back to the login screen.
     * 
     */
    private void backToLogin(){
        try {
            var loader = new FXMLLoader(
                    RscDemoGui.class.getResource("/Login.fxml"));
            var page = (Parent) loader.load();
            var viewController = loader.<Login>getController();
            var connectionInfo = sci.getConnectionInfo();
            var securityInfo = sci.getSecurityInfo();

            viewController.init(stage,
                    connectionInfo.getHost(),
                    Integer.toString(connectionInfo.getPort()),
                    securityInfo.getUserName(),
                    new String(securityInfo.getPassword()));

            clean();

            var scene = new Scene(page);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception ex){
            showError("Failed to go back");
        }
    }

    /**
     * Cleans up all tabs, the service manager and the SecureConnectionInfo
     * 
     */
    private void clean(){
        for(var tab : tabControllers)
            if (tab != null)
                ((Action)tab::clean).silence().run(); //clean tabs ignoring exceptions

        Action closeServiceManager = () -> sm.close();
        closeServiceManager.silence().run();

        sci.clean();
        stage.setOnCloseRequest(closeStageHandler);
    }


    private void ErrorHandler(MessageEvent event) {
        showError(event.getMessage());

    }
    /**
     * Shows the message on the bottom of the screen on a red bar to signal
     * failure to the user. Additionaly the message will be logged on the log tab.
     * @param message
     * The message that will be displayed. "Error: " will be prepended.
     */
    private void showError(String message){
        log("Error: " + message);
        synchronized (messagePane){
            messagePane.setBackground(new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY)));
            messageLabel.setText("Error: " + message);
        }

    }

    private void MessageHandler(MessageEvent event){
        showMessage(event.getMessage());
    }

    /**
     * Shows the message on the bottom of the screen on a green bar to signal
     * success to the user. Additionaly the message will be logged on the log tab.
     * @param message
     * The message that will be displayed. "Message: " will be prepended.
     */
    private void showMessage(String message){
        log("Message " + message);
        synchronized (messagePane){
            messagePane.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN , CornerRadii.EMPTY, Insets.EMPTY)));
            messageLabel.setText("Message: " + message);
        }

    }

    private void LogHandler(MessageEvent event) {
        log(event.getMessage());
    }

    /**
     * Adds message to log list. Currently does not persist logs
     * @param message
     * The message to be added.
     */
    private void log(String message){
        synchronized (logs) {
            logs.add(message);
        }
    }

}
