package com.mycompany.microservice.api.mappers.base;

import com.mycompany.microservice.api.mappers.annotations.ToEntity;
import java.util.Collection;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * The interface Base mapper management.
 *
 * @param <E> the type parameter Entity
 * @param <CR> the type parameter CreateRequest
 * @param <UR> the type parameter UpdateRequest
 * @param <R> the type parameter Response
 */
public interface ManagementBaseMapper<E, CR, UR, R> {

  @ToEntity
  E toEntity(CR request);

  @ToEntity
  E update(UR request, @MappingTarget E entity);

  @ToEntity
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  E patch(UR request, @MappingTarget E entity);

  R toManagementResponse(E entity);

  Collection<R> toManagementResponse(Collection<E> entity);
}