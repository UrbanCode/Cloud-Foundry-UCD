/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2015. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.air.plugin.cf.helper

import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils

import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import org.codehaus.jettison.json.JSONObject
import org.codehaus.jettison.json.JSONArray

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder
import com.urbancode.ud.client.UDRestClient

// provide additional functionality to UDRestClient to update component template properties
public class TemplateHelper extends UDRestClient {

    public TemplateHelper(def uri, def user, def password) {
        super(uri, user, password)
    }

    // create a component property on a given component template
    public def createTemplateProp(def templateName, def propName, def propLabel,
            def propType, def description, def values) {

        def payloadMap = ["name": propName, "label": propLabel, "type": propType,
            "description": description, "allowedValues": values]
        JsonBuilder builder = new JsonBuilder()
        builder(payloadMap)

        def templateId = getTemplateId(templateName)

        // put request to add a property to existing propDefs on the template
        HttpPut put = new HttpPut(url.toString() + "/property/propSheetDef/componentTemplates%26${templateId}%26propSheetDef.-1/propDefs")
        put.addHeader("Accept", "application/json")

        StringEntity input

        try {
            input = new StringEntity(builder.toString())
        }
        catch(UnsupportedEncodingException ex) {
            println("Unsupported characters in http request content: ${content}")
            throw ex
        }

        put.setEntity(input)

        // attempt to create property and update it only if it already exists
        HttpResponse response
        try {
            response = invokeMethod(put)
            println("New component property: ${propName} created on component template: ${templateName}")
        }
        catch (IOException ex) {
            // property already exists.. provide path using it's name to update it
            URI updateUri = new URI(put.getURI().toString() + "/${propName}")
            put.setURI(updateUri)
            invokeMethod(put)
            println("Updated existing component property: ${propName} on component template: ${templateName}")
        }
    }

    // parse through all component templates and return the id of the specified one
    public def getTemplateId(def templateName) {
        HttpGet getRequest = new HttpGet(url.toString() + "/cli/componentTemplate")
        HttpResponse response = invokeMethod(getRequest)

        def json = parseResponse(response)
        def id

        for (def template : json) {
            if (template.name == templateName) {
                id = template.id
            }
        }

        return id
    }

    // return json string from a generic HttpResponse
    public def parseResponse(HttpResponse response) {
        String json = EntityUtils.toString(response.getEntity())
        JsonSlurper slurper = new JsonSlurper()

        return slurper.parseText(json)
    }
}