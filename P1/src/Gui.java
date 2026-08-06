import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.LocalTime;


final class DateValidator
{

    // format dd/MM/yyyy
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);

    public static boolean isValidPastDate(String dateStr)
    {
        if(dateStr == null || dateStr.isEmpty())
        {
            return false;
        }
        try{
            LocalDate parsedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            return !parsedDate.isAfter(today);

        }
        catch (DateTimeParseException e)
        {
            return false;
        }
    }

    public static boolean isValidFutureDate(String dateStr)
    {
        if(dateStr == null || dateStr.isEmpty())
        {
            return false;
        }
        try{
            LocalDate parsedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            return parsedDate.isAfter(today);

        }
        catch (DateTimeParseException e)
        {
            return false;
        }
    }
}

final class TimeValidator
{
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
        .ofPattern("HH:mm")
        .withResolverStyle(ResolverStyle.STRICT);

    public static boolean isValidStartEnd(String startTimeStr, String endTimeStr)
    {
        if (startTimeStr == null || startTimeStr.trim().isEmpty() ||
                endTimeStr == null || endTimeStr.trim().isEmpty()) 
        {
            return false;
        }

        try
        {

            LocalTime start = LocalTime.parse(startTimeStr.trim(), TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endTimeStr.trim(), TIME_FORMATTER);

            return start.isBefore(end);
        }
        catch(DateTimeParseException e)
        {
            return false;
        }

    }
}

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

    public JPanel createLabeledPanel(String labelText, JComponent component)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(component);

        return panel;
    }

    public JPanel createCustomLabeledPanel(JLabel label, String text, JTextField field)
    {
        JPanel panel = new JPanel(new BorderLayout(0,4));
        label.setText(text);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

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


        JTextArea tab1Output = new JTextArea();
        tab1Output.setEditable(false);
        tab1Output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tab1Output.setText("--- RESEARCHER DATA LOG ---\nID: 001 | Name: Dr. Smith | Status: Active\nID: 002 | Name: Prof. Jones | Status: On Leave");
        JScrollPane tab1Scroll = new JScrollPane(tab1Output);
        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(tab1Scroll, BorderLayout.CENTER);

        btnRegister.addActionListener(e ->
                openWindow("Register", new RegisterPanel(0))
                );

        btnSearch.addActionListener(e ->
                openWindow("Search", new SearchPanel(0))
                );

        btnRefresh.addActionListener(e ->
                {

                    tab1Output.setText(API.getInstance().getAllResearcherOutput());

                    tab1Scroll.getVerticalScrollBar().setValue(0);
                }
                );



        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        tab2Header.add(new JLabel("Profiles"));

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchGroup.setBorder(BorderFactory.createTitledBorder("Search Researcher"));

        JTextField nameInput = new JTextField("ENTER RESEARCHER ID", 18);
        JButton checkBookingsBtn = new JButton("Check Current Bookings");

        searchGroup.add(nameInput);
        searchGroup.add(checkBookingsBtn);

        JButton mostBookingsBtn = new JButton("Most Bookings");


        searchGroup.add(mostBookingsBtn);

        tab2Header.add(searchGroup);

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


        checkBookingsBtn.addActionListener(e -> 
                {
                    try
                    {
                        String txt = nameInput.getText().trim();
                        Long id = Long.parseLong(txt);

                        String result = API.getInstance().searchResearcher(id);
                        tab2Output.setText(result);
                    }
                    catch(Exception ex)
                    {

                        JOptionPane.showMessageDialog(this, "Error. Check field", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                });

    }
}

class BookingsPanel extends BasePanel
{


    public BookingsPanel()
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnRegister = new JButton("Create Booking");
        tab1Header.add(btnRefresh);
        tab1Header.add(btnRegister);


        JTextArea tab1Output = new JTextArea();
        tab1Output.setEditable(false);
        tab1Output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tab1Output.setText("Bookings");
        JScrollPane tab1Scroll = new JScrollPane(tab1Output);
        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(tab1Scroll, BorderLayout.CENTER);

        btnRegister.addActionListener(e ->
                openWindow("Register", new RegisterPanel(2))
                );


        btnRefresh.addActionListener(e ->
                {

                    tab1Output.setText(API.getInstance().getAllBookings());
                    tab1Scroll.getVerticalScrollBar().setValue(0);
                }
                );

        tabbedPane.addTab("Overview", tab1);

        add(tabbedPane, BorderLayout.CENTER);


        // now populate the output
        tab1Output.setText(API.getInstance().getAllBookings());



    }
}

