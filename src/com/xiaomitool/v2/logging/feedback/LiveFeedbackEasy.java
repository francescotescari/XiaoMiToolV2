package com.xiaomitool.v2.logging.feedback;

import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.WaitSemaphore;
import com.xiaomitool.v2.utility.utils.CompressUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiveFeedbackEasy {
    private static final ConcurrentLinkedQueue<LiveFeedback.Feedback> QUEUED_FEEDBACKS = new ConcurrentLinkedQueue<>();
    private static boolean isOpen = false;
    private static Instant lastFeedbackInstant = null;
    private static WaitSemaphore feedbackSent = new WaitSemaphore(1);

    private LiveFeedbackEasy() {
    }

    public static void sendError(String error, String additionalInfo) {
        send(error, additionalInfo, LiveFeedback.FeedbackType.ERROR);
    }

    public static void sendSuccess(String message, String additionalInfo) {
        send(message, additionalInfo, LiveFeedback.FeedbackType.SUCCESS);
    }

    public static void sendOpen(String message, String additionalInfo) {
        isOpen = true;
        send(message, additionalInfo, LiveFeedback.FeedbackType.OPEN);
    }

    public static void sendClose() {
        sendClose(null, null);
    }

    public static void sendClose(String message, String additionalInfo) {
        send(message, additionalInfo, LiveFeedback.FeedbackType.CLOSE);
        isOpen = false;
    }

    public static void sendLog(String message, String additionalInfo) {
        send(message, additionalInfo, LiveFeedback.FeedbackType.LOG);
    }

    public static void sendInstallException(InstallException e, ProcedureRunner runner) {
        sendError(e.getCode().toString(), e.getMessage() + StrUtils.exceptionToString(e) + "\n\n" + runner.getStackStrace());
    }

    public static void waitFeedbackSent() throws InterruptedException {
        feedbackSent.waitOnce();
    }

    public static void runOnFeedbackSent(Runnable runnable, boolean newThread) {
        Runnable exec = new Runnable() {
            @Override
            public void run() {
                try {
                    waitFeedbackSent();
                } catch (InterruptedException e) {
                    Log.error("Failed to wait feedbackClosure");
                }
                runnable.run();
            }
        };
        if (newThread) {
            new Thread(exec).start();
        } else {
            exec.run();
        }
    }

    private static synchronized void send(String quickMessage, String additionalInfo, LiveFeedback.FeedbackType type) {
        try {
            if (!isOpen) {
                return;
            }
            feedbackSent.setPermits(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LiveFeedback.MultiFeedback multiFeedback;
                        synchronized (QUEUED_FEEDBACKS) {
                            QUEUED_FEEDBACKS.add(newMessage(quickMessage, additionalInfo, type));
                            boolean shouldSendNow = LiveFeedback.FeedbackType.CLOSE.equals(type) || lastFeedbackInstant == null;
                            if (!shouldSendNow) {
                                Duration duration = Duration.between(lastFeedbackInstant, Instant.now());
                                if (duration.getSeconds() > 60) {
                                    shouldSendNow = true;
                                }
                            }
                            if (!shouldSendNow) {
                                return;
                            }
                            multiFeedback = LiveFeedback.MultiFeedback.newBuilder().addAllFeedbacks(QUEUED_FEEDBACKS).setIstanceId(ToolManager.getRunningInstanceId()).build();
                            QUEUED_FEEDBACKS.clear();
                        }
                        final byte[] dataToSend = getsendableMessage(multiFeedback.toByteArray());
                        LogSender.sendSingleLog(dataToSend);
                    } catch (Exception e) {
                        Log.log("LOG", "Failed to send single log: " + e.getMessage(), false);
                    } finally {
                        feedbackSent.increase();
                    }
                }
            }).start();
        } catch (Throwable t) {
            Log.warn("Failed to send live feedback: " + t.getMessage());
        }
    }

    private static LiveFeedback.Feedback newMessage(String message, String additionalInfo, LiveFeedback.FeedbackType type) {
        LiveFeedback.Feedback.Builder liveFeedback = LiveFeedback.Feedback.newBuilder().setTime(System.currentTimeMillis());
        if (message != null) {
            liveFeedback.setQuickMessage(message);
        }
        if (additionalInfo != null) {
            liveFeedback.setLongMessage(additionalInfo);
        }
        liveFeedback.setType(type);
        return liveFeedback.build();
    }

    private static byte[] getsendableMessage(byte[] originalMessage) throws IOException {
        byte[] compressed = CompressUtils.paddedCompress(originalMessage, 4);
        int len = compressed.length - 4;
        ByteBuffer buffer = ByteBuffer.wrap(compressed, 0, 4);
        buffer.putInt(len);
        return compressed;
    }
}
