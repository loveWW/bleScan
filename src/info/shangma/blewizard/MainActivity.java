package info.shangma.blewizard;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.Theme;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import info.shangma.blewizard.Utils;

public class MainActivity extends Activity implements
		BluetoothAdapter.LeScanCallback {
	private static final String TAG = "BluetoothWizard";


	private BluetoothAdapter mBluetoothAdapter;


	private ListView deviceList;
	private ArrayAdapter<String> adapter;
	private Button scanButton, stopScanButton, connectButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		BluetoothManager blueManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = blueManager.getAdapter();

		
		deviceList = (ListView) findViewById(R.id.list_devices);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Utils.deviceName);
		

		deviceList.setAdapter(adapter);
		deviceList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String key = Utils.deviceAddress.get(position);
				Intent intent = new Intent(MainActivity.this, BLE112Activity.class);
				intent.putExtra(BLE112Activity.SELECTED_SENSOR, key);

				Log.i(TAG, "the sensor is: " + key);
				startActivity(intent);

                return;
			}
		});
		scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Utils.mDevices.clear();
				startScan();
				return;
			}
		});
		
		stopScanButton = (Button) findViewById(R.id.button_stopScan);
		stopScanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopScan();
				return;
			}
		});
		
		connectButton = (Button) findViewById(R.id.button_connect);
		connectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Utils.mDevices.clear();
				updateListAdpater();
				
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			// Bluetooth is disabled
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			finish();
			return;
		}

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	private void updateListAdpater() {
		Set<String> keySet = Utils.mDevices.keySet();
		Iterator it = keySet.iterator();
		Utils.deviceName.clear();
		Utils.deviceAddress.clear();
		while (it.hasNext()) {
			String key = (String) it.next();
			BluetoothDevice device = Utils.mDevices.get(key);
			Utils.deviceName.add(device.getName());	
			Utils.deviceAddress.add(key);
		}
		
		
		Log.i(TAG, "the size of name list is: " + Utils.deviceName.size());
		Log.i(TAG, "the size of address list is: " + Utils.deviceAddress.size());
		
		adapter.notifyDataSetChanged();
		deviceList.invalidateViews();
		deviceList.setAdapter(adapter);

	}


	private Runnable mStopRunnable = new Runnable() {
		@Override
		public void run() {
			stopScan();
		}
	};
	private Runnable mStartRunnable = new Runnable() {
		@Override
		public void run() {
			startScan();
		}
	};

	private void stopScan() {
		mBluetoothAdapter.stopLeScan(this);
	}

	private void startScan() {
		mBluetoothAdapter.startLeScan(this);
	}

	/* BluetoothAdapter.LeScanCallback */

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		// TODO Auto-generated method stub
		/*
		 * We are looking for SensorTag devices only, so validate the name that
		 * each device reports before adding it to our collection
		 */
		Log.i(TAG, "the device is: " + device.getName());
		if (Utils.DEVICE_NAME.equals(device.getName())) {
			if (!Utils.mDevices.containsKey(device.getAddress())) {
				
				Utils.mDevices.put(device.getAddress(), device);
				Log.i(TAG, "the size of devices list is: " + Utils.mDevices.size());
				Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateListAdpater();
					}
				});
			}

		}
	}


}
