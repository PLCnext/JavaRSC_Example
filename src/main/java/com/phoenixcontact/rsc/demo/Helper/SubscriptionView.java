//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import com.phoenixcontact.ade.commonremoting.CoreType;
import com.phoenixcontact.ade.commonremoting.utils.AdeObject;

import java.math.BigInteger;

/**
 * Holds all relevant Information about a subscription.
 * @author andreniggemann
 *
 */
public class SubscriptionView {

    private final String uri;
    private AdeObject object;

    public SubscriptionView(String uri, AdeObject value){
        this.uri = uri;
        this.object = value;
    }

    public CoreType getType() {
        return object.getCoreType();
    }
    public String getUri() {
        return  uri;
    }
    public AdeObject getObject(){
        return object;
    }
    public Object getValue() {
        return object.getValue();
    }


    public void setObject(AdeObject object){
        this.object = object;
    }

    @Override
    public String toString(){
        var sb = new StringBuilder();
        sb.append(uri);
        sb.append(", ");
        sb.append(object.getCoreType());
        sb.append(", ");
        sb.append(object.getValue());
        return sb.toString();
    }


    /**
     * Tries to parse the string provided in valueString to an object matching the CoreType.
     * In case the parsing fails, null will be returned to the caller.
     * @param type
     * @param valueString
     * @return
     */

    public static Object parseToCoreType(CoreType type, String valueString){
        try{
            switch(type){
                case BOOLEAN:
                    var lowercase = valueString.toLowerCase();
                    var isTrue = lowercase.equals("true");
                    var isFalse = lowercase.equals("false");
                    return isTrue | isFalse ? isTrue : null;
                case CHAR:
                    return valueString.length() == 1 ? valueString.charAt(0) : null;
                case I1:
                    return Byte.parseByte(valueString);
                case U1:
                    var s = Short.parseShort(valueString);
                    return s < 0 || s >= (1 << 8) ? null : s;
                case I2:
                    return Short.parseShort(valueString);
                case U2:
                    var i = Integer.parseInt(valueString);
                    return i < 0 || i >= (1 << 16) ? null : i;
                case I4:
                    return Integer.parseInt(valueString);
                case U4:
                    var l = Long.parseLong(valueString);
                    return l < 0 || l >= (1L << 32) ? null : l;
                case I8:
                    return Long.parseLong(valueString);
                case U8:
                    var b = new BigInteger(valueString);
                    return b.compareTo(BigInteger.ZERO) == -1 || b.compareTo(BigInteger.ONE.shiftLeft(64)) != -1 ? null : b;
                case R4:
                    return Float.parseFloat(valueString);
                case R8:
                    return  Double.parseDouble(valueString);
                case STRING:
                    return valueString;
                default:
                    return null;
            }
        } catch (NumberFormatException ex){
            return null;
        }


    }
}
