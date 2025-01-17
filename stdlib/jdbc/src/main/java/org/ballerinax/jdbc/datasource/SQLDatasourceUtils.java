/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package org.ballerinax.jdbc.datasource;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.DatabaseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class contains utility methods for JDBC Client operations.
 *
 * @since 0.8.0
 */
public class SQLDatasourceUtils {

    private static final String POOL_MAP_KEY = UUID.randomUUID().toString();

    /**
     * This will retrieve the string value for the given clob.
     *
     * @param data clob data
     * @return string value
     * @throws  IOException error occurred while reading clob value
     * @throws  SQLException error occurred while reading clob value
     */
    public static String getString(Clob data) throws IOException, SQLException {
        if (data == null) {
            return null;
        }
        try (Reader r = new BufferedReader(data.getCharacterStream())) {
            StringBuilder sb = new StringBuilder();
            int pos;
            while ((pos = r.read()) != -1) {
                sb.append((char) pos);
            }
            return sb.toString();
        }
    }

    /**
     * This will retrieve the string value for the given blob.
     *
     * @param data blob data
     * @return string value
     * @throws  SQLException error occurred while reading blob value
     */
    public static String getString(Blob data) throws SQLException {
        // Directly allocating full length arrays for decode byte arrays since anyway we are building
        // new String in memory.
        // Position of the getBytes has to be 1 instead of 0.
        // "pos - the ordinal position of the first byte in the BLOB value to be extracted;
        // the first byte is at position 1"
        // - https://docs.oracle.com/javase/7/docs/api/java/sql/Blob.html#getBytes(long,%20int)
        if (data == null) {
            return null;
        }
        byte[] encode = getBase64Encode(
                new String(data.getBytes(1L, (int) data.length()), Charset.defaultCharset()));
        return new String(encode, Charset.defaultCharset());
    }

    /**
     * This will retrieve the string value for the given binary data.
     *
     * @param data binary data
     * @return string value
     */
    public static String getString(byte[] data) {
        if (data == null) {
            return null;
        } else {
            return new String(data, Charset.defaultCharset());
        }
    }

    public static String getString(java.util.Date value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        String type;
        if (value instanceof Time) {
            calendar.setTimeInMillis(value.getTime());
            type = "time";
        } else if (value instanceof Timestamp) {
            calendar.setTimeInMillis(value.getTime());
            type = "datetime";
        } else if (value instanceof Date) {
            calendar.setTime(value);
            type = "date";
        } else {
            calendar.setTime(value);
            type = "time";
        }
        return getString(calendar, type);
    }

    /**
     * This will retrieve the string value for the given array data.
     *
     * @param dataArray data
     * @return string value
     * @throws SQLException sql exception when reading result set
     */
    public static String getString(Array dataArray) throws SQLException {
        if (dataArray == null) {
            return null;
        }
        StringJoiner sj = new StringJoiner(",", "[", "]");
        ResultSet rs = dataArray.getResultSet();
        while (rs.next()) {
            Object arrayEl = rs.getObject(2);
            String val = String.valueOf(arrayEl);
            sj.add(val);
        }
        return sj.toString();
    }

    /**
     * This will retrieve the string value for the given struct data.
     *
     * @param udt struct
     * @return string value
     * @throws SQLException sql exception when reading result set
     */
    public static String getString(Struct udt) throws SQLException {
        if (udt.getAttributes() != null) {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            Object[] udtValues = udt.getAttributes();
            for (Object obj : udtValues) {
                sj.add(String.valueOf(obj));
            }
            return sj.toString();
        }
        return null;
    }

