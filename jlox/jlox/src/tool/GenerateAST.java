package tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: generate_ast <output directory>.");
            System.exit(64);
        }
        String outputDir = args.length == 1 ? args[0] : dirFromPrompt();

        defineAST(outputDir, "Expr", Arrays.asList(
                "Assign: Token name, Expr value",
                "Binary: Expr left, Token operator, Expr right",
                "Grouping: Expr expression",
                "Unary: Token operator, Expr right",
                "Literal: Object value",
                "Variable: Token name"
        ));

        defineAST(outputDir, "Stmt", Arrays.asList(
                "Block: List<Stmt> statements",
                "Expression: Expr expression",
                "Print: Expr expression",
                "Var: Token name, Expr initializer"
        ));
    }

    public static String dirFromPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        System.out.print("> ");
        return reader.readLine();
    }

    public static void defineAST(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        writer.println();

        writer.println("    " + "abstract <T> T accept(Visitor<T> visitor);");

        writer.println();

        for (String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    public static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println("    " + "static class " + className + " extends " + baseName + " {");

        String[] fieldList = fields.split(", ");

        for (String field: fieldList) {
            writer.println("        " + "final " + field + ";");
        }

        writer.println();

        writer.println("        " + className + "(" + fields + ")" + " {");
        for (String field: fieldList) {
            String name = field.split(" ")[1];
            writer.println("            " + "this." + name + " = " + name + ";");
        }
        writer.println("        }");

        writer.println();

        writer.println("        " + "@Override");
        writer.println("        " + "<T> T accept(Visitor<T> visitor) {");
        writer.println("            " + "return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    " + "interface Visitor<T> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        " + "T visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }
}
