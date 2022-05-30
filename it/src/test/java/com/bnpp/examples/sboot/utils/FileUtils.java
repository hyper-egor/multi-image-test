package com.bnpp.examples.sboot.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileUtils {

    /**
     * @param dockeredModuleSubModuleName - name of folder of sub-module where desired dockered module is located
     * @return path to Dockerfile (if exists), otherwise : null
     */
    public static Path getDockerFilePath(String dockeredModuleSubModuleName)
    {
        Path parentProjectPath = Paths.get("").toAbsolutePath();
        Path moduleProjectPath = parentProjectPath.resolve(dockeredModuleSubModuleName);
        File file = new File(moduleProjectPath.toString());
        if (!file.exists())
        {   // here we try to jump to parent folder and find sub-module there.
            // such a huck is performed as this search could be initiated from proj root, OR from sub-module's folder
            moduleProjectPath = parentProjectPath.getParent().resolve(dockeredModuleSubModuleName);
            file = new File(moduleProjectPath.toString());
            if (!file.exists())
                return null;
        }
        Path moduleDockerfilePath = moduleProjectPath.resolve("Dockerfile");
        file = new File(moduleDockerfilePath.toString());
        if (!file.exists())
            return null;

        return moduleDockerfilePath;
    }
}
