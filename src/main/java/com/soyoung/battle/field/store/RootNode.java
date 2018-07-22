package com.soyoung.battle.field.store;

import com.soyoung.battle.field.env.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class RootNode {

    private static final String ROO_NODE_FILE = ".rtd";
    private Environment environment;
    private FileChannel fileChannel;

    public RootNode(Environment env){
        this.environment = env;
    }

    //加载根节点数据
    public void load(){

        Path path = environment.dataFile().resolve(ROO_NODE_FILE);

        File root = path.toFile();

        if(root.exists()){

            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(root,"rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(null != raf){
                fileChannel = raf.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                try {
                    fileChannel.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {

            //root文件不存在
        }




    }

    //同步内容到文件
    public void flush(){

    }
}
