package com.laowang.utils;

import com.laowang.model.DownloadBlock;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author wangyonghao
 */
@Slf4j
public class FileUtils {
    /**
     * 创建文件
     * @param filePath
     * @return
     * @Exception SecurityException
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        //文件不存在，检查文件夹是否存在，不存在递归创建。在创建文件，返回结果
        if (!file.isFile()) {
            if (file.getParent() != null) {
                new File(file.getParent()).mkdirs();
            }
            try {
                return file.createNewFile() && file.exists();
            } catch (IOException e) {
                if (file.exists()) {
                    return true;
                }
                log.error("===>文件创建异常");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean rename(String filePath){
        File temp = new File(filePath);
        File goal = new File(filePath.replace(".temp",""));
        if (!goal.exists()){
            temp.renameTo(goal);
        }else{
            filePath = filePath.replaceAll(".temp","");
            String dir = filePath.substring(0,filePath.lastIndexOf("\\")+1);
            String name = filePath.substring(filePath.lastIndexOf("\\")+1);
            for (int i = 1; i < 20; i++) {
                filePath = dir + "("+i+")" + name;
                System.out.println(filePath);
                if (!new File(filePath).exists()){
                    break;
                }
            }
            temp.renameTo(new File(filePath));
        }
        return true;
    }

    /**
     * 保存字节数组到随机读写文件
     * @param block
     */
    public static boolean storeToFile(DownloadBlock block) {
        try {
            RandomAccessFile file = new RandomAccessFile(new File(block.getStoreFilePath()), "rwd");
            file.seek(block.getStart());
            file.write(block.getContent(), 0, (block.getEnd() - block.getStart()));
            file.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 对文件进行gzip压缩
     * @param source
     * @param target
     * @throws IOException
     */
    public static void gzipFile(String source, String target) throws IOException {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        GZIPOutputStream gzout = null;
        try {
            fin = new FileInputStream(source);
            fout = new FileOutputStream(target);
            gzout = new GZIPOutputStream(fout);
            byte[] buf = new byte[1024];
            int num;
            while ((num = fin.read(buf)) != -1) {
                gzout.write(buf, 0, num);
            }
            File s = new File(source);
            s.delete();
        } finally {
            if (gzout != null) {
                gzout.close();
            }
            if (fout != null) {
                fout.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }


    /**
     * 对gzip文件进行解压
     * @param source
     * @throws IOException
     */
    public static String unGzipFile(String source) throws IOException {
        String target = null;
        target = source.substring(0,source.lastIndexOf('.'));
        FileInputStream fin = null;
        GZIPInputStream gzin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(source);
            gzin = new GZIPInputStream(fin);
            fout = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int num;
            while ((num = gzin.read(buf, 0, buf.length)) != -1) {
                fout.write(buf, 0, num);
            }
            File s = new File(source);
            s.delete();
            return target;
        } finally {
            if (fout != null) {
                fout.close();
            }
            if (gzin != null) {
                gzin.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }
}
