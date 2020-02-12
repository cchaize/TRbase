package com.chaize.tr.vue;

import com.chaize.tr.R;
import com.chaize.tr.modele.Shop;

public class ItemShop {
    private Integer image=0;
    private Shop shop;

    final static Integer imgSelection = R.drawable.confirm;

    public ItemShop(Shop shop){
        this.shop = shop;
    }

    public Shop getShop() {
        return shop;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(boolean img) {
        if (img==true)
            image=imgSelection;
        else
            image = 0;
    }

    public String toString(){
        return shop.getNom()+" - "+shop.getVille();
    }
}
