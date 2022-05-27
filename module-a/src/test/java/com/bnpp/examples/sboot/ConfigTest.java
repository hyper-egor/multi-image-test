package com.bnpp.examples.sboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner/*SpringRunner*/.class)
@SpringBootTest
public class ConfigTest  {
    private static final Logger log = LoggerFactory.getLogger(ConfigTest.class);
/*    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {

        @Override
        protected void before() throws Throwable {
            DockerContainerDependent.getConfigContainer();

        }
    };*/
    @Value("${configuration}")
    public String CONFIGURATION;

    @Test
    public void testConfigContainerIsUp()  {
        System.out.println("Config server is up and running, configuration:"+ CONFIGURATION);
        org.junit.Assert.assertNotNull(CONFIGURATION);

    }

}
