package pm.kee.vault;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginHandle;

import java.util.ArrayList;

import pm.kee.vault.capacitor.AutoFillPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initializes the Bridge
    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      // Additional plugins you've installed go here
      // Ex: add(TotallyAwesomePlugin.class);
      add(AutoFillPlugin.class);
    }});
  }

  public AutoFillPlugin getPlugin() {
    PluginHandle handle = this.bridge.getPlugin("AutoFillPlugin");
    if (handle == null) {
      return null;
    }
    AutoFillPlugin myPlugin = (AutoFillPlugin) handle.getInstance();
    return myPlugin;
  }
}
