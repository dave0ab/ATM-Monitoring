package com.zemenbank.atmMonitoring.mappers;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class AtmsResponseMapper {

    public static final AtmsResponseMapper INSTANCE = Mappers.getMapper(AtmsResponseMapper.class);


    @InheritInverseConfiguration
    public abstract ATMS mapToAtms(AtmsResponse atmsResponse);

}
