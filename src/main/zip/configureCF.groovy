/**
 * (c) Copyright IBM Corporation 2015, 2017. All Rights Reserved.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool
import com.urbancode.air.CommandHelper
import com.urbancode.air.plugin.cf.helper.CFHelper
import com.urbancode.air.plugin.cf.helper.ResourceHelper
import com.urbancode.air.plugin.cf.helper.TemplateHelper

def apTool = new AirPluginTool(this.args[0], this.args[1])
def props = apTool.getStepProperties()

def ucdUser = apTool.getAuthTokenUsername()
def ucdPass = apTool.getAuthToken()
def ucdUri = new URI(System.getenv("AH_WEB_URL"))
def resourceHelper = new ResourceHelper(ucdUri, ucdUser, ucdPass)
def templateHelper = new TemplateHelper(ucdUri, ucdUser, ucdPass)
def cfHelper = new CFHelper(props)

def rootPath = props['resourcePath']

def orgFolderPath = rootPath + "/Organizations"
def orgFolderDescription = "All existing organizations in the Cloud Foundry Controller's API Endpoint."
def orgFolder = resourceHelper.getOrCreateSubResource(orgFolderPath, rootPath, "Organizations", orgFolderDescription)

def organizations = cfHelper.getOrgs()
def applications = []

for (def org : organizations) {
    def orgPath = orgFolderPath + "/${org}"
    def orgDescription = "Cloud Foundry Organization with name " + org
    def orgResource = resourceHelper.getOrCreateSubResource(orgPath, orgFolderPath, org,  orgDescription)

    def orgProperties = new HashMap<String, String>()
    orgProperties.put("cf.org", org)

    resourceHelper.addRoleToResource(orgResource, "CloudFoundryOrganization", orgProperties)

    def spaceFolderPath = orgPath + "/Spaces"
    def spaceFolderDescription = "All existing spaces in the ${org} organization."
    def spaceFolder = resourceHelper.getOrCreateSubResource(spaceFolderPath, orgPath, "Spaces", spaceFolderDescription)

    def spaces = cfHelper.getSpaces(org)

    for (def space : spaces) {
        def spacePath = spaceFolderPath + "/${space}"
        def spaceDescription = "Cloud Foundry space within organization: " + org + " named " + space
        def spaceResource = resourceHelper.getOrCreateSubResource(spacePath, spaceFolderPath, space, spaceDescription)

        def spaceProperties = new HashMap<String, String>()
        spaceProperties.put("cf.space", space)

        resourceHelper.addRoleToResource(spaceResource, "CloudFoundrySpace", spaceProperties)

        // acquire applications per space
        def apps = cfHelper.getApplications(org, space)

        for (def app : apps) {
            app = app.trim()
            def appEntry = ["label": "${org}/${space}/${app}", "value": app]
            applications << appEntry
        }
    }
}

// create the property on the Cloud Foundry template
def propDescription = "The existing application on the Cloud Foundry server to manipulate."
templateHelper.createTemplateProp("Cloud Foundry", "cf.existingApp", "Existing Application",
    "SELECT", propDescription, applications)

println("Auto-Configuration completed successfully. ")
System.exit(0)
