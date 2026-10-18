package qc.levis.contrat.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Couple question / réponse (hachée) pour la question secrète dynamique. */
@Embeddable
public class SecurityQuestion {

    @Column(name = "question", nullable = false)
    private String question;

    @JsonIgnore
    @Column(name = "reponse_hash", nullable = false)
    private String reponseHash;

    public SecurityQuestion() {}

    public SecurityQuestion(String question, String reponseHash) {
        this.question = question;
        this.reponseHash = reponseHash;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponseHash() { return reponseHash; }
    public void setReponseHash(String reponseHash) { this.reponseHash = reponseHash; }
}
