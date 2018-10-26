package ga.nikhilkumar.whatsappsender.sender.model;

public class WContact {

    String name;
    String id;

    public WContact(String name, String id) {
        this.name = name;
        this.id = id;


        if (!id.endsWith("@s.whatsapp.net"))
            throw new IllegalArgumentException("ID for Whatsapp contact should ends with : @s.whatsapp.net , for example 9999999999@s.whatsapp.net");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WContact contact = (WContact) o;

        if (name != null ? !name.equals(contact.name) : contact.name != null) return false;
        return id != null ? id.equals(contact.id) : contact.id == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WContact{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
