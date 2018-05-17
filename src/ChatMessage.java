
public class ChatMessage
{
    private String IPSender;
    private String IPreceiver;
    private String message;
    private String datetime;
    private String nickname;
    
    public ChatMessage(){}
    
    public ChatMessage(String senderIP, String receiverIP, String message, String datetime, String nickname)
    {
        this.IPSender = senderIP;
        this.IPreceiver = receiverIP;
        this.message = message;
        this.datetime = datetime;
        this.nickname = nickname;
    }
    
    public String getSenderIP()
    {
        return IPSender;
    }
    
    public void setSenderIP(String senderIP)
    {
        this.IPSender = senderIP;
    }
    
    public String getReceiverIP()
    {
        return IPreceiver;
    }
    
    public void setReceiverIP(String receiverIP)
    {
        this.IPreceiver = receiverIP;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public String getDateTime()
    {
        return datetime;
    }
    
    public void setDateTime(String datetime)
    {
        this.datetime = datetime;
    }
    
    public String getNickname()
    {
        return nickname;
    }
    
    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
}