package kr.co.nextcore.taglocationandgwcollector.gateway;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface GatewayMapper {
    void insertGatewayDataHistory(@Param("list") List<Map<String, Object>> gatewayDataList);

    void deleteCurrGatewayData();

    void insertCurrGatewayData(@Param("list") List<Map<String, Object>> gatewayDataList);

    Set<String> selectDkeyList();

    void updateCurrGatewayData(@Param("list") List<Map<String, Object>> updateList);
}
