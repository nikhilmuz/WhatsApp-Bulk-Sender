package ga.nikhilkumar.whatsappsender.sender.liseteners;

import java.util.List;

import ga.nikhilkumar.whatsappsender.sender.model.WContact;
import ga.nikhilkumar.whatsappsender.sender.model.WMessage;

public interface SendMessageListener {
    void finishSendWMessage(List<WContact> contact, List<WMessage> messages);
}
