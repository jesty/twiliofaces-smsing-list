package org.twiliofaces.smsinglist.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MsgIn implements Serializable
{

   private static final long serialVersionUID = 1L;
   private Long id;
   private String from;
   private String to;
   private String txt;
   private Date insertDate;

   public MsgIn()
   {
   }

   public MsgIn(String from, String to, String txt)
   {
      this.from = from;
      this.to = to;
      this.txt = txt;
      this.insertDate = new Date();
   }

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   @Column(name = "FROMNUMBER")
   public String getFrom()
   {
      return from;
   }

   public void setFrom(String from)
   {
      this.from = from;
   }

   @Column(name = "TONUMBER")
   public String getTo()
   {
      return to;
   }

   public void setTo(String to)
   {
      this.to = to;
   }

   public String getTxt()
   {
      return txt;
   }

   public void setTxt(String txt)
   {
      this.txt = txt;
   }

   public Date getInsertDate()
   {
      return insertDate;
   }

   public void setInsertDate(Date insertDate)
   {
      this.insertDate = insertDate;
   }

   @Override
   public String toString()
   {
      return "MsgIn [id=" + id + ", from=" + from + ", txt=" + txt + ", insertDate=" + insertDate + "]";
   }

}
