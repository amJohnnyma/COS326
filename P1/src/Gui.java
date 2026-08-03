import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui{
    // open standalone window containing any jpanel
    private static void openWindow(String title, JPanel contentPanel)
    {
        JFrame window = new JFrame(title);

        //dont kill whole app when closing child window
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(600,400);
        window.setLocationRelativeTo(null); //center

        window.setContentPane(contentPanel);
        window.setVisible(true);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("COS326 Practical 1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    API.getInstance().close(); // Safely close ObjectDB connection
                }
            });

            frame.setPreferredSize(new Dimension(800,600));

            JPanel root = new JPanel(new BorderLayout(0,16));
            root.setBorder(new EmptyBorder(16,16,16,16));

            //main window
            JLabel welcomeLbl = new JLabel("Main Control Dashboard", SwingConstants.CENTER);
            welcomeLbl.setFont(welcomeLbl.getFont().deriveFont(18.f));

            root.add(welcomeLbl, BorderLayout.CENTER);

            //bottom nav
            Box hbox = Box.createHorizontalBox();
            JButton btnResearchers = new JButton("Researchers");
            JButton btnEquipment = new JButton("Equipment");
            JButton btnBookings = new JButton("Bookings");
            JButton btnPopulate = new JButton("Populate odb");
            JLabel navLbl = new JLabel("Navigation: ");

            btnResearchers.addActionListener(e ->
                    openWindow("Researchers", new ResearchersPanel())
                    );

            btnEquipment.addActionListener(e ->
                    openWindow("Equipment", new EquipmentPanel())
                    );

            btnBookings.addActionListener(e ->
                    openWindow("Bookings", new BookingsPanel())
                    );

            btnPopulate.addActionListener(e ->
                    {
                    API.getInstance().populate();
                    }
                    );
    
            hbox.add(navLbl);
            hbox.add(Box.createHorizontalGlue());
            hbox.add(btnResearchers);
            hbox.add(Box.createRigidArea(new Dimension(8,0)));
            hbox.add(btnEquipment);
            hbox.add(Box.createRigidArea(new Dimension(8,0)));
            hbox.add(btnBookings);
            hbox.add(Box.createRigidArea(new Dimension(8,0)));
            hbox.add(btnPopulate);


            root.add(hbox, BorderLayout.SOUTH);

            frame.setContentPane(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

abstract class BasePanel extends JPanel
{


    public static void openWindow(String title, JPanel contentPanel)
    {
        JFrame window = new JFrame(title);
        

        //dont kill whole app when closing child window
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(600,400);
        window.setLocationRelativeTo(null); //center

        window.setContentPane(contentPanel);
        window.setVisible(true);
    }

    public JPanel createLabeledPanel(String labelText, JTextField textField)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(textField);

        return panel;
    }


}

// scalable panels for new windows
class ResearchersPanel extends BasePanel
{

    // copy this for the rest probably
    public ResearchersPanel()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnRegister = new JButton("Register");
        JButton btnSearch = new JButton("Search");
        tab1Header.add(btnRefresh);
        tab1Header.add(btnRegister);
        tab1Header.add(btnSearch);


        btnRegister.addActionListener(e ->
                openWindow("Register", new RegisterPanel())
                );

        btnSearch.addActionListener(e ->
                openWindow("Search", new SearchPanel())
                );

        JTextArea tab1Output = new JTextArea();
        tab1Output.setEditable(false);
        tab1Output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tab1Output.setText("--- RESEARCHER DATA LOG ---\nID: 001 | Name: Dr. Smith | Status: Active\nID: 002 | Name: Prof. Jones | Status: On Leave");
        JScrollPane tab1Scroll = new JScrollPane(tab1Output);
        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(tab1Scroll, BorderLayout.CENTER);


        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        tab2Header.add(new JLabel("Profiles"));

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchGroup.setBorder(BorderFactory.createTitledBorder("Search Researcher"));

        JTextField nameInput = new JTextField("ENTER RESEARCHER NAME", 18);
        JButton checkBookingsBtn = new JButton("Check Current Bookings");

        searchGroup.add(nameInput);
        searchGroup.add(checkBookingsBtn);

        JButton mostBookingsBtn = new JButton("Most Bookings");

        tab2Header.add(searchGroup);
        tab2Header.add(mostBookingsBtn);

        JTextArea tab2Output = new JTextArea();
        tab2Output.setEditable(false);
        tab2Output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tab2Output.setText("Select action above to query");

        JScrollPane tab2Scroll = new JScrollPane(tab2Output);
        tab2.add(tab2Header, BorderLayout.NORTH);
        tab2.add(tab2Scroll, BorderLayout.CENTER);

        tabbedPane.addTab("Overview", tab1);
        tabbedPane.addTab("Directory", tab2);

        add(tabbedPane, BorderLayout.CENTER);


        // now populate the output
        tab1Output.setText(API.getInstance().getAllResearcherOutput());

    }
}

class BookingsPanel extends BasePanel
{
    public BookingsPanel()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tab1.add(new JLabel("Overview"));
        tab1.add(new JButton("Refresh"));
        tab1.add(new JButton("New Booking"));
        tab1.add(new JButton("Update Booking"));
        tab1.add(new JButton("Cancel Booking"));



        JPanel tab2 = new JPanel(new GridLayout());
        tab2.add(new JLabel("History"));

        tabbedPane.addTab("Overview", tab1);
        tabbedPane.addTab("History", tab2);

        add(tabbedPane, BorderLayout.CENTER);
    }
}

class EquipmentPanel extends BasePanel
{
    public EquipmentPanel()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tab1.add(new JLabel("Overview"));
        tab1.add(new JButton("Refresh"));
        tab1.add(new JButton("Register"));
        tab1.add(new JButton("Search"));

