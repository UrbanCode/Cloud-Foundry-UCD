/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2015. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool
import com.urbancode.air.ExitCodeException
import com.urbancode.air.plugin.cf.helper.CFHelper

AirPluginTool apTool = new AirPluginTool(this.args[0], this.args[1])
CFHelper helper = new CFHelper(apTool.getStepProperties())

int exitCode = 0

try {
    helper.deleteSubdomain()
} catch(ExitCodeException e) {
    println(e)
    exitCode = 1
}
finally {
    helper.logout()
}

System.exit(exitCode)