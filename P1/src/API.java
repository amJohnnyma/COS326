
import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

// Singleton !!!
public class API{

    private static API instance;

    public static synchronized API getInstance()
    {
        if(instance == null)
        {
            instance = new API();
        }
        return instance;
    }

    private EntityManagerFactory emf;
    public API() 
    {
        emf = Persistence.createEntityManagerFactory("system.odb");
    }

    public void close()
    {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public void populate()
    {
        System.out.println("\n==================================================");
        System.out.println("          STARTING DATABASE POPULATION            ");
        System.out.println("==================================================");

        // 1. Create 10 Researchers using registerResearcher()
        int resSuccess = 0;
        for (int i = 1; i <= 10; i++) {
            boolean ok = registerResearcher(
                    "Researcher: " + i,
                    "Department " + ((i % 3) + 1),
                    "researcher" + i + "@up.ac.za"
                    );
            if (ok) resSuccess++;
        }
        System.out.println("✔ Registered " + resSuccess + "/10 Researchers using registerResearcher()");

        // 2. Create 30 Equipment using registerEquipment() (Every 5th is 'Out of service')
        int eqSuccess = 0;
        for (int i = 1; i <= 30; i++) {
            String status = (i % 5 == 0) ? "Out of service" : "Available";
            boolean ok = registerEquipment(
                    "Equipment: " + i,
                    "Category" + ((i % 4) + 1),
                    String.format("%02d", i) + "/08/2026",
                    500.0 * i,
                    status
                    );
            if (ok) eqSuccess++;
        }
        System.out.println("✔ Registered " + eqSuccess + "/30 Equipment items using registerEquipment()");

        // 3. Seed Bookings using createBooking()
        System.out.println("\n--- Seeding Bookings via createBooking() ---");
        int successBookings = 0;

        // Loop to create past and future valid bookings across available equipment and researchers
        for (int i = 1; i <= 50; i++) {
            Long rID = (long) ((i % 10) + 1);
            Long eID = (long) ((i % 30) + 1);

            // Spread dates across July (past) and August (present/future)
            String dateStr = (i <= 25) 
                ? String.format("%02d/07/2026", (i % 20) + 1)
                : String.format("%02d/08/2026", (i % 25) + 1);

            int startHour = 8 + ((i / 30) * 3);
            String startTime = String.format("%02d:00", startHour);
            String endTime = String.format("%02d:00", startHour + 2);

            String result = createBooking(
                    dateStr,
                    startTime,
                    endTime,
                    "Research Project " + i,
                    rID,
                    eID
                    );

            if ("Success".equalsIgnoreCase(result)) {
                successBookings++;
            }
        }
        System.out.println("✔ Successfully created " + successBookings + " valid bookings using createBooking()");

        // 4. Test and print Business Rule outputs to terminal
        System.out.println("\n--------------------------------------------------");
        System.out.println("       BUSINESS RULE VERIFICATION TESTS           ");
        System.out.println("--------------------------------------------------");

        // Rule Test A: Book Out of Service Equipment (eID = 5)
        String rA = createBooking("15/08/2026", "09:00", "11:00", "Test Out of Service", 1L, 5L);
        System.out.println("Rule Check [Out of Service Equipment]: " + rA);

        // Rule Test B: Time overlap / Buffer (<10 mins)
        createBooking("20/08/2026", "10:00", "12:00", "Base Slot", 1L, 1L);
        String rB = createBooking("20/08/2026", "12:05", "14:00", "Overlap Slot", 2L, 1L);
        System.out.println("Rule Check [10-Min Buffer Overlap]:    " + rB);

        // Rule Test C: Max 3 Active Bookings
        createBooking("25/08/2026", "08:00", "09:00", "Active 1", 3L, 2L);
        createBooking("25/08/2026", "10:00", "11:00", "Active 2", 3L, 3L);
        createBooking("25/08/2026", "12:00", "13:00", "Active 3", 3L, 4L);
        String rC = createBooking("25/08/2026", "14:00", "15:00", "Active 4 (Should Fail)", 3L, 6L);
        System.out.println("Rule Check [Max 3 Active Bookings]:    " + rC);

        // 5. Output Query Results to Terminal
        System.out.println("\n==================================================");
        System.out.println("            QUERY OUTPUTS FROM DATABASE           ");
        System.out.println("==================================================");

        System.out.println("\n--- 1. Equipment Summary ---");
        System.out.println(getEquipmentSummary());

        System.out.println("\n--- 2. Top Researcher with Most Bookings ---");
        System.out.println(getHighestBookingResearchers());

        System.out.println("\n--- 3. Unused Equipment ---");
        System.out.println(getUnusedEquipment());

        System.out.println("==================================================\n");
    }


    //bools so i can get feedback on the UI
    public boolean registerResearcher(String fullName, String department, String email)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Researcher r = new Researcher(
                    fullName,
                    department,
                    email
                    );
            em.persist(r);

            em.getTransaction().commit();

        }
        catch(Exception ex)
        {

            if(em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            ex.printStackTrace();
            return false;
        }
        finally {
            em.close();
            return true;
        }
    }

