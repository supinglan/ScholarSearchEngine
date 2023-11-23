package org.example;

import com.google.gson.Gson;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Init {
    static String baseURL = "https://aclanthology.org/";
    static ArrayList<Paper> papers = new ArrayList<>();
    static PaperIndexer indexer = new PaperIndexer("index/index-11-23-1");

    public static void main(String[] args) throws IOException {
        indexer.initializeIndex();
        conferSpider("https://aclanthology.org/volumes/2023.acl-long/", "ACL");
        conferSpider("https://aclanthology.org/events/cl-2023/", "CL");
        conferSpider("https://aclanthology.org/volumes/2022.conll-1/", "CoNLL");
        conferSpider("https://aclanthology.org/events/eacl-2014/", "EACL");
        conferSpider("https://aclanthology.org/events/emnlp-2009/", "EMNLP");

        // 文件路径
        String filePath = "papers/papers.json";
        // 创建Gson对象
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            // 将ArrayList转换为JSON字符串并写入文件
            gson.toJson(papers, writer);
            System.out.println("Paper数组已保存到文件 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void conferSpider(String confURL,String conf) throws IOException {
        Document doc = Jsoup.connect(confURL).get();
        Elements pdfs = doc.select("p.d-sm-flex").select("a[href$=.pdf]");
        Elements titles = doc.select("span.d-block").select("strong");
        int num = Math.min(Math.min(pdfs.size(), titles.size()), 40);
        for (int i = 0; i < num; i++) {

            String pdfUrl = pdfs.get(i).attr("href");
            if (!pdfUrl.endsWith(".pdf")) {
                continue;
            }
            String paperUrl = titles.get(i).select("a").attr("href");
            String title = titles.get(i).text();
            Document paperDoc = Jsoup.connect(baseURL + paperUrl).get();
            Elements authors = paperDoc.select("p.lead").select("a");
            StringBuilder authorText = new StringBuilder();
            for (Element author : authors) {
                authorText.append(author.text()).append(", "); // 将每个作者的文本内容添加到 authorText 中，并在末尾加上空格
            }
            Elements keys = paperDoc.select("div.acl-paper-details").select("dt");
            Elements values = paperDoc.select("div.acl-paper-details").select("dd");
            HashMap<String, String> info = new HashMap<>();
            if (keys.size() == values.size()) {
                for (int j = 0; j < keys.size(); j++) {
                    Element keyElement = keys.get(j);
                    Element valueElement = values.get(j);
                    String key = keyElement.text().replaceAll(":$", "");
                    String value = valueElement.text();
                    info.put(key, value);
                }
            } else continue;

            String year = info.get("Year");
            Elements abstarct = paperDoc.select("div.acl-abstract");
            String abstractText = "";
            if (abstarct.size() == 1) {
                abstractText = abstarct.get(0).text();
            }
            String[] urlParts = pdfUrl.split("/");
            String fileName = urlParts[urlParts.length - 1];
            String path = "papers/pdfs/" + fileName;
            System.out.println(pdfUrl);
            System.out.println("Downloading: " + title);
            PDFDownloader.downloadFile(pdfUrl, path);
            File file = new File(path);
            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            int imageNum = 0;
            Path folder = Paths.get("papers/images/" + fileName.replaceAll(".pdf$", ""));
            if (!Files.exists(folder)) {
                try {
                    Files.createDirectories(folder); // 创建文件夹及其父目录（如果不存在）
                } catch (IOException e) {
                    System.out.println("无法创建文件夹：" + e.getMessage());
                }
                for (PDPage page : document.getPages()) {
                    PDResources resources = page.getResources();
                    Iterable<COSName> objectNames = resources.getXObjectNames();

                    if (objectNames != null) {
                        for (COSName cosName : objectNames) {
                            PDXObject xObject = resources.getXObject(cosName);
                            if (xObject instanceof PDImageXObject imageObject) {
                                BufferedImage image = imageObject.getImage();
                                ImageIO.write(image, "PNG", new File(folder + "/" + imageNum + ".png"));
                                imageNum++;
                            }
                        }
                    }

                }
            }
                document.close();
                Paper paper = new Paper(title, authorText.toString(), abstractText, Integer.parseInt(year), conf, info, path, text);
                papers.add(paper);
                indexer.indexPaper(paper);
                System.out.println("Paper indexed successfully!");
            }
            System.out.println(papers.size());
        }

}