package com.bnpp.examples.sboot.it;

import com.bnpp.examples.common.DockerContainerDependent;
import com.bnpp.examples.common.annotations.DependOnDockerContainer;
import com.bnpp.examples.sboot.HTTPController;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner/*SpringRunner*/.class)
@Testcontainers
@DependOnDockerContainer(containerName = {"module-a","module-b"})
public class MyTest extends DockerContainerDependent  {
    private static final Logger log = LoggerFactory.getLogger(MyTest.class);


    private static long RANDOM_TASK_ID=13414;

    @Test
    public void testCrossModuleTransmition()   {

        GenericContainer moduleAContainer=getGenericContainer("module-a");

        assertTrue(moduleAContainer.isRunning());

        String address = "http://"
                + getIp(moduleAContainer)
                + ":8080/execute?taskId=" + RANDOM_TASK_ID;

        log.info("Constructed Module-a container URL: " + address);

        RestTemplate restTemplate = new RestTemplate();
        String reply = restTemplate.getForObject(address, String.class);
        log.info("Container reply: [" + reply + "]");
        assertThat(reply).contains(HTTPController.TASK_ACCEPTED_OK_SIGN);


    }




}

