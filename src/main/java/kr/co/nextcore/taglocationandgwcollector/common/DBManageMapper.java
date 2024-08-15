package kr.co.nextcore.taglocationandgwcollector.common;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DBManageMapper {
    void deleteLocationHisDataBeforeOneMonth(String beforeOneMonth);
    void deleteGWHisDataBeforeOneMonth(String beforeOneMonth);
}
