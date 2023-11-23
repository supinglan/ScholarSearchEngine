package org.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import java.util.*;


public class PaperIndexer {
    private final String indexPath;

    public PaperIndexer(String indexPath) {
        this.indexPath = indexPath;
    }
    public void initializeIndex() throws IOException {
        // 索引配置
        Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig();
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // 指定创建模式，覆盖已有索引

        // 初始化 IndexWriter，清空索引数据
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            writer.deleteAll(); // 清空索引数据
        }
    }

    public void indexPaper(Paper paper) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));

        // 创建分析器，这里使用标准分析器
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 配置索引器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

        // 创建Lucene文档并添加字段
        Document doc = new Document();
        addField(doc, "title", paper.getTitle());
        addField(doc, "author", paper.getAuthor());
        addField(doc, "abstractText", paper.getAbstractText());
        addField(doc, "year", Integer.toString(paper.getYear()));
        addField(doc, "conference", paper.getConference());

        // 添加额外的字段
        if (paper.getInfo() != null) {
            for (Map.Entry<String, String> entry : paper.getInfo().entrySet()) {
                addField(doc, entry.getKey(), entry.getValue());
            }
        }

        // 添加路径和文本字段
        addField(doc, "path", paper.getPath());
        addField(doc, "text", paper.getText());

        // 将文档添加到索引中
        indexWriter.addDocument(doc);

        // 提交更改并关闭索引器
        indexWriter.commit();
        indexWriter.close();
    }

    private void addField(Document doc, String fieldName, String value) {
        doc.add(new TextField(fieldName, Objects.requireNonNullElse(value, ""), Field.Store.YES));
    }

    public void search(Scanner scanner, int choice) throws IOException, ParseException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        StandardAnalyzer analyzer = new StandardAnalyzer();

        String[] fields = {"title", "text"}; // 替换为实际的字段名

        Map<String, Float> boosts = new HashMap<>();
        boosts.put("title", 5.0f);
        boosts.put("abstractText", 3.0f);
        boosts.put("text", 1.0f);

        boolean continueSearch;
        while (true) {
            continueSearch = true;
            System.out.println("请输入查询内容:");
            String content = scanner.nextLine();
            TopDocs topDocs = null;
            switch (choice) {
                case 1:
                case 5:
                    // 构建多字段查询对象并设置权重
                    MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
                    // 构建多字段的模糊查询对象
                    parser.setFuzzyMinSim(2); // 设置模糊搜索的最小相似度
                    Query query = parser.parse(content + "~"); // 添加模糊搜索的波浪符号
                    // 执行查询
                    topDocs = searcher.search(query, 10);

                    break;
                case 2:
                    QueryParser parserByYear = new QueryParser("year", analyzer);
                    Query queryByYear = parserByYear.parse(String.valueOf(content));
                    // 执行查询
                    topDocs = searcher.search(queryByYear, Integer.MAX_VALUE);
                    break;
                case 3:
                    QueryParser parserByConference = new QueryParser("conference", analyzer);
                    Query queryByConference = parserByConference.parse(content + "~");
                    // 执行查询
                    topDocs = searcher.search(queryByConference, 10);

                    break;
                case 4:
                    QueryParser parserByAuthor = new QueryParser("author", analyzer);
                    Query queryByAuthor = parserByAuthor.parse(content + "~");
                    // 执行查询
                    topDocs = searcher.search(queryByAuthor, 10);

                    break;

                default:
                    System.out.println("系统错误！");
                    break;
            }

            int i = 1;

            assert topDocs != null;
            if (choice != 5) {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    System.out.println("id: " + i);
                    int columnWidth = 150;
                    printWithLineBreaks("title: " + doc.get("title"), columnWidth);
                    printWithLineBreaks("author: " + doc.get("author"), columnWidth);
                    System.out.println("year: " + doc.get("year"));
                    System.out.println("conference: " + doc.get("conference"));
                    i++;
                }
                if (topDocs.scoreDocs.length == 0) {
                    System.out.println("没有符合要求的论文！");
                }
            } else {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    String url = doc.get("URL");
                    String[] urlParts = url.split("/");
                    String name = urlParts[urlParts.length - 1];
                    String folderPath = "papers/images/" + name;
                    File folder = new File(folderPath);
                    File[] files = folder.listFiles();
                    for (File file : files) {
                        System.out.println(folderPath+"/"+file.getName());
                        i++;
                    }
                }
                if (i == 1) {
                    System.out.println("没有符合要求的图片！");
                }
            }
            if (choice == 5) {
                while (continueSearch) {
                    System.out.print("\n请选择操作: ");
                    System.out.print("1.继续查询 ");
                    System.out.println("2.返回主菜单 ");
                    int option = 0;
                    try {
                        option = scanner.nextInt();
                        // 处理读取到的整数
                    } catch (InputMismatchException e) {
                        // 清除输入缓冲区，跳过错误的输入，以免陷入无限循环
                        scanner.next();
                    }
                    scanner.nextLine(); // 消耗换行符
                    switch (option) {
                        case 1:
                            continueSearch = false;
                            break;
                        case 2:
                            // 返回主菜单
                            reader.close();
                            return;
                        default:
                            System.out.println("请输入有效选项（1-2）！");
                            break;
                    }
                }
            }
            else {
                while (continueSearch) {
                    System.out.print("\n请选择操作: ");
                    System.out.print("1.查看详细内容 ");
                    System.out.print("2.继续查询 ");
                    System.out.println("3.返回主菜单");

                    int option = 0;
                    try {
                        option = scanner.nextInt();
                        // 处理读取到的整数
                    } catch (InputMismatchException e) {
                        // 清除输入缓冲区，跳过错误的输入，以免陷入无限循环
                        scanner.next();
                    }
                    scanner.nextLine(); // 消耗换行符

                    switch (option) {
                        case 1:
                            // 执行查看详细内容的操作
                            System.out.println("请输入要查看的论文id:");
                            int id = 0;
                            try {
                                id = scanner.nextInt();
                                // 处理读取到的整数
                            } catch (InputMismatchException e) {
                                // 清除输入缓冲区，跳过错误的输入，以免陷入无限循环
                                scanner.next();
                            }
                            scanner.nextLine();
                            if (id > topDocs.scoreDocs.length) {
                                System.out.println("请输入有效id！");
                                break;
                            }

                            Document doc = searcher.doc(topDocs.scoreDocs[id - 1].doc);
                            List<IndexableField> docFields = doc.getFields();
                            for (IndexableField field : docFields) {
                                String fieldName = field.name(); // 获取字段名
                                String fieldValue = field.stringValue();
                                if (fieldValue != null && (!fieldName.equals("text")) && (!fieldValue.isEmpty())) {
                                    // 设定列宽
                                    int columnWidth = 150;
                                    printWithLineBreaks(fieldName + ": " + fieldValue, columnWidth);
                                }
                            }
                            break;
                        case 2:
                            continueSearch = false;
                            // 继续查询
                            break;
                        case 3:
                            // 返回主菜单
                            reader.close();
                            return;
                        default:
                            System.out.println("请输入有效选项（1-3）！");
                            break;
                    }
                }
            }
        }
    }
    public static void printWithLineBreaks(String input, int columnWidth) {
        for (int i = 0; i < input.length(); i += columnWidth) {
            // 确保不超出字符串长度
            int endIndex = Math.min(i + columnWidth, input.length());
            System.out.println(input.substring(i, endIndex));
        }
    }
}

