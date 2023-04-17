package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Shivan Patel
 */
public class Main {

    /** File for the CWD. */
    static final File CWD = new File(System.getProperty("user.dir"));;

    /** File for head. */
    private static File head;

    /** File for most recent  commit. */
    private static String previous;

    /** File for the .gitlet directory. */
    static final File GITLET = new File(".gitlet");

    /** The stage. */
    private static Stage stage;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            return;
        }
        if (args[0].equals("init")) {
            init();
        } else {
            if (checkInit(args)) {
                if (args[0].equals("add")) {
                    add(args);
                } else if (args[0].equals("commit")) {
                    commit(args);
                } else if (args[0].equals("log")) {
                    log(args);
                } else if (args[0].equals("checkout")) {
                    checkout(args);
                } else if (args[0].equals("rm")) {
                    remove(args);
                } else if (args[0].equals("global-log")) {
                    globalLog(args);
                } else if (args[0].equals("find")) {
                    find(args);
                } else if (args[0].equals("branch")) {
                    branch(args);
                } else if (args[0].equals("rm-branch")) {
                    removebranch(args);
                } else if (args[0].equals("status")) {
                    status();
                } else if (args[0].equals("reset")) {
                    reset(args);
                } else if (args[0].equals("merge")) {
                    me(args);
                } else {
                    System.out.println("Command not found");
                }
            }
        }
    }

    public static void init() throws IOException {
        if (GITLET.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.");
        } else {
            GITLET.mkdir();
            File stageFile = new File(GITLET + "/stage.txt");
            stageFile.createNewFile();
            stage = new Stage();
            stage.saveStageToFile();
            head = new File(GITLET + "/head.txt");
            head.createNewFile();
            Commit initial = new Commit("initial commit", null);
            initial.saveCommitToFile();
            previous = initial.getMetadata();
            Utils.writeContents(head, previous);
            Branch master = new Branch("master", initial);
            master.saveBranchToFile();
            File currentBranchFile = new File(GITLET + "/branch.txt");
            Utils.writeObject(currentBranchFile, master);
        }

    }

    public static void add(String[] args) throws IOException {
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        for (int i = 1; i < args.length; i++) {
            stage.addToStage(args[i]);
        }
        stage.saveStageToFile();
    }

    public static void commit(String[] args) throws IOException {
        head = new File(GITLET + "/head.txt");
        previous = Utils.readContentsAsString(head);
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        File currentBranchFile = new File(GITLET + "/branch.txt");
        Branch currentBranch =
                Utils.readObject(currentBranchFile, Branch.class);
        if (stage.getAddition().isEmpty() && stage.getRemoval().isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else if (args.length == 1) {
            System.out.println("Please enter a commit message.");
        } else if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit next = new Commit(args[1], previous);
            next.mergeStageData(stage);
            if (!args[0].equals("commit")) {
                String mergeParent = args[0];
                next.setMergeParent(mergeParent);
            }
            next.saveCommitToFile();
            previous = next.getMetadata();
            head = new File(GITLET + "/head.txt");
            Utils.writeContents(head, previous);
            stage.clear();
            stage.saveStageToFile();
            currentBranch.moveCommit(next);
            currentBranch.saveBranchToFile();
            Utils.writeObject(currentBranchFile, currentBranch);
        }
    }

    public static void checkout(String[] args) throws IOException {
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        if (args.length == 3) {
            String fileName = args[2];
            String latestCommit = Utils.readContentsAsString(new
                    File(GITLET + "/head.txt"));
            File currentCommitVersion = new
                    File(".gitlet/commits/" + latestCommit + ".txt");
            Commit currentCommit = Utils.readObject(currentCommitVersion,
                    Commit.class);
            String newContents = currentCommit.getData().get(fileName);
            File fileInCWD = new File(CWD + "/" + fileName);
            Utils.writeContents(fileInCWD, newContents);
        }
        if (args.length == 4) {
            String fileName = args[3];
            String commitID = args[1];
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            } else {
                File commitIDFile = new
                        File(".gitlet/commits/" + commitID + ".txt");
                Boolean found;
                if (!commitIDFile.exists()) {
                    List<String> allCommits = Utils.plainFilenamesIn(
                            ".gitlet/commits");
                    found = false;
                    for (String allCommitID : allCommits) {
                        if (allCommitID.startsWith(commitID)) {
                            commitIDFile = new File(".gitlet/commits/"
                                    + allCommitID);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("No commit with that id exists.");
                        return;
                    }
                }
                found = true;
                if (found) {
                    Commit cIDC = Utils.readObject(commitIDFile, Commit.class);
                    if (!cIDC.getData().containsKey(fileName)) {
                        System.out.println("File does not exist in that "
                                + "commit.");
                    } else {
                        String newContents = cIDC.getData().get(fileName);
                        File fileInCWD = new File(CWD + "/" + fileName);
                        Utils.writeContents(fileInCWD, newContents);
                    }
                }
            }
        }

        if (args.length == 2) {
            checkoutLengthTwo(args);
        }
    }

    public static void remove(String[] args) throws IOException {
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        String lC = Utils.readContentsAsString(new File(GITLET + "/head.txt"));
        File cCV = new File(".gitlet/commits/" + lC + ".txt");
        Commit cC = Utils.readObject(cCV, Commit.class);
        String fileName = args[1];
        Boolean failure = true;
        if (stage.getAddition().containsKey(fileName)) {
            failure = false;
            stage.getAddition().remove(fileName);
            stage.saveStageToFile();
        }
        if (cC.getData().containsKey(fileName)) {
            failure = false;
            stage.stageForRemoval(fileName, cC.getData().get(fileName));
            File fileInCWD = new File(CWD + "/" + fileName);
            fileInCWD.delete();
            stage.saveStageToFile();
        }
        if (failure) {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void log(String[] args) {
        head = new File(GITLET + "/head.txt");
        String tCM = Utils.readContentsAsString(head);
        File tF = new File(".gitlet/commits/" + tCM + ".txt");
        Commit trackedCommit = Utils.readObject(tF, Commit.class);
        while (trackedCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + trackedCommit.getMetadata());
            System.out.println("Date: " + trackedCommit.getTimestamp());
            System.out.println(trackedCommit.getMessage());
            System.out.println("");
            tCM = trackedCommit.getParent();
            tF = new File(".gitlet/commits/" + tCM + ".txt");
            trackedCommit = Utils.readObject(tF, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + trackedCommit.getMetadata());
        System.out.println("Date: " + trackedCommit.getTimestamp());
        System.out.println(trackedCommit.getMessage());
    }

    public static void globalLog(String[] args) {
        List<String> allCommits = Utils.plainFilenamesIn(".gitlet/commits");
        for (String commitID : allCommits) {
            Commit commit = Utils.readObject(new File(
                    ".gitlet/commits/" + commitID), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.getMetadata());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println("");
        }
    }

    public static void find(String[] args) {
        String commitMessage = args[1];
        boolean atLeastOne = false;
        List<String> allCommits = Utils.plainFilenamesIn(".gitlet/commits");
        for (String commitID : allCommits) {
            Commit commit = Utils.readObject(new File(
                    ".gitlet/commits/" + commitID), Commit.class);
            if (commit.getMessage().equals(commitMessage)) {
                System.out.println(commit.getMetadata());
                atLeastOne = true;
            }
        }
        if (!atLeastOne) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branch(String[] args) {
        String branchName = args[1];
        List<String> allBranches = Utils.plainFilenamesIn(".gitlet/branches");
        if (allBranches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name already exists.");
        } else {
            String lC = Utils.readContentsAsString(new File(
                    GITLET + "/head.txt"));
            File cCV = new File(".gitlet/commits/" + lC + ".txt");
            Commit currentCommit = Utils.readObject(cCV, Commit.class);
            Branch newBranch = new Branch(branchName, currentCommit);
            newBranch.saveBranchToFile();
        }
    }

    public static void removebranch(String[] args) {
        String branchName = args[1];
        File branchFile = new File(".gitlet/branches/" + branchName + ".txt");
        File currentBranchFile = new File(".gitlet/branch.txt");
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else {
            Branch rB = Utils.readObject(branchFile, Branch.class);
            Branch cB = Utils.readObject(currentBranchFile, Branch.class);
            if (rB.getName().equals(cB.getName())) {
                System.out.println("Cannot remove the current branch.");
            } else {
                branchFile.delete();
            }
        }
    }

    public static void status() {
        stage = Utils.readObject(new File(
                GITLET + "/stage.txt"), Stage.class);
        head = new File(GITLET + "/head.txt");
        List<String> aBN = Utils.plainFilenamesIn(".gitlet/branches");
        Collections.sort(aBN);
        File currentBranchFile = new File(".gitlet/branch.txt");
        Branch cB = Utils.readObject(currentBranchFile, Branch.class);
        System.out.println("=== Branches ===");
        for (String branchName : aBN) {
            File branchFile = new File(".gitlet/branches/" + branchName);
            Branch branchObject = Utils.readObject(branchFile, Branch.class);
            if (branchObject.getName().equals(cB.getName())) {
                System.out.println("*" + branchObject.getName());
            } else {
                System.out.println(branchObject.getName());
            }
        }
        System.out.println("");
        ArrayList<String> stagedNames = new ArrayList<>();
        System.out.println("=== Staged Files ===");
        if (!stage.getAddition().isEmpty()) {
            stage.getAddition().forEach((String fN, String fC) -> {
                stagedNames.add(fN);
            });
        }
        Collections.sort(stagedNames);
        for (String name : stagedNames) {
            System.out.println(name);
        }
        System.out.println("");
        ArrayList<String> removedNames = new ArrayList<>();
        System.out.println("=== Removed Files ===");
        if (!stage.getRemoval().isEmpty()) {
            stage.getRemoval().forEach((String fN, String fC) -> {
                removedNames.add(fN);
            });
        }
        Collections.sort(removedNames);
        for (String name : removedNames) {
            System.out.println(name);
        }
        System.out.println("");
        statusEC(aBN);
    }

    public static void reset(String[] args) throws IOException {
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        head = new File(GITLET + "/head.txt");
        String tCN = Utils.readContentsAsString(head);
        File tF = new File(".gitlet/commits/" + tCN + ".txt");
        Commit tC = Utils.readObject(tF, Commit.class);
        String commitID = args[1];
        File commitIDFile = new File(".gitlet/commits/" + commitID + ".txt");
        if (!commitIDFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit cIDC = Utils.readObject(commitIDFile, Commit.class);
        List<String> cWDFiles = Utils.plainFilenamesIn(CWD);
        for (String fileInCWD : cWDFiles) {
            if (!tC.getData().containsKey(fileInCWD)
                    && cIDC.getData().containsKey(fileInCWD)) {
                System.out.println("There is an untracked file in the"
                        + " way; delete it, or add and commit it first.");
                return;
            }
        }
        tC.getData().forEach((String fileName, String fileContents) -> {
            File fileInCWD = new File(CWD + "/" + fileName);
            fileInCWD.delete();

        });
        cIDC.getData().forEach((String fileName, String fileContents) -> {
            File fileInCWD = new File(CWD + "/" + fileName);
            Utils.writeContents(fileInCWD, fileContents);
        });
        Utils.writeContents(head, cIDC.getMetadata());
        File currentBranchFile = new File(GITLET + "/branch.txt");
        Branch cB = Utils.readObject(currentBranchFile, Branch.class);
        cB.moveCommit(cIDC);
        cB.saveBranchToFile();
        stage.clear();
        stage.saveStageToFile();
    }

    public static void me(String[] args) throws IOException {
        stage = Utils.readObject(new File(GITLET + "/stage.txt"), Stage.class);
        head = new File(GITLET + "/head.txt");
        File nBF = new File(".gitlet/branches/" + args[1] + ".txt");
        File cBF = new File(".gitlet/branch.txt");
        Branch cB = Utils.readObject(cBF, Branch.class);
        if (!stage.getRemoval().isEmpty() || !stage.getAddition().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!nBF.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (cB.getName().equals(args[1])) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Branch nB = Utils.readObject(nBF, Branch.class);
        Commit cC = cB.getCommit(); Commit nBC = nB.getCommit();
        ArrayList<String> cCP = new ArrayList<>();
        ArrayList<String> nCP = new ArrayList<>();
        Commit cP = cC; Commit nP = nBC;
        mergeH1(cP, nP, cCP, nCP); int cI = -1;
        boolean continueIteration = true;
        for (int i = 0; i < cCP.size(); i++) {
            if (continueIteration) {
                for (int j = 0; j < nCP.size(); j++) {
                    if (cCP.get(i).equals(nCP.get(j))) {
                        cI = i; continueIteration = false;
                    }
                }
            }
        }
        Commit splitPointCommit = Utils.readObject(new File(
                ".gitlet/commits/" + cCP.get(cI) + ".txt"), Commit.class);
        if (cCP.contains(nBC.getMetadata())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        }
        if (nCP.contains(cC.getMetadata())) {
            String[] arguments = {"checkout", nB.getName()};
            checkout(arguments);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        final Boolean[] hasConflict = {false};
        List<String> cWDFiles = Utils.plainFilenamesIn(CWD);
        for (String fileInCWD : cWDFiles) {
            if (!cC.getData().containsKey(fileInCWD)
            ) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        mergeH2(nBC, cC, splitPointCommit, hasConflict);
        mergeH3(nBC, nB, cB, hasConflict);
    }

    public static boolean checkInit(String[] args) {
        String[] validCommands = {"add", "commit", "rm", "log",
            "global-log", "find", "status", "checkout", "branch",
            "rm-branch", "reset", "merge"};
        List<String> validCommandsList = Arrays.stream(validCommands).toList();
        if (validCommandsList.contains(args[0])) {
            if (GITLET.exists()) {
                return true;
            } else {
                System.out.println("Not in an initialized Gitlet directory.");
                return false;
            }
        } else {
            System.out.println("No command with that name exists.");
            return false;
        }

    }


    public static void checkoutLengthTwo(String[] args) throws IOException {
        String cOBN = args[1];
        if (!new File(".gitlet/branches/" + cOBN + ".txt").exists()) {
            System.out.println("No such branch exists.");
        } else {
            Branch cOB = Utils.readObject(new File(
                    ".gitlet/branches/" + cOBN + ".txt"), Branch.class);
            Commit bC = cOB.getCommit();
            File cBF = new File(GITLET + "/branch.txt");
            Branch cB = Utils.readObject(cBF, Branch.class);
            if (cB.getName().equals(cOB.getName())) {
                System.out.println("No need to checkout the current branch.");
            } else {
                head = new File(GITLET + "/head.txt");
                String cH = Utils.readContentsAsString(head);
                File hCF = new File(".gitlet/commits/" + cH + ".txt");
                Commit hC = Utils.readObject(hCF, Commit.class);
                HashMap<String, String> sD1 = stage.getAddition();
                HashMap<String, String> sD2 = stage.getRemoval();
                List<String> cWDFiles = Utils.plainFilenamesIn(CWD);
                iCWD(cWDFiles, hC, cOB);
                Utils.writeObject(cBF, cOB);
                Utils.writeContents(head, bC.getMetadata());
                bC.getData().forEach((String fileName, String fileContents) -> {
                    File fileInCWD = new File(CWD + "/" + fileName);
                    Utils.writeContents(fileInCWD, fileContents);
                });
                cB.getCommit().getData().forEach(
                        (String fN, String fileContents) -> {
                        if (!cOB.getCommit().getData().containsKey(fN)) {
                            File d = new File(CWD + "/" + fN); d.delete();
                        }
                    });
                if (!sD1.isEmpty()) {
                    sD1.forEach((String fN, String fileContents) -> {
                        if (!cOB.getCommit().getData().containsKey(fN)) {
                            File d = new File(CWD + "/" + fN); d.delete();
                        }
                    });
                }
                if (!sD2.isEmpty()) {
                    sD2.forEach((String fN, String fileContents) -> {
                        if (!cOB.getCommit().getData().containsKey(fN)) {
                            File d = new File(CWD + "/" + fN); d.delete();
                        }
                    });
                }
                if (!cB.equals(cOB)) {
                    stage.clear(); stage.saveStageToFile();
                }
            }
        }
    }

    public static void iCWD(List<String> cWDFiles, Commit hC, Branch cOB) {
        for (String fICWD : cWDFiles) {
            if (!hC.getData().containsKey(fICWD)
                    && cOB.getCommit().getData().containsKey(fICWD)
                    && !stage.getAddition().containsKey(fICWD)
                    && !stage.getRemoval().containsKey(fICWD)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it "
                        + "first.");
                return;
            }
        }
    }

    public static void statusEC(List<String> aBN) {
        String tCN = Utils.readContentsAsString(head);
        File tF = new File(".gitlet/commits/" + tCN + ".txt");
        Commit trackedCommit = Utils.readObject(tF, Commit.class);
        ArrayList<String> untrackedFiles = new ArrayList<>();
        List<String> cWDFiles = Utils.plainFilenamesIn(CWD);
        for (String fileInCWD : cWDFiles) {
            if (!trackedCommit.getData().containsKey(fileInCWD)
                    && !stage.getRemoval().containsKey(fileInCWD)
                    && !stage.getAddition().containsKey(fileInCWD)
                    && aBN.size() == 1) {
                untrackedFiles.add(fileInCWD);
            }
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> notStaged = new ArrayList<>();
        trackedCommit.getData().forEach((String fN, String fC) -> {
            File fileInCWD = new File(CWD + "/" + fN);
            if (!fileInCWD.exists() && !stage.getRemoval().containsKey(fN)) {
                notStaged.add(fN + " (deleted)");
            }
            if (fileInCWD.exists()
                    && !Utils.readContentsAsString(fileInCWD).equals(fC)
                    && !stage.getAddition().containsKey(fN)
                    && !stage.getRemoval().containsKey(fN)) {
                notStaged.add(fN + " (modified)");
            }
        });
        stage.getAddition().forEach((String fileName, String fC) -> {
            File fICWD = new File(CWD + "/" + fileName);
            if (fICWD.exists()
                    && !Utils.readContentsAsString(fICWD).equals(fC)) {
                notStaged.add(fileName + " (modified)");
            }
            if (!fICWD.exists()) {
                notStaged.add(fileName + " (deleted)");
            }
        });
        Collections.sort(notStaged);
        for (String file : notStaged) {
            System.out.println(file);
        }

        System.out.println("");
        System.out.println("=== Untracked Files ===");

        Collections.sort(untrackedFiles);
        for (String name : untrackedFiles) {
            System.out.println(name);
        }
        System.out.println("");
    }

    public static void mergeH1(Commit cP,
             Commit nP, ArrayList<String> cCP, ArrayList<String> nCP) {
        while (cP.getParent() != null) {
            String cPPN = cP.getParent();
            if (cP.getMergeParent() != null) {
                cPPN = cP.getMergeParent();
            }
            cCP.add(cPPN);
            File tF = new File(".gitlet/commits/" + cPPN + ".txt");
            cP = Utils.readObject(tF, Commit.class);
        }
        while (nP.getParent() != null) {
            String nPPN = nP.getParent();
            if (nP.getMergeParent() != null) {
                nPPN = cP.getMergeParent();
            }
            nCP.add(nPPN);
            File tF = new File(".gitlet/commits/" + nPPN + ".txt");
            nP = Utils.readObject(tF, Commit.class);
        }
    }

    public static void mergeH2(
            Commit nBC, Commit cC, Commit sPC, Boolean[] hasConflict) {
        nBC.getData().forEach((String fileName, String fileContents) -> {
            String cCC = cC.getData().get(fileName);
            String nCC = nBC.getData().get(fileName);
            String spCC = sPC.getData().get(fileName);
            if (spCC == null && cCC == null && nCC != null) {
                try {
                    String[] ar = {"", nBC.getMetadata(), "--", fileName};
                    checkout(ar);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sPC.getData().forEach((String fN, String fC) -> {
            String cCC = cC.getData().get(fN);
            String nCC = nBC.getData().get(fN);
            if ((nCC != null && cCC != null
                    && !cCC.equals(nCC) && !fC.equals(cCC)
                    && !fC.equals(nCC))
                    || (nCC == null && !fC.equals(cCC))
                    || (cCC == null && !fC.equals(nCC))) {
                String cCCT; String nCCT;
                if (cCC == null) {
                    cCCT = "";
                } else {
                    cCCT = cCC;
                }
                if (nCC == null) {
                    nCCT = "";
                } else {
                    nCCT = nCC;
                }
                String nC = "<<<<<<< HEAD\n" + cCCT + "=======\n"
                        + nCCT + ">>>>>>>\n";
                File fileinCWD = new File(CWD + "/" + fN);
                Utils.writeContents(fileinCWD, nC);
                hasConflict[0] = true;
                try {
                    stage.addToStage(fN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fC.equals(cCC) && nCC == null) {
                try {
                    String[] arguments = {"", fN}; remove(arguments);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (fC.equals(cCC) && !fC.equals(nCC)) {
                try {
                    String[] ar = {"", nBC.getMetadata(), "--", fN};
                    checkout(ar); stage.addToStage(fN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static void mergeH3(
            Commit nBC, Branch nB, Branch cB, Boolean[] hasConflict)
            throws IOException {

        String[] commitArgs = {nBC.getMetadata(),
            "Merged " + nB.getName() + " into "
                + cB.getName() + "."};
        commit(commitArgs);
        if (hasConflict[0]) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}
