package net.alcaris.plugin.items.models;

import java.util.List;

@SuppressWarnings("unused")
public class ItemsResponse {
    private String status;
    private List<ItemModel> data;

    public String getStatus() {
        return status;
    }

    public List<ItemModel> getData() {
        return data;
    }
}
