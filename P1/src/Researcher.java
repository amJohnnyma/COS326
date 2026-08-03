
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Researcher implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue
    private Long rID;

    private String fullName;
    private String department;
    private String email;

    @OneToMany(mappedBy = "researcher", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    public Researcher() {}

    public Researcher(String fullName, String department, String email) {
        this.fullName = fullName;
        this.department = department;
        this.email = email;
    }

    public List<Booking> getBookings()
    {
        return bookings;
    }

    public void addBooking(Booking booking)
    {
    }

    public void removeBooking(Long bID)
    {
    }

    public Long getrID() {
        return rID;
    }

    public void setrID(Long rID) {
        this.rID = rID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public String toString() {
        return String.format("[ ID = %-4d | Name = %-20s | Dept = %-15s | Email = %s ]", 
                rID, fullName, department, email);
    }
}
