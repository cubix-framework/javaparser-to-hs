import java.io.*;

import japa.parser.*;
import japa.parser.ast.*;

public class JavaparserToHS {
    public static void main(String[] args) throws Exception {
        String infile = args[0];
        String outfile = args[1];

        FileInputStream in = new FileInputStream(infile);
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));
        
        try {
            CompilationUnit cu = JavaParser.parse(in);
            cu.accept(new ToDeriveReadVisitor(out), null);
        } finally {
            in.close();
            out.close();
        }
    }
}