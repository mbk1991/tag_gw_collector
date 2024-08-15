package kr.co.nextcore.taglocationandgwcollector.location;

import kr.co.nextcore.taglocationandgwcollector.common.RestService;
import kr.co.nextcore.taglocationandgwcollector.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LocationService {
    @Value("${location.url}")
    private String LOCATION_URL;
    @Value("${location.token}")
    private String LOCATION_TOKEN;
    @Value("${sector.id}")
    private String SECTOR_ID;
    @Value("${building.id.tbn}")
    private String BUILDING_ID_TBN;
    @Value("${building.id.blr.out}")
    private String BUILDING_ID_BLR_OUT;
    @Value("${building.id.blr.in}")
    private String BUILDING_ID_BLR_IN;
    @Value("${websocket.api.url}")
    private String WEB_SOCKET_URL;

    @Autowired
    private RestService restService;
    @Autowired
    private LocationMapper locationMapper;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void locationDataProcess() {

        logger.info("*** 1. LOCATION API REQUEST ***");
        long sTime = System.currentTimeMillis();
        List<Map<String, Object>> locationDataList = requestAPIbySector();
        if (locationDataList == null || locationDataList.isEmpty()) {
            logger.info("Location Data List is Null -> End Process");
            return;
        }

        logger.info("*** 2. LOCATION DB PROCESS ***");
        upsertLocationData(locationDataList);

        logger.info("*** 3. LOCATION WEBSOCKET PROCESS ***");
        sendWebsocketAPI(locationDataList, "location");

        logger.info("*** 4. LOCATION CHECK CRITICAL AREA ***");
        checkCriticalArea(locationDataList);
        logger.info("Location Process: cycle exec time : {}ms ", System.currentTimeMillis() - sTime);

    }

    private List<Map<String, Object>> requestAPIbySector() {

        logger.info(">>> API Request sectorId({})", SECTOR_ID);
        ResponseEntity<String> responseEntity = requestAPI();
        if (responseEntity == null) {
            logger.error("API response is NULL.  -> continue");
        }

        Map<String, Object> bodyDataMap = Util.jsonToMap(responseEntity.getBody());
        if (bodyDataMap.get("message").toString().equals("fail")) {
            logger.error("API response, value of 'message' Key is 'fail'. (need confirm nineOne)  -> continue");
        }

        List<Map<String, Object>> locationDataMapList = (List<Map<String, Object>>) bodyDataMap.get("userLocations");
        if (locationDataMapList == null || locationDataMapList.isEmpty()) {
            logger.error("'userLocations' Data is NULL or Empty -> continue");
        }

        String regDate = "";
        if (regDate.equals("")) regDate = Util.dateFormat((String) bodyDataMap.get("today"), "yyyyMMddHHmmss");

        List<Map<String, Object>> locationDataList = parseData(locationDataMapList, regDate);

        int size = locationDataList.size();
        logger.info("Location Data List Size : {}", size);
        return size != 0 ? locationDataList : null;
    }

    private List<Map<String, Object>> parseData(List<Map<String, Object>> lList, String regDate) {
        logger.info(">>> parse data");
        List<Map<String, Object>> locationDataList = new ArrayList<>();
        for (Map lData : lList) {
            try {
                Map<String, Object> m = new HashMap<>();
                m.put("DKEY", lData.get("user_id").toString());
                m.put("REGDATE", regDate);
                m.put("BUILDING_ID", (int)Double.parseDouble(lData.get("building_id").toString()));
                m.put("BATT", battValToPercentage(Double.parseDouble(lData.get("batt").toString())));
                m.put("X", Double.parseDouble(lData.get("x").toString()));
                m.put("Y", Double.parseDouble(lData.get("y").toString()));
                m.put("Z", Double.parseDouble(lData.get("level_id").toString()));
                m.put("NAME", makeName(lData.get("user_id").toString()));
                locationDataList.add(m);

                logger.info("----- {} -----", m.get("DKEY"));
                logger.info("      REGDATE : {}", m.get("REGDATE"));
                logger.info("  BUILDING_ID : {}", m.get("BUILDING_ID"));
                logger.info("         BATT : {}", m.get("BATT"));
                logger.info("            X : {}", m.get("X"));
                logger.info("            Y : {}", m.get("Y"));
                logger.info("            Z : {}", m.get("Z"));
                logger.info("         NAME : {}", m.get("NAME"));

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        logger.info("----------\n");
        return locationDataList;
    }

    private String makeName(String dkey) {
        return Integer.parseInt(dkey.replace("Tag_", ""),16) + "";
    }

    private double battValToPercentage(double batt) {
        double p;
        if (batt <= 3.2) {
            p = 0.0;
        } else if (3.2 < batt && batt <= 3.3) {
            p = 5.0;
        } else if (3.3 < batt && batt <= 3.5) {
            p = 10.0;
        } else if (3.5 < batt && batt < 3.6) {
            p = 15.0;
        } else if (3.6 <= batt && batt < 4.2) {
            p = 20 + ((batt - 3.6) * 10) * 13.4;
        } else {
            p = 100.0;
        }
        return p;
    }

    private ResponseEntity<String> requestAPI() {
        logger.info("*** Request API");
        logger.info("sector_id: {}", SECTOR_ID);
        logger.info("token: {}", LOCATION_TOKEN);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("sector_id", SECTOR_ID);
        bodyMap.put("token", LOCATION_TOKEN);
        String body = Util.mapToJson(bodyMap);
        return restService.requestRestAPI(body, LOCATION_URL, HttpMethod.POST);
    }

    private void upsertLocationData(List<Map<String, Object>> locationDataList) {
        try {
            //select dkey list
            logger.info("select dkey list");
            Set<String> dKeySet = locationMapper.selectDkeyList();

            logger.info("list size : {}", locationDataList.size());
            //insert List, update List
            List<Map<String, Object>> insertList = new ArrayList<>();
            List<Map<String, Object>> updateList = new ArrayList<>();

            for(Map<String, Object> m:locationDataList){
                if(dKeySet.contains(m.get("DKEY"))){
                    updateList.add(m);
                }else{
                    insertList.add(m);
                }
            }

            logger.info("insertList size : {}, updateList size : {}",insertList.size(), updateList.size());

            logger.info("insertLocationDataHistory");
            locationMapper.insertLocationDataHistory(locationDataList);
            if(insertList.size() >= 1){
                logger.info("insertCurrLocationData");
                locationMapper.insertCurrLocationData(insertList);
            }
            if(updateList.size() >= 1){
                logger.info("updateCurrLocationData");
                locationMapper.updateCurrLocationData(updateList);
            }
            logger.info("done database process");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void sendWebsocketAPI(List<Map<String, Object>> lList, String type) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("type", type);
        bodyMap.put("data", lList);
        String body = Util.mapToJson(bodyMap);
        restService.requestRestAPI(body, WEB_SOCKET_URL, HttpMethod.POST);
    }

    private void sendWebsocketAPI(Map<String, Object> lMap, String type) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("type", type);
        bodyMap.put("data", lMap);
        String body = Util.mapToJson(bodyMap);
        restService.requestRestAPI(body, WEB_SOCKET_URL, HttpMethod.POST);
    }

    private void checkCriticalArea(List<Map<String, Object>> lList) {
        List<Map<String, Object>> criticalAreaList = locationMapper.selectCriticalAreaList();
        for (Map criticalArea : criticalAreaList) {
            for (Map locationData : lList) {
                if (isInCriticalArea(criticalArea, locationData)) {
                    if ((locationMapper.hasOccuredLastOneMinute(locationData) == 0)) {
                        locationMapper.insertEventList(locationData);
                        sendWebsocketAPI(locationData, "fault");
                    }
                }
            }
        }
    }

    private boolean isInCriticalArea(Map<String, Object> criticalArea, Map<String, Object> locationData) {
        double x = Double.parseDouble(locationData.get("X").toString());
        double y = Double.parseDouble(locationData.get("Y").toString());

        double critical_x_min = Double.parseDouble(criticalArea.get("box_0").toString());
        double critical_x_max = Double.parseDouble(criticalArea.get("box_9").toString());
        double critical_y_min = Double.parseDouble(criticalArea.get("box_8").toString()) * (-1);
        double critical_y_max = Double.parseDouble(criticalArea.get("box_2").toString()) * (-1);

        if ((critical_x_min <= x && x <= critical_x_max) &&
                (critical_y_min <= y && y <= critical_y_max)) {
            logger.info("{} is in Critical Area ({})!!", locationData.get("DKEY"), criticalArea.get("box_name"));

            locationData.put("BOX_IDX", criticalArea.get("box_idx"));
            locationData.put("BOX_NAME", criticalArea.get("box_name"));

            return true;
        }
        return false;
    }

    public String getWEB_SOCKET_URL() {
        return WEB_SOCKET_URL;
    }
}
