//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.RscController;

import com.phoenixcontact.ade.commonremoting.CommonRemotingException;
import com.phoenixcontact.ade.commonremoting.utils.OutParam;
import com.phoenixcontact.arp.system.commons.services.io.*;
import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;
import com.phoenixcontact.rsc.demo.Helper.CustomFileSystemEntry;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 
 * @author andre niggemann
 *
 * Background Service for the FileService Tab
 * 
 */
public class FileService {

    private final CoreCommunication communication;
    private final IFileService fileService;
    private final IDirectoryService directoryService;

    //Is inserted into every folder, to mark it as unfoldable in the TreeView
    private final TreeItem<FileSystemEntry> dummyItem = new TreeItem<>(new FileSystemEntry());


    public FileService(ServiceManager sm, CoreCommunication communication) throws CommonRemotingException {
        this.communication = communication;

        fileService = sm.getService(IFileService.class);
        directoryService = sm.getService(IDirectoryService.class);
    }

    //Uploads a files and folders to the plc
    public synchronized void upload(String source, FileSystemEntry entry, boolean overwrite) {

       try{
            var sourceFile = new File(source);
            if(!sourceFile.exists()){
                communication.error("The file does not exist");
                return;
            }
            if (sourceFile.isFile()) {
                var path = entry.getPath();
                if(entry.isFile()){
                    //A file can only be overwritten if overwrite is set and the names match
                    if(!overwrite || !CustomFileSystemEntry.getName(entry).equals(sourceFile.getName())){
                        communication.error("The names must match to overwrite the file and the overwrite flag must be set");
                        return;
                    }
                }
                else
                    //When the destination is a folder the fileName is appended to the folder path
                    path = path + "/" + sourceFile.getName();

                try (FileInputStream data = new FileInputStream(sourceFile)) {

                    var error = fileService.write(path, overwrite, new TraitItem[0], data);

                    if (FileSystemError.NONE.equals(error)) {
                        communication.message("Uploading file successful");
                    } else {
                        communication.error("Upload unsuccessful");
                    }
                }
            }
            else if(entry.isDirectory() ){
                CustomFileSystemEntry nextEntry;
                if(sourceFile.getName().equals(CustomFileSystemEntry.getName(entry))){
                    nextEntry = (CustomFileSystemEntry)entry;
                }
                else{
                    var folderPath = entry.getPath() + "/" + sourceFile.getName();
                    directoryService.create(folderPath);
                    var newEntry = new CustomFileSystemEntry();
                    newEntry.setPath(folderPath);
                    newEntry.setDirectory(true);
                    newEntry.setFile(false);
                    nextEntry = newEntry;
                }

                for(var file : sourceFile.listFiles()){
                    upload(file.getPath(), nextEntry, overwrite);
                }

            }
            else
                communication.error("Cannot copy a folder into a file");

        } catch (Exception ex) {
            communication.error("Upload failed");
        }
    }

    //Downloads files and folders from the plc to the specified target.
    public synchronized void download(FileSystemEntry source, String target, boolean overwrite) {

        try{
            var targetFile = new File(target);
            if(targetFile.exists()){
                //If you want to overwrite a file the following conditions must not be met
                if(! overwrite){
                    communication.error("File already exists");
                    return;
                }
                else if(targetFile.isDirectory() ^ source.isDirectory()){
                    communication.error("Cannot override a file with a folder or vice vers");
                    return;
                }
                else if(targetFile.getName().equals(CustomFileSystemEntry.getName(source))){
                    communication.error("Names must match to overwrite");
                    return;
                }
            }

            if(source.isFile()){
                var traits = new Traits();

                var outFileStream = new OutParam<InputStream>();
                var outputTraits = new OutParam<TraitItem[]>();

                var result = fileService.read(traits, source.getPath(), outFileStream, outputTraits);

                if(FileSystemError.NONE.equals(result))
                    try (var dataStream = outFileStream.getValue()){
                    try (FileOutputStream output = new FileOutputStream(target, !overwrite)) {
                        dataStream.transferTo(output);
                    }}

                else
                    communication.error("Downloading file unsuccessful");

            }
            else { //If it is a folder create it, if it does not exist already and recursively download all files and folders
                if(!targetFile.exists())
                    targetFile.mkdir();
                var files = directoryService.enumerateFileSystemEntries(source.getPath(), "*", false);
                for(var file : files){
                    download(file, targetFile.getPath() + "\\" + CustomFileSystemEntry.getName(file), overwrite);
                }
            }
        }
        catch (Exception ex){
            communication.error("Download failed");
        }
    }

    //Removes filesystem entries with all it´s child nodes
    public synchronized void remove(FileSystemEntry entry){
        try{
            if(entry.isDirectory()){
                var result = directoryService.delete(entry.getPath());
                if(result.getValue() == FileSystemError.NONE.getValue())
                    communication.message("Removed the folder " + entry.getPath());
                else
                    communication.error("Failed to remove the folder " + entry.getPath());
            }
            else {
                var result = fileService.delete(entry.getPath());
                if(result.getValue() == FileSystemError.NONE.getValue())
                    communication.message("Removed the file " + entry.getPath());
                else
                    communication.error("Removing the file " + entry.getPath() + " failed");
            }
        }
        catch(Exception ex){
            communication.error("Removing " + entry + " failed");
        }

    }


    public synchronized void addFilesToNode(TreeItem<FileSystemEntry> node) {
        try{
            var path = node.getValue().getPath();
            if (node.getValue().isDirectory()){
                var children = node.getChildren();
                children.removeAll(children);
                for(var entry : directoryService.enumerateFileSystemEntries(path, "*", false)){
                    var treeItem = createTreeItem(entry);
                    children.add(treeItem);
                }
            }
        }
        catch (Exception ex){
            communication.error("Failed to expand tree");
        }
    }


    private TreeItem<FileSystemEntry> createTreeItem(FileSystemEntry entry) {
        var customEntry = new CustomFileSystemEntry();
        customEntry.setDirectory(entry.isDirectory());
        customEntry.setFile(entry.isFile());
        customEntry.setPath(entry.getPath());
        TreeItem<FileSystemEntry> treeItem = new TreeItem<>(customEntry);
        if(customEntry.isDirectory()){
            treeItem.getChildren().add(dummyItem);
            treeItem.expandedProperty().addListener(x -> addFilesToNode(treeItem));
        }
        return treeItem;
    }



    public synchronized void createFolder(FileSystemEntry entry, String name) {
        try {
            if(entry.isDirectory()) {
                var result = directoryService.create(entry.getPath() + "/" + name);
                if(result.getValue() == FileSystemError.NONE.getValue())
                    communication.message("Successfully created the folder");
                else
                    communication.error("Creating the folder failed");
            }
            else
                communication.error("Cannot create subfolder to a file");
        }
        catch (Exception ex){
            communication.error("Failed to create Folder " + name + " under " + entry.getPath());
        }
    }
}
