package com.yxh.msghelper;

import java.util.List;

// this is too complicated.
public class DataFilters {

    List<Filter> mFilters;
    List<String> mRelations; // and, or

    class Filter{
        String mColumn;
        String mColType; // int, long, string; get it from class def?
        String mCategory;// term, discrete, range
        String mValue; // "15", "Mike", "a,b,c", "(5,", "[10, 1000)"

        void Filter(String column, String type, String category, String value){

        }
    }

    void addFilterAndRelation(Filter filter, String relation){
        mFilters.add(filter);
        mRelations.add(relation);
    }

    // return: "where col1 = val1 and col2 in (1,3,5) and col3 > 30 and col3 <=50"
    // and/or 如何表示？
    String getSQL(){
        String sqlstr=null;

        return sqlstr;
    }

}
