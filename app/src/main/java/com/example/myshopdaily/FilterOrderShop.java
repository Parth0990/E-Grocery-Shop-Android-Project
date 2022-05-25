package com.example.myshopdaily;

import android.widget.Filter;

import com.example.myshopdaily.adapters.AdapterOrderShop;
import com.example.myshopdaily.adapters.AdapterProductSeller;
import com.example.myshopdaily.models.ModelOrderShop;
import com.example.myshopdaily.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;

    private ArrayList<ModelOrderShop>filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // validate data for search query
        if (constraint != null && constraint.length()>0){
            //search filed not empty,not searching retrun oirginal/complete list

            //change to upper case,to make case
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check search by title
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){

                    //add filter data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count=filteredModels.size();
            results.values =filteredModels;
        }
        else {
            results.count=filterList.size();
            results.values =filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence s, FilterResults results) {
        adapter.orderShopArrayList =(ArrayList<ModelOrderShop>) results.values;

//ref adapter
        adapter.notifyDataSetChanged();

    }
}
