package view;

import model.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class BoardPanel extends javax.swing.JPanel {

    // 24 positions as (x,y) fractions of panel size [0..1]
    private static final double[][] POSITIONS = {
        {0.05, 0.05}, {0.50, 0.05}, {0.95, 0.05},  // 0,1,2
        {0.18, 0.18}, {0.50, 0.18}, {0.82, 0.18},  // 3,4,5
        {0.31, 0.31}, {0.50, 0.31}, {0.69, 0.31},  // 6,7,8
        {0.05, 0.50}, {0.18, 0.50}, {0.31, 0.50},  // 9,10,11
        {0.69, 0.50}, {0.82, 0.50}, {0.95, 0.50},  // 12,13,14
        {0.31, 0.69}, {0.50, 0.69}, {0.69, 0.69},  // 15,16,17
        {0.18, 0.82}, {0.50, 0.82}, {0.82, 0.82},  // 18,19,20
        {0.05, 0.95}, {0.50, 0.95}, {0.95, 0.95}   // 21,22,23
    };

    // Lines to draw (pairs of positions)
    private static final int[][] LINES = {
        {0,1},{1,2},{3,4},{4,5},{6,7},{7,8},
        {9,10},{10,11},{12,13},{13,14},
        {15,16},{16,17},{18,19},{19,20},{21,22},{22,23},
        {0,9},{9,21},{3,10},{10,18},{6,11},{11,15},
        {1,4},{4,7},{16,19},{19,22},
        {2,14},{14,23},{5,13},{13,20},{8,12},{12,17}
    };

    private Game game;
    private Consumer<Integer> clickHandler;
    private int hoveredPos = -1;
    private static final int PIECE_RADIUS = 20;
    private static final int HIT_RADIUS = 26;

    // Colors
    private static final Color BG_COLOR       = new Color(15, 12, 8);
    private static final Color BOARD_COLOR     = new Color(28, 22, 14);
    private static final Color LINE_COLOR      = new Color(100, 80, 40);
    private static final Color NODE_EMPTY      = new Color(55, 45, 25);
    private static final Color NODE_HOVER      = new Color(200, 170, 80, 180);
    private static final Color SELECTED_GLOW   = new Color(255, 220, 50);
    private static final Color P1_COLOR        = new Color(220, 80, 60);
    private static final Color P2_COLOR        = new Color(60, 140, 220);
    private static final Color P1_BRIGHT       = new Color(255, 130, 110);
    private static final Color P2_BRIGHT       = new Color(110, 190, 255);
    private static final Color VALID_MOVE      = new Color(100, 200, 100, 120);

    public BoardPanel(Game game, Consumer<Integer> clickHandler) {
        this.game = game;
        this.clickHandler = clickHandler;
        setPreferredSize(new Dimension(520, 520));
        setBackground(BG_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int pos = getPosAt(e.getX(), e.getY());
                if (pos != -1) clickHandler.accept(pos);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int pos = getPosAt(e.getX(), e.getY());
                if (pos != hoveredPos) { hoveredPos = pos; repaint(); }
            }
        });
    }

    private int[] getPixel(int pos) {
        int w = getWidth(), h = getHeight();
        int margin = 30;
        int size = Math.min(w, h) - margin * 2;
        int ox = (w - size) / 2;
        int oy = (h - size) / 2;
        return new int[]{
            (int)(ox + POSITIONS[pos][0] * size),
            (int)(oy + POSITIONS[pos][1] * size)
        };
    }

    private int getPosAt(int mx, int my) {
        for (int i = 0; i < Board.SIZE; i++) {
            int[] p = getPixel(i);
            int dx = mx - p[0], dy = my - p[1];
            if (dx*dx + dy*dy <= HIT_RADIUS*HIT_RADIUS) return i;
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g);
        drawLines(g);
        drawNodes(g);
    }

    private void drawBackground(Graphics2D g) {
        // Radial gradient background
        int w = getWidth(), h = getHeight();
        RadialGradientPaint rg = new RadialGradientPaint(
            w/2f, h/2f, Math.max(w,h)/1.5f,
            new float[]{0f, 1f},
            new Color[]{new Color(30, 24, 12), new Color(8, 6, 3)}
        );
        g.setPaint(rg);
        g.fillRect(0, 0, w, h);

        // Subtle grid texture
        g.setColor(new Color(255,255,255,4));
        g.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < w; i += 20) g.drawLine(i, 0, i, h);
        for (int j = 0; j < h; j += 20) g.drawLine(0, j, w, j);
    }

    private void drawLines(Graphics2D g) {
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int[] line : LINES) {
            int[] p1 = getPixel(line[0]);
            int[] p2 = getPixel(line[1]);
            // Glow
            g.setColor(new Color(100, 80, 40, 30));
            g.setStroke(new BasicStroke(6f));
            g.drawLine(p1[0], p1[1], p2[0], p2[1]);
            // Line
            g.setColor(LINE_COLOR);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }
    }

    private void drawNodes(Graphics2D g) {
        Board board = game.getBoard();
        int selected = game.getSelectedPos();
        GameState state = game.getState();
        int currentId = game.getCurrentPlayer().getId();

        for (int i = 0; i < Board.SIZE; i++) {
            int[] p = getPixel(i);
            int cell = board.getCell(i);
            boolean isSelected = (i == selected);
            boolean isHovered = (i == hoveredPos);

            // Valid move highlight
            if (selected != -1 && cell == 0 &&
                (state == GameState.FLYING || board.areNeighbors(selected, i))) {
                g.setColor(VALID_MOVE);
                g.fillOval(p[0]-PIECE_RADIUS, p[1]-PIECE_RADIUS, PIECE_RADIUS*2, PIECE_RADIUS*2);
            }

            if (cell == 0) {
                // Empty node
                if (isHovered && state == GameState.PLACING) {
                    // Show ghost piece
                    Color ghostColor = (currentId == 1) ? new Color(220,80,60,80) : new Color(60,140,220,80);
                    g.setColor(ghostColor);
                    g.fillOval(p[0]-PIECE_RADIUS, p[1]-PIECE_RADIUS, PIECE_RADIUS*2, PIECE_RADIUS*2);
                }
                // Node dot
                g.setColor(isHovered ? NODE_HOVER : NODE_EMPTY);
                int r = isHovered ? 8 : 5;
                g.fillOval(p[0]-r, p[1]-r, r*2, r*2);
                g.setColor(LINE_COLOR);
                g.setStroke(new BasicStroke(1f));
                g.drawOval(p[0]-r, p[1]-r, r*2, r*2);
            } else {
                Color base = (cell == 1) ? P1_COLOR : P2_COLOR;
                Color bright = (cell == 1) ? P1_BRIGHT : P2_BRIGHT;

                // Selected glow
                if (isSelected) {
                    for (int glow = 20; glow > 0; glow -= 4) {
                        g.setColor(new Color(SELECTED_GLOW.getRed(), SELECTED_GLOW.getGreen(),
                                             SELECTED_GLOW.getBlue(), 10));
                        int gr = PIECE_RADIUS + glow;
                        g.fillOval(p[0]-gr, p[1]-gr, gr*2, gr*2);
                    }
                    g.setColor(SELECTED_GLOW);
                    g.setStroke(new BasicStroke(2.5f));
                    g.drawOval(p[0]-PIECE_RADIUS-5, p[1]-PIECE_RADIUS-5,
                               (PIECE_RADIUS+5)*2, (PIECE_RADIUS+5)*2);
                }

                // Piece shadow
                g.setColor(new Color(0,0,0,80));
                g.fillOval(p[0]-PIECE_RADIUS+3, p[1]-PIECE_RADIUS+3, PIECE_RADIUS*2, PIECE_RADIUS*2);

                // Piece body gradient
                RadialGradientPaint rg = new RadialGradientPaint(
                    p[0]-PIECE_RADIUS/3f, p[1]-PIECE_RADIUS/3f, PIECE_RADIUS*1.5f,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{bright, base, base.darker()}
                );
                g.setPaint(rg);
                g.fillOval(p[0]-PIECE_RADIUS, p[1]-PIECE_RADIUS, PIECE_RADIUS*2, PIECE_RADIUS*2);

                // Piece border
                g.setColor(isSelected ? SELECTED_GLOW : base.brighter());
                g.setStroke(new BasicStroke(isSelected ? 2.5f : 1.5f));
                g.drawOval(p[0]-PIECE_RADIUS, p[1]-PIECE_RADIUS, PIECE_RADIUS*2, PIECE_RADIUS*2);

                // Shine
                g.setColor(new Color(255,255,255,60));
                g.fillOval(p[0]-PIECE_RADIUS/2, p[1]-PIECE_RADIUS+3, PIECE_RADIUS/2, PIECE_RADIUS/3);
            }
        }
    }
}
