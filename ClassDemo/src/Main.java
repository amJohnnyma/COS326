import javax.persistence.*;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        // Open database connection (creates points.odb file if it doesn't exist)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("points.odb");
        EntityManager em = emf.createEntityManager();

        // Store 10 Point objects
        em.getTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Point p = new Point(i, i * 2);
            em.persist(p);
        }
        em.getTransaction().commit();

        // Run JPQL queries
        Query q1 = em.createQuery("SELECT COUNT(p) FROM Point p");
        System.out.println("Total Points: " + q1.getSingleResult());

        Query q2 = em.createQuery("SELECT AVG(p.x) FROM Point p");
        System.out.println("Average X: " + q2.getSingleResult());

        // Retrieve and display objects
        TypedQuery<Point> query = em.createQuery("SELECT p FROM Point p", Point.class);
        List<Point> results = query.getResultList();
        for (Point p : results) {
            System.out.println(p);
        }

        em.close();
        emf.close();
    }
}
