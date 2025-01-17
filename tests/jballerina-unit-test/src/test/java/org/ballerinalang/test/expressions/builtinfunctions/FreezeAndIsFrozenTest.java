/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.builtinfunctions;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This class tests the freeze() and isFrozen() builtin functions.
 *
 * @since 0.985.0
 */
public class FreezeAndIsFrozenTest {

    private static final String FREEZE_ERROR_OCCURRED_ERR_MSG =
            "error occurred on freeze: 'freeze()' not allowed on 'PersonObj'";
    private static final String FREEZE_SUCCESSFUL = "freeze successful";

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/freeze-and-isfrozen.bal");
        negativeResult = BCompileUtil.compile(
                "test-src/expressions/builtinoperations/freeze-and-isfrozen-negative.bal");
    }

    @Test()
    public void testFreezeOnNilTypedValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFreezeOnNilTypedValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(dataProvider = "booleanValues")
    public void testBooleanFreeze(boolean i) {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanFreeze", new BValue[]{new BBoolean(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected booleans to be frozen");
    }

    @Test(dataProvider = "intValues")
    public void testIntFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntFreeze", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected ints to be frozen");
    }

    @Test(dataProvider = "byteValues")
    public void testByteFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testByteFreeze", new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected bytes to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected bytes to be frozen");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatFreeze(double i) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatFreeze", new BValue[]{new BFloat(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected floats to be frozen");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalFreeze(BigDecimal i) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalFreeze", new BValue[]{new BDecimal(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected decimals to be frozen");
    }

    @Test(dataProvider = "stringValues")
    public void testStringFreeze(String i) {
        BValue[] returns = BRunUtil.invoke(result, "testStringFreeze", new BValue[]{new BString(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected strings to be frozen");
    }

    @Test
    public void testBasicTypeNullableUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypeNullableUnionFreeze", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be frozen");
    }

    @Test
    public void testBasicTypeUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypeUnionFreeze", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be frozen");
    }

    @Test
    public void testBasicTypesAsJsonFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypesAsJsonFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json values to be the same");
    }

    @Test
    public void testIsFrozenOnStructuralTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testIsFrozenOnStructuralTypes", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as not frozen");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be identified as frozen");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=modification not " +
                    "allowed on frozen value.*",
            dataProvider = "frozenBasicTypeArrayModificationFunctions")
    public void testFrozenBasicTypeArrayModification(String frozenBasicTypeArrayModificationFunction) {
        BRunUtil.invoke(result, frozenBasicTypeArrayModificationFunction, new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=modification not " +
                    "allowed on frozen value.*")
    public void testFrozenDecimalArrayModification() {
        BRunUtil.invoke(result, "testFrozenDecimalArrayModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=failed to set element to " 
                    + "json: message=modification not allowed on frozen value.*")
    public void testFrozenJsonArrayModification() {
        BRunUtil.invoke(result, "testFrozenJsonArrayModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map insertion: " 
                    + "modification not allowed on frozen value.*")
    public void testFrozenJsonModification() {
        BRunUtil.invoke(result, "testFrozenJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map insertion: " 
                    + "modification not allowed on frozen value.*")
    public void testAdditionToFrozenJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate " 
                    + "message=Failed to remove element from map: modification not allowed on frozen value.*")
    public void testRemovalFromFrozenJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map insertion: " 
                    + "modification not allowed on frozen value.*")
    public void testFrozenInnerJsonModification() {
        BRunUtil.invoke(result, "testFrozenInnerJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map insertion: " 
                    + "modification not allowed on frozen value.*")
    public void testAdditionToFrozenInnerJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to remove element " 
                    + "from map: modification not allowed on frozen value.*")
    public void testRemovalFromFrozenInnerJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}XMLOperationError message=Failed to add" +
                    " children to xml element: modification not allowed on frozen value.*")
    public void testFrozenXmlAppendChildren() {
        BRunUtil.invoke(result, "testFrozenXmlAppendChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}XMLOperationError message=Failed to " +
                    "remove children from xml element: modification not allowed on frozen value.*")
    public void testFrozenXmlRemoveChildren() {
        BRunUtil.invoke(result, "testFrozenXmlRemoveChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}XMLOperationError message=Failed to " +
                    "remove attribute: modification not allowed on frozen value.*")
    public void testFrozenXmlRemoveAttribute() {
        BRunUtil.invoke(result, "testFrozenXmlRemoveAttribute", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}XMLOperationError message=Failed to set" +
                    " attributes: modification not allowed on frozen value.*")
    public void testFrozenXmlSetAttributes() {
        BRunUtil.invoke(result, "testFrozenXmlSetAttributes", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}XMLOperationError message=Failed to set" +
                    " children to xml element: modification not allowed on frozen value.*")
    public void testFrozenXmlSetChildren() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map " +
                    "insertion: modification not allowed on frozen value.*")
    public void testFrozenMapUpdate() {
        BRunUtil.invoke(result, "testFrozenMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to remove " +
                    "element from map: modification not allowed on frozen value.*")
    public void testFrozenMapRemoval() {
        BRunUtil.invoke(result, "testFrozenMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to clear " +
                    "map: modification not allowed on frozen value.*")
    public void testFrozenMapClear() {
        BRunUtil.invoke(result, "testFrozenMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid map " +
                    "insertion: modification not allowed on frozen value.*")
    public void testFrozenInnerMapUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to remove " +
                    "element from map: modification not allowed on frozen value.*")
    public void testFrozenInnerMapRemoval() {
        BRunUtil.invoke(result, "testFrozenInnerMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to clear " +
                    "map: modification not allowed on frozen value.*")
    public void testFrozenInnerMapClear() {
        BRunUtil.invoke(result, "testFrozenInnerMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=modification not " +
                    "allowed on frozen value.*")
    public void testFrozenAnyArrayAddition() {
        BRunUtil.invoke(result, "testFrozenAnyArrayAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=modification not " +
                    "allowed on frozen value.*")
    public void testFrozenAnyArrayUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid update of" +
                    " record field: modification not allowed on frozen value.*")
    public void testFrozenAnyArrayElementUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayElementUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=modification not " +
                    "allowed on frozen value.*")
    public void testFrozenTupleUpdate() {
        BRunUtil.invoke(result, "testFrozenTupleUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid update of" +
                    " record field: modification not allowed on frozen value.*")
    public void testFrozenRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Invalid update of" +
                    " record field: modification not allowed on frozen value.*")
    public void testFrozenInnerRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to add " +
                    "data to the table: modification not allowed on frozen value.*")
    public void testFrozenTableAddition() {
        BRunUtil.invoke(result, "testFrozenTableAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}InvalidUpdate message=Failed to remove " +
                    "data from the table: modification not allowed on frozen value.*")
    public void testFrozenTableRemoval() {
        BRunUtil.invoke(result, "testFrozenTableRemoval", new BValue[0]);
    }

    @Test
    public void testSimpleUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnionFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as frozen");
    }

    @Test(description = "test a map of type not purtestAdditionToFrozenInnerJsonely anydata, a combination of anydata" +
            " and non-anydata")
    public void testValidComplexMapFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexMapFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test(description = "test an array of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexArrayFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexArrayFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test(description = "test a record of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexRecordFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexRecordFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test(description = "test a tuple of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexTupleFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexTupleFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test(description = "test a union of member type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexUnionFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test
    public void testValidSelfReferencingValueFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidSelfReferencingValueFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be frozen since no error " +
                "was encountered");
    }

    @Test
    public void testStructureWithErrorValueFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testStructureWithErrorValueFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFrozenValueUpdatePanicWithCheckTrap() {
        BValue[] returns = BRunUtil.invoke(result, "testFrozenValueUpdatePanicWithCheckTrap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "failed to set element to json: message=modification not allowed on frozen value");
    }

    @Test
    public void testFreezeAndIsFrozenNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 29);
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'PersonObj'", 19, 19);
        validateError(negativeResult, index++, "function invocation on type 'other' is not supported", 19, 19);
        validateError(negativeResult, index++, "incompatible types: expected 'PersonObj', found 'other'", 19, 19);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'stream<int>'", 22, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'future<boolean>'", 25,
                      9);
        validateError(negativeResult, index++, "variable 'boolFuture' is not initialized", 25, 9);
        validateError(negativeResult, index++, "variable 'boolFuture' is not initialized", 25, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'map<PersonObj>'", 30,
                      9);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found 'map<(stream<int>|PersonObj)>'", 33, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'PersonObj[]'", 38, 9);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '(PersonObjTwo|PersonObj)?[]'", 41, 10);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '[(PersonObj|PersonObjTwo),PersonObjTwo]'", 48,
                      9);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'Department'", 53, 9);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found 'map<(string|PersonObj)>'", 58, 32);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', "
                + "found 'map<[(string|PersonObj),(FreezeAllowedDepartment|float)]>'", 61, 26);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '(boolean|PersonObj|float)?[]'", 64, 39);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '(boolean|PersonObj|float)?[]'", 66, 16);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found '[(string|PersonObj),"
                + "(FreezeAllowedDepartment|float)]'", 69, 60);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found 'FreezeAllowedDepartment'", 72, 35);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found '(string|PersonObj)'",
                      75, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found 'error'", 80, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'any', found 'error'", 80, 9);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found 'map<(string|PersonObj)>'", 90, 39);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '(string|typedesc<anydata>|float)?[]'", 101, 53);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found 'FreezeAllowedDepartment2'", 109, 42);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '[int,(string|PersonObj|float),boolean]'", 115,
                      21);
        validateError(negativeResult, index++,
                      "incompatible types: expected 'anydata', found '(int|Department|PersonObj)'", 122, 42);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', found '(anydata|error)'", 129,
                      19);
        validateError(negativeResult, index, "incompatible types: expected 'anydata', found '(anydata|error)'", 129,
                      19);
    }

    @DataProvider(name = "booleanValues")
    public Object[][] booleanValues() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {-123457},
                {0},
                {1},
                {53456032}
        };
    }

    @DataProvider(name = "byteValues")
    public Object[][] byteValues() {
        return new Object[][]{
                {0},
                {1},
                {255}
        };
    }

    @DataProvider(name = "floatValues")
    public Object[][] floatValues() {
        return new Object[][]{
                {-1234.57},
                {0.0},
                {1.1},
                {53456.032}
        };
    }

    @DataProvider(name = "decimalValues")
    public Object[][] decimalValues() {
        return new Object[][]{
                {new BigDecimal("-1234.57", MathContext.DECIMAL128)},
                {new BigDecimal("53456.032", MathContext.DECIMAL128)},
                {new BigDecimal("0.0", MathContext.DECIMAL128)},
                {new BigDecimal("1.1", MathContext.DECIMAL128)}
        };
    }

    @DataProvider(name = "stringValues")
    public Object[][] stringValues() {
        return new Object[][]{
                {"a"},
                {"Hello, from Ballerina!"}
        };
    }

    @DataProvider(name = "frozenBasicTypeArrayModificationFunctions")
    public Object[][] frozenBasicTypeArrayModificationFunctions() {
        return new Object[][]{
                {"testFrozenIntArrayModification"},
                {"testFrozenByteArrayModification"},
                {"testFrozenBooleanArrayModification"},
                {"testFrozenFloatArrayModification"},
                {"testFrozenStringArrayModification"}
        };
    }
}
