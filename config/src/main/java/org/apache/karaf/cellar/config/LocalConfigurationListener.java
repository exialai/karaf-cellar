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

import org.apache.karaf.cellar.core.Configurations;
import org.apache.karaf.cellar.core.Group;
import org.apache.karaf.cellar.core.Node;
import org.apache.karaf.cellar.core.control.SwitchStatus;
import org.apache.karaf.cellar.core.event.EventProducer;
import org.apache.karaf.cellar.core.event.EventType;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Local configuration listener.
 */
public class LocalConfigurationListener extends ConfigurationSupport implements ConfigurationListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(LocalConfigurationListener.class);

    private static final long SYNC_TIMEOUT = 200;

    private EventProducer eventProducer;

    /**
     * Handle local configuration events.
     * If the event is a pending event stop it. Else broadcast it to the cluster.
     *
     * @param event
     */
    public void configurationEvent(ConfigurationEvent event) {

        // check if the producer is ON
        if (eventProducer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            LOGGER.warn("CELLAR CONFIG: cluster event producer is OFF");
            return;
        }

        String pid = event.getPid();

        Dictionary localDictionary = null;
        if (event.getType() != ConfigurationEvent.CM_DELETED) {
            Configuration conf = null;
            try {
                conf = configurationAdmin.getConfiguration(pid);
            } catch (Exception e) {
                LOGGER.error("CELLAR CONFIG: can't retrieve configuration with PID {}", pid, e);
                return;
            }

            localDictionary = conf.getProperties();
            if (localDictionary.get(Constants.SYNC_PROPERTY) != null) {
                Long syncTimestamp = new Long((String) localDictionary.get(Constants.SYNC_PROPERTY));
                Long currentTimestamp = System.currentTimeMillis();
                if ((currentTimestamp - syncTimestamp) <= SYNC_TIMEOUT) {
                    return;
                }
            }
        }

        Set<Group> groups = groupManager.listLocalGroups();

        if (groups != null && !groups.isEmpty()) {
            for (Group group : groups) {
                // check if the pid is allowed for outbound.
                if (isAllowed(group, Constants.CATEGORY, pid, EventType.OUTBOUND)) {

                    // update the distributed map if needed
                    Map<String, Properties> configurationTable = clusterManager.getMap(Constants.CONFIGURATION_MAP + Configurations.SEPARATOR + group.getName());

                    try {
                        if (event.getType() == ConfigurationEvent.CM_DELETED) {
                            // update the distributed map
                            configurationTable.remove(pid);
                            // broadcast the cluster event
                            RemoteConfigurationEvent remoteConfigurationEvent = new RemoteConfigurationEvent(pid);
                            remoteConfigurationEvent.setType(ConfigurationEvent.CM_DELETED);
                            remoteConfigurationEvent.setSourceGroup(group);
                            eventProducer.produce(remoteConfigurationEvent);
                        } else {
                            Properties localProperties = new Properties();
                            filter(localDictionary, localProperties);
                            // update the distributed map
                            configurationTable.put(pid, localProperties);
                            // broadcast the cluster event
                            RemoteConfigurationEvent remoteConfigurationEvent = new RemoteConfigurationEvent(pid);
                            remoteConfigurationEvent.setSourceGroup(group);
                            eventProducer.produce(remoteConfigurationEvent);
                        }
                    } catch (Exception e) {
                        LOGGER.error("CELLAR CONFIG: failed to push configuration with PID {} to the distributed map", pid, e);
                    }
                    // broadcast the cluster event
                } else LOGGER.warn("CELLAR CONFIG: configuration with PID {} is marked as BLOCKED OUTBOUND", pid);
            }
        }
    }


    /**
     * Initialization Method.
     */
    public void init() {

    }

    /**
     * Destruction Method.
     */
    public void destroy() {

    }

    public EventProducer getEventProducer() {
        return eventProducer;
    }

    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

}
