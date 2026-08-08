import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue
    private Long eID;

    private String name;
    private String category;
    private String purchaseDate;
    private double replacementCost;
    private String status;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    public Equipment() {}

    public Equipment(String name, String category, String purchaseDate, double replacementCost, String status) {
        this.name = name;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.replacementCost = replacementCost;
        this.status = status;

    }

    public List<Booking> getBookings()
    {
        return bookings;
    }

    public void addBooking(Booking booking)
    {
        if (booking != null && !this.bookings.contains(booking)) {
            this.bookings.add(booking);
            booking.setEquipment(this);
        }
    }

    public void removeBooking(Long bID)
    {
        if (bID != null) {
            this.bookings.removeIf(b -> bID.equals(b.getbID()));
        }
    }

    public Long getrID() {
        return eID;
    }

    public void setrID(Long eID) {
        this.eID = eID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(double replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Equipment [eID=%d, name='%s', category='%s', purchaseDate='%s', replacementCost=%.2f, status='%s']",
                eID, name, category, purchaseDate, replacementCost, status);
    }


}
