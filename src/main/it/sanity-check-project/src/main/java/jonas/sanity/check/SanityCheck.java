package jonas.sanity.check;

import java.util.ArrayList;
//import akka.http.*;
import org.apache.commons.math3.analysis.function.Add;
import org.apache.commons.math3.random.AbstractRandomGenerator;


public class SanityCheck {

    

    public static void main(String[] args){

        Add add = new Add();
        double x = add.value(1,3);

        AbstractRandomGenerator.class.getName();

        System.out.println("Hello World");

        ArrayList<Integer> dependencyTest = new ArrayList<>();

        for(int i = 0; i < 100; i++){
            dependencyTest.add(i);
        }

        System.out.println(dependencyTest.toString());
    }




}
