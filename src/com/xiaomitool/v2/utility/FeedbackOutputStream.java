package com.xiaomitool.v2.utility;

import com.xiaomitool.v2.resources.ResourcesConst;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeedbackOutputStream extends OutputStream {
    public static int FLAG_COMPRESSED = 0x1;
    public static int FLAG_USERMESSAGE = 0x2;
    private final Boolean writeAccess = Boolean.TRUE;
    private final LinkedList<FeedbackChunck> dataChunks = new LinkedList<>();
    private ByteArrayOutputStream currentOutputStream = new ByteArrayOutputStream();
    private FeedbackChunck userMessage;
    private Integer flagOnClose;

    private static boolean hasFlag(int flags, int hasThisFlag) {
        return (flags & hasThisFlag) == hasThisFlag;
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (writeAccess) {
            currentOutputStream.write(b);
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        synchronized (writeAccess) {
            currentOutputStream.write(data);
        }
    }

    @Override
    public void write(byte[] data, int offset, int len) {
        synchronized (writeAccess) {
            currentOutputStream.write(data, offset, len);
        }
    }

    public void setUserMessage(String message) {
        if (message != null) {
            this.userMessage = new FeedbackChunck(message, FLAG_USERMESSAGE);
        } else {
            this.userMessage = null;
        }
    }

    public void flushChuck(int flags) throws IOException {
        synchronized (writeAccess) {
            if (currentOutputStream.size() == 0) {
                return;
            }
            currentOutputStream.close();
            FeedbackChunck chunck = new FeedbackChunck(currentOutputStream, flags);
            if (hasFlag(flags, FLAG_USERMESSAGE)) {
                if (userMessage != null) {
                    throw new IOException("User message already present");
                }
                this.userMessage = chunck;
            } else {
                this.dataChunks.add(chunck);
            }
            currentOutputStream = new ByteArrayOutputStream();
        }
    }

    @Override
    public void close() throws IOException {
        if (currentOutputStream.size() > 0) {
            if (flagOnClose != null) {
                flushChuck(flagOnClose);
                flagOnClose = null;
            } else {
                throw new IOException("Chunk not flushed yet, please flush it first");
            }
        }
    }

    public void close(int flags) throws IOException {
        flushChuck(flags);
        close();
    }

    public InputStream getReadInputStream() {
        List<InputStream> streamList = new ArrayList<>();
        if (userMessage != null) {
            streamList.add(new FeedbackChunckInputStream(userMessage));
        }
        for (FeedbackChunck dataChunk : dataChunks) {
            streamList.add(new FeedbackChunckInputStream(dataChunk));
        }
        return new MultipleInputStream(streamList);
    }

    public void setFlagsOnClose(int flagOnClose) {
        this.flagOnClose = flagOnClose;
    }

    public static class FeedbackChunckInputStream extends InputStream {
        private final MultipleInputStream inputStream;

        private FeedbackChunckInputStream(FeedbackChunck chunck) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putInt(chunck.flags);
            buffer.putInt(chunck.length);
            this.inputStream = new MultipleInputStream(new ByteArrayInputStream(buffer.array()), new ByteArrayInputStream(chunck.data));
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(byte[] data) throws IOException {
            return inputStream.read(data);
        }

        @Override
        public int read(byte[] data, int off, int len) throws IOException {
            return inputStream.read(data, off, len);
        }
    }

    private static class FeedbackChunck {
        private byte[] data;
        private int length;
        private int flags;

        private FeedbackChunck(ByteArrayOutputStream outputStream, int flags) {
            this.data = outputStream.toByteArray();
            this.length = this.data.length;
            this.flags = flags;
        }

        private FeedbackChunck(String data, int flags) {
            this.data = data.getBytes(ResourcesConst.interalCharset());
            this.length = this.data.length;
            this.flags = flags;
        }
    }
}
