/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2015, 2016. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool
import com.urbancode.air.CommandHelper
import com.urbancode.air.plugin.cf.helper.ResourceHelper

def apTool = new AirPluginTool(args[0], args[1])
def isWindows = apTool.isWindows
def props = apTool.getStepProperties()
def ch = new CommandHelper(new File(".").canonicalFile)

// ucd setup
def ucdUser = apTool.getAuthTokenUsername()
def ucdPass = apTool.getAuthToken()
def ucdUri = new URI(System.getenv("AH_WEB_URL"))
def ucdHelper = new ResourceHelper(ucdUri, ucdUser, ucdPass)

def standardInstallPaths = []
def overridePath = props['overrideCommandPath']
def api = props['apiEndpoint']
def roleName = "CloudFoundryController"

// override path must be checked first if it exists
if (overridePath) {
    standardInstallPaths << overridePath
}

if (isWindows) {
    standardInstallPaths << "C:" + File.separator + "Program Files" + File.separator + "CloudFoundry" + File.separator + "cf.exe"
    standardInstallPaths << "C:" + File.separator + "Program Files (x86)" + File.separator + "CloudFoundry" + File.separator + "cf.exe"
}
else {
    standardInstallPaths << "/opt/CloudFoundry/cf"
    standardInstallPaths << "/usr/share/CloudFoundry/cf"
}

def foundPath = ""
for (def path : standardInstallPaths) {
    File cfPath = new File(path)

    if (cfPath.exists() && cfPath.isFile()) {
        foundPath = cfPath.getAbsolutePath()
        break
    }
}

if (foundPath) {
    println("Found cf command line tool at " + foundPath)
}
else {
    // discovery process cannot continue without cf executable
    println("Cannot find cf command line executable tool. Ending discovery process.")
    System.exit(0)
}

def apiEndpoint = ""

def apiSet = {
    it.out.close() // close stdin
    def out = new PrintStream(System.out, true)
    def outputStream = new StringBuilder()
    try {
        it.waitForProcessOutput(outputStream, out)
    }
    finally {
        out.flush();
    }

    // parse out url from output
    int beginIndex = outputStream.indexOf(':') + 2
    outputStream.delete(0, beginIndex)
    int endIndex = outputStream.indexOf(' ')
    outputStream.delete(endIndex, outputStream.length())

    apiEndpoint = outputStream.toString()
}

if (api) {
    apiEndpoint = api
}
else {
    // acquire api endpoint if not already specified
    def cmdArgs = [foundPath, "api"]
    int exitVal = ch.runCommand("Acquiring API Endpoint...", cmdArgs, apiSet)

    if (exitVal != 0) {
        println("Could not acquire an API Endpoint. This must be set manually instead.")
    }
}

// create the resource in ucd
def rootResourcePath = props['resourcePath']
def subResourcePath = rootResourcePath + "/CloudFoundryController - " + apiEndpoint
def subResourceDescription = "Cloud Foundry Controller with API endpoint " + apiEndpoint
def subResourceName = subResourcePath.substring(subResourcePath.lastIndexOf("/CloudFoundryController") + 1)
def subResource = ucdHelper.getOrCreateSubResource(subResourcePath, rootResourcePath, subResourceName, subResourceDescription)

// create properties for the role
def roleProperties = new HashMap<String, String>()
roleProperties.put("cf.commandPath", foundPath)
roleProperties.put("cf.api", apiEndpoint)

ucdHelper.addRoleToResource(subResource, "CloudFoundryController", roleProperties)