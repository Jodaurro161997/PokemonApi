package com.projectApirest.api;

import com.projectApirest.api.presenter.PokemonPresenter;
import com.projectApirest.api.service.ApiService;
import com.projectApirest.api.view.PokemonMainFrame;

import javax.swing.*;

/**
 * ENTRY POINT — Wires together MVP:
 *   Model     → Pokemon (record) + ApiService
 *   View      → PokemonMainFrame (Swing)
 *   Presenter → PokemonPresenter
 */
public class Main {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ApiService       service   = new ApiService();
            PokemonMainFrame view      = new PokemonMainFrame(service);
            PokemonPresenter presenter = new PokemonPresenter(view, service);

            view.setPresenter(presenter);
            view.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    presenter.onDestroy();
                }
            });

            view.setVisible(true);
            presenter.loadCurrentBatch();
        });
    }
}