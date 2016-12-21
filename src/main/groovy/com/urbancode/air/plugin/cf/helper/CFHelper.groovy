/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2015, 2016. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.air.plugin.cf.helper

import com.urbancode.air.CommandHelper
import com.urbancode.air.ExitCodeException

class CFHelper {
    CommandHelper helper
    final def workDir = new File('.').canonicalFile
    boolean isAuthenticated
    def cfFile
    def props
    def api
    def username
    def password
    def selfSigned
    def organization
    def space
    def cfHome
    def interpreter

    final def isWindows = System.getProperty('os.name').contains("Windows")

    CFHelper(def props) {
        this.props = props
        helper = new CommandHelper(workDir)
        cfFile = props['commandPath']?.trim() ?: "cf"
        api = props['api']?.trim()
        username = props['user']?.trim()
        password = props['password']
        selfSigned = props['selfSigned'] ? props['selfSigned'].toBoolean() : ""
        organization = props['org']?.trim()
        space = props['space']?.trim()
        cfHome = props['cfHome']?.trim()
        interpreter = props['interpreter']
    }

    void bindService() {
        def application = props['application']
        def service = props['service']

        setupEnvironment(api, organization, space)

        // Execute bind-service
        def commandArgs = [
            cfFile,
            "bind-service",
            application,
            service
        ]

        runHelperCommand("[Action] Executing CF bind-service", commandArgs)
    }

    void createDomain() {
        def domain = props['domain']

        setupEnvironment(api, organization, space)

        // Execute create-domain
        def commandArgs = [
            cfFile,
            "create-domain",
            organization,
            domain
        ]

        runHelperCommand("[Action] Executing CF create-domain", commandArgs)
    }

    void createOrUpdateUserProvidedService() {
        def service         = props['service'].trim()
        def credentials     = props['credentials'].trim()
        def logDrainURL     = props['logDrainURL'].trim()
        def routeServiceURL = props['routeServiceURL'].trim()

        setupEnvironment(api, organization, space)

        def services = getServices()
        def command = ""
        if (!services.contains(service)) {
            println "[Ok] Service not found. Creating the user-provided service..."
            command = "create-user-provided-service"
        }
        else {
            println "[Ok] Service found. Updating the user-provided service..."
            command = "update-user-provided-service"
        }

        // Execute create-user-provided-service or update-user-provided service
        def commandArgs = [
            cfFile,
            command,
            service,
            "-p",
            credentials
        ]

        if (logDrainURL) {
            commandArgs << "-l"
            commandArgs << logDrainURL
        }

        if (routeServiceURL) {
            commandArgs << "-r"
            commandArgs << routeServiceURL
        }

        runHelperCommand("[Action] Executing CF ${command}", commandArgs)
    }

    void createSubdomain() throws Exception{
        def subdomain = props['subdomain']
        def domain = props['domain']

        setupEnvironment(api, organization, space)

        // Execute create-domain
        def commandArgs = [
            cfFile,
            "create-domain",
            organization,
            subdomain + "." + domain
        ]

        runHelperCommand("[Action] Executing CF create-domain", commandArgs)
    }

    void createRoute() {
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment(api, organization, space)

        // Execute create-route
        def commandArgs = [
            cfFile,
            "create-route",
            space,
            domain
        ]

        if (hostname) {
            commandArgs << "-n"
            commandArgs << hostname
        }

        runHelperCommand("[Action] Executing CF create-route", commandArgs)
    }

    void createService() {
        def serviceName = props['service']
        def serviceType = props['serviceType']
        def plan = props['plan']

        setupEnvironment(api, organization, space)

        // Execute create-service
        def commandArgs = [
            cfFile,
            "create-service",
            serviceType,
            plan,
            serviceName
        ]

        runHelperCommand("[Action] Executing CF create-service", commandArgs)
    }

    void deleteApp() {
        def app = props['application']
        def deleteRoutes = props['delete_Routes']

        setupEnvironment(api, organization, space)

        // Execute delete-app
        def commandArgs = [cfFile, "delete", app, "-f"]

        if (deleteRoutes == "true") {
            commandArgs << "-r"
        }

        runHelperCommand("[Action] Executing CF delete-app", commandArgs)
    }

    void deleteDomain() {
        def domain = props['domain']

        setupEnvironment(api, organization, space)

        // Execute delete-domain
        def commandArgs = [
            cfFile,
            "delete-domain",
            domain,
            "-f"
        ]

        runHelperCommand("[Action] Executing CF delete-domain", commandArgs)
    }

