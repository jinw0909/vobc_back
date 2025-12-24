package io.vobc.vobc_back.dto.publisher;

import io.vobc.vobc_back.domain.publisher.Publisher;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PublisherForm {

    public Long id;
    public String name;
    public String introduction;
    public String logo;

    public PublisherForm(Publisher publisher) {
        this.id = publisher.getId();
        this.name = publisher.getName();
        this.introduction = publisher.getIntroduction();
    }

}