    public static ObjectValue createSQLDBClient(MapValue<String, Object> clientEndpointConfig,
            MapValue<String, Object> globalPoolOptions) {
        String url = clientEndpointConfig.getStringValue(Constants.EndpointConfig.URL);
        String username = clientEndpointConfig.getStringValue(Constants.EndpointConfig.USERNAME);
        String password = clientEndpointConfig.getStringValue(Constants.EndpointConfig.PASSWORD);
        MapValue<String, Object> dbOptions = (MapValue<String, Object>) clientEndpointConfig
                .getMapValue(Constants.EndpointConfig.DB_OPTIONS);
        MapValue<String, Object> poolOptions = (MapValue<String, Object>) clientEndpointConfig
                .getMapValue(Constants.EndpointConfig.POOL_OPTIONS);
        boolean userProvidedPoolOptionsNotPresent = poolOptions == null;
        if (userProvidedPoolOptionsNotPresent) {
            poolOptions = globalPoolOptions;
        }
        PoolOptionsWrapper poolOptionsWrapper = new PoolOptionsWrapper(poolOptions);
        String dbType = url.split(":")[1].toUpperCase(Locale.getDefault());

        SQLDatasource.SQLDatasourceParamsBuilder builder = new SQLDatasource.SQLDatasourceParamsBuilder(dbType);
        SQLDatasource.SQLDatasourceParams sqlDatasourceParams = builder.withPoolOptions(poolOptionsWrapper)
                .withJdbcUrl(url).withUsername(username).withPassword(password).withDbOptionsMap(dbOptions)
                .withIsGlobalDatasource(userProvidedPoolOptionsNotPresent).build();

        return createSQLClient(sqlDatasourceParams);
    }

    public static ErrorValue getSQLDatabaseError(SQLException exception, String messagePrefix) {
        String sqlErrorMessage =
                exception.getMessage() != null ? exception.getMessage() : Constants.DATABASE_ERROR_MESSAGE;
        int vendorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        return getSQLDatabaseError(messagePrefix + sqlErrorMessage, vendorCode, sqlState);
    }

    public static ErrorValue getSQLDatabaseError(SQLException exception) {
        return getSQLDatabaseError(exception, "");
    }

    public static ErrorValue getSQLDatabaseError(DatabaseException exception, String messagePrefix) {
        String message = exception.getMessage() != null ? exception.getMessage() : Constants.DATABASE_ERROR_MESSAGE;
        int vendorCode = exception.getSqlErrorCode();
        String sqlState = exception.getSqlState();
        String sqlErrorMessage = exception.getSqlErrorMessage();
        return getSQLDatabaseError(messagePrefix + message + sqlErrorMessage, vendorCode, sqlState);
    }

    static ErrorValue getSQLDatabaseError(DatabaseException exception) {
        return getSQLDatabaseError(exception, "");
    }

