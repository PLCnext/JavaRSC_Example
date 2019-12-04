//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View.Tabs;

import com.phoenixcontact.arp.system.commons.services.io.FileSystemEntry;
import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;
import com.phoenixcontact.rsc.demo.Helper.CustomFileSystemEntry;
import com.phoenixcontact.rsc.demo.RscController.FileService;
import com.phoenixcontact.rsc.demo.View.TabController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;


/**
 * 
 * @author andreniggemann
 * 
 * 
 * The view controller for the file service tab loaded from FileServiceTab.fxml
 * 
 *
 */
public class FileServiceTab implements TabController {
    @FXML
    CheckBox overrideBox;
    @FXML
    TextField sourceBox;

    @FXML TextField destinationBox;

    @FXML TextField folderNameBox;


    @FXML
    TreeView<FileSystemEntry> explorer;
    TreeItem<FileSystemEntry> rootItem;

    FileService controller;
    CoreCommunication communication;

    @FXML
    private void initialize() {
        var entry = new CustomFileSystemEntry();
        entry.setPath("/");
        entry.setDirectory(true);
        entry.setFile(false);
        rootItem = new TreeItem<>(entry);
    }

    @Override
    public void init(ServiceManager sm, CoreCommunication communication) throws Exception{
        this.communication = communication;
        controller = new FileService(sm, communication);
        controller.addFilesToNode(rootItem);
        explorer.setRoot(rootItem);
        rootItem.expandedProperty().addListener(x -> controller.addFilesToNode(rootItem));

        //Expand path to /opt/plcnext when present
        rootItem.setExpanded(true);
        rootItem.getChildren()
                .stream()
                .filter(x -> x.getValue().getPath().equals("/opt"))
                .findFirst()
                .ifPresent(x -> {
                    x.setExpanded(true);
                    x.getChildren()
                            .stream()
                            .filter(y -> y.getValue().getPath().equals("/opt/plcnext"))
                            .findFirst().ifPresent(y -> y.setExpanded(true));
                });
    }

    /**
     * Trigger upload from host pc to the plc
     * The path to the file on the host is specified in the sourceBox
     * The element to upload to is specified in the treeview. If it is a folder the file is added below with the original name.
     * If it is a file, the file needs to have the same name and the overwrite must be marked
     * Uploading folders is currently not supported.
     * @param event
     */
    @FXML
    private void upload(MouseEvent event){
        var selectedItem = explorer.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            communication.error("No item selected to copy to");
            return;
        }

       var source = sourceBox.getText();

        if(source.length() == 0){
            communication.error("The supplied path must not be empty");
            return;
        }

        controller.upload(source, selectedItem.getValue(), overrideBox.isSelected());
        refresh(selectedItem);


    }

    /**
     * Trigger download from plc to host pc
     * @param mouseEvent
     */
    @FXML
    private void download(MouseEvent mouseEvent) {
        var selectedItem = explorer.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            communication.error("No item selected to copy to");
            return;
        }

        var destination = destinationBox.getText();

        if(destination.length() == 0){
            communication.error("The supplied path must not be empty");
            return;
        }

        controller.download(selectedItem.getValue(), destination, overrideBox.isSelected());
    }

    /**
     * Start folder creation on the plc
     * The name of the new folder is specified in the folderNameBox
     * The folder is created as a subfolder to the folder selected in the tree view
     * @param event
     */
    @FXML
    private void createFolder(MouseEvent event){
        var selectedItem = explorer.getSelectionModel().getSelectedItem();
        //there is a item selected
        if (selectedItem != null){
            var entry = selectedItem.getValue();
            var name = folderNameBox.getText();
            if(name.length() > 0) {
                controller.createFolder(entry, name);
                //refresh
                refresh(selectedItem);
            }
            else
                communication.error("Name box may not be empty");
        }
        else
            communication.error("Select a file in the explorer for removal");
    }

    /**
     * Refresh treeview below an entry
     * @param entry
     */
    private void refresh(TreeItem<?> entry){
        if(entry.isExpanded()){
            entry.setExpanded(false);
            entry.setExpanded(true);
        }
    }

    /**
     * remove selected file or folder from the plc.
     * @param mouseEvent
     */
    @FXML
    private void remove(MouseEvent mouseEvent){
        var selectedItem = explorer.getSelectionModel().getSelectedItem();
        if (selectedItem != null){
            var entry = selectedItem.getValue();
            controller.remove(entry);

            //refresh
            refresh(selectedItem.getParent());
        }
        else
            communication.error("Select a file in the explorer for removal");
    }


    @Override
    public void clean() {
        //No special clean up necessary here
    }
}
