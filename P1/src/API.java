
import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;


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
                        "2026/08/" + String.format("%02d", i),
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
                        "2026/08/" + String.format("%02d", (i % 25) + 1),
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

    public boolean createBooking()
    {
        // equipment marked as out of service may not be booked
        // must have valid start/end time
        // researcher may not have more than 3 active bookings
        // Must check for conflicting equipment bookings
        // researcher may not create multiple bookings for the same equipment on the same day
        // bookings must be made for present or future
        return false;
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
        return "None";
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


    public boolean testRegister()
    {

        EntityManager em = emf.createEntityManager();
        try
        {

        em.getTransaction().begin();
        Researcher r = new Researcher("John Doe", "EBIT", "ejd@gmail.com");
        em.persist(r);

        Equipment e = new Equipment("Computa", "Electronic", "03/08/2026", 1000000, "Broken");
        em.persist(e);

        Booking b = new Booking("04/08/2026", "08:00", "17:00", "Typing", r, e);
        em.persist(b);


        em.getTransaction().commit();
        } catch (Exception ex)
        {
            if(em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
            ex.printStackTrace();
            return false;
        } finally 
        {
            em.close();
        }
        return true;
    }


    


}
