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

import org.apache.karaf.cellar.core.command.Result;

/**
 * Consumer switch result.
 */
public class ConsumerSwitchResult extends Result {

    protected Boolean success = Boolean.TRUE;
    protected Boolean status = Boolean.TRUE;

    /**
     * Constructor
     *
     * @param id
     */
    public ConsumerSwitchResult(String id) {
        super(id);
    }

    /**
     * Constructor
     *
     * @param id
     * @param success
     */
    public ConsumerSwitchResult(String id, Boolean success) {
        super(id);
        this.success = success;
    }

    /**
     * Constructor
     *
     * @param id
     * @param success
     * @param status
     */
    public ConsumerSwitchResult(String id, Boolean success, Boolean status) {
        super(id);
        this.success = success;
        this.status = status;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

}
