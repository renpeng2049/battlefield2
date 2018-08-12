package com.soyoung.battle.field.store;

public class Cursor {

    private Table table;
    private Integer pageNo;
    private Integer cellNo;


    public Cursor(){

    }

    public Cursor(Table table,Integer pageNo,Integer cellNo){
        this.table = table;
        this.pageNo = pageNo;
        this.cellNo = cellNo;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getCellNo() {
        return cellNo;
    }

    public void setCellNo(Integer cellNo) {
        this.cellNo = cellNo;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "table=" + table +
                ", pageNo=" + pageNo +
                ", cellNo=" + cellNo +
                '}';
    }
}
