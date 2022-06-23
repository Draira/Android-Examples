package android.example.firebaseexamples;

import com.google.firebase.firestore.Exclude;

public class Note8 {
    private String documentId;
    private String title;
    private String description;
    private int priority;

    public Note8() {
    }

    public Note8(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
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
}
