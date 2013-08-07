package cn.navior.example.htcbluetooth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 65535;
	
	private int searchIndex = 0;
	private ArrayAdapter< String > devicesArrayAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	
	private TextView title;
	
	/* temporary data storage */
	private HashMap< String, HashMap< Integer, Item > > record;
	
	// Create a BroadcastReceiver for ACTION_XX
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            short rssi = intent.getShortExtra( BluetoothDevice.EXTRA_RSSI, (short)0 );
	            
	            // Add the name and address to an array adapter to show in a ListView
	            devicesArrayAdapter.add(device.getName() + "  " + device.getAddress() + "  " + rssi );
	            devicesArrayAdapter.notifyDataSetChanged();
	            
	            // add the record into the hashmap
	            if( !record.containsKey( device.getAddress() ) ){
	            	Item item = new Item( device.getAddress() );
	            	item.setRssi( rssi + 0 );
	            	item.setSearchIndex( searchIndex );
	            	item.setName( device.getName() );
	            	HashMap< Integer, Item > map = new HashMap< Integer, Item >();
	            	map.put( searchIndex, item );
	            	record.put( device.getAddress(), map );
	            }
	            else{
	            	HashMap< Integer, Item > map = record.get( device.getAddress() );
	            	if( !map.containsKey( searchIndex ) ){
	            		Item item = new Item( device.getAddress() );
		            	item.setRssi( rssi + 0 );
		            	item.setSearchIndex( searchIndex );
		            	item.setName( device.getName() );
		            	map.put( searchIndex, item );
		            	record.put( device.getAddress(), map );
	            	}
	            }
	        }
	        
	        // When the discovering process ends
	        if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ){
	        	title.setText( "Searching has finished." );
	        	
	        	
	        }
	        
	        // When the discovering process starts
	        if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) ){
	        	searchIndex++;
	        	title.setText( "Searching has started." );
	        }
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// fields initialized
		title = ( TextView )findViewById( R.id.main_title );
		record = new HashMap< String, HashMap< Integer, Item > >();
		devicesArrayAdapter = new ArrayAdapter< String >( MainActivity.this, android.R.layout.simple_expandable_list_item_1 );
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(mReceiver, filter);
		
		// get the local Bluetooth device model
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// request for Bluetooth if it's not on
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		/* set the button listeners */
		// set listener for starting search
		Button startSearch = ( Button )findViewById( R.id.main_start_search );
		startSearch.setOnClickListener( new OnClickListener(){
			public void onClick( View v ){
				devicesArrayAdapter.clear();
				devicesArrayAdapter.notifyDataSetChanged();
				if( !mBluetoothAdapter.startDiscovery() ){
					Toast.makeText( MainActivity.this, "failed to start", Toast.LENGTH_SHORT ).show();
				}
			}
		});
		
		// set listener for stopping search
		Button stopSearch = ( Button )findViewById( R.id.main_stop_search );
		stopSearch.setOnClickListener( new OnClickListener(){
			public void onClick( View v ){
				if( !mBluetoothAdapter.cancelDiscovery() ){
					Toast.makeText( MainActivity.this, "failed to stop", Toast.LENGTH_SHORT ).show();
				}
			}
		});
		
		// set listener for recording
		Button recordButton = ( Button )findViewById( R.id.main_record );
		recordButton.setOnClickListener( new OnClickListener(){
			public void onClick( View v ){
				// start a new thread to save the records
	        	Thread t = new Thread(){
	        		@Override
	        		public void run() {
	        			// create the directory
	        			File directory = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "bluetooth_rssi" );
	                    if( !directory.exists() ) {
	                    	directory.mkdir();
	                    }
	                    
	                    // create the time string
	                    SimpleDateFormat tempDate = new SimpleDateFormat( "yyyy-MM-dd-kk-mm-ss", Locale.ENGLISH );
	            		String datetime = tempDate.format(new java.util.Date());
	                    
	            		// create the file name
	                    File recordFile = new File( directory.getAbsolutePath() + "/" + datetime + ".txt" );
	                    if( recordFile.exists() ) {
	                    	recordFile.delete();
	                    }
	                    
	                    // write the records into file
	                    try {
							PrintWriter writer = new PrintWriter( recordFile );
							ArrayList< HashMap< Integer, Item > > maps = new ArrayList< HashMap< Integer, Item > >( record.values() );
							for( int i = 0; i < maps.size(); i++ ){
								HashMap< Integer, Item > map = maps.get( i );
								ArrayList< Item > temp = new ArrayList< Item >( map.values() );
								// sort the item list
								ArrayList< Item > items = new ArrayList< Item >();
								int maxIndex = -1;	// the index of item with the maximum search index
								int maxValue = -1;	// the maximum search index
								for( int k = 0; k < temp.size(); k++ ){
									for( int j = 0; j < temp.size(); j++ ){	// select the maximum one
										if( temp.get( j ) != null && temp.get( j ).getSearchIndex() > maxValue ){
											maxIndex = j;
											maxValue = temp.get( j ).getSearchIndex();
										}
									}
									items.add( temp.get( maxIndex ) );
									temp.remove( maxIndex );
									maxIndex = -1;
									maxValue = -1;
								}
								for( int j = items.size() - 1; j >= 0; j-- ){
									Item item = items.get( j );
									writer.write( item.getSearchIndex() + "," + item.getName() + "," + item.getMac() + "," + item.getRssi() + "\n" );
								}
							}
							writer.close();	// close the writer
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
	        		}
	        	};
	        	t.run();
	        	
	        	// clear the records
	        	record.clear();
			}
		});
		
		// set list adapter
		ListView lv = ( ListView )findViewById( R.id.main_device_list );
		lv.setAdapter( devicesArrayAdapter );
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		// unregister the receiver
		unregisterReceiver( mReceiver );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
