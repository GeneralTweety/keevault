package pm.kee.vault.capacitor;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class AutoFillPlugin extends Plugin {

    public static void test() {

        JSObject ret = new JSObject();
        ret.put("value", "some value");
        //notifyListeners("myPluginEvent", ret);
    }

    @PluginMethod()
    public void customCall(PluginCall call) {
        String message = call.getString("message");
        call.success();
    }
}
