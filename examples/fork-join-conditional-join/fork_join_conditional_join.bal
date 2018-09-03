import ballerina/io;
import ballerina/runtime;

public function main() {
    // Declare the fork-join statement.
    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: ", i, " s: ", s);
            runtime:sleep(100);
            // Reply to the `join` block from worker w1.
            (i, s) -> fork;
        }

        worker w2 {
            float f = 10.344;
            io:println("[w2] f: ", f);
            runtime:sleep(100);
            // Reply to the `join` block from worker w2.
            f -> fork;
        }
        // `some 1` is used as the `join` condition, which means that the `join` block needs to wait for any one of the
        // workers to finish executing. When the `join` condition has been satisfied, the `results` map will contain the
        // value returned by the worker.
    } join (some 1) (map results) {
        // Checks if the worker that finished executing is worker `w1`.
        if (results["w1"] != null) {
            int iW1;
            string sW1;
            (iW1, sW1) = check <(int, string)>results["w1"];
            io:println("[join-block] iW1: ", iW1, " sW1: ", sW1);
        }

        // Checks if the worker that finished executing is worker `w2`.
        if (results["w2"] != null) {
            float fW2 = check <float>results["w2"];
            io:println("[join-block] fW2: ", fW2);
        }
    }

    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: ", i, " s: ", s);
            runtime:sleep(100);
            // Reply to the `join` block from worker w1.
            (i, s) -> fork;
        }

        worker w2 {
            // Sleep the `w2` for 2 seconds.
            runtime:sleep(2000);
        }
    } join (all) (map results) {
        // This line will not be reached since all workers will not be able to finish their tasks before the timeout.
    } timeout (1000) (map results) {
        // The `timeout` clause provides an upper bound on how long the `fork` will run until it is aborted.
        // Timeout should be provided in `milliseconds`. Results of any workers which were completed before the
        // timeout will be available in the `results` map (similar to the join condition).

        // Check whether the `w1` was finished before the timeout.
        if (results["w1"] != null) {
            int iW1;
            string sW1;
            (iW1, sW1) = check <(int, string)>results["w1"];
            io:println("[timeout-block] iW1: ", iW1, " sW1: ", sW1);
        }
    }
}
