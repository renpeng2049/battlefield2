package com.soyoung.battle.field.store;

import java.nio.ByteBuffer;

public class Page {

    public static final Integer PAGE_SIZE = 4096;

    private Integer pageNo;
    private ByteBuffer pageBuffer;


    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize(){
        return PAGE_SIZE;
    }

    public ByteBuffer getPageBuffer() {
        return pageBuffer;
    }

    public void setPageBuffer(ByteBuffer pageBuffer) {
        this.pageBuffer = pageBuffer;
    }

    public void append(ByteBuffer buffer){

        this.pageBuffer.put(buffer);
    }

    public static Page EMPTY(Integer pageNo){

        Page page = new Page();
        page.setPageNo(pageNo);
        ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);

        page.setPageBuffer(buffer);

        return page;
    }

    public void flush(){


    }

    @Override
    public String toString() {
        return "Page{" +
                "pageNo=" + pageNo +
                ", pageBuffer=" + pageBuffer +
                '}';
    }
}
