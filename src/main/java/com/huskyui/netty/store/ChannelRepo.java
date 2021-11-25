package com.huskyui.netty.store;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author 王鹏
 */
public class ChannelRepo {
    private Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    private Set<Channel> channelSet = new CopyOnWriteArraySet<>();


    public ChannelRepo put(Channel channel){
        channelSet.add(channel);
        return this;
    }

    public void remove(Channel channel){
        channelSet.remove(channel);
    }

    public Set<Channel> channels(){
        return channelSet;
    }

    public ChannelRepo put(String key,Channel channel){
        channelMap.put(key,channel);
        return this;
    }

    public Channel get(String name){
        return channelMap.get(name);
    }


    public void remove(String name){
        channelMap.remove(name);
    }

    public int size(){
        return channelMap.size();
    }
}
