package com.soyoung.battle.field.store;

import java.nio.ByteBuffer;

public class Pager {

    private Store store;

    public Pager(Store store){
        this.store = store;
    }

    public void appendPage(Page page){

        store.append(page.getPageBuffer());
    }

    /**
     * 根据页码获取页
     * @param pageNo
     * @return
     */
    public Page getPage(Integer pageNo){

        Integer offset = Page.PAGE_SIZE * pageNo;

        ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE);

        boolean flag = store.read(buffer,offset);

        Page page = new Page();
        page.setPageNo(pageNo);
        if(flag){
            page.setPageBuffer(buffer);
        }else {
            page = null;
        }

        return page;
    }

    public void savePage(Page page){

        long position = Page.PAGE_SIZE * page.getPageNo();
        store.wirte(page.getPageBuffer(),position);
    }
}
