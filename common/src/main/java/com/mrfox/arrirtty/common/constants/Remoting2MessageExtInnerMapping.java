package com.mrfox.arrirtty.common.constants;

import java.util.HashMap;
import java.util.Map;

/***
 * RemotingModel与MessageExtInner字段映射
 * */
public class Remoting2MessageExtInnerMapping {

    public enum Remoting2MessageExtInnerMappingEnum{
        TAGS("tags","t","tag"),
        TOPIC("topic","to","topic-主题"),
        QUEUE_ID("queueId","qid","队列id"),
        RECONSUME_TIMES("reconsumeTimes","rts","重试次数");

        private String m1;
        private String m2;
        private String desc;

        public String m2(){
            return this.m2;
        }

        public String m1(){
            return this.m1;
        }

        Remoting2MessageExtInnerMappingEnum(String m1, String m2, String desc) {
            this.m1 = m1;
            this.m2 = m2;
            this.desc = desc;
        }
    }

    public static final Map<String,String> MAPPING_MAP = new HashMap<>();

    static {
        MAPPING_MAP.put(Remoting2MessageExtInnerMappingEnum.TAGS.m1,Remoting2MessageExtInnerMappingEnum.TAGS.m2);
        MAPPING_MAP.put(Remoting2MessageExtInnerMappingEnum.TOPIC.m1,Remoting2MessageExtInnerMappingEnum.TOPIC.m2);
        MAPPING_MAP.put(Remoting2MessageExtInnerMappingEnum.QUEUE_ID.m1,Remoting2MessageExtInnerMappingEnum.QUEUE_ID.m2);
        MAPPING_MAP.put(Remoting2MessageExtInnerMappingEnum.RECONSUME_TIMES.m1,Remoting2MessageExtInnerMappingEnum.RECONSUME_TIMES.m2);

    }
}
