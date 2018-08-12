package com.soyoung.battle.field.store;

import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.env.Environment;
import org.apache.logging.log4j.Logger;

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
public class Store {

    Logger logger = Loggers.getLogger(Store.class);
    private static final String DB_SUFFIX = ".db";
    private String tableName;
    private Environment environment;
    private FileChannel writeFileChannel;
    private FileChannel readFileChannel;

    public Store(String tableName,Environment env){
        this.tableName = tableName;
        this.environment = env;
    }

    public void load(){
        Path path = environment.dataFile().resolve(tableName + DB_SUFFIX);

        File adb = path.toFile();
        logger.info("store location :{}",adb.getPath());
        if(adb.exists()){

            writeFileChannel = getFileChannel(adb);
            readFileChannel = getFileChannel(adb);
        } else {
            //文件不存在，创建文件
            try {
                adb.createNewFile();
                writeFileChannel = getFileChannel(adb);
                readFileChannel = getFileChannel(adb);

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
            byteBuffer.flip();
            writeFileChannel.write(byteBuffer);
            writeFileChannel.force(true);
        } catch (IOException e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * 写入
     * @param byteBuffer
     * @return
     */
    public boolean wirte(ByteBuffer byteBuffer,Long postion){

        boolean flag = true;
        try {
            byteBuffer.flip();
            writeFileChannel.write(byteBuffer,postion);
            writeFileChannel.force(true);
        } catch (IOException e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;
    }


    public boolean read(ByteBuffer byteBuffer){

        boolean flag = false;
        try {
            int i = readFileChannel.read(byteBuffer);
            if(i != -1){
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flag;
    }

    public boolean read(ByteBuffer byteBuffer,Integer position){

        boolean flag = false;
        try {
            int i = readFileChannel.read(byteBuffer,position);
            if(i != -1){
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flag;
    }


    public void stop(){

        try {
            writeFileChannel.close();
            readFileChannel.close();
        } catch (IOException e) {

        } finally {
            if(null != writeFileChannel){
                try {
                    writeFileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != readFileChannel){
                try {
                    readFileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
