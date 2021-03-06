package com.bnpp.examples.sboot.it;

import com.bnpp.examples.sboot.HTTPController;
import com.bnpp.examples.sboot.utils.DockerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;

import static com.bnpp.examples.sboot.utils.FileUtils.getDockerFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/* below annotation is to enable read properties */
@RunWith(SpringJUnit4ClassRunner/*SpringRunner*/.class)
@Testcontainers
@SpringBootTest
public class AllContainersE2ETests {
    private static final Logger log = LoggerFactory.getLogger(AllContainersE2ETests.class);

    @Value("${module-a.server.port}")
    public int moduleAServerPort;

    @Value("${solace.jms.msgVpn}")
    public String SOLACE_VPN_NAME;

    @Value("${solace.queueName}")
    public String SOLACE_QUEUE_NAME;

    @Value("${solace.httpApi.port}")
    public int SOLACE_HTTP_PORT_INSIDE_DOCKER;

    public static final String SOLACE_IMAGE = "solace/solace-pubsub-standard";
    public static final String SOLACE_HTTP_USER = "admin";
    public static final String SOLACE_HTTP_PASS = "admin";

    /*  Docker container's name ('host' in docker network)
        TODO: move this magic key to config..
     */
    public static final String CONFIG_SERVER_DOCKER_HOST = "conf-server";
    public static String configServerHostIP;

    public static final String SOLACE_DOCKER_HOST = "solace";
    public static String solaceHostIP;

    private long RANDOM_TASK_ID;
    private GenericContainer moduleAContainer;
    private String address;

    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            configServerHostIP = DockerUtils.getContainerIP(CONFIG_SERVER_DOCKER_HOST);
            log.info("========= IP for " + CONFIG_SERVER_DOCKER_HOST + " is " + configServerHostIP);

            // Try recv solace IP : for cases when we pre-run Solace and create it's queues manually
            solaceHostIP = DockerUtils.getContainerIP(SOLACE_DOCKER_HOST);
            log.info("========= IP for " + SOLACE_DOCKER_HOST + " is " + solaceHostIP);
        }

        @Override
        protected void after() {
            // nope
        }
    };

    @Before
    public void beforeTest() throws Exception {
        checkSolace();
        RANDOM_TASK_ID = (long) (Math.random() * Long.MAX_VALUE);
        moduleAContainer = runModuleAContainer();
        log.info("====== Module-A server port: " + moduleAServerPort);
        address = "http://"
                + moduleAContainer.getHost()
                + ":" + moduleAContainer.getMappedPort(moduleAServerPort) + "/execute?taskId=" + RANDOM_TASK_ID;
    }

    @After
    public void afterTest() {
        moduleAContainer.stop();
    }

    @Test
    public void testCrossModuleTransmition() {
        log.info("Constructed Module-a container URL: " + address);
        RestTemplate restTemplate = new RestTemplate();
        String reply = restTemplate.getForObject(address, String.class);
        log.info("Container reply: [" + reply + "]");
        log.info(reply);
        assertThat(reply).contains(HTTPController.TASK_ACCEPTED_OK_SIGN);
    }

    @Test
    public void testCrossModuleTransmitionNoError() {
        log.info("Constructed Module-a container URL: " + address);
        RestTemplate restTemplate = new RestTemplate();
        String reply = restTemplate.getForObject(address, String.class);
        log.info("Container reply: [" + reply + "]");
        assertFalse(reply.contains("Error"));
    }

    /**
     *
     */
    private GenericContainer runSolaceContainer() throws Exception {
        log.info("Starting solace...");
        GenericContainer solaceContainer =
                new GenericContainer(SOLACE_IMAGE)
                        .withExposedPorts(SOLACE_HTTP_PORT_INSIDE_DOCKER, 55555, 8008)
                        .withEnv("username_admin_globalaccesslevel", SOLACE_HTTP_USER)
                        .withEnv("username_admin_password", SOLACE_HTTP_PASS)
                        .withNetworkAliases(SOLACE_DOCKER_HOST)
                        .withSharedMemorySize(2_000_000_000L) // min 1Gb needed
                        .waitingFor(
                                Wait.forLogMessage(".*Running pre-startup checks:.*", 1).
                                        withStartupTimeout(Duration.ofMinutes(2))
                        );
        try {
            solaceContainer.start();
            log.info("============= Solace started with ip: " + solaceContainer.getContainerInfo().getNetworkSettings().getIpAddress());
        } catch (Exception e) {
            log.info("============= Failed to start Solace");
            log.info("============= S1 " + solaceContainer.getLogs());
            throw e;
        } finally {
            // Check logs is useful to find key log string signaling that Solace started OK (for waitingFor(..) construction)
            log.info("============= S1 " + solaceContainer.getLogs());
        }

        // Create queue (example URL: http://localhost:8080/SEMP/v2/config/msgVpns/default/queues)
        String url = "http://"
                + solaceContainer.getHost()
                + ":" + solaceContainer.getMappedPort(SOLACE_HTTP_PORT_INSIDE_DOCKER)   // get 'outside' port mapped to port inside docker
                + "/SEMP/v2/config/msgVpns/" + SOLACE_VPN_NAME + "/queues";
        String request = "{\"queueName\":\"" + SOLACE_QUEUE_NAME + "\",\"accessType\":\"non-exclusive\",\"egressEnabled\":true,\"ingressEnabled\":true,\"permission\":\"consume\"}";
        log.info("Constructed Solace create queue HTTP POST URL: " + url);
        log.info("Body: " + request);
        // TODO: find better way to correctly diagnose full Solace startup for not to wait 20 sec here
        Thread.sleep(20_000L); // TODO: if exist 'healthCkecks'
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(SOLACE_HTTP_USER, SOLACE_HTTP_PASS);
        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        String reply = respEntity.toString();
        log.info("Solace replyed: " + reply);
        return solaceContainer;
    }

    private void checkSolace() throws Exception {
        if (solaceHostIP == null) {
            // Solace container is not up yet.
            GenericContainer solaceContainer = runSolaceContainer();
            solaceHostIP = solaceContainer.getContainerInfo().getNetworkSettings().getIpAddress();
        }
    }

    private GenericContainer runModuleAContainer() {
        Path moduleADockerfilePath = getDockerFilePath("module-a");
        log.info("====== Dockerfile: " + moduleADockerfilePath);
        GenericContainer moduleAContainer = new GenericContainer(new ImageFromDockerfile()
                .withDockerfile(moduleADockerfilePath))
                .withExposedPorts(moduleAServerPort)
                .withExtraHost(CONFIG_SERVER_DOCKER_HOST, configServerHostIP)
                .withExtraHost(SOLACE_DOCKER_HOST, solaceHostIP)
                .waitingFor(Wait.forListeningPort());   // tell's Testcontainers to wait with tests before the container starts to reply with excpected strategy
        log.info("============= 1 " + moduleAContainer.getLogs());
        try {
            moduleAContainer.start();
        } catch (Exception e) {
            throw e;
        } finally {
            log.info("============= 2 " + moduleAContainer.getLogs());
        }

        assertTrue(moduleAContainer.isRunning());

        return moduleAContainer;
    }
}
