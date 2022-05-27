package com.bnpp.examples.sboot;

import com.bnpp.examples.common.DockerContainerDependent;
import com.bnpp.examples.common.annotations.DependOnDockerContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner/*SpringRunner*/.class)
@SpringBootTest
@DependOnDockerContainer(containerName = "solace")
public class SolaceTest extends DockerContainerDependent {
    private static final Logger log = LoggerFactory.getLogger(SolaceTest.class);

    @Value("${solace.jms.msgVpn}")
    public String SOLACE_VPN_NAME;

    @Value("${solace.queueName}")
    public String SOLACE_QUEUE_NAME;

    @Value("${solace.httpApi.port}")
    public int SOLACE_HTTP_PORT;

    @Value("${solace.http.user}")
    public String SOLACE_HTTP_USER;
    @Value("${solace.http.pass}")
    public String SOLACE_HTTP_PASS;

    @Test
    public void testSolaceContainerIsUp() throws Exception {

//        String request = "{\"queueName\":\"" + SOLACE_QUEUE_NAME + "\",\"accessType\":\"non-exclusive\",\"egressEnabled\":true,\"ingressEnabled\":true,\"permission\":\"consume\"}";
        String request = "{}";

        log.info("Body: " + request);

        ResponseEntity<String> respEntity = httpPostSolace(request);
        String reply = respEntity.toString();

        log.info("Solace replyed: " + reply);

        assertEquals(HttpStatus.OK,respEntity.getStatusCode());

    }

    private ResponseEntity<String> httpPostSolace(String request) {
        String url = "http://"+solaceContainer.getHost()+":" + SOLACE_HTTP_PORT
                + "/SEMP/v2/config/msgVpns/" + SOLACE_VPN_NAME + "/queues";
        log.info("Constructed Solace create queue HTTP POST URL: " + url);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(SOLACE_HTTP_USER, SOLACE_HTTP_PASS);
        HttpEntity<String> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    }
}
