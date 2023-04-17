package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;


public class Commit implements Serializable {

    /** The message of the commit. */
    private String _message;

    /** The timestamp of the commit. */
    private String _timestamp;

    /** The parent's metadata. */
    private String _parent;

    /** The counter of the commit. */
    private String _counter;

    /** Hashmap of all the files and content of the commit. */
    private HashMap<String, String> _data;

    /** The parent of the given branch when merged. */
    private String _mergeParent;

    /** The directory for all the commits. */
    static final File COMMIT_FOLDER = new File(".gitlet/commits");

    public Commit(String message, String parent) {
        _message = message;
        _parent = parent;
        _data = new HashMap<>();
        _mergeParent = null;
        if (parent == null) {
            java.util.Date date = new java.util.Date(0);
            String stringDate =
                    String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
            _timestamp = stringDate;
            _message = "initial commit";
            _counter = "0";
        } else {
            java.util.Date date = new java.util.Date();
            String stringDate =
                    String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
            _timestamp = stringDate;
            getParentData();
        }
    }

    public void getParentData() {
        File fileParent = new File(".gitlet/commits/" + _parent + ".txt");
        Commit commitParent = Utils.readObject(fileParent, Commit.class);
        _data = commitParent.getData();
        _counter = commitParent.getCount() + "1";
    }

    public Commit(Commit clone, String message, String parent) {
        _message = message;
        _parent = parent;
    }

    public void saveCommitToFile() {
        COMMIT_FOLDER.mkdir();
        File commit = new File(".gitlet/commits/" + getMetadata() + ".txt");
        Utils.writeObject(commit, this);
    }
    public String getMetadata() {
        return Utils.sha1(getMessage(), getTimestamp(), "commit", _counter);
    }

    public Commit getCommitFromFile(String uid) {
        File commit = new File(".gitlet/commits/" + uid + ".txt");
        return Utils.readObject(commit, Commit.class);
    }

    public HashMap<String, String> getData() {
        return _data;
    }

    public void mergeStageData(Stage stage) {
        HashMap<String, String> stageData1 = stage.getAddition();
        HashMap<String, String> stageData2 = stage.getRemoval();

        if (!stageData1.isEmpty()) {
            stageData1.forEach((String k, String v) -> {
                if (_data.containsKey(k)) {
                    _data.replace(k, v);
                } else {
                    _data.put(k, v);
                }
            });
        }

        if (!stageData2.isEmpty()) {
            stageData2.forEach((String k, String v) -> {
                if (_data.containsKey(k)) {
                    _data.remove(k);
                }
            });
        }
    }

    public String getMessage() {
        return _message;
    }

    public String getTimestamp() {
        return _timestamp;
    }

    public String getParent() {
        return _parent;
    }

    public void setMergeParent(String commitID) {
        _mergeParent = commitID;
    }

    public String getMergeParent() {
        return _mergeParent;
    }

    public String getCount() {
        return _counter;
    }

}
