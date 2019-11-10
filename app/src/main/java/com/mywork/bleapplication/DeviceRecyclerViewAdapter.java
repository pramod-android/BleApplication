/*
 * MIT License
 * <p>
 * Copyright (c) 2017 Donato Rimenti
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.mywork.bleapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class DeviceRecyclerViewAdapter
        extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {


    private List<BluetoothDevice> devices;
    Context mContext;
    ItemClickListener mClickListener;

    public DeviceRecyclerViewAdapter(Context context) {
        this.mContext = context;
      //  this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_device_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)  {
        holder.mItem = devices.get(position);
        holder.mImageView.setImageResource(getDeviceIcon(devices.get(position)));
        holder.mDeviceNameView.setText(devices.get(position).getName());
        holder.mDeviceAddressView.setText(devices.get(position).getAddress());
    }

    /**
     * Returns the icon shown on the left of the device inside the list.
     *
     * @param device the device for the icon to get.
     * @return a resource drawable id for the device icon.
     */
    private int getDeviceIcon(BluetoothDevice device) {

            return R.drawable.ic_bluetooth_black_24dp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        if(devices!=null){
            return devices.size();
        }else {
            return 0;
        }

    }

    public void cleanView() {
        devices.clear();
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        final View mView;
        final ImageView mImageView;
        final TextView mDeviceNameView;
        final TextView mDeviceAddressView;
        Button buttonConnect,buttonStart,buttonStop;


        BluetoothDevice mItem;
        ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.device_icon);
            mDeviceNameView = (TextView) view.findViewById(R.id.device_name);
            mDeviceAddressView = (TextView) view.findViewById(R.id.device_address);
            buttonConnect=(Button)view.findViewById(R.id.buttonConnect);
            buttonStart=(Button)view.findViewById(R.id.buttonStart);
            buttonStop=(Button)view.findViewById(R.id.buttonStop);

            buttonConnect.setOnClickListener(this);
            buttonStart.setOnClickListener(this);
            buttonStop.setOnClickListener(this);
            view.setOnClickListener(this);
        }



        public BluetoothDevice getmItem(int position){
            BluetoothDevice bluetoothDevice=devices.get(position);
            return bluetoothDevice;
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                if (view.getId()==buttonStart.getId()){
                    mClickListener.onStartButtonClick(view,getmItem(getAdapterPosition()));
                }else if(view.getId()==buttonStop.getId()){
                    mClickListener.onStopButtonClick(view,getmItem(getAdapterPosition()));
                }else if(view.getId()==buttonConnect.getId()){
                    mClickListener.onConnectButtonClick(view,getmItem(getAdapterPosition()));
                }else {
                    mClickListener.onItemClick(view, getAdapterPosition());
                }
            }
        }
    }
    public void SetDeviceList(List<BluetoothDevice> bluetoothDevices){
        devices=bluetoothDevices;
        notifyDataSetChanged();
    }
    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onConnectButtonClick(View view, BluetoothDevice position);
        void onStartButtonClick(View view, BluetoothDevice position);
        void onStopButtonClick(View view, BluetoothDevice position);
    }
}
