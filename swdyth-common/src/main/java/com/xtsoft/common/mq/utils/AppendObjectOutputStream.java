package com.xtsoft.common.mq.utils;

import java.io.*;

/**
 * @author: cuizhaojin
 * @date: 2024/8/16 14:27
 * @description:
 */
public class AppendObjectOutputStream extends ObjectOutputStream {

    //定义成静态的好处
    private static File f;

    /**
     * 初始化静态文件对象，并返回类对象
     * @param file 文件对象，用于初始化静态文件对象
     * @param out 输出流
     * @return MyObjectOutputStream
     * @throws IOException
     */
    public static  AppendObjectOutputStream newInstance(File file, OutputStream out)
            throws IOException {
        f = file;//本方法最重要的地方：构建文件对象，是两个文件对象属于同一个
        return new AppendObjectOutputStream(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        if (!f.exists() || (f.exists() && f.length() == 0)) {
            super.writeStreamHeader();
        } else {
            super.reset();
        }

    }

    public AppendObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }


}

