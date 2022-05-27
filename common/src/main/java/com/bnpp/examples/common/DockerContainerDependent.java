package com.bnpp.examples.common;

import com.bnpp.examples.common.annotations.DependOnDockerContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.Arrays;

public abstract class DockerContainerDependent {
    protected static Network network= Network.newNetwork();
    protected static GenericContainer configContainer;
    protected static final String configContainerName="conf-server";
    protected static final String configContainerPath="/home/pavel/IdeaProjects/example-configs";
    protected static GenericContainer solaceContainer;
    protected static final String solaceContainerName="solace";
    protected static final String solaceContainerImage="solace/solace-pubsub-standard";
    protected static final String SOLACE_HTTP_USER = "admin";
    protected static final String SOLACE_HTTP_PASS = "admin";


    public DockerContainerDependent() {
        if(this.getClass().isAnnotationPresent(DependOnDockerContainer.class)) {
            Arrays.asList(this.getClass().getAnnotation(DependOnDockerContainer.class).containerName())
                    .forEach(containerName->{
                                switch (containerName) {
                                    case configContainerName: getConfigContainer(); break;
                                    case solaceContainerName: getSolaceContainer(); break;
                                }
                            }
                    );
        }


    }

    public synchronized static GenericContainer getConfigContainer() {
        if(configContainer==null) {
            configContainer =  new GenericContainer(configContainerName)
                    .withNetwork(network)
                    .withExposedPorts(8888)
                    .withNetworkAliases(solaceContainerName)
                    .withFileSystemBind(configContainerPath,"/config-storage", BindMode.READ_WRITE)
                    .waitingFor(
                            Wait.forLogMessage(".*Started ConfigServerApplication.*", 1).
                                    withStartupTimeout(Duration.ofMinutes(2))
                    );

            configContainer.start();
        }
        return configContainer;
    }

    public synchronized static GenericContainer getSolaceContainer() {

        if(solaceContainer==null) {
            solaceContainer =
                    new GenericContainer(solaceContainerImage)
                            //    .withExposedPorts(SOLACE_HTTP_PORT_INSIDE_DOCKER, 55555, 8008)
                            .withEnv("username_admin_globalaccesslevel", SOLACE_HTTP_USER)
                            .withEnv("username_admin_password", SOLACE_HTTP_PASS)
                            .withNetworkAliases(solaceContainerName)
                            .withAccessToHost(true)
                            .withNetwork(network)
                            .withSharedMemorySize(2_000_000_000L) // min 1Gb needed
                            .waitingFor(
                                    Wait.forLogMessage(".*Running pre-startup checks:.*", 1).
                                            withStartupTimeout(Duration.ofMinutes(2))
                            )
                            .waitingFor(Wait.forListeningPort());

            solaceContainer.start();
        }

        return solaceContainer;
    }

}
