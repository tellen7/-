package com.laowang.common;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class FileUtil {
    /**
     * 功能：获取文件指定位置的字节数组
     *
     * @param filePath 文件path
     * @param start    开始位置
     * @param end      结束位置
     * @return 文件存在且不是文件夹：文件指定位置段的字节数组；否则：null
     */
    public static byte[] getFileBytesByPosition(String filePath, int start, int end) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory())
            return null;
        try {
            byte[] result = new byte[end - start];
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(start);
            raf.read(result, 0, end - start);
            raf.close();
            return result;
        } catch (FileNotFoundException e) {
            log.error("===>请求文件{}不存在", filePath);
            e.printStackTrace();
        } catch (IOException e) {
            log.error("===>文件IO异常{} ", filePath);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件的length，这个可以保存到数据库中
     *
     * @param filePath
     * @return 文件长度
     */
    public static Integer getFileLength(String filePath) {
        log.info("获取文件的长度======>{}");
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        //int 32位，最大能表示4GB，所以文件能表示4GB个byte
        return (int) file.length();
    }

    /**
     * 服务端不用
     * 功能：随机写入文件
     */
    public static byte[] storeToFile(String fileName, int start, int end, byte[] bytes) {
        try {
            File tempFile = new File(fileName);
            //文件不存在，检查文件夹是否存在，不存在递归创建,再创建文件
            if (!tempFile.isFile()) {
                if (tempFile.getParent() != null) {
                    new File(tempFile.getParent()).mkdirs();
                }
                tempFile.createNewFile();
            }
            RandomAccessFile file = new RandomAccessFile(new File(fileName), "rwd");
            file.seek(start);
            file.write(bytes, 0, end - start);
            file.close();
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error("===>写入文件异常");
            e.printStackTrace();
        }
        return null;
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
     * @param target
     * @throws IOException
     */
    public static void unGzipFile(String source, String target) throws IOException {
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


    public static void main(String[] args) {
//        byte[] result = getFileBytesByPosition("D:/bakbaseMessage3.zip",0,(int)new File("D:/bakbaseMessage3.zip").length());
//        String s = Base64.getEncoder().encodeToString(result);
//        System.out.println(s);
//        System.out.println(s.length());
//        byte[] a = Base64.getDecoder().decode(s);
//        FileUtil.storeToFile("D:/123.zip",0,a.length,a);

    }
}
