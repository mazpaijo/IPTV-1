package cn.com.pcalpha.iptv.channel.stream;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiyida on 2018/8/8.
 */

public class ChannelStream implements Serializable {
    private Integer id;
    private String name;
    private String url;
    private String carrier;//运营商 CMCC,CUCC,CTCC
    private Date playTime;//上次播放标记

    private String channelName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Date getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Date playTime) {
        this.playTime = playTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj instanceof ChannelStream) {
            ChannelStream o = (ChannelStream) obj;
            if (o.getId().equals(this.getId())) {
                return true;
            }
        }
        return false;
    }
}
