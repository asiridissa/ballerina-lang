#At the command line, navigate to the directory that contains the 
#`.bal` file and run the `ballerina run` command. 
$ ballerina run throw.bal
Record ID: 1, value: record1
error: ballerina.runtime:CallFailedException, message: call failed
        at main(throw.bal:32)
caused by error, message: record is null
        at readRecord(throw.bal:17)

