package com.projectApirest.api.service;

import com.projectApirest.api.model.Pokemon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SERVICE (MVP) - Capa de datos: consume la PokeAPI con Java 11+ HttpClient
 */
public class ApiService {

    private static final String BASE_URL     = "https://pokeapi.co/api/v2/pokemon/";
    private static final String SPECIES_URL  = "https://pokeapi.co/api/v2/pokemon-species/";

    private final HttpClient httpClient;


    /** Caché de todos los nombres de Pokémon para la búsqueda por prefijo */
    private final AtomicReference<List<String>> nameCache = new AtomicReference<>(Collections.emptyList());

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // ── Pokémon ───────────────────────────────────────────────────────────────

    /** Obtiene un Pokémon por ID o nombre, incluyendo su descripción de especie */
    public Pokemon getPokemon(String identifier) {
        String url = BASE_URL + identifier.toLowerCase().trim();
        JSONObject json = fetchJson(url, identifier);
        Pokemon pokemon = Pokemon.fromJson(json);
        // Obtener descripción en español (o inglés como fallback) desde pokemon-species
        try {
            JSONObject species = fetchJson(SPECIES_URL + pokemon.id(), identifier);
            String desc = extractDescription(species);
            if (!desc.isBlank()) pokemon = pokemon.withDescription(desc);
        } catch (Exception ignored) {}
        return pokemon;
    }

    /** Extrae el flavor text en español, o inglés si no hay */
    private String extractDescription(JSONObject species) {
        JSONArray entries = species.optJSONArray("flavor_text_entries");
        if (entries == null) return "";
        String english = "";
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String lang = entry.getJSONObject("language").getString("name");
            String text = entry.getString("flavor_text")
                    .replace("\n", " ").replace("\f", " ").replaceAll("\s+", " ").trim();
            if (lang.equals("es")) return text;
            if (lang.equals("en") && english.isEmpty()) english = text;
        }
        return english;
    }

    /** Carga varios Pokémon en rango [startId, endId] */
    public List<Pokemon> getPokemons(int startId, int endId,
                                     java.util.function.Consumer<String> progressCallback) {
        List<Pokemon> list = new ArrayList<>();
        for (int i = startId; i <= endId; i++) {
            try {
                Pokemon p = getPokemon(String.valueOf(i));
                list.add(p);
                if (progressCallback != null) progressCallback.accept("✓ " + p.displayName());
            } catch (Exception e) {
                if (progressCallback != null) progressCallback.accept("✗ #" + i + " error");
            }
        }
        return list;
    }

    // ── Cadena evolutiva ──────────────────────────────────────────────────────

    /**
     * Devuelve todos los Pokémon de la cadena evolutiva del Pokémon dado.
     * Orden: base → evoluciones → mega/variantes.
     */
    public List<Pokemon> getEvolutionChain(int pokemonId) {
        // 1. species → evolution_chain.url
        String speciesUrl = SPECIES_URL + pokemonId;
        JSONObject species = fetchJson(speciesUrl, String.valueOf(pokemonId));
        String chainUrl = species.getJSONObject("evolution_chain").getString("url");

        // 2. chain → nombres
        JSONObject chainData = fetchJson(chainUrl, "evolution_chain");
        List<String> names = new ArrayList<>();
        collectChainNames(chainData.getJSONObject("chain"), names);

        // 3. cargar cada Pokémon de la cadena
        List<Pokemon> result = new ArrayList<>();
        for (String name : names) {
            try { result.add(getPokemon(name)); }
            catch (Exception ignored) {}
        }
        return result;
    }

    /** Recorre recursivamente el árbol de evolución y recoge los nombres */
    private void collectChainNames(JSONObject node, List<String> names) {
        String name = node.getJSONObject("species").getString("name");
        names.add(name);
        JSONArray evolvesTo = node.getJSONArray("evolves_to");
        for (int i = 0; i < evolvesTo.length(); i++) {
            collectChainNames(evolvesTo.getJSONObject(i), names);
        }
    }

    /**
     * Devuelve todos los nombres de Pokémon (cacheado en memoria).
     * Se llama una sola vez; las llamadas siguientes usan la caché.
     */
    public List<String> getAllPokemonNames() {
        List<String> cached = nameCache.get();
        if (!cached.isEmpty()) return cached;

        try {
            // limit=2000 trae todos los Pokémon disponibles
            JSONObject json = fetchJson("https://pokeapi.co/api/v2/pokemon?limit=2000", "names");
            JSONArray results = json.getJSONArray("results");
            List<String> names = new ArrayList<>(results.length());
            for (int i = 0; i < results.length(); i++) {
                names.add(results.getJSONObject(i).getString("name"));
            }
            nameCache.set(names);
            return names;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Busca todos los Pokémon cuyo nombre contenga el texto dado en cualquier posición.
     * Retorna lista vacía si ninguno coincide.
     */
    public List<String> findByPrefix(String prefix) {
        String p = prefix.toLowerCase().trim();
        List<String> all = getAllPokemonNames();
        List<String> matches = new ArrayList<>();
        for (String name : all) {
            if (name.contains(p)) matches.add(name);
        }
        return matches;
    }

    // ── Imagen como bytes (evita problemas SSL con ImageIO.read(URL)) ─────────

    /**
     * Descarga los bytes de una imagen desde la URL dada.
     * Usar HttpClient garantiza seguimiento de redirects y TLS correcto.
     */
    public byte[] fetchImageBytes(String imageUrl) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<byte[]> res = httpClient.send(
                    req, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() == 200) return res.body();
        } catch (Exception e) {
            // silencio: la vista mostrará placeholder
        }
        return null;
    }

    // ── Helper HTTP ───────────────────────────────────────────────────────────

    private JSONObject fetchJson(String url, String identifier) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> new JSONObject(response.body());
                case 404 -> throw new PokemonNotFoundException(
                        "Pokémon '%s' no encontrado.".formatted(identifier));
                default  -> throw new ApiException(
                        "Error HTTP %d de la API.".formatted(response.statusCode()));
            };
        } catch (PokemonNotFoundException | ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("La petición fue interrumpida.", e);
        } catch (IOException e) {
            throw new ApiException("Error de conexión: " + e.getMessage(), e);
        }
    }

    // ── Excepciones personalizadas ────────────────────────────────────────────

    public static class PokemonNotFoundException extends RuntimeException {
        public PokemonNotFoundException(String msg) { super(msg); }
    }

    public static class ApiException extends RuntimeException {
        public ApiException(String msg)                  { super(msg); }
        public ApiException(String msg, Throwable cause) { super(msg, cause); }
    }
}