    void deleteRoute() {
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment(api, organization, space)

        // Execute delete-route
        def commandArgs = [
            cfFile,
            "delete-route",
            domain
        ]

        if (hostname) {
            commandArgs << "-n"
            commandArgs << hostname
        }

        commandArgs << "-f"

        runHelperCommand("[Action] Executing CF delete-route", commandArgs)
    }

    void deleteService() {
        def name = props['name']

        setupEnvironment(api, organization, space)

        // Execute delete-service
        def commandArgs = ["cf", "delete-service", name]

        commandArgs << "-f"

        runHelperCommand("[Action] Executing CF delete-service", commandArgs)
    }

    void deleteSubdomain() {
        def subdomain = props['subdomain']
        def domain = props['domain']

        setupEnvironment(api, organization, space)

        // Execute delete-domain
        def commandArgs = [
            "cf",
            "delete-domain",
            subdomain + "." + domain
        ]

        commandArgs << "-f"

        runHelperCommand("[Action] Executing CF delete-domain", commandArgs)
    }

    void executeCFScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment(api, organization, space)

        def scriptName = ""
        def commandArgs = []
        if (isWindows) {
            scriptName = "cfScript.bat"
            commandArgs = ["cmd", "/C", scriptName]
        }
        else {
            scriptName = "cfScript.sh"
            commandArgs = ["sh", "-c", "bash " + scriptName]
        }
        File cfScript = new File(scriptName)
        cfScript.deleteOnExit()
        cfScript << script

        for (arg in args.split("\\s+")) {
            commandArgs << arg
        }

