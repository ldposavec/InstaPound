package hr.algebra.nrako.instapound.enums;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.Data;
//import lombok.NoArgsConstructor;

//@Entity
//@Data
//@NoArgsConstructor
//@Table(name = "ROLES")
//public class UserRole {
//
//    @Id
//    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
//    private Long id;
//    private String roleName;
//
//    public UserRole(String roleName) {
//        this.roleName = roleName;
//    }
//}
public enum UserRole {
    ANONYMOUS,
    REGISTERED,
    ADMIN
}