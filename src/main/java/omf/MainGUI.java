package omf;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import omf.enu.InputState;
import omf.enu.SpeedOptimize;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MainGUI {

    private static List<MacroRuleInput> inputs = new ArrayList<>();
    private static int tierMomentum = 0;
    private static float startingAngle = 0F;
    private static float minVectorAngleGoal = -180f;
    private static float maxVectorAngleGoal = 180f;
    private static SpeedOptimize optimize = SpeedOptimize.MAX_SPEED;
    private static Effects effects = new Effects(0, 0, 0);
    private static JTextArea terminalArea;
    private static double momentumWidth = 1.6;
    private static double momentumHeight = 1.6;
    private static boolean frontWall = false;
    private static boolean rightWall = false;
    private static boolean backWall = false;
    private static boolean leftWall = false;

    private static JTextField tierMomentumField;
    private static JTextField startingAngleField;
    private static JTextField minAngleField;
    private static JTextField maxAngleField;
    private static JTextField speedField;
    private static JTextField slownessField;
    private static JTextField widthField;
    private static JTextField heightField;
    private static JCheckBox frontWallCheck;
    private static JCheckBox rightWallCheck;
    private static JCheckBox backWallCheck;
    private static JCheckBox leftWallCheck;
    private static JComboBox<SpeedOptimize> optimizeCombo;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void start() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Create the main frame
        JFrame frame = new JFrame("OMF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());


        // Add a menu bar for Save/Load
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("Save State");
        saveItem.addActionListener(e -> saveStateToClipboard());

        JMenuItem loadItem = new JMenuItem("Load State");
        loadItem.addActionListener(e -> loadStateFromText(frame));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);


        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JButton addInputButton = new JButton("Add Tick");
        DefaultListModel<MacroRuleInput> inputListModel = new DefaultListModel<>();
        JList<MacroRuleInput> inputList = new JList<>(inputListModel);
        inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (MacroRuleInput input : inputs) {
            inputListModel.addElement(input);
        }

        inputList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // Detect double-click
                    int selectedIndex = inputList.locationToIndex(e.getPoint());
                    if (selectedIndex != -1) {
                        MacroRuleInput input = inputs.get(selectedIndex);
                        MacroRuleInput updatedInput = showInputDialog(frame, input);
                        if (updatedInput != null) {
                            inputs.set(selectedIndex, updatedInput);
                            inputListModel.set(selectedIndex, updatedInput);
                        }
                    }
                }
            }
        });

        inputList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int selectedIndex = inputList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        inputs.remove(selectedIndex);
                        inputListModel.remove(selectedIndex);

                        // Automatically reselect an item
                        if (selectedIndex > 0) {
                            inputList.setSelectedIndex(selectedIndex - 1); // Select the one before
                        } else if (!inputListModel.isEmpty()) {
                            inputList.setSelectedIndex(0); // Select the first item if at the start
                        }
                    }
                }
            }
        });

        // Enable drag-and-drop reordering
        inputList.setDragEnabled(true);
        inputList.setDropMode(DropMode.INSERT);
        inputList.setTransferHandler(new TransferHandler() {
            private int dragSourceIndex = -1;

            @Override
            protected Transferable createTransferable(JComponent c) {
                dragSourceIndex = inputList.getSelectedIndex();
                return new StringSelection(""); // We don't need to transfer actual data
            }

            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dropLocation.getIndex();

                if (dragSourceIndex != -1 && dropIndex != dragSourceIndex) {
                    // Reorder the items in the list
                    MacroRuleInput draggedItem = inputs.remove(dragSourceIndex);
                    if (dropIndex > dragSourceIndex) {
                        dropIndex--; // Adjust drop index due to shifting during removal
                    }
                    inputs.add(dropIndex, draggedItem);

                    // Update the UI model
                    inputListModel.remove(dragSourceIndex);
                    inputListModel.add(dropIndex, draggedItem);

                    // Reselect the dragged item at its new position
                    inputList.setSelectedIndex(dropIndex);
                    return true;
                }

                return false;
            }
        });

        // Terminal Panel (Left side)
        JPanel terminalPanel = new JPanel(new BorderLayout());
        terminalArea = new JTextArea();
        terminalArea.setEditable(false); // Make the terminal read-only
        terminalArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane terminalScrollPane = new JScrollPane(terminalArea);
        terminalPanel.add(new JLabel("Terminal Output:"), BorderLayout.NORTH);
        terminalPanel.add(terminalScrollPane, BorderLayout.CENTER);

        redirectSystemOutToTerminal();

        System.out.println("Welcome to OMF (Optimal Momentum Finder), by Drakou111, thanks to Leg0shii for movement code.");
        System.out.println("The goal of this program is to come up with macros given some restrictions, like momentum, goal angles, etc.");
        System.out.println();
        System.out.println("On the right side of the window, you can modify the restrictions.");
        System.out.println("- Tier Momentum - What tier the momentum should be in.");
        System.out.println("- Starting Angle - What angle should you be facing the whole time.");
        System.out.println("- Min Vector Angle - Minimum final speed vector angle you should reach (0 if don't care).");
        System.out.println("- Max Vector Angle - Maximum final speed vector angle you should reach (0 if don't care).");
        System.out.println("- Optimize - What to optimize for.");
        System.out.println("- Speed & Slowness - Potion effects to use. 0 is no effects.");
        System.out.println("- Momentum width/length - How much momentum is allowed (x and z). (eg: 1bm backwall would be width=1.6, length=1)");
        System.out.println("- Frontwall/Rightwall/Backwall/Leftwall - If there's a wall on that side.");
        System.out.println();
        System.out.println("In the middle is where you can specify your macro inputs.");
        System.out.println("- Any input set to ON WILL use it during that tick.");
        System.out.println("- Any input set to OFF WON'T use it during that tick.");
        System.out.println("- Any input set to UNKNOWN will get optimized by brute force.");
        System.out.println();
        System.out.println("Once you're satisfied, press 'Start BruteForcer' and the outputs should appear here!");
        System.out.println();
        System.out.println("HAVE FUN!");


        addInputButton.addActionListener(e -> {
            MacroRuleInput input = new MacroRuleInput(InputState.OFF, InputState.OFF, InputState.OFF, InputState.OFF, InputState.OFF, InputState.OFF, InputState.OFF, 0.0F);
            int selectedIndex = inputList.getSelectedIndex();

            if (selectedIndex == -1) {
                // If no item is selected, add to the end
                inputs.add(input);
                inputListModel.addElement(input);
            } else {
                // Insert after the selected slot
                int insertIndex = selectedIndex + 1;
                inputs.add(insertIndex, input);
                inputListModel.add(insertIndex, input);
                inputList.setSelectedIndex(insertIndex); // Keep the new item selected
            }
        });


        JButton duplicateInputButton = new JButton("Duplicate Selected");
        duplicateInputButton.addActionListener(e -> {
            int selectedIndex = inputList.getSelectedIndex();
            if (selectedIndex != -1) {
                MacroRuleInput input = inputs.get(selectedIndex);
                MacroRuleInput duplicate = new MacroRuleInput(
                        input.allStates[0], input.allStates[1], input.allStates[2],
                        input.allStates[3], input.allStates[4], input.allStates[5],
                        input.allStates[6], input.turn
                );
                inputs.add(selectedIndex + 1, duplicate);
                inputListModel.add(selectedIndex + 1, duplicate);
            }
        });

        JButton removeInputButton = new JButton("Remove Selected");
        removeInputButton.addActionListener(e -> {
            int selectedIndex = inputList.getSelectedIndex();
            if (selectedIndex != -1) {
                inputs.remove(selectedIndex);
                inputListModel.remove(selectedIndex);

                // Automatically reselect an item
                if (selectedIndex > 0) {
                    inputList.setSelectedIndex(selectedIndex - 1); // Select the one before
                } else if (!inputListModel.isEmpty()) {
                    inputList.setSelectedIndex(0); // Select the first item if at the start
                }
            }
        });

        JButton clearInputsButton = new JButton("Clear All");
        clearInputsButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to clear all inputs?",
                    "Confirm Clear",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                inputs.clear();
                inputListModel.clear();
            }
        });

        JPanel inputButtonsPanel = new JPanel(new GridLayout(1, 3));
        inputButtonsPanel.add(addInputButton);
        inputButtonsPanel.add(duplicateInputButton);
        inputButtonsPanel.add(removeInputButton);
        inputButtonsPanel.add(clearInputsButton);

        inputPanel.add(new JScrollPane(inputList), BorderLayout.CENTER);
        inputPanel.add(inputButtonsPanel, BorderLayout.SOUTH);

        // Parameter Panel (Right side)
        JPanel paramPanel = new JPanel(new GridLayout(0, 2));
        tierMomentumField = new JTextField(String.valueOf(tierMomentum));
        startingAngleField = new JTextField(String.valueOf(startingAngle));
        minAngleField = new JTextField(String.valueOf(minVectorAngleGoal));
        maxAngleField = new JTextField(String.valueOf(maxVectorAngleGoal));

        optimizeCombo = new JComboBox<>(SpeedOptimize.values());
        optimizeCombo.setSelectedItem(optimize);

        speedField = new JTextField(String.valueOf(effects.speed));
        slownessField = new JTextField(String.valueOf(effects.slowness));

        widthField = new JTextField(String.valueOf(momentumWidth));
        heightField = new JTextField(String.valueOf(momentumHeight));
        frontWallCheck = new JCheckBox("Front Wall", frontWall);
        rightWallCheck = new JCheckBox("Right Wall", rightWall);
        backWallCheck = new JCheckBox("Back Wall", backWall);
        leftWallCheck = new JCheckBox("Left Wall", leftWall);

        paramPanel.add(new JLabel("Tier Momentum:"));
        paramPanel.add(tierMomentumField);
        paramPanel.add(new JLabel("Starting Angle:"));
        paramPanel.add(startingAngleField);
        paramPanel.add(new JLabel("Min Vector Angle Goal:"));
        paramPanel.add(minAngleField);
        paramPanel.add(new JLabel("Max Vector Angle Goal:"));
        paramPanel.add(maxAngleField);
        paramPanel.add(new JLabel("Optimize:"));
        paramPanel.add(optimizeCombo);
        paramPanel.add(new JLabel("Speed Effect:"));
        paramPanel.add(speedField);
        paramPanel.add(new JLabel("Slowness Effect:"));
        paramPanel.add(slownessField);

        paramPanel.add(new JLabel("Momentum Width (X):"));
        paramPanel.add(widthField);
        paramPanel.add(new JLabel("Momentum Length (Z):"));
        paramPanel.add(heightField);
        paramPanel.add(frontWallCheck);
        paramPanel.add(rightWallCheck);
        paramPanel.add(backWallCheck);
        paramPanel.add(leftWallCheck);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start BruteForcer");
        JButton stopButton = new JButton("Stop BruteForcer");

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        AtomicReference<Thread> bruteForceThread = new AtomicReference<>();  // Track the current brute force thread

        startButton.addActionListener(e -> {
            try {
                tierMomentum = Integer.parseInt(tierMomentumField.getText());
                startingAngle = Float.parseFloat(startingAngleField.getText());
                minVectorAngleGoal = Float.parseFloat(minAngleField.getText());
                maxVectorAngleGoal = Float.parseFloat(maxAngleField.getText());
                optimize = (SpeedOptimize) optimizeCombo.getSelectedItem();
                effects.jumpBoost = 0;
                effects.speed = (Integer.parseInt(speedField.getText()));
                effects.slowness = (Integer.parseInt(slownessField.getText()));
                momentumWidth = Double.parseDouble(widthField.getText());
                momentumHeight = Double.parseDouble(heightField.getText());
                frontWall = frontWallCheck.isSelected();
                rightWall = rightWallCheck.isSelected();
                backWall = backWallCheck.isSelected();
                leftWall = leftWallCheck.isSelected();

                MomentumRule momentumRule = new MomentumRule(
                        momentumWidth, momentumHeight,
                        frontWall, rightWall, backWall, leftWall
                );

                MacroRule rules = new MacroRule(inputs, momentumRule, tierMomentum, startingAngle,
                        minVectorAngleGoal, maxVectorAngleGoal, optimize, effects);

                // Interrupt the previous thread if it exists
                if (bruteForceThread.get() != null && bruteForceThread.get().isAlive()) {
                    bruteForceThread.get().interrupt();
                }

                // Create a new thread to run BruteForcer
                bruteForceThread.set(new Thread(() -> {
                    try {
                        BruteForcer.start(rules);  // Start the brute forcing process
                    } catch (Exception ex) {
                        // Handle any errors in the brute forcing process
                        JOptionPane.showMessageDialog(frame, "Error in brute forcing: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }));

                // Start the new brute force thread
                bruteForceThread.get().start();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please check your parameters.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Stop BruteForcer button action
        stopButton.addActionListener(e -> {
            // Interrupt the thread if it is running
            if (bruteForceThread.get() != null && bruteForceThread.get().isAlive()) {
                bruteForceThread.get().interrupt();
                JOptionPane.showMessageDialog(frame, "BruteForcer has been stopped.", "Stopped", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "No BruteForcer is running.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, paramPanel);
        horizontalSplitPane.setDividerLocation(800);
        horizontalSplitPane.setDividerSize(30);  // Adjust this value as needed

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(horizontalSplitPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, terminalPanel);
        splitPane.setDividerLocation(500);
        splitPane.setDividerSize(30);  // Adjust this value as needed

        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void saveStateToClipboard() {
        // Create an object to store the state
        AppState state = new AppState(inputs, tierMomentum, startingAngle, minVectorAngleGoal, maxVectorAngleGoal,
                optimize, effects, momentumWidth, momentumHeight, frontWall, rightWall, backWall, leftWall);

        // Serialize the state to JSON and encode as Base64
        String json = gson.toJson(state);
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());

        // Copy the Base64 string to the clipboard
        StringSelection selection = new StringSelection(base64);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);

        JOptionPane.showMessageDialog(null, "State saved to clipboard!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void loadStateFromText(Frame frame) {
        // Create a JDialog for input
        JDialog dialog = new JDialog(frame, "Enter Base64 String", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(frame);

        // Create an input field for the Base64 string
        JTextField inputField = new JTextField();
        inputField.setColumns(30);

        // Create buttons for "OK" and "Cancel"
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Handle "OK" button click
        okButton.addActionListener(e -> {
            try {
                // Get the Base64 string entered by the user
                String base64 = inputField.getText().trim();

                if (base64.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid Base64 string", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Decode the Base64 string and deserialize the JSON
                String json = new String(Base64.getDecoder().decode(base64));
                AppState state = gson.fromJson(json, AppState.class);

                // Restore the state
                inputs = state.inputs;
                tierMomentum = state.tierMomentum;
                startingAngle = state.startingAngle;
                minVectorAngleGoal = state.minVectorAngleGoal;
                maxVectorAngleGoal = state.maxVectorAngleGoal;
                optimize = state.optimize;
                effects = state.effects;
                momentumWidth = state.momentumWidth;
                momentumHeight = state.momentumHeight;
                frontWall = state.frontWall;
                rightWall = state.rightWall;
                backWall = state.backWall;
                leftWall = state.leftWall;

                // Dispose the current window and restart the application
                frame.dispose();
                start();

                JOptionPane.showMessageDialog(dialog, "State loaded successfully!", "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to load state: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Handle "Cancel" button click
        cancelButton.addActionListener(e -> dialog.dispose());

        // Set up the layout of the dialog
        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Base64 String:"));
        panel.add(inputField);
        panel.add(okButton);
        panel.add(cancelButton);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    // Add a class to represent the application state
    private static class AppState {
        List<MacroRuleInput> inputs;
        int tierMomentum;
        float startingAngle;
        float minVectorAngleGoal;
        float maxVectorAngleGoal;
        SpeedOptimize optimize;
        Effects effects;
        double momentumWidth;
        double momentumHeight;
        boolean frontWall;
        boolean rightWall;
        boolean backWall;
        boolean leftWall;

        AppState(List<MacroRuleInput> inputs, int tierMomentum, float startingAngle, float minVectorAngleGoal,
                 float maxVectorAngleGoal, SpeedOptimize optimize, Effects effects, double momentumWidth,
                 double momentumHeight, boolean frontWall, boolean rightWall, boolean backWall, boolean leftWall) {
            this.inputs = inputs;
            this.tierMomentum = tierMomentum;
            this.startingAngle = startingAngle;
            this.minVectorAngleGoal = minVectorAngleGoal;
            this.maxVectorAngleGoal = maxVectorAngleGoal;
            this.optimize = optimize;
            this.effects = effects;
            this.momentumWidth = momentumWidth;
            this.momentumHeight = momentumHeight;
            this.frontWall = frontWall;
            this.rightWall = rightWall;
            this.backWall = backWall;
            this.leftWall = leftWall;
        }
    }

    private static void redirectSystemOutToTerminal() {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) {
                terminalArea.append(String.valueOf((char) b));
                terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
            }

            @Override
            public void write(byte[] b, int off, int len) {
                terminalArea.append(new String(b, off, len));
                terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
            }
        };

        System.setOut(new PrintStream(outputStream, true));
    }

    private static MacroRuleInput showInputDialog(Component parent, MacroRuleInput input) {
        JComboBox<InputState> wBox = new JComboBox<>(InputState.values());
        wBox.setSelectedItem(input.allStates[0]);

        JComboBox<InputState> aBox = new JComboBox<>(InputState.values());
        aBox.setSelectedItem(input.allStates[1]);

        JComboBox<InputState> sBox = new JComboBox<>(InputState.values());
        sBox.setSelectedItem(input.allStates[2]);

        JComboBox<InputState> dBox = new JComboBox<>(InputState.values());
        dBox.setSelectedItem(input.allStates[3]);

        JComboBox<InputState> sprintBox = new JComboBox<>(InputState.values());
        sprintBox.setSelectedItem(input.allStates[4]);

        JComboBox<InputState> shiftBox = new JComboBox<>(InputState.values());
        shiftBox.setSelectedItem(input.allStates[5]);

        JComboBox<InputState> jumpBox = new JComboBox<>(InputState.values());
        jumpBox.setSelectedItem(input.allStates[6]);

        JTextField turnField = new JTextField(20); // 10-character-wide input field
        turnField.setText(String.valueOf(input.turn)); // Assuming 'input' has a getTurn() method

        turnField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = turnField.getText();
                try {
                    input.turn = Float.parseFloat(text); // Assuming 'input' has a setTurn(float) method
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Please enter a valid float value for Turn.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE
                    );
                    turnField.requestFocus(); // Refocus the field if input is invalid
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("W:"));
        panel.add(wBox);
        panel.add(new JLabel("A:"));
        panel.add(aBox);
        panel.add(new JLabel("S:"));
        panel.add(sBox);
        panel.add(new JLabel("D:"));
        panel.add(dBox);
        panel.add(new JLabel("SPRINT:"));
        panel.add(sprintBox);
        panel.add(new JLabel("SHIFT:"));
        panel.add(shiftBox);
        panel.add(new JLabel("JUMP:"));
        panel.add(jumpBox);
        panel.add(new JLabel("TURN:"));
        panel.add(turnField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit MacroRuleInput", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new MacroRuleInput(
                (InputState) wBox.getSelectedItem(),
                (InputState) aBox.getSelectedItem(),
                (InputState) sBox.getSelectedItem(),
                (InputState) dBox.getSelectedItem(),
                (InputState) sprintBox.getSelectedItem(),
                (InputState) shiftBox.getSelectedItem(),
                (InputState) jumpBox.getSelectedItem(),
                Float.parseFloat(turnField.getText())
            );
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::start);
    }
}
