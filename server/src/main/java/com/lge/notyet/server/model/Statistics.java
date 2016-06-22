package com.lge.notyet.server.model;

import com.eclipsesource.json.JsonArray;

import java.util.List;

public class Statistics {
    List<String> colunmnameList;
    List<JsonArray> valuesList;

    public Statistics(List<String> colunmnameList, List<JsonArray> valuesList) {
        this.colunmnameList = colunmnameList;
        this.valuesList = valuesList;
    }

    public List<String> getColunmnameList() {
        return colunmnameList;
    }

    public List<JsonArray> getValuesList() {
        return valuesList;
    }
}
