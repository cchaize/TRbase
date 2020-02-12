package com.chaize.tr.vue;

//package com.listradiobutton_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaize.tr.R;
import com.chaize.tr.modele.Shop;

import java.util.ArrayList;

public class GridListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ItemShop> arrayList;
    private int selectedPosition = -1;

    public GridListAdapter(Context context, ArrayList<ItemShop> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int pos, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);

        if (view == null) {
            viewHolder = new ViewHolder();

            //inflate the layout on basis of boolean
            view = inflater.inflate(R.layout.row_shop_layout, viewGroup, false);

            viewHolder.txtShopName = (TextView) view.findViewById(R.id.txtShopName);
            viewHolder.imgSelectShop = (ImageView) view.findViewById(R.id.imgSelectShop);

            view.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) view.getTag();
        Shop shop = arrayList.get(pos).getShop();
        viewHolder.txtShopName.setText(shop.getNom()+" - "+shop.getVille());
        viewHolder.imgSelectShop.setBackgroundResource(arrayList.get(pos).getImage());

        viewHolder.txtShopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemCheckChanged( pos);
            }
        });
        viewHolder.imgSelectShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemCheckChanged(pos);
            }
        });

        return view;
    }

    //On selecting any view set the current position to selectedPositon and notify adapter
    public void itemCheckChanged(final int pos) {
        if (selectedPosition>=0 && selectedPosition<arrayList.size())
            arrayList.get(selectedPosition).setImage(false);
        if (pos>=0 && pos<arrayList.size())
            arrayList.get(pos).setImage(true);
        selectedPosition = pos;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private TextView txtShopName;
        private ImageView imgSelectShop;
    }

    //Return the selectedPosition item
    public ItemShop getSelectedItem() {
        if (selectedPosition != -1) {
            Toast.makeText(context, "Magasin sélectionné : " + arrayList.get(selectedPosition), Toast.LENGTH_SHORT).show();
            return arrayList.get(selectedPosition);
        }
        return null;
    }

    //Delete the selected position from the arrayList
    public void deleteSelectedPosition() {
        if (selectedPosition != -1) {
            arrayList.remove(selectedPosition);
            selectedPosition = -1;//after removing selectedPosition set it back to -1
            notifyDataSetChanged();
        }
    }

}