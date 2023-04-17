package gitlet;

import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {

    /** The dir for the branches. **/
    static final File BRANCH_FOLDER = new File(".gitlet/branches");

    /** The name of the branch. */
    private String _name;

    /** The head commit of the branch. */
    private Commit _commit;

    /** String representing the commitID. */
    private String commitID;

    public Branch(String name, Commit commit) {
        _name = name;
        _commit = commit;
    }

    public void saveBranchToFile() {
        BRANCH_FOLDER.mkdir();
        File branch = new File(".gitlet/branches/" + getName() + ".txt");
        Utils.writeObject(branch, this);
    }

    public void moveCommit(Commit commit) {
        _commit = commit;
    }

    public String getName() {
        return _name;
    }

    public Commit getCommit() {
        return _commit;
    }

    public String getCommitID() {
        return _commit.getMetadata();
    }


}
