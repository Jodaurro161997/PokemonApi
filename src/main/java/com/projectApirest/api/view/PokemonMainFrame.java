package com.projectApirest.api.view;

import com.projectApirest.api.model.Pokemon;
import com.projectApirest.api.presenter.PokemonPresenter;
import com.projectApirest.api.service.ApiService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokemonMainFrame extends JFrame implements PokemonView {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(12,  12,  22);
    private static final Color BG_CARD        = new Color(22,  22,  38);
    private static final Color BG_LIST        = new Color(17,  17,  30);
    private static final Color ACCENT_RED     = new Color(220,  50,  50);
    private static final Color ACCENT_GOLD    = new Color(255, 195,   0);
    private static final Color TEXT_PRIMARY   = new Color(240, 240, 255);
    private static final Color TEXT_SECONDARY = new Color(145, 145, 175);
    private static final Color BORDER_COLOR   = new Color(38,  38,  62);

    // Colores por tipo
    private static final Map<String, Color> TYPE_COLORS = new HashMap<>();
    static {
        TYPE_COLORS.put("fire",     new Color(240, 128,  48));
        TYPE_COLORS.put("water",    new Color( 80, 130, 240));
        TYPE_COLORS.put("grass",    new Color(100, 195,  70));
        TYPE_COLORS.put("electric", new Color(248, 210,  40));
        TYPE_COLORS.put("psychic",  new Color(248,  80, 130));
        TYPE_COLORS.put("ice",      new Color(130, 210, 215));
        TYPE_COLORS.put("dragon",   new Color(100,  50, 240));
        TYPE_COLORS.put("dark",     new Color( 70,  55,  45));
        TYPE_COLORS.put("normal",   new Color(180, 155,  95));
        TYPE_COLORS.put("fighting", new Color(195,  45,  38));
        TYPE_COLORS.put("poison",   new Color(155,  55, 160));
        TYPE_COLORS.put("ground",   new Color(215, 180,  90));
        TYPE_COLORS.put("flying",   new Color(155, 135, 240));
        TYPE_COLORS.put("bug",      new Color(155, 180,  28));
        TYPE_COLORS.put("rock",     new Color(180, 155,  50));
        TYPE_COLORS.put("ghost",    new Color(100,  78, 148));
        TYPE_COLORS.put("steel",    new Color(175, 180, 205));
        TYPE_COLORS.put("fairy",    new Color(235, 145, 172));
    }

    // Nombres en español
    private static final Map<String, String> TYPE_NAMES = new HashMap<>();
    static {
        TYPE_NAMES.put("fire",     "Fuego");
        TYPE_NAMES.put("water",    "Agua");
        TYPE_NAMES.put("grass",    "Planta");
        TYPE_NAMES.put("electric", "Eléctrico");
        TYPE_NAMES.put("psychic",  "Psíquico");
        TYPE_NAMES.put("ice",      "Hielo");
        TYPE_NAMES.put("dragon",   "Dragón");
        TYPE_NAMES.put("dark",     "Siniestro");
        TYPE_NAMES.put("normal",   "Normal");
        TYPE_NAMES.put("fighting", "Lucha");
        TYPE_NAMES.put("poison",   "Veneno");
        TYPE_NAMES.put("ground",   "Tierra");
        TYPE_NAMES.put("flying",   "Volador");
        TYPE_NAMES.put("bug",      "Bicho");
        TYPE_NAMES.put("rock",     "Roca");
        TYPE_NAMES.put("ghost",    "Fantasma");
        TYPE_NAMES.put("steel",    "Acero");
        TYPE_NAMES.put("fairy",    "Hada");
    }

    // Iconos emoji por tipo — más grandes ahora que son tarjetas verticales
    private static final Map<String, String> TYPE_ICONS = new HashMap<>();
    static {
        TYPE_ICONS.put("fire",     "🔥");
        TYPE_ICONS.put("water",    "💧");
        TYPE_ICONS.put("grass",    "🍃");
        TYPE_ICONS.put("electric", "⚡");
        TYPE_ICONS.put("psychic",  "🔮");
        TYPE_ICONS.put("ice",      "❄");
        TYPE_ICONS.put("dragon",   "🐉");
        TYPE_ICONS.put("dark",     "🌑");
        TYPE_ICONS.put("normal",   "⭐");
        TYPE_ICONS.put("fighting", "👊");
        TYPE_ICONS.put("poison",   "☠");
        TYPE_ICONS.put("ground",   "🌍");
        TYPE_ICONS.put("flying",   "🌬");
        TYPE_ICONS.put("bug",      "🐛");
        TYPE_ICONS.put("rock",     "🪨");
        TYPE_ICONS.put("ghost",    "👻");
        TYPE_ICONS.put("steel",    "⚙");
        TYPE_ICONS.put("fairy",    "🌸");
    }

    // ── Componentes ───────────────────────────────────────────────────────────
    private PokemonPresenter  presenter;
    private ApiService        apiService;

    private JTextField        searchField;
    private JButton           searchBtn, prevListBtn, nextListBtn;
    private JLabel            pokemonIdLabel, pokemonNameLabel;
    private AnimatedGifLabel  pokemonImageLabel;
    private JPanel            typesPanel, statsPanel, evolutionPanel;
    private JLabel            heightLabel, weightLabel, expLabel;
    private JTextArea         descriptionLabel;
    private JLabel            statusLabel;
    private JProgressBar      loadingBar;
    private JButton           pokePrevBtn, pokeNextBtn;

    private DefaultListModel<Pokemon> listModel;
    private JList<Pokemon>            pokemonJList;
    private List<Pokemon>             loadedPokemons = new ArrayList<>();

    private JPanel  contentPanel;
    private Color   bgTo    = BG_DARK;
    private float   bgAlpha = 1f;
    private Timer   bgTimer;
    private int     currentPokemonId = -1;

    private final ExecutorService imageLoader = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "img-loader"); t.setDaemon(true); return t;
    });

    // ── Constructor ───────────────────────────────────────────────────────────

    public PokemonMainFrame(ApiService apiService) {
        super("Pokédex — Java MVP");
        this.apiService = apiService;
        buildUI();
        pack();
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void setPresenter(PokemonPresenter p) { this.presenter = p; }

    // ── Construcción ─────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Top Bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 22, 12, 22)));

        JLabel logo = new JLabel("  Pokédex");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(ACCENT_GOLD);
        logo.setIcon(circleIcon(ACCENT_RED, 14));
        bar.add(logo, BorderLayout.WEST);

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        sp.setOpaque(false);
        searchField = styledField("ID o nombre (ej: 25, pikachu)");
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.addActionListener(e -> doSearch());
        searchBtn = styledBtn("Buscar", ACCENT_RED);
        searchBtn.addActionListener(e -> doSearch());
        sp.add(searchField); sp.add(searchBtn);
        bar.add(sp, BorderLayout.CENTER);

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(120, 6));
        loadingBar.setForeground(ACCENT_GOLD);
        loadingBar.setBackground(BG_DARK);
        JPanel rp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rp.setOpaque(false); rp.add(loadingBar);
        bar.add(rp, BorderLayout.EAST);
        return bar;
    }

    // ── Centro ────────────────────────────────────────────────────────────────

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildListPanel(), buildDetailPanel());
        split.setDividerLocation(215);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setBackground(BG_DARK);
        return split;
    }

    // ── Lista lateral ─────────────────────────────────────────────────────────

    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIST);
        panel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JLabel title = new JLabel("  Pokémon");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(TEXT_SECONDARY);
        title.setBackground(BG_CARD);
        title.setOpaque(true);
        title.setBorder(new EmptyBorder(9, 12, 9, 12));
        panel.add(title, BorderLayout.NORTH);

        listModel    = new DefaultListModel<>();
        pokemonJList = new JList<>(listModel);
        pokemonJList.setBackground(BG_LIST);
        pokemonJList.setForeground(TEXT_PRIMARY);
        pokemonJList.setSelectionBackground(new Color(55, 55, 100));
        pokemonJList.setSelectionForeground(ACCENT_GOLD);
        pokemonJList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pokemonJList.setFixedCellHeight(36);
        pokemonJList.setCellRenderer(new PokeListRenderer());
        pokemonJList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Pokemon p = pokemonJList.getSelectedValue();
                if (p != null) presenter.selectFromList(p);
            }
        });

        JScrollPane scroll = new JScrollPane(pokemonJList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_LIST);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);

        prevListBtn = styledBtn("◀  Lista anterior", new Color(45, 75, 135));
        nextListBtn = styledBtn("Lista siguiente  ▶", new Color(45, 75, 135));
        prevListBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        nextListBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        prevListBtn.setPreferredSize(new Dimension(210, 32));
        nextListBtn.setPreferredSize(new Dimension(210, 32));
        prevListBtn.addActionListener(e -> presenter.loadPrevBatch());
        nextListBtn.addActionListener(e -> presenter.loadNextBatch());
        prevListBtn.setEnabled(false);

        JPanel pagPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        pagPanel.setBackground(BG_CARD);
        pagPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(8, 5, 8, 5)));
        pagPanel.add(prevListBtn);
        pagPanel.add(nextListBtn);
        panel.add(pagPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ── Panel detalle ─────────────────────────────────────────────────────────

    private JPanel buildDetailPanel() {
        contentPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (bgAlpha > 0) {
                    Color tint = new Color(bgTo.getRed(), bgTo.getGreen(), bgTo.getBlue(),
                            Math.min(255, (int)(bgAlpha * 50)));
                    g2.setColor(tint);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.add(buildDetailHeader(),    BorderLayout.NORTH);
        contentPanel.add(buildDetailBody(),      BorderLayout.CENTER);
        contentPanel.add(buildEvolutionStrip(),  BorderLayout.SOUTH);
        return contentPanel;
    }

    private JPanel buildDetailHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(18, 28, 6, 28));

        pokemonIdLabel = new JLabel("#???");
        pokemonIdLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        pokemonIdLabel.setForeground(TEXT_SECONDARY);
        pokemonNameLabel = new JLabel("Selecciona un Pokémon");
        pokemonNameLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        pokemonNameLabel.setForeground(TEXT_PRIMARY);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(pokemonIdLabel);
        left.add(pokemonNameLabel);
        h.add(left, BorderLayout.WEST);

        pokePrevBtn = arrowNavBtn("←");
        pokeNextBtn = arrowNavBtn("→");
        pokePrevBtn.addActionListener(e -> { if (currentPokemonId > 1) presenter.navigatePrevious(currentPokemonId); });
        pokeNextBtn.addActionListener(e -> { if (currentPokemonId > 0) presenter.navigateNext(currentPokemonId); });
        JPanel navP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navP.setOpaque(false);
        navP.add(pokePrevBtn); navP.add(pokeNextBtn);
        h.add(navP, BorderLayout.EAST);
        return h;
    }

    /**
     * Layout del cuerpo:
     *  - Columna IZQUIERDA (40%): imagen grande + tipos verticales
     *  - Columna DERECHA  (60%): info básica (fila) + stats + descripción   ← todo vertical
     */
    private JPanel buildDetailBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 28, 4, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTH;
        g.fill   = GridBagConstraints.BOTH;
        g.weighty = 1;

        // Columna izquierda: imagen + tipos en vertical
        g.gridx = 0; g.weightx = 0.38; g.insets = new Insets(0, 0, 0, 24);
        body.add(buildImageColumn(), g);

        // Columna derecha: info básica + stats + descripción (todo vertical)
        g.gridx = 1; g.weightx = 0.62; g.insets = new Insets(0, 0, 0, 0);
        body.add(buildRightColumn(), g);
        return body;
    }

    // ── Columna izquierda: imagen + tipos verticales ──────────────────────────

    private JPanel buildImageColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);

        // Imagen grande
        pokemonImageLabel = new AnimatedGifLabel(300, 300);
        col.add(pokemonImageLabel, BorderLayout.CENTER);

        // Badges de tipo — ahora en panel vertical (BoxLayout Y)
        typesPanel = new JPanel();
        typesPanel.setLayout(new BoxLayout(typesPanel, BoxLayout.Y_AXIS));
        typesPanel.setOpaque(false);
        typesPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane typesScroll = new JScrollPane(typesPanel);
        typesScroll.setOpaque(false);
        typesScroll.getViewport().setOpaque(false);
        typesScroll.setBorder(null);
        typesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        typesScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        col.add(typesScroll, BorderLayout.SOUTH);
        return col;
    }

    // ── Columna derecha: info básica + stats + descripción (todo vertical) ────

    private JPanel buildRightColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // 1) Fila de info básica (altura, peso, exp)
        JPanel infoRow = new JPanel(new GridLayout(1, 3, 10, 0));
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        heightLabel = infoCard("Altura", "—");
        weightLabel = infoCard("Peso",   "—");
        expLabel    = infoCard("Exp.",   "—");
        infoRow.add(heightLabel);
        infoRow.add(weightLabel);
        infoRow.add(expLabel);
        col.add(infoRow);
        col.add(Box.createVerticalStrut(20));

        // 2) Título de estadísticas
        JLabel statsTitle = new JLabel("Estadísticas Base");
        statsTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        statsTitle.setForeground(TEXT_PRIMARY);
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        col.add(statsTitle);

        // 3) Panel de stats
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(statsPanel);
        col.add(Box.createVerticalStrut(20));

        // 4) Título de descripción
        JLabel descTitle = new JLabel("Descripción");
        descTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        descTitle.setForeground(TEXT_PRIMARY);
        descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        descTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        col.add(descTitle);

        // 5) Caja de descripción
        descriptionLabel = new JTextArea("Selecciona un Pokémon.");
        descriptionLabel.setFont(new Font("Serif", Font.ITALIC, 14));
        descriptionLabel.setForeground(new Color(195, 195, 225));
        descriptionLabel.setBackground(new Color(18, 18, 32));
        descriptionLabel.setOpaque(true);
        descriptionLabel.setEditable(false);
        descriptionLabel.setLineWrap(true);
        descriptionLabel.setWrapStyleWord(true);
        descriptionLabel.setFocusable(false);
        descriptionLabel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 14, 12, 14)));
        descriptionLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(descriptionLabel);

        // Glue para empujar contenido hacia arriba si sobra espacio
        col.add(Box.createVerticalGlue());
        return col;
    }

    // ── Tira de evoluciones ───────────────────────────────────────────────────

    private JPanel buildEvolutionStrip() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 24, 12, 24)));

        JLabel evoTitle = new JLabel("Cadena evolutiva");
        evoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        evoTitle.setForeground(TEXT_SECONDARY);
        evoTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(evoTitle, BorderLayout.NORTH);

        evolutionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        evolutionPanel.setOpaque(false);
        wrapper.add(evolutionPanel, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Status Bar ────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(5, 18, 5, 18)));
        statusLabel = new JLabel("Listo.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        bar.add(statusLabel, BorderLayout.WEST);
        JLabel credit = new JLabel("pokeapi.co");
        credit.setFont(new Font("SansSerif", Font.PLAIN, 11));
        credit.setForeground(new Color(55, 55, 85));
        bar.add(credit, BorderLayout.EAST);
        return bar;
    }

    // ── PokemonView ───────────────────────────────────────────────────────────

    @Override
    public void showPokemon(Pokemon p) {
        currentPokemonId = p.id();
        pokemonIdLabel.setText("#%03d".formatted(p.id()));
        pokemonNameLabel.setText(p.displayName());

        // Tipos: layout VERTICAL con tarjetas grandes
        typesPanel.removeAll();
        for (String t : p.types()) {
            typesPanel.add(typeBadgeVertical(t));
            typesPanel.add(Box.createVerticalStrut(8));
        }
        typesPanel.revalidate(); typesPanel.repaint();

        updateInfoCard(heightLabel, "Altura", "%.1f m".formatted(p.height() / 10.0));
        updateInfoCard(weightLabel, "Peso",   "%.1f kg".formatted(p.weight() / 10.0));
        updateInfoCard(expLabel,    "Exp.",   String.valueOf(p.baseExperience()));

        String desc = p.description();
        descriptionLabel.setText((desc != null && !desc.isBlank()) ? desc : "Sin descripción disponible.");

        // Colores del tipo para barras y halo
        Color color1 = p.types().isEmpty() ? new Color(180, 155, 95)
                : TYPE_COLORS.getOrDefault(p.types().get(0), new Color(180, 155, 95));
        Color color2 = p.types().size() > 1
                ? TYPE_COLORS.getOrDefault(p.types().get(1), color1) : color1;

        pokemonImageLabel.setTypeColors(color1, color2);

        statsPanel.removeAll();
        for (Pokemon.Stat s : p.stats()) {
            statsPanel.add(statRow(s.name(), s.baseStat(), color1, color2));
            statsPanel.add(Box.createVerticalStrut(10));
        }
        statsPanel.revalidate(); statsPanel.repaint();

        animateBg(color1);
        pokemonImageLabel.showPlaceholder("⌛");
        loadImageAsync(p.animatedGifUrl(), p.bestImageUrl());
    }

    @Override
    public void showPokemonList(List<Pokemon> pokemons) {
        loadedPokemons = new ArrayList<>(pokemons);
        listModel.clear();
        for (Pokemon p : pokemons) listModel.addElement(p);
    }

    @Override
    public void showEvolutionChain(List<Pokemon> chain, int selectedId) {
        evolutionPanel.removeAll();
        for (int i = 0; i < chain.size(); i++) {
            if (i > 0) evolutionPanel.add(arrowLabel());
            evolutionPanel.add(evoCard(chain.get(i), chain.get(i).id() == selectedId));
        }
        evolutionPanel.revalidate(); evolutionPanel.repaint();
    }

    @Override
    public void selectPokemonInList(Pokemon pokemon) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).id() == pokemon.id()) {
                pokemonJList.setSelectedIndex(i);
                pokemonJList.ensureIndexIsVisible(i);
                return;
            }
        }
        pokemonJList.clearSelection();
    }

    @Override public void showError(String msg) {
        statusLabel.setForeground(new Color(220, 80, 80)); statusLabel.setText(msg); loadingBar.setVisible(false);
    }

    @Override
    public void showNotFoundAlert(String query) {
        String msg = "<html><b>No se encontró ningún Pokémon</b><br><br>"
                + "No existe ningún Pokémon con el nombre o ID:<br>"
                + "<font color='#cc4444'>&nbsp;<b>" + query + "</b>&nbsp;</font><br><br>"
                + "<font size='3'>Verifica que el nombre esté bien escrito.</font></html>";
        JOptionPane.showMessageDialog(this, msg, "Pokémon no encontrado", JOptionPane.WARNING_MESSAGE);
        statusLabel.setForeground(new Color(220, 80, 80));
        statusLabel.setText("No se encontró: " + query);
    }

    @Override public void showStatus(String msg) { statusLabel.setForeground(TEXT_SECONDARY); statusLabel.setText(msg); }
    @Override public void setLoading(boolean l)  { loadingBar.setVisible(l); searchBtn.setEnabled(!l); }

    @Override
    public void setPaginationState(boolean hasPrev, boolean hasNext) {
        prevListBtn.setEnabled(hasPrev);
        nextListBtn.setEnabled(hasNext);
    }

    @Override
    public void clearDisplay() {
        currentPokemonId = -1;
        pokemonIdLabel.setText("#???");
        pokemonNameLabel.setText("Selecciona un Pokémon");
        typesPanel.removeAll(); typesPanel.revalidate(); typesPanel.repaint();
        statsPanel.removeAll(); statsPanel.repaint();
        pokemonImageLabel.showPlaceholder("🎮");
        pokemonImageLabel.setTypeColors(BG_CARD, BG_CARD);
        updateInfoCard(heightLabel, "Altura", "—");
        updateInfoCard(weightLabel, "Peso",   "—");
        updateInfoCard(expLabel,    "Exp.",   "—");
        descriptionLabel.setText("Selecciona un Pokémon.");
        evolutionPanel.removeAll(); evolutionPanel.revalidate(); evolutionPanel.repaint();
        animateBg(BG_DARK);
    }

    // ── Tarjeta de tipo VERTICAL (icono grande + nombre abajo) ───────────────

    /**
     * Tarjeta de tipo en formato vertical:
     *  ┌─────────────┐
     *  │    🔥  (28) │
     *  │    Fuego    │
     *  └─────────────┘
     */
    private JPanel typeBadgeVertical(String type) {
        Color bg    = TYPE_COLORS.getOrDefault(type.toLowerCase(), new Color(100, 100, 130));
        String icon = TYPE_ICONS.getOrDefault(type.toLowerCase(), "");
        String name = TYPE_NAMES.getOrDefault(type.toLowerCase(), type.toUpperCase());

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo semi-transparente del color del tipo
                Color bgAlpha = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200);
                g2.setColor(bgAlpha);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Borde más brillante
                g2.setColor(bg.brighter());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(280, 72));
        card.setPreferredSize(new Dimension(270, 68));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Icono grande
        JLabel iconLabel = new JLabel(icon.isEmpty() ? "?" : icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 28));   // ← 28pt: grande
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nombre del tipo
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fila horizontal: icono + nombre lado a lado
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.add(iconLabel);
        row.add(nameLabel);

        card.add(row);
        return card;
    }

    // ── Tarjetas de evolución ─────────────────────────────────────────────────

    private JPanel evoCard(Pokemon p, boolean selected) {
        Color typeColor = p.types().isEmpty() ? BORDER_COLOR
                : TYPE_COLORS.getOrDefault(p.types().get(0), BORDER_COLOR);
        Color cardBg = selected ? new Color(
                Math.min(255, typeColor.getRed()/2 + 18),
                Math.min(255, typeColor.getGreen()/2 + 18),
                Math.min(255, typeColor.getBlue()/2 + 18)) : BG_CARD;

        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(selected ? typeColor : BORDER_COLOR);
                g2.setStroke(new BasicStroke(selected ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(90, 102));
        card.setBorder(new EmptyBorder(6, 4, 6, 4));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel spriteLabel = new JLabel("·", SwingConstants.CENTER);
        spriteLabel.setForeground(TEXT_SECONDARY);
        loadMiniSprite(p, spriteLabel);

        JLabel nameLabel = new JLabel(p.displayName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        nameLabel.setForeground(selected ? Color.WHITE : TEXT_SECONDARY);
        JLabel idLabel = new JLabel("#%03d".formatted(p.id()), SwingConstants.CENTER);
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
        idLabel.setForeground(new Color(100, 100, 140));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);
        info.add(nameLabel); info.add(idLabel);
        card.add(spriteLabel, BorderLayout.CENTER);
        card.add(info,        BorderLayout.SOUTH);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { presenter.selectFromList(p); }
        });
        return card;
    }

    private JLabel arrowLabel() {
        JLabel l = new JLabel("›", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(new Color(80, 80, 115));
        l.setPreferredSize(new Dimension(20, 102));
        return l;
    }

    private void loadMiniSprite(Pokemon p, JLabel target) {
        imageLoader.submit(() -> {
            byte[] bytes = apiService.fetchImageBytes(p.spriteUrl());
            if (bytes != null) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (img != null) {
                        Image sc = img.getScaledInstance(52, 52, Image.SCALE_FAST);
                        SwingUtilities.invokeLater(() -> { target.setIcon(new ImageIcon(sc)); target.setText(null); });
                        return;
                    }
                } catch (Exception ignored) {}
            }
            SwingUtilities.invokeLater(() -> target.setText("?"));
        });
    }

    // ── Animación de fondo ────────────────────────────────────────────────────

    private void animateBg(Color target) {
        if (bgTimer != null) bgTimer.stop();
        bgTo = target; bgAlpha = 0f;
        bgTimer = new Timer(16, null);
        bgTimer.addActionListener(e -> {
            bgAlpha += 0.05f;
            if (bgAlpha >= 1f) { bgAlpha = 1f; bgTimer.stop(); }
            contentPanel.repaint();
        });
        bgTimer.start();
    }

    // ── Carga de imagen ───────────────────────────────────────────────────────

    private void loadImageAsync(String gifUrl, String fallback) {
        imageLoader.submit(() -> {
            byte[] bytes = apiService.fetchImageBytes(gifUrl);
            if (bytes != null && bytes.length > 0) {
                ImageIcon icon = new ImageIcon(bytes);
                SwingUtilities.invokeLater(() -> pokemonImageLabel.showGif(icon));
                return;
            }
            bytes = apiService.fetchImageBytes(fallback);
            if (bytes != null && bytes.length > 0) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (img != null) {
                        Image sc = img.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                        SwingUtilities.invokeLater(() -> pokemonImageLabel.showGif(new ImageIcon(sc)));
                        return;
                    }
                } catch (Exception ignored) {}
            }
            SwingUtilities.invokeLater(() -> pokemonImageLabel.showPlaceholder("?"));
        });
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    private void doSearch() { presenter.searchPokemon(searchField.getText()); }

    private JPanel statRow(String name, int val, Color color1, Color color2) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        String fn = switch (name) {
            case "hp" -> "HP"; case "attack" -> "Ataque"; case "defense" -> "Defensa";
            case "special-attack" -> "Sp. Ataque"; case "special-defense" -> "Sp. Defensa";
            case "speed" -> "Velocidad"; default -> name;
        };
        JLabel nl = new JLabel(fn);
        nl.setFont(new Font("SansSerif", Font.PLAIN, 13)); nl.setForeground(TEXT_SECONDARY);
        nl.setPreferredSize(new Dimension(100, 22));
        JLabel vl = new JLabel(String.valueOf(val));
        vl.setFont(new Font("SansSerif", Font.BOLD, 13)); vl.setForeground(TEXT_PRIMARY);
        vl.setPreferredSize(new Dimension(32, 22)); vl.setHorizontalAlignment(SwingConstants.RIGHT);

        JComponent bar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = getHeight(), w = getWidth(), arc = h;
                g2.setColor(new Color(28, 28, 48));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                int fillW = Math.max(arc, (int)(w * (val / 255.0)));
                GradientPaint gp = new GradientPaint(0, 0, color1.darker(), fillW, 0, color2.brighter());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, fillW, h, arc, arc);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, fillW, h / 2, arc, arc);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 12));
        row.add(nl, BorderLayout.WEST); row.add(bar, BorderLayout.CENTER); row.add(vl, BorderLayout.EAST);
        return row;
    }

    private JLabel infoCard(String label, String val) {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setBackground(new Color(18, 18, 32)); l.setOpaque(true);
        l.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(9, 4, 9, 4)));
        updateInfoCard(l, label, val); return l;
    }

    private void updateInfoCard(JLabel l, String label, String val) {
        l.setText("<html><center><font color='#9191b4' size='2'>" + label
                + "</font><br><b><font color='#f0f0ff' size='3'>" + val + "</font></b></center></html>");
    }

    private JTextField styledField(String ph) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    ((Graphics2D)g).setColor(new Color(95, 95, 125));
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(ph, 8, getHeight()/2+5);
                }
            }
        };
        tf.setBackground(new Color(28, 28, 48)); tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY); tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isEnabled()
                        ? (getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg)
                        : new Color(40, 40, 60);
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE); btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36)); return btn;
    }

    private JButton arrowNavBtn(String arrow) {
        Color bg = new Color(38, 38, 68);
        JButton btn = new JButton(arrow) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? new Color(65, 65, 115) : bg;
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(90, 90, 150)); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 22));
        btn.setForeground(new Color(195, 195, 240));
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(50, 40));
        return btn;
    }

    private Icon circleIcon(Color c, int s) {
        return new Icon() {
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillOval(x, y, s, s); g2.dispose();
            }
            public int getIconWidth()  { return s; }
            public int getIconHeight() { return s; }
        };
    }

    // ── List cell renderer ────────────────────────────────────────────────────

    private class PokeListRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(
                JList<?> list, Object val, int idx, boolean sel, boolean focus) {
            Pokemon p = (Pokemon) val;
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, val, idx, sel, focus);
            lbl.setText("  #%03d  %s".formatted(p.id(), p.displayName()));
            lbl.setBorder(new EmptyBorder(4, 6, 4, 6));
            lbl.setBackground(sel ? new Color(52, 52, 98) : (idx % 2 == 0 ? BG_LIST : new Color(20, 20, 35)));
            lbl.setForeground(sel ? ACCENT_GOLD : TEXT_PRIMARY);
            return lbl;
        }
    }

    // ── AnimatedGifLabel con halo de tipo ─────────────────────────────────────

    private static class AnimatedGifLabel extends JComponent {
        private Image  image;
        private String placeholder = "🎮";
        private Color  haloColor1  = new Color(24, 24, 40);
        private Color  haloColor2  = new Color(24, 24, 40);

        AnimatedGifLabel(int w, int h) {
            setPreferredSize(new Dimension(w, h));
            setMinimumSize(new Dimension(w, h));
            setMaximumSize(new Dimension(w, h));
            setOpaque(false);
        }

        void setTypeColors(Color c1, Color c2) {
            // Ya no se usa halo — fondo transparente
        }

        void showGif(ImageIcon icon) {
            image = icon.getImage(); placeholder = null;
            icon.setImageObserver(this); repaint();
        }

        void showPlaceholder(String t) { image = null; placeholder = t; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();

            // Fondo completamente transparente — se ve el tint del panel padre
            // (sin fillRect, sin halo, sin borde)

            if (image != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int margin = 8;
                int sz = Math.min(w, h) - margin * 2;
                g2.drawImage(image, (w - sz) / 2, (h - sz) / 2, sz, sz, this);
            } else if (placeholder != null) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 52));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(120, 120, 150, 180));
                g2.drawString(placeholder, (w - fm.stringWidth(placeholder)) / 2, (h + fm.getAscent()) / 2);
            }
            g2.dispose();
        }
    }
}