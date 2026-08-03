import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue
    private Long bID;

    private String date;
    private String startTime;
    private String endTime;
    private String purpose;


    //JPA relationship annotations
    // Many bookings can be made by one researcher
    @ManyToOne
    private Researcher researcher;

    // Many bookings can book one equipment
    @ManyToOne
    private Equipment equipment;

    public Booking() {}

    public Booking(String date, String startTime, String endTime, String purpose, Researcher researcher, Equipment equipment) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.researcher = researcher;
        this.equipment = equipment;
    }

    public Researcher getResearcher() {
        return researcher;
    }

    public void setResearcher(Researcher researcher) {
        this.researcher = researcher;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Long getbID() {
        return bID;
    }

    public void setbID(Long bID) {
        this.bID = bID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public String toString() {
        return String.format("Booking [bID=%d, date='%s', startTime='%s', endTime='%s', purpose='%s']",
                bID, date, startTime, endTime, purpose);
    }
}
