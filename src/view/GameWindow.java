package view;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame {

    private Game game;
    private BoardPanel boardPanel;
    private StatusPanel statusPanel;
    private JButton resetBtn;
    private JButton modeBtn;

    private static final Color BG         = new Color(12, 10, 6);
    private static final Color GOLD       = new Color(200, 165, 70);
    private static final Color BTN_BG     = new Color(35, 28, 14);
    private static final Color BTN_BORDER = new Color(100, 80, 35);
    private static final Color AI_COLOR   = new Color(60, 200, 120);

    // Timer used to let AI make consecutive moves (e.g. mill -> remove)
    private Timer aiTimer;

    public GameWindow() {
        game = new Game();
        setTitle("Nine Men's Morris — Mühle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        askGameMode();
    }

    // ─── Mode dialog ────────────────────────────────────────────────────────

    private void askGameMode() {
        // Custom panel
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 12));
        panel.setBackground(new Color(22, 18, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel lbl = new JLabel("Zgjidh mënyrën e lojës:", SwingConstants.CENTER);
        lbl.setForeground(GOLD);
        lbl.setFont(new Font("Georgia", Font.BOLD, 15));

        JButton pvpBtn = makeModeButton("👥  Lojtarë vs Lojtarë", BTN_BG);
        JButton aiBtn  = makeModeButton("🤖  Kundër Kompjuterit", new Color(20, 40, 25));

        panel.add(lbl);
        panel.add(pvpBtn);
        panel.add(aiBtn);

        JDialog dialog = new JDialog(this, "Zgjidhni mënyrën", true);
        dialog.getContentPane().setBackground(new Color(22, 18, 10));
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(320, 180));
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        pvpBtn.addActionListener(e -> {
            game.setVsComputer(false);
            updateModeButton();
            refresh();
            dialog.dispose();
        });
        aiBtn.addActionListener(e -> {
            game.setVsComputer(true);
            updateModeButton();
            refresh();
            dialog.dispose();
            scheduleAiMove();
        });

        dialog.setVisible(true);
    }

    private JButton makeModeButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(BTN_BORDER);
                g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g.setColor(GOLD);
                g.setFont(new Font("Georgia", Font.PLAIN, 14));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                             (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setPreferredSize(new Dimension(260, 42));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── UI build ────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        boardPanel  = new BoardPanel(game, this::onBoardClick);
        statusPanel = new StatusPanel(game);

        JPanel gap = new JPanel();
        gap.setOpaque(false);
        gap.setPreferredSize(new Dimension(10, 0));

        root.add(boardPanel,  BorderLayout.CENTER);
        root.add(statusPanel, BorderLayout.EAST);
        root.add(gap,         BorderLayout.WEST);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bar.setBackground(BG);

        resetBtn = createStyledButton("↺  Fillo Sërish");
        resetBtn.addActionListener(e -> {
            game.reset();
            refresh();
            if (game.isVsComputer()) scheduleAiMove();
        });

        modeBtn = createStyledButton("👥  PvP");
        modeBtn.addActionListener(e -> askGameMode());

        JButton helpBtn = createStyledButton("?  Rregullat");
        helpBtn.addActionListener(e -> showHelp());

        bar.add(resetBtn);
        bar.add(modeBtn);
        bar.add(helpBtn);
        return bar;
    }

    private void updateModeButton() {
        if (game.isVsComputer()) {
            modeBtn.putClientProperty("label", "🤖  vs AI");
        } else {
            modeBtn.putClientProperty("label", "👥  PvP");
        }
        modeBtn.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g.setColor(BTN_BORDER);
                else if (getModel().isRollover()) g.setColor(new Color(55, 45, 20));
                else g.setColor(BTN_BG);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g.setColor(BTN_BORDER);
                g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g.setColor(GOLD);
                g.setFont(new Font("Georgia", Font.PLAIN, 13));
                Object lbl = getClientProperty("label");
                String display = (lbl != null) ? lbl.toString() : getText();
                FontMetrics fm = g.getFontMetrics();
                g.drawString(display, (getWidth()-fm.stringWidth(display))/2,
                             (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setPreferredSize(new Dimension(150, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Board click / AI scheduling ────────────────────────────────────────

    private void onBoardClick(int pos) {
        boolean changed = game.handleClick(pos);
        refresh();
        if (changed && game.isVsComputer()) {
            scheduleAiMove();
        }
    }

    /**
     * Schedules an AI move with a short delay so the board repaints first,
     * giving visual feedback. After the AI acts it schedules again in case
     * it formed a mill and needs to remove a piece.
     */
    private void scheduleAiMove() {
        if (aiTimer != null && aiTimer.isRunning()) aiTimer.stop();
        aiTimer = new Timer(350, e -> {
            ((Timer) e.getSource()).stop();
            boolean acted = game.triggerAiMove();
            refresh();
            if (acted) {
                // AI may need another move (e.g. after forming a mill it must remove)
                scheduleAiMove();
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void refresh() {
        boardPanel.repaint();
        statusPanel.repaint();
    }

    // ─── Help dialog ─────────────────────────────────────────────────────────

    private void showHelp() {
        String msg =
            "NINE MEN'S MORRIS — RREGULLAT\n\n" +
            "FAZA 1 — VENDOSJA:\n" +
            "  Çdo lojtarë vendos 9 gurë nga radha.\n" +
            "  Nëse bën rresht me 3 gurë (mulli),\n" +
            "  hiq 1 gur të kundërshtarit.\n\n" +
            "FAZA 2 — LËVIZJA:\n" +
            "  Lëviz gurët te fqinjët e lirë.\n" +
            "  Klikoje gurin, pastaj destinacionin.\n\n" +
            "FAZA 3 — FLUTURIMI:\n" +
            "  Kur ke 3 gurë, mund të shkosh kudo!\n\n" +
            "FITORJA:\n" +
            "  Armiku ka < 3 gurë, ose nuk mund të lëvizë.\n\n" +
            "KUJDES:\n" +
            "  Nuk mund të heqësh gurin e armikut\n" +
            "  që është brenda mullirit (përveç\n" +
            "  nëse të gjithë janë në mulli).";

        JOptionPane optPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optPane.createDialog(this, "Rregullat e Lojës");
        dialog.getContentPane().setBackground(new Color(22, 18, 10));
        dialog.setVisible(true);
    }
}