class EquipmentPanel extends BasePanel
{
    public EquipmentPanel()
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnRegister = new JButton("Register");
        JButton btnSearch = new JButton("Search");
        JLabel lblSummary = new JLabel("No summary available");
        tab1Header.add(btnRefresh);
        tab1Header.add(btnRegister);
        tab1Header.add(btnSearch);


        lblSummary.setText(API.getInstance().getEquipmentSummary());


        tab1Header.add(lblSummary);


        JTextArea tab1Output = new JTextArea();
        tab1Output.setEditable(false);
        tab1Output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tab1Output.setText("Equipment");
        JScrollPane tab1Scroll = new JScrollPane(tab1Output);
        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(tab1Scroll, BorderLayout.CENTER);

        btnRegister.addActionListener(e ->
                openWindow("Register", new RegisterPanel(1))
                );

        btnSearch.addActionListener(e ->
                openWindow("Search", new SearchPanel(1))
                );

        btnRefresh.addActionListener(e ->
                {

                    tab1Output.setText(API.getInstance().getAllEquipmentOutput());
                    tab1Scroll.getVerticalScrollBar().setValue(0);
                    lblSummary.setText(API.getInstance().getEquipmentSummary());
                }
                );



        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        tab2Header.add(new JLabel("Profiles"));

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchGroup.setBorder(BorderFactory.createTitledBorder("Search Equipment"));

        JTextField eID= new JTextField("ENTER EQUIPMENT ID", 18);
        JButton checkBookingsBtn = new JButton("Check history");
        JButton checkAvailablebtn = new JButton("Check Available");


        searchGroup.add(eID);
        searchGroup.add(checkBookingsBtn);
        searchGroup.add(checkAvailablebtn);


        tab2Header.add(searchGroup);

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
        tab1Output.setText(API.getInstance().getAllEquipmentOutput());

        checkAvailablebtn.addActionListener(e -> 
                {
                    String result = API.getInstance().getAvailableEquipment();
                    tab2Output.setText(result);
                });
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
    private JComboBox eStatus;

    private JTextField bookingDate;
    private JTextField startTime;
    private JTextField endTime;
    private JTextField rID;
    private JTextField eID;
    private JTextField bID;
    private JTextField purpose;

    // why a new format. I felt like it >:)
    private JLabel lblBID, lblRID, lblEID, lblDate, lblStart, lblEnd, lblPurpose;
    private JButton btnSubmit;
    private JLabel modeTitleLabel;
    private String currentMode = "CREATE";

    private void setFieldState(JTextField field, JLabel label, String baseText, boolean enabled, boolean required) {
        field.setEnabled(enabled);
        if (!enabled) {
            field.setText("");
        }

        String reqTag = required ? " * required" : "";
        label.setText(baseText + reqTag);
    }
    private void setFormMode(String mode) {
        this.currentMode = mode;

        switch (mode) {
            case "CREATE":
                modeTitleLabel.setText("Mode: Create New Booking");
                btnSubmit.setText("Create Booking");

                // bID is auto-generated in CREATE mode
                setFieldState(bID, lblBID, "Booking ID (Auto)", false, false);
                setFieldState(rID, lblRID, "Researcher ID", true, true);
                setFieldState(eID, lblEID, "Equipment ID", true, true);
                setFieldState(bookingDate, lblDate, "Date (dd/MM/yyyy)", true, true);
                setFieldState(startTime, lblStart, "Start Time (HH:mm)", true, true);
                setFieldState(endTime, lblEnd, "End Time (HH:mm)", true, true);
                setFieldState(purpose, lblPurpose, "Purpose", true, true);
                break;

            case "UPDATE":
                modeTitleLabel.setText("Mode: Update Existing Booking");
                btnSubmit.setText("Update Booking");

                // All fields editable in UPDATE mode
                setFieldState(bID, lblBID, "Booking ID (to update)", true,true);
                setFieldState(rID, lblRID, "Researcher ID", true,false);
                setFieldState(eID, lblEID, "Equipment ID", true,false);
                setFieldState(bookingDate, lblDate, "Date (dd/MM/yyyy)", true,false);
                setFieldState(startTime, lblStart, "Start Time (HH:mm)", true, false);
                setFieldState(endTime, lblEnd, "End Time (HH:mm)", true, false);
                setFieldState(purpose, lblPurpose, "Purpose", true, false);
                break;

            case "CANCEL":
                modeTitleLabel.setText("Mode: Cancel/Delete Booking");
                btnSubmit.setText("Cancel Booking");

                // Only bID is required to cancel a booking
                setFieldState(bID, lblBID, "Booking ID", true, true);
                setFieldState(rID, lblRID, "Researcher ID", false, false);
                setFieldState(eID, lblEID, "Equipment ID", false, false);
                setFieldState(bookingDate, lblDate, "Date (dd/MM/yyyy)", false, false);
                setFieldState(startTime, lblStart, "Start Time (HH:mm)", false, false);
                setFieldState(endTime, lblEnd, "End Time (HH:mm)", false, false);
                setFieldState(purpose, lblPurpose, "Purpose", false, false);
                break;
        }
    }

