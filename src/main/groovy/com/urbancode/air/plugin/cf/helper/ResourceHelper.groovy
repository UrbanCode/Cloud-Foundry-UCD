/**
 * (c) Copyright IBM Corporation 2015, 2017. All Rights Reserved.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.air.plugin.cf.helper

import com.urbancode.ud.client.ResourceClient

class ResourceHelper {
    final def MAX_RETRIES = 3
    def udClient

    public ResourceHelper(def uri, def user, def password) {
        udClient = new ResourceClient(uri, user, password)
    }

    def getOrCreateSubResource(def resourcePath, def parentPath, def resourceName, def description) {
        def resource = null
        def tries = 0
        def done = false
        while (!done && tries < MAX_RETRIES) {
            try {
                try {
                    resource = udClient.getResourceByPath(resourcePath)
                }
                catch (IOException e) {
                    // resource does not exist
                    resource = null
                }
                if (resource == null) {
                    def parentResource = udClient.getResourceByPath(parentPath)
                    def parentResourceIdString = parentResource.getString("id")
                    def parentResourceId = UUID.fromString(parentResourceIdString)
                    def nodeId = udClient.createSubResource(parentResourceId, resourceName, description)
                    resource = udClient.getResourceByPath(resourcePath)
                }
                println("Resource created successfully: " + resourcePath)
                done = true
            }
            catch (IOException e) {
                tries++;
                done = false
                println("Got IOException in getOrCreateSubResource for ${resourcePath}")
                if (tries < MAX_RETRIES) {
                    println ("Retrying...")
                }
                else {
                    throw e
                }
            }
        }

        return UUID.fromString(resource.getString("id"));
    }

    void addRoleToResource(def resource, def role, def properties) {
        def tries = 0
        def done = false
        while (!done && tries < MAX_RETRIES) {
            try {
                udClient.setRoleOnResource(resource.toString(), role, properties)
                done = true
            } catch (IOException e) {
                tries++
                done = false
                println("Got IOException in addRoleToResource for ${resource}")
                if (tries < MAX_RETRIES) {
                    println ("Retrying...")
                } else {
                    throw e
                }
            }
        }
    }

    def getResource(def resourcePath) {
        def resource = udClient.getResourceByPath(resourcePath)

        return resource
    }

    void setResourceOnProperty(def resourcePath, def propName, def propValue) {
        udClient.setResourceProperty(resourcePath, propName, propValue)
    }

}
