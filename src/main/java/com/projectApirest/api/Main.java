package com.projectApirest.api;

import com.projectApirest.api.presenter.PokemonPresenter;
import com.projectApirest.api.service.ApiService;
import com.projectApirest.api.view.PokemonMainFrame;

import javax.swing.*;
import java.awt.Color;

/**
 * ENTRY POINT — Wires together MVP:
 *   Model     → Pokemon (record) + ApiService
 *   View      → PokemonMainFrame (Swing)
 *   Presenter → PokemonPresenter
 */
public class Main {

    // Bloque estático: se ejecuta ANTES de main() y antes de que AWT se inicialice
    static {
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.noddraw",        "true");
        System.setProperty("sun.java2d.ddforcevram",    "false");
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background",      new Color(12, 12, 22));
            UIManager.put("SplitPane.background",  new Color(12, 12, 22));
            UIManager.put("ScrollPane.background", new Color(17, 17, 30));
            UIManager.put("Viewport.background",   new Color(17, 17, 30));
            UIManager.put("Label.background",      new Color(12, 12, 22));
            UIManager.put("List.background",       new Color(17, 17, 30));
        } catch (Exception ignored) {}

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

            // Mostrar la ventana YA pintada — evita el flash blanco inicial
            // validate() fuerza el layout antes del primer setVisible
            view.validate();
            view.setVisible(true);
            view.repaint();
            presenter.loadCurrentBatch();
        });
    }
}