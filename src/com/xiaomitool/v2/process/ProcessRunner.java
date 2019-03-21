package com.xiaomitool.v2.process;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.Thrower;
import com.xiaomitool.v2.utility.WaitSemaphore;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ProcessRunner {
    protected Path executable;
    protected LinkedList<String> arguments;
    private int secondsTimeout = 1800;
    private Process runningProcess = null;
    private int exitValue = 0;
    private ProcessStatus status = ProcessStatus.READY;
    private InputStream stdout, stderr;
    //private Thread syncStdoutThread = null, syncStderrThread = null;
    protected List<String> outputBuffer = Collections.synchronizedList(new LinkedList<>());
    private final WaitSemaphore readFinishedSemaphore = new WaitSemaphore(0);
    private File workingDir;

    public ProcessRunner(Path exe){
        this(exe, null);
    }
    public ProcessRunner(String pathExe, String[] arguments){
        this(Paths.get(pathExe),arguments);
    }
    public ProcessRunner(String pathExe){
        this(Paths.get(pathExe));
    }

    public ProcessRunner(Path exe, String[] arguments){
        this.executable = exe;
        this.arguments = new LinkedList<>();
        if (arguments != null){
            this.arguments.addAll(Arrays.asList(arguments));
        }

    }

    public void setWorkingDir(File workingDir){
        this.workingDir = workingDir;
    }



    enum ProcessStatus{
        READY,
        RUNNING,
        FINISHED,
        EXCEPTION
    }

    private void setStatus(ProcessStatus status){
        this.status = status;
    }
    private Thrower<IOException> IOThrower;


    private Process start() throws IOException {
        List<String> args = buildFinalArgumentsList();
        Log.debug("Process args: ["+String.join(" ",args)+"]");
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        if (this.workingDir != null) {
            builder.directory(this.workingDir);
        }
        Process proc = builder.start();
        IOThrower = new Thrower<>();

        /*OutputStream outputStream = proc.getOutputStream();
        if (outputStream != null){
            outputStream.close();
        }*/
        final InputStream inputStream = proc.getInputStream();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    readFinishedSemaphore.setPermits(0);
                    Scanner scanner = new Scanner(inputStream);
                    scanner.useDelimiter(Pattern.compile("[\\r\\n;]+"));
                    String data;
                    Log.debug("Starting process output read");
                    while (scanner.hasNextLine()) {
                        data = manageLineOutput(scanner.nextLine());
                        if (data == null) {
                            continue;
                        }
                        outputBuffer.add(data);
                        Log.debug(data);
                        for (RunnableWithArg toDo : syncCallbacks) {
                            toDo.run(data);
                        }
                    }
                    Log.debug("Process has no more output lines");
                } catch (Exception e){
                    //IOThrower.set(e);

                } finally {
                    readFinishedSemaphore.increase();
                }
            }
        }).start();
        setStatus(ProcessStatus.RUNNING);
        return runningProcess = proc;
    }

    protected List<String> buildFinalArgumentsList(){
        LinkedList<String> list = new LinkedList<>();
        list.add(executable.toString());
        list.addAll(arguments);
        Log.debug("New process: "+list.toString());
        return list;
    }

    public boolean kill(){
        if (!ProcessStatus.RUNNING.equals(this.status) || runningProcess == null || !runningProcess.isAlive()){
            return false;
        }
        runningProcess.destroy();
        return true;
    }

    private String manageLineOutput(String line){
        return line;
    }


    public int runWait(int timeout) throws IOException {

        Process process = null;
        if (status != ProcessStatus.RUNNING || runningProcess == null) {
            process = start();
        } else {
            process = runningProcess;
        }
        IOThrower.check();



        try {
            if(!process.waitFor(timeout, TimeUnit.SECONDS)){
                throw new InterruptedException("Process didn't exited before timeout");
            }
            IOThrower.check();

            setStatus(ProcessStatus.FINISHED);
            this.exitValue = process.exitValue();
        } catch (InterruptedException e) {
            Log.debug("Thread interruped while waiting process");


            this.exitValue = -1;
            setStatus(ProcessStatus.EXCEPTION);
        }
        if(process.isAlive()){
            process.destroyForcibly();
        }
        Log.debug("Processe ended with exit code "+this.exitValue);
        return this.exitValue;
    }

    public int runWait() throws IOException {
        return runWait(this.secondsTimeout);
    }
    private final List<RunnableWithArg> syncCallbacks = Collections.synchronizedList(new ArrayList<>());
    public void addSyncCallback(RunnableWithArg callback){
        this.syncCallbacks.add(callback);

    }
    public void addArgument(String arg){
        this.arguments.addLast(arg);
    }

    public String getOutputString(){
        waitOutputReadFinished();
        return String.join("\n",outputBuffer);
    }
    public List<String> getOutputLines() {
        waitOutputReadFinished();
        return new LinkedList<>(outputBuffer);
    }

    public int getExitValue() {
        return exitValue;
    }
    private boolean waitOutputReadFinished(){
        try {
            readFinishedSemaphore.waitOnce(2);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
