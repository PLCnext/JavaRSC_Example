//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import com.phoenixcontact.arp.system.commons.services.io.FileSystemEntry;



/**
 * Exists primarily to overwrite toString because it is called for the display in the tree view
 * 
 * @author andreniggemann
 *
 */
public class CustomFileSystemEntry extends  FileSystemEntry{
    @Override
    public String toString(){
        return getName(this);

    }

    /**
     * returns the name of the folder or file.
     * @param entry
     * The Filesystementry the name of should be computed.
     * @return
     * Returns the past after the last slash. 
     * If the path ends with a slash like root the slash is returned
     */
    public static String getName(FileSystemEntry entry){
        var path = entry.getPath();

        return path.substring(
                Math.min(path.lastIndexOf('/') + 1,
                        path.length() - 1));
    }
}
