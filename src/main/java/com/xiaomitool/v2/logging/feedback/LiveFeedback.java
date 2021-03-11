package com.xiaomitool.v2.logging.feedback;

public final class LiveFeedback {
    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_feedback_Feedback_descriptor;
    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_feedback_MultiFeedback_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_feedback_Feedback_fieldAccessorTable;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_feedback_MultiFeedback_fieldAccessorTable;
    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        java.lang.String[] descriptorData = {
                "\n\022LiveFeedback.proto\022\010feedback\"i\n\010Feedba" +
                        "ck\022$\n\004type\030\001 \002(\0162\026.feedback.FeedbackType" +
                        "\022\014\n\004time\030\002 \002(\003\022\024\n\014quickMessage\030\003 \001(\t\022\023\n\013" +
                        "longMessage\030\004 \001(\t\"I\n\rMultiFeedback\022%\n\tfe" +
                        "edbacks\030\001 \003(\0132\022.feedback.Feedback\022\021\n\tist" +
                        "anceId\030\002 \002(\t*D\n\014FeedbackType\022\t\n\005ERROR\020\000\022" +
                        "\013\n\007SUCCESS\020\001\022\007\n\003LOG\020\002\022\010\n\004OPEN\020\003\022\t\n\005CLOSE" +
                        "\020\004B2\n\"com.xiaomitool.v2.logging.feedback" +
                        "B\014LiveFeedback"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_feedback_Feedback_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_feedback_Feedback_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_feedback_Feedback_descriptor,
                new java.lang.String[]{"Type", "Time", "QuickMessage", "LongMessage",});
        internal_static_feedback_MultiFeedback_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_feedback_MultiFeedback_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_feedback_MultiFeedback_descriptor,
                new java.lang.String[]{"Feedbacks", "IstanceId",});
    }

    private LiveFeedback() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
    }

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    public enum FeedbackType
            implements com.google.protobuf.ProtocolMessageEnum {
        ERROR(0, 0),
        SUCCESS(1, 1),
        LOG(2, 2),
        OPEN(3, 3),
        CLOSE(4, 4),
        ;
        public static final int ERROR_VALUE = 0;
        public static final int SUCCESS_VALUE = 1;
        public static final int LOG_VALUE = 2;
        public static final int OPEN_VALUE = 3;
        public static final int CLOSE_VALUE = 4;
        private static final FeedbackType[] VALUES = values();
        private static com.google.protobuf.Internal.EnumLiteMap<FeedbackType>
                internalValueMap =
                new com.google.protobuf.Internal.EnumLiteMap<FeedbackType>() {
                    public FeedbackType findValueByNumber(int number) {
                        return FeedbackType.valueOf(number);
                    }
                };
        private final int index;
        private final int value;

        FeedbackType(int index, int value) {
            this.index = index;
            this.value = value;
        }

        public static FeedbackType valueOf(int value) {
            switch (value) {
                case 0:
                    return ERROR;
                case 1:
                    return SUCCESS;
                case 2:
                    return LOG;
                case 3:
                    return OPEN;
                case 4:
                    return CLOSE;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<FeedbackType>
        internalGetValueMap() {
            return internalValueMap;
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
            return com.xiaomitool.v2.logging.feedback.LiveFeedback.getDescriptor().getEnumTypes().get(0);
        }

        public static FeedbackType valueOf(
                com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException(
                        "EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        public final int getNumber() {
            return value;
        }

        public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
            return getDescriptor();
        }
    }

    public interface FeedbackOrBuilder extends
            com.google.protobuf.MessageOrBuilder {
        boolean hasType();

        com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType getType();

        boolean hasTime();

        long getTime();

        boolean hasQuickMessage();

        java.lang.String getQuickMessage();

        com.google.protobuf.ByteString
        getQuickMessageBytes();

        boolean hasLongMessage();

        java.lang.String getLongMessage();

        com.google.protobuf.ByteString
        getLongMessageBytes();
    }

    public interface MultiFeedbackOrBuilder extends
            com.google.protobuf.MessageOrBuilder {
        java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback>
        getFeedbacksList();

        com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback getFeedbacks(int index);

        int getFeedbacksCount();

        java.util.List<? extends com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder>
        getFeedbacksOrBuilderList();

        com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder getFeedbacksOrBuilder(
                int index);

        boolean hasIstanceId();

        java.lang.String getIstanceId();

        com.google.protobuf.ByteString
        getIstanceIdBytes();
    }

    public static final class Feedback extends
            com.google.protobuf.GeneratedMessage implements
            FeedbackOrBuilder {
        public static final int TYPE_FIELD_NUMBER = 1;
        public static final int TIME_FIELD_NUMBER = 2;
        public static final int QUICKMESSAGE_FIELD_NUMBER = 3;
        public static final int LONGMESSAGE_FIELD_NUMBER = 4;
        private static final Feedback defaultInstance;
        private static final long serialVersionUID = 0L;
        public static com.google.protobuf.Parser<Feedback> PARSER =
                new com.google.protobuf.AbstractParser<Feedback>() {
                    public Feedback parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new Feedback(input, extensionRegistry);
                    }
                };

        static {
            defaultInstance = new Feedback(true);
            defaultInstance.initFields();
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;
        private int bitField0_;
        private com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType type_;
        private long time_;
        private java.lang.Object quickMessage_;
        private java.lang.Object longMessage_;
        private byte memoizedIsInitialized = -1;
        private int memoizedSerializedSize = -1;

        private Feedback(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private Feedback(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private Feedback(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 8: {
                            int rawValue = input.readEnum();
                            com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType value = com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType.valueOf(rawValue);
                            if (value == null) {
                                unknownFields.mergeVarintField(1, rawValue);
                            } else {
                                bitField0_ |= 0x00000001;
                                type_ = value;
                            }
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000002;
                            time_ = input.readInt64();
                            break;
                        }
                        case 26: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000004;
                            quickMessage_ = bs;
                            break;
                        }
                        case 34: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000008;
                            longMessage_ = bs;
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static Feedback getDefaultInstance() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_Feedback_descriptor;
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public static Builder newBuilder(com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Feedback getDefaultInstanceForType() {
            return defaultInstance;
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_Feedback_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.class, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder.class);
        }

        @java.lang.Override
        public com.google.protobuf.Parser<Feedback> getParserForType() {
            return PARSER;
        }

        public boolean hasType() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType getType() {
            return type_;
        }

        public boolean hasTime() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public long getTime() {
            return time_;
        }

        public boolean hasQuickMessage() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public java.lang.String getQuickMessage() {
            java.lang.Object ref = quickMessage_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    quickMessage_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString
        getQuickMessageBytes() {
            java.lang.Object ref = quickMessage_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                quickMessage_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public boolean hasLongMessage() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public java.lang.String getLongMessage() {
            java.lang.Object ref = longMessage_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    longMessage_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString
        getLongMessageBytes() {
            java.lang.Object ref = longMessage_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                longMessage_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            type_ = com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType.ERROR;
            time_ = 0L;
            quickMessage_ = "";
            longMessage_ = "";
        }

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;
            if (!hasType()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTime()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeEnum(1, type_.getNumber());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt64(2, time_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeBytes(3, getQuickMessageBytes());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBytes(4, getLongMessageBytes());
            }
            getUnknownFields().writeTo(output);
        }

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeEnumSize(1, type_.getNumber());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(2, time_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(3, getQuickMessageBytes());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(4, getLongMessageBytes());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        @java.lang.Override
        protected java.lang.Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder {
            private int bitField0_;
            private com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType type_ = com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType.ERROR;
            private long time_;
            private java.lang.Object quickMessage_ = "";
            private java.lang.Object longMessage_ = "";

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_Feedback_descriptor;
            }

            private static Builder create() {
                return new Builder();
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_Feedback_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.class, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder.class);
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            public Builder clear() {
                super.clear();
                type_ = com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType.ERROR;
                bitField0_ = (bitField0_ & ~0x00000001);
                time_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                quickMessage_ = "";
                bitField0_ = (bitField0_ & ~0x00000004);
                longMessage_ = "";
                bitField0_ = (bitField0_ & ~0x00000008);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_Feedback_descriptor;
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback getDefaultInstanceForType() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.getDefaultInstance();
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback build() {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback buildPartial() {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback result = new com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.type_ = type_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.time_ = time_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.quickMessage_ = quickMessage_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.longMessage_ = longMessage_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback) {
                    return mergeFrom((com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback other) {
                if (other == com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.getDefaultInstance()) return this;
                if (other.hasType()) {
                    setType(other.getType());
                }
                if (other.hasTime()) {
                    setTime(other.getTime());
                }
                if (other.hasQuickMessage()) {
                    bitField0_ |= 0x00000004;
                    quickMessage_ = other.quickMessage_;
                    onChanged();
                }
                if (other.hasLongMessage()) {
                    bitField0_ |= 0x00000008;
                    longMessage_ = other.longMessage_;
                    onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasType()) {
                    return false;
                }
                return hasTime();
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            public boolean hasType() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType getType() {
                return type_;
            }

            public Builder setType(com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                type_ = value;
                onChanged();
                return this;
            }

            public Builder clearType() {
                bitField0_ = (bitField0_ & ~0x00000001);
                type_ = com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackType.ERROR;
                onChanged();
                return this;
            }

            public boolean hasTime() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public long getTime() {
                return time_;
            }

            public Builder setTime(long value) {
                bitField0_ |= 0x00000002;
                time_ = value;
                onChanged();
                return this;
            }

            public Builder clearTime() {
                bitField0_ = (bitField0_ & ~0x00000002);
                time_ = 0L;
                onChanged();
                return this;
            }

            public boolean hasQuickMessage() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public java.lang.String getQuickMessage() {
                java.lang.Object ref = quickMessage_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        quickMessage_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public Builder setQuickMessage(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                quickMessage_ = value;
                onChanged();
                return this;
            }

            public com.google.protobuf.ByteString
            getQuickMessageBytes() {
                java.lang.Object ref = quickMessage_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    quickMessage_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setQuickMessageBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                quickMessage_ = value;
                onChanged();
                return this;
            }

            public Builder clearQuickMessage() {
                bitField0_ = (bitField0_ & ~0x00000004);
                quickMessage_ = getDefaultInstance().getQuickMessage();
                onChanged();
                return this;
            }

            public boolean hasLongMessage() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public java.lang.String getLongMessage() {
                java.lang.Object ref = longMessage_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        longMessage_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public Builder setLongMessage(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                longMessage_ = value;
                onChanged();
                return this;
            }

            public com.google.protobuf.ByteString
            getLongMessageBytes() {
                java.lang.Object ref = longMessage_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    longMessage_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setLongMessageBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                longMessage_ = value;
                onChanged();
                return this;
            }

            public Builder clearLongMessage() {
                bitField0_ = (bitField0_ & ~0x00000008);
                longMessage_ = getDefaultInstance().getLongMessage();
                onChanged();
                return this;
            }
        }
    }

    public static final class MultiFeedback extends
            com.google.protobuf.GeneratedMessage implements
            MultiFeedbackOrBuilder {
        public static final int FEEDBACKS_FIELD_NUMBER = 1;
        public static final int ISTANCEID_FIELD_NUMBER = 2;
        private static final MultiFeedback defaultInstance;
        private static final long serialVersionUID = 0L;
        public static com.google.protobuf.Parser<MultiFeedback> PARSER =
                new com.google.protobuf.AbstractParser<MultiFeedback>() {
                    public MultiFeedback parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new MultiFeedback(input, extensionRegistry);
                    }
                };

        static {
            defaultInstance = new MultiFeedback(true);
            defaultInstance.initFields();
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;
        private int bitField0_;
        private java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback> feedbacks_;
        private java.lang.Object istanceId_;
        private byte memoizedIsInitialized = -1;
        private int memoizedSerializedSize = -1;

        private MultiFeedback(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private MultiFeedback(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private MultiFeedback(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                                feedbacks_ = new java.util.ArrayList<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback>();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            feedbacks_.add(input.readMessage(com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.PARSER, extensionRegistry));
                            break;
                        }
                        case 18: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000001;
                            istanceId_ = bs;
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                    feedbacks_ = java.util.Collections.unmodifiableList(feedbacks_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static MultiFeedback getDefaultInstance() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_MultiFeedback_descriptor;
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public static Builder newBuilder(com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public MultiFeedback getDefaultInstanceForType() {
            return defaultInstance;
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_MultiFeedback_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.class, com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.Builder.class);
        }

        @java.lang.Override
        public com.google.protobuf.Parser<MultiFeedback> getParserForType() {
            return PARSER;
        }

        public java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback> getFeedbacksList() {
            return feedbacks_;
        }

        public java.util.List<? extends com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder>
        getFeedbacksOrBuilderList() {
            return feedbacks_;
        }

        public int getFeedbacksCount() {
            return feedbacks_.size();
        }

        public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback getFeedbacks(int index) {
            return feedbacks_.get(index);
        }

        public com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder getFeedbacksOrBuilder(
                int index) {
            return feedbacks_.get(index);
        }

        public boolean hasIstanceId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getIstanceId() {
            java.lang.Object ref = istanceId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    istanceId_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString
        getIstanceIdBytes() {
            java.lang.Object ref = istanceId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                istanceId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            feedbacks_ = java.util.Collections.emptyList();
            istanceId_ = "";
        }

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;
            if (!hasIstanceId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            for (int i = 0; i < getFeedbacksCount(); i++) {
                if (!getFeedbacks(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            for (int i = 0; i < feedbacks_.size(); i++) {
                output.writeMessage(1, feedbacks_.get(i));
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(2, getIstanceIdBytes());
            }
            getUnknownFields().writeTo(output);
        }

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;
            size = 0;
            for (int i = 0; i < feedbacks_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(1, feedbacks_.get(i));
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(2, getIstanceIdBytes());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        @java.lang.Override
        protected java.lang.Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedbackOrBuilder {
            private int bitField0_;
            private java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback> feedbacks_ =
                    java.util.Collections.emptyList();
            private com.google.protobuf.RepeatedFieldBuilder<
                    com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder, com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder> feedbacksBuilder_;
            private java.lang.Object istanceId_ = "";

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_MultiFeedback_descriptor;
            }

            private static Builder create() {
                return new Builder();
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_MultiFeedback_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.class, com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.Builder.class);
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getFeedbacksFieldBuilder();
                }
            }

            public Builder clear() {
                super.clear();
                if (feedbacksBuilder_ == null) {
                    feedbacks_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                } else {
                    feedbacksBuilder_.clear();
                }
                istanceId_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.internal_static_feedback_MultiFeedback_descriptor;
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback getDefaultInstanceForType() {
                return com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.getDefaultInstance();
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback build() {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback buildPartial() {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback result = new com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (feedbacksBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) == 0x00000001)) {
                        feedbacks_ = java.util.Collections.unmodifiableList(feedbacks_);
                        bitField0_ = (bitField0_ & ~0x00000001);
                    }
                    result.feedbacks_ = feedbacks_;
                } else {
                    result.feedbacks_ = feedbacksBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.istanceId_ = istanceId_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback) {
                    return mergeFrom((com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback other) {
                if (other == com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback.getDefaultInstance())
                    return this;
                if (feedbacksBuilder_ == null) {
                    if (!other.feedbacks_.isEmpty()) {
                        if (feedbacks_.isEmpty()) {
                            feedbacks_ = other.feedbacks_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                        } else {
                            ensureFeedbacksIsMutable();
                            feedbacks_.addAll(other.feedbacks_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.feedbacks_.isEmpty()) {
                        if (feedbacksBuilder_.isEmpty()) {
                            feedbacksBuilder_.dispose();
                            feedbacksBuilder_ = null;
                            feedbacks_ = other.feedbacks_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                            feedbacksBuilder_ =
                                    com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                                            getFeedbacksFieldBuilder() : null;
                        } else {
                            feedbacksBuilder_.addAllMessages(other.feedbacks_);
                        }
                    }
                }
                if (other.hasIstanceId()) {
                    bitField0_ |= 0x00000002;
                    istanceId_ = other.istanceId_;
                    onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasIstanceId()) {
                    return false;
                }
                for (int i = 0; i < getFeedbacksCount(); i++) {
                    if (!getFeedbacks(i).isInitialized()) {
                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (com.xiaomitool.v2.logging.feedback.LiveFeedback.MultiFeedback) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private void ensureFeedbacksIsMutable() {
                if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                    feedbacks_ = new java.util.ArrayList<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback>(feedbacks_);
                    bitField0_ |= 0x00000001;
                }
            }

            public java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback> getFeedbacksList() {
                if (feedbacksBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(feedbacks_);
                } else {
                    return feedbacksBuilder_.getMessageList();
                }
            }

            public int getFeedbacksCount() {
                if (feedbacksBuilder_ == null) {
                    return feedbacks_.size();
                } else {
                    return feedbacksBuilder_.getCount();
                }
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback getFeedbacks(int index) {
                if (feedbacksBuilder_ == null) {
                    return feedbacks_.get(index);
                } else {
                    return feedbacksBuilder_.getMessage(index);
                }
            }

            public Builder setFeedbacks(
                    int index, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback value) {
                if (feedbacksBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureFeedbacksIsMutable();
                    feedbacks_.set(index, value);
                    onChanged();
                } else {
                    feedbacksBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setFeedbacks(
                    int index, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder builderForValue) {
                if (feedbacksBuilder_ == null) {
                    ensureFeedbacksIsMutable();
                    feedbacks_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    feedbacksBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addFeedbacks(com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback value) {
                if (feedbacksBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureFeedbacksIsMutable();
                    feedbacks_.add(value);
                    onChanged();
                } else {
                    feedbacksBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addFeedbacks(
                    int index, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback value) {
                if (feedbacksBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureFeedbacksIsMutable();
                    feedbacks_.add(index, value);
                    onChanged();
                } else {
                    feedbacksBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addFeedbacks(
                    com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder builderForValue) {
                if (feedbacksBuilder_ == null) {
                    ensureFeedbacksIsMutable();
                    feedbacks_.add(builderForValue.build());
                    onChanged();
                } else {
                    feedbacksBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addFeedbacks(
                    int index, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder builderForValue) {
                if (feedbacksBuilder_ == null) {
                    ensureFeedbacksIsMutable();
                    feedbacks_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    feedbacksBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllFeedbacks(
                    java.lang.Iterable<? extends com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback> values) {
                if (feedbacksBuilder_ == null) {
                    ensureFeedbacksIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, feedbacks_);
                    onChanged();
                } else {
                    feedbacksBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearFeedbacks() {
                if (feedbacksBuilder_ == null) {
                    feedbacks_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                    onChanged();
                } else {
                    feedbacksBuilder_.clear();
                }
                return this;
            }

            public Builder removeFeedbacks(int index) {
                if (feedbacksBuilder_ == null) {
                    ensureFeedbacksIsMutable();
                    feedbacks_.remove(index);
                    onChanged();
                } else {
                    feedbacksBuilder_.remove(index);
                }
                return this;
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder getFeedbacksBuilder(
                    int index) {
                return getFeedbacksFieldBuilder().getBuilder(index);
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder getFeedbacksOrBuilder(
                    int index) {
                if (feedbacksBuilder_ == null) {
                    return feedbacks_.get(index);
                } else {
                    return feedbacksBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder>
            getFeedbacksOrBuilderList() {
                if (feedbacksBuilder_ != null) {
                    return feedbacksBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(feedbacks_);
                }
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder addFeedbacksBuilder() {
                return getFeedbacksFieldBuilder().addBuilder(
                        com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.getDefaultInstance());
            }

            public com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder addFeedbacksBuilder(
                    int index) {
                return getFeedbacksFieldBuilder().addBuilder(
                        index, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.getDefaultInstance());
            }

            public java.util.List<com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder>
            getFeedbacksBuilderList() {
                return getFeedbacksFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<
                    com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder, com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder>
            getFeedbacksFieldBuilder() {
                if (feedbacksBuilder_ == null) {
                    feedbacksBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                            com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback, com.xiaomitool.v2.logging.feedback.LiveFeedback.Feedback.Builder, com.xiaomitool.v2.logging.feedback.LiveFeedback.FeedbackOrBuilder>(
                            feedbacks_,
                            ((bitField0_ & 0x00000001) == 0x00000001),
                            getParentForChildren(),
                            isClean());
                    feedbacks_ = null;
                }
                return feedbacksBuilder_;
            }

            public boolean hasIstanceId() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public java.lang.String getIstanceId() {
                java.lang.Object ref = istanceId_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        istanceId_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public Builder setIstanceId(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                istanceId_ = value;
                onChanged();
                return this;
            }

            public com.google.protobuf.ByteString
            getIstanceIdBytes() {
                java.lang.Object ref = istanceId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    istanceId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setIstanceIdBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                istanceId_ = value;
                onChanged();
                return this;
            }

            public Builder clearIstanceId() {
                bitField0_ = (bitField0_ & ~0x00000002);
                istanceId_ = getDefaultInstance().getIstanceId();
                onChanged();
                return this;
            }
        }
    }
}
