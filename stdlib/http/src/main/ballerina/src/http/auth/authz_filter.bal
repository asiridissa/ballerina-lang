// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/cache;
import ballerina/reflect;

# Representation of the Authorization filter
#
# + authzHandler - `AuthzHandler` instance for handling authorization
# + scopes - Array of scopes
public type AuthzFilter object {

    public AuthzHandler authzHandler;
    public string[]|string[][]? scopes;

    public function __init(AuthzHandler authzHandler, string[]|string[][]? scopes) {
        self.authzHandler = authzHandler;
        self.scopes = scopes;
    }

    # Filter function implementation which tries to authorize the request
    #
    # + caller - Caller for outbound HTTP responses
    # + request - `Request` instance
    # + context - `FilterContext` instance
    # + return - A flag to indicate if the request flow should be continued(true) or aborted(false), a code and a message
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        boolean|AuthorizationError authorized = true;
        var scopes = getScopes(context);
        if (scopes is string[]|string[][]) {
            authorized = handleAuthzRequest(self.authzHandler, request, context, scopes);
        } else {
            if (scopes) {
                var selfScopes = self.scopes;
                if (selfScopes is string[]|string[][]) {
                    authorized = handleAuthzRequest(self.authzHandler, request, context, selfScopes);
                } else {
                    authorized = true;
                }
            } else {
                authorized = true;
            }
        }
        return isAuthzSuccessful(caller, authorized);
    }

    public function filterResponse(Response response, FilterContext context) returns boolean {
        return true;
    }
};

function handleAuthzRequest(AuthzHandler authzHandler, Request request, FilterContext context,
                            string[]|string[][] scopes) returns boolean|AuthorizationError {
    boolean|AuthorizationError authorized = true;
    runtime:Principal? principal = runtime:getInvocationContext()?.principal;
    if (scopes is string[]) {
        if (scopes.length() > 0) {
            var canProcessResponse = authzHandler.canProcess(request);
            if (canProcessResponse is boolean && canProcessResponse) {
                if(principal is runtime:Principal) {
                    authorized = authzHandler.process(principal.username,context.serviceName, context.resourceName,
                                                    request.method, scopes);
                } else {
                    authorized = false;
                }
            } else {
                authorized = canProcessResponse;
            }
        } else {
            // scopes are not defined, no need to authorize
            authorized = true;
        }
    } else {
        if (scopes[0].length() > 0) {
            var canProcessResponse = authzHandler.canProcess(request);
            if (canProcessResponse is boolean && canProcessResponse) {
                if(principal is runtime:Principal) {
                 authorized = authzHandler.process(principal.username,
                                    context.serviceName, context.resourceName, request.method, scopes);
                } else {
                    authorized = false;
                }
            } else {
                authorized = canProcessResponse;
            }
        }
    }
    return authorized;
}

# Verifies if the authorization is successful. If not responds to the user.
#
# + caller - Caller for outbound HTTP responses
# + authorized - Authorization status for the request, or `AuthorizationError` if error occurred
# + return - Authorization result to indicate if the filter can proceed(true) or not(false)
function isAuthzSuccessful(Caller caller, boolean|AuthorizationError authorized) returns boolean {
    Response response = new;
    response.statusCode = 403;
    if (authorized is boolean) {
        if (!authorized) {
            response.setTextPayload("Authorization failure");
            var err = caller->respond(response);
            if (err is error) {
                panic <error> err;
            }
            return false;
        }
    } else {
        // TODO: Remove the below casting when new lang syntax are merged.
        error e = authorized;
        response.setTextPayload("Authorization failure. " + e.reason());
        var err = caller->respond(response);
        if (err is error) {
            panic <error> err;
        }
        return false;
    }
    return true;
}
