package com.bnpp.examples.sboot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DockerUtils {

    private static final String IP_ADDRESS_PARAM_NAME = "\"IPAddress\"";

    /**  */
    public static String getContainerIP(String containerHostName)
    {
        String ip = null;
        try {
            Process p = runCommand("docker inspect " + containerHostName);
            String result = readCommandStdOut(p);
            // We're looking for '"IPAddress": "172.17.0.2"' substring
            int ind = result.lastIndexOf( IP_ADDRESS_PARAM_NAME );
            if (ind > 0)
            {
                int ind1 = result.indexOf( "\"", ind + IP_ADDRESS_PARAM_NAME.length() );    // first "
                int ind2 = result.indexOf( "\"", ind1 + 1 );                                // second "
                ip = result.substring(ind1+1, ind2);
            }
        } catch (Exception e)
        {
            System.out.println("Failed to get IP from 'docker inspect '" + containerHostName + "' command; " + e.getMessage());
            e.printStackTrace();
        }

        return ip;
    }

    /**  */
    public static void startDocker(String containerId) {
        if (!checkAlive(containerId)) {
            runCommand("docker start " + containerId);
            System.out.println("Trying to start " + containerId + " container");
        } else
            System.out.println("Container is up-and-running. I don't do nothing");
    }

    /**  */
    public static void stopDocker(String containerId) {
        if (checkAlive(containerId)) {
            System.out.println("Trying to stop " + containerId + " container");
            runCommand("docker stop " + containerId);
        } else
            System.out.println("Container is not working. I don't do nothing");
    }

    /**  */
    private static boolean checkAlive(String containerId) {
        System.out.println("Checking the container " + containerId);
        Process p = runCommand("docker inspect -f {{.State.Running}} " + containerId);
        return readCommandResult(p);
    }

    /**  */
    private static Process runCommand(String command) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    /**  */
    private static String readCommandStdOut(Process proc) {
        String rv = "";
        try {
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            // read the output from the command
            String s;
            while ((s = stdInput.readLine()) != null) {
                rv += s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rv;
    }

    /**  */
    private static boolean readCommandResult(Process proc) {
        String s;
        boolean result = false;
        try {
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                System.out.println("Container is working: " + s);
                if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
                    result = Boolean.parseBoolean(s);
            }
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}