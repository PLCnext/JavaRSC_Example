//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View;

import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.SecureConnectionInfo;
import com.phoenixcontact.rsc.demo.RscDemoGui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;



/**
 * 
 * @author andreniggemann
 * 
 * The view controller for the login screen loaded from Login.fxml
 * After the login button is clicked, this page is responsible for
 * loading the Main Screen. This can only proceed, when a connection
 * to a controller is established.
 *
 */
public class Login {
	
    @FXML TextField ipField;

    @FXML TextField portField;

    @FXML TextField userField;

    @FXML
    PasswordField passwordField;

    ServiceManager sm;
    Stage stage;

    /**
     * Get initial values for the textboxes
     * 
     * @param stage
     * @param ip
     * @param port
     * @param user
     * @param password
     */
    public void init(Stage stage, String ip, String port, String user, String password){
        this.stage = stage;

        ipField.setText(ip);
        portField.setText(port);
        userField.setText(user);
        passwordField.setText(password);
    }

    /**
     * Handles the login Button click
     * 
     * @param event
     * 
     */
    @FXML
    private void loginClicked(MouseEvent event) {
        try {
            var maybeInfo = SecureConnectionInfo.tryCreate(
                    ipField.getText(),
                    portField.getText(),
                    userField.getText(),
                    passwordField.getText());

            if (maybeInfo.isPresent()) {
                var sci = maybeInfo.get();
                var loader = new FXMLLoader(
                        RscDemoGui.class.getResource("/Main.fxml"));
                var page = (Parent) loader.load();
                var viewController = loader.<MainViewController>getController();

                Scene scene = new Scene(page);
                viewController.init(stage, sci);



                stage.setScene(scene);
                stage.show();
            }
        }
        catch (Exception ex){
            System.out.println(ex.getCause());
        }

    }




}
