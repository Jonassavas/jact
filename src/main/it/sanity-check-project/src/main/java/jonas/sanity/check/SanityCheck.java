package jonas.sanity.check;

import java.io.DataInput;
import java.io.DataInputStream;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.function.Add;
import org.joda.time.LocalDate;
import com.fasterxml.jackson.core.format.DataFormatDetector;

import javax.xml.crypto.Data;

public class SanityCheck {

    

    public static void main(String[] args){
        // Testing plugin by adding math3 dependency calls
        Add add = new Add();
        double x = add.value(1,3);

        // Testing joda-time by adding dependency calls
        LocalDate currentDate = LocalDate.now();
        System.out.println(currentDate);

        System.out.println("Hello World");


        DataFormatDetector df = new DataFormatDetector();
        df.toString();

        ArrayList<Integer> dependencyTest = new ArrayList<>();

        for(int i = 0; i < 100; i++){
            dependencyTest.add(i);
        }

        System.out.println(dependencyTest.toString());
    }




}
