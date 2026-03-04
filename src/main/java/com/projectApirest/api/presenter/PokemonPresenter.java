package com.projectApirest.api.presenter;

import com.projectApirest.api.model.Pokemon;
import com.projectApirest.api.service.ApiService;
import com.projectApirest.api.view.PokemonView;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokemonPresenter {

    private static final int BATCH = 25;

    private final PokemonView     view;
    private final ApiService      apiService;
    private final ExecutorService executor;

    private int currentPage = 0;

    public PokemonPresenter(PokemonView view, ApiService apiService) {
        this.view       = view;
        this.apiService = apiService;
        this.executor   = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "pokedex-worker");
            t.setDaemon(true);
            return t;
        });
    }

    // ── Búsqueda inteligente ─────────────────────────────────────────────────

    /**
     * Lógica de búsqueda:
     * 1. Si es número → busca exactamente ese ID.
     * 2. Si es texto exacto existente → muestra ese Pokémon.
     * 3. Si no existe exacto → busca por prefijo y muestra todos los que coincidan.
     * 4. Si tampoco hay prefijo → muestra alerta de "no encontrado".
     */
    public void searchPokemon(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            onUiThread(() -> view.showError("Ingresa un ID o nombre válido."));
            return;
        }
        String query = identifier.trim().toLowerCase();
        onUiThread(() -> { view.setLoading(true); view.showStatus("Buscando: " + query + "..."); });

        executor.submit(() -> {
            // Caso 1: búsqueda numérica — exacta siempre
            if (query.matches("\\d+")) {
                doExactSearch(query);
                return;
            }

            // Caso 2: siempre buscar por prefijo para texto
            onUiThread(() -> view.showStatus("Buscando \"" + query + "\"..."));
            List<String> matches = apiService.findByPrefix(query);

            if (matches.isEmpty()) {
                onUiThread(() -> { view.setLoading(false); view.showNotFoundAlert(query); });
                return;
            }

            // Cargar todos los que coinciden con el prefijo
            List<Pokemon> results = new java.util.ArrayList<>();
            for (String name : matches) {
                try { results.add(apiService.getPokemon(name)); }
                catch (Exception ignored2) {}
            }

            if (results.isEmpty()) {
                onUiThread(() -> { view.setLoading(false); view.showNotFoundAlert(query); });
                return;
            }

            // Buscar si hay coincidencia exacta para mostrarla primero en el detalle
            final Pokemon exactMatch = results.stream()
                    .filter(p -> p.name().equalsIgnoreCase(query))
                    .findFirst()
                    .orElse(results.get(0));

            List<Pokemon> finalResults = results;
            onUiThread(() -> {
                view.setLoading(false);
                view.showPokemonList(finalResults);
                view.showPokemon(exactMatch);
                view.selectPokemonInList(exactMatch);
                String label = finalResults.size() == 1
                        ? "✓ " + exactMatch.displayName() + " encontrado."
                        : finalResults.size() + " Pokémon con \"" + query + "\"";
                view.showStatus(label);
            });
            loadEvolutionChainFor(exactMatch);
        });
    }

    // ── Paginación ────────────────────────────────────────────────────────────

    public void loadCurrentBatch() {
        int start = currentPage * BATCH + 1;
        int end   = start + BATCH - 1;
        onUiThread(() -> {
            view.setLoading(true);
            view.showStatus("Cargando #" + start + " – #" + end + "...");
            view.clearDisplay();
            view.setPaginationState(currentPage > 0, true);
        });
        executor.submit(() -> {
            List<Pokemon> list = apiService.getPokemons(start, end,
                    p -> onUiThread(() -> view.showStatus(p)));
            onUiThread(() -> {
                view.setLoading(false);
                view.showPokemonList(list);
                view.setPaginationState(currentPage > 0, true);
                view.showStatus("Pokémon #" + start + " – #" + end);
                if (!list.isEmpty()) {
                    view.showPokemon(list.get(0));
                    view.selectPokemonInList(list.get(0));
                }
            });
            if (!list.isEmpty()) loadEvolutionChainFor(list.get(0));
        });
    }

    public void loadNextBatch() { currentPage++; loadCurrentBatch(); }
    public void loadPrevBatch() { if (currentPage > 0) { currentPage--; loadCurrentBatch(); } }

    // ── Navegación pokémon-a-pokémon ──────────────────────────────────────────

    public void navigatePrevious(int id) { if (id > 1) searchExact(String.valueOf(id - 1)); }
    public void navigateNext(int id)     { searchExact(String.valueOf(id + 1)); }

    /** Selecciona de la lista lateral sin alterar la lista misma */
    public void selectFromList(Pokemon pokemon) {
        onUiThread(() -> {
            view.showPokemon(pokemon);
            view.selectPokemonInList(pokemon);
            view.showStatus("✓ " + pokemon.displayName() + " — cargando evoluciones...");
        });
        loadEvolutionChainFor(pokemon);
    }

    public void onDestroy() { executor.shutdownNow(); }

    // ── Privados ──────────────────────────────────────────────────────────────

    /** Búsqueda exacta por ID o nombre — puede llamarse tanto desde el EDT como desde el executor */
    private void searchExact(String identifier) {
        onUiThread(() -> { view.setLoading(true); view.showStatus("Buscando: " + identifier + "..."); });
        executor.submit(() -> doExactSearch(identifier));
    }

    /** Núcleo de la búsqueda exacta — ejecutar siempre en hilo worker, nunca en EDT */
    private void doExactSearch(String identifier) {
        try {
            Pokemon pokemon = apiService.getPokemon(identifier);
            onUiThread(() -> {
                view.setLoading(false);
                view.showPokemon(pokemon);
                view.selectPokemonInList(pokemon);
                view.showStatus("✓ " + pokemon.displayName() + " — cargando evoluciones...");
            });
            loadEvolutionChainFor(pokemon);
        } catch (ApiService.PokemonNotFoundException e) {
            onUiThread(() -> { view.setLoading(false); view.showNotFoundAlert(identifier); });
        } catch (ApiService.ApiException e) {
            onUiThread(() -> { view.setLoading(false); view.showError("❌ " + e.getMessage()); });
        }
    }

    private void loadEvolutionChainFor(Pokemon pokemon) {
        executor.submit(() -> {
            try {
                List<Pokemon> chain = apiService.getEvolutionChain(pokemon.id());

                // Si la cadena tiene solo 1 Pokémon, buscar por prefijo del nombre
                // para mostrar Pokémon relacionados (ej: mew → mew, mewtwo, mewostic…)
                if (chain.size() == 1) {
                    String prefix = pokemon.name().toLowerCase();
                    // Usar solo los primeros 3 caracteres para encontrar familia amplia
                    String shortPrefix = prefix.length() > 3 ? prefix.substring(0, 3) : prefix;
                    List<String> related = apiService.findByPrefix(shortPrefix);
                    if (related.size() > 1) {
                        List<Pokemon> relatedPokemon = new java.util.ArrayList<>();
                        relatedPokemon.add(pokemon); // El actual siempre primero
                        for (String name : related) {
                            if (!name.equalsIgnoreCase(pokemon.name())) {
                                try { relatedPokemon.add(apiService.getPokemon(name)); }
                                catch (Exception ignored) {}
                            }
                        }
                        final List<Pokemon> finalRelated = relatedPokemon;
                        onUiThread(() -> {
                            view.showEvolutionChain(finalRelated, pokemon.id());
                            view.showStatus(pokemon.displayName() + " — Pokémon relacionados: " + finalRelated.size());
                        });
                        return;
                    }
                }

                onUiThread(() -> {
                    view.showEvolutionChain(chain, pokemon.id());
                    String label = chain.size() == 1
                            ? pokemon.displayName() + " no evoluciona."
                            : chain.stream().map(Pokemon::displayName)
                            .reduce((a, b) -> a + " → " + b).orElse("");
                    view.showStatus(label);
                });
            } catch (Exception e) {
                onUiThread(() -> {
                    view.showEvolutionChain(List.of(pokemon), pokemon.id());
                    view.showStatus("✓ " + pokemon.displayName() + " cargado.");
                });
            }
        });
    }

    private void onUiThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }
}