package pm.kee.vault;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
//import com.getcapacitor.PluginHandle;

import java.util.ArrayList;

import pm.kee.vault.capacitor.NativeCache;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initializes the Bridge
    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      // Additional plugins you've installed go here
      // Ex: add(TotallyAwesomePlugin.class);
      add(NativeCache.class);
    }});
  }

//  public NativeCachePlugin getPlugin() {
//    PluginHandle handle = this.bridge.getPlugin("NativeCachePlugin");
//    if (handle == null) {
//      return null;
//    }
//    NativeCachePlugin myPlugin = (NativeCachePlugin) handle.getInstance();
//    return myPlugin;
//  }
}
