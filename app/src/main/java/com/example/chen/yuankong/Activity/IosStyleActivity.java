package com.example.chen.yuankong.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.SettingView.onSettingViewItemClickListener;
import com.dtr.settingview.lib.SettingView.onSettingViewItemSwitchListener;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.BasicItemViewH;
import com.dtr.settingview.lib.item.SwitchItemView;
import com.example.chen.yuankong.R;

import java.util.ArrayList;
import java.util.List;

public class IosStyleActivity extends Activity {

    private SettingView mSettingView1 = null;
    private SettingView mSettingView2 = null;

    private SettingData mItemData = null;
    private SettingViewItemData mItemViewData = null;
    private List<SettingViewItemData> mListData = new ArrayList<SettingViewItemData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ios_style);

        mSettingView1 = (SettingView) findViewById(R.id.ios_style_setting_view_01);
        mSettingView2 = (SettingView) findViewById(R.id.ios_style_setting_view_02);

        mSettingView1.setOnSettingViewItemClickListener(new onSettingViewItemClickListener() {

            @Override
            public void onItemClick(int index) {
                // TODO Auto-generated method stub
                Toast.makeText(IosStyleActivity.this, "第" + index + "项被点击", Toast.LENGTH_SHORT).show();
                if (index == 4) {
                    mSettingView1.modifySubTitle("中国联通", index);
                } else if (index == 2) {
                    mSettingView1.modifySubTitle("关闭", index);
                }
            }
        });

        mSettingView1.setOnSettingViewItemSwitchListener(new onSettingViewItemSwitchListener() {

            @Override
            public void onSwitchChanged(int index, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {

                    Toast.makeText(IosStyleActivity.this, "第" + index + "项打开", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(IosStyleActivity.this, "第" + index + "项关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initView();
    }

    private void initView() {
        /* ==========================SettingView1========================== */
        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("开启辅助");
        mItemData.setDrawable(getResources().getDrawable(R.drawable.icon07));
        mItemData.setChecked(false);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(IosStyleActivity.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("绑定邮箱");
        mItemData.setSubTitle("未绑定");
        mItemData.setDrawable(getResources().getDrawable(R.drawable.icon02));

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("设置安全手机号");
        mItemData.setSubTitle("未设置");
        mItemData.setDrawable(getResources().getDrawable(R.drawable.icon02));

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
        mListData.add(mItemViewData);

//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("蜂窝移动网络");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon05));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);
//
//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("运营商");
//		mItemData.setSubTitle("中国移动");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon03));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);
//
        mSettingView1.setAdapter(mListData);
		/* ==========================SettingView1========================== */

		/* ==========================SettingView2========================== */
        mListData.clear();

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("同步通讯录");
        mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        mItemData.setChecked(false);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(IosStyleActivity.this));
        mListData.add(mItemViewData);

        mItemViewData = new SettingViewItemData();
        mItemData = new SettingData();
        mItemData.setTitle("同步短信");
        mItemData.setDrawable(getResources().getDrawable(R.drawable.icon01));
        mItemData.setChecked(false);

        mItemViewData.setData(mItemData);
        mItemViewData.setItemView(new SwitchItemView(IosStyleActivity.this));
        mListData.add(mItemViewData);

//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("通知中心");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon10));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);

//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("控制中心");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon10));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);
//
//		mItemViewData = new SettingViewItemData();
//		mItemData = new SettingData();
//		mItemData.setTitle("勿扰模式");
//		mItemData.setDrawable(getResources().getDrawable(R.drawable.icon09));
//
//		mItemViewData.setData(mItemData);
//		mItemViewData.setItemView(new BasicItemViewH(IosStyleActivity.this));
//		mListData.add(mItemViewData);

        mSettingView2.setAdapter(mListData);
		/* ==========================SettingView2========================== */
    }
}
