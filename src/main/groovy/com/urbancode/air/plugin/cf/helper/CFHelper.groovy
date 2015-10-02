package com.urbancode.air.plugin.cf.helper

import com.urbancode.air.AirPluginTool
import com.urbancode.air.CommandHelper

class CFHelper {
    AirPluginTool apTool
    CommandHelper helper
    final def workDir = new File('.').canonicalFile
    def props
    def api
    def username
    def password
    def selfSigned
    def organization
    def space

    CFHelper(AirPluginTool apTool) {
        this.apTool = apTool
        helper = new CommandHelper(workDir)
        props = apTool.getStepProperties()
        api = props['api']
        username = props['user']
        password = props['password']
        selfSigned = props['selfSigned'].toBoolean()
        organization = props['org']
        space = props['space']
    }

    void bindService() {
        def application = props['application']
        def service = props['service']

        setupEnvironment()

        // Execute bind-service
        try {
            def commandArgs = ["cf", "bind-service", application, service]
            helper.runCommand("Executing CF bind-service", commandArgs)
        } catch(Exception e){
            println "ERROR executing bind-service : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void createDomain() {
        def domain = props['domain']

        setupEnvironment()

        // Execute create-domain
        try {
            def commandArgs = ["cf", "create-domain", organization, domain]

            helper.runCommand("Executing CF create-domain", commandArgs)
        } catch(Exception e){
            println "ERROR executing create-domain : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void createSubdomain() throws Exception{
        def subdomain = props['subdomain']
        def domain = props['domain']

        setupEnvironment()

        // Execute create-domain
        try {
            def commandArgs = ["cf", "create-domain", organization, subdomain + "." + domain]

            helper.runCommand("Executing CF create-domain", commandArgs)
        } catch(Exception e){
            println "ERROR executing create-domain : ${e.message}"
            throw new RuntimeException(e)
        } finally {
            logout()
        }

    }

    void createRoute() {
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment()

        // Execute create-route
        try {
            def commandArgs = ["cf", "create-route", space, domain]

            if (hostname) {
                commandArgs << "-n"
                commandArgs << hostname
            }

            helper.runCommand("Executing CF create-route", commandArgs)
        } catch(Exception e){
            println "ERROR executing create-route : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void createService() {
        def name = props['name']
        def service = props['service']
        def plan = props['plan']

        setupEnvironment()

        // Execute create-service
        try {
            def commandArgs = ["cf", "create-service", service, plan, name]
            helper.runCommand("Executing CF create-service", commandArgs)
        } catch(Exception e){
            println "ERROR executing create-service : ${e.message}"
            System.exit(1)
        }

        // Logout
        try {
            def commandArgs = ["cf", "logout"]
            helper.runCommand("Logout from CloudFoundry system", commandArgs)
        } catch(Exception e){
            println "ERROR logging out : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void deleteApp() {
        def app = props['app']
        def deleteRoutes = props['delete_Routes']

        setupEnvironment()

        // Execute create-service
        try {
            def commandArgs = ["cf", "delete", app, "-f"]

            if (deleteRoutes == "true") {
                commandArgs << "-r"
            }

            helper.runCommand("Executing CF delete APP", commandArgs)
        } catch(Exception e){
            println "ERROR executing delete APP : ${e.message}"
            System.exit(1)
        }

        // Logout
        try {
            def commandArgs = ["cf", "logout"]
            helper.runCommand("Logout from CloudFoundry system", commandArgs)
        } catch(Exception e){
            println "ERROR logging out : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void deleteDomain() {
        def domain = props['domain']

        setupEnvironment()

        // Execute delete-domain
        try {
            def commandArgs = ["cf", "delete-domain", domain]

            commandArgs << "-f"

            helper.runCommand("Executing CF delete-domain", commandArgs)
        } catch(Exception e){
            println "ERROR executing delete-domain : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void deleteRoute() {
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment()

        // Execute delete-route
        try {
            def commandArgs = ["cf", "delete-route", domain]

            if (hostname) {
                commandArgs << "-n"
                commandArgs << hostname
            }

            commandArgs << "-f"

            helper.runCommand("Executing CF delete-route", commandArgs)
        } catch(Exception e){
            println "ERROR executing delete-route : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void deleteService() {
        def name = props['name']

        setupEnvironment()

        // Execute delete-service
        try {
            def commandArgs = ["cf", "delete-service", name]

            commandArgs << "-f"

            helper.runCommand("Executing CF delete-service", commandArgs)
        } catch(Exception e){
            println "ERROR executing delete-service : ${e.message}"
            throw new RuntimeException(e)
        } finally {
            logout()
        }
    }

    void deleteSubdomain() {
        def subdomain = props['subdomain']
        def domain = props['domain']

        setupEnvironment()

        // Execute delete-domain
        try {
            def commandArgs = ["cf", "delete-domain", subdomain + "." + domain]

            commandArgs << "-f"

            helper.runCommand("Executing CF delete-domain", commandArgs)
        } catch(Exception e){
            println "ERROR executing delete-domain : ${e.message}"
            throw new RuntimeException(e)
        } finally {
            logout()
        }
    }

    void executeCFScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment()

        // Execute script
        String eol = System.getProperty("line.separator")
        script.splitEachLine(eol) {
            try {
                def commandArgs = [it]

                if (args) {
                    commandArgs << args
                }

                helper.runCommand("Executing CF commands", commandArgs)
            } catch(Exception e){
                println "ERROR executing commands : ${e.message}"
                System.exit(1)
            }
        }

        logout()
    }

    void executeBashScript() {
        def script = props['script']
        def args = props['args']

        setupEnvironment()

        // Execute script
        try {
            def commandArgs = [script]

            if (args) {
                commandArgs << args
            }

            helper.runCommand("Executing CF script", commandArgs, null, true)
        } catch(Exception e){
            println "ERROR executing script : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void mapRoute() {
        def domain = props['domain']
        def hostname = props['hostname']
        def app = props['app']

        setupEnvironment()

        // Execute map-route
        try {
            def commandArgs = ["cf", "map-route", app, domain]

            if (hostname) {
                commandArgs << "-n"
                commandArgs << hostname
            }

            helper.runCommand("Executing CF map-route", commandArgs)
        } catch(Exception e){
            println "ERROR executing map-route : ${e.message}"
            System.exit(1)
        }

        logout()
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

        setupEnvironment()

        // Push the application
        try {
            def commandArgs = ["cf", "push", appName]

            if (buildpack) {
                commandArgs << "-b"
                commandArgs << buildpack
            }

            if (manifest) {
                File manifestPath = new File(workDir, manifest)

                commandArgs << "-f"
                if (manifestPath.exists() && mainifestPath.isFile()) {
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

            helper.runCommand("Deploying CloudFoundry application", commandArgs)
            System.exit(0)
        } catch(Exception e){
            println "ERROR authenticating : ${e.message}"
            System.exit(1)
        }

        logout()

    }

    void restartApp() {
        def app = props['appName']

        setupEnvironment()

        // Execute restart application
        try {
            def commandArgs = ["cf", "restart", app]
            helper.runCommand("Executing CF restart APP", commandArgs)
        } catch(Exception e){
            println "ERROR executing restart APP : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void startApp() {
        def app = props['appName']

        setupEnvironment()

        // Execute start application
        try {
            def commandArgs = ["cf", "start", app]
            helper.runCommand("Executing CF start APP", commandArgs)
        } catch(Exception e){
            println "ERROR executing start APP : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void stopApp() {
        def app = props['app']

        setupEnvironment()

        // Execute stop application
        try {
            def commandArgs = ["cf", "stop", app]
            helper.runCommand("Executing CF stop APP", commandArgs)
        } catch(Exception e){
            println "ERROR executing stop APP : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void unbindService() {
        def service = props['service']
        def app = props['application']

        setupEnvironment()

        // Execute unbind-service
        try {
            def commandArgs = ["cf", "unbind-service", application, service]
            helper.runCommand("Executing CF unbind-service", commandArgs)
        } catch(Exception e){
            println "ERROR executing unbind-service : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void unmapRoute() {
        def app = props['app']
        def domain = props['domain']
        def hostname = props['hostname']

        setupEnvironment()

        // Execute unmap-route
        try {
            def commandArgs = ["cf", "unmap-route", app]

            if (domain) {
                commandArgs << domain
            }

            if (hostname) {
                commandArgs << "-n"
                commandArgs << hostname
            }

            helper.runCommand("Executing CF unmap-route", commandArgs)
        } catch(Exception e){
            println "ERROR executing unmap-route : ${e.message}"
            System.exit(1)
        }

        logout()
    }

    void setupEnvironment() {

        // Setup path
        try {
            def curPath = System.getenv("PATH")
            //println "Current PATH: " + curPath
            def pluginHome = new File(System.getenv("PLUGIN_HOME"))
            println "Setup of path using plugin home: " + pluginHome
            def binDir = new File(pluginHome, "bin")
            def newPath = curPath+":"+binDir.absolutePath
            helper.addEnvironmentVariable("PATH", newPath)
            def cfHome = new File(props['PLUGIN_INPUT_PROPS']).parentFile
            println ("Setting CF_HOME to: " + cfHome)
            helper.addEnvironmentVariable("CF_HOME", cfHome.toString())
            //helper.printEnvironmentVariables()
        } catch(Exception e){
            println "ERROR setting path: ${e.message}"
            System.exit(1)
        }

        // Set cf api
        try {
            def commandArgs = ["cf", "api", api]

            if (selfSigned) {
                commandArgs << "--skip-ssl-validation"
            }

            helper.runCommand("Setting cf target api", commandArgs)
        } catch(Exception e){
            println "ERROR setting api: ${e.message}"
            System.exit(1)
        }

        // Authenticate with username and password
        if (organization && space) {
            try {
                def commandArgs = ["cf", "auth", username, password]

                helper.runCommand("Authenticating with CloudFoundry", commandArgs)
            } catch(Exception e){
                println "ERROR authenticating : ${e.message}"
                System.exit(1)
            }
        }

        else {
            try {
                def commandArgs = ["cf", "login", "-u", username, "-p", password]

                helper.runCommand("Logging into CloudFoundry", commandArgs)
            }
            catch (Exception ex) {
                println "ERROR authenticating : ${ex.message}"
                System.exit(1)
            }
        }

        if (organization) {
            // Set target org
            try {
                def commandArgs = ["cf", "target", "-o", organization]
                helper.runCommand("Setting CloudFoundry target organization", commandArgs)
            } catch(Exception e){
                println "ERROR setting target organization : ${e.message}"
                System.exit(1)
            }
        }

        if (space) {
            // Ensure space exists. create-space does nothing if space exists
            try {
                def commandArgs = ["cf", "create-space", space]
                helper.runCommand("Creating CloudFoundry space", commandArgs)
            } catch(Exception e){
                println "ERROR creating space : ${e.message}"
                System.exit(1)
            }

            // Set target space
            try {
                def commandArgs = ["cf", "target", "-s", space]
                helper.runCommand("Setting CloudFoundry target space", commandArgs)
            } catch(Exception e){
                println "ERROR setting target space : ${e.message}"
                System.exit(1)
            }
        }
    }

    void logout() {
        try {
            def commandArgs = ["cf", "logout"]
            helper.runCommand("Logout from CloudFoundry system", commandArgs)
        } catch(Exception e){
            println "ERROR logging out : ${e.message}"
            System.exit(1)
        }
    }
}
