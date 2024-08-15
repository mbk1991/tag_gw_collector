package kr.co.nextcore.taglocationandgwcollector.gateway;

import kr.co.nextcore.taglocationandgwcollector.common.RestService;
import kr.co.nextcore.taglocationandgwcollector.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GatewayService {
    @Value("${gateway.url}")
    private String GATEWAY_URL;
    @Value("${location.token}")
    private String GATEWAY_TOKEN;
    @Value("${sector.id}")
    private String SECTOR_ID;
    @Value("${building.id.tbn}")
    private String BUILDING_ID_TBN;
    @Value("${building.id.blr.out}")
    private String BUILDING_ID_BLR_OUT;

    @Autowired
    RestService restService;
    @Autowired
    GatewayMapper gatewayMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void gatewayDataProcess() {
        logger.info("*** 1. GW API REQUEST ***");
        long sTime = System.currentTimeMillis();
        List<Map<String, Object>> gatewayDataList = requestAPIEachBuilding(new String[]{BUILDING_ID_TBN, BUILDING_ID_BLR_OUT});
        if(gatewayDataList == null || gatewayDataList.isEmpty()){
            logger.info("GW Data List is Null -> End Process");
            return;
        }

        logger.info("*** 2. GW DB PROCESS ***");
        upsertGatewayData(gatewayDataList);
        logger.info("GW Process: cycle exec time : {}ms ", System.currentTimeMillis() - sTime);

    }

    private List<Map<String, Object>> requestAPIEachBuilding(String[] buildings) {
        List<Map<String,Object>> allLocationDataList = new ArrayList<>();
        for(String buildingId : buildings){
            logger.info(">>> API Request Each Building: buildingId({})", buildingId);

            ResponseEntity<String> responseEntity = requestAPI(buildingId);
            if(responseEntity == null){
                logger.error("API response is NULL.  -> continue");
                continue;
            }

            Map<String, Object> bodyDataMap = Util.jsonToMap(responseEntity.getBody());
            if(bodyDataMap.get("message").toString().equals("fail")){
                logger.error("API response, value of 'message' Key is 'fail'. (need confirm nineOne)  -> continue");
                continue;
            }

            List<Map<String, Object>> locationDataMapList = (List<Map<String, Object>>) bodyDataMap.get("listGateway");
            if(locationDataMapList == null || locationDataMapList.isEmpty()) {
                logger.error("'listGateway' Data is NULL or Empty -> continue");
                continue;
            }

            String regDate = "";
            if(regDate.equals("")) regDate = Util.dateFormat((String) bodyDataMap.get("today"), "yyyyMMddHHmmss");
            List<Map<String, Object>> l = parseData(locationDataMapList, regDate);
            allLocationDataList.addAll(l);
        }

        int size = allLocationDataList.size();
        logger.info("GW Data List Size : {}", size);
        return size != 0? allLocationDataList : null;
    }


    private void upsertGatewayData(List<Map<String, Object>> gatewayDataList) {
        try{
            //select gateway list
            logger.info("select gateway list");
            Set<String> dKeySet = gatewayMapper.selectDkeyList();

            List<Map<String, Object>> insertList = new ArrayList<>();
            List<Map<String, Object>> updateList = new ArrayList<>();

            logger.info("gateWayDataList size {}", gatewayDataList.size());

            for(Map<String, Object> m:gatewayDataList){
                if(dKeySet.contains(m.get("DKEY"))){
                    updateList.add(m);
                }else{
                    insertList.add(m);
                }
            }

            logger.info("insertList size {}, updateList size {}", insertList.size(), updateList.size());

            logger.info("insertGatewayDataHistory");
            gatewayMapper.insertGatewayDataHistory(gatewayDataList);
            if(insertList.size() >= 1){
                logger.info("insertCurrGatewayData");
                gatewayMapper.insertCurrGatewayData(insertList);
            }
            if(updateList.size() >= 1){
                logger.info("updateCurrGatewayData");
                gatewayMapper.updateCurrGatewayData(updateList);
            }

            logger.info("done database process");
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    private List<Map<String, Object>> parseData(List<Map<String, Object>> gList, String regDate) {
        logger.info(">>> parse data");
        List<Map<String, Object>> gatewayDataList = new ArrayList<>();
        gList.stream().forEach(gData -> {
            try{
                Map<String, Object> m = new HashMap<>();
                m.put("DKEY", gData.get("gw_id"));
                m.put("REGDATE",regDate);
                m.put("GW_TYPE",gData.get("gw_type"));
                m.put("GW_BATT",battValToPercentage(Double.parseDouble(gData.get("gw_batt").toString())));
                m.put("GW_LOCATION",gData.get("gw_location"));
                m.put("GW_PRESSURE_BASE_CHECK",gData.get("gw_pressure_base_check"));
                m.put("NAME",makeName(gData.get("gw_id").toString()));
                gatewayDataList.add(m);

                logger.info("----- {} -----", m.get("DKEY"));
                logger.info("                 REGDATE : {}", m.get("REGDATE"));
                logger.info("                 GW_TYPE : {}", m.get("GW_TYPE"));
                logger.info("                 GW_BATT : {}", m.get("GW_BATT"));
                logger.info("             GW_LOCATION : {}", m.get("GW_LOCATION"));
                logger.info("  GW_PRESSURE_BASE_CHECK : {}", m.get("GW_PRESSURE_BASE_CHECK"));
                logger.info("                    NAME : {}", m.get("NAME"));
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        });
        logger.info("----------\n");
        return gatewayDataList;
    }

    private String makeName(String gwId) {
        return gwId.substring(gwId.length()-3);
    }

    private Double battValToPercentage(double gwBatt) {
        Double p;
        if (gwBatt <= 6.8) {
            p =  10.0;
        } else if (gwBatt < 8.2) {
            p = ((gwBatt - 6.8) / (8.2 - 6.8)) * 80 + 20;
        } else {
            p = 100.0;
        }
        return p;
    }

    private ResponseEntity<String> requestAPI(String buildingId) {
        logger.info("*** Request API");
        logger.info("sector_id: {} building_id: {}", SECTOR_ID, buildingId);
        logger.info("token: {}", GATEWAY_TOKEN);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("sector_id", SECTOR_ID);
        bodyMap.put("token", GATEWAY_TOKEN);
        bodyMap.put("building_id", buildingId);
        String body = Util.mapToJson(bodyMap);
        return restService.requestRestAPI(body, GATEWAY_URL, HttpMethod.POST);
    }

}
