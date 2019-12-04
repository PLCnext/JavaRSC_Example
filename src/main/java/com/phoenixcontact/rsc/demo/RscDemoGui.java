//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo;

import com.phoenixcontact.rsc.demo.View.Login;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.FileInputStream;

/**
 * The main class of this demo app
 * Functions as a luncher for the javafx ui.
 * 
 * @author andreniggemann
 *
 */
public class RscDemoGui extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Load Login form from fxml
        var loader = new FXMLLoader(
                RscDemoGui.class.getResource("/Login.fxml"));
        var page = (Parent) loader.load();
        var viewController = loader.<Login>getController();
        Scene scene = new Scene(page);

        //Preset ip, port, user, password from resources/config.txt
        var config = loadConfig();
        viewController.init(primaryStage, config[0], config[1], config[2], config[3]);

        //Display the login screen
        primaryStage.setTitle("RSC Demo App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String[] loadConfig() {
        //Load values for login from config.txt
        String[] config = {"", "", "", ""};
        var file = RscDemoGui.class.getResource("/config.txt").getFile();
        try( var inStream = new FileInputStream(file)) {

            var input = new String(inStream.readAllBytes()).split(",");

            for(int i = 0; i < Math.min(config.length, input.length); i++){
                config[i] = input[i].trim();
            }
        }
        catch (Exception ex){
        	System.out.println(ex.getMessage());
        }
        return config;
    }
}
