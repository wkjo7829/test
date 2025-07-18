package gradeProgram;

public class User {
    private String uId;
    private String uPw;
    private String uName;
    private String uRole;
    private int uGrade;
    private int uClass;

    public User(String uId, String uPw, String uName, String uRole, int uGrade, int uClass) {
        this.uId = uId;
        this.uPw = uPw;
        this.uName = uName;
        this.uRole = uRole;
        this.uGrade = uGrade;
        this.uClass = uClass;
    }

    public String getuId() { return uId; }
    public String getuPw() { return uPw; }
    public String getuName() { return uName; }
    public String getuRole() { return uRole; }
    public int getuGrade() { return uGrade; }
    public int getuClass() { return uClass; }
}
