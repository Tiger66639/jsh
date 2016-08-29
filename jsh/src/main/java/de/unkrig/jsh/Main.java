
/*
 * jsh - The Java Shell
 *
 * Copyright (c) 2016, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.jsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.ICompilerFactory;
import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.commons.compiler.samples.DemoBase;
import org.codehaus.commons.nullanalysis.Nullable;

/**
 * A test program that allows you to play around with the
 * {@link org.codehaus.janino.ScriptEvaluator ScriptEvaluator} class.
 */
public final
class Main extends DemoBase {

    private Main() {}

    /***/
    public static void
    main(String[] args) throws Exception {

        Class<?>       returnType        = void.class;
        List<String>   parameterNames    = new ArrayList<String>();
        List<Class<?>> parameterTypes    = new ArrayList<Class<?>>();
        List<Class<?>> thrownExceptions  = new ArrayList<Class<?>>();
        List<String>   defaultImports    = new ArrayList<String>();
        String         optionalEncoding  = null;
        String         compilerFactoryId = null;

        defaultImports.add("java.io.*");

        int i;
        for (i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (!arg.startsWith("-")) break;
            if ("--return-type".equals(arg)) {
                returnType = DemoBase.stringToType(args[++i]);
            } else
            if ("--parameter".equals(arg)) {
                parameterTypes.add(DemoBase.stringToType(args[++i]));
                parameterNames.add(args[++i]);
            } else
            if ("--thrown-exception".equals(arg)) {
                thrownExceptions.add(DemoBase.stringToType(args[++i]));
            } else
            if ("--default-import".equals(arg)) {
                defaultImports.add(args[++i]);
            } else
            if ("--encoding".equals(arg)) {
                optionalEncoding = args[++i];
            } else
            if ("--compiler-factory".equals(arg)) {
                compilerFactoryId = args[++i];
            } else
           if ("--help".equals(arg)) {
               System.err.println("The \"Java shell\".");
               System.err.println();
               System.err.println("Usage as an interactive shell:");
               System.err.println("  Jsh { <option> }");
               System.err.println("Valid <option>s are:");
               System.err.println(" --thrown-exception <exception-type> (multiple allowed)");
               System.err.println(" --default-import <imports>          (multiple allowed)");
               System.err.println(" --encoding <encoding>");
               System.err.println(" --help");
               System.err.println();
               System.err.println("Usage for executing a script file (containing Java code):");
               System.err.println("  Jsh { <option> } <script-file> { <argument> }");
               System.err.println("Valid <option>s are thos described above, plus:");
               System.err.println(" --return-type <return-type>         (default: void)");
               System.err.println(" --parameter <type> <name>           (multiple allowed)");
               System.err.println(" --compiler-factory <id>             (One of " + Arrays.toString(CompilerFactoryFactory.getAllCompilerFactories()) + ")"); // SUPPRESS CHECKSTYLE LineLength
               System.err.println("If no \"--parameter\"s are specified, then the <argument>s are passed as a");
               System.err.println("single parameter \"String[] args\". Otherwise, the number of <argument>s must");
               System.err.println("exactly match the number of parameters, and each <argument> is converted to");
               System.err.println("the respective parameter's type.");
               System.err.println("Iff the return type is not \"void\", then the return value is printed to STDOUT");
               System.err.println("after the script completes.");
               return;
           } else
           {
               System.err.println("Invalid command line option \"" + arg + "\"; try \"--help\".");
               System.exit(1);
           }
        }

        // Now check if we want INTERACTIVE MODE.
        if (i == args.length) {
            if (compilerFactoryId != null) {
                System.err.println("Compiler factory by cannot be set if reading from STDIN");
                System.exit(1);
            }
            if (returnType != void.class) {
                System.err.println("Return type not possible if reading from STDIN");
                System.exit(1);
            }
            if (!parameterTypes.isEmpty()) {
                System.err.println("Parameters are not possible if the script is read from STDIN");
                System.exit(1);
            }

            InteractiveShell.run(
                defaultImports.toArray(new String[defaultImports.size()]),
                thrownExceptions.toArray(new Class<?>[thrownExceptions.size()]),
                optionalEncoding
            );
        } else {

            final File scriptFile = new File(args[i++]);

            Main.executeScriptFile(
                compilerFactoryId,
                scriptFile,
                optionalEncoding,
                defaultImports,
                returnType,
                parameterNames,
                parameterTypes,
                thrownExceptions,
                Arrays.copyOfRange(args, i, args.length) // args
            );
        }
    }

    private static void
    executeScriptFile(
        @Nullable String compilerFactoryId,
        File             scriptFile,
        @Nullable String optionalEncoding,
        List<String>     defaultImports,
        Class<?>         returnType,
        List<String>     parameterNames,
        List<Class<?>>   parameterTypes,
        List<Class<?>>   thrownExceptions,
        String[]         args
    ) throws Exception {

        ICompilerFactory compilerFactory;
        COMPILER_FACTORY_BY_ID:
        if (compilerFactoryId != null) {
            for (ICompilerFactory cf : CompilerFactoryFactory.getAllCompilerFactories()) {
                if (cf.getId().equals(compilerFactoryId)) {
                    compilerFactory = cf;
                    break COMPILER_FACTORY_BY_ID;
                }
            }
            System.err.println("Invalid compiler factory id \"" + compilerFactoryId + "\"; try \"--help\".");
            System.exit(1);
            return;
        } else {
            compilerFactory = CompilerFactoryFactory.getDefaultCompilerFactory();
        }

        IScriptEvaluator se = compilerFactory.newScriptEvaluator();

        se.setReturnType(returnType);
        se.setDefaultImports(defaultImports.toArray(new String[defaultImports.size()]));
        se.setThrownExceptions(thrownExceptions.toArray(new Class[thrownExceptions.size()]));

        Object[] arguments;
        if (parameterTypes.isEmpty()) {

            parameterTypes.add(String[].class);
            parameterNames.add("args");
            arguments = args;

            if (thrownExceptions.isEmpty()) thrownExceptions.add(Exception.class);
        } else {

            // One command line argument for each parameter.
            if (args.length != parameterTypes.size()) {
                System.err.println(
                    "Argument count ("
                    + args.length
                    + ") and parameter count ("
                    + parameterTypes.size()
                    + ") do not match; try \"--help\"."
                );
                System.exit(1);
            }

            // Convert command line arguments to call arguments.
            arguments = new Object[args.length];
            for (int j = 0; j < args.length; ++j) {
                arguments[j] = DemoBase.createObject(parameterTypes.get(j), args[j]);
            }
        }

        // Create and configure the "ScriptEvaluator" object.
        se.setParameters(
            parameterNames.toArray(new String[0]),
            parameterTypes.toArray(new Class[parameterTypes.size()])
        );
        se.setThrownExceptions(thrownExceptions.toArray(new Class[0]));

        // Scan, parse and compile the script file.
        InputStream is = new FileInputStream(scriptFile);
        try {

            se.cook(scriptFile.toString(), is, optionalEncoding);
            is.close();
        } finally {
            try { is.close(); } catch (Exception e) {}
        }

        // Evaluate script with actual parameter values.
        Object res = se.evaluate(arguments);

        // Print script return value.
        if (returnType != void.class) {
            System.out.println(res instanceof Object[] ? Arrays.toString((Object[]) res) : String.valueOf(res));
        }
    }
}
