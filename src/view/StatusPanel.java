package view;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class StatusPanel extends JPanel {

    private Game game;

    private static final Color BG           = new Color(12, 10, 6);
    private static final Color PANEL_BG     = new Color(22, 18, 10);
    private static final Color BORDER_COLOR = new Color(80, 65, 30);
    private static final Color TEXT_GOLD    = new Color(200, 165, 70);
    private static final Color TEXT_DIM     = new Color(120, 100, 60);
    private static final Color P1_COLOR     = new Color(220, 80, 60);
    private static final Color P2_COLOR     = new Color(60, 140, 220);
    private static final Color MSG_BG       = new Color(30, 25, 12);

    private Font titleFont;
    private Font bodyFont;
    private Font monoFont;

    public StatusPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(220, 520));
        setBackground(BG);

        try {
            titleFont = new Font("Georgia", Font.BOLD, 18);
            bodyFont  = new Font("Georgia", Font.PLAIN, 13);
            monoFont  = new Font("Courier New", Font.BOLD, 12);
        } catch (Exception e) {
            titleFont = new Font("Serif", Font.BOLD, 18);
            bodyFont  = new Font("Serif", Font.PLAIN, 13);
            monoFont  = new Font("Monospaced", Font.BOLD, 12);
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();

        // Background
        g.setColor(BG);
        g.fillRect(0, 0, w, getHeight());

        // Left border line
        GradientPaint gp = new GradientPaint(0,0,BORDER_COLOR.darker(),0,getHeight(),BORDER_COLOR);
        g.setPaint(gp);
        g.fillRect(0, 0, 2, getHeight());

        int y = 30;

        // Title
        g.setFont(titleFont);
        g.setColor(TEXT_GOLD);
        drawCentered(g, "NINE MEN'S", w, y); y += 24;
        drawCentered(g, "MORRIS", w, y); y += 40;

        // Decorative line
        drawDivider(g, w, y); y += 20;

        // Player 1 Card
        y = drawPlayerCard(g, game.getPlayer1(), w, y);
        y += 12;

        // Player 2 Card
        y = drawPlayerCard(g, game.getPlayer2(), w, y);
        y += 12;

        // Divider
        drawDivider(g, w, y); y += 20;

        // Game State
        g.setFont(monoFont);
        g.setColor(TEXT_DIM);
        String stateStr = stateLabel(game.getState());
        drawCentered(g, "[ " + stateStr + " ]", w, y); y += 30;

        // Message box
        y = drawMessageBox(g, game.getMessage(), w, y);

        // Win overlay
        if (game.getState() == GameState.GAME_OVER && game.getWinner() != null) {
            drawWinBanner(g, game.getWinner(), w);
        }
    }

    private int drawPlayerCard(Graphics2D g, Player player, int w, int y) {
        boolean active = (game.getCurrentPlayer() == player)
                         && game.getState() != GameState.GAME_OVER;
        Color pColor = (player.getId() == 1) ? P1_COLOR : P2_COLOR;

        int cardX = 10, cardW = w - 20, cardH = 80;

        // Card bg
        g.setColor(active ? new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), 20) : PANEL_BG);
        g.fillRoundRect(cardX, y, cardW, cardH, 10, 10);

        // Card border
        g.setColor(active ? pColor : BORDER_COLOR);
        g.setStroke(new BasicStroke(active ? 2f : 1f));
        g.drawRoundRect(cardX, y, cardW, cardH, 10, 10);

        // Active indicator
        if (active) {
            g.setColor(pColor);
            g.fillOval(cardX + cardW - 16, y + 8, 10, 10);
            // Pulse ring
            g.setColor(new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), 80));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(cardX + cardW - 18, y + 6, 14, 14);
        }

        // Player name
        g.setFont(bodyFont.deriveFont(Font.BOLD, 14f));
        g.setColor(active ? Color.WHITE : TEXT_DIM);
        g.drawString(player.getName(), cardX + 12, y + 22);

        // Piece circles - to place
        int cx = cardX + 12;
        int cy2 = y + 42;
        for (int i = 0; i < 9; i++) {
            boolean placed = i < (9 - player.getPiecesToPlace());
            boolean onBoard = i < player.getPiecesOnBoard();
            if (placed && onBoard) {
                g.setColor(pColor);
                g.fillOval(cx, cy2, 14, 14);
                g.setColor(pColor.brighter());
                g.setStroke(new BasicStroke(1f));
                g.drawOval(cx, cy2, 14, 14);
            } else if (placed) {
                // removed piece
                g.setColor(new Color(60,20,20));
                g.fillOval(cx, cy2, 14, 14);
            } else {
                g.setColor(new Color(pColor.getRed(),pColor.getGreen(),pColor.getBlue(),60));
                g.fillOval(cx, cy2, 14, 14);
            }
            cx += 18;
            if (i == 4) { cx = cardX + 12; cy2 += 18; }
        }

        // Counts
        g.setFont(monoFont);
        g.setColor(TEXT_DIM);
        g.drawString("Tabela: " + player.getPiecesOnBoard(), cardX + 12, y + 72);
        g.drawString("Rezervë: " + player.getPiecesToPlace(), cardX + 100, y + 72);

        return y + cardH + 4;
    }

    private int drawMessageBox(Graphics2D g, String msg, int w, int y) {
        int margin = 10;
        int boxW = w - margin * 2;

        // Wrap text
        String[] words = msg.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        FontMetrics fm = g.getFontMetrics(bodyFont);
        for (String word : words) {
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(test) > boxW - 20) {
                if (cur.length() > 0) lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());

        int lineH = 18;
        int boxH = lines.size() * lineH + 20;

        g.setColor(MSG_BG);
        g.fillRoundRect(margin, y, boxW, boxH, 8, 8);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(margin, y, boxW, boxH, 8, 8);

        g.setFont(bodyFont);
        g.setColor(TEXT_GOLD);
        int ty = y + 16;
        for (String line : lines) {
            g.drawString(line, margin + 10, ty);
            ty += lineH;
        }

        return y + boxH + 10;
    }

    private void drawWinBanner(Graphics2D g, Player winner, int w) {
        Color pColor = (winner.getId() == 1) ? P1_COLOR : P2_COLOR;
        int by = getHeight() - 70;
        g.setColor(new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), 40));
        g.fillRoundRect(10, by, w-20, 55, 10, 10);
        g.setColor(pColor);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(10, by, w-20, 55, 10, 10);
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        drawCentered(g, "🏆 FITOI!", w, by + 28);
        g.setFont(bodyFont);
        g.setColor(pColor);
        drawCentered(g, winner.getName(), w, by + 48);
    }

    private void drawDivider(Graphics2D g, int w, int y) {
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(20, y, w-20, y);
    }

    private void drawCentered(Graphics2D g, String text, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private String stateLabel(GameState state) {
        return switch (state) {
            case PLACING  -> "VENDOSJA";
            case MOVING   -> "LËVIZJA";
            case FLYING   -> "FLUTURIMI";
            case REMOVING -> "HIQ GURIN";
            case GAME_OVER -> "LOJA MBAROI";
        };
    }
}
