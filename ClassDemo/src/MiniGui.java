import javax.swing.*;
import java.awt.*;

public class MiniGui {
    public static void main(String[] args) {
        // Run UI creation on the Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("COS326 Practical GUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("ObjectDB & Swing Demo");
            JButton button = new JButton("Click Me");

            button.addActionListener(e -> {
                JOptionPane.showMessageDialog(frame, "Hello from Swing!");
            });

            frame.add(label);
            frame.add(button);
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }
}
