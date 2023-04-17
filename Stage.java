package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Main.CWD;
import static gitlet.Main.GITLET;

public class Stage implements Serializable {

    /** Hashmap for files staged for removal. */
    private HashMap<String, String> _removal;

    /** Hashmap for files staged for addition. */
    private HashMap<String, String> _addition;

    public Stage() {
        _addition = new HashMap<>();
        _removal = new HashMap<>();
    }



    public HashMap<String, String> getAddition() {
        return _addition;
    }

    public HashMap<String, String> getRemoval() {
        return _removal;
    }

    public void addToStage(String fileName) throws IOException {
        File currentWorkingVersion = new File(CWD + "/" + fileName);
        if (currentWorkingVersion.exists()) {
            String currentWorkingVersionContents =
                    Utils.readContentsAsString(currentWorkingVersion);
            String latestCommit =
                    Utils.readContentsAsString(new File(GITLET + "/head.txt"));
            File currentCommitVersion =
                    new File(".gitlet/commits/" + latestCommit + ".txt");
            Commit currentCommit =
                    Utils.readObject(currentCommitVersion, Commit.class);
            if (currentCommit.getData().containsKey(fileName)
                    && currentCommit.getData().get(fileName).equals(
                            currentWorkingVersionContents)) {
                if (_removal.containsKey(fileName)) {
                    _removal.remove(fileName);
                }
            } else {
                if (_removal.containsKey(fileName)) {
                    _removal.remove(fileName);
                }
                if (_addition.containsKey(fileName)) {
                    _addition.replace(fileName, Utils.readContentsAsString(
                            new File(CWD + "/" + fileName)));
                } else {
                    _addition.put(fileName, Utils.readContentsAsString(
                            new File(CWD + "/" + fileName)));
                }
            }
        } else {
            System.out.println("File does not exist.");
        }
        saveStageToFile();
    }

    public void clear() {
        _addition.clear();
        _removal.clear();
    }

    public void stageForRemoval(String name, String contents)
            throws IOException {
        _removal.put(name, contents);
        saveStageToFile();
    }


    public void saveStageToFile() throws IOException {
        File stage = new File(GITLET + "/stage.txt");
        Utils.writeObject(stage, this);
    }


}