    public boolean registerEquipment(String name, String category, String purchaseDate, double replacementCost, String status)
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Equipment r = new Equipment(
                    name,
                    category,
                    purchaseDate,
                    replacementCost,
                    status
                    );
            em.persist(r);

            em.getTransaction().commit();

        }
        catch(Exception ex)
        {

            if(em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            ex.printStackTrace();
            return false;
        }
        finally {
            em.close();
            return true;
        }

    }

    public String createBooking(String bDate, String startTime, String endTime, String purpose, Long rID, Long eID)
    {
        //*** equipment marked as out of service may not be booked
        //done on ui:  must have valid start/end time
        //*** researcher may not have more than 3 active bookings
        //*** Must check for conflicting equipment bookings
        //*** researcher may not create multiple bookings for the same equipment on the same day
        //done on ui: bookings must be made for present or future


        // get researcher
        // get equipment
        // -> Out of Service == return false
        // retrieve researcher bookings. May only have 3 from present to future. else return false
        // Check all bookings to see if the equipment:
        // -> 1. Has bookings for it
        // -> 2. The bookings overlap date and time (Do not allow bookings to be booked on same end and start hour e.g. 
        // Booking 1 : 12:00 - 15:00
        // Booking 2 : 15:00 - 17:00 NOT ALLOWED
        // 
        // Booking 1 : 12:00 - 15:00
        // Booking 2 : 15:10 - 17:00 ALLOWED - 10 minutes apart MINIMUM
        // )

        EntityManager em = emf.createEntityManager();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

        LocalDate newDate = LocalDate.parse(bDate, dateFormatter);
        LocalTime newStart = LocalTime.parse(startTime, timeFormatter);
        LocalTime newEnd = LocalTime.parse(endTime, timeFormatter);

        try
        {
            em.getTransaction().begin();

            // do both methods just for fun
            Researcher researcher = em.find(Researcher.class, rID);
            if (researcher == null)
            {
                return "Researcher does not exist (or missing ID input)";
            }

            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT e FROM Equipment e WHERE e.eID = :eID",
                    Equipment.class).setParameter("eID", eID);
            List<Equipment> equipmentL = query.getResultList();
            if(equipmentL.isEmpty())
            {
                return "Equipment does not exist";
            }
            Equipment equipment = equipmentL.get(0);
            if (equipment == null || !"Available".equalsIgnoreCase(equipment.getStatus()))
            {
                return "Equipment does not exist, or is unavailable (or missing ID input)";
            }


            // check researcher bookings
            TypedQuery<Booking> query2 = em.createQuery(
                    "SELECT b FROM Booking b WHERE b.researcher.rID = :rID",
                    Booking.class)
                .setParameter("rID", rID);
            List<Booking> rBookings = query2.getResultList();

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            long activeCount = 0;

            for (Booking b : rBookings)
            {
                LocalDate bDateParsed = LocalDate.parse(b.getDate(), dateFormatter);
                LocalTime bEndParsed = LocalTime.parse(b.getEndTime(), timeFormatter);
                if (bDateParsed.isAfter(today) || (bDateParsed.isEqual(today) && bEndParsed.isAfter(now))) {

                    if (b.getEquipment().getrID().equals(eID) && bDateParsed.isEqual(newDate)) {
                        return "Equipment has already been booked today";
                    }
                    activeCount++;

                }


                if (activeCount >= 3){
                    return "Researcher already has 3 active bookings";
                }

            }

            TypedQuery<Booking> eqBookingsQuery = em.createQuery(
                    "SELECT b FROM Booking b WHERE b.equipment.eID = :eID AND b.date = :bDate", Booking.class)
                .setParameter("eID", eID)
                .setParameter("bDate", bDate);
            List<Booking> equipmentSameDayBookings = eqBookingsQuery.getResultList();

            for (Booking existing : equipmentSameDayBookings) {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), timeFormatter);
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), timeFormatter);

                // Add 10-minute buffer window around existing booking slots
                LocalTime bufferedStart = existingStart.minusMinutes(10);
                LocalTime bufferedEnd = existingEnd.plusMinutes(10);

                // Check if the new time range overlaps with (existing slot ± 10 min)
                // Two time ranges [A, B] and [C, D] overlap if A < D and B > C
                if (newStart.isBefore(bufferedEnd) && newEnd.isAfter(bufferedStart)) {
                    return "Time overlap in bookings. Existing booking time: " + existingStart.toString() + " - " + existingEnd.toString(); // Less than 10 minutes apart or overlapped
                }
            }

            Booking b = new Booking(bDate, startTime, endTime, purpose, researcher, equipment);
            em.persist(b);
            em.getTransaction().commit();

            return "Success";
        }
        catch (Exception ex)
        {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }


    }

    public String getAllBookings()
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Booking> query = em.createQuery(
                    "SELECT b FROM Booking b",
                    Booking.class);
            List<Booking> bookings = query.getResultList();

            if (bookings.isEmpty()) return "No bookings found";

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            StringBuilder upcomingSb = new StringBuilder();
            StringBuilder pastSb = new StringBuilder();

            for (Booking b : bookings) {
                LocalDate bDate = LocalDate.parse(b.getDate(), dateFormatter);
                LocalTime bEnd = LocalTime.parse(b.getEndTime(), timeFormatter);

                if (bDate.isAfter(today) || (bDate.isEqual(today) && bEnd.isAfter(now))) {
                    upcomingSb.append("  - ").append(b.toString()).append("\n");
                } else {
                    pastSb.append("  - ").append(b.toString()).append("\n");
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("--- UPCOMING BOOKINGS ---\n");
            sb.append(upcomingSb.length() == 0 ? "  No upcoming bookings.\n" : upcomingSb);
            sb.append("\n--- PAST BOOKINGS ---\n");
            sb.append(pastSb.length() == 0 ? "  No past bookings.\n" : pastSb);

            return sb.toString();


        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }
    }

    public String searchResearcher(Long rID)
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Researcher> query = em.createQuery(
                    "SELECT r FROM Researcher r WHERE r.rID = :rID",
                    Researcher.class).setParameter("rID", rID);
            List<Researcher> researchers = query.getResultList();

            StringBuilder sb = new StringBuilder();

            for(Researcher r : researchers)
            {
                sb.append(r.toString()).append("\n");

                List<Booking> bookings = r.getBookings();

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
                LocalDate today = LocalDate.now();
                for(Booking b : bookings)
                {
                    LocalDate bookingDate = LocalDate.parse(b.getDate(), dateFormatter);

                    if (!bookingDate.isBefore(today)) {
                        sb.append(b.toString()).append("\n");
                    }
                }

            }




            String resultText = sb.toString();
            return resultText.isEmpty() ? "No researchers found" : resultText;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }
    }

    public String searchResearcher(Long rID, String name, String department, String email)
    {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Researcher> query = em.createQuery(
                    "SELECT r FROM Researcher r WHERE " +
                    "(:rID IS NULL OR r.rID = :rID) AND " +
                    "(:name IS NULL OR LOWER(r.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
                    "(:dept IS NULL OR LOWER(r.department) LIKE LOWER(CONCAT('%', :dept, '%'))) AND " +
                    "(:email IS NULL OR LOWER(r.email) LIKE LOWER(CONCAT('%', :email, '%')))",
                    Researcher.class);
            query.setParameter("rID", rID);
            query.setParameter("name", name.isEmpty() ? null : name);
            query.setParameter("dept", department.isEmpty() ? null : department);
            query.setParameter("email", email.isEmpty() ? null : email);

            List<Researcher> results = query.getResultList();
            if (results.isEmpty()) return "No researchers found";

            StringBuilder sb = new StringBuilder();
            for (Researcher r : results) sb.append(r.toString()).append("\n");
            return sb.toString();
        } finally {
            em.close();
        }
    }

    public boolean updateBooking(Long bID, String bD, String sT, String eD, String purpose, Long rID, Long eID)
    {

        // spec is also weird for this
        // Save Booking in a variable.
        // Cancel, try recreate
        // if fail then put old booking and return error message
        // must have valid start/end time
EntityManager em = emf.createEntityManager();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    try {
        em.getTransaction().begin();

        Booking b = em.find(Booking.class, bID);
        if (b == null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return false;
        }
        Researcher researcher = em.find(Researcher.class, rID);
        if (researcher == null) {
            em.getTransaction().rollback();
            return false;
        }

        Equipment equipment = em.find(Equipment.class, eID);
        if (equipment == null || !"Available".equalsIgnoreCase(equipment.getStatus())) {
            em.getTransaction().rollback();
            return false;
        }

        LocalDate newDate = LocalDate.parse(bD, dateFormatter);
        LocalTime newStart = LocalTime.parse(sT, timeFormatter);
        LocalTime newEnd = LocalTime.parse(eD, timeFormatter);

        TypedQuery<Booking> resBookingsQuery = em.createQuery(
            "SELECT b FROM Booking b WHERE b.researcher.rID = :rID AND b.bID <> :bID", Booking.class)
            .setParameter("rID", rID)
            .setParameter("bID", bID);
        List<Booking> rBookings = resBookingsQuery.getResultList();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        long activeCount = 0;

        for (Booking existing : rBookings) {
            LocalDate bDateParsed = LocalDate.parse(existing.getDate(), dateFormatter);
            LocalTime bEndParsed = LocalTime.parse(existing.getEndTime(), timeFormatter);

            if (bDateParsed.isAfter(today) || (bDateParsed.isEqual(today) && bEndParsed.isAfter(now))) {
                if (existing.getEquipment().getrID().equals(eID) && bDateParsed.isEqual(newDate)) {
                    em.getTransaction().rollback();
                    return false; // Already booked this equipment on the same day
                }
                activeCount++;
            }
        }

        if (activeCount >= 3) {
            em.getTransaction().rollback();
            return false; // Exceeds 3 active bookings limit
        }

        TypedQuery<Booking> eqBookingsQuery = em.createQuery(
            "SELECT b FROM Booking b WHERE b.equipment.eID = :eID AND b.date = :bD AND b.bID <> :bID", Booking.class)
            .setParameter("eID", eID)
            .setParameter("bD", bD)
            .setParameter("bID", bID);
        List<Booking> equipmentSameDayBookings = eqBookingsQuery.getResultList();

        for (Booking existing : equipmentSameDayBookings) {
            LocalTime existingStart = LocalTime.parse(existing.getStartTime(), timeFormatter);
            LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), timeFormatter);

            LocalTime bufferedStart = existingStart.minusMinutes(10);
            LocalTime bufferedEnd = existingEnd.plusMinutes(10);

            if (newStart.isBefore(bufferedEnd) && newEnd.isAfter(bufferedStart)) {
                em.getTransaction().rollback();
                return false; // Time conflict / buffer overlap
            }
        }

        // Perform safe update
        b.setDate(bD);
        b.setStartTime(sT);
        b.setEndTime(eD);
        b.setPurpose(purpose);
        b.setResearcher(researcher);
        b.setEquipment(equipment);

        em.getTransaction().commit();
        return true;

    } catch (Exception ex) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        ex.printStackTrace();
        return false;
    } finally {
        em.close();
    }
    }

    public boolean cancelBooking(Long bID)
    {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bID);
            if (b != null) {
                em.remove(b);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }

    }

    public String getEquipmentSummary()
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT e FROM Equipment e",
                    Equipment.class);
            List<Equipment> equipments= query.getResultList();


            Double totalCost = 0.0;
            for(Equipment e:equipments)
            {
                totalCost += e.getReplacementCost();
            }

            String resultText = totalCost.toString();
            return resultText.isEmpty() ? "No summary available" : "Equipment Summary: " + resultText;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }
    }

    public String getAllEquipmentOutput()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT e FROM Equipment e",
                    Equipment.class);
            List<Equipment> equipments= query.getResultList();

            StringBuilder sb = new StringBuilder();

            for(Equipment e:equipments)
            {
                sb.append(e.toString()).append("\n");
            }

            String resultText = sb.toString();
            return resultText.isEmpty() ? "No Equipment found" : resultText;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }
    }

    public String getAvailableEquipment()
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT e FROM Equipment e",
                    Equipment.class);
            List<Equipment> equipments= query.getResultList();

            StringBuilder sb = new StringBuilder();

            for(Equipment e:equipments)
            {
                if (e.getStatus().equals("Available"))
                {
                    sb.append(e.toString()).append("\n");
                }
            }

            String resultText = sb.toString();
            return resultText.isEmpty() ? "No Equipment found" : resultText;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }
    }

    public String getResearcherBookings(Long rID)
    {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Booking> query = em.createQuery(
                    "SELECT b FROM Booking b WHERE b.researcher.rID = :rID", Booking.class);
            query.setParameter("rID", rID);
            List<Booking> list = query.getResultList();
            if (list.isEmpty()) return "No bookings found for researcher";

            StringBuilder sb = new StringBuilder();
            for (Booking b : list) sb.append(b.toString()).append("\n");
            return sb.toString();
        } finally {
            em.close();
        }
    }

    public String getUnusedEquipment()
    {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Equipment> allEqQuery = em.createQuery("SELECT e FROM Equipment e", Equipment.class);
            List<Equipment> allEquipment = allEqQuery.getResultList();

            TypedQuery<Equipment> bookedEqQuery = em.createQuery("SELECT DISTINCT b.equipment FROM Equipment_Bookings_Subquery", Equipment.class); 
            TypedQuery<Equipment> bookedQuery = em.createQuery("SELECT DISTINCT b.equipment FROM Booking b", Equipment.class);
            List<Equipment> bookedEquipment = bookedQuery.getResultList();

            allEquipment.removeAll(bookedEquipment);

            if (allEquipment.isEmpty()) return "No unused equipment found";

            StringBuilder sb = new StringBuilder();
            for (Equipment e : allEquipment) {
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error";
        } finally {
            em.close();
        }
    }

    public String getHighestBookingResearchers()
    {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Researcher> query = em.createQuery(
                    "SELECT r FROM Researcher r LEFT JOIN r.bookings b GROUP BY r ORDER BY COUNT(b) DESC", 
                    Researcher.class
                    );
            query.setMaxResults(1);
            List<Researcher> list = query.getResultList();

            if (list.isEmpty()) {
                return "No researchers found";
            }

            Researcher topResearcher = list.get(0);
            StringBuilder sb = new StringBuilder();
            sb.append(topResearcher.toString()).append("\n");

            List<Booking> bookings = topResearcher.getBookings();
            if (bookings.isEmpty()) {
                sb.append("  No bookings found for this researcher.\n");
            } else {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();

                StringBuilder upcomingSb = new StringBuilder();
                StringBuilder pastSb = new StringBuilder();

                for (Booking b : bookings) {
                    LocalDate bDate = LocalDate.parse(b.getDate(), dateFormatter);
                    LocalTime bEnd = LocalTime.parse(b.getEndTime(), timeFormatter);

                    // Categorize into Upcoming vs Past
                    if (bDate.isAfter(today) || (bDate.isEqual(today) && bEnd.isAfter(now))) {
                        upcomingSb.append("  - ").append(b.toString()).append("\n");
                    } else {
                        pastSb.append("  - ").append(b.toString()).append("\n");
                    }
                }

                // Build output section for UPCOMING BOOKINGS
                sb.append("\n  --- UPCOMING BOOKINGS ---\n");
                if (upcomingSb.length() == 0) {
                    sb.append("    No upcoming bookings.\n");
                } else {
                    sb.append(upcomingSb);
                }

                // Build output section for PAST BOOKINGS
                sb.append("\n  --- PAST BOOKINGS ---\n");
                if (pastSb.length() == 0) {
                    sb.append("    No past bookings.\n");
                } else {
                    sb.append(pastSb);
                }
            }

            return sb.toString();
        } finally {
            em.close();
        }
    }

    public String getAllResearcherOutput()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            TypedQuery<Researcher> query = em.createQuery(
                    "SELECT r FROM Researcher r",
                    Researcher.class);
            List<Researcher> researchers = query.getResultList();

            StringBuilder sb = new StringBuilder();

            for(Researcher r : researchers)
            {
                sb.append(r.toString()).append("\n");
            }

            String resultText = sb.toString();
            return resultText.isEmpty() ? "No researchers found" : resultText;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            return "Error";

        }
        finally
        {
            em.close();
        }


    }
