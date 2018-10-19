package com.example.shihhaochiu.testble;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class BleAdapter extends BaseAdapter {
    private Context context;
    private List<ScanResult> results;
    public void setResults(List<ScanResult> results){
            this.results = results;
    }
    public BleAdapter(Context context, List<ScanResult> results){
        this.context = context;
       this.results = results;
    }
    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public ScanResult getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view =LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,parent,false);
       TextView text =  view.findViewById(android.R.id.text1);
        ScanResult result = getItem(position);
        text.setText(result.getDevice().getName());
        return view;
    }
}
