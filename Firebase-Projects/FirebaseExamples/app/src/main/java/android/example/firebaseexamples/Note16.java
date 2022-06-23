package android.example.firebaseexamples;

import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Map;

public class Note16 {
    private String documentId;
    private String title;
    private String description;
    private int priority;
    Map<String, Boolean> tags;

    public Note16() {
    }

    public Note16(String title, String description, int priority, Map<String, Boolean> tags) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.tags = tags;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }
}