    public RegisterPanel(int startingIndex)
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRegister = new JButton("Register");
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

        btnRegister.addActionListener(e -> {

            String firstName  = fNameInput.getText().trim();
            String lastName   = sNameInput.getText().trim();
            String department = depInput.getText().trim();
            String email      = emailInput.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || department.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // check the email
            // TODO

            boolean success = API.getInstance().registerResearcher(firstName + " " + lastName, department, email);

            if(!success)
            {
                JOptionPane.showMessageDialog(null, "An error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "Researcher registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        });


        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(inputGroup, BorderLayout.CENTER);


        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnRegister2 = new JButton("Register");
        tab2Header.add(btnRegister2);

        JPanel inputGroup2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup2.setBorder(BorderFactory.createTitledBorder("Enter Details"));

        //is this slow? probably fine
        eName = new JTextField(18);
        eCat = new JTextField(18);
        ePurchDate = new JTextField(18);
        eRepCost = new JTextField(18);
        eStatus = new JComboBox<>(new String[]{"Available", "Out of Service"});

        inputGroup2.add(createLabeledPanel("Name:", eName));
        inputGroup2.add(createLabeledPanel("Category:",eCat));
        inputGroup2.add(createLabeledPanel("Purchase Date (dd/mm/yyyy):",ePurchDate));
        inputGroup2.add(createLabeledPanel("Replacement Cost:",eRepCost));
        inputGroup2.add(createLabeledPanel("Status:",eStatus));

        btnRegister2.addActionListener(e -> {

            String name = eName.getText().trim();
            String cat=eCat.getText().trim();
            String pd=ePurchDate.getText().trim();

            String repoCostString = eRepCost.getText().trim();
            double repCost = 0.0;

            String status=eStatus.getSelectedItem().toString();
            try {

                repCost = Double.parseDouble(repoCostString);

                // check the date
                if (!DateValidator.isValidPastDate(pd))
                {

                    JOptionPane.showMessageDialog(null, "Invalid date", "Error", JOptionPane.ERROR_MESSAGE);
                    return;

                }

                if (repCost < 0)
                {

                    JOptionPane.showMessageDialog(null, "Replacement cost may not be negative", "Error", JOptionPane.ERROR_MESSAGE);
                    return;

                }

            }
            catch (Exception ex)
            {

                JOptionPane.showMessageDialog(null, "Field: Replacement Cost -> " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (name.isEmpty() || cat.isEmpty() || pd.isEmpty() || status.isEmpty() || eRepCost.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            boolean success = API.getInstance().registerEquipment(name, cat, pd, repCost, status);
            if(!success)
            {
                JOptionPane.showMessageDialog(null, "An error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
            }else {
                JOptionPane.showMessageDialog(this, "Equipment registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        tab2.add(tab2Header, BorderLayout.NORTH);
        tab2.add(inputGroup2, BorderLayout.CENTER);

        JPanel tab3 = new JPanel(new BorderLayout(0,8));

        JPanel tab3Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JLabel lblMode = new JLabel("Select mode");
        JButton btnCreateMode = new JButton("Create Booking");
        JButton btnUpdateMode = new JButton("Update Booking");
        JButton btnCancelMode = new JButton("Cancel Booking");

        tab3Header.add(lblMode);
        tab3Header.add(btnCreateMode);
        tab3Header.add(btnUpdateMode);
        tab3Header.add(btnCancelMode);

        JPanel inputGroup3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        inputGroup3.setPreferredSize(new Dimension(650, 220));

        bID = new JTextField(18);
        rID = new JTextField(18);
        eID = new JTextField(18);
        bookingDate = new JTextField(18);
        startTime = new JTextField(18);
        endTime = new JTextField(18);
        purpose = new JTextField(18);

        lblBID = new JLabel();
        lblRID = new JLabel();
        lblEID = new JLabel();
        lblDate = new JLabel();
        lblStart = new JLabel();
        lblEnd = new JLabel();
        lblPurpose = new JLabel();

        inputGroup3.add(createCustomLabeledPanel(lblBID, "Booking ID", bID));
        inputGroup3.add(createCustomLabeledPanel(lblRID, "Researcher ID", rID));
        inputGroup3.add(createCustomLabeledPanel(lblEID, "Equipment ID", eID));
        inputGroup3.add(createCustomLabeledPanel(lblDate, "Date (dd/MM/yyyy)", bookingDate));
        inputGroup3.add(createCustomLabeledPanel(lblStart, "Start Time (HH:mm)", startTime));
        inputGroup3.add(createCustomLabeledPanel(lblEnd, "End Time (HH:mm)", endTime));
        inputGroup3.add(createCustomLabeledPanel(lblPurpose, "Purpose", purpose));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        modeTitleLabel = new JLabel("Current Mode: Create Booking");
        modeTitleLabel.setFont(modeTitleLabel.getFont().deriveFont(Font.BOLD));

        btnSubmit = new JButton("Submit request");

        footerPanel.add(modeTitleLabel);
        footerPanel.add(btnSubmit);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBorder(BorderFactory.createTitledBorder("Booking details"));
        centerContainer.add(inputGroup3, BorderLayout.CENTER);
        centerContainer.add(footerPanel, BorderLayout.SOUTH);

        tab3.add(tab3Header, BorderLayout.NORTH);
        tab3.add(centerContainer, BorderLayout.CENTER);

        btnCreateMode.addActionListener(e -> setFormMode("CREATE"));
        btnUpdateMode.addActionListener(e -> setFormMode("UPDATE"));
        btnCancelMode.addActionListener(e -> setFormMode("CANCEL"));

        btnSubmit.addActionListener(e -> {


            String bD = bookingDate.getText().trim();
            String sT = startTime.getText().trim();
            String eT = endTime.getText().trim();
            String p = purpose.getText().trim();

            //too lazy to make a method (so i typed it twice :D)
            Long researcherID;
            try {
                String text = rID.getText();
                researcherID = (text != null && !text.trim().isEmpty()) ? Long.parseLong(text.trim()) : -1L;
            } catch (NumberFormatException ex) {
                researcherID = -1L;
            }

            Long equipmentID;
            try {
                String text = eID.getText();
                equipmentID = (text != null && !text.trim().isEmpty()) ? Long.parseLong(text.trim()) : -1L;
            } catch (NumberFormatException ex) {
                equipmentID = -1L;
            }

            Long bookingID;
            try {
                String text = bID.getText();
                bookingID = (text != null && !text.trim().isEmpty()) ? Long.parseLong(text.trim()) : -1L;
            } catch (NumberFormatException ex) {
                bookingID = -1L;
            }


            switch (currentMode) {
                case "CREATE":
                    // TODO: Call create logic here

                    try {
                        // check the date
                        if (!DateValidator.isValidFutureDate(bD))
                        {

                            JOptionPane.showMessageDialog(null, "Invalid date", "Error", JOptionPane.ERROR_MESSAGE);
                            return;

                        }
                        // check start and end time is valid
                        if (!TimeValidator.isValidStartEnd(sT, eT))
                        {

                            JOptionPane.showMessageDialog(null, "Invalid time (Use military format)", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                    }
                    catch (Exception ex)
                    {

                        JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (bD.isEmpty() || sT.isEmpty() || eT.isEmpty() || p.isEmpty() || researcherID == -1 || equipmentID == -1) {
                        JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }


                    String success = API.getInstance().createBooking(bD, sT, eT, p, researcherID, equipmentID);
                    if(!success.equalsIgnoreCase("success"))
                    {
                        JOptionPane.showMessageDialog(null, "An error occurred!\n Message: " + success, "Error", JOptionPane.ERROR_MESSAGE);
                    }else {
                        JOptionPane.showMessageDialog(this, "Booking created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                case "UPDATE":
                    // CANCEL BOOKING THEN CREATE BOOKING :)
                    if(bookingID != -1)
                    {

                        if(!bD.isEmpty() || !sT.isEmpty() || !eT.isEmpty() || !p.isEmpty() || researcherID != -1 || equipmentID != -1)
                        {



                            return;
                        } 
                    }

                    JOptionPane.showMessageDialog(null, "Provide Booking ID and atleast one field", "Error", JOptionPane.ERROR_MESSAGE);

                    break;
                case "CANCEL":
                    // TODO: Call cancel logic here
                    break;
            }
        });








        tabbedPane.add("Researcher", tab1);
        tabbedPane.add("Equipment", tab2);
        tabbedPane.add("Bookings", tab3);

        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.setSelectedIndex(startingIndex);


        setFormMode("CREATE");




    }
}

class SearchPanel extends BasePanel
{
    private JTextField fNameInput;
    private JTextField sNameInput;
    private JTextField depInput;
    private JTextField emailInput;

    public SearchPanel(int startingIndex)
    {

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel tab1 = new JPanel(new BorderLayout(0,8));

        JPanel tab1Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnSearch = new JButton("Search");
        tab1Header.add(btnSearch);

        JPanel inputGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup.setBorder(BorderFactory.createTitledBorder("Enter Details for Search"));

        //is this slow? probably fine
        fNameInput = new JTextField(18);
        sNameInput = new JTextField(18);
        depInput   = new JTextField(18);
        emailInput = new JTextField(18);
        JTextField IDInput = new JTextField(18);


        inputGroup.add(createLabeledPanel("ID:", IDInput));
        inputGroup.add(createLabeledPanel("First Name:", fNameInput));
        inputGroup.add(createLabeledPanel("Last Name:", sNameInput));
        inputGroup.add(createLabeledPanel("Department:", depInput));
        inputGroup.add(createLabeledPanel("Email:", emailInput));

        btnSearch.addActionListener(e -> {
            String firstName = fNameInput.getText().trim();
            String lastName = sNameInput.getText().trim();
            String department = depInput.getText().trim();
            String email = emailInput.getText().trim();
            String id = IDInput.getText().trim();

            if (firstName.isEmpty() && lastName.isEmpty() && department.isEmpty() &&email.isEmpty() && id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Atleast one field is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Searching for: ");
            System.out.println("Researcher: " + firstName + " " + lastName);
            System.out.println("Dept: " + department + " | Email: " + email);
        });


        tab1.add(tab1Header, BorderLayout.NORTH);
        tab1.add(inputGroup, BorderLayout.CENTER);


        JPanel tab2 = new JPanel(new BorderLayout(0,8));

        JPanel tab2Header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,0));
        JButton btnSearch2 = new JButton("Search");
        tab2Header.add(btnSearch2);

        JPanel inputGroup2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        inputGroup2.setBorder(BorderFactory.createTitledBorder("Enter Details for Search"));

        //is this slow? probably fine
        JTextField eIDInput = new JTextField(18);
        JTextField  eNameInput = new JTextField(18);
        JTextField eCatInput= new JTextField(18);
        JTextField  eDateInput= new JTextField(18);
        JTextField repCostInput= new JTextField(18);
        JTextField statusInput= new JTextField(18);


        inputGroup2.add(createLabeledPanel("ID:", eIDInput));
        inputGroup2.add(createLabeledPanel("Name:", eNameInput));
        inputGroup2.add(createLabeledPanel("Category:",eCatInput));
        inputGroup2.add(createLabeledPanel("Purchase Date:", eDateInput));
        inputGroup2.add(createLabeledPanel("Replacement Cost:", repCostInput));
        inputGroup2.add(createLabeledPanel("Status:", statusInput));

        btnSearch2.addActionListener(e -> {
            String eName = eNameInput.getText().trim();
            String  eCat= eCatInput.getText().trim();
            String ePD= eDateInput.getText().trim();
            String eRepCost= repCostInput.getText().trim();
            String eStatus= statusInput.getText().trim();
            String eID = eIDInput.getText().trim();

            if (eName.isEmpty() && eCat.isEmpty() && ePD.isEmpty() && eRepCost.isEmpty() && eStatus.isEmpty() && eID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Atleast one field is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

        });


        tab2.add(tab2Header, BorderLayout.NORTH);
        tab2.add(inputGroup2, BorderLayout.CENTER);

        JPanel tab3 = new JPanel(new BorderLayout(0,8));

        tabbedPane.add("Researcher", tab1);
        tabbedPane.add("Equipment", tab2);
        tabbedPane.add("Bookings", tab3);

        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.setSelectedIndex(startingIndex);




    }
}
