// Compile-only API declaration. Not included in the runtime host.
package com.cairn4.moonbase;

public class SettingsLoader {
    public SettingsData settingsData;
    public static synchronized SettingsLoader getInstance() { throw new UnsupportedOperationException(); }
    public void save() { throw new UnsupportedOperationException(); }
}