    private static ErrorValue getSQLDatabaseError(String message, int vendorCode, String sqlState) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("message", message);
        valueMap.put("sqlErrorCode", vendorCode);
        valueMap.put("sqlState", sqlState);
        MapValue<String, Object> sqlClientErrorDetailRecord = BallerinaValues
                .createRecordValue(Constants.JDBC_PACKAGE_PATH, Constants.DATABASE_ERROR_DATA_RECORD_NAME, valueMap);
        return BallerinaErrors.createError(Constants.DATABASE_ERROR_CODE, sqlClientErrorDetailRecord);
    }

    public static ErrorValue getSQLApplicationError(ApplicationException exception, String messagePrefix) {
        String message =
                exception.getMessage() != null ? exception.getMessage() : Constants.APPLICATION_ERROR_MESSAGE;
        String detailedErrorMessage = messagePrefix + message;
        if (exception.getDetailedErrorMessage() != null) {
            detailedErrorMessage += exception.getDetailedErrorMessage();
        }
        return getSQLApplicationError(detailedErrorMessage);
    }

    public static ErrorValue getSQLApplicationError(ApplicationException exception) {
        return getSQLApplicationError(exception, "");
    }

    public static ErrorValue getSQLApplicationError(String detailedErrorMessage) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("message", detailedErrorMessage);
        MapValue<String, Object> sqlClientErrorDetailRecord = BallerinaValues
                .createRecordValue(Constants.JDBC_PACKAGE_PATH, Constants.APPLICATION_ERROR_DATA_RECORD_NAME, valueMap);
        return BallerinaErrors.createError(Constants.APPLICATION_ERROR_CODE, sqlClientErrorDetailRecord);
    }

    static ConcurrentHashMap<String, SQLDatasource> retrieveDatasourceContainer(
            MapValue<String, Object> poolOptions) {
        return (ConcurrentHashMap<String, SQLDatasource>) poolOptions.getNativeData(POOL_MAP_KEY);
    }

    public static void addDatasourceContainer(MapValue<String, Object> poolOptions,
            ConcurrentHashMap<String, SQLDatasource> datasourceMap) {
        poolOptions.addNativeData(POOL_MAP_KEY, datasourceMap);
    }

    static boolean isSupportedDbOptionType(Object value) {
        boolean supported = false;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            int typeTag = type.getTag();
            supported = (typeTag == TypeTags.STRING_TAG || typeTag == TypeTags.INT_TAG || typeTag == TypeTags.FLOAT_TAG
                    || typeTag == TypeTags.BOOLEAN_TAG || typeTag == TypeTags.DECIMAL_TAG
                    || typeTag == TypeTags.BYTE_TAG);
        }
        return supported;
    }

    private static ObjectValue createSQLClient(SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        SQLDatasource sqlDatasource = sqlDatasourceParams.getPoolOptionsWrapper()
                .retrieveDatasource(sqlDatasourceParams);
        ObjectValue sqlClient = BallerinaValues.createObjectValue(Constants.JDBC_PACKAGE_PATH, Constants.JDBC_CLIENT);
        sqlClient.addNativeData(Constants.JDBC_CLIENT, sqlDatasource);
        return sqlClient;
    }

    private static String getString(Calendar calendar, String type) {
        if (!calendar.isSet(Calendar.ZONE_OFFSET)) {
            calendar.setTimeZone(TimeZone.getDefault());
        }
        StringBuffer datetimeString = new StringBuffer(28);
        switch (type) {
        case "date": //'-'? yyyy '-' mm '-' dd zzzzzz?
            appendDate(datetimeString, calendar);
            appendTimeZone(calendar, datetimeString);
            break;
        case "time": //hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
            break;
        case "datetime": //'-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            appendDate(datetimeString, calendar);
            datetimeString.append("T");
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
            break;
        default:
            throw new AssertionError("invalid type for datetime data: " + type);
        }
        return datetimeString.toString();
    }

    private static byte[] getBase64Encode(String st) {
        return Base64.getEncoder().encode(st.getBytes(Charset.defaultCharset()));
    }

    private static void appendTimeZone(Calendar calendar, StringBuffer dateString) {
        int timezoneOffSet = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        int timezoneOffSetInMinits = timezoneOffSet / 60000;
        if (timezoneOffSetInMinits < 0) {
            dateString.append("-");
            timezoneOffSetInMinits = timezoneOffSetInMinits * -1;
        } else {
            dateString.append("+");
        }
        int hours = timezoneOffSetInMinits / 60;
        int minits = timezoneOffSetInMinits % 60;
        if (hours < 10) {
            dateString.append("0");
        }
        dateString.append(hours).append(":");
        if (minits < 10) {
            dateString.append("0");
        }
        dateString.append(minits);
    }

    private static void appendTime(Calendar value, StringBuffer dateString) {
        if (value.get(Calendar.HOUR_OF_DAY) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.HOUR_OF_DAY)).append(":");
        if (value.get(Calendar.MINUTE) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.MINUTE)).append(":");
        if (value.get(Calendar.SECOND) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.SECOND)).append(".");
        if (value.get(Calendar.MILLISECOND) < 10) {
            dateString.append("0");
        }
        if (value.get(Calendar.MILLISECOND) < 100) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.MILLISECOND));
    }

    private static void appendDate(StringBuffer dateString, Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        if (year < 1000) {
            dateString.append("0");
        }
        if (year < 100) {
            dateString.append("0");
        }
        if (year < 10) {
            dateString.append("0");
        }
        dateString.append(year).append("-");
        // sql date month is started from 1 and calendar month is
        // started from 0. so have to add one
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 10) {
            dateString.append("0");
        }
        dateString.append(month).append("-");
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
            dateString.append("0");
        }
        dateString.append(calendar.get(Calendar.DAY_OF_MONTH));
    }
}
