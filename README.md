SRW MX Portable Static Splitter by Dashman
-----------------------------------

This program allows you to split STATIC2_ADD.BIN into 3 files and merge them back after edition. This splitting is made purely out of convenience (to save the editor from using too much memory) and might be unnecessary, but oh well... You'll need to have Java installed in your computer to operate this.


How to split:

1) Extract STATIC2_ADD.BIN from the ISO and place it in the same folder as the application (static2_splitter.jar).
2) In a command / shell window, execute this:

java -jar static2_splitter.jar -s STATIC2_ADD.BIN <extract_folder>

* Alternatively, use split.bat to perform this. The BAT file has to be in the same folder where the BIN and the executable are.

3) This will extract 3 BIN files in <extract_folder>, along with a files.list file. The one you want to use the editor on is DATA.BIN.


How to merge:

1) Put the program inside <extract_folder> along with the generated files and its corresponding LIST file. Make sure all the files have the same name they had when they were extracted (the edited DATA.BIN should be called that way).

2) Execute

java -jar static2_splitter.jar -m STATIC2_ADD.BIN files.list

* Alternatively, use merge.bat to do this.

3) This will generate a new STATIC2_ADD.BIN file in <extract_folder>. You can replace the one in the ISO with this one.


IMPORTANT NOTES:

* Keep backups!

* When reinserting, make sure you don't have already a STATIC2_ADD.BIN file already in the folder. If by any chance your generated file is smaller than the old one, the new file might have issues.

* FIRST.BIN contains the (among other things) font files.

* LAST.BIN contains TX48 textures.