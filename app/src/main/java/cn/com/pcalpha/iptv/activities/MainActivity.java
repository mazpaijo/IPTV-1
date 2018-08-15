/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.com.pcalpha.iptv.activities;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import cn.com.pcalpha.iptv.R;
import cn.com.pcalpha.iptv.constants.FragmentTag;
import cn.com.pcalpha.iptv.model.bo.Param4Channel;
import cn.com.pcalpha.iptv.model.bo.Param4ChannelStream;
import cn.com.pcalpha.iptv.model.domain.Channel;
import cn.com.pcalpha.iptv.model.domain.ChannelStream;
import cn.com.pcalpha.iptv.service.ChannelCategoryService;
import cn.com.pcalpha.iptv.service.ChannelService;
import cn.com.pcalpha.iptv.service.ChannelStreamService;
import cn.com.pcalpha.iptv.fragment.MenuFragment;
import tv.danmaku.ijk.media.example.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends AppCompatActivity {

    private IjkVideoView videoView;
    private ChannelReceiver channelReceiver;

    private ChannelService channelService;
    private ChannelStreamService channelStreamService;
    private ChannelCategoryService channelCategoryService;

    private SharedPreferences sharedPreferences;
    private Channel currentChannel;
    private List<Channel> channelList;

    private static final String receiverAction = "cn.com.pcalpha.iptv.broadcasereceiver.action.channelbroadcase";

    static {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    private void initReceiver() {
        channelReceiver = new ChannelReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(receiverAction);
        registerReceiver(channelReceiver, intentFilter);
    }

    private void initViews(Bundle savedInstanceState) {
        videoView = findViewById(R.id.video_view);
        //AndroidMediaController controller = new AndroidMediaController(this, false);
        videoView.setMediaController(null);
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showMainMenuFragment();
                return false;
            }
        });
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenuFragment();
            }
        });
    }

    private void initService() {
        channelService = ChannelService.getInstance(this);
        channelStreamService = ChannelStreamService.getInstance(this);
        channelCategoryService = ChannelCategoryService.getInstance(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initData() {
        Param4Channel param = Param4Channel.build();
        channelList = channelService.find(param);
        currentChannel = channelService.getLastPlay();
        if (null == currentChannel) {
            if (null != channelList && channelList.size() > 0) {
                currentChannel = channelList.get(0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(savedInstanceState);
        initReceiver();
        initService();
        initData();

    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.release(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        play(currentChannel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(channelReceiver);
    }

    //
//    @Override
//    protected void onNewIntent(Intent intent){
//        super.onNewIntent(intent);
//        currentChannel = (Channel) intent.getSerializableExtra("CHANNEL");
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }
        if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
            Channel channel = preChannel();
            play(channel);
        } else if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
            Channel channel = nextChannel();
            play(channel);
        } else if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
            ChannelStream stream = currentChannel.nextStream();
            play(stream);
        } else if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
            ChannelStream stream = currentChannel.preStream();
            play(stream);
        } else if (KeyEvent.KEYCODE_MENU == keyCode || KeyEvent.KEYCODE_ENTER == keyCode) {
            showMainMenuFragment();
        } else {
            getFragmentManager().popBackStackImmediate();
        }
        return false;
    }

    public Channel preChannel() {
        if (null != currentChannel && null != channelList) {
            int prePosition = channelList.indexOf(currentChannel) - 1;

            if (prePosition >= 0 && prePosition < channelList.size()) {
                currentChannel = channelList.get(prePosition);
            } else {
                currentChannel = channelList.get(0);
            }
        }
        return currentChannel;
    }

    public Channel nextChannel() {
        if (null != currentChannel && null != channelList) {
            int nextPosition = channelList.indexOf(currentChannel) + 1;

            if (nextPosition >= 0 && nextPosition < channelList.size()) {
                currentChannel = channelList.get(nextPosition);
            } else {
                currentChannel = channelList.get(channelList.size() - 1);
            }
        }
        return currentChannel;
    }

    private void play(Channel channel) {
        if (null != channel) {
            currentChannel = channel;
            loadStream(currentChannel);//加载源
            play(currentChannel.getLastPlayStream());

            channelService.setLastPlay(channel);
        } else {
            Toast.makeText(this, "未找到合适的节目", Toast.LENGTH_LONG).show();
        }
    }

    private void play(ChannelStream stream) {
        if (null != stream) {
            videoView.release(true);
            videoView.setVideoURI(Uri.parse(stream.getUrl()));
            videoView.start();

            channelService.setLastPlayStream(stream.getChannelName(), stream.getId());
        } else {
            Toast.makeText(this, "未找到合适的节目源", Toast.LENGTH_LONG).show();
        }
    }

    private void loadStream(Channel channel) {
        if (null == channel.getStreams()) {
            Param4ChannelStream param4ChannelStream = Param4ChannelStream.build()
                    .setChannelName(channel.getName())
                    .setCarrier(sharedPreferences.getString("pref_key_carrier", "CMCC"));
            List<ChannelStream> streamList = channelStreamService.find(param4ChannelStream);
            ChannelStream lastPlayStream = channelStreamService.get(channel.getsId());
            channel.setStreams(streamList);
            if (null == lastPlayStream) {
                lastPlayStream = streamList.get(0);
            }
            channel.setLastPlayStream(lastPlayStream);
        }
    }

    private MenuFragment showMainMenuFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragement_main_menu_container);
        MenuFragment mainFragment = new MenuFragment();
        if (null == fragment) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragement_main_menu_container, new MenuFragment(), FragmentTag.MAIN_MENU_FRAGMENT)
                    .addToBackStack(FragmentTag.MAIN_MENU_FRAGMENT)
                    .commit();
        }
        return mainFragment;
    }

    class ChannelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            Channel channel = (Channel) bundle.get("channel");
            play(channel);
        }
    }
}
