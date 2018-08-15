package cn.com.pcalpha.iptv.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.pcalpha.iptv.model.bo.Param4Channel;
import cn.com.pcalpha.iptv.model.domain.Channel;
import cn.com.pcalpha.iptv.model.domain.ChannelCategory;

/**
 * Created by caiyida on 2018/6/24.
 */

public class ChannelCategoryDao extends BaseDao {
    public static final String TABLE_NAME = "CHANNEL_CATEGORY";

    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_LAST_PLAY = "LAST_PLAY";

    public static final String ALL_COLUMNS[] = new String[]{
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_LAST_PLAY
    };

    public static final String SQL_CREATE =
            " CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " VARCHAR, " +
                    COLUMN_LAST_PLAY + " INTEGER " +
                    ") ";
    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static ChannelCategoryDao singleton;
    public static ChannelCategoryDao getInstance(Context context) {
        if(null==singleton){
            synchronized (ChannelCategoryDao.class){
                if(null==singleton){
                    singleton = new ChannelCategoryDao(context);
                }
            }
        }
        return singleton;
    }

    private ChannelCategoryDao(Context context) {
        super(context);
    }

    public void insertAsync(final ChannelCategory channelCategory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                insert(channelCategory);
            }
        }).start();
    }

    public void insert(ChannelCategory channelCategory) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, channelCategory.getId());
        cv.put(COLUMN_NAME, channelCategory.getName());
        cv.put(COLUMN_LAST_PLAY, channelCategory.getLastPlay());
        writeDb.insert(TABLE_NAME, null, cv);
    }

    public void delete(Integer channelNo) {
        String[] whereArgs = new String[]{channelNo.toString()};
        writeDb.delete(TABLE_NAME, "no=?", whereArgs);
    }

    public void clear() {
        writeDb.delete(TABLE_NAME, null, null);
    }

    public void update(ChannelCategory channelCategory) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, channelCategory.getId());
        cv.put(COLUMN_NAME, channelCategory.getName());
        cv.put(COLUMN_LAST_PLAY, channelCategory.getLastPlay());
        String[] whereArgs = new String[]{channelCategory.getId().toString()};
        writeDb.update(TABLE_NAME, cv, "no=?", whereArgs);
    }

    public ChannelCategory get(Integer id) {
        String selection = COLUMN_ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor = readDb.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null,
                COLUMN_ID + " ASC", null);
        if (0 == cursor.getCount()) {
            return null;
        }

        List<ChannelCategory> resultList = buildResult(cursor);
        if(null==resultList||resultList.size()<1){
            return null;
        }
        return resultList.get(0);
    }

    //private List<ChannelCategory> channelCategoryList;//缓存列表
    public List<ChannelCategory>  findAll() {
        //init();
//        if (null != channelCategoryList) {
//            return channelCategoryList;
//        }

        Cursor cursor = readDb.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null,
                COLUMN_ID + " ASC", null);

        List<ChannelCategory> result = buildResult(cursor);
//        channelCategoryList = result;
        return result;
    }

    private List<ChannelCategory> buildResult(Cursor cursor) {
        if (0 == cursor.getCount()) {
            return new ArrayList<>();
        }

        Integer mIndex_id = cursor.getColumnIndex(COLUMN_ID);
        Integer mIndex_name = cursor.getColumnIndex(COLUMN_NAME);
        Integer mIndex_last_play = cursor.getColumnIndex(COLUMN_LAST_PLAY);

        List<ChannelCategory> channelCategoryList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ChannelCategory channelCategory = new ChannelCategory();

            channelCategory.setId(cursor.getInt(mIndex_id));
            channelCategory.setName(cursor.getString(mIndex_name));
            channelCategory.setLastPlay(cursor.getInt(mIndex_last_play));
            channelCategoryList.add(channelCategory);
        }

        return channelCategoryList;
    }

    public void setLastPlay(final String categoryName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_LAST_PLAY + " = 0 ";
                readDb.execSQL(sql);
                String sql2 = "UPDATE " + TABLE_NAME + " SET " + COLUMN_LAST_PLAY + " = 1 WHERE " + COLUMN_NAME + " = " + "'"+categoryName+"'";
                readDb.execSQL(sql2);
            }
        }).start();
    }

    public ChannelCategory getLastPlay() {
        String selection = COLUMN_LAST_PLAY + " = 1 ";
        Cursor cursor = readDb.query(TABLE_NAME, ALL_COLUMNS, selection, null, null, null,
                COLUMN_ID + " ASC", null);
        if (0 == cursor.getCount()) {
            return null;
        }

        List<ChannelCategory> resultList = buildResult(cursor);
        if(null==resultList||resultList.size()<1){
            return null;
        }
        return resultList.get(0);
    }

    public void init() {
        clear();
        this.insert(new ChannelCategory(1, "央视频道"));
        this.insert(new ChannelCategory(2, "卫视频道"));
    }
}
