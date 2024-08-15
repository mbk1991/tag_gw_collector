package kr.co.nextcore.taglocationandgwcollector.scheduler;

import kr.co.nextcore.taglocationandgwcollector.common.DBManageService;
import kr.co.nextcore.taglocationandgwcollector.gateway.GatewayService;
import kr.co.nextcore.taglocationandgwcollector.location.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@EnableScheduling
public class APIScheduler {

    @Autowired
    LocationService locationService;
    @Autowired
    GatewayService gatewayService;
    @Autowired
    DBManageService dbManageService;

    final String LOCATION_CRON = "*/5 * * * * *";

    final String GATEWAY_CRON = "0 0 0,12 * * *";

    final String DB_MANAGE_CRON = "0 0 1 * * 1";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = LOCATION_CRON)
    void collectLocationData(){
        logger.info("\n[[[Scheduled: Location Data Process]]]");
        locationService.locationDataProcess();
    }

    @Scheduled(cron = GATEWAY_CRON)
    void collectGatewayStatusData(){
        logger.info("\n[[[Scheduled: GW Data Process]]]");
        gatewayService.gatewayDataProcess();
    }

    @Scheduled(cron = DB_MANAGE_CRON)
    void manageDatabase(){
        logger.info("\n[[[Scheduled: Manage Database Process]]]");
        dbManageService.deleteHistoryProcess();
    }



}
