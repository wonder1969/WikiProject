
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static boolean flagEnLang = false;

    public static void main(String[] args) throws IOException {
        //Определяем Connection, StringBuilder и Scanner
        HttpURLConnection connection;
        Scanner scaner = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        System.out.print("Добро пожаловать в программу поиска. Выберите язык поиска: 1-Русский 2-English:");
        int language = scaner.nextInt();
        //В зависимости от выбраного языка определяем начало http запроса
        //exsentences=10 - Выберает первые 10 предложений
        //prop=extracts указывает на возвращение текста или ограниченого html
        //explaintext=1 возвращет текст вместо ограниченного html

        switch (language) {
            case 1 -> sb.append("https://ru.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exsentences=10&explaintext=1&formatversion=2&titles=");
            case 2 -> {
                sb.append("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exsentences=10&explaintext=1&formatversion=2&titles=");
                flagEnLang = true;
            }
            default -> {
                System.out.println("Ошибка ввода!");
                return;
            }
        }
        //Получаем текст запроса
        Scanner scanerQuery = new Scanner(System.in);
        System.out.print("Пожалуйста, введите ваш запрос: ");
        String query = scanerQuery.nextLine();

        //Добавляем к http запросу наш текст запроса
        sb.append(URLEncoder.encode(query.replace(" ","_"), "UTF-8"));
        scanerQuery.close();


        //Посмотреть url запроса
        //System.out.println(sb);
        //Формируем Url
        URL url = new URL(sb.toString());

        //Конектимся и отправляем запрос на сервер
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);

        //Получаем статус запроса
        int status = connection.getResponseCode();
        System.out.println("Response Code: " + status);

        //Если удачно начинаем читать json
        if (status == 200) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()), 500)) {
                String line;
                StringBuilder responseContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }//И оправляем в парсер
                ParseJson(responseContent.toString());
            }
        }
    }


    public static void ParseJson(String string){
        //Десериализуем в объект
        QueryResult jsonObj = new Gson().fromJson(string, QueryResult.class);

        //Получаем доступ к нужному тегу
        Query query = jsonObj.getQuery();
        List<Page> page = query.getPages();

        //Выводим полученую информацию
        System.out.printf("Вы искали: %s\n", page.get(0).getTitle());
        System.out.println("******************************************");
        System.out.println(page.get(0).getExtract());
        System.out.println("******************************************");
        if(flagEnLang) System.out.printf("Источник: https://en.wikipedia.org/wiki?curid=%d", page.get(0).getPageid());
        else System.out.printf("Источник: https://ru.wikipedia.org/wiki?curid=%d", page.get(0).getPageid());
    }
    //Классы объекта
    public class Page {
        @SerializedName("pageid")
        @Expose
        private Integer pageid;
        @SerializedName("ns")
        @Expose
        private Integer ns;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("extract")
        @Expose
        private String extract;

        public String getExtract() {
            return extract;
        }

        public String getTitle() {
            return title;
        }

        public Integer getPageid() {
            return pageid;
        }
    }

    public class Query {

        @SerializedName("pages")
        @Expose
        private List<Page> pages = null;

        public List<Page> getPages() {
            return pages;
        }
    }

    public class QueryResult {

        @SerializedName("batchcomplete")
        @Expose
        private Boolean batchcomplete;
        @SerializedName("query")
        @Expose
        private Query query;

        public Query getQuery() {
            return query;
        }
    }
}