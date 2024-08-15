package kr.co.nextcore.taglocationandgwcollector.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RestService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ResponseEntity<String> requestRestAPI(String body, String url, HttpMethod method) {
        try{
            logger.info(">>>Request Rest API");
            logger.info("URL: {} METHOD: {}", url, method);
            HttpEntity<String> requestEntity = new HttpEntity<>(body, makeHeader());
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
            HttpStatusCode statusCode = responseEntity.getStatusCode();
            logger.info("responseCode >> {}", statusCode);
            if(!statusCode.is2xxSuccessful()) throw new ResponseStatusException(statusCode);
            return responseEntity;

        }catch(ResponseStatusException e){
            logger.error(e.getMessage());
            return null;

        }catch(Exception e){
            logger.error(e.getMessage());
            return null;

        }
    }

    private HttpHeaders makeHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setCacheControl(CacheControl.noCache());
        return header;
    }

}
