package scenario.integrita.user;

/**
 * contains user-related information.
 */
public class User {
  public Integer id;
  public byte[] verificationKey;

  public User(Integer id) {
    this.id = id;
  }

  public User() {
  }
}
