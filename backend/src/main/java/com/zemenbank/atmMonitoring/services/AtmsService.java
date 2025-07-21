package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import org.springframework.data.domain.Page;

public interface AtmsService {

    Page<AtmsResponse> getAllAtms(int pageNumber, int pageSize, String status, String search , String sort, String sortBy, String problem);

    Page<AtmsResponse> getAllAtmsSmartBranch(int pageNumber, int pageSize);


    AtmsResponse getAtmById(String atmId);


    String addRemark(String atmId, String remark);

    AtmsResponse getAtmBYIdSmartBranch(String atmId);
}
