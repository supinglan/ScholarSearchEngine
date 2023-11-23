package org.example;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main
{

        static PaperIndexer paperIndexer = new PaperIndexer( "index/index-11-23-1");
        public static void main(String[] args) throws IOException, ParseException {
            Scanner scanner = new Scanner(System.in);

            System.out.println("欢迎使用 ZNLP 学术搜索系统！");
            boolean running = true;

            while (running) {
                System.out.print("\n请选择查询类型: ");
                System.out.print("【1】按内容查询 ");
                System.out.print("【2】按年份查询 ");
                System.out.print("【3】按会议名查询 ");
                System.out.print("【4】按作者查询 ");
                System.out.print("【5A】图片查询 ");
                System.out.println("【6】退出");

                int choice =0;
                try {
                    choice= scanner.nextInt();
                    // 处理读取到的整数
                } catch (InputMismatchException e) {
                    // 清除输入缓冲区，跳过错误的输入，以免陷入无限循环
                    scanner.next();
                }
                scanner.nextLine();


                switch (choice) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        paperIndexer.search(scanner,choice);
                        break;
                    case 6:
                        running = false;
                        break;
                    default:
                        System.out.println("请输入有效选项（1-5）！");
                        break;
                }
            }
            System.out.println("感谢使用 ZNLP 学术搜索系统！");
            scanner.close();
        }



}
