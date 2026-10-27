package bloodbank.model;

public class Hospital {

    private int hospitalId;
    private String hospitalName;
    private String licenseNumber;
    private String address;
    private String city;
    private String contactPerson;
    private String phoneNumber;
    private String email;

    public Hospital() {
    }

    public Hospital(
            int hospitalId,
            String hospitalName,
            String licenseNumber,
            String address,
            String city,
            String contactPerson,
            String phoneNumber,
            String email
    ) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.city = city;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Hospital(
            String hospitalName,
            String licenseNumber,
            String address,
            String city,
            String contactPerson,
            String phoneNumber,
            String email
    ) {
        this.hospitalName = hospitalName;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.city = city;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}