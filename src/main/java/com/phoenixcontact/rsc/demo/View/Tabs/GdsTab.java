//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View.Tabs;

import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;
import com.phoenixcontact.rsc.demo.RscController.GdsServices;
import com.phoenixcontact.rsc.demo.Helper.SubscriptionView;
import com.phoenixcontact.rsc.demo.View.TabController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

/**
 * 
 * @author andreniggemann
 * 
 * 
 * The view controller for the gds tab loaded from GdsTab.fxml
 * 
 *
 */
public class GdsTab implements TabController {

    private ObservableList<SubscriptionView> gdsSubs;

    @FXML
    TableView<SubscriptionView> subscriptionTable;
    @FXML TableColumn<SubscriptionView, String> uriColumn;
    @FXML TableColumn<SubscriptionView, String> typeColumn;
    @FXML TableColumn<SubscriptionView, String> valueColumn;



    @FXML
    TextField writeValueBox;

    @FXML TextField uriBox;


    private CoreCommunication communication;
    private GdsServices controller;

    /**
     * Initialization after loading fxml
     */
    @FXML
    private void initialize() {
        gdsSubs = FXCollections.observableArrayList();//new ArrayObservableList<>();

        subscriptionTable.setItems(gdsSubs);
        
        //Automatically binds the properties by name to the columns.
        //Automatically finds the getters in the SubscriptionView class
        uriColumn.setCellValueFactory(new PropertyValueFactory<>("uri"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    /**
     * Initialisation from the MainViewController to share common data.
     */
    @Override
    public void init(ServiceManager sm, CoreCommunication communication) throws Exception {
        this.communication = communication;

        controller = new GdsServices(sm, communication, gdsSubs);
        subscriptionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    /**
     * Shutdown update thread and unsubscribe
     */
    @Override
    public void clean() throws Exception{
        
        controller.clean();
    }



    /**
     * Tries to subscribe to the specified uri
     * All subscriptions appear in the listview on the left.
     * @param mouseEvent
     */
    @FXML
    private void subscribe(MouseEvent mouseEvent) {
        var uri = uriBox.getText();
        if(uri != null && uri.length() > 0){
            controller.subscribe(uri);
        }
        else {
            communication.error("No uri entered to subscribe to");
        }
    }

    /**
     * Unsubscribes from the currently selected variable
     * @param mouseEvent
     */
    @FXML
    private void unsubscribe(MouseEvent mouseEvent) {
        var selected = subscriptionTable.getSelectionModel().getSelectedItem();
        if(selected != null) {
            controller.unsubscribe(selected);
        }
        else {
            communication.error("No subscribed Item selected to unsubscribe from");
        }

    }


    /**
     * write to a subscribed variable.
     * The variable used is the one selected in the listview
     * The value is parsed from the textbox according to the type of the variable
     */
    @FXML
    private void write(MouseEvent mouseEvent) {
        var selected = subscriptionTable.getSelectionModel().getSelectedItem();
        if(selected != null){
            controller.write(selected, writeValueBox.getText());
        }
        else {
            communication.error("No item selected to write to");
        }

    }

}
