package com.bnpp.examples.sboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class HTTPController {

    public static final String TASK_ACCEPTED_OK_SIGN = "Task accepted.";
    public static final String WRONG_TASK_REQUEST_SIGN = "Something wrong with task request";

    @Autowired
    private JMSSender jmsSender;

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    public String taskReceiver(@RequestParam(name = "taskId", required = false) String _taskId) throws IOException {
        long taskId;
        try {
            taskId = Long.parseLong(_taskId);
        } catch (Exception e)
        {
            return WRONG_TASK_REQUEST_SIGN + ": taskId should be 'long";
        }

        String message2moduleB = "Hi module-b, this is module-a! I've just received task " + taskId + ". Result of my work is: " + (taskId+1);
        String result;
        try {
            jmsSender.sendMessage(message2moduleB);
            result = "Sent to module-b OK.";
        } catch (Exception e)
        {
            result = "Couldn't send to module-b: " + e.getMessage();
        }

        return TASK_ACCEPTED_OK_SIGN + " Task id: " + taskId + "; result: " + result;
    }
}
