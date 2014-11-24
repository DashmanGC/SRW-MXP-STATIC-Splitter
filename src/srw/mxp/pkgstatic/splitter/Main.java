/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srw.mxp.pkgstatic.splitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonatan
 */
public class Main {

    public static class IndexEntry{
        public String name;
        public int offset;
        public int size;

        public IndexEntry(){
            name = "";
            offset = 0;
            size = 0;
        }

        public IndexEntry(String n, int o, int s){
            name = n;
            offset = o;
            size = s;
        }
    }

    static String filename;
    static String destination = ".";
    static RandomAccessFile f;
    static String file_list = "";
    static byte[] seq;
    
    static int first_stop = 0x0017EF00;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        /*
         * USE
         * -s <filename> [<destination_folder>] Splits filename's contents on destination
         * -m <filename> <files_list> Merges the list of files in files_list into filename
         */

        boolean show_use = false;

        if (args.length < 2 || args.length > 3){
            show_use = true;
        }

        else{
            String command = args[0];
            filename = args[1];

            if (command.equals("-s")){

                if (args.length == 3)
                    destination = args[2];

                // Try opening the file
                try{
                    f = new RandomAccessFile(filename, "r");
                    // Read the header / index and obtain the offsets
                    //readHeader();
                    splitFile();
                }catch (IOException ex) {
                    System.err.println("ERROR: Couldn't read file.");   // END
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if (command.equals("-m")){
                if (args.length != 3)
                    show_use = true;
                else{
                    file_list = args[2];
                    // Read the file list and merge the contents into the given filename
                    try{
                        mergeFileListNoTable();
                    }catch (IOException ex) {
                        System.err.println("ERROR: Couldn't read file.");   // END
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            else    // Wrong command
                show_use = true;
        }

        if (show_use){
            System.out.println("ERROR: Wrong number of parameters: " + args.length);
            System.out.println("TO SPLIT:\n java -jar static2_splitter -s <filename> [<destination_folder>]");
            System.out.println("TO MERGE:\n java -jar static2_splitter -m <filename> <files_list>");
        }
    }
    
    
    public static void splitFile() throws IOException{
        // We have 3 parts in STATIC2_ADD.BIN:
        // BEFORE the text data
        // The text data itself
        // AFTER the text data
        // So let's just split the file in those 3 parts.
        
        // Get the first part
        seq = new byte[first_stop];
        
        f.read(seq);
        
        extractFile("FIRST.BIN");
        
        // Find the end of the second part
        boolean stop = false;
        byte[] aux = new byte[4];
        int offset = first_stop;
        
        while (!stop){
            f.seek(offset);
            f.read(aux);
            
            if (aux[0] == 'T' && aux[1] == 'X' && aux[2] == '4' && aux[3] == '8')
                stop = true;
            else
                offset += 64;
        }
        
        // Save the second part (data)
        seq = new byte[offset - first_stop];
        
        f.seek(first_stop);
        f.read(seq);
        
        extractFile("DATA.BIN");
        
        // Save the last part
        seq = new byte[(int) f.length() - offset];
        f.read(seq);
        
        extractFile("LAST.BIN");
        
        writeFileList();
    }
    
    // Outputs the global seq to a file with the given name in destination
    public static void extractFile(String name) throws IOException{
        String path = "";
        
        if (destination.equals("."))
            path = name;
        else{
        // Check if folder with the name of the pak_file exists. If not, create it.
            path = destination;
            File folder = new File(path);
            if (!folder.exists()){
                boolean success = folder.mkdir();
                if (!success){
                    System.err.println("ERROR: Couldn't create folder.");
                    return;
                }
            }
            path += "/" + name;
        }

        // Create the file inside said folder
        try {
            RandomAccessFile f2 = new RandomAccessFile(path, "rw");

            f2.write(seq);

            f2.close();

            file_list += name + "\n";

            //System.out.println(ie.name + " saved successfully.");
        } catch (IOException ex) {
            System.err.println("ERROR: Couldn't write " + name);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void writeFileList() throws IOException{
        String path = "";
        if (destination.equals("."))
            path = "files.list";
        else    // The folder was created previously
            path = destination + "/files.list";

        PrintWriter pw = new PrintWriter(path);

        pw.print(file_list);

        pw.close();
    }

    
    public static void mergeFileListNoTable() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(file_list));
        String line;
        int total_length = 0;
        ArrayList<IndexEntry> entries = new ArrayList<>();

        IndexEntry ie;

        // Read all filenames in files.list and their sizes
        int actual_length = 0;
        int padded_length = 0;

        while ((line = br.readLine()) != null) {

            f = new RandomAccessFile(line, "r");

            actual_length = (int) f.length();
            // We repurpose the offset value to store the padded length
            ie = new IndexEntry(line, padded_length, actual_length);

            entries.add(ie);
            
            total_length += actual_length;

            f.close();
        }
        br.close();

        seq = new byte[total_length];   // Here we'll write the full file
        byte[] aux;

        int pointer_data = 0;

        // Write each of the files into seq and update its pointer in the table
        for (int i = 0; i < entries.size(); i++){
            // Write the file into our byte sequence
            aux = new byte[entries.get(i).size];

            f = new RandomAccessFile(entries.get(i).name, "r");
            f.read(aux);
            f.close();

            System.out.println("P.Data: " + pointer_data +
                    " - Length: " + aux.length + " - Total: " + total_length);

            for (int j = 0; j < aux.length; j++)
                seq[pointer_data + j] = aux[j];

            pointer_data += entries.get(i).size;
        }

        // Save the byte sequence to a file
        f = new RandomAccessFile(filename, "rw");
        f.write(seq);
        f.close();

        System.out.println("Finished. File " + filename + " built successfully.");
    }
}