        if (!isWindows) {
            def chmod = ['chmod', '+x', scriptName]
            runHelperCommand('[Action] Making the runtime script executable...', chmod)
        }
        runHelperCommand("[Action] Executing CF commands", commandArgs)
    }

    void executeBashScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment(api, organization, space)

        // Execute script
        def commandArgs = []
        commandArgs = ["sh", "-c"]
        commandArgs << "bash " + script

        for (arg in args.split("\\s+")) {
            commandArgs << arg
        }

        runHelperCommand("[Action] Executing CF script", commandArgs)
    }

    void mapRoute() {
        def domain = props['domain']
        def hostname = props['hostname']
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute map-route
        def commandArgs = [
            cfFile,
            "map-route",
            app,
            domain
        ]

        if (hostname) {
            commandArgs << "-n"
            commandArgs << hostname
        }

        runHelperCommand("[Action] Executing CF map-route", commandArgs)
    }

    void pushApplication() {
        def appName = props['appName']
        def file = props['path']
        def manifest = props['manifest']
        def domain = props['domain']
        def subdomain = props['subdomain']
        def instances = props['instances']
        def memory = props['memory']
        def disk = props['disk']
        def buildpack = props['buildpack']
        def stack = props['stack']
        def timeout = props['timeout']
        def nostart = props['nostart']
        def noroute = props['noroute']
        def nomanifest = props['nomanifest']
        def nohostname = props['nohostname']
        def randomroute = props['randomroute']

        setupEnvironment(api, organization, space)

        // Push the application
        def commandArgs = [cfFile, "push"]

        if (appName) {
            commandArgs << appName
        }


        if (buildpack) {
            commandArgs << "-b"
            commandArgs << buildpack
        }

        if (manifest) {
            File manifestPath = new File(workDir, manifest)

            commandArgs << "-f"
            if (manifestPath.exists() && manifestPath.isFile()) {
                commandArgs << manifestPath.absolutePath
            }
            else {
                commandArgs << manifest
            }
        }

        if (instances) {
            commandArgs << "-i"
            commandArgs << instances
        }

        if (memory) {
            commandArgs << "-m"
            commandArgs << memory
        }

        if (disk) {
            commandArgs << "-k"
            commandArgs << disk
        }

        if (file) {
            File filePath = new File(workDir, file)

            commandArgs << "-p"
            if (filePath.exists() && filePath.isFile()) {
                commandArgs << filePath.absolutePath
            }
            else {
                commandArgs << file
            }
        }

        if (domain) {
            commandArgs << "-d"
            commandArgs << domain
        }

        if (subdomain) {
            commandArgs << "-n"
            commandArgs << subdomain
        }

        if (stack) {
            commandArgs << "-s"
            commandArgs << stack
        }

        if (timeout) {
            commandArgs << "-t"
            commandArgs << timeout
        }

        if (nostart == "true") {
            commandArgs << "--no-start"
        }

        if (noroute == "true") {
            commandArgs << "--no-route"
        }

        if (nomanifest == "true") {
            commandArgs << "--no-manifest"
        }

        if (nohostname == "true") {
            commandArgs << "--no-hostname"
        }

        if (randomroute == "true") {
            commandArgs << "--random-route"
        }

        runHelperCommand("[Action] Deploying CloudFoundry application", commandArgs)
    }

    void restartApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute restart application
        def commandArgs = [cfFile, "restart", app]
        runHelperCommand("[Action] Executing CF restart APP", commandArgs)
    }

    void startApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute start application
        def commandArgs = [cfFile, "start", app]
        runHelperCommand("[Action] Executing CF start APP", commandArgs)
    }

    void stopApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute stop application
        def commandArgs = [cfFile, "stop", app]

        runHelperCommand("[Action] Executing CF stop APP", commandArgs)
    }

    void unbindService() {
        def service = props['service']
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute unbind-service
        def commandArgs = [
            cfFile,
            "unbind-service",
            app,
            service
        ]

        runHelperCommand("[Action] Executing CF unbind-service", commandArgs)
    }

    void unmapRoute() {
        def app = props['application']
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment(api, organization, space)

        // Execute unmap-route
        def commandArgs = [cfFile, "unmap-route", app]

        if (domain) {
            commandArgs << domain
        }

        if (hostname) {
            commandArgs << "-n"
            commandArgs << hostname
        }

        runHelperCommand("[Action] Executing CF unmap-route", commandArgs)
    }

    // set the api, organization, and space targets for the cf executable
    void setupEnvironment(def api, def organization, def space) {
        // Setup path
        def curPath = System.getenv("PATH")
        def pluginHome = new File(System.getenv("PLUGIN_HOME"))

        println "[Action] Setup of path using plugin home: " + pluginHome
        def binDir = new File(pluginHome, "bin")
        def newPath = curPath+":"+binDir.absolutePath
        helper.addEnvironmentVariable("PATH", newPath)

        // change location of config.json file
        def cfHomeDir
        if(cfHome) {
            cfHomeDir = new File(cfHome)
        }
        else {
            cfHomeDir = new File(props['PLUGIN_INPUT_PROPS']).parentFile
        }

        if (!cfHomeDir.exists() && !cfHomeDir.isDirectory()) {
            throw new ExitCodeException("[Error] The CF_HOME directory '${cfHomeDir.toString()}' does not exist.")
        }

        println ("Setting CF_HOME to '${cfHomeDir}'")
        helper.addEnvironmentVariable("CF_HOME", cfHomeDir.absolutePath)

        // Set cf api
        def commandArgs = [cfFile, "api", api]

        if (selfSigned) {
            commandArgs << "--skip-ssl-validation"
        }

        runHelperCommand("[Action] Setting cf target api", commandArgs)

        // Authenticate with username and password
        if (!isAuthenticated && (username && password)) {
            if (organization && space) {
                commandArgs = [
                    cfFile,
                    "auth",
                    username,
                    password
                ]

                runHelperCommand("[Action] Authenticating with CloudFoundry", commandArgs, false)
            }
            else {
                commandArgs = [
                    cfFile,
                    "login",
                    "-u",
                    username,
                    "-p",
                    password
                ]

                runHelperCommand("[Action] Logging into CloudFoundry", commandArgs, false)
            }
            isAuthenticated = true
        }
        else {
            println "[Ok] Skipping Cloud Foundry authentication because the Username and/or Password properties are empty or authentication already occurred."
        }

        if (organization) {
            // Set target org
            commandArgs = [
                cfFile,
                "target",
                "-o",
                organization
            ]
            runHelperCommand("[Action] Setting CloudFoundry target organization", commandArgs)
        }

        if (space) {
            // Ensure space exists. create-space does nothing if space exists
            commandArgs = [
                cfFile,
                "create-space",
                space
            ]
            runHelperCommand("[Action] Creating CloudFoundry space", commandArgs)

            // Set target space
            commandArgs = [
                cfFile,
                "target",
                "-s",
                space
            ]
            runHelperCommand("[Action] Setting CloudFoundry target space", commandArgs)
        }
    }

    // return a list of all services. api, org, and space must be initialized elsewhere
    def getServices() {
        def commandArgs = [cfFile, "services"]
        def services = getServiceOutput(commandArgs)

        return services
    }

    /* methods used only during auto-discovery.. logout occurs after discovery is finished */

    // return a list of all organizations in the specified api endpoint
    def getOrgs() {
        setupEnvironment(api, null, null)
        def commandArgs = [cfFile, "orgs"]
        def unparsedOrgs = getOutput(commandArgs)
        def orgs = parseOrgsAndSpaces(unparsedOrgs)

        return orgs
    }

    // return a list of all spaces in the specified organization
    def getSpaces(def org) {
        setupEnvironment(api, org, null)
        def commandArgs = [cfFile, "spaces"]
        def unparsedSpaces = getOutput(commandArgs)
        def spaces = parseOrgsAndSpaces(unparsedSpaces)

        return spaces
    }

    // return all applications in the specified org and space
    def getApplications(def org, def space) {
        setupEnvironment(api, org, space)
        def commandArgs = [cfFile, "apps"]
        def unparsedApps = getOutput(commandArgs)

        return parseApps(unparsedApps)
    }

    // parse and return the output of organizations or spaces into lists
    def parseOrgsAndSpaces(def output) {
        def objects = []
        def outputLines = output.tokenize('\n')

        // remove first line of irrelevant output
        outputLines.remove(0)

        for (def object : outputLines) {
            object = object.trim()
            if (object != "name" && object != "") {
                objects.add(object)
            }
        }

        return objects
    }

    // return apps output as a list of application names
    def parseApps(def output) {
        def apps = []
        def outputLines = output.tokenize('\n')

        // length of longest application name formats the output
        def appLength = outputLines[2].indexOf("requested state")

        // remove all lines of irrelevant output
        outputLines.removeRange(0, 3)

        for (def line : outputLines) {
            def app = line.substring(0, appLength)
            apps << app
        }

        return apps
    }

    // run command and return output
    def getOutput(def cmdArgs) {
        def output

        def setOutput = {
            it.out.close() // close stdin
            def out = new PrintStream(System.out, true)
            def outputStream = new StringBuilder()
            try {
                it.waitForProcessOutput(outputStream, out)
            }
            catch (IOException ex) {
                println "[Error] I/O Error found when retrieving the command output. Please review the output log."
                ex.printStackTrace()
                System.exit(1)
            }
            catch (Exception ex) {
                println "[Error] Unknown error found when retrieving the command output. Please review the output log."
                ex.printStackTrace()
                System.exit(1)
            }
            finally {
                out.flush();
            }

            output = outputStream.toString()
        }

        println "----------------------------------------------"
        helper.runCommand("[Action] Running command: ${cmdArgs.join(' ')}", cmdArgs, setOutput)
        println "----------------------------------------------"

        return output
    }

    // run command and return service output (Get's element 0 on each line of the output)
    def getServiceOutput(def cmdArgs) {
        def output = []

        def setOutput = {
            it.out.close() // close stdin
            try {
                // Look at each line, get the first word which is the service's name
                it.in.eachLine { line ->
                    println line
                    output << line.split("\\s+")[0]
                }
            }
            catch (IOException ex) {
                println "[Error] I/O Error found when retrieving the Service list. Please review the output log."
                ex.printStackTrace()
                System.exit(1)
            }
            catch (Exception ex) {
                println "[Error] Unknown found when retrieving the Service list. Please review the output log."
                ex.printStackTrace()
                System.exit(1)
            }
            finally {
                it.waitFor()
                // Remove all output that is not a service name
                output.remove(3) // Getting services in org <org> / space <space> as <username>...
                output.remove(2) // OK
                output.remove(1) // <new line>
                output.remove(0) // No service found || name    service     plan    bound apps  last operation
                println "Service Output: ${output}"
            }
        }

        println "----------------------------------------------"
        helper.runCommand("[Action] Running command: ${cmdArgs.join(' ')}", cmdArgs, setOutput)
        println "----------------------------------------------"
        println "[Ok] Service Names Found: ${output}"
        return output
    }

    // logout of the Cloud Foundry Controller
    void logout() {
        if (isAuthenticated) {
            def commandArgs = [cfFile, "logout"]
            runHelperCommand("[Action] Logout from CloudFoundry system", commandArgs)
            isAuthenticated = false
        }
        else {
            println "[Warning] The user is still authenticated with Cloud Foundry."
        }
    }

    def runHelperCommand(def message, def baseArgs) {
        runHelperCommand(message, baseArgs, true)
    }

    def runHelperCommand(def message, def baseArgs, Boolean usePrefix) {
        def commandArgs = []
        if (interpreter && usePrefix) {
            commandArgs = ["sh", "-c"]
            commandArgs << baseArgs.join(" ")
        }
        else {
            commandArgs = baseArgs
        }

        try {
            println "----------------------------------------------"
            helper.runCommand(message, commandArgs)
            println "----------------------------------------------"
        } catch(ExitCodeException e){
            def errorMessage = "[Error] An error occurred while running the following command: ${commandArgs}"
            throw new ExitCodeException(errorMessage, e)
        }
    }
}
