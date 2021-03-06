/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cellar.config;

import org.apache.karaf.cellar.core.CellarSupport;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Configuration support.
 */
public class ConfigurationSupport extends CellarSupport {

    private static String[] EXCLUDED_PROPERTIES = {"felix.fileinstall.filename", "felix.fileinstall.dir", "felix.fileinstall.tmpdir", "org.ops4j.pax.url.mvn.defaultRepositories"};

    private static final String FELIX_FILEINSTALL_FILENAME = "felix.fileinstall.filename";

    protected File storage;

    /**
     * Filter a dictionary, specially replace the karaf.home property with a relative value.
     *
     * @param source the source dictionary.
     * @param target the target dictionary
     */
    public void filter(Dictionary source, Dictionary target) {
        if (source != null) {
            Enumeration enumaration = source.keys();
            while (enumaration.hasMoreElements()) {
                String key = (String) enumaration.nextElement();
                if (!isExcludedProperty(key)) {
                    String value = String.valueOf(source.get(key));
                    target.put(key, value);
                }
            }
        }
    }

    /**
     * Returns true if property is Filtered.
     *
     * @param propertyName
     * @return
     */
    public boolean isExcludedProperty(String propertyName) {
        for (int i = 0; i < EXCLUDED_PROPERTIES.length; i++) {
            if (EXCLUDED_PROPERTIES[i].equals(propertyName))
                return true;
        }
        return false;
    }

    /**
     * Persist a configuration to a storage.
     *
     * @param pid
     * @throws Exception
     */
    protected void persistConfiguration(ConfigurationAdmin admin, String pid, Dictionary props) {
        try {
            if (pid.matches(".*-.*-.*-.*-.*")) {
                // it's UUID
                return;
            }
            File storageFile = new File(storage, pid + ".cfg");
            Configuration cfg = admin.getConfiguration(pid, null);
            if (cfg != null && cfg.getProperties() != null) {
                Object val = cfg.getProperties().get(FELIX_FILEINSTALL_FILENAME);
                try {
                    if (val instanceof URL) {
                        storageFile = new File(((URL) val).toURI());
                    }
                    if (val instanceof URI) {
                        storageFile = new File((URI) val);
                    }
                    if (val instanceof String) {
                        storageFile = new File(new URL((String) val).toURI());
                    }
                } catch (Exception e) {
                    throw (IOException) new IOException(e.getMessage()).initCause(e);
                }
            }
            org.apache.felix.utils.properties.Properties p = new org.apache.felix.utils.properties.Properties(storageFile);
            for (Enumeration keys = props.keys(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                if (!org.osgi.framework.Constants.SERVICE_PID.equals(key)
                        && !ConfigurationAdmin.SERVICE_FACTORYPID.equals(key)
                        && !FELIX_FILEINSTALL_FILENAME.equals(key)) {
                    p.put((String) key, (String) props.get(key));
                }
            }
            // remove "removed" properties from the file
            ArrayList<String> propertiesToRemove = new ArrayList<String>();
            for (Object key : p.keySet()) {
                if (props.get(key) == null
                        && !org.osgi.framework.Constants.SERVICE_PID.equals(key)
                        && !ConfigurationAdmin.SERVICE_FACTORYPID.equals(key)
                        && !FELIX_FILEINSTALL_FILENAME.equals(key)) {
                    propertiesToRemove.add(key.toString());
                }
            }
            for (String key : propertiesToRemove) {
                p.remove(key);
            }
            // save the cfg file
            storage.mkdirs();
            p.save();
        } catch (Exception e) {
            // nothing to do
        }
    }

    protected void deleteStorage(String pid) {
        File cfgFile = new File(storage, pid + ".cfg");
        cfgFile.delete();
    }

    public File getStorage() {
        return storage;
    }

    public void setStorage(File storage) {
        this.storage = storage;
    }

}
