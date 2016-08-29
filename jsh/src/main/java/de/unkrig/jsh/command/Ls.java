
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

package de.unkrig.jsh.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Prints the name of files, or the names of the members of directories.
 */
public final
class Ls implements Cloneable {

    public static final long serialVersionUID = 1L;

    private boolean lonG;

//    public
//    Ls() {}
//
//    public
//    Ls(Ls that) {
//        this.lonG = that.lonG;
//    }

    /**
     * Activates "long" listing, where not only the
     * @return
     */
    public Ls
    l() { return this.l(true); }

    public Ls
    l(boolean value) { Ls result = this.clonE(); result.lonG = value; return result; }

    private Ls
    clonE() {
        try {
            return (Ls) super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }

    /**
     * Lists the members of the current working directory.
     */
    public void
    $() {
        for (String memberName : new File(".").list()) this.printMember(new File(memberName));
    }

    /**
     * Lists the <var>files</var> which are <em>not</em> directories, then for all <var>files</var> which <em>are</em>
     * directories:
     * <ul>
     *   <li>One empty line</li>
     *   <li>The path of the directory</li>
     *   <li>The names of the directory members (one per line)</li>
     * </ul>
     */
    public void
    $(File... files) { this.$(Arrays.asList(files)); }

    public void
    $(Collection<File> files) {

        if (files.isEmpty()) {
            this.$();
            return;
        }

        if (files.size() == 1) {
            this.print(files.iterator().next());
            return;
        }

        // Iff there is more than one file: Print the non-directory files first, then list the directories.

        List<File> directories = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) {
                directories.add(file);
            } else {
                this.printMember(file);
            }
        }

        for (File d : directories) {
            System.out.println();
            System.out.println(d + ":");
            for (String memberName : d.list()) this.printMember(new File(memberName));
        }
    }

    private void
    printMember(File member) {
        if (this.lonG) {
            System.out.printf(
                "%c%c%c%c %9d %s%n",
                member.isDirectory() ? 'd' : '-',
                member.canRead()     ? 'r' : '-',
                member.canWrite()    ? 'w' : '-',
                member.canExecute()  ? 'x' : '-',
                member.length(),
                member.getName()
            );
        } else {
            System.out.println(member.getName());
        }
    }

    public void
    print(File file) {

        if (file.isDirectory()) {
            for (File member : file.listFiles())
                this.printMember(member);
        } else {
            this.printMember(file);
        }
    }
}
