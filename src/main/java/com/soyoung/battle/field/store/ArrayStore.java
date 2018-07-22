package com.soyoung.battle.field.store;

import com.soyoung.battle.field.env.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * 追加式存储
 */
public class ArrayStore {

    private static final String ARRAY_DB_NAME = ".adb";
    private Environment environment;
    private FileChannel fileChannel;

    public ArrayStore(Environment env){

        this.environment = env;
    }

    public void load(){
        Path path = environment.dataFile().resolve(ARRAY_DB_NAME);

        File adb = path.toFile();

        if(adb.exists()){

            fileChannel = getFileChannel(adb);
        } else {
            //文件不存在，创建文件
            try {
                adb.createNewFile();
                fileChannel = getFileChannel(adb);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public FileChannel getFileChannel(File file){


        FileChannel fileChannel = null;
        try {
            RandomAccessFile raf = null;
            raf = new RandomAccessFile(file,"rw");
            fileChannel = raf.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileChannel;
    }

    /**
     * 追加存储方法
     * @param byteBuffer
     * @return
     */
    public boolean append(ByteBuffer byteBuffer){

        boolean flag = true;
        try {
            fileChannel.write(byteBuffer);
        } catch (IOException e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;
    }
}
