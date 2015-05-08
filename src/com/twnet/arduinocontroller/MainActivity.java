package com.twnet.arduinocontroller;

import com.twnet.arduinocontroller.BluetoothActivity;
import com.twnet.arduinocontroller.bluetoothservice.BluetoothService.Constants;
import com.twnet.arduinocontroller.bluetoothservice.BluetoothService;
import com.twnet.arduinocontroller.btDialogFragment;
import com.twnet.arduinocontroller.R;
import com.twnet.arduinocontroller.btDialogFragment.callbackDialog;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	class ButtonHandler implements OnClickListener {

		//TODO: 可能 需要修改為按下後 而不是按一下
		//TODO: 還沒註冊到按鈕
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.button_forward :
				//TODO: 按下前進的動作
				break;
			case R.id.button_back :
				//TODO: 按下後退的動作
				break;
			case R.id.button_left :
				//TODO: 按下左的動作
				break;
			case R.id.button_right :
				//TODO: 按下右的動作
				break;
			}
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupBluetooth();
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle action Items which is pressed
		switch (item.getItemId()) {
		case R.id.action_bt_activity:
			startActivityForResult(new Intent(this, BluetoothActivity.class),
					BluetoothActivity.RETURN_MAC_ADDRESS);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	protected static final int BT_REQUEST_ENABLE = 0;
	private static BluetoothAdapter mAdapter;
	private static String mConnectedDeviceName;
	private BluetoothService mBTService;
	private Activity mainActivity = this;

	@SuppressLint("ShowToast")
	private void connectDevice(Intent data) {

		// Get the device MAC address
		String address = data.getExtras().getString(
				BluetoothActivity.EXTRA_DEVICE_ADDRESS);

		// 顯示選取狀態氣泡
		Toast.makeText(this,
				"您選取了" + mAdapter.getRemoteDevice(address).getName(),
				Toast.LENGTH_SHORT).show();

		// Get the BluetoothDevice object
		BluetoothDevice device = mAdapter.getRemoteDevice(address);

		// Attempt to connect to the device
		mBTService.connect(device);
		
	}
	

	@SuppressLint("ShowToast")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BT_REQUEST_ENABLE:
			switch (resultCode) {
			case RESULT_CANCELED:
				Toast.makeText(this, "需要開啟藍芽才能遙控", Toast.LENGTH_LONG).show();
				Log.i(this.getClass().getSimpleName(), "RESULT_CANCELED ");
			case RESULT_OK:
				setupBluetooth();
			}
			break;
		case BluetoothActivity.RETURN_MAC_ADDRESS:
			switch (resultCode) {
			case Activity.RESULT_OK:
				connectDevice(data);
				break;

			default:
				break;
			}
		}
	}
	
	void setupBluetooth() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mAdapter == null) {
			btDialogFragment.newInstance(this,
					btDialogFragment.DEVICES_NOT_ENABLE, 6).show();
			Toast.makeText(this, "此裝置不支援藍芽", Toast.LENGTH_LONG).show();
			Log.e(this.getClass().getSimpleName(), "Blueteeth Not Support");
			return;
		}
		if (!mAdapter.isEnabled()) {
			btDialogFragment dialog;
			(dialog = btDialogFragment.newInstance(this,
					btDialogFragment.DEVICES_NOT_ENABLE, 6)).show();
			dialog.setCallback(new callbackDialog() {

				@Override
				public void callback() {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, BT_REQUEST_ENABLE);
				}

			});
		} else if (mAdapter.isEnabled()) {
			mBTService = new BluetoothService(
					new Handler(new UIMessageHander()));
			mBTService.start();

			startBluetoothActivity();
		}

	}
	
	
	class UIMessageHander implements Handler.Callback {


		@Override
		public boolean handleMessage(Message msg) {

			Bundle data;
			data = msg.getData();


			Activity activity = mainActivity;
			switch (msg.what) {
			case Constants.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to)
							+ mConnectedDeviceName);
					break;
				case BluetoothService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
					mConnectedDeviceName = null;
				case BluetoothService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case Constants.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				
				//TODO: 這邊擷取了送出的藍芽資料  做UI 更新用
				
				Log.i("WRITE MESSAGE", "successful!");
				Log.i("WRITE MESSAGE", writeMessage);

				break;
			case Constants.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				
				//TODO: 這邊擷取了收到的藍芽資料
				
				Log.i("READ MESSAGE", "successful!");
				Log.i("READ MESSAGE", readMessage);
				break;
			case Constants.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						Constants.DEVICE_NAME);
				if (null != activity) {
					Toast.makeText(activity,
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case Constants.MESSAGE_TOAST:
				if (null != activity) {
					Toast.makeText(activity,
							msg.getData().getString(Constants.TOAST),
							Toast.LENGTH_SHORT).show();
				}
				break;
			}

			return false;
		}
	}
	
	/**
	 * 開啟藍芽裝置選單
	 */

	private void startBluetoothActivity() {
		startActivityForResult(new Intent(this, BluetoothActivity.class),
				BluetoothActivity.RETURN_MAC_ADDRESS);
	}
	
	private void setStatus(int status) {

		final ActionBar actionBar = getActionBar();
		if (null == actionBar) {
			return;
		}
		actionBar.setSubtitle(status);

		// if (status == 0) {
		// actionBar.setSubtitle(R.string.bt_unpaired);
		// }
		// if (status == 1) {
		// actionBar.setSubtitle(getResources().getString(R.string.bt_paired)
		// + " : " + BluetoothService.getPairedID());
		// }

	}
	private void setStatus(String strStatus) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(strStatus);
	}
	
	public static String getDeviceName() {
		return mConnectedDeviceName;
	}
	
	
}
