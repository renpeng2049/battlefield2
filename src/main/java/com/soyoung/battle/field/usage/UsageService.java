/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.soyoung.battle.field.usage;


import com.soyoung.battle.field.common.component.AbstractComponent;
import com.soyoung.battle.field.common.inject.Inject;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.BaseRestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service to monitor usage of Elasticsearch features.
 */
public class UsageService extends AbstractComponent {

    private final List<BaseRestHandler> handlers;
    private final long sinceTime;

    @Inject
    public UsageService(Settings settings) {
        super(settings);
        this.handlers = new ArrayList<>();
        this.sinceTime = System.currentTimeMillis();
    }

    /**
     * Add a REST handler to this service.
     *
     * @param handler
     *            the {@link BaseRestHandler} to add to the usage service.
     */
    public void addRestHandler(BaseRestHandler handler) {
        handlers.add(handler);
    }



}
