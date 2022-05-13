# multi-image-test
multi-module project to explore e2e tests of several dockered modules on spring boot

<b>Module-A</b>- a separate dockered module which receives task requests via HTTP and transmits them to Module-B

<b>Module-B</b> - receives requests from Module-A via Solace, makes some operation on it sends the reply to some outside endpoint, listening on another Solace topic

<b>it</b> - module with e2e tests. Check AllContainersE2ETests.java
<hr/>

<h2>Prepare environment : Hints</h2>

### Spring cloud config server

Currently proper way to use Config Server is to run it from independent repo: https://github.com/hyper-egor/conf-server
<br/>
This config server uses file system based YAML filed as a source.

<br/>
Old, deprecated version of Config server is located in this project, module 'config-server' and could be executed like that:
<br/>
<code>
cd config-server<br/>
gradle build<br/>
gradle docker<br/>
gradle dockerRun
</code><br/>
Data for this server is sourced from git (see server's application.properties)

### Install Solace brocker image
(https://docs.solace.com/Solace-SW-Broker-Set-Up/Docker-Containers/Set-Up-Docker-Container-Windows.htm)
<ol>
	<li><code>git clone https://github.com/SolaceLabs/solace-single-docker-compose.git</code><br/>
		<code>cd solace-single-docker-compose/template</code></li>
	<li><code>docker-compose -f PubSubStandard_singleNode.yml up -d</code>
	<li>Stop executed container on prev step</li>
</ol>

Solace container could be run either from terminal using commands below or using method AllContainersE2ETests.runSolaceContainer();

Run Solace container manually from terminal:
<br/>
<code>docker run -d -p 8080:8080 -p 55555:55555 -p:8008:8008 --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin --name=solace solace/solace-pubsub-standard
</code>

<br/>

After Solace container is up we must create queue in the <b>default</b> VPN (see URL  below):
<br/>
<code>HTTP POST http://localhost:8080/SEMP/v2/config/msgVpns/default/queues
</code>
POST request body:
<br/>
<code>{"queueName":"A2Bqueue","accessType":"non-exclusive","egressEnabled":true,"ingressEnabled":true,"permission":"consume"}</code>
<br/>
<b>A2Bqueue</b> - is the queue name specified in the config.

### e2e test run configuration

it's important to enhance test configuration with VM options: <code>-Dconfig_server_host=localhost</code>

### Extras
Just for hello-world try's: to run manually just one module-a container from terminal in container we can do the following:
<br/>
<code>docker run -d -it -p:81:8081 --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin --name=aaa --link conf-server --link solace 76e28ae5afa7
</code>
<br/>
in the example above <b>76e28ae5afa7</b> is the docker imange id which is available in the Docker desctop GUI after we run 'gradle docker' in corresponding module.

