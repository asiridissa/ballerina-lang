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

import ballerina/task;

public type Person record {
    string name;
    int age;
};

function attachTimer() {
    Person person = {
        name: "Sam",
        age: 0
    };

    task:Scheduler timer = new({ interval: 100, initialDelay: 1000 });
    checkpanic timer.attach(timerService, person);
    checkpanic timer.start();
}

string result = "";

service timerService = service {
    resource function onTrigger(Person person) {
        person.age = person.age + 1;
        result = <@untainted string> (person.name + " is " + person.age.toString() + " years old");
    }
};

function getResult() returns string {
    return result;
}
