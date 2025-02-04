import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.sound.sampled.*;

import java.net.URISyntaxException;

public class WordleGUI extends JFrame {
    private static final int MAX_ATTEMPTS = 6;
    private static final int CELL_SIZE = 100; // Fixed square size for each cell
    private int currentAttempt = 0;
    private int currentWordIndex = 0; // To keep track of the current word
    private List<String> wordList = new ArrayList<>();
    private JPanel gridPanel;
    private JTextField inputField;
    private JButton submitButton;
    private JButton hintButton;  // Hint button
    private JLabel[][] gridCells = new JLabel[MAX_ATTEMPTS][5];
    private String SECRET_WORD;

    public WordleGUI() {
        loadWordListFromFile("wordlist.txt"); // Load word list from file
        if (wordList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: No words loaded. Please check the wordlist.txt file.");
            System.exit(1);
        }

        // Initialize the first word in the list
        SECRET_WORD = wordList.get(currentWordIndex);
        
        setTitle("Bible Wordle");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 200));

        // Load background image for main game window
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/ocean.gif"));
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout(10, 10));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Bible Wordle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(237, 245, 20)); // Gold color
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        // Add shadow effect
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 5, 0, new Color(9, 230, 35)), // Bottom shadow
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        titlePanel.add(titleLabel);
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        // Grid panel with FlowLayout to prevent stretching
        JPanel gridContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        gridContainer.setOpaque(false);
        gridPanel = new JPanel(new GridLayout(MAX_ATTEMPTS, 5, 5, 5));
        gridPanel.setOpaque(false);

        // Border to create a shadow effect for cells
        Border outerBorder = new MatteBorder(3, 3, 3, 3, new Color(0, 0, 0, 0));
        Border innerBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);
        Border shadowBorder = new CompoundBorder(outerBorder, innerBorder);

        // Creating the grid
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            for (int j = 0; j < 5; j++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                cell.setFont(new Font("SansSerif", Font.BOLD, 24));
                cell.setBorder(shadowBorder);
                cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE)); // Force square size
                gridCells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        gridContainer.add(gridPanel);
        backgroundPanel.add(gridContainer, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputField = new JTextField(10);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        inputField.setBackground(new Color(240, 240, 240)); // Light gray background
        inputField.setForeground(Color.BLACK); // Black text
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        inputField.setCaretColor(Color.BLUE); // Cursor color

        submitButton = new JButton("Submit Guess");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        submitButton.setBackground(new Color(25, 99, 70));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 3),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Hint Button
        hintButton = new JButton("Get Hint");
        hintButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        hintButton.setBackground(new Color(85, 200, 100));  // Light green
        hintButton.setForeground(Color.WHITE);
        hintButton.setFocusPainted(false);
        hintButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 3),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Action for the Hint button
        hintButton.addActionListener(e -> giveHint());

        // New Game Button
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        newGameButton.setBackground(new Color(50, 150, 50));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 3),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        inputPanel.add(inputField);
        inputPanel.add(submitButton);
        inputPanel.add(hintButton); // Add Hint button
        inputPanel.add(newGameButton);

        backgroundPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(backgroundPanel);

        // Button listeners
        submitButton.addActionListener(e -> submitGuess());
        inputField.addActionListener(e -> submitGuess());

        // New Game Button listener
        newGameButton.addActionListener(e -> startNewGame());
    }

    // Method to load the word list from a file
    private void loadWordListFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to provide a hint
    private void giveHint() {
        if (currentAttempt < MAX_ATTEMPTS) {
            for (int i = 0; i < 5; i++) {
                char letter = SECRET_WORD.charAt(i);
                // Find the first correct letter in the secret word and display it
                if (letter == SECRET_WORD.charAt(i)) {
                    JLabel cell = gridCells[currentAttempt][i];
                    cell.setText(String.valueOf(letter).toUpperCase());
                    cell.setBackground(new Color(106, 170, 100)); // Green for correct letters
                    break; // Only display one correct letter for the hint
                }
            }
        }
    }

    private void submitGuess() {
        String guess = inputField.getText().toLowerCase();
        if (guess.length() != 5) {
            JOptionPane.showMessageDialog(this, "Please enter a 5-letter word.");
            return;
        }

        for (int i = 0; i < 5; i++) {
            char letter = guess.charAt(i);
            JLabel cell = gridCells[currentAttempt][i];
            cell.setText(String.valueOf(letter).toUpperCase());

            if (letter == SECRET_WORD.charAt(i)) {
                cell.setBackground(new Color(106, 170, 100)); // Green
            } else if (SECRET_WORD.indexOf(letter) != -1) {
                cell.setBackground(new Color(201, 180, 88)); // Yellow
            } else {
                cell.setBackground(new Color(120, 124, 126)); // Gray
            }
        }

        if (guess.equals(SECRET_WORD)) {
            playWinSound();
            showGameOverScreen("🎉 Congratulations! You guessed the word!");
            endGame();
            return;
        }

        currentAttempt++;
        if (currentAttempt >= MAX_ATTEMPTS) {
            playGameOverSound();
            showGameOverScreen("😢 Game Over! The correct word was: " + SECRET_WORD);
            endGame();
        }

        inputField.setText("");
    }


  private void showGameOverScreen(String message) {
    // Play the Game Over sound
    

    // Create a modal dialog relative to the main frame
    JDialog dialog = new JDialog(this, "Result", true);
    dialog.setSize(350, 200);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    
    // Create a panel to hold everything inside the dialog
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(10, 10));
    
    // Set a background color for the dialog panel
    panel.setBackground(new Color(50, 50, 50));  // Dark gray background for the dialog
    
    // Create a message label
    JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
    messageLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    messageLabel.setForeground(Color.WHITE); // White text color for the message
    messageLabel.setOpaque(true);
    messageLabel.setBackground(new Color(222, 20, 20)); // Blue background color for the message
    messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding inside label
    
    // Add the message label to the panel
    panel.add(messageLabel, BorderLayout.CENTER);
    
    // Create a "New Game" button
    JButton newGameButton = new JButton("New Game");
    newGameButton.setFont(new Font("SansSerif", Font.BOLD, 16));
    newGameButton.setBackground(new Color(50, 150, 50));  // Green background for the button
    newGameButton.setForeground(Color.WHITE); // White text color for the button
    newGameButton.setFocusPainted(false);
    newGameButton.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(34, 139, 34), 3),  // Green border
        BorderFactory.createEmptyBorder(10, 20, 10, 20)  // Padding around the button
    ));
    
    // Hover effect for the "New Game" button
    newGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            newGameButton.setBackground(new Color(34, 139, 34));  // Darker green on hover
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            newGameButton.setBackground(new Color(50, 150, 50));  // Original green
        }
    });
    
    // Add an action listener to restart the game when the button is clicked
    newGameButton.addActionListener(e -> {
        dialog.dispose();
        startNewGame();  // Restart the game
    });
    
    // Create a panel for the button
    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(false); // Transparent background for the button panel
    buttonPanel.add(newGameButton);
    
    // Add the button panel to the dialog panel
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    // Set the content pane of the dialog
    dialog.add(panel);
    dialog.setVisible(true);
}

    private void startNewGame() {
        // Reset current attempt counter
        currentAttempt = 0;

        // Clear grid cells
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            for (int j = 0; j < 5; j++) {
                gridCells[i][j].setText("");
                gridCells[i][j].setBackground(Color.WHITE); // Reset background
            }
        }

        // Choose a new secret word
        SECRET_WORD = wordList.get(new Random().nextInt(wordList.size()));

        // Re-enable input and submit button
        inputField.setEditable(true);
        submitButton.setEnabled(true);

        // Clear the text field
        inputField.setText("");
    }

    private void endGame() {
        // Move to the next word in the list
        currentWordIndex = (currentWordIndex + 1) % wordList.size(); // Loop back to the start
        SECRET_WORD = wordList.get(currentWordIndex); // Set the next word
        currentAttempt = 0;
    }

    private void playWinSound() {
        try {
            File soundFile = new File("mixkit-video-game-win-2016.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playGameOverSound() {
        try {
            File soundFile = new File("mixkit-sad-game-over-trombone-471.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WordleGUI().setVisible(true));
    }
}
