package kr.co.nextcore.taglocationandgwcollector.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DBManageService {
    @Autowired
    DBManageMapper dbManageMapper;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public void deleteHistoryProcess() {
        String beforeOneMonth = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        logger.info(">>>delete before {} ", beforeOneMonth);
        logger.info("deleteLocationHisDataBeforeOneMonth");
        dbManageMapper.deleteLocationHisDataBeforeOneMonth(beforeOneMonth);
        logger.info("deleteGWHisDataBeforeOneMonth");
        dbManageMapper.deleteGWHisDataBeforeOneMonth(beforeOneMonth);
    }
}
