/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package zuo.biao.library.ui;

import java.util.ArrayList;
import java.util.List;

import zuo.biao.library.base.BaseActivity;
import zuo.biao.library.base.BaseViewBottomWindow;
import zuo.biao.library.bean.Entry;
import zuo.biao.library.bean.GridPickerConfigBean;
import zuo.biao.library.manager.CityDB;
import zuo.biao.library.ui.GridPickerView.OnTabClickListener;
import zuo.biao.library.util.PlaceUtil;
import zuo.biao.library.util.StringUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

/**地址选择弹窗
 * @author Lemon
 * @use toActivity(PlacePickerWindow.createIntent(...));
 *      *然后在onActivityResult方法内获取data.getStringExtra(PlacePickerWindow.RESULT_PLACE);
 */
public class PlacePickerWindow extends BaseViewBottomWindow<List<Entry<Boolean, String>>, GridPickerView> implements OnClickListener {
	private static final String TAG = "PlacePickerWindow";

	//启动方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	public static final String INTENT_MIN_LEVEL = "INTENT_MIN_LEVEL";//最小深度。 省/... - minLevel = 0; 市/... - minLevel = 1;
	public static final String INTENT_MAX_LEVEL = "INTENT_MAX_LEVEL";//最大深度。 ...市/ - maxLevel = 1;  .../乡(街) - maxLevel = 3;

	public static final String RESULT_PLACE_LIST = "RESULT_PLACE_LIST";
	
	/**启动这个Activity的Intent
	 * @param context
	 * @param limitLevel
	 * @return
	 */
	public static Intent createIntent(Context context, String packageName, int maxLevel) {
		return createIntent(context, packageName, 0, maxLevel);
	}
	/**启动这个Activity的Intent
	 * @param context
	 * @param minLevel
	 * @param maxLevel
	 * @return
	 */
	public static Intent createIntent(Context context, String packageName, int minLevel, int maxLevel) {
		return new Intent(context, PlacePickerWindow.class).
				putExtra(INTENT_PACKAGE_NAME, packageName).
				putExtra(INTENT_MIN_LEVEL, minLevel).
				putExtra(INTENT_MAX_LEVEL, maxLevel);
	}

	//启动方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Override
	@NonNull
	public BaseActivity getActivity() {
		return this;
	}

	public static final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cityDB = CityDB.getInstance(context, StringUtil.getTrimedString(getIntent().getStringExtra(INTENT_PACKAGE_NAME)));

		//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initListener();
		//功能归类分区方法，必须调用>>>>>>>>>>

	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	@Override
	public void initView() {//必须调用
		super.initView();

	}


	private List<Entry<Boolean, String>> list;
	private void setPickerView(final int tabPosition, final int itemPositon) {
		runThread(TAG + "setPickerView", new Runnable() {
			@Override
			public void run() {

				list = getList(tabPosition, containerView.getSelectedItemList());
				runUiThread(new Runnable() {
					@Override
					public void run() {
						containerView.setView(tabPosition, list, itemPositon);
					}
				});
			}
		});		
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private int minLevel;
	private int maxLevel;

	private CityDB cityDB;
	@Override
	public void initData() {//必须调用
		super.initData();

		minLevel = getIntent().getIntExtra(INTENT_MIN_LEVEL, 0);
		maxLevel = getIntent().getIntExtra(INTENT_MAX_LEVEL, 2);
		if (maxLevel < 0 || minLevel > maxLevel) {
			Log.e(TAG, "initData maxLevel < 0 || minLevel > maxLevel >> finish(); return; ");
			finish();
			return;
		}
		if (minLevel < 0) {
			minLevel = 0;
		}

		runThread(TAG + "initData", new Runnable() {

			@Override
			public void run() {
				final ArrayList<GridPickerConfigBean> configList = new ArrayList<GridPickerConfigBean>();
				configList.add(new GridPickerConfigBean("", "浙江", 10));
				configList.add(new GridPickerConfigBean("", "杭州", 0));

				final ArrayList<String> selectedItemNameList = new ArrayList<String>();
				for (GridPickerConfigBean gpcb : configList) {
					selectedItemNameList.add(gpcb.getSelectedItemName());
				}

				list = getList(selectedItemNameList.size() - 1, selectedItemNameList);
				runUiThread(new Runnable() {

					@Override
					public void run() {
						containerView.init(configList, list);
					}
				});
			}
		});

	}


	private synchronized List<Entry<Boolean, String>> getList(int tabPosition, ArrayList<String> selectedItemList) {
		int level = minLevel + tabPosition;
		if (selectedItemList == null || selectedItemList.size() <= 0 || PlaceUtil.isContainLevel(level) == false) {
			return null;
		}

		list = new ArrayList<Entry<Boolean, String>>();
		List<String> cityNameList = null;
		switch (level) {
		case PlaceUtil.LEVEL_PROVINCE:
			cityNameList = cityDB.getAllProvince();
			break;
		case PlaceUtil.LEVEL_CITY:
			cityNameList = cityDB.getProvinceAllCity(StringUtil.getTrimedString(selectedItemList.get(0)));
			break;
		case PlaceUtil.LEVEL_DISTRICT:
			break;
		case PlaceUtil.LEVEL_TOWN:
			break;
		case PlaceUtil.LEVEL_ROAD:
			break;
		default:
			break;
		}

		if (cityNameList != null) {
			for (String name : cityNameList) {
				list.add(new Entry<Boolean, String>(true, name));
			}
		}
		return list;
	}


	
	@Override
	public String getTitleName() {
		return "选择日期";
	}
	@Override
	public String getReturnName() {
		return "";
	}
	@Override
	public String getForwardName() {
		return "";
	}

	@Override
	@NonNull
	protected GridPickerView createView() {
		return new GridPickerView(context, getResources());
	}

	/**
	 * @warn 和android系统SDK内一样，month从0开始
	 */
	@Override
	protected void setResult() {
		setResult(RESULT_OK, new Intent().putStringArrayListExtra(RESULT_PLACE_LIST, containerView.getSelectedItemList()));
	}


	//data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//listener事件监听区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initListener() {//必须调用
		super.initListener();

		containerView.setOnTabClickListener(onTabClickListener);
		containerView.setOnItemSelectedListener(onItemSelectedListener);
	}


	private OnTabClickListener onTabClickListener = new OnTabClickListener() {

		@Override
		public void onTabClick(int tabPosition, TextView tvTab) {
			setPickerView(tabPosition, containerView.getSelectedItemPosition(tabPosition));
		}
	};

	private OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
			containerView.doOnItemSelected(containerView.getCurrentTabPosition()
					, position, containerView.getCurrentSelectedItemName());
			setPickerView(containerView.getCurrentTabPosition() + 1, 0);
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};


	//系统自带监听方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//listener事件监听区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}