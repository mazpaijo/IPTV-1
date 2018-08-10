package cn.com.pcalpha.iptv.model.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by caiyida on 2018/2/5.
 */

public class Channel implements Serializable {
    private Integer id;
    private String no;
    private String name;//名字 例如:央视一套
    //private String src;//多个源url逗号分隔
    private Integer lastPlay = 0;//上次播放标记 0，1
    private Integer streamId;
    private String categoryName;//1.央视;2.卫视;9.其他

    private ChannelStream lastPlayStream;//记录播放源
    private ChannelCategory category;
    private List<ChannelStream> streams;

    public Channel() {
    }

    public Channel(String no, String name, String categoryName) {
        this.no = no;
        this.name = name;
        this.lastPlay = lastPlay;
        this.categoryName = categoryName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStreamId() {
        return streamId;
    }

    public void setStreamId(Integer streamId) {
        this.streamId = streamId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

//    public void setSrc(String src) {
//        this.src = src;
//    }
//
//    public String getSrc() {
//        return src;
//    }
//
//    public String getSrc(Integer srcIndex) {
//        String[] srcArr = src.split(",");
//        if (srcIndex != null || srcIndex < srcArr.length) {
//            return srcArr[srcIndex];
//        }
//        return srcArr[0];
//    }

    public Integer getLastPlay() {
        return lastPlay;
    }

    public void setLastPlay(Integer lastPlay) {
        this.lastPlay = lastPlay;
    }

    public ChannelCategory getCategory() {
        return category;
    }

    public void setCategory(ChannelCategory category) {
        this.category = category;
    }


    public ChannelStream getLastPlayStream() {
        return lastPlayStream;
    }

    public void setLastPlayStream(ChannelStream lastPlayStream) {
        this.lastPlayStream = lastPlayStream;
    }

    public List<ChannelStream> getStreams() {
        return streams;
    }

    public void setStreams(List<ChannelStream> streams) {
        this.streams = streams;
    }

    public ChannelStream preStream(){
        if(null!=streams&&null!=lastPlayStream){
            int prePosition = streams.indexOf(lastPlayStream)-1;

            if(prePosition>=0&&prePosition<streams.size()){
                lastPlayStream = streams.get(prePosition);
            }else{
                lastPlayStream = streams.get(0);
            }
        }
        return lastPlayStream;
    }

    public ChannelStream nextStream(){
        if(null!=streams&&null!=lastPlayStream){
            int nextPosition = streams.indexOf(lastPlayStream)+1;

            if(nextPosition>=0&&nextPosition<streams.size()){
                lastPlayStream = streams.get(nextPosition);
            }else{
                lastPlayStream = streams.get(streams.size()-1);
            }
        }
        return lastPlayStream;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Channel){
            Channel o = (Channel)obj;
            if(o.getName().equals(this.getName())){
                return true;
            }
        }
        return false;
    }
}
