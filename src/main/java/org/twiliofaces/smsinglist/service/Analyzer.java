package org.twiliofaces.smsinglist.service;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.twiliofaces.smsinglist.jms.operation.SendMessage2SmsSenderMDB;
import org.twiliofaces.smsinglist.model.MsgIn;
import org.twiliofaces.smsinglist.model.MsgOut;
import org.twiliofaces.smsinglist.model.Sms;
import org.twiliofaces.smsinglist.model.User;
import org.twiliofaces.smsinglist.model.enums.CommandsEnum;
import org.twiliofaces.smsinglist.repository.MsgInRepository;
import org.twiliofaces.smsinglist.repository.UserRepository;
import org.twiliofaces.smsinglist.util.MsgUtils;
import org.twiliofaces.smsinglist.util.ParserUtils;
import org.twiliofaces.smsinglist.util.SmsUtils;

@Stateless
@LocalBean
public class Analyzer
{
   @Inject
   UserRepository userRepository;

   @Inject
   MsgInRepository msgInRepository;

   @Asynchronous
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void checkSms(Sms sms)
   {
      MsgIn msgIn = SmsUtils.toMsgIn(sms);
      // know i the number?
      User user = userRepository.findByNumber(sms.getFrom());
      MsgOut msgOut, msgOutN;
      // YES
      if (user != null)
      {
         msgInRepository.persist_withNewTx(msgIn);
         // I KNOW THE USER
         CommandsEnum commandInside = ParserUtils.containsCommand(msgIn.getTxt());
         switch (commandInside)
         {
         case ALL:
            List<String> nicknames = userRepository.getAllNicknamesNotIn(sms.getFrom());
            msgOut = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.all(nicknames),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOut);
            break;
         case CHANGE:
            String newNickname = ParserUtils.getNickname(sms.getBody());
            user.setNickname(newNickname);
            userRepository.change(user);
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.change(),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         case HOWTO:
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.help(),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         case INVITE:
            // prendo il numero e lo invito: dicendo che è nickname che lo invita a iscriversi
            String newNumber = ParserUtils.getInviteNumber(sms.getBody());
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.invite(newNumber,
                     user.getNickname()),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         case NONE:
            List<String> numbers = userRepository.getNumbersNotIn(sms.getFrom());
            msgOut = new MsgOut(numbers, MsgUtils.said(user, msgIn.getTxt()), msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOut);
            break;
         case PAUSE:
            userRepository.pause(user);
            break;
         case PRIV:
            // devo conoscere il nickname
            // devo prendere il msg dopo il nickname
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.comingsoon(),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         case UNPAUSE:
            userRepository.unpause(user);
            break;
         case LEAVE:
            userRepository.unsubscribe(user);
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.bye(user),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         default:
            System.out.println("ERRORE OLD USER - SMS: " + sms.toString());
            System.out.println("ERRORE OLD USER - MSGIN: " + msgIn.toString());
            break;

         }
      }
      // NO!!!
      else
      {
         // I DON'T KNOW THE USER
         System.out.println("NEW USER");
         CommandsEnum commandInside = ParserUtils.containsCommand(msgIn.getTxt());
         System.out.println(commandInside);
         switch (commandInside)
         {
         case HOWTO:
            msgInRepository.persist_withNewTx(msgIn);
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.help(),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         case SUBSCRIBE:
            msgInRepository.persist_withNewTx(msgIn);
            // AGGIUNGO NUOVO NUMERO E INVIO MSG CONFERMA
            String nickname = ParserUtils.getNickname(sms.getBody());
            User userN = new User(sms.getFrom(), nickname);
            userRepository.persist_withNewTx(userN);
            msgOutN = new MsgOut(Arrays.asList(new String[] { sms.getFrom() }), MsgUtils.welcome(userN),
                     msgIn.getId());
            SendMessage2SmsSenderMDB.execute(msgOutN);
            break;
         default:
            System.out.println("ERRORE NEW USER - SMS: " + sms.toString());
            System.out.println("ERRORE NEW USER - MSGIN: " + msgIn.toString());
            break;
         }
      }

   }
}
