package com.bnpp.examples.sboot;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class HTTPController {

    public static final String TASK_ACCEPTED_OK_SIGN = "Task accepted.";
    public static final String WRONG_TASK_REQUEST_SIGN = "Something wrong with task request";

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    public String taskReceiver(@RequestParam(name = "taskId", required = false) String _taskId) throws IOException {
        long taskId;
        try {
            taskId = Long.parseLong(_taskId);
        } catch (Exception e)
        {
            return WRONG_TASK_REQUEST_SIGN + ": taskId should be 'long";
        }

        // TODO: perform task execution...

        return TASK_ACCEPTED_OK_SIGN + " Task id: " + taskId;
    }
}
