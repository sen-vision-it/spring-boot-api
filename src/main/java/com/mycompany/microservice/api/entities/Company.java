package com.mycompany.microservice.api.entities;

import static com.mycompany.microservice.api.entities.Company.TABLE_NAME;
import static com.mycompany.microservice.api.utils.StringUtils.numericOnly;

import com.mycompany.microservice.api.entities.base.BaseEntity;
import io.hypersistence.utils.hibernate.id.BatchSequenceGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@Table(name = TABLE_NAME, schema = "public")
public class Company extends BaseEntity {
  public static final String TABLE_NAME = "company";

  @Serial private static final long serialVersionUID = 2137607105409362080L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company")
  @GenericGenerator(
      name = "company",
      type = BatchSequenceGenerator.class,
      parameters = {
        @Parameter(name = "sequence", value = "company_id_seq"),
        @Parameter(name = "fetch_size", value = "1")
      })
  private Long id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column private String name;

  @Column(unique = true)
  private String officialName;

  @Column(nullable = false, unique = true)
  private String federalTaxId;

  @Column(unique = true)
  private String stateTaxId;

  @Column private String phone;
  @Column private String email;

  @Column private String addressStreet;
  @Column private String addressStreetNumber;
  @Column private String addressComplement;
  @Column private String addressCityDistrict;

  @Column private String addressPostCode;
  @Column private String addressCity;
  @Column private String addressStateCode;
  @Column private String addressCountry;

  @Column private BigDecimal addressLatitude;
  @Column private BigDecimal addressLongitude;

  @Column private Boolean isClient;
  @Column private Boolean isManagement;
  @Column private Boolean isInternal;

  public Company(final Long id) {
    this.id = id;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  @PrePersist
  @PreUpdate
  private void preSave() {
    this.phone = numericOnly(this.phone);
    this.federalTaxId = numericOnly(this.federalTaxId);
    this.stateTaxId = numericOnly(this.stateTaxId);
    this.addressPostCode = numericOnly(this.addressPostCode);
  }

  @Override
  public String toString() {
    return "Company{"
        + "id="
        + this.id
        + ", slug='"
        + this.slug
        + "', name='"
        + this.name
        + "', officialName='"
        + this.officialName
        + "', federalTaxId='"
        + this.federalTaxId
        + "', stateTaxId='"
        + this.stateTaxId
        + "', phone='"
        + this.phone
        + "', email='"
        + this.email
        + "', addressStreet='"
        + this.addressStreet
        + "', addressStreetNumber='"
        + this.addressStreetNumber
        + "', addressComplement='"
        + this.addressComplement
        + "', addressCityDistrict='"
        + this.addressCityDistrict
        + "', addressPostCode='"
        + this.addressPostCode
        + "', addressCity='"
        + this.addressCity
        + "', addressStateCode='"
        + this.addressStateCode
        + "', addressCountry='"
        + this.addressCountry
        + "', addressLatitude="
        + this.addressLatitude
        + ", addressLongitude="
        + this.addressLongitude
        + ", isClient="
        + this.isClient
        + ", isManagement="
        + this.isManagement
        + ", isInternal="
        + this.isInternal
        + "', createdBy="
        + this.getCreatedBy()
        + ", updatedBy="
        + this.getUpdatedBy()
        + "', createdAt="
        + this.getCreatedAt()
        + ", updatedAt="
        + this.getUpdatedAt()
        + '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final Company other)) {
      return false;
    }
    return this.getId() != null && this.getId().equals(other.getId());
  }

  @Override
  public int hashCode() {
    return this.getClass().hashCode();
  }

  public boolean is(final String slug) {
    return StringUtils.isNotBlank(this.slug) && this.slug.equals(slug);
  }
}