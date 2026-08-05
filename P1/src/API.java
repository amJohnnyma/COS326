
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
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            // keep references for making bookings later
            List<Researcher> researchers = new ArrayList<>();
            List<Equipment> equipment = new ArrayList<>();

            //create 10 Researchers
            for (int i = 1; i <= 10; i ++)
            {
                Researcher r = new Researcher(
                        "Researcher: " + i,
                        "Department " + ((i % 3) + 1),
                        "researcher" + i + "@up.ac.za"
                        );
                em.persist(r);
                researchers.add(r);
            }

            //create 30 Equipment
            for (int i = 1; i <= 30; i ++)
            {
                Equipment r = new Equipment(
                        "Equipment: " + i,
                        "Category" + ((i % 4) + 1),
                        String.format("%02d", i)+ "/08/2026"  ,
                        500.0 * i,
                        (i % 5 == 0) ? "Out of service" : "Available"
                        );
                em.persist(r);
                equipment.add(r);
            }

            // create 50 bookings
            for (int i = 1; i <= 50; i ++)
            {
                Researcher r = researchers.get(i % researchers.size());
                Equipment e = equipment.get(i % equipment.size());

                Booking b = new Booking(
                        String.format("%02d", (i % 25) + 1) + "/08/2026"  ,
                        "08:00",
                        "12:00",
                        "Research Project " + i,
                        r,
                        e
                        );

                if (r.getBookings() != null) r.getBookings().add(b);
                if (e.getBookings() != null) e.getBookings().add(b);

                em.persist(b);
            }

            em.getTransaction().commit();
        }
        catch(Exception ex)
        {
            if(em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            ex.printStackTrace();
        }
        finally
        {
            em.close();
        }
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
                    "SELECT b FROM Booking b WHERE b.equipment.eID = :eID AND b.bookingDate = :bDate", Booking.class)
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

            StringBuilder sb = new StringBuilder();

            for(Booking b: bookings)
            {
                sb.append(b.toString()).append("\n");
            }

            String resultText = sb.toString();
            return resultText.isEmpty() ? "No bookings found" : resultText;

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

                for(Booking b : bookings)
                {
                    sb.append(b.toString()).append("\n");
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
        return "None";
    }

    public boolean updateBooking(Long bID)
    {
        // cannot change equipment or researcher
        // must have valid start/end time
        return false;
    }

    public boolean cancelBooking(Long bID)
    {
        return false;
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
        return "None";
    }

    public String getUnusedEquipment()
    {
        return "None";
    }

    public String getHighestBookingResearchers()
    {
        return "None";
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







}
