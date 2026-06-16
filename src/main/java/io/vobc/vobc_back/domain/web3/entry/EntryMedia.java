package io.vobc.vobc_back.domain.web3.entry;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EntryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_media_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id")
    private Entry entry;

    private String url;
    private String type;
    private String uuid;

    @CreatedDate
    @Column(updatable = false)
    private Long createdAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedDate
    private Long updatedAt;

    @LastModifiedBy
    private Long updatedBy;

}


