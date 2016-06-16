package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class GetDBQueryResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facility/dbquery/get/#";

    public GetDBQueryResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

//    public static JsonObject createResponseObject(String[][] metaAndResultSetObjectTable) {
//        JsonObject responseObject = new JsonObject();
//        JsonArray metaAndResultSetTable = new JsonArray();
//        JsonArray metaAndResultSetArray;
//        for (String[] metaAndResultSetObjectArray : metaAndResultSetObjectTable) {
//            metaAndResultSetArray = new JsonArray();
//            for(String metaAndResultSetObject: metaAndResultSetObjectArray) {
//                metaAndResultSetArray.add(metaAndResultSetObject);
//            }
//            metaAndResultSetTable.add(metaAndResultSetArray);
//        }
//        responseObject.add("metaAndResultSet", metaAndResultSetTable);
//        return responseObject;
//    }

    public static JsonObject createResponseObject(ResultSet rs) throws SQLException {
        JsonObject responseObject = new JsonObject();
        JsonArray resultSetTable = new JsonArray();
        JsonArray resultSetArray;

        ResultSetMetaData columns= rs.getMetaData();
        int columnCount= columns.getColumnCount();

        while (rs.next()) {
            resultSetArray = new JsonArray();
            for (int i=1;i<=columnCount;i++) {
                resultSetArray.add(rs.getString(i));
            }
            resultSetTable.add(resultSetArray);
        }


        responseObject.add("resultSet", resultSetTable);
        return responseObject;
    }

}
