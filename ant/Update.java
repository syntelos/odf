/*
 * Gap Data
 * Copyright (C) 2009 John Pritchard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

/**
 * Accept a file path source, and a target path expression to copy the
 * source file to.
 * @author jdp
 */
public class Update 
    extends Object
    implements java.io.FilenameFilter
{

    private final static String Svn;
    static {
        String exe = null;
        try {
            StringTokenizer strtok = new StringTokenizer(System.getenv("PATH"),File.pathSeparator);
            while (strtok.hasMoreTokens()){
                String pel = strtok.nextToken();
                File chk = new File(pel,"svn");
                if (chk.isFile() && chk.canExecute()){
                    exe = chk.getPath();
                    break;
                }
            }
        }
        catch (Exception exc){
            throw new InternalError();
        }
        Svn = exe;
    }
    public final static boolean SVN = ((null != Svn)&&(new File(".svn").isDirectory()));

    static {
        if (SVN)
            System.err.println("Using subversion");
        else
            System.err.println("Not using subversion");
    }

    private final static Runtime RT = Runtime.getRuntime();

    public static void usage(){
        System.err.println("Usage");
        System.err.println();
        System.err.println("  Update source[':'source]* target[':'target]*");
        System.err.println();
        System.err.println("Description");
        System.err.println();
        System.err.println("  Copy one or more source files into the");
        System.err.println("  target directories.  Delete old versions.");
        System.err.println();
        System.err.println("  For no targets found on the cmd line, exit");
        System.err.println("  silently.");
        System.err.println();
    }
    public static void main(String[] argv){
        if (1 < argv.length){

            File[] sources = Source(argv[0]);
            if (null != sources){
                for (File src: sources){
                    File[] targets = Target(src,argv);
                    if (null != targets){
                        final long srclen = src.length();
                        try {
                            FileChannel source = new FileInputStream(src).getChannel();
                            try {
                                for (File tgt: targets){

                                    FileChannel target = new FileOutputStream(tgt).getChannel();
                                    try {
                                        source.transferTo(0L,srclen,target);
                                    }
                                    finally {
                                        target.close();
                                    }
                                    System.out.printf("Updated %s\n",tgt.getPath());

                                    File[] deletes = Delete(src,tgt);
                                    if (null != deletes){

                                        for (File del: deletes){
                                            if (SVN){
                                                if (SvnDelete(del))
                                                    System.out.printf("Deleted %s\n",del.getPath());
                                                else
                                                    System.out.printf("Failed to delete %s\n",del.getPath());
                                            }
                                            else if (del.delete())
                                                System.out.printf("Deleted %s\n",del.getPath());
                                            else
                                                System.out.printf("Failed to delete %s\n",del.getPath());
                                        }
                                    }
                                    if (SVN)
                                        SvnAdd(tgt);
                                }
                            }
                            finally {
                                source.close();
                            }
                        }
                        catch (Exception exc){
                            exc.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
                System.exit(0);
            }
            else {
                System.err.printf("Source file(s) not found in '%s'\n",argv[0]);
                System.exit(1);
            }
        }
        else {
            usage();
            System.exit(1);
        }
    }

    private final static boolean SvnDelete(File file)
        throws java.io.IOException,
               java.lang.InterruptedException
    {
        String[] cmd = new String[]{
            Svn, "delete", "--force", file.getName()
        };
        String[] env = new String[0];

        Process p = RT.exec(cmd,env,file.getParentFile());
        if (0 == p.waitFor()){

            return true;
        }
        else {
            System.out.printf("svn delete --force %s in %s%n",file.getName(),file.getParentFile().getPath());
            Copy(p.getInputStream(),System.out);
            Copy(p.getErrorStream(),System.err);
            return false;
        }
    }
    private final static boolean SvnAdd(File file)
        throws java.io.IOException,
               java.lang.InterruptedException
    {
        String[] cmd = new String[]{
            Svn, "add", file.getName()
        };
        String[] env = new String[0];

        Process p = RT.exec(cmd,env,file.getParentFile());
        if (0 == p.waitFor()){

            return true;
        }
        else {
            System.out.printf("svn add %s in %s%n",file.getName(),file.getParentFile().getPath());
            Copy(p.getInputStream(),System.out);
            Copy(p.getErrorStream(),System.err);
            return false;
        }
    }
    private final static File[] Source(String path){
        File[] list = null;
        String[] files = path.split(":");
        for (int ccc = 0, ccz = files.length; ccc < ccz; ccc++){

            File tgt = new File(files[ccc]);

            if (tgt.isFile()){
                list = Add(list,tgt);
            }
        }
        return list;
    }
    private final static File[] Target(File src, String[] argv){
        File[] list = null;
        for (int cc = 1, count = argv.length; cc < count; cc++){
            String[] files = argv[cc].split(":");
            for (int ccc = 0, ccz = files.length; ccc < ccz; ccc++){

                File tgt = new File(files[ccc]);

                if (tgt.isDirectory()){
                    tgt = new File(tgt,src.getName());
                }

                list = Add(list,tgt);
            }
        }
        return list;
    }
    private final static File[] Delete(File src, File tgt){
        File tgtd = tgt.getParentFile();
        if (null != tgtd){
            String name = src.getName();
            String basename = Basename(name,"[-\\.][a-zA-Z0-9\\.]+");
            return tgtd.listFiles(new Update(basename,name));
        }
        return null;
    }
    private final static String Basename(String name, String re){
        String[] s = name.split(re);
        if (null != s && 0 < s.length)
            return s[0];
        else
            throw new IllegalArgumentException(String.format("%s //%s//",name,re));
    }
    private final static void Copy(InputStream in, OutputStream out){
        try {
            try {
                byte[] iob = new byte[128];
                int read;
                while (0 < (read = in.read(iob,0,128)))
                    out.write(iob,0,read);
            }
            finally {
                in.close();
            }
        }
        catch (IOException exc){
        }
    }
    public final static File[] Add(File[] list, File item){
        if (null == item)
            return list;
        else if (null == list)
            return new File[]{item};
        else {
            int len = list.length;
            File[] copier = new File[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }


    /**
     * Delete files filter
     */
    private final String basename, exclude;


    public Update(String basename, String exclude){
        super();
        this.basename = basename;
        this.exclude = exclude;
    }

    public boolean accept(File dir, String name){
        /*
         * Files to delete
         */
        if (name.startsWith(this.basename)){
            return (!name.equals(exclude));
        }
        else
            return false;
    }
}
