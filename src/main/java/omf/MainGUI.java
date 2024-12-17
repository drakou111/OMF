package omf;

import omf.enu.InputState;
import omf.enu.SpeedOptimize;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.formdev.flatlaf.FlatDarkLaf;

public class MainGUI {

    private static List<MacroRuleInput> inputs = new ArrayList<>();
    private static int tierMomentum = 0;
    private static float startingAngle = 0F;
    private static float minVectorAngleGoal = -180f;
    private static float maxVectorAngleGoal = 180f;
    private static SpeedOptimize optimize = SpeedOptimize.MAX_SPEED;
    private static Effects effects = new Effects(0, 0, 0);
    private static JTextArea terminalArea;
    private static double momentumWidth = 10;
    private static double momentumHeight = 10;
    private static boolean frontWall = false;
    private static boolean rightWall = false;
    private static boolean backWall = false;
    private static boolean leftWall = false;

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

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JButton addInputButton = new JButton("Add Tick");
        DefaultListModel<MacroRuleInput> inputListModel = new DefaultListModel<>();
        JList<MacroRuleInput> inputList = new JList<>(inputListModel);
        inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
            MacroRuleInput input = new MacroRuleInput(
                    InputState.OFF, InputState.OFF, InputState.OFF,
                    InputState.OFF, InputState.OFF, InputState.OFF, InputState.OFF
            );
            inputs.add(input);
            inputListModel.addElement(input);
        });

        JButton duplicateInputButton = new JButton("Duplicate Selected");
        duplicateInputButton.addActionListener(e -> {
            int selectedIndex = inputList.getSelectedIndex();
            if (selectedIndex != -1) {
                MacroRuleInput input = inputs.get(selectedIndex);
                MacroRuleInput duplicate = new MacroRuleInput(
                        input.allStates[0], input.allStates[1], input.allStates[2],
                        input.allStates[3], input.allStates[4], input.allStates[5],
                        input.allStates[6]
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

        JPanel inputButtonsPanel = new JPanel(new GridLayout(1, 3));
        inputButtonsPanel.add(addInputButton);
        inputButtonsPanel.add(duplicateInputButton);
        inputButtonsPanel.add(removeInputButton);

        inputPanel.add(new JScrollPane(inputList), BorderLayout.CENTER);
        inputPanel.add(inputButtonsPanel, BorderLayout.SOUTH);

        // Parameter Panel (Right side)
        JPanel paramPanel = new JPanel(new GridLayout(0, 2));
        JTextField tierMomentumField = new JTextField(String.valueOf(tierMomentum));
        JTextField startingAngleField = new JTextField(String.valueOf(startingAngle));
        JTextField minAngleField = new JTextField(String.valueOf(minVectorAngleGoal));
        JTextField maxAngleField = new JTextField(String.valueOf(maxVectorAngleGoal));

        JComboBox<SpeedOptimize> optimizeCombo = new JComboBox<>(SpeedOptimize.values());
        optimizeCombo.setSelectedItem(optimize);

        JTextField speedField = new JTextField(String.valueOf(effects.speed));
        JTextField slownessField = new JTextField(String.valueOf(effects.slowness));

        JTextField widthField = new JTextField(String.valueOf(momentumWidth));
        JTextField heightField = new JTextField(String.valueOf(momentumHeight));
        JCheckBox frontWallCheck = new JCheckBox("Front Wall", frontWall);
        JCheckBox rightWallCheck = new JCheckBox("Right Wall", rightWall);
        JCheckBox backWallCheck = new JCheckBox("Back Wall", backWall);
        JCheckBox leftWallCheck = new JCheckBox("Left Wall", leftWall);

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

        //TODO: Add stop bruteforcer button maybe.

        startButton.addActionListener(e -> {
            try {
                tierMomentum = Integer.parseInt(tierMomentumField.getText());
                startingAngle = Float.parseFloat(startingAngleField.getText());
                minVectorAngleGoal = Float.parseFloat(minAngleField.getText()) * (float) Math.PI / 180f;
                maxVectorAngleGoal = Float.parseFloat(maxAngleField.getText()) * (float) Math.PI / 180f;
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

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit MacroRuleInput", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new MacroRuleInput(
                    (InputState) wBox.getSelectedItem(),
                    (InputState) aBox.getSelectedItem(),
                    (InputState) sBox.getSelectedItem(),
                    (InputState) dBox.getSelectedItem(),
                    (InputState) sprintBox.getSelectedItem(),
                    (InputState) shiftBox.getSelectedItem(),
                    (InputState) jumpBox.getSelectedItem()
            );
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::start);
    }
}
