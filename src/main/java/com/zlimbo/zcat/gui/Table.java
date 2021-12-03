package com.zlimbo.zcat.gui;

import com.zlimbo.zcat.config.ZCatConfig;
import javafx.scene.control.Tab;

public class Table {

    private String tabName;
    private Tab tab;

    private int pageRecordNum;
    private int pageIndex;
    private int sumPageNum;
    private String msg;

    public Table(String tabName, Tab tab) {
        this.tabName = tabName;
        this.tab = tab;
        pageRecordNum = ZCatConfig.DEFAULT_PAGE_NUM;
        pageIndex = 0;
        msg = "";
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public int getPageRecordNum() {
        return pageRecordNum;
    }

    public void setPageRecordNum(int pageRecordNum) {
        this.pageRecordNum = pageRecordNum;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getSumPageNum() {
        return sumPageNum;
    }

    public void setSumPageNum(int sumPageNum) {
        this.sumPageNum = sumPageNum;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
