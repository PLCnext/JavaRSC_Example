//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import com.phoenixcontact.arp.system.rsc.ConnectionInfo;
import com.phoenixcontact.arp.system.rsc.SecurityInfo;

import java.util.Arrays;
import java.util.Optional;

/**
 * Combination of ConnectionInfo and SecurityInfo to pass it around conveniently
 * 
 * @author andreniggemann
 *
 */
public class SecureConnectionInfo {

    private ConnectionInfo connectionInfo;
    private SecurityInfo securityInfo;

    /**
     * Checks if the arguments are valid arguments to establish a connection.
     * 
     * @param ip
     * @param port
     * @param userName
     * @param password
     * @return
     * Returns Some of a new SecureConnection Info when the arguments were valid, else none.
     */
    public static Optional<SecureConnectionInfo> tryCreate(String ip, String port, String userName, String password){
        try {
            var portNr = Integer.parseInt(port); //throws if port is not parseable to an int
            var connectTimeout = 10000;
            var readTimeout = 10000;

            //TODO check for valid ip adress etc.

            var connectionInfo = new ConnectionInfo(ip, portNr, connectTimeout, readTimeout);
            var securityInfo = new SecurityInfo(userName, password.toCharArray());
            var sci = new SecureConnectionInfo(connectionInfo, securityInfo);

            return Optional.of(sci);
        }
        catch(Exception ex) {
            return Optional.empty();
        }
    }


    private SecureConnectionInfo(ConnectionInfo connectionInfo, SecurityInfo securityInfo){
        this.connectionInfo = connectionInfo;
        this.securityInfo = securityInfo;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public SecurityInfo getSecurityInfo() {
        return securityInfo;
    }

    /**
     * Override the password field with zeros to remove the clear text password from memory.
     */
	public void clean(){
        Arrays.fill(securityInfo.getPassword(), '\0');
    }
}
