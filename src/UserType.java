public Class UserType
{   
    private String username;
    private String password;
    private boolean verification;
    
    private UserType(String username, String password)
    {
        this.username = username;
        this.password = password;
        verification = false;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getPassword()
    {
        return password;
    }

    public boolean getVerification(){
        return verification;
    }

    public int Authenticate(){
        File accounts = new File(System.getProperty("user.dir"), "users.txt");				 
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(accounts));
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String account;
        try {
            while ((account = br.readLine()) != null) {	
                String[] accArr = account.split(",");	
                if(username.equals(accArr[0])) {
                    if(password.equals(accArr[1])) {
                        verification = true;
                        return Integer.parseInt(accArr[2]);
                    } 
                    else
                        return -1;
                }
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }		
        return -1;
    }
}
