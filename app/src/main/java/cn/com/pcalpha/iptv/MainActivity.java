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

package cn.com.pcalpha.iptv;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
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

    private IjkVideoView mVideoView;
    private TextView mInputChannelView;
    private View mEpgView;
    private TextView mEpgChannelNo;
    private TextView mEpgChannelName;


    private ChannelReceiver mChannelReceiver;

    private ChannelService channelService;
    private ChannelStreamService channelStreamService;
    private ChannelCategoryService channelCategoryService;

    private SharedPreferences mSharedPreferences;
    private Channel mCurrentChannel;
    private List<Channel> mChannelList;


    private static final String CHANNEL_CHANGE_ACTION = "cn.com.pcalpha.iptv.action.PLAY_CHANNEL";

    static {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    private void initReceiver() {
        mChannelReceiver = new ChannelReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHANNEL_CHANGE_ACTION);
        registerReceiver(mChannelReceiver, intentFilter);
    }

    private void initViews(Bundle savedInstanceState) {
        mInputChannelView = findViewById(R.id.input_channel_no_view);
        mVideoView = findViewById(R.id.video_view);
        //AndroidMediaController controller = new AndroidMediaController(this, false);
        mVideoView.setMediaController(null);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showMainMenuFragment();
                return false;
            }
        });
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenuFragment();
            }
        });
        mVideoView.requestFocus();

        mEpgView = findViewById(R.id.epg);
        mEpgChannelNo = findViewById(R.id.epg_channel_no);
        mEpgChannelName = findViewById(R.id.epg_channel_name);
    }

    private void initService() {
        channelService = ChannelService.getInstance(this);
        channelStreamService = ChannelStreamService.getInstance(this);
        channelCategoryService = ChannelCategoryService.getInstance(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initData() {
        Param4Channel param = Param4Channel.build();
        mChannelList = channelService.find(param);
        mCurrentChannel = channelService.getLastPlay();
        if (null == mCurrentChannel) {
            if (null != mChannelList && mChannelList.size() > 0) {
                mCurrentChannel = mChannelList.get(0);
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
        mVideoView.release(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        play(mCurrentChannel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mChannelReceiver);
    }

    //
//    @Override
//    protected void onNewIntent(Intent intent){
//        super.onNewIntent(intent);
//        mCurrentChannel = (Channel) intent.getSerializableExtra("CHANNEL");
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mVideoView.hasFocus()) {
            if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
                Channel channel = preChannel();
                play(channel);
                return true;
            } else if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
                Channel channel = nextChannel();
                play(channel);
                return true;
            } else if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
                if (null != mCurrentChannel) {
                    ChannelStream stream = mCurrentChannel.nextStream();
                    play(stream);
                }
                return true;
            } else if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
                if (null != mCurrentChannel) {
                    ChannelStream stream = mCurrentChannel.preStream();
                    play(stream);
                }
                return true;
            }
        }

        if (KeyEvent.KEYCODE_ENTER == keyCode
                || KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
            showMainMenuFragment();
            return true;
        } else if (KeyEvent.KEYCODE_BACK == keyCode) {
            getFragmentManager().popBackStackImmediate();
            return true;
        }

        if (KeyEvent.KEYCODE_NUMPAD_1 == keyCode
                || KeyEvent.KEYCODE_1 == keyCode) {
            showInputChannelView(1);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_2 == keyCode
                || KeyEvent.KEYCODE_2 == keyCode) {
            showInputChannelView(2);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_3 == keyCode
                || KeyEvent.KEYCODE_3 == keyCode) {
            showInputChannelView(3);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_4 == keyCode
                || KeyEvent.KEYCODE_4 == keyCode) {
            showInputChannelView(4);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_5 == keyCode
                || KeyEvent.KEYCODE_5 == keyCode) {
            showInputChannelView(5);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_6 == keyCode
                || KeyEvent.KEYCODE_6 == keyCode) {
            showInputChannelView(6);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_7 == keyCode
                || KeyEvent.KEYCODE_7 == keyCode) {
            showInputChannelView(7);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_8 == keyCode
                || KeyEvent.KEYCODE_8 == keyCode) {
            showInputChannelView(8);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_9 == keyCode
                || KeyEvent.KEYCODE_9 == keyCode) {
            showInputChannelView(9);
            return true;
        } else if (KeyEvent.KEYCODE_NUMPAD_0 == keyCode
                || KeyEvent.KEYCODE_0 == keyCode) {
            showInputChannelView(0);
            return true;
        }

        return false;
    }


    public Channel preChannel() {
        if (null != mCurrentChannel && null != mChannelList) {
            int prePosition = mChannelList.indexOf(mCurrentChannel) - 1;

            if (prePosition >= 0 && prePosition < mChannelList.size()) {
                mCurrentChannel = mChannelList.get(prePosition);
            } else {
                mCurrentChannel = mChannelList.get(0);
            }
        }
        return mCurrentChannel;
    }

    public Channel nextChannel() {
        if (null != mCurrentChannel && null != mChannelList) {
            int nextPosition = mChannelList.indexOf(mCurrentChannel) + 1;

            if (nextPosition >= 0 && nextPosition < mChannelList.size()) {
                mCurrentChannel = mChannelList.get(nextPosition);
            } else {
                mCurrentChannel = mChannelList.get(mChannelList.size() - 1);
            }
        }
        return mCurrentChannel;
    }

    private void play(Channel channel) {
        if (null != channel) {
            mCurrentChannel = channel;
            showEpg(channel);
            loadStream(mCurrentChannel);//加载源
            play(mCurrentChannel.getLastPlayStream());
            channelService.setLastPlay(channel);
        } else {
            Toast.makeText(this, "未找到合适的节目", Toast.LENGTH_LONG).show();
        }
    }

    private void play(ChannelStream stream) {
        if (null != stream) {
            mVideoView.release(true);
            mVideoView.setVideoURI(Uri.parse(stream.getUrl()));
            mVideoView.start();

            channelService.setLastPlayStream(stream.getChannelName(), stream.getId());
        } else {
            Toast.makeText(this, "未找到合适的节目源", Toast.LENGTH_LONG).show();
        }
    }

    private void loadStream(Channel channel) {
        if (null == channel.getStreams()) {
            Param4ChannelStream param4ChannelStream = Param4ChannelStream.build()
                    .setChannelName(channel.getName());
            List<ChannelStream> streamList = channelStreamService.find(param4ChannelStream);
            ChannelStream lastPlayStream = channelStreamService.get(channel.getsId());
            if (null == lastPlayStream) {
                lastPlayStream = streamList.get(0);
            }
            channel.setStreams(streamList);
            channel.setLastPlayStream(lastPlayStream);
        }
    }

    private void showMainMenuFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragement_main_menu_container);
        MenuFragment mainFragment = new MenuFragment();
        if (null == fragment) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragement_main_menu_container, new MenuFragment(), FragmentTag.MAIN_MENU_FRAGMENT)
                    .addToBackStack(FragmentTag.MAIN_MENU_FRAGMENT)
                    .commit();
        }
    }

    class ChannelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            Channel channel = (Channel) bundle.get("channel");
            play(channel);
        }
    }

    private void showEpg(Channel channel) {
        mEpgChannelNo.setText(channel.getNo());
        mEpgChannelName.setText(channel.getName());

        if (View.VISIBLE == mEpgView.getVisibility()) {
            return;
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                hideEpg();
            }
        };
        mEpgView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(run, 3000);
    }

    private void hideEpg() {
        mEpgView.setVisibility(View.GONE);
    }

    final Handler mHandler = new Handler();

    void showInputChannelView(int num) {
        mInputChannelView.setText(mInputChannelView.getText() + String.valueOf(num));
        if (View.VISIBLE == mInputChannelView.getVisibility()) {
            return;
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "频道" + mInputChannelView.getText(), Toast.LENGTH_LONG).show();
                hideInputChannelView();
            }
        };
        mInputChannelView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(run, 2000);
    }

    void hideInputChannelView() {
        mInputChannelView.setVisibility(View.GONE);
        mInputChannelView.setText("");
    }
}