/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinax.jdbc.functions;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasourceUtils;

import java.util.UUID;

/**
 * Returns the JDBC Client.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerinax", packageName = "java.jdbc",
        functionName = "createClient",
        isPublic = true
)
public class CreateClient {

    public static ObjectValue createClient(Strand strand, MapValue<String, Object> config, MapValue<String,
                Object> globalPoolOptions) {
        ObjectValue jdbcClient = SQLDatasourceUtils.createSQLDBClient(config, globalPoolOptions);
        jdbcClient.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
        return jdbcClient;
    }
}
