package com.projectApirest.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * MODEL (MVP) - Representa un Pokémon con todos sus datos
 */
public record Pokemon(
        int id,
        String name,
        int height,
        int weight,
        int baseExperience,
        List<String> types,
        List<Stat> stats,
        String spriteUrl,
        String artworkUrl,
        String description   // ← flavor text de pokemon-species
) {
    public record Stat(String name, int baseStat) {}

    public static Pokemon fromJson(JSONObject json) {
        return fromJson(json, "");
    }

    public static Pokemon fromJson(JSONObject json, String description) {
        int id             = json.getInt("id");
        String name        = json.getString("name");
        int height         = json.getInt("height");
        int weight         = json.getInt("weight");
        int baseExperience = json.optInt("base_experience", 0);

        JSONArray typesArray = json.getJSONArray("types");
        List<String> types = new ArrayList<>();
        for (int i = 0; i < typesArray.length(); i++)
            types.add(typesArray.getJSONObject(i).getJSONObject("type").getString("name"));

        JSONArray statsArray = json.getJSONArray("stats");
        List<Stat> stats = new ArrayList<>();
        for (int i = 0; i < statsArray.length(); i++) {
            JSONObject s = statsArray.getJSONObject(i);
            stats.add(new Stat(s.getJSONObject("stat").getString("name"), s.getInt("base_stat")));
        }

        JSONObject sprites = json.getJSONObject("sprites");
        String spriteUrl   = sprites.optString("front_default", "");
        String artworkUrl  = "";
        try {
            artworkUrl = sprites.getJSONObject("other")
                    .getJSONObject("official-artwork")
                    .optString("front_default", "");
        } catch (Exception ignored) {}

        return new Pokemon(id, name, height, weight, baseExperience,
                types, stats, spriteUrl, artworkUrl, description);
    }

    /** Copia del record con descripción actualizada */
    public Pokemon withDescription(String desc) {
        return new Pokemon(id, name, height, weight, baseExperience,
                types, stats, spriteUrl, artworkUrl, desc);
    }

    public String animatedGifUrl() {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions"
                + "/generation-v/black-white/animated/" + id + ".gif";
    }

    public String bestImageUrl() {
        return (artworkUrl != null && !artworkUrl.isBlank()) ? artworkUrl : spriteUrl;
    }

    public String displayName() {
        if (name == null || name.isBlank()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}