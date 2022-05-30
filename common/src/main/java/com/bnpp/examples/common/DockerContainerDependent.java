package com.bnpp.examples.common;

import com.bnpp.examples.common.annotations.DependOnDockerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DockerContainerDependent {
    private static final Logger log = LoggerFactory.getLogger(DockerContainerDependent.class);
    private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
    protected static Network network= Network.newNetwork();
    protected static ConcurrentHashMap<String,GenericContainer> containerByName=new ConcurrentHashMap<>();
    protected static GenericContainer<?> configContainer;
    protected static final String configContainerName="conf-server";
    protected static final String configContainerPath="/home/pavel/IdeaProjects/example-configs";

    protected static final String solaceContainerName="solace";
    protected static final String solaceContainerImage="solace";
    protected static final String SOLACE_HTTP_USER = "admin";
    protected static final String SOLACE_HTTP_PASS = "admin";


    public DockerContainerDependent() {

        if(this.getClass().isAnnotationPresent(DependOnDockerContainer.class)) {
            Arrays.asList(this.getClass().getAnnotation(DependOnDockerContainer.class).containerName())
                    .forEach(containerName->{
                                switch (containerName) {
                                    case configContainerName: getConfigContainer(); break;
                                    case solaceContainerName: getSolaceContainer(); break;
                                    case "module-a": getModuleAContainer(); break;
                                    default: getGenericContainer(containerName);
                                }
                            }
                    );
        }


    }
    public static String getIp(GenericContainer<?> container) {
        return container.getContainerInfo().getNetworkSettings().getNetworks().values().stream().findAny().get().getIpAddress();
    }

    public static GenericContainer<?> getContainerByName(String containerName) {
        return containerByName.get(containerName);
    }


    public synchronized static GenericContainer<?> getGenericContainer(String containerName) {
        if(getContainerByName(containerName)==null) {
            getConfigContainer();

            GenericContainer container = new GenericContainer(containerName)
                    .withNetworkAliases(containerName)
                    .withNetwork(network)


//                          .waitingFor(Wait.forHealthcheck())
                    ;

            containerByName.put(containerName, container);
        }
        return containerByName.get(containerName);
    }

    public synchronized static GenericContainer<?> getModuleAContainer() {
        GenericContainer container=getGenericContainer("module-a")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/").forStatusCode(404));
        container.start();
        return container;
    }

    public synchronized static GenericContainer<?> getConfigContainer() {
        if(configContainer==null) {
            configContainer =  new GenericContainer(configContainerName)
                    .withNetwork(network)
                    .withNetworkAliases(configContainerName)
                    .withExposedPorts(8888)
                    .withFileSystemBind(configContainerPath,"/config-storage", BindMode.READ_WRITE)
                    .waitingFor(Wait.forHttp("/a/a"))
                    ;

            configContainer.start();
        }
        return configContainer;
    }


    public synchronized static GenericContainer<?> getSolaceContainer() {

        if(getContainerByName(solaceContainerName)==null) {
            getConfigContainer();
            GenericContainer solaceContainer =
                    new GenericContainer(solaceContainerImage)
                            //    .withExposedPorts(SOLACE_HTTP_PORT_INSIDE_DOCKER, 55555, 8008)
                            .withEnv("username_admin_globalaccesslevel", SOLACE_HTTP_USER)
                            .withEnv("username_admin_password", SOLACE_HTTP_PASS)
                            .withNetworkAliases(solaceContainerName)
                            .withNetwork(network)
                            .withExposedPorts(8080)
                            .withSharedMemorySize(2_000_000_000L) // min 1Gb needed
                            .waitingFor(Wait.forHttp("/"))
//                          .waitingFor(Wait.forHealthcheck())
                    ;
            solaceContainer.start();
            try {
                Thread.sleep(3_000L); //to-do wait strategy
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            containerByName.put(solaceContainerName,solaceContainer);
        }
        return containerByName.get(solaceContainerName);
    }


}
