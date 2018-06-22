package com.soyoung.battle.field.btree;

public interface B {

    Object get(Comparable key);

    void remove(Comparable key);

    void insertOrUpdate(Comparable key, Object obj);

}
