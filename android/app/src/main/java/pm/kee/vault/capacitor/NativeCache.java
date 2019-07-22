package pm.kee.vault.capacitor;

import android.content.Context;
import android.content.SharedPreferences;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import pm.kee.vault.data.EncryptedDataStorage;
import pm.kee.vault.data.source.local.ESPAutofillDataSource;

import static pm.kee.vault.util.Util.logw;

@NativePlugin()
public class NativeCache extends Plugin {

    public static void test() {

        JSObject ret = new JSObject();
        ret.put("value", "some value");
        //notifyListeners("myPluginEvent", ret);
    }

    @PluginMethod()
    public void update(PluginCall call) {
        String id = call.getString("id");
        JSObject message = call.getData();
        logw(message.toString());

        SharedPreferences localAfDataSourceSharedPrefs =
                this.getContext().getSharedPreferences(ESPAutofillDataSource.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        EncryptedDataStorage storage = new EncryptedDataStorage(localAfDataSourceSharedPrefs);
        storage.setJSON(id, message.toString()); //TODO: check some stuff and preprocess it before saving - at least check some sort of version number so we can ignore if shit gets out of sync somehow.
        call.success();
    }
}
