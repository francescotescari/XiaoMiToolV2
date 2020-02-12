package com.xiaomitool.v2.process;

import com.xiaomitool.v2.resources.ResourcesConst;

import java.util.List;

public class ShellRunner extends ProcessRunner {
    private StringBuilder builder = null;

    public ShellRunner(String... args) {
        super(ResourcesConst.getShellPath(), ResourcesConst.getShellArgs());
        if (args != null && args.length > 0) {
            for (String arg : args) {
                this.addArgument(arg);
            }
        }
    }

    @Override
    public void addArgument(String args) {
        if (builder == null) {
            builder = new StringBuilder(args);
        } else {
            builder.append(" ").append(args);
        }
    }

    @Override
    public List<String> buildFinalArgumentsList() {
        if (builder != null) {
            super.addArgument(builder.toString());
        }
        return super.buildFinalArgumentsList();
    }
}
