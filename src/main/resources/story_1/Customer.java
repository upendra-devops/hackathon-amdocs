import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String city;
    private String country;
    private String postalCode;
    private String phoneNumber;
    private String emailAddress;

    // Getters and setters
}