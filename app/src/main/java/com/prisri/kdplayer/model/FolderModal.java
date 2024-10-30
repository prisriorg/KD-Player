package com.prisri.kdplayer.model;

public class FolderModal {
    private int items;
    private String folder_name;
    private boolean newtab = false;

    public FolderModal(int items, String folder_name, boolean newtab) {
        this.items = items;
        this.folder_name = folder_name;
        this.newtab = newtab;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public boolean isNewtab() {
        return newtab;
    }

    public void setNewtab(boolean newtab) {
        this.newtab = newtab;
    }
}
