/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2015. All Rights Reserved.
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

    CFHelper(def props) {
        this.props = props
        helper = new CommandHelper(workDir)
        cfFile = props['commandPath'] ?: "cf"
        api = props['api']
        username = props['user']
        password = props['password']
        selfSigned = props['selfSigned'] ? props['selfSigned'].toBoolean() : ""
        organization = props['org']
        space = props['space']
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

        runHelperCommand("Executing CF bind-service", commandArgs)
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

        runHelperCommand("Executing CF create-domain", commandArgs)
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

        runHelperCommand("Executing CF create-domain", commandArgs)
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

        runHelperCommand("Executing CF create-route", commandArgs)
    }

    void createService() {
        def name = props['name']
        def service = props['service']
        def plan = props['plan']

        setupEnvironment(api, organization, space)

        // Execute create-service
        def commandArgs = [
            cfFile,
            "create-service",
            service,
            plan,
            name
        ]

        runHelperCommand("Executing CF create-service", commandArgs)
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

        runHelperCommand("Executing CF delete-app", commandArgs)
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

        runHelperCommand("Executing CF delete-domain", commandArgs)
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

        runHelperCommand("Executing CF delete-route", commandArgs)
    }

    void deleteService() {
        def name = props['name']

        setupEnvironment()

        // Execute delete-service
        def commandArgs = ["cf", "delete-service", name]

        commandArgs << "-f"

        runHelperCommand("Executing CF delete-service", commandArgs)
    }

    void deleteSubdomain() {
        def subdomain = props['subdomain']
        def domain = props['domain']

        setupEnvironment()

        // Execute delete-domain
        def commandArgs = [
            "cf",
            "delete-domain",
            subdomain + "." + domain
        ]

        commandArgs << "-f"

        runHelperCommand("Executing CF delete-domain", commandArgs)
    }

    void executeCFScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment(api, organization, space)

        // Execute script
        String eol = System.getProperty("line.separator")

        script.splitEachLine(eol) {
            def commandArgs = [it]

            if (args) {
                commandArgs << args
            }

            runHelperCommand("Executing CF commands", commandArgs)
        }
    }

    void executeBashScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment(api, organization, space)

        // Execute script
        def commandArgs = [script]

        if (args) {
            commandArgs << args
        }

        runHelperCommand("Executing CF script", commandArgs, null, true)
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

        runHelperCommand("Executing CF map-route", commandArgs)
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
        def commandArgs = [cfFile, "push", appName]

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

        runHelperCommand("Deploying CloudFoundry application", commandArgs)

    }

    void restartApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute restart application
        def commandArgs = [cfFile, "restart", app]
        runHelperCommand("Executing CF restart APP", commandArgs)
    }

    void startApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute start application
        def commandArgs = [cfFile, "start", app]
        runHelperCommand("Executing CF start APP", commandArgs)
    }

    void stopApp() {
        def app = props['application']

        setupEnvironment(api, organization, space)

        // Execute stop application
        def commandArgs = [cfFile, "stop", app]

        runHelperCommand("Executing CF stop APP", commandArgs)
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

        runHelperCommand("Executing CF unbind-service", commandArgs)
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

        runHelperCommand("Executing CF unmap-route", commandArgs)
    }

    // set the api, organization, and space targets for the cf executable
    void setupEnvironment(def api, def organization, def space) {
        // Setup path
        def curPath = System.getenv("PATH")
        def pluginHome = new File(System.getenv("PLUGIN_HOME"))

        println "Setup of path using plugin home: " + pluginHome
        def binDir = new File(pluginHome, "bin")
        def newPath = curPath+":"+binDir.absolutePath
        helper.addEnvironmentVariable("PATH", newPath)

        // change location of config.json file
        def cfHome = new File(props['PLUGIN_INPUT_PROPS']).parentFile
        println ("Setting CF_HOME to: " + cfHome)
        helper.addEnvironmentVariable("CF_HOME", cfHome.toString())

        // Set cf api
        def commandArgs = [cfFile, "api", api]

        if (selfSigned) {
            commandArgs << "--skip-ssl-validation"
        }

        runHelperCommand("Setting cf target api", commandArgs)

        // Authenticate with username and password
        if (!isAuthenticated) {
            if (organization && space) {
                commandArgs = [
                    cfFile,
                    "auth",
                    username,
                    password
                ]

                runHelperCommand("Authenticating with CloudFoundry", commandArgs)
                isAuthenticated = true
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

                runHelperCommand("Logging into CloudFoundry", commandArgs)
                isAuthenticated = true
            }
        }

        if (organization) {
            // Set target org
            commandArgs = [
                cfFile,
                "target",
                "-o",
                organization
            ]
            runHelperCommand("Setting CloudFoundry target organization", commandArgs)
        }

        if (space) {
            // Ensure space exists. create-space does nothing if space exists
            commandArgs = [
                cfFile,
                "create-space",
                space
            ]

            runHelperCommand("Creating CloudFoundry space", commandArgs)

            // Set target space
            commandArgs = [
                cfFile,
                "target",
                "-s",
                space
            ]

            runHelperCommand("Setting CloudFoundry target space", commandArgs)
        }
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
            finally {
                out.flush();
            }

            output = outputStream.toString()
        }

        int exitVal = helper.runCommand("Running command: ${cmdArgs.join(' ')}", cmdArgs, setOutput)

        return output
    }

    // logout of the Cloud Foundry Controller
    void logout() {
        def commandArgs = [cfFile, "logout"]
        runHelperCommand("Logout from CloudFoundry system", commandArgs)
    }

    def runHelperCommand(def message, def command) {
        try {
            helper.runCommand(message, command)
        } catch(ExitCodeException e){
            def errorMessage = "ERROR running command: ${commandArgs.join(' ')}"
            throw new ExitCodeException(errorMessage, ex)
        }
    }
}