        JPanel tab2 = new JPanel(new GridLayout());
        tab2.add(new JLabel("Summary"));

        JPanel tab3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tab3.add(new JButton("Available"));
        tab3.add(new JButton("Never Booked"));

        tabbedPane.addTab("Overview", tab1);
        tabbedPane.addTab("Info", tab2);
        tabbedPane.addTab("Additional", tab3);

        add(tabbedPane, BorderLayout.CENTER);
    }
}

class RegisterPanel extends BasePanel
{
    private JTextField fNameInput;
    private JTextField sNameInput;
    private JTextField depInput;
    private JTextField emailInput;

    private JTextField eName;
    private JTextField eCat;
    private JTextField ePurchDate;
    private JTextField eRepCost;
    private JTextField eStatus;

    public RegisterPanel()
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnRegister = new JButton("Register");
        tab1Header.add(btnRefresh);
        tab1Header.add(btnRegister);

        JPanel inputGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup.setBorder(BorderFactory.createTitledBorder("Enter Details"));

        //is this slow? probably fine
        fNameInput = new JTextField(18);
        sNameInput = new JTextField(18);
        depInput   = new JTextField(18);
        emailInput = new JTextField(18);

        inputGroup.add(createLabeledPanel("First Name:", fNameInput));
        inputGroup.add(createLabeledPanel("Last Name:", sNameInput));
        inputGroup.add(createLabeledPanel("Department:", depInput));
        inputGroup.add(createLabeledPanel("Email:", emailInput));

/* TEMP TEST */
 btnRegister.addActionListener(e -> {
    // 1. Extract values
    String firstName  = fNameInput.getText().trim();
    String lastName   = sNameInput.getText().trim();
    String department = depInput.getText().trim();
    String email      = emailInput.getText().trim();

    // 2. Validate inputs (example check)
    if (firstName.isEmpty() || lastName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "First and Last Name are required!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    boolean success = API.getInstance().testRegister();

});
////////////////////


        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(inputGroup, BorderLayout.CENTER);


        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh2 = new JButton("Refresh");
        JButton btnRegister2 = new JButton("Register");
        tab2Header.add(btnRefresh2);
        tab2Header.add(btnRegister2);

        JPanel inputGroup2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup2.setBorder(BorderFactory.createTitledBorder("Enter Details"));

        //is this slow? probably fine
        eName = new JTextField(18);
        eCat = new JTextField(18);
        ePurchDate = new JTextField(18);
        eRepCost = new JTextField(18);
        eStatus = new JTextField(18);

        inputGroup2.add(createLabeledPanel("Name:", eName));
        inputGroup2.add(createLabeledPanel("Category:",eCat));
        inputGroup2.add(createLabeledPanel("Purchase Date:",ePurchDate));
        inputGroup2.add(createLabeledPanel("Replacement Cost:",eRepCost));
        inputGroup2.add(createLabeledPanel("Status:",eStatus));

/* TEMP TEST */
 btnRegister2.addActionListener(e -> {
    // 1. Extract values
    String name = eName.getText().trim();
    String cat=eCat.getText().trim();
    String pd=ePurchDate.getText().trim();
    String repCost=eRepCost.getText().trim();
    String status=eStatus.getText().trim();


    System.out.println("Name, Category: " + name + ", " + cat);
    System.out.println("pd: " + pd + " | rep cost: " + repCost + " | status: " + status);
});
////////////////////


        tab2.add(tab2Header, BorderLayout.NORTH);
        tab2.add(inputGroup2, BorderLayout.CENTER);

        JPanel tab3 = new JPanel(new BorderLayout(0,8));

        tabbedPane.add("Researcher", tab1);
        tabbedPane.add("Equipment", tab2);
        tabbedPane.add("Bookings", tab3);

        add(tabbedPane, BorderLayout.CENTER);




    }
}

class SearchPanel extends BasePanel
{
    private JTextField fNameInput;
    private JTextField sNameInput;
    private JTextField depInput;
    private JTextField emailInput;

    public SearchPanel()
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnSearch = new JButton("Search");
        tab1Header.add(btnRefresh);
        tab1Header.add(btnSearch);

        JPanel inputGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup.setBorder(BorderFactory.createTitledBorder("Enter Details for Search"));

        //is this slow? probably fine
        fNameInput = new JTextField(18);
        sNameInput = new JTextField(18);
        depInput   = new JTextField(18);
        emailInput = new JTextField(18);

        inputGroup.add(createLabeledPanel("First Name:", fNameInput));
        inputGroup.add(createLabeledPanel("Last Name:", sNameInput));
        inputGroup.add(createLabeledPanel("Department:", depInput));
        inputGroup.add(createLabeledPanel("Email:", emailInput));

/* TEMP TEST */
 btnSearch.addActionListener(e -> {
    // 1. Extract values
    String firstName  = fNameInput.getText().trim();
    String lastName   = sNameInput.getText().trim();
    String department = depInput.getText().trim();
    String email      = emailInput.getText().trim();

    // 2. Validate inputs (example check)
    if (firstName.isEmpty() && lastName.isEmpty() && department.isEmpty() &&email.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Atleast one field is required!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 3. Process or print data
    System.out.println("Searching for: ");
    System.out.println("Researcher: " + firstName + " " + lastName);
    System.out.println("Dept: " + department + " | Email: " + email);
});
////////////////////


        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(inputGroup, BorderLayout.CENTER);


        JPanel tab2 = new JPanel(new BorderLayout(0,8));
        JPanel tab3 = new JPanel(new BorderLayout(0,8));

        tabbedPane.add("Researcher", tab1);
        tabbedPane.add("Equipment", tab2);
        tabbedPane.add("Bookings", tab3);

        add(tabbedPane, BorderLayout.CENTER);




    }
}
