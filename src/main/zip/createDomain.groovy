/**
 * (c) Copyright IBM Corporation 2015, 2017. All Rights Reserved.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool
import com.urbancode.air.ExitCodeException
import com.urbancode.air.plugin.cf.helper.CFHelper

AirPluginTool apTool = new AirPluginTool(this.args[0], this.args[1])
CFHelper helper = new CFHelper(apTool.getStepProperties())

int exitCode = 0

try {
    helper.createDomain()
} catch(ExitCodeException e) {
    println(e)
    exitCode = 1
}
finally {
    helper.logout()
}

System.exit(exitCode)
