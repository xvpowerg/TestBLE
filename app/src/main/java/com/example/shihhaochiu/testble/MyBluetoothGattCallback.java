package com.example.shihhaochiu.testble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class MyBluetoothGattCallback extends BluetoothGattCallback {
    private BluetoothLeService bluetoothLeService;

    public MyBluetoothGattCallback(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    String TAG = "MyBluetoothGattCallback";

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String intentAction;
        Log.d(TAG, "BluetoothGattCallback----onConnectionStateChange" + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            intentAction = bluetoothLeService.ACTION_GATT_CONNECTED;
            bluetoothLeService.mConnectionState = bluetoothLeService.STATE_CONNECTED;
            bluetoothLeService.broadcastUpdate(intentAction);
            Log.i(TAG, "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            if (bluetoothLeService.mBluetoothGatt.discoverServices()) {
                Log.i(TAG, "Attempting to start service discovery:");

            } else {
                Log.i(TAG, "Attempting to start service discovery:not success");

            }


        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            intentAction = bluetoothLeService.ACTION_GATT_DISCONNECTED;
            bluetoothLeService.mConnectionState = bluetoothLeService.STATE_DISCONNECTED;
            Log.i(TAG, "Disconnected from GATT server.");
            bluetoothLeService.broadcastUpdate(intentAction);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            bluetoothLeService.broadcastUpdate(bluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //this block should be synchronized to prevent the function overloading
        synchronized (this) {
            //CharacteristicWrite success
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite success:" + new String(characteristic.getValue()));
                if (bluetoothLeService.mCharacteristicRingBuffer.isEmpty()) {
                    bluetoothLeService.mIsWritingCharacteristic = false;
                } else {
                    BluetoothLeService.BluetoothGattCharacteristicHelper
                            bluetoothGattCharacteristicHelper = bluetoothLeService.mCharacteristicRingBuffer.next();
                    if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() >
                            bluetoothLeService.MAX_CHARACTERISTIC_LENGTH) {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.
                                    setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0,
                                            bluetoothLeService.
                                                    MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));

                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }


                        if (bluetoothLeService.mBluetoothGatt.
                                writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.
                                mCharacteristicValue.substring(bluetoothLeService.MAX_CHARACTERISTIC_LENGTH);
                    } else {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.
                                    mCharacteristicValue.getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }

                        if (bluetoothLeService.
                                mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = "";

//	            			System.out.print("before pop:");
//	            			Log.d(TAG,mCharacteristicRingBuffer.size());
                        bluetoothLeService.mCharacteristicRingBuffer.pop();
//	            			System.out.print("after pop:");
//	            			Log.d(TAG,mCharacteristicRingBuffer.size());
                    }
                }
            }
            //WRITE a NEW CHARACTERISTIC
            else if (status == bluetoothLeService.WRITE_NEW_CHARACTERISTIC) {
                if ((!bluetoothLeService.mCharacteristicRingBuffer.isEmpty()) &&
                        bluetoothLeService.mIsWritingCharacteristic == false) {
                    BluetoothLeService.BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper =
                            bluetoothLeService.mCharacteristicRingBuffer.next();
                    if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() >
                            bluetoothLeService.MAX_CHARACTERISTIC_LENGTH) {

                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(
                                    bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0,
                                            bluetoothLeService.MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }

                        if (bluetoothLeService.mBluetoothGatt.
                                writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                        } else {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue =
                                bluetoothGattCharacteristicHelper.mCharacteristicValue.
                                        substring(bluetoothLeService.MAX_CHARACTERISTIC_LENGTH);
                    } else {
                        try {
                            bluetoothGattCharacteristicHelper.mCharacteristic.setValue(
                                    bluetoothGattCharacteristicHelper.mCharacteristicValue.
                                            getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            // this should never happen because "US-ASCII" is hard-coded.
                            throw new IllegalStateException(e);
                        }


                        if (bluetoothLeService.mBluetoothGatt.
                                writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[0]);
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[1]);
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[2]);
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[3]);
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[4]);
//	            	        	Log.d(TAG,(byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[5]);

                        } else {
                            Log.d(TAG, "writeCharacteristic init " +
                                    new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                        }
                        bluetoothGattCharacteristicHelper.mCharacteristicValue = "";

//		            			System.out.print("before pop:");
//		            			Log.d(TAG,mCharacteristicRingBuffer.size());
                        bluetoothLeService.mCharacteristicRingBuffer.pop();
//		            			System.out.print("after pop:");
//		            			Log.d(TAG,mCharacteristicRingBuffer.size());
                    }
                }

                bluetoothLeService.mIsWritingCharacteristic = true;

                //clear the buffer to prevent the lock of the mIsWritingCharacteristic
                if (bluetoothLeService.mCharacteristicRingBuffer.isFull()) {
                    bluetoothLeService.mCharacteristicRingBuffer.clear();
                    bluetoothLeService.mIsWritingCharacteristic = false;
                }
            } else
            //CharacteristicWrite fail
            {
                bluetoothLeService.mCharacteristicRingBuffer.clear();
                Log.d(TAG, "onCharacteristicWrite fail:" + new String(characteristic.getValue()));
                Log.d(TAG, "status:" + status + "");
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicRead  " + characteristic.getUuid().toString());
            bluetoothLeService.broadcastUpdate(bluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor characteristic,
                                  int status) {
        Log.d(TAG, "onDescriptorWrite  " + characteristic.getUuid().toString() + " " + status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged  " + new String(characteristic.getValue()));
        bluetoothLeService.broadcastUpdate(
                bluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
    }
}
