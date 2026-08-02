import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Point implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue
    private Long id;

    private int x;
    private int y;

    public Point() {}

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return String.format("Point [id=%d, x=%d, y=%d]", id, x, y);
    }
}
