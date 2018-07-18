package com.soyoung.battle.field.store;

import com.google.common.collect.Lists;

import java.util.List;

public class SampleSchema {

    private Integer id;

    private String name;

    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public List<Column> getColumnList(){

        List<Column> columnList = Lists.newArrayList();
        Column id = new Column("id",null,PriTypeEnum.INTEGER.getLength());
        Column name = new Column("name",null,32);
        Column email = new Column("email",null,128);

        columnList.add(id);
        columnList.add(name);
        columnList.add(email);

        return columnList;
    }

    public int getRowSize(){

        return getColumnList().stream().mapToInt(Column::getLength).sum();
    }

    @Override
    public String toString() {
        return "SampleSchema{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
