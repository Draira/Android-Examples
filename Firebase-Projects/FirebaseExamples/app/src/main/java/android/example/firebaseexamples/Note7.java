package android.example.firebaseexamples;

import com.google.firebase.firestore.Exclude;

public class Note7 {
    private String documentId;
    private String title;
    private String description;

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Note7() {
    }

    public Note7(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

