package tn.formini.services.recommendations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.formini.entities.formations.Formation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FormationRecommendationService {

    private static final String GOOGLE_BOOKS_API_KEY = "AIzaSyCfYy9jz7OGKjU_9EUYxqNV46BhBsyX06Y";
    private static final String YOUTUBE_API_KEY = "AIzaSyCzI8l4ftjBinhQhAY0q44eGQLtdbfoWUg";

    public static class BookRecommendation {
        public String title;
        public String author;
        public String publisher;
        public String publishedDate;
        public String description;
        public String previewUrl;
        public String volumeId;
        public String thumbnailUrl;

        @Override
        public String toString() {
            return "📖 " + title + (author != null && !author.isEmpty() ? " - " + author : "");
        }
    }

    public static class VideoRecommendation {
        public String title;
        public String videoId;
        public String url;
        public String channelTitle;
        public String publishedAt;
        public String description;
        public String thumbnailUrl;

        @Override
        public String toString() {
            return "🎬 " + title + (channelTitle != null && !channelTitle.isEmpty() ? " - " + channelTitle : "");
        }
    }

    public static class RecommendationResult {
        public List<BookRecommendation> books = new ArrayList<>();
        public List<VideoRecommendation> videos = new ArrayList<>();
    }

    public RecommendationResult getRecommendations(Formation formation) {
        RecommendationResult result = new RecommendationResult();
        String searchQuery = formation.getTitre();

        System.out.println("🔍 Recherche pour: " + searchQuery);

        // Simplifier la recherche pour mieux fonctionner
        String simpleQuery = searchQuery.split(":")[0].trim();
        System.out.println("🔍 Requête simplifiée: " + simpleQuery);

        try {
            result.books = searchGoogleBooks(simpleQuery);
            System.out.println("✅ " + result.books.size() + " livres trouvés");
        } catch (Exception e) {
            System.err.println("❌ Erreur Books: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            result.videos = searchYouTube(simpleQuery);
            System.out.println("✅ " + result.videos.size() + " vidéos trouvées");
        } catch (Exception e) {
            System.err.println("❌ Erreur YouTube: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public List<BookRecommendation> searchGoogleBooks(String query) throws Exception {
        List<BookRecommendation> books = new ArrayList<>();
        String encodedQuery = URLEncoder.encode(query, "UTF-8");

        String urlString = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery +
                "&maxResults=10&key=" + GOOGLE_BOOKS_API_KEY;

        System.out.println("📡 URL Google Books: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int responseCode = conn.getResponseCode();
        System.out.println("📡 Code: " + responseCode);

        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String json = response.toString();
            System.out.println("📡 JSON reçu (début): " + (json.length() > 200 ? json.substring(0, 200) : json));

            // Parser le JSON avec Gson
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            if (jsonObject.has("items")) {
                JsonArray items = jsonObject.getAsJsonArray("items");
                System.out.println("📡 Nombre d'items trouvés: " + items.size());

                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.get(i).getAsJsonObject();
                    JsonObject volumeInfo = item.getAsJsonObject("volumeInfo");

                    if (volumeInfo != null) {
                        BookRecommendation book = new BookRecommendation();

                        if (volumeInfo.has("title")) {
                            book.title = volumeInfo.get("title").getAsString();
                        } else {
                            continue;
                        }

                        if (volumeInfo.has("authors")) {
                            JsonArray authors = volumeInfo.getAsJsonArray("authors");
                            book.author = authors.size() > 0 ? authors.get(0).getAsString() : "Auteur inconnu";
                        } else {
                            book.author = "Auteur inconnu";
                        }

                        book.publisher = volumeInfo.has("publisher") ? volumeInfo.get("publisher").getAsString() : "";
                        book.publishedDate = volumeInfo.has("publishedDate") ? volumeInfo.get("publishedDate").getAsString() : "";
                        book.description = volumeInfo.has("description") ? volumeInfo.get("description").getAsString() : "Pas de description";
                        book.previewUrl = volumeInfo.has("previewLink") ? volumeInfo.get("previewLink").getAsString() : "";

                        books.add(book);
                        System.out.println("  📚 Ajouté: " + book.title + " par " + book.author);
                    }
                }
            } else {
                System.out.println("📡 Aucun item trouvé dans la réponse");
            }
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            System.err.println("Erreur API: " + errorResponse.toString());
        }

        conn.disconnect();
        return books;
    }

    public List<VideoRecommendation> searchYouTube(String query) throws Exception {
        List<VideoRecommendation> videos = new ArrayList<>();
        String encodedQuery = URLEncoder.encode(query + " cours tutorial", "UTF-8");

        String urlString = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=" +
                encodedQuery + "&type=video&key=" + YOUTUBE_API_KEY;

        System.out.println("📡 URL YouTube: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int responseCode = conn.getResponseCode();
        System.out.println("📡 Code YouTube: " + responseCode);

        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String json = response.toString();
            System.out.println("📡 JSON YouTube (début): " + (json.length() > 200 ? json.substring(0, 200) : json));

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            if (jsonObject.has("items")) {
                JsonArray items = jsonObject.getAsJsonArray("items");
                System.out.println("📡 Nombre de vidéos: " + items.size());

                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.get(i).getAsJsonObject();
                    JsonObject snippet = item.getAsJsonObject("snippet");

                    if (snippet != null) {
                        VideoRecommendation video = new VideoRecommendation();

                        if (snippet.has("title")) {
                            video.title = snippet.get("title").getAsString();
                        } else {
                            continue;
                        }

                        video.channelTitle = snippet.has("channelTitle") ? snippet.get("channelTitle").getAsString() : "";
                        video.description = snippet.has("description") ? snippet.get("description").getAsString() : "Pas de description";

                        if (video.description.length() > 200) {
                            video.description = video.description.substring(0, 200) + "...";
                        }

                        JsonObject id = item.getAsJsonObject("id");
                        if (id != null && id.has("videoId")) {
                            video.videoId = id.get("videoId").getAsString();
                            video.url = "https://www.youtube.com/watch?v=" + video.videoId;
                        }

                        videos.add(video);
                        System.out.println("  🎬 Ajouté: " + video.title + " - " + video.channelTitle);
                    }
                }
            }
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            System.err.println("Erreur YouTube: " + errorResponse.toString());
        }

        conn.disconnect();
        return videos;
    }
}