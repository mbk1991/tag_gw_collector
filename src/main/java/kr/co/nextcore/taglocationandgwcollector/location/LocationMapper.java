package kr.co.nextcore.taglocationandgwcollector.location;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface LocationMapper {
    void insertLocationDataHistory(@Param("list") List<Map<String, Object>> locationDataList);

    void deleteCurrLocationData();

    void insertCurrLocationData(@Param("list") List<Map<String, Object>> locationDataList);

    List<Map<String, Object>> selectCriticalAreaList();

    void insertEventList(Map locationData);

    int hasOccuredLastOneMinute(Map locationData);

    Set<String> selectDkeyList();

    void updateCurrLocationData(@Param("list") List<Map<String, Object>> updateList);
}
