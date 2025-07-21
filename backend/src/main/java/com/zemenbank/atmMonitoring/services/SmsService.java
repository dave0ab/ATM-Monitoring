package com.zemenbank.atmMonitoring.services;



public interface SmsService {

    void sendSms(String messageString , String PhoneNumber);
}
