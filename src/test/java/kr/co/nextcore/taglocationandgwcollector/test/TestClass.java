package kr.co.nextcore.taglocationandgwcollector.test;

import com.mysql.cj.jdbc.Clob;
import kr.co.nextcore.taglocationandgwcollector.common.DBManageService;
import kr.co.nextcore.taglocationandgwcollector.common.Util;
import kr.co.nextcore.taglocationandgwcollector.gateway.GatewayService;
import kr.co.nextcore.taglocationandgwcollector.location.LocationService;



import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class TestClass {
    @Autowired
    LocationService locationService;
    @Autowired
    GatewayService gatewayService;
    @Autowired
    DBManageService dbManageService;

    @Test
    void LocationProcess_테스트(){
        //locationDataProcess all test
        locationService.locationDataProcess();
    }

    @Test
    void GatewayProcess_테스트(){
        //gatewayDataProcess all test
        gatewayService.gatewayDataProcess();
    }

    @Test
    void DBManageProcess_테스트(){
        dbManageService.deleteHistoryProcess();
    }

    
    @Test
    void MapToJSON_변환테스트(){
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        String s = Util.mapToJson(map);
        assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}",s);
    }

    @Test
    void DKEYtoName_파싱테스트(){
        String dkey = "Tag_0000001A"; //
        String tag = dkey.replace("Tag_", "");
        int i = Integer.parseInt(tag, 16);
        assertEquals("26", i+"");
    }

    @Test
    void GETDATE_파싱테스트(){
        String date= "2024-05-22 18:24:20";
        LocalDateTime getDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yyyyMMddHHmmss = getDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        assertEquals("20240522182420", yyyyMMddHHmmss);
    }

    @Test
    void GW_NAME_파싱테스트(){
        //FFF00002 -> 002
        String gwName = "FFF00002";
        String test = gwName.substring(gwName.length()-3);
        assertEquals("002", test);
    }

    @Test
    void 로깅테스트1_java_util_logging(){
//        Logger logger = Logger.getLogger("java.tuil.logger.test");
//        logger.info("java.util.Logger info()");
//        logger.fine("java.util.Logger fine()");
//        logger.warning("java.util.Logger warning()");
    }

    @Test
    void 로깅테스트(){
        Logger logger = LoggerFactory.getLogger("로거 이름");
        logger.debug("debug!");
        logger.info("info!");
        logger.warn("warn!");
        logger.error("error!");
        System.out.println("logger.getClass() = " + logger.getClass());
        System.out.println("logger.getName() = " + logger.getName());
        System.out.println("logger.toString() = " + logger.toString());
    }

    @Test
    void getClassLoader_출력확인(){
        ClassLoader classLoader = this.getClass().getClassLoader();
        System.out.println("classLoader.getName() = " + classLoader.getName());
        System.out.println("classLoader.toString() = " + classLoader.toString());

        System.out.println("System.getProperty(\"slf4j.provider\") = " + System.getProperty("slf4j.provider"));
    }

    @Test
    void 서비스프로바이더_테스트(){
        ClassLoader l = this.getClass().getClassLoader();
        ServiceLoader<Clob> serviceList = ServiceLoader.load(Clob.class, l);
        System.out.println("serviceList = " + serviceList);
        Iterator i = serviceList.iterator();
        while(i.hasNext()){
            System.out.println("-");
            Clob p = (Clob) i.next();
            System.out.println("p.getClass().getName() = " + p.getClass().getName());
            System.out.println("p.toString() = " + p.toString());

        }

    }

    @Test
    void websocketurl_env테스트(){
        String webSocketUrl = locationService.getWEB_SOCKET_URL();
        System.out.println("webSocketUrl = " + webSocketUrl);
    }
    
}
