package org.bots.sources;

import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;
import org.bots.model.sources.FilmixFilesMessage;
import org.bots.model.sources.FilmixResponse;
import org.bots.model.sources.FilmixSearchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.bots.common.Constants.FILMIX_SUCCESS;

@Component
public class FilmixSource implements MovieSources {

    private static final Logger log = LoggerFactory.getLogger(FilmixSource.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sources.filmix.search2}")
    private String searchUrl;

    @Value("${sources.filmix.getData}")
    private String getDataUrl;

    public Movie getMovieById(Integer id){
       Movie movie = getMovie(id);
       fillMoviePlaylist(movie);
       return movie;
    }

    private Movie getMovie(Integer id){

        return new Movie();
    }

    private void fillMoviePlaylist(Movie movie){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("post_id", String.valueOf(movie.getId()));
        FilmixResponse<FilmixFilesMessage> response = null;
        try {
            response = sendRestRequest(getDataUrl, map, new ParameterizedTypeReference<FilmixResponse<FilmixFilesMessage>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(FILMIX_SUCCESS.equalsIgnoreCase(response.getType())){
            movie.setMovieContentLinks(null);
        }

    }

    @Override
    public List<MovieSearchResponse> searchMovie(String searchText) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("search_word", searchText);
        FilmixResponse<FilmixSearchMessage> response = null;
        try {
            response = sendRestRequest(searchUrl, map, new ParameterizedTypeReference<FilmixResponse<FilmixSearchMessage>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<MovieSearchResponse> result = new ArrayList<>();
        if(FILMIX_SUCCESS.equalsIgnoreCase(response.getType())){
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

    private <T> T sendRestRequest(String url, MultiValueMap<String, String> map, ParameterizedTypeReference<T> responseType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new Exception("Response error");
        }
    }

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


}
