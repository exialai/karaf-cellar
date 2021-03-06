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
package org.apache.karaf.cellar.core.control;

import org.apache.karaf.cellar.core.Consumer;
import org.apache.karaf.cellar.core.command.CommandHandler;

/**
 * Consumer switch command handler.
 */
public class ConsumerSwitchCommandHandler extends CommandHandler<ConsumerSwitchCommand, ConsumerSwitchResult> {

    public static final String SWITCH_ID = "org.apache.karaf.cellar.command.producer.switch";
    private final Switch commandSwitch = new BasicSwitch(SWITCH_ID);

    private Consumer consumer;

    /**
     * Handle the {@code ProducerSwitchCommand} command.
     *
     * @param command
     */
    public ConsumerSwitchResult execute(ConsumerSwitchCommand command) {
        //Query
        if (command.getStatus() == null) {
            return new ConsumerSwitchResult(command.getId(), Boolean.TRUE, consumer.getSwitch().getStatus().getValue());
        }
        //Turn on the switch
        if (command.getStatus().equals(SwitchStatus.ON)) {
            consumer.getSwitch().turnOn();
            return new ConsumerSwitchResult(command.getId(), Boolean.TRUE, Boolean.TRUE);
        }
        //Turn on the switch
        else if (command.getStatus().equals(SwitchStatus.OFF)) {
            consumer.getSwitch().turnOff();
            return new ConsumerSwitchResult(command.getId(), Boolean.TRUE, Boolean.FALSE);
        } else {
            return new ConsumerSwitchResult(command.getId(), Boolean.FALSE, consumer.getSwitch().getStatus().getValue());
        }
    }

    public Class<ConsumerSwitchCommand> getType() {
        return ConsumerSwitchCommand.class;
    }

    public Switch getSwitch() {
        return commandSwitch;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

}
