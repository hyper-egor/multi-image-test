package com.bnpp.examples.sboot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(/* Random server port to avoid conflicts with running app */
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModuleATestPack {

    private static final Logger log = LoggerFactory.getLogger(ModuleATestPack.class);

    @LocalServerPort
    private int port;

    @Autowired
    private HTTPController httpController;

    @Autowired
    private TestRestTemplate restTemplate;

    // run's on 'gradle build'
    @Test
    void contextLoads() throws Exception {

        long randomedTaskId = (long)(Math.random() * Long.MAX_VALUE);

        assertThat( httpController ).isNotNull();
        String reply = this.restTemplate.getForObject("http://localhost:" + port + "/execute?taskId=" + randomedTaskId, String.class);
        log.info("Service reply: [" + reply + "]");
        assertThat(reply).contains( HTTPController.TASK_ACCEPTED_OK_SIGN );
    }

}
