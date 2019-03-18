package com.xiaomitool.v2.process;

import com.xiaomitool.v2.resources.ResourcesConst;

import java.nio.file.Path;

public class ShellRunner extends ProcessRunner {
    public ShellRunner(String... args) {
        super(ResourcesConst.getShellPath(), ResourcesConst.getShellArgs());
        if (args != null && args.length > 0){
            for (String arg : args){
                this.addArgument(arg);
            }
        }
    }
}
