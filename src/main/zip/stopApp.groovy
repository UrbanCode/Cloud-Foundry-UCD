import com.urbancode.air.AirPluginTool
import com.urbancode.air.plugin.cf.helper.CFHelper

AirPluginTool apTool = new AirPluginTool(this.args[0], this.args[1])
CFHelper helper = new CFHelper(apTool)

helper.stopApp()