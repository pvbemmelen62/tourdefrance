package nl.xs4all.pvbemmel.letour;

class Cyclist {
  private String firstName;
  private String lastName;
  private String country;
  private int number;

  
  public Cyclist() {
  }
  public Cyclist(int number, String firstName, String lastName,
      String country) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.country = country;
    this.number = number;
  }
  public String getFirstName() {
    return firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public String getName() {
    return firstName + " " + lastName;
  }
  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public int getNumber() {
    return number;
  }
  public void setNumber(int number) {
    this.number = number;
  }

  public String toString() {
    String rv = String.format("[%4d, %s, %s, (%s)]", number, firstName,
        lastName, country);
    return rv;
  }
  
  public static class Test {
    public static void main(String[] args) {
      Cyclist c = new Cyclist(1, "Paul", "van Bemmelen", "Ned");
      System.out.println(c);
      System.out.flush();
    }
  }
}