import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.sound.sampled.*;
import javax.swing.*;

public class MemoryGame extends JFrame implements ActionListener {

    int gridSize = 4, totalCards;
    JButton[] cards;
    ImageIcon[] cardIcons, hiddenIconArr;
    ImageIcon hiddenIcon;
    JButton firstCard, secondCard;
    int firstPos, secondPos;
    javax.swing.Timer hideTimer, timeCounter;
    boolean busy = false, started = false;
    int moveCount = 0, timeTaken = 0;
    JLabel infoLabel;
    JComboBox<String> levelSelect, modeSelect;
    JButton restartBtn, themeBtn, soundBtn;
    JPanel boardPanel, topPanel;
    boolean darkTheme = true, soundOn = true, imageMode = true;

    public MemoryGame() {
        setTitle("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        createHeader();
        createBoard();

        hideTimer = new javax.swing.Timer(700, e -> hideCards());
        hideTimer.setRepeats(false);

        timeCounter = new javax.swing.Timer(1000, e -> {
            timeTaken++;
            updateLabel();
        });

        applyTheme();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void playSound(String fileName) {
        if (!soundOn) return;
        try {
            File f = new File("sounds/" + fileName);
            AudioInputStream a = AudioSystem.getAudioInputStream(f);
            Clip c = AudioSystem.getClip();
            c.open(a);
            c.start();
        } catch (Exception ignored) {}
    }

    void createHeader() {
        topPanel = new JPanel();

        infoLabel = new JLabel("Moves: 0 | Time: 0s");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));

        levelSelect = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        levelSelect.setBackground(Color.GREEN);
        levelSelect.addActionListener(e -> {
            String lvl = (String) levelSelect.getSelectedItem();
            if (lvl.equals("Easy")) levelSelect.setBackground(Color.GREEN);
            else if (lvl.equals("Medium")) levelSelect.setBackground(Color.YELLOW);
            else levelSelect.setBackground(Color.RED);
            changeLevel();
        });

        modeSelect = new JComboBox<>(new String[]{"Images", "Numbers"});
        modeSelect.setBackground(new Color(200, 200, 255));
        modeSelect.addActionListener(e -> {
            imageMode = modeSelect.getSelectedItem().equals("Images");
            restartGame();
        });

        themeBtn = new JButton("Theme");
        themeBtn.setBackground(new Color(155, 89, 182));
        themeBtn.setForeground(Color.WHITE);
        themeBtn.addActionListener(e -> toggleTheme());

        soundBtn = new JButton("🔊");
        soundBtn.setBackground(new Color(243, 156, 18));
        soundBtn.addActionListener(e -> {
            soundOn = !soundOn;
            soundBtn.setText(soundOn ? "🔊" : "🔇");
        });

        restartBtn = new JButton("Restart");
        restartBtn.setBackground(new Color(52, 152, 219));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.addActionListener(e -> restartGame());

        topPanel.add(infoLabel);
        topPanel.add(levelSelect);
        topPanel.add(modeSelect);
        topPanel.add(themeBtn);
        topPanel.add(soundBtn);
        topPanel.add(restartBtn);

        add(topPanel, BorderLayout.NORTH);
    }

    void createBoard() {
        if (boardPanel != null) remove(boardPanel);

        totalCards = gridSize * gridSize;
        cards = new JButton[totalCards];
        cardIcons = new ImageIcon[totalCards];
        generateCards();

        boardPanel = new JPanel(new GridLayout(gridSize, gridSize, 8, 8));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < totalCards; i++) {
            cards[i] = new JButton(hiddenIcon);
            cards[i].setFocusPainted(false);
            cards[i].addActionListener(this);
            boardPanel.add(cards[i]);
        }

        add(boardPanel, BorderLayout.CENTER);
        applyTheme();
        pack();
    }

    void generateCards() {
        int size = (gridSize <= 4) ? 80 : (gridSize <= 6) ? 70 : 60;
        hiddenIcon = createBackIcon(size);

        ArrayList<ImageIcon> temp = new ArrayList<>();
        int pairs = totalCards / 2;

        if (imageMode) {
            for (int i = 1; i <= pairs; i++) {
                URL u = getClass().getResource("/images/img" + i + ".png");
                ImageIcon icon = new ImageIcon(new ImageIcon(u).getImage()
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH));
                temp.add(icon);
                temp.add(icon);
            }
        } else {
            for (int i = 1; i <= pairs; i++) {
                ImageIcon icon = createNumberIcon(size, i);
                temp.add(icon);
                temp.add(icon);
            }
        }

        Collections.shuffle(temp);
        for (int i = 0; i < totalCards; i++) cardIcons[i] = temp.get(i);
    }

    ImageIcon createNumberIcon(int s, int n) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(46, 204, 113));
        g.fillRoundRect(0, 0, s, s, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, s / 2));
        g.drawString("" + n, s / 2 - 10, s / 2 + 15);
        g.dispose();
        return new ImageIcon(img);
    }

    ImageIcon createBackIcon(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(0, 0, s, s, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, s / 2));
        g.drawString("?", s / 2 - 8, s / 2 + 10);
        g.dispose();
        return new ImageIcon(img);
    }

    public void actionPerformed(ActionEvent e) {
        if (busy) return;

        if (!started) {
            timeCounter.start();
            started = true;
        }

        JButton clicked = (JButton) e.getSource();
        int pos = Arrays.asList(cards).indexOf(clicked);

        if (clicked.getIcon() != hiddenIcon) return;

        clicked.setIcon(cardIcons[pos]);
        playSound("flip.wav");

        if (firstCard == null) {
            firstCard = clicked;
            firstPos = pos;
        } else {
            secondCard = clicked;
            secondPos = pos;
            moveCount++;
            updateLabel();
            busy = true;

            if (cardIcons[firstPos] == cardIcons[secondPos]) {
                playSound("match.wav");
                firstCard.setEnabled(false);
                secondCard.setEnabled(false);
                resetSelection();
                busy = false;
                checkFinished();
            } else {
                playSound("wrong.wav");
                hideTimer.start();
            }
        }
    }

    void hideCards() {
        firstCard.setIcon(hiddenIcon);
        secondCard.setIcon(hiddenIcon);
        resetSelection();
        busy = false;
    }

    void resetSelection() {
        firstCard = null;
        secondCard = null;
    }

    void updateLabel() {
        infoLabel.setText("Moves: " + moveCount + " | Time: " + timeTaken + "s");
    }

    void checkFinished() {
        for (JButton b : cards)
            if (b.isEnabled()) return;

        timeCounter.stop();
        playSound("win.wav");
        JOptionPane.showMessageDialog(this,
                "Completed!\nMoves: " + moveCount + "\nTime: " + timeTaken + " seconds");
    }

    void toggleTheme() {
        darkTheme = !darkTheme;
        createBoard();
    }

    void applyTheme() {
        Color bg = darkTheme ? new Color(30, 30, 30) : Color.WHITE;
        Color fg = darkTheme ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bg);
        topPanel.setBackground(bg);
        boardPanel.setBackground(bg);
        infoLabel.setForeground(fg);
    }

    void restartGame() {
        moveCount = 0;
        timeTaken = 0;
        started = false;
        timeCounter.stop();
        createBoard();
        updateLabel();
    }

    void changeLevel() {
        String lvl = (String) levelSelect.getSelectedItem();
        gridSize = lvl.equals("Easy") ? 4 : lvl.equals("Medium") ? 6 : 8;
        restartGame();
    }

    public static void main(String[] args) {
        new MemoryGame();
    }
}
