package com.bnpp.examples.sboot.it;

import com.bnpp.examples.sboot.HTTPController;
import com.bnpp.examples.sboot.utils.DockerUtils;
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
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.NoSuchElementException;

import static com.bnpp.examples.sboot.utils.FileUtils.getDockerFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner/*SpringRunner*/.class)
@Testcontainers
@SpringBootTest
public class MyTest {
    private static final Logger log = LoggerFactory.getLogger(MyTest.class);

    public static final String SOLACE_IMAGE = "solace/solace-pubsub-standard";
    public static final String SOLACE_HTTP_USER = "admin";
    public static final String SOLACE_HTTP_PASS = "admin";

    public static final String CONFIG_SERVER_DOCKER_HOST = "conf-server";
    public static String configServerHostIP;

    @Value("${solace.host}")
    public static final String SOLACE_DOCKER_HOST="";

    public static String solaceHostIP;
    @Value("${configdir}")
    private static String configdir;

    @ClassRule
    public static Network network= Network.newNetwork();

    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {

            GenericContainer configContainer =  new GenericContainer(CONFIG_SERVER_DOCKER_HOST)
                    .withNetwork(AllContainersE2ETests.network)
                    .withExposedPorts(8888)
                    .withNetworkAliases(CONFIG_SERVER_DOCKER_HOST)
                    .withFileSystemBind( configdir,"/config-storage", BindMode.READ_WRITE)
                    .waitingFor(
                            Wait.forLogMessage(".*Started ConfigServerApplication.*", 1).
                                    withStartupTimeout(Duration.ofMinutes(2))
                    );

            configContainer.start();

            log.info("Config-server started");
        }

        @Override
        protected void after() {
            // nope
        }
    };

    /**
     *
     */


    private GenericContainer runSolaceContainer() throws Exception {
        log.info("Starting solace...");
        GenericContainer solaceContainer =
                new GenericContainer(SOLACE_IMAGE)
                        .withEnv("username_admin_globalaccesslevel", SOLACE_HTTP_USER)
                        .withEnv("username_admin_password", SOLACE_HTTP_PASS)
                        .withNetworkAliases(SOLACE_DOCKER_HOST)
                        .withNetwork(network)
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

        return solaceContainer;
    }


}

