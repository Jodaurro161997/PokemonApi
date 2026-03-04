package com.projectApirest.api.view;

import com.projectApirest.api.model.Pokemon;
import java.util.List;

/**
 * VIEW Interface (MVP) — contrato entre Presenter y Vista
 */
public interface PokemonView {
    void showPokemon(Pokemon pokemon);

    /** Actualiza la lista lateral (carga de batch) */
    void showPokemonList(List<Pokemon> pokemons);

    /** Muestra la cadena evolutiva en el panel inferior (no toca la lista lateral) */
    void showEvolutionChain(List<Pokemon> chain, int selectedId);

    void selectPokemonInList(Pokemon pokemon);
    void showError(String message);

    /** Muestra un diálogo de alerta cuando no se encuentra el Pokémon */
    void showNotFoundAlert(String query);
    void showStatus(String message);
    void setLoading(boolean loading);
    void clearDisplay();

    /** Habilita/deshabilita los botones de paginación */
    void setPaginationState(boolean hasPrev, boolean hasNext);
}