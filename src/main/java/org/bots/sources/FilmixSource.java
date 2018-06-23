package org.bots.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bots.model.datebase.Movie;
import org.bots.model.datebase.MovieFile;
import org.bots.model.items.MovieSearchResponse;
import org.bots.model.sources.FilmixDataResponse;
import org.bots.model.sources.FilmixFilesMessage;
import org.bots.model.sources.FilmixPlaylist;
import org.bots.model.sources.FilmixSearchResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bots.common.Constants.FILMIX_SUCCESS;

@Component
@Slf4j
public class FilmixSource implements MovieSources {

    private static final String SOURCE = "filmix";

    private static final String PLAYLIST_TRUE = "yes";
    private static final Pattern p = Pattern.compile(".+(\\[.*?]).*");

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sources.filmix.search2}")
    private String searchUrl;

    @Value("${sources.filmix.data}")
    private String dataUrl;

    @Value("${sources.filmix.info}")
    private String infoUrl;

    private static String decoder(String data)
    {
        String[] a = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "=", "B", "D", "H", "I", "J", "L", "M", "N", "U", "V", "Z", "c", "f", "i", "k", "n", "p"};
        String[] b = new String[] {"d", "9", "b", "e", "R", "X", "8", "T", "r", "Y", "G", "W", "s", "u", "Q", "y", "a", "w", "o", "g", "z", "v", "m", "l", "x", "t"};

        for (int i=0; i < a.length; i++)
        {
            data = data.replace(b[i], "__");
            data = data.replace(a[i], b[i]);
            data = data.replace("__", a[i]);
        }

        return new String(Base64.getDecoder().decode(data));
    }

    public Movie getMovieById(Integer id) {
        String token = null;
        Movie movie = getMovie(id, token);
        List<MovieFile> movieFiles = fillMovieFiles(movie, "60kl9voiena7uqsog0gpel8l55");
        movie.setMovieFiles( movieFiles);
        return movie;
    }

    private Movie getMovie(Integer id, String token) {
        Movie resultMovie = new Movie();
        resultMovie.setId(id);
        try {
            Document doc = Jsoup.connect(String.format(infoUrl, id)).get();
            Elements content = doc.getElementsByClass("titles-left");
            Element element = content.get(0);
            Elements href = element.select(" div > a");
            String infoLink = href.get(0).attr("href");

            doc = Jsoup.connect(infoLink).get();
            Element elementInfo = doc.getElementById("dle-content");
            Elements imgTag = elementInfo.select("article > div > span > a");
            resultMovie.setPoster(imgTag.attr("href"));

            Elements name = elementInfo.select("div > div > h1.name");
            resultMovie.setTitle(name.get(0).childNode(0).outerHtml());


            Elements originName = elementInfo.select("div > div > div.origin-name");
            resultMovie.setOriginalTitle(originName.get(0).childNode(0).outerHtml());

            Elements directors = doc.select("div.directors > span > span > a > span[itemprop=\"name\"]");
            resultMovie.setDirectors(directors.stream().map(elem -> elem.childNode(0).outerHtml()).collect(Collectors.toList()));

            Elements actors = doc.select("div.actors > span > span > a > span[itemprop=\"name\"]");
            resultMovie.setCasts(actors.stream().map(elem -> elem.childNode(0).outerHtml()).collect(Collectors.toList()));

            Elements category = doc.select("div.category > span > span > a[itemprop=\"genre\"]");
            resultMovie.setCategories(category.stream().map(elem -> elem.childNode(0).outerHtml()).collect(Collectors.toList()));

            Elements year = doc.select("div.year > span > a[itemprop=\"copyrightYear\"]");
            resultMovie.setDate(year.stream().map(elem -> elem.childNode(0).outerHtml()).reduce((s, s2) -> s + " - " + s2).orElse(""));

            Elements ratio = doc.select("div > div > footer > span.imdb > p");
            resultMovie.setRatio(ratio.get(0).childNode(0).outerHtml());

        } catch (IOException e) {
            log.error("Error parsing movie description", e);
        }

        return resultMovie;
    }

    private List<MovieFile> fillMovieFiles(Movie movie, String filmixToken) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("post_id", String.valueOf(movie.getId()));

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "FILMIXNET=" + filmixToken);
        FilmixDataResponse response = null;
        try {
            response = sendRestRequest(dataUrl, requestParams, headers, FilmixDataResponse.class);
        } catch (Exception e) {
            log.error("Error getting movie file list", e);
        }
        List<MovieFile> movieFiles = new ArrayList<>();

        if (response != null && FILMIX_SUCCESS.equalsIgnoreCase(response.getType())) {
            if (response.getMessage() != null && response.getMessage().getTranslations() != null) {
                FilmixFilesMessage.Translation translation = response.getMessage().getTranslations();
                if (PLAYLIST_TRUE.equalsIgnoreCase(translation.getPlaylist())) {
                    movie.setWithPlaylist(true);
                    translation.getFlash().forEach((key, value) -> {
                        movieFiles.addAll(createPlaylist(key, decoder(value)));
                    });
                } else {
                    translation.getFlash().forEach((key, value) -> {
                        MovieFile movieFile = new MovieFile();
                        movieFile.setName(key);
                        movieFile.setSource(SOURCE);
                        movieFile.setUrls(generateFileLinkMap(decoder(value)));
                        movieFiles.add(movieFile);
                    });
                }
            }
        }
        return movieFiles;
    }

    private List<MovieFile> createPlaylist(String translation, String url){
        List<MovieFile> result = new ArrayList<>();
        FilmixPlaylist filmixPlaylist = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", "FILMIXNET=6o53jodaeddcie9i5kca1qk9j7");
            HttpEntity<String> request = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            String decoded = decoder(response.getBody());
            filmixPlaylist = mapper.readValue(decoded, FilmixPlaylist.class);
        } catch (Exception e) {
            log.error("Error creating playlist", e);
        }
        if(filmixPlaylist != null) {
            List<FilmixPlaylist> grouped = filmixPlaylist.getPlaylist()
                    .stream()
                    .map(FilmixPlaylist::getPlaylist)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            grouped.forEach(fpl -> result.add(createMovieFiles(translation, fpl)));
        }
        return result;
    }

    private MovieFile createMovieFiles(String translation, FilmixPlaylist filmixFile){
        MovieFile movieFile = new MovieFile();
        movieFile.setSource(SOURCE);
        movieFile.setName(filmixFile.getComment());
        movieFile.setSeason(Integer.valueOf(filmixFile.getSeason()));
        movieFile.setSeries(Integer.valueOf(filmixFile.getSerieId()));
        movieFile.setUrls(generateFileLinkMap(filmixFile.getFile()));
        movieFile.setTranslation(translation);
        return movieFile;
    }

    private Map<String, String> generateFileLinkMap(final String url){
        Map<String,String> result = new LinkedHashMap<>();
        Matcher m = p.matcher( url);
        if(m.matches()){
            String qualityStr = m.group(1);
            List<String> qualityList = makeQualityList(qualityStr);
            qualityList.forEach(s -> {
                result.put(s, url.replace(qualityStr, s));
            });
        }
        return result;
    }

    private List<String> makeQualityList(String qualityList) {
        String str = qualityList.substring(1, qualityList.length()-1);
        return Arrays.stream(str.split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<MovieSearchResponse> searchMovie(String searchText) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("search_word", searchText);
        FilmixSearchResponse response = null;
        try {
            response = sendRestRequest(searchUrl, requestParams, null, FilmixSearchResponse.class);
        } catch (Exception e) {
            log.error("Error getting search request", e);
        }

        List<MovieSearchResponse> result = new ArrayList<>();
        if(response != null && FILMIX_SUCCESS.equalsIgnoreCase(response.getType())){
                response.getMessage().forEach(msg -> {
                    MovieSearchResponse searchResult = new MovieSearchResponse();
                    searchResult.setId(msg.getId());
                    searchResult.setOriginalName(msg.getOriginalName());
                    searchResult.setTitle(msg.getTitle());
                    searchResult.setYear(msg.getYear());
                    searchResult.setPoster(msg.getPoster());
                    result.add(searchResult);
                });
            }
        return result;
    }

    private <T> T sendRestRequest(String url, MultiValueMap<String, String> map, Map<String,String> headMap, Class<T> responseType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Requested-With", "XMLHttpRequest");
        if(headMap != null){
            headMap.forEach(headers::set);
        }
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return mapper.readValue(response.getBody(), responseType);
        } else {
            throw new Exception("Response error");
        }
    }
}
