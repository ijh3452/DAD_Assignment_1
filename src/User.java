import java.io.Serializable;

public class User implements Serializable
{
    private String id;
    private String password;
    private String nickname;
    private String usertype;
    
    User(String id, String password, String nickname, String type)
    {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.usertype = type;
    }
    
    public String getID()
    {
        return id;
    }
    
    public String getNickname()
    {
        return nickname;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void setUsertype(String type)
    {
        this.usertype = type;
    }
    
    public void setID(String id)
    {
        this.id = id;
    }
    
    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
}
