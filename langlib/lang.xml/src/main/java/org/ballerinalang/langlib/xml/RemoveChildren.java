/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.langlib.xml;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Remove any children that matches a given name, from an XML.
 * This operation has no effect if the XML is not of element type.
 * 
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml",
        functionName = "removeChildren",
        args = {@Argument(name = "qname", type = TypeKind.STRING)},
        isPublic = true
)
public class RemoveChildren extends BlockingNativeCallableUnit {

    private static final String OPERATION = "remove children from xml element";

    @Override
    public void execute(Context ctx) {
        try {
            BXML<?> xml = (BXML<?>) ctx.getRefArgument(0);
            String qname = ctx.getStringArgument(0);
            xml.removeChildren(qname);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }

        // Setting output value.
        ctx.setReturnValues();
    }

    public static void removeChildren(Strand strand, XMLValue<?> xml, String qname) {
        try {
            xml.removeChildren(qname);
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }
    }
}