public String searchEquipment(Long eID) {
    EntityManager em = emf.createEntityManager();
    try {
        Equipment eq = em.find(Equipment.class, eID);
        if (eq == null) return "Equipment with ID " + eID + " not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(eq.toString()).append("\n");

        TypedQuery<Booking> query = em.createQuery(
            "SELECT b FROM Booking b WHERE b.equipment.eID = :eID", Booking.class);
        query.setParameter("eID", eID);
        List<Booking> bookings = query.getResultList();

        if (bookings.isEmpty()) {
            sb.append("  No bookings found for this equipment.\n");
        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            StringBuilder upcomingSb = new StringBuilder();
            StringBuilder pastSb = new StringBuilder();

            for (Booking b : bookings) {
                LocalDate bDate = LocalDate.parse(b.getDate(), dateFormatter);
                LocalTime bEnd = LocalTime.parse(b.getEndTime(), timeFormatter);

                if (bDate.isAfter(today) || (bDate.isEqual(today) && bEnd.isAfter(now))) {
                    upcomingSb.append("  - ").append(b.toString()).append("\n");
                } else {
                    pastSb.append("  - ").append(b.toString()).append("\n");
                }
            }

            sb.append("\n  --- UPCOMING BOOKINGS ---\n");
            sb.append(upcomingSb.length() == 0 ? "    No upcoming bookings.\n" : upcomingSb);
            sb.append("\n  --- PAST BOOKINGS ---\n");
            sb.append(pastSb.length() == 0 ? "    No past bookings.\n" : pastSb);
        }

        return sb.toString();
    } catch (Exception ex) {
        ex.printStackTrace();
        return "Error searching equipment";
    } finally {
        em.close();
    }
}







}
