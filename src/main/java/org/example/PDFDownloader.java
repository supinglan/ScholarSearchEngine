package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PDFDownloader {
    public static void downloadFile(String fileUrl, String savePath) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            // 检查是否能成功建立连接
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 打开连接的输入流
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(savePath);
                // 从输入流中读取数据，并写入文件输出流中
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                // 关闭流
                outputStream.close();
                inputStream.close();
                System.out.println("文件下载成功！");
            } else {
                System.out.println("下载失败，服务器返回错误：" + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
