//This is an example step groovy to show the proper use of APTool
import com.urbancode.air.AirPluginTool;

def apTool = new AirPluginTool(this.args[0], this.args[1]) //assuming that args[0] is input props file and args[1] is output props file

def props = apTool.getStepProperties();

def propValue = props['propName'];

//do stuff here
apTool.setOutputProperty("outPropName", "outPropValue");

apTool.storeOutputProperties();//write the output properties to the file
