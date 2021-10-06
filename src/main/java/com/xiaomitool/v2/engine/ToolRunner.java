package com.xiaomitool.v2.engine;

import com.xiaomitool.v2.engine.actions.ActionsStatic;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableMessage;
import com.xiaomitool.v2.utility.WaitSemaphore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToolRunner {
  private static int last_msg = 0;
  private static ExecutorService runner = Executors.newSingleThreadExecutor();
  private static WaitSemaphore semaphore = new WaitSemaphore();

  public static void run(RunnableMessage runnableMessage) {
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            semaphore.setPermits(0);
            try {
              last_msg = runnableMessage.run();
            } catch (InterruptedException e) {
              Log.error("Executor runner thread interrutped :(");
              return;
            }
            semaphore.increase();
          }
        };
    runner.submit(runnable);
  }

  public static int runWait(RunnableMessage runnableMessage) {
    run(runnableMessage);
    return waitMessage();
  }

  public static int waitMessage() {
    try {
      semaphore.waitOnce();
    } catch (InterruptedException e) {
      Log.error("Main tool runner thread interrutped :(");
    }
    return last_msg;
  }

  public static void start() {
    runner.submit(
        () -> {
          try {
            ActionsStatic.MAIN().run();
          } catch (InterruptedException e) {
            Log.warn("Main tool runner thread interrutped: " + e.getMessage());
          }
        });
  }
}
