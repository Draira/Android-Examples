package android.example.firebaseexamples;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Note15 {
    private String documentId;
    private String title;
    private String description;
    private int priority;
    List <String> tags;

    public Note15() {
    }

    public Note15(String title, String description, int priority, List <String> tags) {
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

    public List<String> getTags() {
        return tags;
    }
}
