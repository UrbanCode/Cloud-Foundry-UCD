/**
 * (c) Copyright IBM Corporation 2015, 2017. All Rights Reserved.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool
import com.urbancode.air.CommandHelper
import com.urbancode.air.plugin.cf.helper.ResourceHelper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.filefilter.DirectoryFileFilter

def apTool = new AirPluginTool(args[0], args[1])
def isWindows = apTool.isWindows
def props = apTool.getStepProperties()
def ch = new CommandHelper(new File(".").canonicalFile)

// ucd setup
def ucdUser = apTool.getAuthTokenUsername()
def ucdPass = apTool.getAuthToken()
def ucdUri = new URI(System.getenv("AH_WEB_URL"))
def ucdHelper = new ResourceHelper(ucdUri, ucdUser, ucdPass)

def cfHome = props['cfHome']?.trim()
if(!cfHome) {
    cfHome = System.getenv("CF_HOME")?.trim()
    if (cfHome) {
        println("[OK] Found environment variable 'CF_HOME=${cfHome}'.")
    }
}

def installPaths = []
def overridePath = props['overrideCommandPath']
def api = props['apiEndpoint']
def roleName = "CloudFoundryController"

/* API Endpoint must not include protocol */
api = api.replaceAll("^(http://|https://)", "")

// override path must be checked first if it exists
if (overridePath) {
    installPaths << overridePath
}
else if (cfHome) {
    installPaths << cfHome
}
else {
    if (isWindows) {
        installPaths << "C:" + File.separator + "Program Files" + File.separator + "CloudFoundry" + File.separator + "cf.exe"
        installPaths << "C:" + File.separator + "Program Files (x86)" + File.separator + "CloudFoundry" + File.separator + "cf.exe"
    }
    else {
        installPaths << "/opt/CloudFoundry/cf"
        installPaths << "/usr/share/CloudFoundry/cf"
    }
}

/* Search through all files and directories in installPaths */
def foundPath = ""
for (def path : installPaths) {
    File cfPath = new File(path)

    if (cfPath.exists()) {
        if (cfPath.isFile()) {
            foundPath = cfPath.getAbsolutePath()
            break
        }
        else if (cfPath.isDirectory()) {
            def files = FileUtils.listFiles(cfPath, new RegexFileFilter("cf|cf.exe"), DirectoryFileFilter.DIRECTORY)

            if (files) {
                foundPath = files[0].getAbsolutePath()
                break
            }
        }
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

    // parse out api endpoint from output
    def output = outputStream.toString()
    output = output.split("\r?\n")[0] // Get first line of output
    output = output.split(":")
    apiEndpoint = output[output.length - 1] // Isolate endpoint

    /* Remove beginning slashes */
    while (apiEndpoint.charAt(0) == '/') {
        apiEndpoint = apiEndpoint.substring(1, apiEndpoint.length())
    }
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

println("API IS " + apiEndpoint)
// create properties for the role
def roleProperties = new HashMap<String, String>()
roleProperties.put("cf.commandPath", foundPath)
roleProperties.put("cf.api", apiEndpoint)

ucdHelper.addRoleToResource(subResource, "CloudFoundryController", roleProperties)
