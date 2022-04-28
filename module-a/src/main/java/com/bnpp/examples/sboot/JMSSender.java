package com.bnpp.examples.sboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class JMSSender {

    private static final Logger log = LoggerFactory.getLogger(JMSSender.class);

    @Value("${solace.queueName}")
    private String queueName;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    private void customizeJmsTemplate() {
        // Update the jmsTemplate's connection factory to cache the connection
        CachingConnectionFactory ccf = new CachingConnectionFactory();
        ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
        jmsTemplate.setConnectionFactory(ccf);

        // By default Spring Integration uses Queues, but if you set this to true you
        // will send to a PubSub+ topic destination
        jmsTemplate.setPubSubDomain(false);
    }

    /**     */
    public void sendMessage(String message) throws Exception {
        log.info("==== Sending message [" + message + "] ====");
        jmsTemplate.convertAndSend(queueName, message);
    }

}