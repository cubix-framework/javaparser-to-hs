import java.io.*;

import japa.parser.*;
import japa.parser.ast.*;

public class CheckParses {
    public static void main(String[] args) throws Exception {
        String infile = args[0];

        FileInputStream in = new FileInputStream(infile);
        
        try {
            CompilationUnit cu = JavaParser.parse(in);
        } catch(Exception e) {
            System.exit(1);
        } finally {
            in.close();
        }
    }
}