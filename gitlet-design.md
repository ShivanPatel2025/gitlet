# Gitlet Design Document
author: Shivan Patel

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

###Main.java
This class is the entry point of the program as it implements methods in 
order to set up persistence and support the various commands that make up
gitlet.
####Fields 
1. static final File CWD: Pointer to the current working directory of the program.
2. static final File GITLET: A pointer to the .gitlet directory that is created with gitlet init.
###Blob.java
This class represents the blob/contents of a file. The constructor of the blob takes in a file.
####Fields
1) instance variable File file: refers to the file of the blob
2) instance variable String content: refers to the contents of the file of the blob
###Commit.java
This class represents a commit instance.
####Fields
1) instance variable String parent: refers to the parent commit
2) instance variable String metadata: contain the metadata
3) instance variable String message: Contains the message passed in
4) instance variable String timestamp: contains the timestamp of commit creation
5) instance variable int counter: commit number
6) instance variable boolean master: tells us if this commit is master
7) instance variable boolean head: tells us if this commit is head.
8) instance variable ArrayList<Blob> content: contains all the blobs of the files being commited.
###Stage.java
Represents the staging area in our gitlet. It includes the files staged for removal and addition.
####Fields
1) ArrayList<File> addition: Files that are staged for addition
2) ArrayList<File> removal: Files that are staged for removal.
###Branch.java
Represents a branch instance.
####Fields
1. ArrayList<String> commits: an arraylist of the UIDs of the commits in the branch.
## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.

###Main.java
1. main(String[]args): The entry point of the program. It checks to see the array isn't empty,
and then based on the input calls the correct method through a series of if statements.
2. init(): Initiliazes the .gitlet repository by calling setupPersistance. It 
creates the first commit with the correct timestamp and the head/master pointers.
We also initialize the staging area. 
3. add(String[] args): adds the files to the staging area using addFile() in the stage class.
4. commit(String[] args): Creates new commit by cloning the parent. Changes the metadata
through message and timestamp. Uses stage to determine changes, then clears it. Moves the head/master pointer.
5. rm(String[] args): If the file is staged, unstage it. If it is tracked, stage for removal and remove from CWD.
6. log() -  Goes from head commit, to parent, until all commits have been visited. Prints
the time and UID as well as the message.
7. global log() -  - after checkpoint
8. find() -  - after checkpoint
9. status()   - after checkpoint
10. checkout() - Replaces the given file with either the version of the file in the head commit or the version in a commit identified by UID.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

###Commit [All instance variables]
####After each commit, we save it to a text filed named after the UID inside of a commits directory inside .gitlet

###Blob [file, content]
####After each commit we save it to a text file in a blobs directory inside .gitlet named after the file name, and has the contents of the file inside the file.

###Stage [addition, removal]
####After each add command, we save the stage to a text file in a stage directory inside .gitlet, named addition and removal.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

