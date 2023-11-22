import org.casbin.jcasbin.main.*;

public class Main {
    public static void main(String[] args) {
        Enforcer enforcer = new Enforcer("basic_model.conf", "basic_policy.csv");
        String sub = "alice"; // the user that wants to access a resource.
        String obj = "data1"; // the resource that is going to be accessed.
        String act = "read"; // the operation that the user performs on the resource.

        if (enforcer.enforce(sub, obj, act) == true) {
            // permit alice to read data1
        } else {
            // deny the request, show an error
        }
    }
}