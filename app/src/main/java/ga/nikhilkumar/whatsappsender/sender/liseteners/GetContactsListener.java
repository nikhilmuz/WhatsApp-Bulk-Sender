package ga.nikhilkumar.whatsappsender.sender.liseteners;

import java.util.List;

import ga.nikhilkumar.whatsappsender.sender.model.WContact;

public interface GetContactsListener {

    void receiveWhatsappContacts(List<WContact> contacts);
}
