// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Detail record {|
    string message?;
    error cause?;
    (anydata|error)...;
|};

public const CONNECTION_TIMED_OUT = "{ballerina/io}ConnectionTimedOut";
public type TimeOutError error<CONNECTION_TIMED_OUT, Detail>;

public const GENERIC_ERROR = "{ballerina/io}GenericError";
public type GenericError error<GENERIC_ERROR, Detail>;

public type Error GenericError|TimeOutError;

function testTypeTestingErrorUnion() returns [string, Detail]? {
    Error? err = getError();

    if (err is Error) {
        Detail dt = err.detail();
        return [err.reason(), dt];
    }
}

function getError() returns Error? {
    GenericError err = error(GENERIC_ERROR, message = "Test union of errors with type test");
    return err;
}


public type AnotherDetail record {
    string message;
    error cause?;
};

public const REASON_1 = "Reason1";
public type FirstError error<REASON_1, AnotherDetail>;

public const REASON_2 = "Reason2";
public type SecondError error<REASON_2, AnotherDetail>;

public type ErrorUnion FirstError|SecondError;

public function testPassingErrorUnionToFunction() returns AnotherDetail? {
    var result = funcFoo();
    if (result is error) {
        return getAnotherDetail(result);
    }
}

public function funcFoo() returns int|ErrorUnion {
    FirstError e = error(REASON_1, message = "Test passing error union to a function");
    return e;
}

public function getAnotherDetail(error e) returns AnotherDetail {
    return <AnotherDetail>e.detail();
}
